package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.WebContentEntity;

/**
 * Class to check constraints for Politness -- As per 4.3 guidelines 
 * @author Aakriti Singla
 *
 */
public class PolitenessModule {

	// to set up client for various functionalities
	protected WebCrawlerClient crawler_client = null;

	// to access data stored for the current /robots.txt
	RobotsTxtInfo robots = null;

	// hash map to store when to crawl next time for a specific domain
	protected HashMap<String, Long> map_next_domain_crawl;

	// data based parameters
	protected String hostname;
	protected String host_protocol;
	protected String content_type;
	protected double content_length;

	boolean is_reloc = false;
	boolean is_polite = true;
	// store the header response as <key,value> pair from HEAD request
	protected HashMap<String, String> response_headers;

	/**
	 * Default Constructor
	 */
	public PolitenessModule() {
		map_next_domain_crawl =  new HashMap<String, Long>();
		robots = new RobotsTxtInfo();
		hostname = null;
		is_reloc = false;
		is_polite = true;
		// set default protocol type to http/https
		host_protocol = "HTTP";
		content_type = null;
		content_length = 0;
		response_headers = new HashMap<String,String>(); 
	}

	/**
	 * download webcontent for a webpage if crawled after checking politeness criteria
	 * @param serve_url
	 * @return
	 * @throws IOException
	 */
	public WebContentEntity fetch_webpage (String serve_url) throws IOException {
		this.is_reloc = false;
		this.is_polite = true;
		// set up Client object to access protocol 
		crawler_client = new WebCrawlerClient(serve_url);
		this.hostname = crawler_client.get_hostname();
		// check if the request is polite 
		if (! this.is_polite_request(serve_url))
			//return null;
			this.is_polite = false;
		// System.err.println("[DEBUG] Is Polite? + " + serve_url + "		" + is_polite);
		// check if the file is a valid file
		// // // // System.out.println("[Output from log4j] Is Valid File ? + " );
		boolean is_file_valid = this.is_valid_file(serve_url);
		if (! this.is_polite)
			return null;
		if (! is_file_valid)
			return null;
		// if the file passes all the tests download the file content and store in BerkleyDB
		// // // // System.out.println("[Output from log4j] If Yes , Is Valid File  + " );
		WebContentEntity webpage = null;
		// // // // System.out.println("[Output from log4j] Before add webpage ");
		System.out.println("[Display] Downloading :");
		webpage = this.extract_add_webpage();
		// // // // System.out.println("[Output from log4j] After add webpage");
		return webpage;
	}
	/**
	 * to download web page contents and store in the BerkleyDB based on checks
	 * @return
	 * @throws IOException
	 */
	public WebContentEntity extract_add_webpage() throws IOException {
		/**
		 *  check for absolute urls 
		 */
		Date date_modified = null; 
		String absolute_url = crawler_client.get_curr_url();
		if(this.response_headers.containsKey("last-modified")) {
			String last_modified = this.response_headers.get("last-modified");
			date_modified = DBWrapper.get_date_from_header_string(last_modified);
		}
		// check if the webpage already exists
		WebContentEntity webpage = DBWrapper.get_webpage(absolute_url);
		String document_contents = null;
		Date current_date = new Date();
		if (webpage == null) {
			// the webpage doesn't exist, thus, add webpage
			//System.out.println("[Display] Downloading :" + absolute_url);
			document_contents = crawler_client.extract_content_from_url(absolute_url);
			// System.out.println("[Display] Downloading :" + absolute_url);
			ThreadCrawler.num_files_crawled++;
			System.out.println("[Display] Number of files crawled + " + ThreadCrawler.num_files_crawled);
			/**
			 *  initialize the webcontent attributes in constructor
			 *  with data from head request and extracted body content 
			 */			
			webpage = new WebContentEntity (absolute_url, current_date, document_contents, this.content_type.split(";")[0], this.content_length );
			// add webpage to BerkleyDB
			DBWrapper.add_webcontent(webpage);
			return webpage;
		}

		// else if the webpage already exists in the database 
		Date last_crawled_date = webpage.get_last_crawled_date();
		if (last_crawled_date.after(date_modified)) {
			// no need to download the file and extract file from the database
			document_contents = webpage.get_url_content();
			System.out.println("[Display] Not Modified :" + absolute_url);
		}
		else {
			// when the document is modified at later date after storing
			document_contents = crawler_client.extract_content_from_url(absolute_url);
			// delete existing tuple for webpage 
			DBWrapper.delete_webpage(absolute_url);
			// create new tuple for the webpage
			/**
			 *  initialize the webcontent attributes in constructor
			 *  with data from head request and extracted body content 
			 */			
			webpage = new WebContentEntity (absolute_url, current_date, document_contents, this.content_type, this.content_length );
			// add webpage to BerkleyDB
			DBWrapper.add_webcontent(webpage);
		}
		return webpage;
	}
	/**
	 * to send Head request to the server to check for content type and content size 
	 * @param current_url
	 * @return
	 */
	public boolean is_valid_file (String current_url) throws IOException {
		// System.out.print("[Output from log4j] Checking validity for url +" + current_url);

		// create and send HEAD request
		// // // // System.out.println("[Output from log4j] before head request + " + current_url);
		this.response_headers = crawler_client.fetch_head_response_url(current_url);
		// // // // System.out.println("[Output from log4j] after head request + " + response_headers.size());
		// // // // System.out.println("[Output from log4j] After fetching response header in is_valid_file");

		// base case - no response headers recieved from the server 
		if (this.response_headers == null)
			return false;
		// check for location header 
		if (this.response_headers.containsKey("location")) {
			this.is_reloc = true;
			String re_loc = this.response_headers.get("location");
			if(re_loc.startsWith("/")){
				URL url_obj = new URL(current_url);
				String path = url_obj.getPath();
				String abs_reloc;
				if(path.endsWith(".xml") || path.endsWith(".html") || path.endsWith("htm"))
					path = path.substring(0,path.lastIndexOf("/"));
				if(path.endsWith("/"))
					abs_reloc = url_obj.getProtocol() + "://" + url_obj.getHost() + path.substring(0,path.length()-1) + re_loc;
				else
					abs_reloc = url_obj.getProtocol() + "://" + url_obj.getHost() + path + re_loc;
				System.err.println("[Output from log4j] Found Relocation url +" + abs_reloc);
				WebURLQueue queue = new WebURLQueue();
				queue.addToQueue(abs_reloc);
				return false;
			} 
			else {
				WebURLQueue queue = new WebURLQueue();
				queue.addToQueue(re_loc);
				return false;
			}
		}
		// if content type is not present in response header 
		if (!(this.response_headers.containsKey("content-type")))
			return false;
		// check valid content types
		this.content_type = this.response_headers.get("content-type");
		// if valid content type
		if (!content_type.equals("text/xml") && !content_type.equals("text/html") && !content_type.endsWith("+xml") && !content_type.equals("application/xml"))
			return false;
		// // // // System.out.println("[Output from log4j] Chheck till Content type");
		// check content-length exists
		if (this.response_headers.containsKey("content-length")) {
			this.content_length = Double.parseDouble(this.response_headers.get("content-length"));
			// checking allowed content-length for the document
			if (this.content_length > (XPathCrawler.maxsize_doc * 1024 * 1024))
				return false;

		}
		// // // // System.out.println("[Output from log4j] Chheck till Content Length");
		return true;
	}

