package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Storage System for Berkley-DB :
 * Entity Name : UserEntity
 * Columns : user_name, user_pswd, channel_names 
 * 
 * @author : Aakriti Singla
 * */

@Entity
public class WebContentEntity {

	@PrimaryKey
	private String absolute_url;
	private Date last_crawled_date;
	private String url_content;
	private String content_type;
	private Double content_size;
	
	/**
	 * Default Constructor
	 */
	public WebContentEntity(){
		
	}
	
	/**
	 * Parameterized Constructor
	 * @param absolute_url
	 * @param last_crawled_date
	 * @param url_content
	 * @param content_type
	 * @param content_size
	 */
	public WebContentEntity(String absolute_url, Date last_crawled_date, String url_content, String content_type, Double content_size) {
		this.set_absolute_url(absolute_url);
		this.set_last_crawled_date(last_crawled_date);
		this.set_url_content(url_content);
		this.set_content_type(content_type);
		this.set_content_size(content_size);
	}

	/**
	 * @return the absolute_url
	 */
	public String get_absolute_url() {
		return this.absolute_url;
	}

	/**
	 * @param absolute_url the absolute_url to set
	 */
	public void set_absolute_url(String absolute_url) {
		this.absolute_url = absolute_url;
	}

	/**
	 * @return the last_crawled_date
	 */
	public Date get_last_crawled_date() {
		return this.last_crawled_date;
	}

	/**
	 * @param last_crawled_date the last_crawled_date to set
	 */
	public void set_last_crawled_date(Date last_crawled_date) {
		this.last_crawled_date = last_crawled_date;
	}

	/**
	 * @return the url_content
	 */
	public String get_url_content() {
		return this.url_content;
	}

	/**
	 * @param url_content the url_content to set
	 */
	public void set_url_content(String url_content) {
		this.url_content = url_content;
	}

	/**
	 * @return the content_type
	 */
	public String get_content_type() {
		return this.content_type;
	}

	/**
	 * @param content_type the content_type to set
	 */
	public void set_content_type(String content_type) {
		this.content_type = content_type;
	}
	
	/**
	 * @return the content_size
	 */
	public Double get_content_size() {
		return this.content_size;
	}

	/**
	 * @param content_size the content_size to set
	 */
	public void set_content_size(Double content_size) {
		this.content_size = content_size;
	}
}
