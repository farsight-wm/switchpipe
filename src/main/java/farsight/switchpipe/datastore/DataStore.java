package farsight.switchpipe.datastore;

import java.time.LocalDateTime;

import farsight.switchpipe.SwitchID;
import farsight.switchpipe.exception.SwitchpipeDataException;

public interface DataStore extends DataSource {
	
	public String store(SwitchID switchID, byte[] data) throws SwitchpipeDataException;
	public String generateInvokeID(LocalDateTime timestamp);
	public String generateInvokeID();


}
