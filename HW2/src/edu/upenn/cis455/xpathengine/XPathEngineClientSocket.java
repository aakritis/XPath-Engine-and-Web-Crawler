package edu.upenn.cis455.xpathengine;

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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;


public class XPathEngineClientSocket {

	protected URL url_obj;
	protected String hostname;
	protected int port_no;
	protected String content_type = null;

	private String curr_path;

	protected boolean is_param_set = true;

	// default constructor
	public XPathEngineClientSocket() {

	}

	// parameterized constructer to create the DOM object from url
	public XPathEngineClientSocket(String url) {
		try {
			this.url_obj = new URL(url);
			this.hostname = url_obj.getHost();
			this.curr_path = url_obj.getPath();
			// handling root directory case
			if (curr_path == "") 
				curr_path = "/";
			this.port_no = url_obj.getPort();
			// if port no is not specified
			if(this.port_no == -1) 
				this.port_no = 80;
			
			System.out.println("[Output from log4j] Data from the parameterized constructor url_obj + " + this.url_obj);
			System.out.println("[Output from log4j] Data from the parameterized constructor hostname + " + this.hostname);
			System.out.println("[Output from log4j] Data from the parameterized constructor current path + " + this.curr_path);
			System.out.println("[Output from log4j] Data from the parameterized constructor port_no + " + this.port_no);
 		} 
		catch (MalformedURLException e) {
			System.out.println("[Output from log4j] Error while setting the Constructor for XPathEngineClientSocket + " + e.getMessage());
			this.is_param_set = false;
		}
	}

	@SuppressWarnings("resource")
	public String extract_content_from_url (String url) throws IOException {

		if (! this.is_param_set)
			return null;

		Socket socket = new Socket(this.hostname, this.port_no);

		// sending GET /path HTTP/1.1 request
		PrintWriter out_obj = new PrintWriter(socket.getOutputStream(), true);
		out_obj.write("GET " + this.curr_path + " HTTP/1.1\r\n");
		out_obj.write("User-Agent: CIS455XPathEngine\r\n");
		out_obj.write("Host: " + this.hostname + ":" + this.port_no + "\r\n");
		out_obj.write("Connection: close\r\n\r\n");
		out_obj.flush();

		// reading data from response 
		BufferedReader in_obj = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		StringBuffer content_data = new StringBuffer();
		String curr_line = in_obj.readLine();

		// if the data is not retrieved properly
		if(!curr_line.endsWith("200 OK")) 
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
		System.out.println("[Output from log4j] In client socket, File content extracted from obj + " + content);
		return content;
	} 

	public Document extract_dom_from_url(String url) throws UnknownHostException, IOException, InterruptedException{

		Document dom_obj = null;
		// extracting body content from url 
		String body_content = this.extract_content_from_url(url);
		
		System.out.println ("[Output from log4j] In client socket , after extracting content from url + " + body_content);

		if (body_content == null)
			return null;
		
		System.out.println ("[Output from log4j] In client socket , after extracting content from url, content type is  + " + this.content_type);
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
}
