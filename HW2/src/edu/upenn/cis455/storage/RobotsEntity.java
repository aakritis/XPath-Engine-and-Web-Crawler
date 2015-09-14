package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Storage System for Berkley-DB :
 * Entity Name : RobotsEntity
 * Columns : domain_name , allowed_urls, disallowed_urls, crawler_interval, nextcrawl_interval; 
 * 
 * @author : Aakriti Singla
 * */
@Entity
public class RobotsEntity {
	@PrimaryKey
	private String domain_name;
	private String allowed_urls;
	private String disallowed_urls;
	private Long crawler_interval;
	private Long nextcrawl_interval;
	
	/**
	 * Getter for domain_name
	 * @return the domain_name
	 */
	public String get_domain_name() {
		return domain_name;
	}
	/**
	 * Setter for domain_name
	 * @param domain_name the domain_name to set
	 */
	public void set_domain_name(String domain_name) {
		this.domain_name = domain_name;
	}
	/**
	 * Getter for disallowed_urls
	 * @return the disallowed_urls
	 */
	public String get_disallowed_urls() {
		return disallowed_urls;
	}
	/**
	 * Setter for disallowed_urls
	 * @param disallowed_urls the disallowed_urls to set
	 */
	public void set_disallowed_urls(String disallowed_urls) {
		this.disallowed_urls = disallowed_urls;
	}
	/**
	 * Getter for allowed_urls
	 * @return the allowed_urls
	 */
	public String get_allowed_urls() {
		return allowed_urls;
	}
	/**
	 * Setter for allowed_urls 
	 * @param allowed_urls the allowed_urls to set
	 */
	public void set_allowed_urls(String allowed_urls) {
		this.allowed_urls = allowed_urls;
	}
	/**
	 * Getter for crawler_interval
	 * @return the crawler_interval
	 */
	public Long get_crawler_interval() {
		return crawler_interval;
	}
	/**
	 * Setter for crawler_interval
	 * @param crawler_interval the crawler_interval to set
	 */
	public void set_crawler_interval(Long crawler_interval) {
		this.crawler_interval = crawler_interval;
	}
	/**
	 * Getter for nextcrawl_interval
	 * @return the nextcrawl_interval
	 */
	public Long get_nextcrawl_interval() {
		return nextcrawl_interval;
	}
	/**
	 * Setter for nextcrawl_interval
	 * @param nextcrawl_interval the nextcrawl_interval to set
	 */
	public void set_nextcrawl_interval(Long nextcrawl_interval) {
		this.nextcrawl_interval = nextcrawl_interval;
	}
}
