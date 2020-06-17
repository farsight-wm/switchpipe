package farsight.switchpipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

import farsight.switchpipe.activation.ActivationPolicy;
import farsight.switchpipe.activation.ActivationState;
import farsight.switchpipe.activation.ActivationStrategy;
import farsight.switchpipe.activation.ConfigurablePolicy;
import farsight.switchpipe.activation.ServiceState;
import farsight.switchpipe.configuration.SwitchpipeConfiguration;
import farsight.switchpipe.datastore.DataSource;
import farsight.switchpipe.datastore.DataStore;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;
import farsight.switchpipe.exception.SwitchpipeDataException;
import farsight.switchpipe.exception.SwitchpipeException;
import farsight.switchpipe.pipelinecodec.PipelineCodec;

/*
 * 
 * 
 */
public class Switchpipe {
	
	public static final int MAX_FALLBACK_TRIES = 5;
	
	public static class StoreHolder {
		public final DataSource source;
		public final DataStore store;
		public final boolean isStore;
		public final String id;
		public final String fallbackId;
		private boolean hasFallback = false;
		private StoreHolder fallback = null;

		public String store(SwitchID switchID, byte[] data) throws SwitchpipeDataException {
			if(!isStore)
				throw new SwitchpipeDataException("Source " + id + " is not write enabled!");
			return store.store(switchID, data);
		}

		public StoreHolder(String id, String fallbackId, DataSource source) {
			this.id = id;
			this.fallbackId = fallbackId;
			this.source = source;
			this.store = source instanceof DataStore ? (DataStore) source : null;
			this.isStore = this.store != null;
		}

		public void setFallback(StoreHolder fallback) {
			this.fallback = fallback;
			this.hasFallback = fallback != null;
		}
		
		public SwitchID find(SwitchID switchID) throws SwitchpipeDataException {
			return find(switchID, MAX_FALLBACK_TRIES);
		}
		
		public SwitchID find(SwitchID switchID, int triesLeft) throws SwitchpipeDataException {
			if(triesLeft <= 0)
				throw new SwitchpipeDataException("Max fallback tries hit, while loading pipeline data");
			SwitchID result = source.findPipeline(switchID);
			if(result == null && hasFallback) {
				return fallback.find(switchID, triesLeft - 1);
			}
			return result;
		}

		public InputStream load(SwitchID switchID) throws SwitchpipeDataException {
			return load(switchID, MAX_FALLBACK_TRIES);
		}
		
		public InputStream load(SwitchID switchID, int triesLeft) throws SwitchpipeDataException {
			if(triesLeft <= 0)
				throw new SwitchpipeDataException("Max fallback tries hit, while loading pipeline data");
			InputStream result = source.loadIfExists(switchID);
			if(result == null && hasFallback) {
				return fallback.load(switchID, triesLeft - 1);
			}
			return result;
		}
	}

	public final boolean switchEnabled;
	public final ActivationPolicy activationPolicy;
	private final ConfigurablePolicy configurablePolicy;
	public final ActivationStrategy activationStrategy;
	public final PipelineCodec pipelineCodec;
	
	private final HashMap<String, StoreHolder> stores;
	private final StoreHolder defaultStore;
	
	public final SwitchpipeConfiguration conf;
	
	public Switchpipe(SwitchpipeConfiguration conf) throws SwitchpipeConfigurationException {
		this.conf = conf;
		switchEnabled = conf.switchEnabled;
		activationPolicy = conf.createActivationPolicyInstance();
		configurablePolicy = (activationPolicy instanceof ConfigurablePolicy) ? (ConfigurablePolicy)activationPolicy : null;
		activationStrategy = conf.activationStrategy;
		pipelineCodec = conf.createPipelineCodecInstance();
		stores = conf.createStoresMap();
		String defaultStoreID = conf.getDefaultStoreID();
		if(!stores.containsKey(defaultStoreID)) {
			defaultStoreID = conf.sources.length > 0 ? conf.sources[0].id : null;
		}
		if(defaultStoreID == null)
			throw new SwitchpipeConfigurationException("No valid default Store defined!");
		defaultStore = stores.get(defaultStoreID);
		if(!defaultStore.isStore)
			throw new SwitchpipeConfigurationException("Default store is not writeable!");
	}

	private StoreHolder getSource(String storeID) throws SwitchpipeDataException {
		StoreHolder storeHolder = storeID == null ? defaultStore : stores.get(storeID);
		if (storeHolder == null) {
			throw SwitchpipeDataException.invalidStoreID(storeID);
		}
		return storeHolder;
	}

	private StoreHolder getStore(String storeID) throws SwitchpipeDataException {
		StoreHolder storeHolder = getSource(storeID);
		if (!storeHolder.isStore)
			throw SwitchpipeDataException.invalidStoreID(storeID);
		return storeHolder;
	}
	
	
	public boolean isEnabled() {
		return switchEnabled;
	}
	
