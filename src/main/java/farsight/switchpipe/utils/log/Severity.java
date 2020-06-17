package farsight.switchpipe.utils.log;

import com.wm.util.JournalLogger;

public enum Severity {
	 FATAL(JournalLogger.CRITICAL)
	,ERROR(JournalLogger.ERROR)
	,WARNING(JournalLogger.WARNING)
	,INFO(JournalLogger.INFO)
	,DEBUG(JournalLogger.VERBOSE2)
	,TRACE(JournalLogger.VERBOSE5);
	
	public final int wmLogLevel;
	Severity(int wmLevel) {
		 wmLogLevel = wmLevel;
	}
}
