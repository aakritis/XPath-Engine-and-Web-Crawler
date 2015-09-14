package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserChannelEntity;
import edu.upenn.cis455.storage.UserEntity;
import edu.upenn.cis455.storage.WebContentEntity;

@SuppressWarnings({ "serial", "unused" })
public class CrawlerServlet extends HttpServlet {

	final int LOGIN = 1;
	final int REGISTER = 2;
	final int CHANNEL = 3;
	final int LOGOUT = 4;
	final int CHANNELXML = 5;
	final int DELETECHANNEL = 6;

	String env_root = null;
	/*public void init(ServletConfig config) throws ServletException {
		// System.out.println("[Ouptut from log4j] CrawlerServlet started!");
		//ServletContext context = getServletContext();
		//this.env_root = context.getInitParameter("BDBStore");
		// System.out.println("[Ouptut from log4j] CrawlerServlet started!");
	}*/

	/* You may want to override one or both of the following methods */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//// System.out.println("Post 1");
		String userpage = request.getParameter("page");
		//// System.out.println("Post 2");
		String username;
		String password;
		if(userpage == null) {
			// System.out.println("Post null");
			doGet(request, response);
			return;
		}
		//// System.out.println("Post 3");
		this.set_db_directory();
		//// System.out.println("Post 4");
		PrintWriter out = response.getWriter();
		
