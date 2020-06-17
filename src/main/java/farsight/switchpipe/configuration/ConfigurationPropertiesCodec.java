package farsight.switchpipe.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import farsight.switchpipe.SwitchpipeAPI;
import farsight.switchpipe.activation.ActivationStrategy;
import farsight.switchpipe.exception.SwitchpipeConfigurationException;
import farsight.utils.properties.AdvancedProperties;

public class ConfigurationPropertiesCodec {
	
	public static SwitchpipeConfiguration read(Path path) throws SwitchpipeConfigurationException {
		try {
			return read(AdvancedProperties.create(Files.newBufferedReader(path)), path);
		} catch (IOException e) {
			throw SwitchpipeConfigurationException.cannotReadConfiguration(path, e);
		}
	}
	
	public static SwitchpipeConfiguration read(AdvancedProperties source, Path configurationSource) throws SwitchpipeConfigurationException {
		SwitchpipeConfiguration.Builder builder = SwitchpipeConfiguration.builder();
		builder.setSwitchEnabled(source.getBoolean("switchEnabled", true));
		builder.setActicationStrategy(ActivationStrategy.parse(source.get("activation.strategy")));
		builder.setActivationPolicy(source.get("activation.class"));
		builder.setActivationPolicyParams(source.getAsStringMap("activation.class"));
		builder.setDefaultStoreID(source.get("store.defaultStore"));
		builder.setPipelineCodec(source.get("pipelineCodec.class"));
		builder.setPipelineCodecParams(source.getAsStringMap("pipelineCodec.class"));
		if(source.containsGroup("store.defaults")) {
			builder.setDefaults(readSource(source, "store.defaults", null, true));
		} else {
			builder.setDefaults(SourceConfiguration.builder().setDefaults(true).build());
		}
		for(String key: source.getKeys("stores")) {
			builder.addSource(readSource(source, "stores." + key, key, false));
		}
		builder.setConfigurationSource(configurationSource);
		return builder.build();
	}
	
	public static SourceConfiguration readSource(AdvancedProperties source, String path, String id, boolean loadDefaults) throws SwitchpipeConfigurationException {
		SourceConfiguration.Builder builder = SourceConfiguration.builder();
		if(loadDefaults)
			builder.setDefaults(true);
		builder.setId(source.getDefault(AdvancedProperties.createPath(path, "id"), id));
		builder.setBasePath(source.get(path, "base"));
		builder.setFilePattern(source.get(path, "pattern"));
		builder.setSourceClass(source.get(path, "class"));
		builder.setTimestampPattern(source.get(path, "timestampPattern"));
		builder.setFallbackId(source.get(path, "fallback"));
		builder.setSourceParams(source.getAsStringMap(AdvancedProperties.createPath(path, "class")));
		return builder.build();
	}
	
	public static AdvancedProperties toProperties(SwitchpipeConfiguration configuration) {
		AdvancedProperties result = new AdvancedProperties(true);
		result.putComment("Autogenerated on: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		result.putEmptyLine();
		
		result.put("switchEnabled", configuration.switchEnabled);
		result.putNonNull("activation.strategy", configuration.activationStrategy);
		result.put("activation.class", configuration.getActivationPolicy());
		result.putMap("activation.class", configuration.activationPolicyParams);
		result.put("pipelineCodec.class", configuration.getPipelineCodec());
		result.putMap("pipelineCodec.class", configuration.pipelineCodecParams);
		
		result.putNonNull("store.defaultStore", configuration.defaultStoreID);
		appendSource(result, "store.default.", configuration.defaults, null);
		for(SourceConfiguration source: configuration.sources)
			appendSource(result, "stores.", source, configuration.defaults);

		return result;
	}
	
	private static void appendSource(AdvancedProperties result, String prefix, SourceConfiguration source, SourceConfiguration defaults) {
		if(source.id != null)
			prefix += source.id + '.';

		appendNonDefault(result, prefix + "class", source.getSource(), defaults != null ? defaults.getSource() : null);
		if(source.sourceParams != null) {
			result.putMap(prefix + "class", source.sourceParams);
		}
		appendNonDefault(result, prefix + "base", source.basePath, defaults != null ? defaults.basePath : null);
		appendNonDefault(result, prefix + "pattern", source.filePattern, defaults != null ? defaults.filePattern : null);
		appendNonDefault(result, prefix + "timestampPattern", source.timestampPattern, defaults != null ? defaults.timestampPattern : null);
		appendNonDefault(result, prefix + "fallback", source.fallbackId, defaults != null ? defaults.fallbackId : null);
	}
	
	private static void appendNonDefault(AdvancedProperties result, String key, String value, String defaultValue) {
		if(value == null)
			return;
		if(defaultValue == null || !defaultValue.equals(value))
			result.put(key, value);
	}
	
	
	public static void main(String[] args) {
		SwitchpipeAPI.CONFIG_LOCATIONS = new String[] {
				"is/switchpipe_test.conf",
		};
		SwitchpipeAPI api = SwitchpipeAPI.defaultInstance();
		api.initialize();
		
		toProperties(api.getConfiguration());
	}
	
	

}
