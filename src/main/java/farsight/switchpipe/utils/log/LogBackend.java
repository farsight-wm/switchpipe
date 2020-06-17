package farsight.switchpipe.utils.log;

public interface LogBackend {
	
	public void log(Severity severity, String message);
	public boolean enabled(Severity severity);

}
