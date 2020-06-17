package farsight.switchpipe.activation;

public class AlwaysEnabledPolicy implements ActivationPolicy {

	@Override
	public ActivationState check(String serviceID) {
		return ActivationState.ACTIVE;
	}

	@Override
	public ActivationState trigger(String serviceID) {
		return ActivationState.ACTIVE;
	}

}
