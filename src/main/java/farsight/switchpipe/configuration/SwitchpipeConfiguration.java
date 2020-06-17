package farsight.switchpipe.configuration;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import farsight.switchpipe.Switchpipe.StoreHolder;
import farsight.switchpipe.activation.ActivationPolicy;
import farsight.switchpipe.activation.ActivationStrategy;
import farsight.switchpipe.activation.NsHintPolicy;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;
import farsight.switchpipe.pipelinecodec.DefaultPipelineCodec;
import farsight.switchpipe.pipelinecodec.PipelineCodec;
import farsight.utils.ReflectionUtils;

public class SwitchpipeConfiguration {

	@SuppressWarnings("unchecked")
	protected static <T> Class<? extends T> findClass(String className, Class<T> baseClass, Class<? extends T> defaultClass) throws SwitchpipeConfigurationException {
		if (className == null)
			return defaultClass;

		if (!className.contains(".")) {
			// relative
			className = baseClass.getPackage().getName() + "." + className;
		}

		try {
			Class<?> definedClass = Class.forName(className);
			if (baseClass.isAssignableFrom(definedClass)) {
				return (Class<? extends T>) definedClass;
			} else {
				throw SwitchpipeConfigurationException.classNotValid(definedClass, baseClass);
			}
		} catch (Exception e) {
			throw SwitchpipeConfigurationException.classNotFound(className, e);
		}
	}
	
	protected static String className(Class<?> actualClass, Class<?> baseClass) {
		if(actualClass == null)
			return null;
		String base = baseClass.getPackage().getName();
		String actual = actualClass.getCanonicalName();
		
		if(actual.startsWith(base) && actual.lastIndexOf('.') == base.length())
			return actual.substring(base.length() + 1);
		return actual;
	}

	public static class Builder {

		// defaults
		private boolean switchEnabled = true;
		private ActivationStrategy activationStrategy = ActivationStrategy.DEFAULT_ENABLED;
		private Map<String, String> activationPolicyParams = null;
		private String activationPolicyClass = null;
		private String pipelineCodecClass = null;
		private Map<String, String> pipelineCodecParams = null;
		private SourceConfiguration defaults = null;
		private LinkedList<SourceConfiguration> sources = new LinkedList<>();
		private String defaultStoreID = null;
		private Object configurationSource = null;

		public Builder setSwitchEnabled(boolean enbabled) {
			this.switchEnabled = enbabled;
			return this;
		}

		public Builder setActicationStrategy(ActivationStrategy activationStrategy) {
			this.activationStrategy = activationStrategy;
			return this;
		}

		public Builder setActivationPolicy(String activationPolicyClass) {
			this.activationPolicyClass = activationPolicyClass;
			return this;
		}

		public Builder setPipelineCodec(String pipelineCodecClass) {
			this.pipelineCodecClass = pipelineCodecClass;
			return this;
		}

		public Builder setActivationStrategy(ActivationStrategy activationStrategy) {
			this.activationStrategy = activationStrategy;
			return this;
		}

		public Builder setActivationPolicyParams(Map<String, String> activationPolicyParams) {
			this.activationPolicyParams = activationPolicyParams;
			return this;
		}

		public Builder setPipelineCodecParams(Map<String, String> pipelineCodecParams) {
			this.pipelineCodecParams = pipelineCodecParams;
			return this;
		}

		public Builder setDefaults(SourceConfiguration defaults) {
			this.defaults = defaults;
			return this;
		}

		public Builder addSource(SourceConfiguration source) {
			this.sources.add(source);
			return this;
		}
		
		public Builder addSource(SourceConfiguration.Builder builder) throws SwitchpipeConfigurationException {
			return addSource(builder.build());
		}
		
		public Builder setDefaultStoreID(String defaultStoreID) {
			this.defaultStoreID = defaultStoreID;
			return this;
		}
		
		private SourceConfiguration[] createSources() throws SwitchpipeConfigurationException {
			SourceConfiguration[] result = sources.toArray(new SourceConfiguration[sources.size()]);
			for(int i = 0; i < result.length; i++) {
				//merge
				result[i] = result[i].mergeDefault(defaults);
			}
			return result;
		}