		int page = Integer.parseInt(userpage);
		switch(page){
		case REGISTER:
			username = request.getParameter("username");
			password = request.getParameter("password");
			if(username.isEmpty() || password.isEmpty()){
				doGet(request, response);
			}
			if (DBWrapper.has_user(username)) {
				this.show_request_outcome("existed", out);
			}
			else{
				// System.out.println("Before Registeration + " + username + "		" + password);
				UserEntity new_user = new UserEntity(username, password);
				DBWrapper.add_user(new_user);
				// System.out.println("After Registeration");
				this.show_request_outcome("registered", out);
			}
			break;
			
		case LOGIN:
			username = request.getParameter("username");
			password = request.getParameter("password");
			if (DBWrapper.authenticate_user(username, password)) {
				request.getSession().setAttribute("logged_user", username);
				response.sendRedirect(request.getContextPath() + "/crawler?page=3");
			}
			else {
				this.show_request_outcome("incorrect", out);
			}
			break;

		case CHANNEL:
			String chname = request.getParameter("chname");
			String xpaths = request.getParameter("xpaths");
			String xslURL = request.getParameter("url");

			if(chname == null || xpaths == null || xslURL == null){
				this.show_request_outcome("badchannel", out);
			}
			if(DBWrapper.has_channel(chname)) {
				this.show_request_outcome("channel_existed", out);
			}
			ArrayList<String>xpathsArray = new ArrayList<String>();
			for(String piece : xpaths.split(";")){
				xpathsArray.add(piece.trim());
			}
			UserChannelEntity new_channel = new UserChannelEntity(chname, xpathsArray, xslURL);
			username = (String) request.getSession().getAttribute("logged_user");
			DBWrapper.add_channel(new_channel);

			UserEntity owner = DBWrapper.get_user(username);
			owner.addchannel_names(chname);
			DBWrapper.add_user(owner);
			doGet(request, response);
			break;
		}
	}

	@Override
	/**
	 * define function on ACTION = GET for various display pages
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// accessing hidden field 'page' in the created html pages 
		
		String disp_page = request.getParameter("page");
		// setting response file type as html
		response.setContentType("text/html");
		PrintWriter out_obj = response.getWriter();
		// start of html page 
		out_obj.println("<html><body>");

		// by default, on load display login page 
		if (disp_page == null)
			this.display_login_page(out_obj);
		else {
			int page_id = Integer.parseInt(disp_page);
			switch(page_id){
			case LOGIN :
				this.display_login_page(out_obj);
				break;
			case REGISTER :
				this.display_registration_page(out_obj);
				break;
			case CHANNEL :
				this.display_channels_page(out_obj, request);
				break;
			case LOGOUT :
				this.display_logout_page(out_obj, request);
				break;
			case CHANNELXML :
				this.show_channel_xml(out_obj, request);
				break;
			case DELETECHANNEL :
				this.delete_channel(request, response); 
				break;
			default :
				this.display_login_page(out_obj);
				break;
			}
		}
		out_obj.println("</body></html>");
	}

	/**
	 * Display pages for various cases 
	 * Login Page, Registration Page, Logout Page, Channels Page, Add Channel Page, Delete Channel Page, Display Channel Page 
	 **/

	/**
	 * to display html page for user login in doGet request
	 * @param out_obj
	 */
	public void display_login_page (PrintWriter out_obj) {
		// form for sign in 
		out_obj.println("<form action='crawler' method = 'POST' >");
		out_obj.println("<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>XPath Engine</font></h1>");
		out_obj.println("<h2 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>LOGIN</font></h2><br/>");
		out_obj.println("<table align = 'center'><tr>");
		out_obj.println("<td align = 'center'><font color = '#009933'><label>Username</label></font></td>");
		out_obj.println("<td align = 'center'><input type='text' name='username' id='username' value='" + "' /></td></tr>");
		out_obj.println("<tr><td align='center'><font color = '#009933'><label>Password</label></td>");
		out_obj.println("<td align='center'><input type='text' name='password' id='password' value='" + "' /></td></tr>");
		out_obj.println("</table><p align='center'><input type='submit' name='login' value='Login'/></p>");
		out_obj.println("<input type='hidden' name='page' value='1'/>");
		out_obj.println("</form>");

		// form for redirecting to register  
		out_obj.println("<form action='crawler' method='GET'>");
		out_obj.println("<p align='center'><input type='submit' name='action' value='Registeration'/></p>");
		out_obj.println("<input type='hidden' name='page' value='2'/></form>");

		// form for redirecting to display all channels  
		out_obj.println("<form action='crawler' method='GET'>");
		out_obj.println("<p align='center'><input type='submit' name='action' value='Display All Channels'/></p>");
		out_obj.println("<input type='hidden' name='page' value='3'/></form>");

		// form for redirecting to Logout page  
		out_obj.println("<form action='crawler' method='GET'>");
		out_obj.println("<p align='center'><input type='submit' name='action' value='Logout'/></p>");
		out_obj.println("<input type='hidden' name='page' value='4'/></form>");
	}

	/**
	 * to display html page for user registration in doGet request
	 * @param out_obj
	 */
	public void display_registration_page (PrintWriter out_obj) {
		// form for registration 
		out_obj.println("<form action='crawler' method = 'POST' >");
		out_obj.println("<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>XPath Engine</font></h1>");
		out_obj.println("<h2 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Create New Account</font></h2><br/>");
		out_obj.println("<table align = 'center'><tr>");
		out_obj.println("<td align = 'center'><font color = '#009933'><label>Username</label></font></td>");
		out_obj.println("<td align = 'center'><input type='text' name='username' id='username' value='" + "' /></td></tr>");
		out_obj.println("<tr><td align='center'><font color = '#009933'><label>Password</label></td>");
		out_obj.println("<td align='center'><input type='text' name='password' id='password' value='" + "' /></td></tr>");
		out_obj.println("</table><p align='center'><input type='submit' name='create' value='Create Account'/></p>");
		out_obj.println("<input type='hidden' name='page' value='2'/>");
		out_obj.println("</form>");
	}

	/**
	 * to display html page for user logout in doGet request
	 * @param out_obj
	 * @param request
	 */
	public void display_logout_page (PrintWriter out_obj, HttpServletRequest request) {
		request.getSession().setAttribute("logged_user", null);
		this.show_request_outcome("logout", out_obj);
		out_obj.println("<p align='center'>Successfully logged out <br/> <a href='crawler?page=1>Back to Login Page</a></p>");
	}
	/**
	 * to display list of all channels for the logged in user
	 * @param out_obj
	 * @param request
	 */
	public void display_channels_page (PrintWriter out_obj, HttpServletRequest request) {
		String logged_user = (String) request.getSession().getAttribute("logged_user");
		// acess db root directory
		this.set_db_directory();
		
		out_obj.println("<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>XPath Engine</font></h1>");
		out_obj.println("<h2 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Create New Account</font></h2><br/>");
		
		ArrayList<UserChannelEntity> channels = DBWrapper.get_all_channels();
		
		if (channels == null || channels.size() == 0)
			out_obj.println("<p align='center'> No Channels Available </p>");
		else {
			out_obj.println("<br/><br/><p align='center'><h3> All Channels : <h3></p>");
			out_obj.println("<table align = 'center'><tr>");
			// if user not logged in , only display names of channels 
			if (logged_user == null){
				out_obj.println("<tr><th><font color = '#009933'>Channel Name</font></th></tr>");
				for(UserChannelEntity channel : channels){
					out_obj.println("<tr><td>" + channel.get_channel_name() + "</td></tr>");
				}
				out_obj.println("<p align='center'>To view channel contents, please <a href='?page=1'>Log In</a></p>");
			}
			else {
				// if the user is logged in, view details , add / delete channels
				out_obj.println("<tr><th><font color = '#009933'>Channel Name</font></th></tr>");
				for(UserChannelEntity channel : channels){
					String name = channel.get_channel_name();
					if (DBWrapper.user_has_channel(logged_user, name)){
						out_obj.println("<tr><td><a href='?page=5&chname=" + name + "'>" + name + "</a></td><td><a href='?page=6&chname=" + name + "'>Delete</a></td></tr>");
					}
					else{
						out_obj.println("<tr> <td>" + name + "</td><td></td><tr>"); 
					}
				}
			}
			out_obj.println("</table><br/>");
		}
		if (logged_user != null)
			this.display_add_channel_page(out_obj);
	}
	/**
	 * Helper function to access db directory and set up db 
	 */
	public void set_db_directory() {
		// System.out.println("Before SetUp Env +");
		// getServletContext().getInitParameter("BDBstore")
		this.env_root = getServletContext().getInitParameter("BDBstore");
		// System.out.println("After SetUp Env + " + env_root);
		DBWrapper.setup_environment(this.env_root);
		// System.out.println("After SetUp Env +");
	}

	/**
	 * display channel xml page
	 * @param out
	 * @param req
	 */
	
	public void show_channel_xml(PrintWriter out, HttpServletRequest req){
		String logged_user = (String) req.getSession().getAttribute("logged_user");
		String chname = req.getParameter("chname");
		this.set_db_directory();
		UserChannelEntity channel = DBWrapper.get_channel(chname);
		if(logged_user == null)
			out.println("You are not logged in. To see channel contents, please <a href='?page=1'>Log In</a>");
		else if(channel == null) 
			out.println("New Channel");
		else{
			out.println("Welcome to channel:  " + channel.get_channel_name() + "<br>");
			out.println("<br><br>All XMLs matched on the Channel:<br><br><br><br>");

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
			String xsl_path = channel.get_xsl_url();
			String head = "";
			
			out.println(head);
			out.println("<documentcollection>");
			
			for(String extend : channel.get_extend_urls()){
				
				WebContentEntity webpage = DBWrapper.get_webpage(extend);
				String xml_content = webpage.get_url_content();
				StringBuffer cbuf = new StringBuffer();
				if(xml_content.contains("?>")){
					//head = xml_content.substring(0, xml_content.indexOf("?>") + 2) + "<xml-stylesheet type=\"text/xsl\" href=\"" + xsl_path + "\"?>";
					int startId = xml_content.indexOf("?>") + 2;
					cbuf = new StringBuffer(xml_content.substring(startId));
				}
				String last_crawled = dateFormat.format(webpage.get_last_crawled_date()) + "T" + dateFormat.format(webpage.get_last_crawled_date());
				out.println("<document crawled=\"" + last_crawled + "\" location=\"" + webpage.get_absolute_url() + "\">");
			    out.println(xml_content);
				out.println("</document>");
			}
			out.println("</documentcollection>");
		}
		if(logged_user != null)
			this.display_add_channel_page(out);
	}
	/**
	 * to display add channel page to current logged on user 
	 * @param out_obj
	 */
	public void display_add_channel_page (PrintWriter out_obj) {
		out_obj.println("<form action='crawler' method = 'POST'>");
		out_obj.println("<h2 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Add New Channel</font></h2><br/>");

		out_obj.println("<table align = 'center'><tr>");
		out_obj.println("<td align = 'center'><font color = '#009933'><label>Channel</label></font></td>");
		out_obj.println("<td align = 'center'><input type='text' name='chname' id='chname' value='" + "'/></td></tr>");

		out_obj.println("<td align = 'center'><font color = '#009933'><label>XPaths(seperate by semicolons)</label></font></td>");
		out_obj.println("<td align = 'center'><input type='text' name='xpaths' id='xpaths' value='" + "'/></td></tr>");

		// xsl url
		out_obj.println("<td align = 'center'><font color = '#009933'><label>XSL URL</label></font></td>");
		out_obj.println("<td align = 'center'><input type='text' name='url' id='url' /></td></tr>");

		out_obj.println("</table><p align='center'><input type='submit' name='add' value='Add'/></p>");
		out_obj.println("<input type='hidden' name='page' value='3'/>");
		out_obj.println("</form>");
		out_obj.println("<p align='center'><a href='crawler'>Back</a></p>");
	}
	/**
	 * function to delete existing channel
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void delete_channel(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String username = (String) request.getSession().getAttribute("logged_user");
		if(username == null)
			doGet(request, response);
		String chname = request.getParameter("chname");
		DBWrapper.delete_channel(username, chname);
		this.show_request_outcome("deleted", response.getWriter()); 
	}
	/**
	 * generic message page to display relevant outcomes of request
	 * @param result
	 * @param out
	 */
	public void show_request_outcome(String result, PrintWriter out){
		String info = "";
		out.println("<html><body>");
		if(result.equals("existed"))
			info = "<p align='center'>User already Exists. <a href='crawler'>Back</a>";
		if(result.equals("registered"))
			info = "<p align='center'>Successfully Registered. Thanks! + <a href='crawler'>Back</a><p>";
		if(result.equals("incorrect"))
			info = "<p align='center'>Incorrect username or password. <a href='crawler'>Back</a><p>";
		if(result.equals("badchannel"))
			info = "<p align='center'>Channel/XPaths/XSL URL cannot be empty.<a href='crawler'>Back</a><p>";
		if(result.equals("channel_existed"))
			info = "<p align='center'>Channel already Exists. <a href='crawler'>Back</a><p>";
		if(result.equals("deleted"))
			info = "<p align='center'>Channel Deleted. <a href='crawler?page=3'>Back</a><p>";
		if(result.equals("logout"))
			info = "<p align='center'>Sucessfully Logged Out. <a href='crawler?page=1'>Back</a>";
		out.println(info + "</body></html>");
	}
}
