package farsight.switchpipe.datastore;

import java.io.InputStream;

import farsight.switchpipe.SwitchID;
import farsight.switchpipe.configuration.SourceConfiguration;
import farsight.switchpipe.exception.SwitchpipeDataException;

public interface DataSource {
	
	public void configure(SourceConfiguration configuration);
	
	public SwitchID findPipeline(SwitchID switchID) throws SwitchpipeDataException;
	public InputStream loadIfExists(SwitchID switchID) throws SwitchpipeDataException;
	public InputStream loadStream(SwitchID switchID) throws SwitchpipeDataException;
	public boolean exists(SwitchID switchID)  throws SwitchpipeDataException;	
}
