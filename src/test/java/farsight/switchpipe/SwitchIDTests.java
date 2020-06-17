package farsight.switchpipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class SwitchIDTests {

	@Test
	public void testServiceID() {
		assertEquals("serviceID", SwitchID.parse("invokeID$serviceID@storeID").serviceID);
	}

	@Test
	public void testInvokeID() {
		assertEquals("invokeID", SwitchID.parse("invokeID$serviceID@storeID").invokeID);
	}

	@Test
	public void testStoreID() {
		assertEquals("storeID", SwitchID.parse("invokeID$serviceID@storeID").storeID);
	}
	
	@Test
	public void testDateInput_Today() {
		assertEquals(LocalDate.now(), SwitchID.parse(">").date.toLocalDate());
	}

	@Test
	public void testDateInput_FixedDate() {
		assertEquals(LocalDate.parse("2019-01-15"), SwitchID.parse("190115T<").date.toLocalDate());
	}
	
	@Test
	public void testDateInput_FixedTime() {
		SwitchID id = SwitchID.parse("T20:00:01<");
		assertEquals(LocalTime.parse("20:00:01"), id.date.toLocalTime());
		assertEquals(LocalDate.now(), id.date.toLocalDate());
		assertTrue(id.isRelative());
		assertTrue(id.isFirst());
	}
	
	@Test
	public void testDateInput_Offset() {
		SwitchID id = SwitchID.parse(">10");
		assertEquals(LocalDate.now(), id.date.toLocalDate());
		assertEquals(10, id.offset);
	}
	
	@Test
	public void testWrite() {
		SwitchID id = SwitchID.parse("!specialID");
		assertTrue(id.isWrite());
		assertEquals("specialID", id.invokeID);
	}
	
	@Test
	public void testPrefix() {
		//final String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		SwitchID id = SwitchID.parse("01>");
		assertEquals(ChronoUnit.HOURS, id.precision);
		assertEquals("01", id.getPrefix("HHmmssSSS"));
		id = SwitchID.parse("01:22>");
		assertEquals(ChronoUnit.MINUTES, id.precision);
		assertEquals("0122", id.getPrefix("HHmmssSSS"));
		id = SwitchID.parse("01223>");
		assertEquals(ChronoUnit.SECONDS, id.precision);
		assertEquals("012230", id.getPrefix("HHmmssSSS"));
	}

	@Test
	public void testWriteQuery_generate() {
		SwitchID switchID = SwitchID.parse("!");
		assertEquals("", switchID.invokeID);
		assertTrue(switchID.isWrite());
	}
	
}
