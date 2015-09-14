package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import edu.upenn.cis455.storage.DBWrapper;

/**
 * XPathCrawler - Main Class for Starting Crawling
 * @author Aakriti Singla
 *
 */
@SuppressWarnings("unused")
public class XPathCrawler //extends Thread {
{
	
	// input parameters from the arguments
	protected String seed_url;
	// default values for parameters
	protected static String env_dir = System.getProperty("user.dir") + "/database";
	protected static int maxsize_doc = 100; // in megabytes
	protected static int num_files = 100;
	
	// create Thread Pool for multi-threaded web crawler - Extra Credit 
	protected static ArrayList<Thread> threadPool = new ArrayList<Thread> ();
	
	// initialize object of ThreadCrawler class
	protected ThreadCrawler threadCrawler = null;
	
	// to store the sites urls yet to be processed 
	protected static Queue<String> url_queue = new LinkedList<String>();
	
	/**
	 * Default Constructor
	 */
	public XPathCrawler(){
		
	}
	/**
	 * Parameterized Constructor
	 * @param web_url
	 * @param env_dir
	 * @param maxsize_doc
	 * @param num_files
	 */
	public XPathCrawler(String seed_url, String env_dir, int maxsize_doc, int num_files) {
		this.seed_url = seed_url;
		XPathCrawler.env_dir = env_dir;
		XPathCrawler.maxsize_doc = maxsize_doc;
		XPathCrawler.num_files = num_files;
		
		// setting up BerkleyDB environment 
		DBWrapper.setup_environment(env_dir);
		
		/*
		WebURLQueue queue_obj = new WebURLQueue();
		// // System.out.println("[Output from log4j] Adding seed url to queue in XPathCrawler + " + this.seed_url);
		queue_obj.addToQueue(this.seed_url);
		// // System.out.println("[Output from log4j] Seed URL added");
		
		// create thread pool with fixed size for multi-threaded web crawler - Extra Credit
		for ( int iThread = 0 ; iThread < 10 ; iThread++ ){
			threadCrawler = new ThreadCrawler();
			Thread workerthread = new Thread (threadCrawler);
			workerthread.setName("Thread - " + iThread);
			threadPool.add(workerthread);
			workerthread.start();
		}
		*/
	}
	/**
	 * function to write seed url to the request queue 
	 */
	public void run() {
		try {
			// // System.out.println("[Output from log4j] In run method of XPathCrawler");
			WebURLQueue queue_obj = new WebURLQueue();
			// // System.out.println("[Output from log4j] Adding seed url to queue in XPathCrawler + " + this.seed_url);
			queue_obj.addToQueue(this.seed_url);
			// // System.out.println("[Output from log4j] Seed URL added");
			this.threadCrawler = new ThreadCrawler();
			this.threadCrawler.run();
						
		}
		catch (Exception ex) {
			System.err.println("[Output from log4j] Error while enqueueing Seed URL in XPathCrawler + " + ex);
		}
	}
	
	/**
	 * Start Crawling from the Seed URL 
	 * @throws IOException
	 */
	//public void start_crawl() throws IOException {
		
	//}
	/**
	 * to stop web crawler -- shutdown implementation
	 */
	//public void stop_crawl(){
		
	//}
	
	/**
	 * Main function to start XPathCrawler 
	 * @param args
	 */
	public static void main(String args[]) {
		/* TODO : Implementation of Web Crawler */
		if (args.length < 3 ) {
			// // System.out.println("Name : Aakriti Singla");
			// // System.out.println("PennID : aakritis");
			// // System.out.println("[Output from log4j][Usage Document] java XPathCrawler <web_seed_url> <env_dir> <maxsize_doc>");
			return;
		}
		
		String seed_url = args[0];
		String env_dir = args[1];
		int maxsize_doc = Integer.parseInt(args[2]);
		// default values for no of files to retrieve before stopping
		int num_files = 100;
		
		if (args.length == 4)
			num_files = Integer.parseInt(args[3]);
		
		XPathCrawler crawler_obj = new XPathCrawler(seed_url, env_dir, maxsize_doc, num_files);
		//Thread thread = new Thread (crawler_obj,"XPathCrawler Thread");
		//thread.start();
		crawler_obj.run();
	}
}
