package farsight.switchpipe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
//import com.wm.lang.ns.NSNode;
//import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSService;

import farsight.switchpipe.activation.ConfigurablePolicy;
import farsight.switchpipe.activation.PersistablePolicy;
import farsight.switchpipe.activation.ServiceState;
import farsight.switchpipe.activation.ServiceState.ServiceStateEntry;
import farsight.switchpipe.configuration.ConfigurationPropertiesCodec;
import farsight.switchpipe.configuration.SwitchpipeConfiguration;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;
import farsight.switchpipe.exception.SwitchpipeException;
import farsight.switchpipe.utils.log.LogFacade;
import farsight.switchpipe.utils.log.ServerLogBackend;
import farsight.switchpipe.utils.log.Severity;
import farsight.utils.InvokeUtils;
import farsight.utils.idata.DataBuilder;
import farsight.utils.idata.ListBuilder;
import farsight.utils.properties.AdvancedProperties;

public class SwitchpipeAPI {
	
	public static String[] CONFIG_LOCATIONS = new String[] {
			"config/switchpipe.conf"
	};
	
	
	// singleton
	
	private static class Instance_Holder {
		private static final SwitchpipeAPI INSTANCE;
		
		static {
			INSTANCE = new SwitchpipeAPI();
			INSTANCE.initialize();
		}
	}
	
	public static SwitchpipeAPI defaultInstance() {
		return Instance_Holder.INSTANCE;
	}

	private String[] configLocations = null;
	private Switchpipe switchpipe = null;
	
	private final LogFacade LOGGER = ServerLogBackend.createFor("switchpipe");
	
	public SwitchpipeAPI() {
		this(CONFIG_LOCATIONS);
	}
	
	public SwitchpipeAPI(String configLocation) {
		this(new String[] {configLocation});
	}
	
	public SwitchpipeAPI(String[] configLocations) {
		this.configLocations = configLocations;
	}
	
	// (re)initialize
	public void initialize() {
		if(switchpipe != null || configLocations == null)
			return;
		
		String[] configLocations = this.configLocations;
		this.configLocations = null;
		
		for(String configLocation: configLocations) {
			Path path = null;
			path = Paths.get(configLocation);
			if(Files.exists(path) && Files.isReadable(path)) {
				try {
					reconfigure(path);
					return;
				} catch(SwitchpipeConfigurationException e) {
					//ignore and try next
				}
			}
		}
		
		try {
			LOGGER.logInfo("No valid config file found - using default configuration");
			reconfigure(SwitchpipeConfiguration.defaultConfiguration());
		} catch(SwitchpipeConfigurationException e) {
			LOGGER.logError("Cannot use default configuration: " + e.getMessage());
		}
	}
	
	public void reconfigure(SwitchpipeConfiguration config) throws SwitchpipeConfigurationException {
		configLocations = null; //just in case this is the "first" initialization
		switchpipe = new Switchpipe(config);
	}
	
	public void reconfigure(Path path) throws SwitchpipeConfigurationException {
		reconfigure(ConfigurationPropertiesCodec.read(path));
		LOGGER.logFormated("Loaded configuration: %s", Severity.INFO, path);
	}
	
	public void reloadConfiguration() throws SwitchpipeException {
		Object source = getSwitchpipe().getConfiguration().getSource();
		if(source == null)
			throw new SwitchpipeConfigurationException("Configuration has no source set. Cannot reload config without new source.");
		if(source instanceof Path) {
			reconfigure((Path) source);
		} else {
			throw new SwitchpipeConfigurationException("Configuration source type unknown");
		}
	}
	
	public String getConfigurationProperties() throws SwitchpipeException {
		AdvancedProperties props = ConfigurationPropertiesCodec.toProperties(getSwitchpipe().getConfiguration());
		return props.toString();
	}
	
	public IData getConfigurationIData() throws SwitchpipeException {
		AdvancedProperties props = ConfigurationPropertiesCodec.toProperties(getSwitchpipe().getConfiguration());
		final DataBuilder builder = DataBuilder.create();
		props.forEach((key, path, value) -> {
			if(key.equals("class")) {
				builder.insert(path + ".class", value, '.');
			} else {
				builder.insert(path, value, '.');
			}
		});
		return builder.build();
	}
	
	public boolean isInitialized() {
		return switchpipe != null;
	}

	
	public Switchpipe getSwitchpipe() throws SwitchpipeException {
		if(switchpipe != null)
			return switchpipe;
		initialize();
		if(switchpipe == null)
			throw SwitchpipeConfigurationException.noConfigurationFound();
		return switchpipe;
	}
	
	private Switchpipe getInitializedSwitchpipe() {
		return isInitialized() ? switchpipe : null;
	}
		
