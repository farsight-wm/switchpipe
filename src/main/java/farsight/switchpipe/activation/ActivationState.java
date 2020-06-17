package farsight.switchpipe.activation;

public enum ActivationState {
	ACTIVE(true), INACTIVE(false), UNDEFINED(false);

	public final boolean booleanValue;

	private ActivationState(boolean booleanValue) {
		this.booleanValue = booleanValue;	
	}
	
	public static ActivationState get(boolean definedState) {
		return definedState ? ACTIVE : INACTIVE;
	}
}