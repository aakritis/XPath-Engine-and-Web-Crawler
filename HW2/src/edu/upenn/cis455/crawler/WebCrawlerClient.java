package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * Crawler Client to access Url/extract contents from Url/ check Robots.txt
 * @author cis455
 *
 */
public class WebCrawlerClient {

	protected URL url_obj;
	protected String hostname;
	protected int port_no;
	protected String content_type = null;

	private String curr_path;
	private String curr_url;
	protected boolean is_param_set = true;

	// default constructor
	public WebCrawlerClient() {

	}

	// parameterized constructer to create the DOM object from url
	public WebCrawlerClient(String url) {
		try {
			this.url_obj = new URL(url);
			this.hostname = url_obj.getHost();
			this.curr_path = url_obj.getPath();
			this.set_curr_url(url);
			// handling root directory case
			if (curr_path == "") 
				curr_path = "/";
			this.port_no = url_obj.getPort();
			// if port no is not specified
			if(this.port_no == -1) 
				this.port_no = 80;

			//// // System.out.println("[Output from log4j] Data from the parameterized constructor url_obj + " + this.url_obj);
			//// // System.out.println("[Output from log4j] Data from the parameterized constructor hostname + " + this.hostname);
			//// // System.out.println("[Output from log4j] Data from the parameterized constructor current path + " + this.curr_path);
			//// // System.out.println("[Output from log4j] Data from the parameterized constructor port_no + " + this.port_no);
		} 
		catch (MalformedURLException e) {
			// // System.out.println("[Output from log4j] Error while setting the Constructor for XPathEngineClientSocket + " + e.getMessage());
			this.is_param_set = false;
		}
	}

	/**
	 * Getter function for hostname parameter 
	 * @return
	 */
	public String get_hostname () {
		return this.hostname;
	}

	/**
	 * function to extract contents from webpage
	 * @param url
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public String extract_content_from_url (String url) throws IOException {
		// System.out.println("[DEBUG] GET "+url);
		// // System.out.println("[Output from log4j] Is Param Set ? + " +  this.is_param_set);
		if (! this.is_param_set)
			return null;

		URL curr_url = new URL(url);
		if (curr_url.getProtocol().equalsIgnoreCase("https")){
			//// // System.out.println("[Output from log4j] Before HTTPS Connection");
			HttpsURLConnection con = null;
			int status = 0;
			con = (HttpsURLConnection) curr_url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "cis455crawler");
			con.setInstanceFollowRedirects(false);
			con.setRequestProperty("Host" , this.hostname + ":" + this.port_no );
			status = con.getResponseCode();
			//// // System.out.println("[Output from log4j] After HTTPS Connection status +" + status);
			if (status!=200 && status!=301 && status!=304)
				return null;
			this.content_type = con.getContentType().split(";")[0];

			BufferedReader in_obj = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer content_data = new StringBuffer();
			String curr_line;
			boolean first_line = true;
			//String output = "";
			while((curr_line = in_obj.readLine()) != null){
				if (first_line){
					content_data.append(curr_line);
					first_line = false;
					continue;
				}
				content_data.append("\n" + curr_line);
			}
			in_obj.close();			
			String content = content_data.toString();
			//// // System.out.println("[Output from log4j] After HTTPS Connection content +" + content);
			return content;
		}
		else {
			// // // System.out.println("[Output from log4j] Before Socket");
			Socket socket = new Socket(this.hostname, this.port_no);
			// // // System.out.println("[Output from log4j] After Socket");
			// // // System.out.println("[Output from log4j] Before Sending Request");

			// sending GET /path HTTP/1.1 request
			PrintWriter out_obj = new PrintWriter(socket.getOutputStream(), true);
			out_obj.write("GET " + this.curr_path + " HTTP/1.1\r\n");
			out_obj.write("User-Agent: cis455crawler\r\n");
			out_obj.write("Host: " + this.hostname + ":" + this.port_no + "\r\n");
			out_obj.write("Connection: close\r\n\r\n");
			out_obj.flush();

			// // // System.out.println("[Output from log4j] After Sending Request");

			// reading data from response 
			BufferedReader in_obj = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			StringBuffer content_data = new StringBuffer();
			String curr_line = in_obj.readLine();

			// check for any redirect response
			/**
			 * check if we need to return the body content of the redirected url ? 
			 * set current url in client to the redirected url ?
			 * or below loop is not required ?
			 * or if for 301/302 request only head is read 
			 * and we are not concerned with the body content
			 * as null is returned in the next if loop   
			 */
			if(curr_line.contains("301") || curr_line.contains("302")){
				String redirect_url = this.fetch_redirect_url(in_obj);
				this.fetch_head_response_url(redirect_url);
			}

