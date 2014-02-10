package DPJRuntime;

public class DPJArray<T> {
    private final T[] elts;
    public final int start;
    public final int length;
    
    public DPJArray(int length) {
        super();
        this.elts = (T[])new Object[length];
        this.start = 0;
        this.length = length;
    }
    
    public DPJArray(T[] elts) {
        super();
        this.elts = elts;
        this.start = 0;
        this.length = elts.length;
    }
    
    private DPJArray(T[] elts, int start, int length) {
        super();
        this.elts = elts;
        this.start = start;
        this.length = length;
    }
    
    public T get(int idx) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return elts[start + idx];
    }
    
    public void put(int idx, T val) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        elts[start + idx] = val;
    }
    
    public DPJArray<T> subarray(int start, int length) {
        if (start < 0 || length < 0 || start + length > this.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return new DPJArray<T>(elts, this.start + start, length);
    }
    
    public T[] toArray() {
        return elts;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (length > 0) {
            sb.append(this.get(0));
            for (int i = 1; i < length; ++i) {
                sb.append(" ");
                sb.append(this.get(i));
            }
        }
        return sb.toString();
    }
    
    public void swap(int i, int j) {
        T tmp = elts[start + i];
        elts[start + i] = elts[start + j];
        elts[start + j] = tmp;
    }
}
