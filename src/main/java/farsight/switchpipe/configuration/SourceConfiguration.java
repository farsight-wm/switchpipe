package farsight.switchpipe.configuration;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;

import farsight.switchpipe.datastore.DataSource;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;
import farsight.utils.ReflectionUtils;

public class SourceConfiguration {
	
	public static final String DEFAULT_BASE_PATH = "pipeline";
	public static final String DEFAULT_TIMESTAMP_PATTERN = "yyMMdd-HHmmssSSS";
	public static final String DEFAULT_FILE_PATTERN = "${serviceID}/${invokeID}.xml";
	public static final String DEFAULT_SOURCE_CLASS = "FileDataSource";
	
	public static class Builder {
		public String id = null;
		public String fallbackId = null;
		public String basePath = null;
		public String sourceClass = null;
		public String timestampPattern = null;
		public String filePattern = null;
		public Map<String, String> sourceParams = null;
		
		public Builder setId(String id) {
			this.id = id;
			return this;
		}
		
		public Builder setFallbackId(String fallbackId) {
			this.fallbackId = fallbackId;
			return this;
		}
		
		public Builder setBasePath(String basePath) {
			this.basePath = basePath;
			return this;
		}
		
		public Builder setTimestampPattern(String timestampPattern) {
			this.timestampPattern = timestampPattern;
			return this;
		}
		
		public Builder setFilePattern(String filePattern) {
			this.filePattern = filePattern;
			return this;
		}
		
		public Builder setSourceClass(String className) {
			this.sourceClass = className;
			return this;
		}
		
		public Builder setSourceParams(Map<String, String> sourceParams) {
			this.sourceParams = sourceParams;
			return this;
		}

		public SourceConfiguration build() throws SwitchpipeConfigurationException {
			return new SourceConfiguration(id, fallbackId, basePath, timestampPattern, filePattern, SwitchpipeConfiguration.findClass(sourceClass, DataSource.class, null), sourceParams);
		}

		public Builder setDefaults(boolean overwrite) {
			if(overwrite || basePath == null) setBasePath(DEFAULT_BASE_PATH);
			if(overwrite || timestampPattern == null) setTimestampPattern(DEFAULT_TIMESTAMP_PATTERN);
			if(overwrite || filePattern == null) setFilePattern(DEFAULT_FILE_PATTERN);
			if(overwrite || sourceClass == null) setSourceClass(DEFAULT_SOURCE_CLASS);
			return this;
		}
		
		private String overwrite(String source, String value, boolean overwrite) {
			return overwrite ? (value == null ? source : value) : (source == null ? value : source); 
		}

		public Builder mergeConfiguration(SourceConfiguration conf, boolean overwrite) {
			setId(overwrite(id, conf.id, overwrite));
			setFallbackId(overwrite(fallbackId, conf.fallbackId, overwrite));
			setBasePath(overwrite(basePath, conf.basePath, overwrite));
			setSourceClass(overwrite(sourceClass, conf.sourceClass == null ? null : conf.sourceClass.getCanonicalName(), overwrite));
			setTimestampPattern(overwrite(timestampPattern, conf.timestampPattern, overwrite));
			setFilePattern(overwrite(filePattern, conf.filePattern, overwrite));
			if(overwrite && conf.sourceParams != null || sourceParams == null)
				setSourceParams(conf.sourceParams);
			return this;
		}
	}
	
	public final String id;
	public final String fallbackId;
	public final String basePath;
	public final String timestampPattern;
	public final String filePattern;
	public final Class<? extends DataSource> sourceClass;
	public final Map<String, String> sourceParams;
	
	public SourceConfiguration(String id, String fallbackId, String basePath, String timestampPattern,
			String filePattern, Class<? extends DataSource> sourceClass, Map<String, String> sourceParams) {
		this.id = id;
		this.fallbackId = fallbackId;
		this.basePath = basePath;
		this.timestampPattern = timestampPattern;
		this.filePattern = filePattern;
		this.sourceClass = sourceClass;
		this.sourceParams = sourceParams;
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public SourceConfiguration mergeDefault(SourceConfiguration defaults) throws SwitchpipeConfigurationException {
		Builder builder = builder().mergeConfiguration(this, true);
		if(defaults != null)
			builder.mergeConfiguration(defaults, false);
		return builder.setDefaults(false).build();
	}
	
	public DataSource createSource() throws SwitchpipeConfigurationException {
		Constructor<? extends DataSource> constructor = ReflectionUtils.getConstructorIfExistent(sourceClass, SourceConfiguration.class);
		try {
			if(constructor != null) {
				return constructor.newInstance(this); 
			} else {
				DataSource instance = ReflectionUtils.createInstance(sourceClass);
				instance.configure(this);
				return instance;
			}
		} catch(ReflectiveOperationException e) {
			throw SwitchpipeConfigurationException.cannotCreateInstance(sourceClass, e);
		}
	}
	
	public String getParam(String key) {
		return sourceParams != null ? sourceParams.get(key) : null;
	}
	
	public String getParam(String key, String defaultValue) {
		String value = getParam(key);
		return value == null ? defaultValue : value;
	}
	
	public String getSource() {
		return SwitchpipeConfiguration.className(sourceClass, DataSource.class);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[SourceConfiguration\n");
		buf.append("	id=" + id + "\n");
		buf.append("	fallbackId=" + fallbackId + "\n");
		buf.append("	basePath=" + basePath + "\n");
		buf.append("	timestampPattern=" + timestampPattern + "\n");
		buf.append("	filePattern=" + filePattern + "\n");
		buf.append("	sourceClass=" + sourceClass.getCanonicalName() + "\n");
		if(sourceParams != null) {
			buf.append("	params=[\n");
			for(Entry<String, String> entry: sourceParams.entrySet())
				buf.append("\t\t" + entry.getKey() + "=" + entry.getValue() + "\n");
			buf.append("	]\n");
		}
		buf.append("]");
		return buf.toString();
	}
	

}
