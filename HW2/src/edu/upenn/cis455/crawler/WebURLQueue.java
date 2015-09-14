package edu.upenn.cis455.crawler;

/**
 * Class to create synchronized enqueue/dequeue functions for site urls
 * @author Aakriti Singla
 *
 */

public class WebURLQueue {
	
	private String removed_site_url;
	/**
	 * Default Constructor
	 */
	public WebURLQueue() {
		this.removed_site_url = null;
	}
	
	/**
	 * function to enqueue crawled site urls to sync queued
	 * @param site_url
	 */
	public void addToQueue(String site_url) {
		try{
			// synchronized block to add site url to shared queue
			/* for multithreading */
			/*
			synchronized (XPathCrawler.url_queue) {
				XPathCrawler.url_queue.add(site_url);
				XPathCrawler.url_queue.notify();
			}*/
			XPathCrawler.url_queue.add(site_url);
		}
		catch(Exception e) {
			System.err.println(" [Output from log4j] Error from enqueue function in WebURLQueue " + e);
		}
	}
	/**
	 * function to dequeue crawled site to read contents from the url
	 * @return
	 */
	public String readFromQueue() {
		try {
			/*for multi threading*/
			/*
			// if queue is empty, no url to crawl, wait
			while (XPathCrawler.url_queue.isEmpty()) {
				synchronized (XPathCrawler.url_queue) {
					XPathCrawler.url_queue.wait();
				}
			}
			// else remove first element from queue
			synchronized (XPathCrawler.url_queue) {
				// removes the first element from the queue
				// returns null if there is no element 
				removed_site_url = XPathCrawler.url_queue.poll();
				if (removed_site_url != null)
					XPathCrawler.url_queue.notify();
				return removed_site_url;
			}
			*/
			this.removed_site_url = XPathCrawler.url_queue.poll();
			return this.removed_site_url;
		} 
		catch (Exception e) {
			System.err.println(" [Output from log4j] Error from dequeue function in WebURLQueue "+ e);
			return null;
		}
	}
	/**
	 * Getter function for removed_site_url
	 * @return
	 */
	public String get_removed_site_url(){
		return this.removed_site_url;
	}
}
