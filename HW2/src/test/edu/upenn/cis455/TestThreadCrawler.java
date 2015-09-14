/**
 * 
 */
package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.crawler.PolitenessModule;
import edu.upenn.cis455.crawler.URLProcessorModule;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserChannelEntity;
import edu.upenn.cis455.storage.WebContentEntity;

/**
 * @author cis455
 *
 */
public class TestThreadCrawler extends TestCase {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		DBWrapper.setup_environment("/home/cis455/database");
		
	}

	/**
	 * Test method for {@link edu.upenn.cis455.crawler.ThreadCrawler#run()}.
	 */
	@Test
	public void testRunXMLUrl() {
		// check add channel
		ArrayList<String> xpath = new ArrayList<String>();
		xpath.add("/dwml/head/product");
		UserChannelEntity channel = new UserChannelEntity("Weather", xpath, "test.xsl");
		DBWrapper.add_channel(channel);
		boolean has_channel = DBWrapper.has_channel("Weather");
		assertEquals(true,has_channel);
		// run crawler
		String seed_url = "https://dbappserv.cis.upenn.edu/crawltest/misc/weather.xml";
		// check for xml file 
		PolitenessModule politeness = new PolitenessModule();
		URLProcessorModule processor = new URLProcessorModule();
		try {
			WebContentEntity webpage = politeness.fetch_webpage(seed_url);
			processor.process_webpage(webpage);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
		}
		UserChannelEntity channel_fetch = DBWrapper.get_channel("Weather");
		ArrayList<String> extend_url = channel_fetch.get_extend_urls();
		assertEquals(1,extend_url.size());
		assertEquals("https://dbappserv.cis.upenn.edu/crawltest/misc/weather.xml", extend_url.get(0));
	}
	
	@Test
	public void testRunDisallowedUrl() {
		// check add channel
		String seed_url = "https://dbappserv.cis.upenn.edu/crawltest/marie/private/";
		//String seed_url = "https://dbappserv.cis.upenn.edu/crawltest/marie/private/middleeast.xml";
		// check for xml file 
		PolitenessModule politeness = new PolitenessModule();
		URLProcessorModule processor = new URLProcessorModule();
		try {
			WebContentEntity webpage = politeness.fetch_webpage(seed_url);
			processor.process_webpage(webpage);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
		}
		WebContentEntity webpage = DBWrapper.get_webpage("https://dbappserv.cis.upenn.edu/crawltest/marie/private/middleeast.xml");
		assertEquals(null,webpage);
	}
	
}
