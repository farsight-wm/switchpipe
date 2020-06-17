package farsight.switchpipe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.StringTokenizer;


public class SwitchID {

	public static final char CHAR_STORE_ID = '@', CHAR_SERVICE_ID = '$', CHAR_TAIL = '<', CHAR_HEAD = '>';
	public static final int MODE_EXACT_QUERY = 0;
	public static final int MODE_RELATIVE_HEAD_QUERY = CHAR_HEAD;
	public static final int MODE_RELATIVE_TAIL_QUERY = CHAR_TAIL;
	public static final int MODE_WRITE_QUERY = '!';
	
	/*
	 * * legal patterns: <ID> = (?i)[a-z9-9_-.] <NSID> = <ID> with [.:]
	 * 
	 * <ID> <ID>@<ID> <ID>=<ID> <ID>=<ID>@<ID>
	 * 
	 * <invokeID> <invokeID>$<serviceID> <invokeID>@<storeID>
	 * 
	 * <invokeID> = <exacatInvokeID> | <relativeInvokeID> <exactInvokeID> =
	 * <namedID> | <timestampID> <relativeInvokeID> = (<partialDate>)? ( '<' |
	 * '>' ) (<offset>) ?
	 * 
	 * <partial> = <partialTime> | <partialDate> 'T' <partialTime> <partialTime>
	 * = ( hh | hhmm | hhmmss )( '-' <offsetDays> )? <partialDate> = dd | mmdd |
	 * yy?yymmdd
	 * 
	 * hh hhmm hhmmss -"- '-' x
	 * 
	 * 
	 * yyyyMMdd yyyyMMdd'T'
	 * 
	 * 
	 * Special - Timeed Search patterns < first (from today) > last ><num> >{x}
	 * <timestamp> > Examples:
	 * 
	 * 
	 * 
	 */

	public final String invokeID, serviceID, storeID;
	public final int offset;
	public final LocalDateTime date;
	public final ChronoUnit precision;
	private final int mode;

	private SwitchID(int mode, String invokeID, String serviceID, String storeID) {
		this.invokeID = invokeID;
		this.serviceID = serviceID;
		this.storeID = storeID;
		this.date = null;
		this.precision = null;
		this.offset = 0;
		this.mode = mode;
	}

	private SwitchID(int mode, LocalDateTime date, ChronoUnit precision, int offset, String serviceID, String storeID) {
		this.invokeID = null;
		this.serviceID = serviceID;
		this.storeID = storeID;
		this.date = date;
		this.precision = precision;
		this.offset = offset;
		this.mode = mode;
	}

	// public API
	
	public boolean isRelative() {
		return mode == MODE_RELATIVE_HEAD_QUERY || mode == MODE_RELATIVE_TAIL_QUERY;
	}
	
	public boolean isWrite() {
		return mode == MODE_WRITE_QUERY;
	}
	
	public String getStoreID(String fallbackStoreID) {
		return storeID == null ? fallbackStoreID : storeID;
	}
	
	public String getServiceID(String fallbackServiceID) {
		return serviceID == null ? fallbackServiceID : serviceID;
	}

	public boolean isLatest() {
		return mode == MODE_RELATIVE_HEAD_QUERY;
	}
	
	public boolean isFirst() {
		return mode == MODE_RELATIVE_TAIL_QUERY;
	}
	
	public boolean isExact() {
		return mode == MODE_EXACT_QUERY;
	}
	
	public static String mapServiceName(String serviceName) {
		if(serviceName == null)
			return null;
		if(serviceName.contains(":")) {
			//nsname
			return serviceName.replaceAll("\\.|\\:", "/");
		} else
		// XXX legacy quote handling... may be removed later
		if (serviceName.contains("\"")) {
			// handle quotes
			StringBuilder buf = new StringBuilder();
			String[] quotedParts = serviceName.split("\"");
			for (int i = 0; i < quotedParts.length; i++) {
				if (i % 2 == 0) {
					// unquoted
					buf.append(quotedParts[i].replaceAll("\\.|\\:", "/"));
				} else {
					// quoted
					buf.append(quotedParts[i]);
				}
			}
			return buf.toString();
		} else {
			// serviceID
			return serviceName;
		}
	}

	public String servicePath() {
		return mapServiceName(serviceID);
	}

	public String getPrefix(String timstampPattern) {
		//dynamically remove pattern digits
		switch(precision) {
		case DAYS:
			timstampPattern = timstampPattern.replace("H", "");//cut hours
		case HOURS:
			timstampPattern = timstampPattern.replace("m", "");//cut minutes
		case MINUTES:
			timstampPattern = timstampPattern.replace("s", "");//cut sec
		case SECONDS:
		default:
			timstampPattern = timstampPattern.replace("S", "");//cut ms
		}

		return date.format(DateTimeFormatter.ofPattern(timstampPattern));
	}
	
	// === parse SwitchID ===	
	
	public static SwitchID parse(String invokeID) {
		return parse(invokeID, null, null);
	}
	
	public static SwitchID parse(String string, String serviceID, String storeID) {
		if(string == null || string.isEmpty()) {
			return createAutogenerateID(serviceID, storeID);
		}
		
		StringTokenizer tokenizer = new StringTokenizer(string, "" + CHAR_SERVICE_ID + CHAR_STORE_ID, true);
		
		String invokeID = tokenizer.nextToken();
		String token;

		char tokenType = CHAR_SERVICE_ID;
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			char first = token.charAt(0);
			switch (first) {
			case CHAR_SERVICE_ID:
			case CHAR_STORE_ID:
				tokenType = first;
				break;
			default:
				if (tokenType == CHAR_SERVICE_ID) {
					serviceID = token;
				} else {
					storeID = token;
				}
			}
		}
		
