package hu.ibello.output.cucumber.model;

public class Feature {

    private String uri;
    private String keyword;
    private String name;
    private Tag[] tags;
    private Element[] elements;

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

    public Tag[] getTags() {
        return tags;
    }

    public Element[] getElements() {
        return elements;
    }

    public void addTag(Tag tag) {
        if (tags == null) {
            tags[0] = tag;
            return;
        }
        tags[tags.length] = tag;
    }

    public void addElement(Element element) {
        if (elements == null) {
            elements[0] = element;
            return;
        }
        elements[elements.length] = element;
    }

}
