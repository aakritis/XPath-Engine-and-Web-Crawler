package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

public class XPathEngineImpl implements XPathEngine {

	private String[] xpaths;
	private int path_index;
	private HashMap<String, ArrayList<String>> map_node_name;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor !!
	}

	public void setXPaths(String[] s) {
		/* TODO: Store the XPath expressions that are given to this method */
		this.xpaths = s;
	}

	public boolean isValid(int i) {
		try {
			/* TODO: Check which of the XPath expressions are valid */
			this.path_index = i;
			// System.out.println("[Ouptut from log4j] Validating XPath : " + this.xpaths[path_index]);

			// current xpath being checked 
			String curr_xpath = this.xpaths[path_index];

			/* xpath should start with axis (/) and step cannot be empty
			 * isValidXPath XPath -> axis step
			 */
			if (! curr_xpath.startsWith("/") || curr_xpath.equals("/"))
				return false;

			this.map_node_name = new HashMap<String,ArrayList<String>>();

			// removing first / check the remaining xpath
			// start processing from step -> multi level expression -> test

			return this.is_valid_step(curr_xpath.substring(1));
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while checking isValid Xpath grammar" + e);
			return false;
		}
	}

	// Function to check step -> (split string into multiple sublevels dividing based on "/" "/") 
	public boolean is_valid_step (String grammar_step) {
		try {
			// System.out.println("[Ouptut from log4j] Validating step : " + grammar_step);

			ArrayList<String> sub_levels = this.extract_sub_levels(grammar_step);
			for (String level : sub_levels) 
				if (!is_valid_level(level)) 
					return false;
			// System.out.println("[Output from log4j] Validity from is_valid_step + TRUE");
			return true;
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while checking is_valid_step in step grammar " + e);
			return false;
		}
	}

	// extracting sub levels from "/" to "/" without brackets 
	public ArrayList<String> extract_sub_levels (String sub_path) {
		try {
			// System.out.println("[Ouptut from log4j] Extracting Sub Levels is step grammar ");
			ArrayList<String> sub_level = new ArrayList<String>();

			// base case - if only nodename 
			// step -> nodename 
			if (! sub_path.contains("/")) {
				sub_level.add(sub_path);
				return sub_level;
			}

			// step -> nodename ([test])* (axis step)?
			// converting to step -> levels  + xpath?
			StringBuffer one_level = new StringBuffer();
			// character stack to manage level within special characters 
			Stack<Character> bracket_stack = new Stack<Character>();

			//handling quotes 
			boolean is_in_quotes = false;

			// traversing subpath character-wise
			for (int index = 0; index < sub_path.length(); index++) {
				char ch = sub_path.charAt(index);

				switch(ch) {
				case '"':
					// changing from false to true on first occurence and back 
					is_in_quotes = !is_in_quotes;
					break;
				case '[':
					if(!is_in_quotes) bracket_stack.push(ch);
					break;
				case  ']' :
					if(!is_in_quotes){
						if(bracket_stack.isEmpty())
							break;
						bracket_stack.pop();
					}
					break;
				case '/':
					if(!is_in_quotes){
						if(!bracket_stack.isEmpty()) 
							break; 
						sub_level.add(one_level.toString());
						one_level.setLength(0);
						continue;
					}
				}
				one_level.append(ch);
			}
			sub_level.add(one_level.toString());
			return sub_level;
		}
		catch (Exception e) {
			// System.out.println("[Output from log4j] Error while extracting sub levels in step grammar extract_sub_levels " + e);
			return null;
		}
	}

	// function to check every extracted level in the step function
	public boolean is_valid_level (String sub_level) {
		try {
			// System.out.println("[Ouptut from log4j] Validating sub-levels in step : " + sub_level);

			// to check that xml should not appear as nodename
			if (sub_level.toLowerCase().startsWith("xml"))
				return false;
			
			String regex_sub_level = "(\\s)*([A-Z_a-z][A-Z_a-z-.0-9]*)(\\s)*(\\[.+\\])*";
			Pattern sub_level_pattern = Pattern.compile(regex_sub_level);
			Matcher sub_level_matcher = sub_level_pattern.matcher(sub_level);

			if (sub_level_matcher.matches()) {
				String node_name = null;
				StringBuffer curr_test = new StringBuffer();

				Stack<Character> bracket_stack = new Stack<Character>();

				boolean is_in_quotes = false;
				boolean has_node_name = false;
				boolean is_in_test = false;
				// adding to allow \" within ""
				boolean is_escape_char = false;

				for (int index = 0; index < sub_level.length(); index++) {
					char ch = sub_level.charAt(index);
					// checking based on character , thus using switch case 
					switch (ch) {
					case '\\' :
						if (is_in_quotes)
							is_escape_char = true;
						break;
					case '"' :
						if (is_escape_char){
							is_escape_char = false;
							break;
						}
						else {
							is_in_quotes = !is_in_quotes;
						}
						break;
					case '[' :
						if (!is_in_quotes) {
							bracket_stack.push(ch);
							is_in_test = true;
							if (!has_node_name) {
								node_name = sub_level.substring(0,index);
								has_node_name = true;
							}
						}
						break;
					case ']' :
						if (!is_in_quotes) {
							bracket_stack.pop();
							if (bracket_stack.isEmpty()) {
								is_in_test = false;
								if (!this.is_valid_test(curr_test.toString()))
									return false;
								if (this.map_node_name.containsKey(node_name))
									this.map_node_name.get(node_name).add(curr_test.toString());
								else {
									ArrayList<String> test_data = new ArrayList<String>();
									test_data.add(curr_test.toString());
									this.map_node_name.put(node_name, test_data);
								}
								curr_test.setLength(0);
							}
						}
						break;
					}
					if(is_in_test) {
						curr_test.append(ch);
					}
				}
				// System.out.println("[Output from log4j] Validity from is_valid_level + TRUE");
				return true;
			}
			else {
				// System.out.println("[Ouptut from log4j] Invalid Level in is_valid_level : " + sub_level);
				return false;
			}
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while checking is_valid_level in step grammar " + e);
			return false;
		}
	}

	public boolean is_valid_test (String grammar_test) {
		try {
			// System.out.println("[Ouptut from log4j] Validating grammar in test : " + grammar_test);
			// removes brackets 
			String test_val = grammar_test.substring(1,grammar_test.length());
			// System.out.println("[Ouptut from log4j] Validating grammar in test - required substring : " + test_val);

			/* checking pattern 2 test -> text() = "..."
			            pattern 3 test -> contains(text(),"...")
			            pattern 4 test -> @attrname = "..."  */

			String regex_test_cases = "(((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*\\=(\\s)*\\\"(.*?)\\\"(\\s)*)|"  + // test case 2  
					"((\\s)*contains(\\s)*\\((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*,(\\s)*\\\"(.*?)\\\"(\\s)*\\))|" + // test case 3
					"((\\s)*\\@([A-Z_a-z][A-Z_a-z0-9-.]*)(\\s)*\\=\\\"[^\\\"]+\\\"(\\s)*))"; // test case 4

			Pattern pattern_test = Pattern.compile(regex_test_cases);
			Matcher matcher_test = pattern_test.matcher(test_val);

			if (matcher_test.matches()) {
				// System.out.println("[Ouptut from log4j] Simple Match Test 2/3/4 cases matched - Validated");
				// System.out.println("[Output from log4j] Validity from is_valid_test + TRUE");
				return true;
			}
			else {
				// checking pattern 1 test -> step
				// System.out.println("[Ouptut from log4j] Check for further step in grammar - Continue Validating");
				return this.is_valid_step(test_val);
			}
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while checking is_valid_l	evel in step grammar " + e);
			return false;
		}
	}


	public boolean[] evaluate(Document d) { 
		/* TODO: Check whether the document matches the XPath expressions */

		try {
			// to store match result for every xpath 
			boolean[] matches_result = new boolean [this.xpaths.length];

			// when no document object model created
			if (d == null) {
				return matches_result;
			} 

			// to validate each xpath for the given document 
			for ( int i_path = 0; i_path < this.xpaths.length; i_path++ ) {
				this.path_index = i_path;

				// System.out.println ("[Output from log4j] Evaluating XPath " + this.xpaths[i_path]);

				// check if the passed xpath is a valid xpath 
				boolean is_valid_path = isValid(i_path);
				// System.out.println("[Output from log4j] Is XPath valid? " + is_valid_path);

				if (is_valid_path) {
					// remove axis (/) from the xpath before evaluation
					String intial_step = this.xpaths[i_path].substring(1);

					// extract root element from the given document 
					ArrayList<Node> root_node = new ArrayList<Node>();
					root_node.add(d.getDocumentElement());

					matches_result[i_path] = is_matched_step (intial_step, root_node);
					// System.out.println("Match result for XPath " + this.xpaths[i_path] + " is " + matches_result[i_path]);
				}
			}

			return matches_result;
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while checking evaluate  " + e);
			boolean [] err_array = new boolean [this.xpaths.length];
			return err_array;
		}
	}

	/* Evaluate XPath by traversing DOM level by level 
	 * return true if all levels are matched properly
	 * */
	public boolean is_matched_step (String initial_step, ArrayList<Node> curr_level_nodes) {
		try {
			// extract levels in the current step
			ArrayList<String> sub_levels = this.extract_sub_levels(initial_step);
			ArrayList<Node> next_level_nodes = curr_level_nodes;
			int count_match = 0;

			for (String sub_level : sub_levels ) {
				// System.out.println("[Output from log4j] Printing sub_level in is_matched_step + " + sub_level);
				next_level_nodes = this.is_matched_level(sub_level, next_level_nodes);

				if (next_level_nodes == null) {
					count_match = 0;
					return false;
				}

				count_match = next_level_nodes.size();

				// System.out.println("[Output from log4j] Number of next level nodes " + count_match);
			}

			if (count_match > 0)
				return true;
			else 
				return false;
		}
		catch (Exception e) {
			System.err.println("[Output from log4j] Error while checking is_matched_step  " + e);
			return false;
		}
	}

	// return the nodes of the level that matches
	public ArrayList<Node> is_matched_level (String curr_level , ArrayList<Node> match_node) {
		try {
			// System.out.println("[Ouptut from log4j] Inside is_matched_level for level " + curr_level);
			ArrayList<Node> next_level_nodes = new ArrayList<Node>();
			String nodename = null;

			if (! curr_level.contains("[")) {
				// only node name in the current level
				nodename = curr_level.trim();
			}
			else {
				// if the current level contains ([test])*
				nodename = curr_level.split("[\\[]")[0];
			}

			for (Node node : match_node) {
				// case : when current level doesn't have ([test])* 
				if (! this.map_node_name.containsKey(nodename)) {
					if(nodename.equalsIgnoreCase(node.getNodeName())) {
						NodeList children = node.getChildNodes();
						for(int child = 0; child < children.getLength(); child++) {
							next_level_nodes.add(children.item(child));
						}
					}
				}
				else {
					ArrayList<String> level_test = this.map_node_name.get(nodename);
					for (String test: level_test) {
						if(! this.is_matched_test(test, node))
							continue;
						NodeList children = node.getChildNodes();
						for(int child = 0; child < children.getLength(); child++) {
							next_level_nodes.add(children.item(child));
						}
					}
				}
			}
			return next_level_nodes;
		}
		catch(Exception e) {
			System.err.println("[Output from log4j] Error while checking is_matched_level  " + e);
			return null;
		}
	}

	// called for levels that contains ([test])*
	public boolean is_matched_test (String curr_test, Node curr_level_node){
		try {
			// System.out.println("[Ouptut from log4j] Inside is_matched_test for test " + curr_test);
			String test = curr_test.substring(1, (curr_test.length())).trim();

			String text_reg_ex = "(\\s)*text(\\s)*\\((\\s)*\\)(\\s)*\\=(\\s)*\\\"(.*?)\\\"(\\s)*";      // test patterns 2:  text() = "..."
			String contains_reg_ex = "(\\s)*contains(\\s)*\\((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*,(\\s)*\\\"(.*?)\\\"(\\s)*\\)"; // test patterns 3:  contains(text(), "...")
			String attr_reg_ex = "(\\s)*\\@([A-Z_a-z][A-Z_a-z0-9-.]*)(\\s)*\\=\\\"[^\\\"]+\\\"(\\s)*";           // test patterns 4:  @attname = "..."

			if(test.matches(text_reg_ex)) {
				// spliting on the bases of " to get text between the quotes
				int char_count = test.length() - test.replaceAll("\"", "").length();
				// System.out.println("[Output from log4j] Value of total no of quotes " +  char_count);
				String quoted_text = "";
				if (char_count == 2)
					quoted_text = test.split("\"")[1].trim();
				else {
					String[] quote_arr = test.split("\"");
					for (int i=1 ; i< quote_arr.length ; i++) {
						quoted_text += quote_arr[i].trim(); 
					}
				}
				// System.out.println("[Output from log4j] Value extracted on basis of split of " +  quoted_text);
				Node node_child = curr_level_node.getFirstChild();
				if (node_child != null && node_child.getNodeType() == Node.TEXT_NODE && node_child.getNodeValue().equals(quoted_text))
					return true;
			}
			else if (test.matches(contains_reg_ex)) {
				// spliting on the bases of " to get text between the quotes 
				// String quoted_text = test.split("\"")[1].trim();
				// spliting on the bases of " to get text between the quotes
				int char_count = test.length() - test.replaceAll("\"", "").length();
				// System.out.println("[Output from log4j] Value of total no of quotes " +  char_count);
				String quoted_text = "";
				if (char_count == 2)
					quoted_text = test.split("\"")[1].trim();
				else {
					String[] quote_arr = test.split("\"");
					for (int i=1 ; i< quote_arr.length ; i++) {
						quoted_text += quote_arr[i].trim(); 
					}
				}
				// System.out.println("[Output from log4j] Value extracted on basis of split of " +  quoted_text);
				Node node_child = curr_level_node.getFirstChild();
				if (node_child != null && node_child.getNodeType() == Node.TEXT_NODE && node_child.getNodeValue().contains(quoted_text))
					return true;
			}
			else if (test.matches(attr_reg_ex)) {
				// extracting the attribute name by splitting on " and @ and replacing space by ""
				String attr_text = test.split("\"")[0].split("@")[1].split("=")[0].replace("\\s*","");
				// // System.out.println("[Output from log4j] Value of attr_text + " + attr_text);
				// spliting on the bases of " to get text between the quotes 
				String quoted_text = test.split("\"")[1].trim();
				// // System.out.println("[Output from log4j] Value of quoted_text + " + quoted_text);
				// // System.out.println("[Output from log4j] Value of curr_level_node + " + curr_level_node);
				NamedNodeMap node_map = curr_level_node.getAttributes();
				// // System.out.println("[Output from log4j] Value of node_map + " + node_map);
				if (node_map != null) {
					Node node_child = node_map.getNamedItem(attr_text);
					// // System.out.println("[Output from log4j] Value of node_child +" + node_child + " getNodeValue + " + node_child.getNodeValue());
					if (node_child != null && quoted_text.equals(node_child.getNodeValue()))
						return true;
				}
			}
			else  { // if its a nested step call // test pattern 1 : test -> step 
				ArrayList<Node> child_level_nodes = new ArrayList<Node>();
				NodeList children = curr_level_node.getChildNodes();
				for(int child = 0; child < children.getLength(); child ++){
					child_level_nodes.add(children.item(child));
				}
				return this.is_matched_step(test, child_level_nodes);
			}

			return false;
		}
		catch(Exception e) {
			System.err.println("[Output from log4j] Error while checking is_matched_test  " + e);
			return false;
		}
	}
}

