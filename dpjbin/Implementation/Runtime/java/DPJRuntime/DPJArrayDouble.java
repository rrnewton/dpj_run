package DPJRuntime;

public class DPJArrayDouble {
    private final double[] elts;
    public final int start;
    public final int length;
    
    public DPJArrayDouble(int length) {
        super();
        this.elts = new double[length];
        this.start = 0;
        this.length = length;
    }
    
    public DPJArrayDouble(double[] elts) {
        super();
        this.elts = elts;
        this.start = 0;
        this.length = elts.length;
    }
    
    private DPJArrayDouble(double[] elts, int start, int length) {
        super();
        this.elts = elts;
        this.start = start;
        this.length = length;
    }
    
    public double get(int idx) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return elts[start + idx];
    }
    
    public void put(int idx, double val) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        elts[start + idx] = val;
    }
    
    public DPJArrayDouble subarray(int start, int length) {
        if (start < 0 || length < 0 || start + length > this.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return new DPJArrayDouble(elts, this.start + start, length);
    }
    
    public double[] toArray() {
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
        double tmp = elts[start + i];
        elts[start + i] = elts[start + j];
        elts[start + j] = tmp;
    }
}
