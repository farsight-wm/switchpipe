package farsight.switchpipe.activation;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.PackageStore;
import com.wm.lang.ns.NSNode;
import com.wm.util.Values;

import farsight.switchpipe.activation.ServiceState.ServiceStateEntry;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;
import farsight.utils.NamespaceUtils;

/**
 * 
 */
public class NsHintPolicy implements PersistablePolicy {
	
	private static final String KEY_HINT_CONFIGURATION = "$_SWITCHPIPE3";
	private static final String KEY_MODE = "mode";
	private static final String KEY_VALUE = "value";
	
	private static final int MODE_UNCONFIGURED = -1;
	private static final int MODE_OFF = 0;
	private static final int MODE_ENABLED = 1;
	private static final int MODE_TEMPORARY_COUNT = 2;
	private static final int MODE_TEMPORARY_DATE = 3;
	
	private static void setMode(NSNode node, int mode, long value, boolean persistent) {
		if(node == null)
			return;
		
		Values hint;
		if(mode < 0) {
			//unconfigure
			hint = null;
		} else {
			hint = new Values();
			hint.put(KEY_MODE, mode);
			if(mode > 1)
				hint.put(KEY_VALUE, value);
		}
		
		setConfiguration(node, hint);
		if(persistent)
			persistConfiguration(node);
	}
	
	private static Values getConfiguration(NSNode node) {
		Values hints = node.getHints();
		if(hints == null) {
			return null;
		}
		Object o = hints.get(KEY_HINT_CONFIGURATION);
		if(o == null || !(o instanceof Values)) {
			return null;
		}
		return (Values)o;
	}
	
	private static void setConfiguration(NSNode node, Values hint) {
		Values nodeHints = node.getHints();
		if(hint == null) {
			//remove
			if(nodeHints == null)
				return;
			nodeHints.remove(KEY_HINT_CONFIGURATION);
			if(nodeHints.size() == 0)
				node.setHints(null);
		} else {
			//set/update
			if(nodeHints == null) 
				node.setHints(nodeHints = new Values());
			nodeHints.setValue(KEY_HINT_CONFIGURATION, hint);	
		}
	}
	
	private static void persistConfiguration(NSNode service) {
		//get PackageStore
		PackageStore store = ((com.wm.app.b2b.server.Package) service.getPackage()).getStore();
		
		//read current persisted description
		IDataMap desc = new IDataMap(store.getDescription(service.getNSName()));
		
		//update with current hints
		Values hints = service.getHints();
		if(hints == null) {
			desc.remove("node_hints");
		} else {
			desc.put("node_hints", hints);
		}
		
		//write description
		try {
			store.updateDescription(service.getNSName(), desc.getIData());
		} catch (IOException e) {
			//what to do?
		}
	}
	
	private static ActivationState isSaveEnabled(NSNode node, boolean isExecute) {
		Values spHints = getConfiguration(node);
		if(spHints == null)
			return ActivationState.UNDEFINED;

		//get operating mode
		int mode = spHints.getInt(KEY_MODE, MODE_UNCONFIGURED);
		long value = spHints.getLong(KEY_VALUE, 0);
		
		switch(mode) {
		case MODE_OFF:
			return ActivationState.INACTIVE;
		case MODE_ENABLED:
			return ActivationState.ACTIVE;
		case MODE_TEMPORARY_DATE:
			if (value > 0 && value > new Date().getTime()) {
				//valid
				return ActivationState.ACTIVE;
			} else {
				setMode(node, MODE_OFF, 0, false);
				return ActivationState.INACTIVE;
			}
		case MODE_TEMPORARY_COUNT:
			if(isExecute) {
				if(value > 1) {
					setMode(node, MODE_TEMPORARY_COUNT, value - 1, false);
				} else {
					setMode(node, MODE_OFF, 0, false);
				}
			} 
			return ActivationState.get(value > 0);
		default:
			setMode(node, MODE_UNCONFIGURED, 0, false);
			return ActivationState.UNDEFINED;
		}
	}
	
	// --- Implementation of interfaces ---
	
	// ActivationPolicy.class

	@Override
	public ActivationState trigger(String serviceID) {
		NSNode node = getNode(serviceID);
		if(node == null)
			return ActivationState.UNDEFINED;

		return isSaveEnabled(node, true);
	}

	@Override
	public ActivationState check(String serviceID) {
		NSNode node = getNode(serviceID);
		if(node == null)
			return ActivationState.UNDEFINED;
		
		return isSaveEnabled(node, false);
	}

	// PersistablePolicy.class

	public boolean configure(String serviceID, ServiceState state) throws SwitchpipeConfigurationException {
		return configure(serviceID, state, false);
	}
	
	@Override
	public List<ServiceStateEntry> getConfiguredServices(String pkgName) {
		LinkedList<ServiceStateEntry> configuredSerices = new LinkedList<>();
		Values config;
		for(NSNode service: NamespaceUtils.getNodes(NSNode.class, pkgName))
			if((config = getConfiguration(service)) != null)
				configuredSerices.add(createEntry(service, config));
		return configuredSerices;	
	}
	
	@Override
	public boolean configure(String serviceID, ServiceState state, boolean persist)
			throws SwitchpipeConfigurationException {
		NSNode node = getNode(serviceID);
		if(node == null)
			//throw new SwitchpipeConfigurationException("ServiceID is not a valid NSName");
			return false;
		
		switch(state.type) {
		case UNCONFIGURED:
			setMode(node, MODE_UNCONFIGURED, 0, persist);
			return true;
		case ACTIVE:
			setMode(node, MODE_ENABLED, 0, persist);
			return true;
		case OFF:
			setMode(node, MODE_OFF, 0, persist);
			return true;
		case TEMPORARY_COUNT:
			setMode(node, MODE_TEMPORARY_COUNT, state.getCounter(), persist);
			return true;
		case TEMPORARY_TIME:
			setMode(node, MODE_TEMPORARY_DATE, java.sql.Timestamp.valueOf(state.getDate()).getTime(), persist);
			return true;
		}
		return false;
	}

	// -- helper --
	
	private NSNode getNode(String serviceID) {
		return NamespaceUtils.getNode(serviceID);
	}
	
	private ServiceStateEntry createEntry(NSNode node, Values spHints) {
		final String serviceID = node.getNSName().getFullName();
		if(spHints == null) {
			return new ServiceStateEntry(serviceID, ServiceState.UNCONFIGURED);
		} else {
			//get operating mode
			int mode = spHints.getInt(KEY_MODE, MODE_UNCONFIGURED);
			long value = spHints.getLong(KEY_VALUE, 0);
			
			switch(mode) {
			case MODE_OFF:
				return new ServiceStateEntry(serviceID, ServiceState.OFF);
			case MODE_ENABLED:
				return new ServiceStateEntry(serviceID, ServiceState.ACTIVE);
			case MODE_TEMPORARY_DATE:
				return new ServiceStateEntry(serviceID, ServiceState.createTemporaryDate(value));
			case MODE_TEMPORARY_COUNT:
				return new ServiceStateEntry(serviceID, ServiceState.createTemporaryCounter((int)value));
			default:
				return new ServiceStateEntry(serviceID, ServiceState.UNCONFIGURED);
			}
		}
	}

}