	private String autoGetServiceID(String serviceID) {
		if(serviceID == null) {
			NSService caller = getCaller();
			if(caller != null)
				serviceID = caller.getNSName().getFullName();
		}
		return serviceID;
	}

	public String switchPipeline(IData pipeline, String invokeID, String serviceID, String storeID) {
		try {
			serviceID = autoGetServiceID(serviceID);
			
			//if no service id can be determined -> do nothing
			if(serviceID == null)
				return null;
			
			return getSwitchpipe().switchPipeline(pipeline, invokeID, serviceID, storeID);
		} catch(Throwable e) {
			LOGGER.logError("Error in switchPipeline");
			LOGGER.log(e);
			IDataMap p = new IDataMap(pipeline);
			p.put("_switchpipeError", e.getMessage());
			return "";
		}
	}
	
	public String storePipeline(IData pipeline, String invokeID, String serviceID, String storeID) throws ServiceException {
		try {
			serviceID = autoGetServiceID(serviceID);
			return getSwitchpipe().storePipeline(pipeline, invokeID, serviceID, storeID);
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public IData loadPipeline(String invokeID, String serviceID, String storeID) throws ServiceException {
		try {
			serviceID = autoGetServiceID(serviceID);
			return getSwitchpipe().loadPipeline(invokeID, serviceID, storeID);
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public void restorePipeline(IData pipeline, String invokeID, String serviceID, String storeID) throws ServiceException {
		try {
			serviceID = autoGetServiceID(serviceID);
			getSwitchpipe().restorePipeline(pipeline, invokeID, serviceID, storeID);
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public SwitchID findPipeline(String invokeID, String serviceID, String storeID) throws ServiceException {
		try {
			serviceID = autoGetServiceID(serviceID);
			return getSwitchpipe().findPipeline(invokeID, serviceID, storeID);
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public boolean isSwitchpipeEnabled(String serviceID) throws ServiceException {
		try {
			serviceID = autoGetServiceID(serviceID);
			Switchpipe switchpipe = getSwitchpipe();
			return switchpipe.switchEnabled && switchpipe.checkActivation(serviceID);
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public static void replacePipeline(IData pipeline, IData replacement) {
		//Clear ...
		IDataCursor c = pipeline.getCursor();
		c.first();
		while(c.delete());
		
		//... and replace
		IDataUtil.merge(replacement, pipeline);
	}

	public static NSService getCaller(int level) {
		try {
			return InvokeUtils.getCallingService(level);
		} catch(Throwable e) {
			return null;
		}
	}
	
	public static NSService getCaller() {
		return getCaller(1);
	}

	public SwitchpipeConfiguration getConfiguration() {
		return switchpipe == null ? null : switchpipe.getConfiguration();
	}
	
	
	// -- Activation Configuration API ----------------------------------------
	
	public boolean isConfigurablePolicy() {
		Switchpipe switchpipe = getInitializedSwitchpipe();
		return switchpipe != null && switchpipe.activationPolicy instanceof ConfigurablePolicy;
	}
	
	public boolean isPersistablePolicy() {
		Switchpipe switchpipe = getInitializedSwitchpipe();
		return switchpipe != null && switchpipe.activationPolicy instanceof PersistablePolicy;
	}
	
	public ConfigurablePolicy getConfigurablePolicy() throws ServiceException {
		try {
			Switchpipe switchpipe = getSwitchpipe();
			if(switchpipe.activationPolicy instanceof ConfigurablePolicy)
				return (ConfigurablePolicy) switchpipe.activationPolicy;
			throw new SwitchpipeConfigurationException("ActivationPolicy is not configurable");
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public PersistablePolicy getPersistablePolicy() throws ServiceException {
		try {
			Switchpipe switchpipe = getSwitchpipe();
			if(switchpipe.activationPolicy instanceof PersistablePolicy)
				return (PersistablePolicy) switchpipe.activationPolicy;
			throw new ServiceException("ActivationPolicy is not persistable");
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public IData[] getConfiguredServicesAsIDataList(String pkgName) throws ServiceException {
		try {
			ConfigurablePolicy policy = getConfigurablePolicy();
			if(policy == null) return null;
			
			ListBuilder list = ListBuilder.create();
			for(ServiceStateEntry entry: policy.getConfiguredServices(pkgName)) {
				list.add(entry.getIData());
			}
			return list.build();
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
	}
	
	public boolean configureService(String serviceID, ServiceState state, boolean persistent) throws ServiceException {
		try {
			if(persistent) {
				return getPersistablePolicy().configure(serviceID, state, true);
			} else {
				return getConfigurablePolicy().configure(serviceID, state);
			}
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}	
	}

}
