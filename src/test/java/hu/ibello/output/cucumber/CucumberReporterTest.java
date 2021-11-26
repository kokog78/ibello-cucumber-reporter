package hu.ibello.output.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.registerCustomDateFormat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import hu.ibello.JsonTransformerMock;
import hu.ibello.apitest.HttpClient;
import hu.ibello.apitest.RestClient;
import hu.ibello.bdd.ExamplesHandler;
import hu.ibello.bdd.FeatureHandler;
import hu.ibello.core.Value;
import hu.ibello.data.TestDataBuilder;
import hu.ibello.functions.RegressionTool;
import hu.ibello.graph.GraphTool;
import hu.ibello.model.TestRun;
import hu.ibello.output.TestResultLoader;
import hu.ibello.plugins.PluginException;
import hu.ibello.plugins.PluginInitializer;
import hu.ibello.requirements.RequirementHandler;
import hu.ibello.table.TableTool;
import hu.ibello.transform.CsvTransformer;
import hu.ibello.transform.JsonTransformer;

public class CucumberReporterTest {

	private CucumberReporter reporter;
	private File file;
	private List<String> errors;
	// tear down commented!!!
	@Test
	public void testRunFinished_should_log_error_if_file_cannot_be_written() throws Exception {
		// make the file non-writable
		file.delete();
		file.mkdirs();
		File file2 = new File(file, "a.txt");
		file2.createNewFile();

		TestRun testRun = new TestRun();
		reporter.testRunFinished(testRun);
		
		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).contains(file.getAbsolutePath());
	}
	
	@Test
	public void testRunFinished_should_log_empty_results() throws Exception {
		TestRun testRun = new TestRun();
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[]");
	}

	@Test
	public void testRunFinished_() throws Exception {
		TestRun testRun;
		testRun = reporter.testRunMockDataCreator();
		reporter.testRunFinished(testRun);
		String json = loadJson();
	}


	@Before
	public void init() throws IOException, PluginException {
		errors = new ArrayList<>();
		file = File.createTempFile("ibello-", ".json");
		reporter = new CucumberReporter() {
			@Override
			protected File getReportFile() {
				return file;
			}
		};
		PluginInitializer initializer = new PluginInitializer() {
			
			@Override
			public TestDataBuilder testData() {
				return null;
			}
			
			@Override
			public TableTool table() {
				return null;
			}
			
			@Override
			public void info(String message) {
			}
			
			@Override
			public GraphTool graph() {
				return null;
			}
			
			@Override
			public void error(String message, Throwable exception) {
				errors.add(message);
			}

			@Override
			public Value getConfigurationValue(String name) {
				return null;
			}

			@Override
			public String getMergedURL(String url) {
				return null;
			}

			@Override
			public RestClient restClient() {
				return null;
			}

			@Override
			public HttpClient httpClient() {
				return null;
			}

			@Override
			public FeatureHandler features() {
				return null;
			}

			@Override
			public ExamplesHandler examples() {
				return null;
			}

			@Override
			public RequirementHandler requirements() {
				return null;
			}

			@Override
			public TestResultLoader testResults() {
				return null;
			}

			@Override
			public JsonTransformer json() {
				return new JsonTransformerMock();
			}

			@Override
			public CsvTransformer csv() {
				return null;
			}

			@Override
			public RegressionTool regression() {
				return null;
			}
		};
		reporter.initialize(initializer);
	}
	
	@After
	public void teardown() throws IOException {
		/*if (file.isDirectory()) {
			FileUtils.deleteDirectory(file);
		} else {
			file.delete();
		}*/
	}
	
	private String loadJson() throws IOException {
		String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		json = json.replaceAll("\\s+", "");
		return json;
	}
}
