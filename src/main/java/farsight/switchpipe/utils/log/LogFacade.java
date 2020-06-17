package farsight.switchpipe.utils.log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogFacade implements LogBackend {
	
	private final LogBackend lbe;

	public LogFacade(LogBackend logInterface) {
		this.lbe = logInterface;
	}
	
	// log interface

	public void log(Severity severity, String message) {
		lbe.log(severity, message);
	}
	
	public boolean enabled(Severity severity) {
		return lbe.enabled(severity);
	}
	
	// extended functionality

	public void logList(Severity severity, String message, Object... args) {
		StringBuilder b = new StringBuilder(message);
		for(Object o: args) {
			b.append(" ");
			b.append(String.valueOf(o));
		}
		log(severity, b.toString());
	}
	
	public void logFormated(String format, Severity severity, Object... args) {
		try {
			String message = String.format(format, args);
			log(severity, message);
		} catch(Exception e) {
			log(e);
		}
	}
	
	public <T extends Throwable> T log(T throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		log(Severity.ERROR, sw.toString());
		return throwable;
	}

	//shortcuts
	
	public boolean errorEnabled() {
		return enabled(Severity.ERROR);
	}

	public boolean warningEnabled() {
		return enabled(Severity.WARNING);
	}

	public boolean infoEnabled() {
		return enabled(Severity.INFO);
	}

	public boolean debugEnabled() {
		return enabled(Severity.DEBUG);
	}

	public boolean traceEnabled() {
		return enabled(Severity.TRACE);
	}

	public void logError(String message) {
		log(Severity.ERROR, message);
	}

	public void logWarning(String message) {
		log(Severity.WARNING, message);
	}

	public void logInfo(String message) {
		log(Severity.INFO, message);
	}

	public void logDebug(String message) {
		log(Severity.DEBUG, message);
	}

	public void logTrace(String message) {
		log(Severity.TRACE, message);
	}

}
