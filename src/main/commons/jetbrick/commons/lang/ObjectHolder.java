package jetbrick.commons.lang;

public final class ObjectHolder<T> {

    private T value;

    public ObjectHolder() {
    }

    public ObjectHolder(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void put(T value) {
        this.value = value;
    }

}
