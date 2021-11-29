package hu.ibello.output.cucumber.model;

import java.util.ArrayList;
import java.util.List;

public class Element {

    private String keyword;
    private String type;
    private String id;
    private int line;
    private String name;
    private List<Tag> tags;
    private List<Step> steps;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }

    public void addStep(Step step) {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<Step> getSteps() {
        return steps;
    }

}
