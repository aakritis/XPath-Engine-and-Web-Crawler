package edu.upenn.cis455.crawler;

import java.io.IOException;

import edu.upenn.cis455.storage.WebContentEntity;


@SuppressWarnings("unused")
public class ThreadCrawler //extends Thread {
{

	/**
	 * to keep count of no of files crawled by the crawler
	 * static Integer object for synchronization purposes 
	 */
	protected static Integer num_files_crawled = 0;
	// Queue class to enqueue / dequeue Urls in sync block for processing
	protected WebURLQueue weburl_queue = null;
	// check protocol (http/https), check politeness and extract webcontents
	protected PolitenessModule politeness;
	protected URLProcessorModule processor;
	/**
	 * Default Constructor
	 */
	public ThreadCrawler(){
		weburl_queue = new WebURLQueue();
		politeness = new PolitenessModule();
		processor = new URLProcessorModule();
	}
	/**
	 * Function to implement run function from Thread class (thread.start()) 
	 */
	public void run() {
		try {
			// // System.out.println("[Output from log4j] In run method of ThreadCrawler");
			// crawling till the time the site url queue is not empty
			while (! XPathCrawler.url_queue.isEmpty() ) {
				try {
					// dequeue url from the sync url queue (Step 1)
					// // // System.out.println("[Output from log4j] Before reading from queue");
					String dequeued_url = weburl_queue.readFromQueue();
					// System.out.println("[DEBUG] Dequeue: "+dequeued_url);
					// // // System.out.println("[Output from log4j] Before Politeness");
					// check for politeness and return WebContentEntity object (Step 2)
					WebContentEntity webpage = politeness.fetch_webpage(dequeued_url);
					// // System.out.println("[Output from log4j] In Thread Crawler, After politeness +" );	// +  webpage.get_url_content());
					// Processing current url and adding new urls to sync queue (Step 3)

					processor.process_webpage(webpage);

					// increement num_files_crawled in the synchronization block
					//num_files_crawled++;

					// check termination condition for crawler
					// // System.out.println("[Output from log4j] In Thread Crawler, number of files crawled till now + " + num_files_crawled);
					// // System.out.println("[Output from log4j] In Thread Crawler, number of files allowed + " + XPathCrawler.num_files);
					// // System.out.println("[Output from log4j] In Thread Crawler, Is Queue Empty ? + " + XPathCrawler.url_queue.isEmpty());
					if (num_files_crawled >= XPathCrawler.num_files)
						// stop crawling when allowable no of files to be extracted is reached 
						// call shutdown for the crawler in XPathCrawler
						break;
				}
				catch(Exception ex){
					// System.out.println(ex);
					ex.printStackTrace();
				}
			}
		} 
		catch (Exception ioex){
			// may throw an IO exception
			// // System.out.println("[Output from log4j] Error in run function of ThreadCrawler + " + ioex);
		}
		finally {

		}
	}

}
