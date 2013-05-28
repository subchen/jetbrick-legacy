package jetbrick.dao.id;

public class SequenceId {
    public static final long NOT_FOUND = 0;
    private static final long CACHE_SIZE = 50;
    private final SequenceIdProvider provider;
    private final String name;
    private final long beginValue;
    private long value;

    protected SequenceId(SequenceIdProvider provider, String name, long beginValue) {
        this.provider = provider;
        this.name = name;
        this.beginValue = beginValue;
        this.value = -1;

        if (beginValue <= 0) {
            throw new IllegalArgumentException("begin value must be great than zero.");
        }
    }

    public String getName() {
        return name;
    }

    public synchronized long nextVal() {
        if (value < 0) {
            value = provider.load(name);
            if (value <= NOT_FOUND) {
                value = beginValue - 1;
            }
            provider.store(name, value + CACHE_SIZE);
        }

        value++;

        if (value % CACHE_SIZE == 0) {
            provider.store(name, value + CACHE_SIZE);
        }

        return value;
    }
}
