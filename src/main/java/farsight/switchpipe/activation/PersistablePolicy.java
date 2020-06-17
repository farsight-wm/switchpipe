package farsight.switchpipe.activation;

import farsight.switchpipe.exception.SwitchpipeConfigurationException;

public interface PersistablePolicy extends ConfigurablePolicy {
	
	public boolean configure(String serviceID, ServiceState state, boolean persist) throws SwitchpipeConfigurationException;

}
