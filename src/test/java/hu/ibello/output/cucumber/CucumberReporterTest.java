package hu.ibello.output.cucumber;

import static org.assertj.core.api.Assertions.assertThat;

import hu.ibello.model.Element;
import hu.ibello.model.ExceptionInfo;
import hu.ibello.model.Outcome;
import hu.ibello.model.SpecElement;
import hu.ibello.model.StepElement;
import hu.ibello.model.TestElement;
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
	public void testRunFinished_test_with_no_data_should_log_empty_results() throws Exception {
		TestRun testRun = new TestRun();
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[]");
	}
	@Test
	public void testRunFinished_test_with_one_empty_spec_element_should_log_two_brackets() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{}]");
	}

	@Test
	public void testRunFinished_test_with_one_empty_test_element_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"name\":\"testElementisEmpty!!\"}]}]");
	}

	@Test
	public void testRunFinished_test_with_one_empty_step_element_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"line\":0}]}]}]");
	}
	@Test
	public void testRunFinished_test_with_one_step_element_and_one_children_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		children.add(new Element());
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"FAILED\",\"duration\":0.0,\"error_message\":\"\"}}]}]}]");
	}
	@Test
	public void testRunFinished_test_with_one_step_element_with_duration_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		Element element = new Element();
		element.setDurationMs(10000);
		children.add(element);
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"FAILED\",\"duration\":10000.0,\"error_message\":\"\"}}]}]}]");
	}
	@Test
	public void testRunFinished_test_with_one_step_element_with_outcome_passed_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		Element element = new Element();
		element.setOutcome(Outcome.SUCCESS);
		children.add(element);
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"PASSED\",\"duration\":0.0,\"error_message\":\"\"}}]}]}]");
	}
	@Test
	public void testRunFinished_test_with_one_step_element_with_outcome_pending_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		Element element = new Element();
		element.setOutcome(Outcome.PENDING);
		children.add(element);
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"PENDING\",\"duration\":0.0,\"error_message\":\"\"}}]}]}]");
	}
	@Test
	public void testRunFinished_test_with_one_step_element_with_outcome_failure_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		Element element = new Element();
		element.setOutcome(Outcome.FAILURE);
		children.add(element);
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"FAILED\",\"duration\":0.0,\"error_message\":\"\"}}]}]}]");
	}

	@Test
	public void testRunFinished_test_with_one_step_element_with_outcome_error_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		Element element = new Element();
		element.setOutcome(Outcome.ERROR);
		children.add(element);
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"FAILED\",\"duration\":0.0,\"error_message\":\"\"}}]}]}]");
	}

	@Test
	public void testRunFinished_test_with_one_step_element_with_error_message_should_log_well() throws Exception {
		TestRun testRun = new TestRun();
		List<SpecElement> specElements = new ArrayList<>();
		SpecElement specElement = new SpecElement();
		List<TestElement> testElements = new ArrayList<>();
		TestElement testElement = new TestElement();
		StepElement stepElement = new StepElement();
		List<Element> children = new ArrayList<>();
		Element element = new Element();
		List<ExceptionInfo> exceptionList = new ArrayList<>();
		exceptionList.add(new ExceptionInfo());
		element.setException(exceptionList);
		children.add(element);
		stepElement.setChildren(children);
		List<StepElement> stepElements = new ArrayList<>();
		stepElements.add(stepElement);
		testElement.setStep(stepElements);
		testElements.add(testElement);
		specElement.setTest(testElements);
		specElements.add(specElement);
		testRun.setSpec(specElements);
		reporter.testRunFinished(testRun);
		String json = loadJson();
		assertThat(json).isEqualTo("[{\"elements\":[{\"line\":0,\"steps\":[{\"keyword\":\"keeeeeeyword\",\"line\":0,\"hidden\":false,\"result\":{\"status\":\"FAILED\",\"duration\":0.0,\"error_message\":\"0.errormessage:null\\\\n\"}}]}]}]");
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
		if (file.isDirectory()) {
			FileUtils.deleteDirectory(file);
		} else {
			file.delete();
		}
	}
	
	private String loadJson() throws IOException {
		String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		json = json.replaceAll("\\s+", "");
		return json;
	}
}
