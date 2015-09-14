package edu.upenn.cis455.storage;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {

	private static String env_directory = null;
	private static File dbdir_file;

	private static Environment my_env = null;
	private static EntityStore store = null;

	// index accessors
	private static PrimaryIndex<String, UserEntity> user_pindex;
	private static PrimaryIndex<String, UserChannelEntity> userchannel_pindex;
	private static PrimaryIndex<String, WebContentEntity> webcontent_pindex;
	// adding index accessor for RobotsEntity
	// private static PrimaryIndex<String, RobotsEntity> robots_pindex;

	/* TODO: write code to set up db context */
	/**
	 * to set up db enviornment for the passed root 
	 * @param env_root
	 */
	public static void setup_environment (String env_root) {
		// base case, if the same root exists
		// System.out.println("In SetUp Env");
		if (my_env != null && my_env.isValid() && env_root.equals(env_directory)) {
			// System.out.println("[Output from log4j] Environment Directory Already exists");
			return;
		}

		env_directory = env_root;
		// base case 
		if(env_directory == null)
			env_directory = System.getProperty("user.dir") + "/database";

		// create env_directory
		File tempdir_file = new File(env_directory);
		if (tempdir_file.exists())
			dbdir_file = tempdir_file;
		else {
			tempdir_file.mkdir();
			// System.out.println("[Output from log4j] Directory created for Database Storage + " + tempdir_file);
			dbdir_file = tempdir_file; 
		}

		// System.out.println("[Output from log4j] Database Started at + " + env_directory);

		// environment set up
		EnvironmentConfig environmentconfig_obj = new EnvironmentConfig();
		StoreConfig storeconfig_obj = new StoreConfig();

		environmentconfig_obj.setAllowCreate(true);
		environmentconfig_obj.setTransactional(true);

		storeconfig_obj.setAllowCreate(true);
		storeconfig_obj.setDeferredWrite(true);

		my_env = new Environment(dbdir_file,environmentconfig_obj);
		store = new EntityStore(my_env,"EntityStore",storeconfig_obj);

		user_pindex = store.getPrimaryIndex(String.class, UserEntity.class);
		userchannel_pindex = store.getPrimaryIndex(String.class, UserChannelEntity.class);
		webcontent_pindex = store.getPrimaryIndex(String.class, WebContentEntity.class);
	}

	/**
	 * Closing the databases
	 */
	public static void close() {
		try {
			if (store != null)
				store.close();
			if (my_env != null)
				my_env.close();
		}
		catch(DatabaseException dbex) {
			System.err.println("[Output from log4j] Error closing database + " + dbex);
		}
	}
	
	/**
	 * convert string to date format as per http rules 
	 * @param str
	 * @return
	 */
	public static Date get_date_from_header_string(String str){
		Date date = null;
		// defining standard formats for date type
		String[] format = {"EEEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM dd HH:mm:ss yyyy", "EEE, dd MMM yyyy HH:mm:ss zzz","EEE, dd MMM yyyy HH"};
		SimpleDateFormat parser = new SimpleDateFormat(format[0]);
		try {
			// if matches format 0 
			date = parser.parse(str);
		} 
		catch (ParseException e) {
			parser = new SimpleDateFormat(format[1]);
			try {
				// if matched format 1
				date = parser.parse(str);
			} 
			catch (ParseException e1) {
				parser = new SimpleDateFormat(format[2]);
				try {
					// if matched format 1
					date = parser.parse(str);
				} 
				catch (ParseException e2) {
					parser = new SimpleDateFormat(format[3]);
					try {
						// if matched format 1
						date = parser.parse(str);
					} 
					catch (ParseException e3) {
						System.err.println("[Output from log4j] Error in format of Date in DBWrapper + " + e3); 
						return null;
					}
				}
			}
		}
		return date;
	}

	/* TODO: write object store wrapper for BerkeleyDB */

	// start of Object store wrapper for WebContentEntity
	/**
	 * add webpage to WebContentEntity
	 * @param webpage
	 */
	public static void add_webcontent (WebContentEntity webpage) {
		if (webpage != null)
			webcontent_pindex.put(webpage);
		store.sync();
	}
	/**
	 * to fetch the requested webpage
	 * @param web_url
	 * @return
	 */
	public static WebContentEntity get_webpage (String web_url) {
		return webcontent_pindex.get(web_url);
	}
	/**
	 * to remove the requested webpage
	 * @param web_url
	 */
	public static void delete_webpage (String web_url) {
		webcontent_pindex.delete(web_url);
	}
	// end of Object store wrapper for WebContentEntity

	// start of Object store wrapper for UserEntity
	/**
	 * add user to UserEntity
	 * @param user
	 */
	public static void add_user (UserEntity user) {
		if (user != null)
			user_pindex.put(user);
		store.sync();
	}
	/**
	 * to fetch the requested user 
	 * @param username
	 * @return
	 */
	public static UserEntity get_user (String username) {
		if (has_user(username))
			return user_pindex.get(username);
		else 
			return null;
	}
	/**
	 * to check if requested user exists
	 * @param username
	 * @return
	 */
	public static boolean has_user (String username) {
		return  user_pindex.contains(username);
	}
	/**
	 * to authenticate user login into system
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean authenticate_user (String username, String password ) {
		if (has_user(username))
			if(get_user(username).get_user_pswd().equals(password))
				return true;
		return false;
	}
	// end of Object store wrapper for UserEntity

	// start of Object store wrapper for UserChannelEntity
	/**
	 * to add new channel 
	 * @param channel
	 */
	public static void add_channel (UserChannelEntity channel) {
		if (channel != null)
			userchannel_pindex.put(channel);
		store.sync();
	}
	/**
	 * to extract channel details from given channel name 
	 * @param channelname
	 * @return
	 */
	public static UserChannelEntity get_channel (String channelname) {
		if (has_channel(channelname))
			return userchannel_pindex.get(channelname);
		return null;
	}
	/**
	 * to check if the given channelname exists
	 * @param channelname
	 * @return
	 */
	public static boolean has_channel (String channelname) {
		return userchannel_pindex.contains(channelname);
	}
	/**
	 * to check if channelname exists for current logged in user
	 * @param username
	 * @param channelname
	 * @return
	 */
	public static boolean user_has_channel (String username, String channelname) {
		ArrayList<String> channels = new ArrayList<String>();
		channels = user_pindex.get(username).get_channel_names();
		return channels.contains(channelname);
	}
	/**
	 * to get list of all channelentity objects
	 * @return
	 */
	public static ArrayList<UserChannelEntity> get_all_channels() {
		ArrayList<UserChannelEntity> channels = new ArrayList<UserChannelEntity>();

		EntityCursor<UserChannelEntity> entity_cursor = userchannel_pindex.entities();
		try {
			Iterator<UserChannelEntity> iter_obj = entity_cursor.iterator();
			while (iter_obj.hasNext())
				channels.add((UserChannelEntity)iter_obj.next());
		}
		catch(Exception ex){
			// System.out.println("[Output from log4j] Error while iterating through UserChannelEntity + " + ex);
		}
		finally {
			entity_cursor.close();
		}

		return channels;
	}
	/**
	 * to delete the channel and omit same from user entity 
	 * @param user_name
	 * @param channelname
	 */
	public static void delete_channel (String username, String channelname) {
		userchannel_pindex.delete(channelname);
		UserEntity channel_owner = get_user(username) ;
		ArrayList<String> channels = channel_owner.get_channel_names();
		if(channels.contains(channelname))
			channel_owner.get_channel_names().remove(channelname);
		add_user(channel_owner);
	}
	// end of Object store wrapper for UserChannelEntity

}
