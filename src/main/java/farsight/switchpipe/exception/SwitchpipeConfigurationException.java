package farsight.switchpipe.exception;

import java.nio.file.Path;

public class SwitchpipeConfigurationException extends SwitchpipeException {

	private static final long serialVersionUID = 1L;

	public SwitchpipeConfigurationException(String message) {
		super(message);
	}

	public SwitchpipeConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public static SwitchpipeConfigurationException classNotFound(String className, Throwable cause) {
		return new SwitchpipeConfigurationException(String.format("Unable to load class: %s", className), cause);
	}

	public static SwitchpipeConfigurationException classNotValid(Class<?> clazz, Class<?> base) {
		return new SwitchpipeConfigurationException(String.format("Class: %s is not assignable from %s", clazz, base));
	}

	public static SwitchpipeConfigurationException cannotReadConfiguration(Path path, Throwable cause) {
		return new SwitchpipeConfigurationException(String.format("Cannot read configuration from: %s", path), cause);
	}

	public static SwitchpipeConfigurationException cannotCreateInstance(Class<?> clazz, Throwable cause) {
		return new SwitchpipeConfigurationException(String.format("Cannot create class instance: %s", clazz), cause);
	}
	
}
