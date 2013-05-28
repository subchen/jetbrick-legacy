package jetbrick.commons.json;

public interface JSONFilter {

    public boolean accept(Class<?> clazz);

    public String apply(Object object, JSONFilter[] filters);
}
