package farsight.switchpipe.activation;

public interface ActivationPolicy {

	ActivationState check(String serviceID);
	ActivationState trigger(String serviceID);

}