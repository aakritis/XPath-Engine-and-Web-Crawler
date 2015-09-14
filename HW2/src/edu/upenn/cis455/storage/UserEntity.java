package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.ArrayList;

/**
 * Storage System for Berkley-DB :
 * Entity Name : UserEntity
 * Columns : user_name, user_pswd, channel_names 
 * 
 * @author : Aakriti Singla
 * */
@Entity
public class UserEntity {

	@PrimaryKey
	private String user_name;
	private String user_pswd;
	
	private ArrayList<String> channel_names = new ArrayList<String>();
	/**
	 * Default Constructor
	 */
	public UserEntity(){
		
	}
	/**
	 * Parameterized Constructor : to insert user_name and user_pswd
	 * @param user_name
	 * @param user_pswd
	 */
	public UserEntity(String user_name, String user_pswd) {
		this.set_user_name(user_name);
		this.set_user_pswd(user_pswd);
	}
	
	/**
	 * Getter function for column user_name
	 * @return user_name
	 */
	public String get_user_name() {
		return this.user_name;
	}
	
	/**
	 * Setter function for column user_name
	 * @param user_name
	 */
	public void set_user_name(String user_name) {
		this.user_name = user_name;
	}
	
	/**
	 * Getter function for column user_pswd
	 * @return user_pswd
	 */
	public String get_user_pswd() {
		return this.user_pswd;
	}
	
	/**
	 * Setter function for column user_pswd
	 * @param user_pswd
	 */
	public void set_user_pswd(String user_pswd) {
		this.user_pswd = user_pswd;
	}
	
	/**
	 * to add new channel name to existing ArrayList of channel_names
	 * @param channel_name
	 */
	public void addchannel_names (String channel_name) {
		this.channel_names.add(channel_name);
	}
	/**
	 * @return the channel_names
	 */
	public ArrayList<String> get_channel_names() {
		return channel_names;
	}
	/**
	 * @param channel_names the channel_names to set
	 */
	public void set_channel_names(ArrayList<String> channel_names) {
		this.channel_names = channel_names;
	}
	/**
	 * to check whether the user has the required channel 
	 * @param channel - primary key of ChannelEntity
	 * @return true/false
	 */
	public boolean is_user_channel (UserChannelEntity channel) {
		return this.channel_names.contains(channel);
	}
}
