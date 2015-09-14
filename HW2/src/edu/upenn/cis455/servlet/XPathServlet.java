package edu.upenn.cis455.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;
import edu.upenn.cis455.xpathengine.XPathEngineClientSocket;


@SuppressWarnings({ "serial", "unused" })
public class XPathServlet extends HttpServlet {

	/* TODO: Implement user interface for XPath engine here */

	public void init(ServletConfig config) throws ServletException {
		// System.out.println("[Ouptut from log4j] XPathServlet started!");
	}

	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/* TODO: Implement user interface for XPath engine here */
		String xpath_data = request.getParameter("xpath");
		String doc_url = request.getParameter("url");

		// handling base case for empty /null xpath and doc url values 
		// sending a temporary redirect response to the client using the specified redirect location URL i.e. current page
		if(xpath_data == null || doc_url == null || xpath_data.trim().equals("") || doc_url.trim().equals("")) {
			response.sendRedirect(request.getContextPath() + "/xpath");
			return;
		}

		String[] multiple_xpaths = xpath_data.split(";");

		// for trimming the current array values of xpaths for extra spaces
		String[] trimmed_xpaths = new String[multiple_xpaths.length];
		for (int index = 0; index < multiple_xpaths.length; index++) {
			multiple_xpaths[index] = URLDecoder.decode(multiple_xpaths[index], "UTF-8");
			trimmed_xpaths[index] = multiple_xpaths[index].trim();
		}
		
		
		// to get the document object from the document url
		doc_url = URLDecoder.decode(doc_url, "UTF-8");

		// actual dom obj implementation
		
		Document dom_xml_obj = null;
		try {
			// System.out.println("[Output from log4j] Extracting DOM Object from URL");
			dom_xml_obj = this.extract_dom_from_url(doc_url);
		} 
		catch (InterruptedException e) {
			System.err.println("[Output from log4j] Error while Extracting DOM Object from URL " + e);
			e.printStackTrace();
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while Extracting DOM Object from URL " + e);
		}

		/* For testing evaulate function by converting local xml file to DOM Object
		 * 
		// for testing purpose hardcoding document object
		*/
		/*File xml_file = new File("/home/cis455/Documents/CIS 455 Internet & Web Systems/courses.xml");
		Document dom_xml_obj = null;
		// System.out.println("[Output from log4j] Creating DOM Object");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			dom_xml_obj = builder.parse(xml_file);
			// System.out.println("[Output from log4j] Passed Creating DOM Object");
		} 
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		// creating obj of XPathEngineImpl
		XPathEngineImpl xpath_engine_obj = new XPathEngineImpl();
		xpath_engine_obj.setXPaths(trimmed_xpaths);

		// to check isValid function for all the entered xpaths
		for (int index = 0; index < trimmed_xpaths.length; index++){
			boolean is_valid_xpath = xpath_engine_obj.isValid(index);
			// System.out.println("[Output from log4j] Validation for + " + trimmed_xpaths[index] + " is + " + is_valid_xpath);
		} 

		// tell users if sucess or not
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>DOM Validation and Evaluation Results</font></h1><hr/>");

		if(dom_xml_obj == null) {
			// create error output 
			System.err.println("[Output from log4j] DOM Object Does Not Exists");
			out.println("&nbsp;&nbsp;&nbsp;&nbsp;<h2 align='middle' padding = '0' margin = '0'><font color = 'red'>DOM Object is Invalid</font></h2><hr/>");
			out.println("</body></html>");
		}
		else{
			// System.out.println("[Output from log4j] DOM Object Exists");
			out.println("<table align = 'center'><tr><th>XPaths to be Matched</th><th>has_matched?</th></tr>");
			boolean[] existed = xpath_engine_obj.evaluate(dom_xml_obj);
			for(int i = 0; i < existed.length; i ++){
				if(existed[i])
					out.println("<tr><td align = 'center'>" + trimmed_xpaths[i] + "</td><td align = 'center'><font color = 'green'>Yes</font></td><tr>");
				else
					out.println("<tr><td align = 'center'>" + trimmed_xpaths[i] + "</td><td align = 'center'><font color = 'red'>No</font></td><tr>");
			}
			out.println("</table></body></html>");
		}
	}

	// access html/xml page and convert to dom object tree
	public Document extract_dom_from_url(String url) throws UnknownHostException, IOException, InterruptedException{
		// handle case where http:// is not appended in the url
		if (!url.contains("http://"))
			url = "http://" + url;
		
		// System.out.println("[Output from log4j] Extracting DOM Object from URL + " + url);
		XPathEngineClientSocket client_socket = new XPathEngineClientSocket(url); 
		return client_socket.extract_dom_from_url(url);
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* TODO: Implement user interface for XPath engine here */
		PrintWriter out = response.getWriter();
		out.println("<html><body>");

		out.println("<form action='xpath' method = 'POST'>");
		out.println("<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>XPath Engine</font></h1><br/>");
		// creating text box for XPath 
		out.println("<table align = 'center'><tr>");
		out.println("<td align = 'center'><font color = '#009933'><label>XPath</label></font></td>");
		out.println("<td align = 'center'><input type='text' name='xpath' id='xpath' /></td></tr>");
		// creating text box for HTML/XML Document URL 
		out.println("<tr><td align='center'><font color = '#009933'><label>HTML/XML Document URL</label></td>");
		out.println("<td align='center'><input type='text' name='url' id='url' /></td></tr>");

		out.println("</table><p align='center'><input type='submit' class='submit' value='Query'/></p>");
		out.println("</form></body></html>");
	}
}

