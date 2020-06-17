package farsight.switchpipe.pipelinecodec;

import java.io.InputStream;
import java.util.Map;

import com.wm.data.IData;

import farsight.utils.idata.PipelineSerializer;

public class DefaultPipelineCodec implements PipelineCodec {
	
	private static final int DEFAULT_MAX_PIPELINE_SIZE = 2 << 20; // 2MB
	private static final String UNLIMITED_PIPELINE_SIZE = "NO_LIMIT";

	
	private final int maxSize;
	
	public DefaultPipelineCodec() {
		this(DEFAULT_MAX_PIPELINE_SIZE);
	}
	
	public DefaultPipelineCodec(Map<String, String> args) {
		this(getMaxPipelineSizeFromArgs(args));
	}
	
	public DefaultPipelineCodec(int maxPipelineSize) {
		if(maxPipelineSize < 0)
			maxPipelineSize = 0;
		this.maxSize = maxPipelineSize;
	}

	private static int getMaxPipelineSizeFromArgs(Map<String, String> args) {
		if(args == null || !args.containsKey("maxSize"))
			return DEFAULT_MAX_PIPELINE_SIZE;
		String maxSizeString = args.get("maxSize").trim();
		
		if(maxSizeString.equals(UNLIMITED_PIPELINE_SIZE))
			return 0;
		return parseSizeString(maxSizeString, DEFAULT_MAX_PIPELINE_SIZE);
	}

	public static int parseSizeString(String value, int defaultValue) {
		if(value == null)
			return defaultValue;
		value = value.trim();
		
		if (value.matches("\\d+\\s?[mMkKbB]")) {
			String number = value.substring(0, value.length() - 1).trim();
			String unit = value.substring(value.length() - 1).toLowerCase();

			int shift = 0;
			if ("k".equals(unit))
				shift = 10;
			else if ("m".equals(unit))
				shift = 20;

			return Integer.valueOf(number, 10) << shift;
		} else if(value.matches("\\d+")) {
			return Integer.valueOf(value, 10);
		}
		return defaultValue;
	}

	@Override
	public byte[] encodePipeline(IData pipeline) {
		return PipelineSerializer.serializePipelineXML(pipeline, maxSize);
	}

	@Override
	public IData decodePipeline(InputStream stream) {
		return PipelineSerializer.deserializePipelineXML(stream);
	}

}
