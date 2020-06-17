package farsight.switchpipe.pipelinecodec;

import java.io.InputStream;

import com.wm.data.IData;

public interface PipelineCodec {
	
	public byte[] encodePipeline(IData pipeline);
	public IData decodePipeline(InputStream stream);

}