		int mode, offset, pos = 0;
		if ((pos = invokeID.indexOf(CHAR_TAIL)) > -1) {
			// tail mode
			mode = MODE_RELATIVE_TAIL_QUERY;
		} else if ((pos = invokeID.indexOf(CHAR_HEAD)) > -1) {
			// head mode
			mode = MODE_RELATIVE_HEAD_QUERY;
		} else {
			if(invokeID.charAt(0) == '!') {
				//write mode
				mode = MODE_WRITE_QUERY;
				invokeID = invokeID.substring(1);
			} else {
				// exact mode
				mode = MODE_EXACT_QUERY;
			}
			return new SwitchID(mode, invokeID, serviceID, storeID);
		}
		
		//parse offset, timestamp and get precision
		String timestamp = invokeID.substring(0, pos);
		offset = parseInt(invokeID.substring(pos + 1), 0);
		
		LocalDateTime date = null;
		ChronoUnit precision = null;

		if (timestamp == null || timestamp.isEmpty()) {
			//not given -> assume today
			precision = ChronoUnit.DAYS;
			date = LocalDateTime.now().truncatedTo(precision);
		} else {
			boolean isOffset = timestamp.charAt(0) == '-';
			if(isOffset) timestamp = timestamp.substring(1);
			
			String datePart = null;
			String timePart = null;
			
			pos = timestamp.indexOf('T');
			if(pos == -1) {
				//only time
				datePart = "";
				timePart = timestamp;
			} else {
				datePart = timestamp.substring(0, pos);
				timePart = timestamp.substring(pos + 1);
			}
			
			//parse time
			if(timePart.indexOf(':') > 0) {
				//notation with ':'
				String[] parts = timePart.split(":|\\.");
				
				switch(parts.length) {
				case 0:
					precision = ChronoUnit.HOURS;
					timePart = "000000000";
					break;
				case 1:
					precision = ChronoUnit.HOURS;
					timePart = padTimeComponent(parts[0], "00", true) + "0000000";
					break;
				case 2:
					precision = ChronoUnit.MINUTES;
					timePart = padTimeComponent(parts[0], "00", true) + padTimeComponent(parts[1], "00", true) + "00000";
					break;
				default:
					precision = ChronoUnit.SECONDS;
					timePart = padTimeComponent(parts[0], "00", true) + padTimeComponent(parts[1], "00", true) +  padTimeComponent(parts[2], "00", true) + "000";
				}
			} else if(timePart.isEmpty()) {
					precision = ChronoUnit.DAYS;
					timePart = "000000000";
			} else {
				//length dependent
				int l = timePart.length();
				if(l > 4)
					precision = ChronoUnit.SECONDS;
				else if(l > 2)
					precision = ChronoUnit.MINUTES;
				else
					precision = ChronoUnit.HOURS;
				
				//this is required otherwise String "1" to be interpreted as "10:00"
				if(l == 1)
					timePart = "0" + timePart;
				
				timePart = padTimeComponent(timePart, "000000", false) + "000";
			}
			
			LocalTime time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HHmmssSSS"));
			if(isOffset) {
				int days = 0;
				if(!datePart.isEmpty()) {
					days = parseInt(datePart, 0);
				}
				
				System.out.println(time);
				date = LocalDateTime.now().truncatedTo(precision).minusDays(days).minusHours(time.getHour())
						.minusMinutes(time.getMinute()).minusSeconds(time.getSecond());
			} else {
				datePart = padTimeComponent(datePart.replace("-", ""), LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), true);
				date = LocalDate.parse(datePart, DateTimeFormatter.BASIC_ISO_DATE).atTime(time);
			}
		}
		
		return new SwitchID(mode, date, precision, offset, serviceID, storeID);
	}
	
	public static SwitchID createAutogenerateID(String serviceID, String storeID) {
		return new SwitchID(MODE_WRITE_QUERY, null, serviceID, storeID);
	}
	
	public static SwitchID createExactQuery(String invokeID, String serviceID, String storeID) {
		return new SwitchID(MODE_EXACT_QUERY, invokeID, serviceID, storeID);
	}

	private static String padTimeComponent(String src, String pattern, boolean right) {
		int l_p = pattern.length(), l_s = src.length();
		
		if(l_p > l_s) {
			if(right)
				return pattern.substring(0, l_p - l_s) + src;
			else
				return src + pattern.substring(l_s);
		} else if(l_p == l_s) {
			return src;
		} else {
			if(right)
				return src.substring(0, l_p);
			else
				return src.substring(l_s - l_p);
		}
	}
	
	private static int parseInt(String str, int defaultValue) {
		try {
			return Integer.valueOf(str, 10);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	// === toString ===
	
	public String toString() {
		return buildInvokeID() + buildServiceID() + buildStoreID();
	}
	
	private String buildStoreID() {
		return storeID == null ? "" : CHAR_STORE_ID + storeID;
	}

	private String buildServiceID() {
		return serviceID == null ? "" : CHAR_SERVICE_ID + serviceID;
	}

	private String buildInvokeID() {
		if (isRelative()) {
			// hasDate? hasTime?
			// hasOffset?
			return date.format(DateTimeFormatter.BASIC_ISO_DATE) + (char) mode + (offset > 0 ? offset : "");
		} else {
			return invokeID;
		}
	}

}
