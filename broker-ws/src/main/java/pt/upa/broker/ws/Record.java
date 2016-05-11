package pt.upa.broker.ws;


public class Record<VALUE> {
    public final long key;
    public final VALUE value;

    private static long maxCurrentKey = 0;

    public Record(VALUE value) {
        if (value == null) {
            throw new IllegalArgumentException("null argument");
        }
        this.key = maxCurrentKey++;
        this.value = value;
    }
}