	/**
	 * check if the requested url follows all norms defined in the /robots.txt for domain
	 * @param current_url
	 * @return
	 * @throws IOException
	 */
	public boolean is_polite_request (String current_url) throws IOException {
		// System.out.print("[Output from log4j] Checking politeness parameters for url +" + current_url);

		// if the request robots.txt is not already stored in internal memory 
		// // // System.out.println("[Output from log4j] Checking hostname + " + this.hostname);
		if (! robots.crawlContainAgent(this.hostname)) {
			// // // System.out.println("[Output from log4j] Extracting robots.txt");
			// crawling for the first name the given domain name 
			String robots_rules = crawler_client.fetch_robots_rules();
			//// // // System.out.println("[Output from log4j] Extracting robots.txt- Data is +" + robots_rules);
			// base case without any rules in robots.txt
			if (robots_rules == null) {
				// // // System.out.println("[Output from log4j] Robots.txt is empty for + " + this.hostname);
				return true;
			}
			// parse robots.txt to store rules
			// // // System.out.println("[Output from log4j] Before Parse Robots");
			this.parse_robots_rules(robots_rules);

			// get crawl_delay for current host name
			// // // System.out.println("[Output from log4j] Before Crawl delay data");
			Long delay = robots.getCrawlDelay(this.hostname);
			//// // // System.out.println("[Output from log4j] After Crawl delay data + " + delay);
			Long last_crawled = (delay * 1000) + System.currentTimeMillis();
			// // // System.out.println("[Output from log4j] Before Last Crawl");
			this.map_next_domain_crawl.put(this.hostname, last_crawled);
			// // // System.out.println("[Output from log4j] After Last Crawl + " + last_crawled);
		}
		// // // System.out.println("[Output from log4j] After Robots.txt");
		// if crawled frequently as per the crawl delay specified , set thread to sleep mode
		Long delay = robots.getCrawlDelay(this.hostname);
		Long time_gap = delay - System.currentTimeMillis();
		if(time_gap > 0){
			// // // System.out.println("[Output from log4j] Thread in wait state because of crawl delay");
			WebURLQueue urlQueue = new WebURLQueue();
			urlQueue.addToQueue(current_url);
			return false;
		}
		// update last crawled time for 
		Long last_crawled = (delay * 1000) + System.currentTimeMillis(); 
		// // // // System.out.println("[Output from log4j] After Last Crawl + " + last_crawled);
		this.map_next_domain_crawl.put(hostname, last_crawled);

		// check if the url is allowed 
		ArrayList<String> domain_allowed_links = robots.getAllowedLinks(this.hostname);
		if (domain_allowed_links != null)
			for (String allowed : domain_allowed_links) {
				//// // // System.out.println("[Output from log4j] Comparing allowed url + " + allowed + " with current url + " + current_url);
				if (allowed.equals("/"))
					// allowed all urls to the cis455crawler
					return true;
				URL url_obj = new URL(current_url);
				String compare_url = url_obj.getPath();
				if (compare_url.startsWith(allowed)) {
					if(compare_url.length() == allowed.length())
						return true;
					else if(compare_url.charAt(allowed.length()-1) == '/')
						return true;
				}
			}
		// // // // System.out.println("[Output from log4j] After Allowed URLS");
		// check if the url is disallowed 
		ArrayList<String> domain_disallowed_links = robots.getDisallowedLinks(this.hostname);
		if (domain_disallowed_links != null)
			for (String disallowed : domain_disallowed_links) {
				//// // // System.out.println("[Output from log4j] Comparing disallowed url + " + disallowed + " with current url + " + current_url);
				if (disallowed.equals("/"))
					// disallowed all urls to the cis455crawler
					return false;
				URL url_obj = new URL(current_url);
				String compare_url = url_obj.getPath();
				//System.out.println("[DEBUG] compare_url +" + compare_url + "	disallowed +" + disallowed);
				if (compare_url.startsWith(disallowed)) {
					//System.err.println("[DEBUG] compare_url +" + compare_url + "	disallowed +" + disallowed);
					if(compare_url.length() == disallowed.length())
						return false;
					else if(compare_url.charAt(disallowed.length()-1) == '/')
						return false;
				}
			}
		// // // // System.out.println("[Output from log4j] After Disllowed URLS");
		return true;
	}

