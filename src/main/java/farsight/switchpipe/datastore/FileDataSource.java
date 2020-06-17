package farsight.switchpipe.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import farsight.switchpipe.SwitchID;
import farsight.switchpipe.configuration.SourceConfiguration;
import farsight.switchpipe.exception.SwitchpipeDataException;

public class FileDataSource extends AbstractFileDataSource {
	
	public FileDataSource(SourceConfiguration configuration) {
		super(configuration);
	}

	@Override
	public InputStream loadIfExists(SwitchID switchID) throws SwitchpipeDataException {
		Path path = findPath(switchID);
		if(isReadableFile(path)) try {
			return Files.newInputStream(path);			
		} catch (IOException e) {
			//ignore
		}
		return null;
	}
	
	@Override
	public SwitchID findPipeline(SwitchID switchID) throws SwitchpipeDataException {
		Path path = findPath(switchID);
		if(path == null)
			return null;
		
		String string = path.toString();
		//XXX get extension from configuration/codec
		string = string.substring(0, string.lastIndexOf('.'));
		
		//works only with constant length IDs do not use parts like day names!
		int idLength = LocalDateTime.now().format(timestampFormat).length();
		string = string.substring(string.length() - idLength);
		
		return SwitchID.createExactQuery(string, switchID.serviceID, switchID.storeID);
	}
	







}
