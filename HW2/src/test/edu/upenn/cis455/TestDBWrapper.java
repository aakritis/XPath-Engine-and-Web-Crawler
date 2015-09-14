/**
 * 
 */
package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserEntity;
import edu.upenn.cis455.storage.WebContentEntity;

/**
 * @author cis455
 *
 */
@SuppressWarnings("deprecation")
public class TestDBWrapper extends TestCase {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		DBWrapper.setup_environment("/home/cis455/database");
	}

	/**
	 * Test method for {@link edu.upenn.cis455.storage.DBWrapper#delete_webpage(java.lang.String)}.
	 */
	@Test
	public void testDelete_webpage() {
		double size = 100;
		WebContentEntity webpage = new WebContentEntity("https://dbappserv.cis.upenn.edu/crawltest.html", new Date(), "<html><body></body></html>", "text/html", size);
		DBWrapper.add_webcontent(webpage);
		// check extract web page
		WebContentEntity testwebpage = DBWrapper.get_webpage("https://dbappserv.cis.upenn.edu/crawltest.html");
		assertEquals("https://dbappserv.cis.upenn.edu/crawltest.html",testwebpage.get_absolute_url());
		// check delete web page
		DBWrapper.delete_webpage("https://dbappserv.cis.upenn.edu/crawltest.html");
		WebContentEntity testnullpage = DBWrapper.get_webpage("https://dbappserv.cis.upenn.edu/crawltest.html");
		assertEquals(null,testnullpage);
	}

	/**
	 * Test method for {@link edu.upenn.cis455.storage.DBWrapper#add_user(edu.upenn.cis455.storage.UserEntity)}.
	 */
	@Test
	public void testAdd_user() {
		
		String username = "aakritis";
		String password = "dushangarg4490";
		UserEntity user_obj = new UserEntity(username,password);
		DBWrapper.add_user(user_obj);
		// test for exists user
		boolean has_user = DBWrapper.has_user("aakritis");
		assertEquals(true, has_user);
		// test for authentication
		boolean is_authentic = DBWrapper.authenticate_user("aakritis", "dushangarg4490");
		assertEquals(true,is_authentic);
	}

}