		public SwitchpipeConfiguration build() throws SwitchpipeConfigurationException {
			return new SwitchpipeConfiguration(switchEnabled,
					findClass(activationPolicyClass, ActivationPolicy.class, NsHintPolicy.class),
					activationPolicyParams, activationStrategy,
					findClass(pipelineCodecClass, PipelineCodec.class, DefaultPipelineCodec.class),
					pipelineCodecParams, defaults, 
					createSources(), defaultStoreID, configurationSource);
		}

		public void setConfigurationSource(Object configurationSource) {
			this.configurationSource = configurationSource;
			
		}
	}

	public final boolean switchEnabled;
	public final Class<? extends ActivationPolicy> activationPolicyClass;
	public final Map<String, String> activationPolicyParams;
	public final ActivationStrategy activationStrategy;
	public final Class<? extends PipelineCodec> pipelineCodecClass;
	public final Map<String, String> pipelineCodecParams;
	public final SourceConfiguration defaults;
	public final SourceConfiguration[] sources;
	public final String defaultStoreID;
	public final Object configurationSource;

	public SwitchpipeConfiguration(boolean switchEnabled, Class<? extends ActivationPolicy> activationPolicyClass,
			Map<String, String> activationPolicyParams, ActivationStrategy activationStrategy,
			Class<? extends PipelineCodec> pipelineCodecClass, Map<String, String> pipelineCodecParams,
			SourceConfiguration defaults, SourceConfiguration[] sources, String defaultStoreID, Object configurationSource) {
		super();
		this.switchEnabled = switchEnabled;
		this.activationPolicyClass = activationPolicyClass;
		this.activationPolicyParams = activationPolicyParams;
		this.activationStrategy = activationStrategy;
		this.pipelineCodecClass = pipelineCodecClass;
		this.pipelineCodecParams = pipelineCodecParams;
		this.defaults = defaults;
		this.sources = sources;
		this.defaultStoreID = defaultStoreID;
		this.configurationSource = configurationSource;
	}

	private static <T> T createInstance(Class<T> clazz, Map<String, String> args)
			throws SwitchpipeConfigurationException {
		if (args == null) {
			try {
				return ReflectionUtils.createInstance(clazz);
			} catch (ReflectiveOperationException e) {
				// ignore
			}
		}
		try {
			Constructor<T> constructor = ReflectionUtils.getConstructorIfExistent(clazz, Map.class);
			if (constructor == null)
				throw SwitchpipeConfigurationException.cannotCreateInstance(clazz, null);
			return constructor.newInstance(args);
		} catch (ReflectiveOperationException e) {
			throw SwitchpipeConfigurationException.cannotCreateInstance(clazz, e);
		}
	}
	
	public HashMap<String, StoreHolder> createStoresMap() throws SwitchpipeConfigurationException {
		HashMap<String, StoreHolder> stores = new HashMap<>();
		for(SourceConfiguration source: sources) {
			//create Instance
			stores.put(source.id, new StoreHolder(source.id, source.fallbackId, source.createSource()));
		}

		//TODO check for circular dependencies?!
		stores.forEach((key, holder) -> {
			if(holder.fallbackId != null) {
				holder.setFallback(stores.get(holder.fallbackId));
			}
		});
		
		return stores;
	}

	public PipelineCodec createPipelineCodecInstance() throws SwitchpipeConfigurationException {
		return createInstance(pipelineCodecClass, pipelineCodecParams);
	}

	public ActivationPolicy createActivationPolicyInstance() throws SwitchpipeConfigurationException {
		return createInstance(activationPolicyClass, activationPolicyParams);
	}

	public String getDefaultStoreID() {
		return this.defaultStoreID;
	}

	public static Builder builder() {
		return new Builder();
	}

	public Object getSource() {
		return configurationSource;
	}
	
	public String getActivationPolicy() {
		if(activationPolicyClass == null)
			return null;
		return className(activationPolicyClass, ActivationPolicy.class);
	}
	
	public String getPipelineCodec() {
		if(pipelineCodecClass == null)
			return null;
		return className(pipelineCodecClass, PipelineCodec.class);
	}
}
