package hu.ibello.output.cucumber;

import hu.ibello.output.cucumber.model.Tag;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
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
import java.util.Set;

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
		// TODO this condition throws NullPointerException is the tests.getSpec() is null; on the other side, this list cannot be null - see the getSpec() method!
		if (!tests.getSpec().isEmpty() || tests.getSpec() != null) {
			List<SpecElement> specElementList = tests.getSpec();
			for (int i = 0; i < specElementList.size(); i++) {
				features.add(specElementToCucumberFeature(specElementList.get(i), tests));
			}
		}
		return features;
	}

	private CucumberFeature specElementToCucumberFeature(SpecElement specElement, TestRun tests) {
		CucumberFeature feature = new CucumberFeature();
		if (specElement != null) {
			for (int i = 0; i < specElement.getTest().size(); i++) {
				Element convertedElement = elementConverterFromTestElement(specElement.getTest().get(i));
				feature.setName(specElement.getName());
				feature.setKeyword("Feature");
				feature.addElement(convertedElement);
				feature.setUri("");
			}
		}
		if (!tests.getTag().isEmpty()) {
			feature.getTags().addAll(createTags(tests.getTag()));
		}
		return feature;
		}

	private Element elementConverterFromTestElement(TestElement testElement) {
		Element element = new Element();
		// TODO this entire if-else branch is wrong, it is possible that a test does not have any steps but it has a name and id
		// TODO this condition throws NullPointerException is the testElement.getStep() is null; on the other side, this list cannot be null - see the getStep() method!
		if (testElement.getStep().isEmpty() || testElement.getStep() == null) {
			// TODO this message is awkward, use an English sentence
			element.setName("testElement isEmpty!!");
		} else {
			for (int i = 0; i < testElement.getStep().size(); i++) {
				Step step = stepElementToStep(testElement.getStep().get(i));
				element.addStep(step);
			}
			element.setId(testElement.getId());
			element.setKeyword("Scenario");
			element.setName(testElement.getName());
		}
		return element;
	}

	private Step stepElementToStep(StepElement stepElement) {
		Step step = new Step();
		if (stepElement != null) {
				Result result = new Result();
				result.setDuration((double) stepElement.getDurationMs());
				result.setStatus(outcomeToStatus(stepElement.getOutcome()));
				String errorMessage = "";
				// TODO the stepElement.getException() cannot be null, see the getException() method
				if(stepElement.getException() != null || stepElement.getException().isEmpty()){
					for (int i = 0; i < stepElement.getException().size(); i++) {
						errorMessage += i + ". error message : " + stepElement.getException().get(i).getTitle()+" " + "\n";
					}
				}
				// TODO here the error message is always set even if there is no error in the step; this is wrong, set the error message only if there was an error
				result.setError_message(errorMessage);
				step.setResult(result);
				step.setName(stepElement.getName());
				step.setHidden(false);
				step.setKeyword("*");
			}
		return step;
	}

	private Set<Tag> createTags(List<String> tagListString) {
		Set<Tag> tags = new HashSet<>();
		if (!tagListString.isEmpty()) {
			for (String tagString: tagListString) {
				Tag tag = new Tag();
				tag.setName(tagString);
				tags.add(tag);
			}
		}
		return tags;
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