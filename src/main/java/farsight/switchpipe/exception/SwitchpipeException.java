package farsight.switchpipe.exception;

public class SwitchpipeException extends Exception {

	private static final long serialVersionUID = -5130018762388207233L;

	public SwitchpipeException(String message) {
		super(message);
	}
	
	public SwitchpipeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static SwitchpipeException noConfigurationFound() {
		return new SwitchpipeException("No configuration file could be found");
	}	
	
}