	/**
	 * Set data for hashmaps storing rules for allow, disallow, crawlerdelay
	 * @param robots_rules
	 */
	public void parse_robots_rules (String robots_rules) {
		String robot_user = "";
		ArrayList<String> allowed_links = new ArrayList<String>();
		ArrayList<String> disallowed_links = new ArrayList<String> ();
		Long delay = (long) 0;
		//// // // System.out.println("[Output from log4j] Inside Parse Robots");
		// pattern to match / parse thru the given robots.txt string
		Pattern pattern = Pattern.compile("(\\S+)(:[ ]?)(\\S+)([ ]?)");
		//// // // System.out.println("[Output from log4j] After Compile");
		Matcher matcher_text = pattern.matcher(robots_rules);
		//// // // System.out.println("[Output from log4j] After Match");
		while(matcher_text.find()){

			// extracting <key, value> pair from robots.txt
			//// // // System.out.println("[Output from log4j] size + " + matcher_text.groupCount());
			String key = matcher_text.group(1);
			//// // // System.out.println("[Output from log4j] key + " + key);
			String value = matcher_text.group(3);
			//// // // System.out.println("[Output from log4j] value + " + value);
			//// // // System.out.println("[Output from log4j] Extracting <key,value> pair from robots.txt + <" + key + "," + value + ">");

			if(key.equalsIgnoreCase("User-agent")) {
				robot_user = value;
				// precedence for cis455crawler if found after *
				if (robot_user.equals("cis455crawler")) {
					allowed_links = new ArrayList<String>();
					disallowed_links = new ArrayList<String>();
				}
			}
			else if (key.equalsIgnoreCase("Allow")) {
				if (robot_user.equals("cis455crawler") || robot_user.equals("*"))
					allowed_links.add(value);
			}
			else if (key.equalsIgnoreCase("Disallow")) {
				if (robot_user.equals("cis455crawler") || robot_user.equals("*"))
					disallowed_links.add(value);
			}			
			else if (key.equalsIgnoreCase("Crawl-delay")) {
				if (robot_user.equals("cis455crawler") || robot_user.equals("*"))
					delay = (long) Integer.parseInt(value);
			}
		}

		// adding allowed links to hash map in RobotsTxtInfo;
		if(allowed_links.size() > 0) {
			for(String allowed : allowed_links)
				robots.addAllowedLink(this.hostname, allowed);
		}
		// adding allowed links to hash map in RobotsTxtInfo
		if(disallowed_links.size() > 0) {
			for(String disallowed : disallowed_links) 
				robots.addDisallowedLink(this.hostname, disallowed);
		}
		// adding crawler delay
		robots.addCrawlDelay(this.hostname, delay);
		//// // // System.out.println("[Output from log4j] After robots txt processing");
	}
}
