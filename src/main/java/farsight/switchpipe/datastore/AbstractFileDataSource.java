package farsight.switchpipe.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import farsight.switchpipe.SwitchID;
import farsight.switchpipe.configuration.SourceConfiguration;
import farsight.switchpipe.exception.SwitchpipeDataException;
import farsight.switchpipe.expression.ElExpression;

public abstract class AbstractFileDataSource implements DataSource {

	protected final Path basePath;
	protected final DateTimeFormatter timestampFormat;
	protected final ElExpression filePattern;
	protected final String timstampPattern;
	
	public AbstractFileDataSource(SourceConfiguration configuration) {
		this.basePath = Paths.get(configuration.basePath);
		this.timstampPattern = configuration.timestampPattern;
		this.timestampFormat = DateTimeFormatter.ofPattern(configuration.timestampPattern);
		this.filePattern = ElExpression.parse(configuration.filePattern);
	}

	@Override
	public abstract InputStream loadIfExists(SwitchID switchID) throws SwitchpipeDataException;

	@Override
	public InputStream loadStream(SwitchID switchID) throws SwitchpipeDataException {
		InputStream result = loadIfExists(switchID);
		if(result == null) {
			throw SwitchpipeDataException.pipelineNotFound(switchID);
		}
		return result;
	}
	
	@Override
	public boolean exists(SwitchID switchID) {
		Path path = findPath(switchID);
		return path != null && isReadableFile(path);	
	}

	@Override
	public void configure(SourceConfiguration configuration) {} //nothing to do
	
	protected Map<String, String> createExpressionMap(String serviceID, String invokeID) {
		HashMap<String, String> map = new HashMap<>();
		map.put("serviceID", serviceID);
		map.put("invokeID", invokeID);
		return map;
	}
	
	protected String eavaluteExpression(SwitchID switchID) {
		return filePattern.evaluate(createExpressionMap(switchID.servicePath(), switchID.invokeID));
	}
	
	
	protected Path findPath(SwitchID switchID) {
		return findPath(switchID, basePath);
	}
	
	protected Path findPath(SwitchID switchID, Path base) {
		if(switchID.isExact())
			return base.resolve(eavaluteExpression(switchID));
		
		//find path by date, direction, offset
		Path searchPath = base.resolve(eavaluteExpression(switchID)).getParent();
		
		final String prefix = switchID.getPrefix(timstampPattern);
		final String suffix = ".xml"; //TODO make this property of serializer, and serialzier configurable at source
		
		Optional<Path> optional = null;
		try {
			if(switchID.isLatest()) {
				optional = Files.walk(searchPath)
						.sequential()
						.sorted(Comparator.reverseOrder())
						.filter(p -> {
							if (!Files.isRegularFile(p))
								return false;
							String file = p.getFileName().toString();
							return file.startsWith(prefix) && file.endsWith(suffix);})
						.skip(switchID.offset)
						.findFirst();
			} else {
				optional = Files.walk(searchPath)
						.sequential()
						.filter(p -> {
							if (!Files.isRegularFile(p))
								return false;
							String file = p.getFileName().toString();
							return file.startsWith(prefix) && file.endsWith(suffix);})
						.skip(switchID.offset)
						.findFirst();
			}
		} catch(IOException e) {
			return null;
		}
		
		if(optional != null && optional.isPresent()) {
			return optional.get();
		}
		
		return null;
	}
	
	protected boolean isReadableFile(Path path) {
		return path != null && Files.exists(path) && Files.isRegularFile(path);
	}
	
	

}
