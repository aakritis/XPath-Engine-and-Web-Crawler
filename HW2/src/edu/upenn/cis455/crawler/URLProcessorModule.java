package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserChannelEntity;
import edu.upenn.cis455.storage.WebContentEntity;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class URLProcessorModule {

	// class attributes 
	boolean is_html;

	private ArrayList<String> extracted_href;
	String absolute_url = null;
	/**
	 * Default Constructor
	 */
	public URLProcessorModule() {
		this.is_html = false;
		extracted_href = new ArrayList<String>();
	}
	/**
	 * function to process the current webpage 
	 * extract urls from href tags of html pages 
	 * check urls matching user channels
	 * eliminate duplicates and add new urls to url_queue
	 * @param webpage
	 * @throws MalformedURLException 
	 */
	public void process_webpage(WebContentEntity webpage) throws MalformedURLException {
		// base case if the passed webpage is empty

		if (webpage == null)
			// crawling stops for the current page and is returned to ThreadCrawler page
			return;
		// if it is a HTML page
		// // // System.out.println("[Output from log4j] Content Type of WebPage + " + webpage.get_content_type());
		if (webpage.get_content_type().split(";")[0].equals("text/html")) {
			this.is_html = true;
			// extract href links from the HTML page
			extracted_href = new ArrayList<String>();
			this.extract_href_links(webpage);
			// // System.out.println("[Output from log4j] Extracting HREF links from html");
		}
		else { 
			// if the content type is of xml type
			this.is_html = false;
			this.extract_xml_urls(webpage);
		}

		if (this.is_html) {
			this.insert_extracted_urls();
		}

	}
	/**
	 * for XML documents 
	 * @param webpage
	 */
	public void extract_xml_urls (WebContentEntity webpage) {
		this.absolute_url = webpage.get_absolute_url();
		Document dom = this.get_dom_from_url(webpage.get_url_content());
		ArrayList<UserChannelEntity> channels = DBWrapper.get_all_channels();
		for (UserChannelEntity channel: channels) {
			ArrayList<String> array_xpaths = channel.get_xpaths();
			String[] xpaths = array_xpaths.toArray(new String[array_xpaths.size()]);
			XPathEngineImpl filter = new XPathEngineImpl();
			filter.setXPaths(xpaths);
			boolean[] matched = filter.evaluate(dom);
			for (int pathid = 0; pathid < xpaths.length; pathid++)
				if (matched[pathid]){
					// System.out.println("[DEBUG] is_matched ");
					channel.add_extend_urls(this.absolute_url);
					DBWrapper.add_channel(channel);
					// System.out.println("[DEBUG] extended urls + " + channel.get_extend_urls().get(0));
				}
		}
	} 
	/**
	 * to get DOM object
	 * @param doc
	 * @return
	 */
	public Document get_dom_from_url(String doc){
		Document dom = null;
		String encode = "UTF-8";
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(doc.getBytes(encode));
			dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(byteStream);
			return dom;
		} 
		catch (SAXException e) {
			//e.printStackTrace();
		} 
		catch (ParserConfigurationException e) {
			//e.printStackTrace();
		} 
		catch (IOException e) {
			//e.printStackTrace();
		}
		return dom;
	}

	/**
	 * Parse through the html pages and extract links
	 * @param webpage
	 * @throws MalformedURLException 
	 */
	public void extract_href_links (WebContentEntity webpage) throws MalformedURLException {
		this.absolute_url = webpage.get_absolute_url();
		// regular expression to search for href tags
		String reg_ex = "<a\\s+href\\s*=\\s*[\\'\\\"](.*?)[\\\"]";
		Pattern pattern = Pattern.compile(reg_ex,Pattern.CASE_INSENSITIVE);
		Matcher link_match = pattern.matcher(webpage.get_url_content());
		URL absoluteURLObj = new URL(this.absolute_url);
		// till there is a match for <a href /> tag
		while (link_match.find()) {
			String site_link = link_match.group(1).trim();
			// ignore links like javascript, mailto etc
			if ( site_link.equals("#") || site_link.toLowerCase().startsWith("javascript") || site_link.toLowerCase().startsWith("mailto"))
				continue;
			// for proper absolute urls 
			if (site_link.startsWith("http://"))
				this.extracted_href.add(site_link);
			else {
				// when given relative urls -- perform normalization
				String abs_path = absoluteURLObj.getPath();
				if(abs_path.endsWith("html") || abs_path.endsWith("htm"))
					abs_path = abs_path.substring(0,abs_path.lastIndexOf("/"));
				
				if (site_link.startsWith("/")) {
					if(! abs_path.endsWith("/"))
						site_link = absoluteURLObj.getProtocol() + "://" + absoluteURLObj.getHost() + abs_path + site_link;
					else 
						site_link = absoluteURLObj.getProtocol() + "://" + absoluteURLObj.getHost() + abs_path.substring(0, abs_path.length()-1) + site_link;
				}
					
				else { 
					if(! abs_path.endsWith("/"))
						site_link = absoluteURLObj.getProtocol() + "://" + absoluteURLObj.getHost() + abs_path + "/" + site_link;
					else 
						site_link = absoluteURLObj.getProtocol() + "://" + absoluteURLObj.getHost() + abs_path + site_link;
				}
	
				// // System.out.println("[Output from log4j] Site link to add +" + site_link);
				this.extracted_href.add(site_link);
			}
		}
	}
	/**
	 * to remove duplicates from Arraylist and add extracted urls to queue
	 */
	public void insert_extracted_urls () {
		for (String url : this.extracted_href)
			// check if contains function has to be created in synchronized block for shared queue
			if (XPathCrawler.url_queue.contains(url))
				continue;
			else{
				WebURLQueue queue_obj = new WebURLQueue();
				queue_obj.addToQueue(url);
			}
	}
}
