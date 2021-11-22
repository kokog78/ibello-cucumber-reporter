package hu.ibello.output.cucumber.model;

public class Element {

    private String keyword;
    private String type;
    private String id;
    private int line;
    private String name;
    private Tag[] tags;
    private Step[] steps;

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
            tags[0] = tag;
            return;
        }
        tags[tags.length] = tag;
    }

    public void addStep(Step step) {
        if (steps == null) {
            steps[0] = step;
            return;
        }
        steps[steps.length] = step;
    }

    public Tag[] getTags() {
        return tags;
    }

    public Step[] getSteps() {
        return steps;
    }

}