	private SwitchID find(SwitchID switchID) throws SwitchpipeDataException {
		return getSource(switchID.storeID).find(switchID);
	}

	private IData load(SwitchID switchID) throws SwitchpipeDataException {
		//make sure input streams are closed properly
		try (InputStream in = getSource(switchID.storeID).load(switchID)) {
			if(in != null)
				return pipelineCodec.decodePipeline(in);
		} catch(IOException e) {
			//ignore
		}
		return null;
	}
	
	private String store(SwitchID switchID, IData pipeline) throws SwitchpipeDataException {
		if(pipeline == null || switchID.serviceID == null)
			return null;
		return getStore(switchID.storeID).store(switchID, pipelineCodec.encodePipeline(pipeline));
	}
	

	/**
	 * This service performs the switch in auto mode. It is guaranteed not to
	 * throw any kind of exception.
	 * 
	 * Attention: The speed of this service depends on the i/o speed of the used
	 * store.
	 * 
	 * 
	 * @param pipeline
	 *            The services execution pipeline. Must not be null!
	 * @param invokeID
	 *            The ID that defines weather to store or to restore a pipeline
	 * @param serviceID
	 *            The ID of the service that is derived by the calling service
	 *            or explicitly provided by the calling service itself. Must not
	 *            be null!
	 * @param storeID
	 *            The storeID to be used to store/restore data from. If null,
	 *            the default store is used.
	 * @return Returns the generated invokeID that may be used to restore the
	 *         pipeline when store action is triggered. Otherwise it returns
	 *         null.
	 * @throws SwitchpipeException 
	 */
	public String switchPipeline(IData pipeline, String invokeID, String serviceID, String storeID) throws SwitchpipeException {
		//check invokeID
		if(invokeID == null || invokeID.isEmpty()) {
			//store mode
			if(switchEnabled && triggerActivation(serviceID)) {
				return store(SwitchID.createAutogenerateID(serviceID, storeID), pipeline);
			} else {
				return null;
			}
		} else {
			//parse invokeID
			SwitchID parsed = SwitchID.parse(invokeID, serviceID, storeID);
			
			if(parsed.isWrite()) {
				//"forced" write mode 
				return storePipeline(pipeline, parsed.invokeID, parsed.serviceID, parsed.storeID);
			} else {
				SwitchID switchID = parsed.isRelative() ? find(parsed) : parsed;
				
				if(switchID == null)
					throw SwitchpipeDataException.pipelineNotFound(parsed);
				
				IData restore = load(switchID);
				if(restore == null) {
					throw SwitchpipeDataException.pipelineNotFound(parsed);
				}
				replacePipeline(pipeline, restore);
				return parsed.invokeID;
			}
		}
	}
	
	private void replacePipeline(IData pipeline, IData replacement) {
		// Clear ...
		IDataCursor c = pipeline.getCursor();
		c.first();
		while (c.delete());

		// ... and replace
		IDataUtil.merge(replacement, pipeline);
	}


	public String storePipeline(IData pipeline, String invokeID, String serviceID, String storeID) throws SwitchpipeDataException {
		SwitchID parsed = SwitchID.parse(invokeID, serviceID, storeID);
		return store(parsed, pipeline);
	}
	
	public IData loadPipeline(String invokeID, String serviceID, String storeID) throws SwitchpipeDataException {
		SwitchID parsed = SwitchID.parse(invokeID, serviceID, storeID);
		return load(parsed);
	}
	
	public void restorePipeline(IData pipeline, String invokeID, String serviceID, String storeID) throws SwitchpipeDataException {
		SwitchID parsed = SwitchID.parse(invokeID, serviceID, storeID);
		IData replace = load(parsed);
		if(replace == null)
			throw SwitchpipeDataException.pipelineNotFound(parsed);
		replacePipeline(pipeline, replace);
	}
	
	public SwitchID findPipeline(String invokeID, String serviceID, String storeID) throws SwitchpipeDataException {
		SwitchID parsed = SwitchID.parse(invokeID, serviceID, storeID);
		if(parsed.isWrite())
			return null;
		return find(parsed);
	}
	
	public boolean checkActivation(String serviceID) {
		ActivationState result = activationPolicy.check(serviceID);
		return result == ActivationState.UNDEFINED ? activationStrategy.undefinedState.booleanValue
				: result.booleanValue;
	}
	
	private boolean triggerActivation(String serviceID) {
		ActivationState result = activationPolicy.trigger(serviceID);
		
		if(result == ActivationState.UNDEFINED) {
			result = activationStrategy.undefinedState;
			
			if(activationStrategy.setUndefined && configurablePolicy != null) {
				try {
					configurablePolicy.configure(serviceID, ServiceState.get(result));
				} catch(SwitchpipeConfigurationException e) {
					//log and ignore?
				}
			}
		}
		return result.booleanValue;
	}

	public SwitchpipeConfiguration getConfiguration() {
		return conf;
	}
	
	
	
	
	

}
