package jetbrick.commons.json;

public class JSONSource implements JSONString {

    private String source;

    public JSONSource(String source) {
        this.source = source;
    }

    @Override
    public String toJSONString() {
        return source;
    }

    @Override
    public String toString() {
        return source;
    }
}
