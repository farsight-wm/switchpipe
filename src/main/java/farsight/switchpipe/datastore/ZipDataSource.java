package farsight.switchpipe.datastore;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import farsight.switchpipe.SwitchID;
import farsight.switchpipe.configuration.SourceConfiguration;
import farsight.switchpipe.expression.ElExpression;

public class ZipDataSource extends FileDataSource implements DataSource {
	
	protected final ElExpression zipPattern;
	protected final DateTimeFormatter zipTimestampForamt;	
	
	public ZipDataSource(SourceConfiguration configuration) {
		super(configuration);
		this.zipPattern = ElExpression.parse(configuration.getParam("zipPattern", "archive.${zipTimestamp}.zip"));
		this.zipTimestampForamt = DateTimeFormatter.ofPattern(configuration.getParam("zipTimestamp", "YYYYMMdd"));
	}

	@Override
	protected Path findPath(SwitchID switchID) {
		try {
			//1st find and open Archive
			FileSystem archive = getArchive(switchID);
			//2nd start normal findPath search within archive
			if(archive != null)
				return findPath(switchID, archive.getRootDirectories().iterator().next());
			
		} catch(IOException e) {
			//not found
		}
		return null;
	}
	
	private FileSystem getArchive(SwitchID switchID) throws IOException {
		LocalDateTime invokeDate = getInvokeDate(switchID);
		Map<String, String> expressionMap = createExpressionMap(switchID.servicePath(), switchID.invokeID);
		expressionMap.put("zipTimestamp", zipTimestampForamt.format(invokeDate));
		String path = zipPattern.evaluate(expressionMap);
		Path archivePath = basePath.resolve(path);
		
		if(isReadableFile(archivePath)) {
			return FileSystems.newFileSystem(archivePath, null);
		}
		return null;
	}

	private LocalDateTime getInvokeDate(SwitchID switchID) {
		//1st use switchIDs own date
		if(switchID.date != null)
			return switchID.date;
		
		//2nd try to parse invokeID
		if(switchID.invokeID != null) {
			try {
				return LocalDateTime.parse(switchID.invokeID, timestampFormat);
			} catch (DateTimeParseException e) {
				//to bad...
			}
		}
		
		//last resort: fallback to now
		return LocalDateTime.now();
	}

}
