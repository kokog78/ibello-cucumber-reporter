package hu.ibello.output.cucumber;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import hu.ibello.inject.Injectable;
import hu.ibello.inject.Scope;
import hu.ibello.model.ITestRun;
import hu.ibello.model.SpecElement;
import hu.ibello.model.TestRun;
import hu.ibello.output.cucumber.model.CucumberFeature;
import hu.ibello.plugins.IbelloReporter;
import hu.ibello.plugins.PluginException;
import hu.ibello.plugins.PluginInitializer;

@Injectable(Scope.SINGLETON)
public class CucumberReporter implements IbelloReporter {

	private final static String RESULTS_JSON = "results.json";
	private final static String RESULTS_DIR = "ibello.dir.results";
	
	private PluginInitializer initializer;

	@Override
	public void initialize(PluginInitializer initializer) throws PluginException {
		this.initializer = initializer;
	}

	@Override
	public void testRunStarting(ITestRun tests) {
		// do nothing here
	}

	@Override
	public void specificationFinished(SpecElement spec) {
		// do nothing here
	}

	@Override
	public void testRunFinished(TestRun tests) {
		List<CucumberFeature> features = toFeatures(tests);
		File file = getReportFile();
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			String json = initializer.json().toJson(features);
			saveUTF8String(json, file);
		} catch (Exception ex) {
			initializer.error("Cannot save file: " + file.getAbsolutePath(), ex);
		}
	}
	
	@Override
	public void shutdown() throws PluginException {
		// do nothing
	}
	
	protected File getReportFile() {
		File dir = initializer.getConfigurationValue(RESULTS_DIR).toFile();
		return new File(dir, RESULTS_JSON);
	}

	private void saveUTF8String(String content, File destination) throws IOException {
		try (InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)) ) {
			Files.copy(input, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	private List<CucumberFeature> toFeatures(TestRun tests) {
		List<CucumberFeature> features = new ArrayList<>();
		// TODO Antal: lista feltöltése
		// spec -> feature
		// test -> scenario
		// step -> step
		return features;
	}
	
}
