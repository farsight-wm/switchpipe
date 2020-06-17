package farsight.switchpipe.activation;

import static farsight.switchpipe.activation.ActivationState.*;

public enum ActivationStrategy {
	DEFAULT_ENABLED(ACTIVE, false),
	DEFAULT_DISABLED(INACTIVE, false),
	AUTO_ENABLE(ACTIVE, true),
	AUTO_DISABLE(INACTIVE, true);
	
	private static final ActivationStrategy DEFAULT_STRATEGY = ActivationStrategy.DEFAULT_ENABLED;
	
	public final ActivationState undefinedState;
	public final boolean setUndefined;
	
	private ActivationStrategy(ActivationState undefinedState, boolean setUndefined) {
		this.undefinedState = undefinedState;
		this.setUndefined = setUndefined;
	}
	
	public static ActivationStrategy parse(String str) {
		if(str == null)
			return DEFAULT_STRATEGY;
		try {
			return ActivationStrategy.valueOf(str);
		} catch(Exception e) {
			return DEFAULT_STRATEGY;
		}
	}
}