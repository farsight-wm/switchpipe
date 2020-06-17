package farsight.switchpipe.activation;

import java.util.List;

import farsight.switchpipe.exception.SwitchpipeConfigurationException;

public interface ConfigurablePolicy extends ActivationPolicy {
	
	ActivationState trigger(String serviceID);
	public boolean configure(String serviceID, ServiceState state) throws SwitchpipeConfigurationException;
	public List<ServiceState.ServiceStateEntry> getConfiguredServices(String pkgName) throws SwitchpipeConfigurationException;

}
