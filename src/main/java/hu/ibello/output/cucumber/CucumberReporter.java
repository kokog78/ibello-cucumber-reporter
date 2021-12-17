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
import hu.ibello.model.Outcome;
import hu.ibello.model.SpecElement;
import hu.ibello.model.StepElement;
import hu.ibello.model.TestElement;
import hu.ibello.model.TestRun;
import hu.ibello.output.cucumber.model.CucumberFeature;
import hu.ibello.output.cucumber.model.Element;
import hu.ibello.output.cucumber.model.Result;
import hu.ibello.output.cucumber.model.Status;
import hu.ibello.output.cucumber.model.Step;
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
		try (InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
			Files.copy(input, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	private List<CucumberFeature> toFeatures(TestRun tests) {
		List<CucumberFeature> features = new ArrayList<>();
		if (!tests.getSpec().isEmpty()) {
			List<SpecElement> specElementList = tests.getSpec();   //spec -> feature
			for (int i = 0; i < specElementList.size(); i++) {
				features.add(specElementToCucumberFeature(specElementList.get(i)));
			}
		}
		return features;
	}

	private CucumberFeature specElementToCucumberFeature(SpecElement specElement) {
		CucumberFeature cucumberFeature = new CucumberFeature();
		for (int i = 0; i < specElement.getTest().size(); i++) {
			Element convertedElement = elementConverterFromTestElement(specElement.getTest().get(i));
			cucumberFeature.addElement(convertedElement);
			}
		return cucumberFeature;
		}

	private Element elementConverterFromTestElement(TestElement testElement) {
		Element element = new Element();
		if (testElement.getStep().isEmpty()) {
			element.setName("testElement isEmpty!!");
		} else {
			for (int i = 0; i < testElement.getStep().size(); i++) {
				Step step = stepConverterFromStepElement(testElement.getStep().get(i));
				element.addStep(step);
			}
		}
		return element;
	}

	private Step stepConverterFromStepElement(StepElement stepElement) {
		Step step = new Step();
		for (int i = 0; i < stepElement.getChildren().size(); i++) {
			hu.ibello.model.Element toConvert = stepElement.getChildren().get(i);
			Result result = new Result();
			result.setDuration((double) toConvert.getDurationMs());
			result.setStatus(outcomeToStatus(toConvert.getOutcome()));
			String errorMessage = "";
			if(toConvert.getException() != null || toConvert.getException().isEmpty()){
				for (int j = 0; j < toConvert.getException().size(); j++) {
					errorMessage += i + ". error message : " + toConvert.getException().get(i).getTitle() + "\\n";
				}
			}
			result.setError_message(errorMessage);
			step.setResult(result);
			step.setName(toConvert.getName());
			if (toConvert.getType() != null) {
				step.setKeyword(toConvert.getType().toString());
			} else {
				step.setKeyword("");
			}
			step.setHidden(false);
			step.setLine(i);
		}
		return step;
	}
	private Status outcomeToStatus (Outcome outcome) {
		if (outcome != null) {
			switch (outcome) {
				case SUCCESS:
					return Status.PASSED;
				case PENDING:
					return Status.PENDING;
				default:
					return Status.FAILED;
			}
		}
		return Status.FAILED;
	}
}