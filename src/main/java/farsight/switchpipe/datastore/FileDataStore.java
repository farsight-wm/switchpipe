package farsight.switchpipe.datastore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import farsight.switchpipe.SwitchID;
import farsight.switchpipe.configuration.SourceConfiguration;
import farsight.switchpipe.exception.SwitchpipeDataException;

public class FileDataStore extends FileDataSource implements DataStore {
	
	public FileDataStore(SourceConfiguration configuration) {
		super(configuration);
	}

	@Override
	public String store(SwitchID switchID, byte[] data) throws SwitchpipeDataException {
		String invokeID = switchID.invokeID;
		if(invokeID == null || invokeID.isEmpty())
			invokeID = generateInvokeID();
		Path path = basePath.resolve(filePattern.evaluate(createExpressionMap(switchID.servicePath(), invokeID)));
		try {
			//create directory if not existent
			Files.createDirectories(path.getParent());
			
			//save file (in current thread)
			Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			
			return invokeID;
			
		} catch (IOException e) {
			throw SwitchpipeDataException.unableToWriteRessource(path, e);
		}
	}

	@Override
	public String generateInvokeID(LocalDateTime timestamp) {
		return timestampFormat.format(timestamp);
	}
	
	@Override
	public String generateInvokeID() {
		return generateInvokeID(LocalDateTime.now());
	}
		
	
	
}
