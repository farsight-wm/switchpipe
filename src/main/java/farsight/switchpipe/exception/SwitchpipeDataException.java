package farsight.switchpipe.exception;

public class SwitchpipeDataException extends SwitchpipeException {

	private static final long serialVersionUID = 1L;

	public SwitchpipeDataException(String message) {
		super(message);
	}

	public SwitchpipeDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public static SwitchpipeDataException unableToReadRessource(Object resource, Throwable cause) {
		return new SwitchpipeDataException(String.format("Unable to read resource: %s", String.valueOf(resource)), cause);
	}

	public static SwitchpipeDataException notValidResource(Object resource) {
		return new SwitchpipeDataException(String.format("Target is not a valid resource: %s", String.valueOf(resource)));
	}

	public static SwitchpipeDataException unableToWriteRessource(Object resource, Throwable cause) {
		return new SwitchpipeDataException(String.format("Unable to write resource: %s", String.valueOf(resource)), cause);
	}

	public static SwitchpipeDataException invalidStoreID(String storeID) {
		return new SwitchpipeDataException("Invalid storeID: " + storeID);
	}

	public static SwitchpipeDataException pipelineNotFound(Object pipelineSource) {
		return new SwitchpipeDataException(String.format("Unable to load pipeline: %s", pipelineSource));
	}

	
}
