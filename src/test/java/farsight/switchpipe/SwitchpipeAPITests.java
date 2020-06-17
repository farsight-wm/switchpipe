package farsight.switchpipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.util.coder.IDataXMLCoder;

public class SwitchpipeAPITests {

	private static SwitchpipeAPI api;

	@BeforeClass
	public static void initTestClass() {
		SwitchpipeAPI.CONFIG_LOCATIONS = new String[] { "src/test/resources/switchpipe.conf", };
		api = SwitchpipeAPI.defaultInstance();
		api.initialize();
		new IDataXMLCoder(); // do not count time for initializing for single tests...
	}

	@Test
	public void testRestoreFromFile() throws ServiceException {
		IData data = api.loadPipeline("key2", "svcID", "pipeline");
		assertNotNull(data);
		IDataMap map = new IDataMap(data);
		assertEquals("fromFile", map.getAsString("key"));
	}

	@Test
	public void testRestoreFromZip() throws ServiceException {
		IData data = api.loadPipeline("191031-123456789", "svcID", "pipeline");
		assertNotNull(data);
		IDataMap map = new IDataMap(data);
		assertEquals("fromZip", map.getAsString("key"));
	}

	@Test
	public void testLatestPipeline() throws ServiceException {
		IData data = api.loadPipeline("191106T>", "test", "pipeline");
		assertNotNull(data);
		IDataMap map = new IDataMap(data);
		assertEquals("191106-180513358", map.getAsString("timestamp"));
	}

	@Test
	public void testSecondPipeline() throws ServiceException {
		IData data = api.loadPipeline("191106T<1", "test", "pipeline");
		assertNotNull(data);
		IDataMap map = new IDataMap(data);
		assertEquals("191106-164206315", map.getAsString("timestamp"));
	}

	@Test
	public void testSearchInZip() throws ServiceException {
		IData data = api.loadPipeline("191031T<", "svcID", "pipeline");
		assertNotNull(data);
		IDataMap map = new IDataMap(data);
		assertEquals("fromZip", map.getAsString("key"));
	}

	@Test
	public void testFindPipeline() throws ServiceException {
		SwitchID switchID = api.findPipeline("191031T<", "svcID", "pipeline");
		assertNotNull(switchID);
		assertEquals("191031-123456789", switchID.invokeID);
	}
}
