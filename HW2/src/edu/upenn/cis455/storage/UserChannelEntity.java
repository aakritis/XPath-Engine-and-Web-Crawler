package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.ArrayList;

/**
 * Storage System for Berkley-DB :
 * Entity Name : ChannelEntity
 * Columns : channel_name, xpaths, xsl_url, extend_urls 
 * 
 * @author : Aakriti Singla
 * */
@Entity
public class UserChannelEntity {
	
	@PrimaryKey
	private String channel_name;
	private ArrayList<String> xpaths = new ArrayList<String>();
	private String xsl_url;
	
	private ArrayList<String> extend_urls = new ArrayList<String>(); 
	
	/**
	 * Default Constructor
	 */
	public UserChannelEntity(){
		
	}
	/**
	 * Parameteized Constructor
	 * @param channel_name
	 * @param xpaths
	 * @param xsl_url
	 */
	public UserChannelEntity (String channel_name , ArrayList<String> xpaths, String xsl_url) {
		this.set_channel_name(channel_name);
		this.set_xpaths(xpaths);
		this.set_xsl_url(xsl_url);
	}

	/**
	 * @return the channel_name
	 */
	public String get_channel_name() {
		return this.channel_name;
	}

	/**
	 * @param channel_name the channel_name to set
	 */
	public void set_channel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	/**
	 * @return the xpaths
	 */
	public ArrayList<String> get_xpaths() {
		return this.xpaths;
	}

	/**
	 * @param xpaths the xpaths to set
	 */
	public void set_xpaths(ArrayList<String> xpaths) {
		this.xpaths = xpaths;
	}

	/**
	 * @return the xsl_url
	 */
	public String get_xsl_url() {
		return this.xsl_url;
	}

	/**
	 * @param xsl_url the xsl_url to set
	 */
	public void set_xsl_url(String xsl_url) {
		this.xsl_url = xsl_url;
	}
	
	/**
	 * @return the extend_urls
	 */
	public ArrayList<String> get_extend_urls() {
		return extend_urls;
	}
	/**
	 * @param extend_urls the extend_urls to set
	 */
	public void set_extend_urls(ArrayList<String> extend_urls) {
		this.extend_urls = extend_urls;
	}
	
	/**
	 * add matching url to the list of extend urls for the given channel
	 * @param matched_url
	 */
	public void add_extend_urls (String matched_url) {
		if(extend_urls == null)
			extend_urls = new ArrayList<String>();
		this.extend_urls.add(matched_url);
	}
}
