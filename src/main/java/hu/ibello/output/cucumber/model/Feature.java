package hu.ibello.output.cucumber.model;

import java.util.ArrayList;
import java.util.List;

public class Feature {

    private String uri;
    private String keyword;
    private String name;
    private List<Tag> tags;
    private List<Element> elements;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }

    public void addElement(Element element) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(element);
    }

}
