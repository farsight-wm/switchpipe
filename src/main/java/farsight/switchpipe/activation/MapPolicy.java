package farsight.switchpipe.activation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import farsight.switchpipe.activation.ServiceState.ServiceStateEntry;
import farsight.switchpipe.activation.ServiceState.Type;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;

public class MapPolicy implements ActivationPolicy, ConfigurablePolicy {
	
	private HashMap<String, ServiceState> map = new HashMap<>();
	
	public MapPolicy() {
		//TODO add param to load file to map?
	}

	@Override
	public boolean configure(String serviceID, ServiceState state)
			throws SwitchpipeConfigurationException {
		
		if(state == null || state.type == Type.UNCONFIGURED)
			map.remove(serviceID);
		else
			map.put(serviceID, state);
		
		return true;
	}


	@Override
	public List<ServiceStateEntry> getConfiguredServices(String pkgName) throws SwitchpipeConfigurationException {
		ArrayList<ServiceStateEntry> result = new ArrayList<>(map.size());
		map.forEach((serviceID, state) -> result.add(new ServiceStateEntry(serviceID, state)));
		return result;
	}

	@Override
	public ActivationState check(String serviceID) {
		ServiceState entry = map.get(serviceID);
		if(entry == null)
			return ActivationState.UNDEFINED;
		return entry.check();
	}

	@Override
	public ActivationState trigger(String serviceID) {
		ServiceState entry = map.get(serviceID);
		if(entry == null)
			return ActivationState.UNDEFINED;
		return entry.trigger();
	}
}