			// // // System.out.println("[Output from log4j] After Sending Request, first line +" + curr_line);

			// if the data is not retrieved properly
			if(!(curr_line.endsWith("200 OK") || curr_line.contains("301") || curr_line.contains("302"))) 
				return null;

			// reading response header line-wise to get content-type
			curr_line = in_obj.readLine();
			while((curr_line != null && !curr_line.equals(""))) {
				String hash_key = curr_line.split(":")[0].trim();
				if(hash_key.equalsIgnoreCase("content-type"))
					this.content_type = curr_line.split(":")[1].trim();
				curr_line = in_obj.readLine();
			}

			// reading body content from the header to get the document 
			curr_line = in_obj.readLine();
			while(curr_line != null){
				content_data.append(curr_line + "\r\n");
				curr_line = in_obj.readLine();
			}
			// closing socket and print writer objects 
			out_obj.close();
			in_obj.close();
			socket.close();
			String content = content_data.toString();
			// // // System.out.println("[Output from log4j] In client socket, File content extracted from obj + " + content);
			return content;
		}		
	} 

	/**
	 * function to extract dom object from url differentiating xml/html files 
	 * @param url
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Document extract_dom_from_url(String url) throws UnknownHostException, IOException, InterruptedException{

		Document dom_obj = null;
		// extracting body content from url 
		String body_content = this.extract_content_from_url(url);

		// // System.out.println ("[Output from log4j] In client socket , after extracting content from url + " + body_content);

		if (body_content == null)
			return null;

		// // System.out.println ("[Output from log4j] In client socket , after extracting content from url, content type is  + " + this.content_type);
		// checking whether in coming reponse file is a HTML or XML file type
		if(this.content_type.endsWith("html") || this.content_type.endsWith("htm")){
			Tidy tidy = new Tidy();
			tidy.setDocType("omit");
			tidy.setTidyMark(false);

			// using string writer/ reader object to parse the current html file to dom obj 
			StringWriter writer = new StringWriter(body_content.length());
			dom_obj = tidy.parseDOM(new StringReader(body_content), writer);
			return dom_obj;
		}
		else if(this.content_type.endsWith("xml")){
			try {
				dom_obj = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.url_obj.openStream());
				return dom_obj;
			} 
			catch (SAXException e) {
				e.printStackTrace();
			} 
			catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return dom_obj;
	}	
	/**
	 * to extract contents from the robots.txt for the given domain
	 * @return
	 * @throws IOException
	 */
	public String fetch_robots_rules() throws IOException{

		String robot_url = url_obj.getProtocol() + "://" + url_obj.getHost() + "/robots.txt";
		// // System.out.println("[Output from log4j] Fetch robots.txt + " + robot_url);
		return this.extract_content_from_url(robot_url);
	}

	private String getDate(long time)
	{
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String ret_date = sdf.format(date).toString();
		return ret_date;
	}

	/**
	 * Function to send HEAD request and return response header 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public HashMap<String, String> fetch_head_response_url (String request_url) throws IOException{
		// System.out.println("[DEBUG] HEAD "+request_url);
		// check if the URL is a malformed url 
		if(! this.is_param_set) 
			return null;
		URL curr_url = new URL(request_url);
		if (curr_url.getProtocol().equalsIgnoreCase("https")){
			//// // System.out.println("[Output from log4j] Before HTTPS Connection");
			HttpsURLConnection con = null;
			int status = 0;
			con = (HttpsURLConnection) curr_url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "cis455crawler");
			con.setInstanceFollowRedirects(false);
			con.setRequestProperty("Host" , this.hostname + ":" + this.port_no );
			status = con.getResponseCode();
			//// // System.out.println("[Output from log4j] After HTTPS Connection status +" + status);
			if (status!=200 && status!=301 && status!=304) {
				
				return null;
			}
			
			HashMap<String, String> response_headers = new HashMap<String, String>();
			//// // System.out.println("In Head, Content-type + " + con.getContentType() + "	Content-length + " + con.getContentLength() + "		Last-Modified + " + con.getLastModified());
			response_headers.put("content-type", ""+con.getContentType().split(";")[0]);
			response_headers.put("content-length", ""+con.getContentLength());
			response_headers.put("last-modified",""+getDate(con.getLastModified()));
			if(con.getHeaderField("Location") != null){
				response_headers.put("location", ""+con.getHeaderField("Location"));
			}
			return response_headers;
		}
		else {
			Socket clientsocket = new Socket(this.hostname, this.port_no);

			// send HEAD request 
			PrintWriter out_obj = new PrintWriter(clientsocket.getOutputStream(), true);
			out_obj.write("HEAD " + this.curr_path + " HTTP/1.1\r\n");
			out_obj.write("User-Agent: cis455crawler\r\n");
			out_obj.write("Host: " + this.hostname + ":" + this.port_no + "\r\n");
			out_obj.write("Connection: close\r\n\r\n");
			out_obj.flush();

			// read HEAD response header
			BufferedReader in_obj = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
			HashMap<String, String> response_headers = new HashMap<String, String>();
			String thisLine = in_obj.readLine(); 
			/*
			// check for any redirect url
			if(thisLine.contains("301") || thisLine.contains("302")){
				String redirect_url = this.fetch_redirect_url(in_obj);
				// recursive call for fetching header response for redirected url
				// // System.out.println("[Output from log4j] Calling fetch_head_response_url for redirect url +" + redirect_url);
				return fetch_head_response_url(redirect_url);
			}*/

			// if the response is not proper
			if(!thisLine.endsWith("200 OK") || !thisLine.contains("301") || !thisLine.contains("302")) 
				return null;

			try {
				// read head reponse line wise 
				thisLine = in_obj.readLine();
				while((thisLine != null && !thisLine.equals(""))){ 
					String key = thisLine.split(":")[0].trim();
					String value = thisLine.split(":")[1].trim();
					// // System.out.println("[Output from log4j] Adding <key,value> to response header hash map + <" + key + "," + value + ">");
					response_headers.put(key.toLowerCase(), value);
					thisLine = in_obj.readLine();
				}
			}
			finally {
				out_obj.close();
				in_obj.close();
				clientsocket.close();
			}
			return response_headers;
		}
	}
	/**
	 * to get the redirected url if the response header has status 301/302
	 * @param in_obj
	 * @return
	 * @throws IOException
	 */
	private String fetch_redirect_url(BufferedReader in_obj) throws IOException {
		// read next line in the response header 
		String curr_line = in_obj.readLine();
		// search for the redirect location in the in_obj object 
		while (curr_line != null  && !curr_line.equals(""))
			if (curr_line.contains(":")) {
				String[] curr_data = curr_line.split(":",1);
				String key = curr_data[0].trim();
				String value = curr_data[1].trim();
				// // System.out.println("[Output from log4j] In redirect url, <key,value> is + <" + key + "," + value + ">");
				if (key.equalsIgnoreCase("Location"))
					// return value which is the url for key Location
					return value;
			}
		return null;
	}

	/**
	 * Getter function for current url processed in client
	 * @return the curr_url
	 */
	public String get_curr_url() {
		return curr_url;
	}

	/**
	 * Setter function for setting url to process in client 
	 * @param curr_url the curr_url to set
	 */
	public void set_curr_url(String curr_url) {
		this.curr_url = curr_url;
	}
}
