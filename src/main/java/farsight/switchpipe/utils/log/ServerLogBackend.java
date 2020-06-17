package farsight.switchpipe.utils.log;

import com.wm.util.JournalLogger;

public class ServerLogBackend implements LogBackend {
	
	public static LogFacade createFor(String function) {
		return createFor(function, 4);
	}
	
	public static LogFacade createFor(String function, int defaultCode) {
		return new LogFacade(new ServerLogBackend(JournalLogger.FAC_FLOW_SVC, defaultCode, function));
	}
	
	private final int facility;
	private final int defaultCode;
	private final String function;
	
	public ServerLogBackend(int facility, int defaultCode, String function) {
		this.facility = facility;
		this.defaultCode = defaultCode;
		this.function = function;
	}
	
	//static interface
	
	private static void log(int code, int fac, Severity severity, String function, String message) {
		try {
			JournalLogger.log(code, fac, severity.wmLogLevel, function, message);
		} catch (Exception e) {} //ignore
	}

	// instance interface

	public void log(Severity severity, String message) {
		log(defaultCode, facility, severity, function, message);
	}
	
	public boolean enabled(Severity severity) {
		return JournalLogger.isLogEnabled(facility, severity.wmLogLevel);
	}



}
