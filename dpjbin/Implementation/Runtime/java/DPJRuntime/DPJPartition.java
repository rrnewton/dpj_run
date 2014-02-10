package DPJRuntime;

public class DPJPartition<T> {
    private final DPJArray<T> A;
    private final DPJArray<T>[] segs;
    public final int length;
    private final int stride;
    
    public DPJPartition(DPJArray<T> A, final int idx) {
        super();
        this.A = A;
        this.length = 2;
        this.stride = 0;
        segs = (DPJArray<T>[])new DPJArray[length];
        segs[0] = (DPJArray<T>)A.subarray(0, idx);
        segs[1] = (DPJArray<T>)A.subarray(idx, A.length - idx);
    }
    
    public DPJPartition(DPJArray<T> A, final int idx, boolean exclude) {
        super();
        this.A = A;
        this.length = 2;
        this.stride = 0;
        this.segs = (DPJArray<T>[])new DPJArray[length];
        segs[0] = (DPJArray<T>)A.subarray(0, idx);
        if (exclude) {
            segs[1] = (DPJArray<T>)A.subarray(idx + 1, A.length - idx - 1);
        } else {
            segs[1] = (DPJArray<T>)A.subarray(idx, A.length - idx);
        }
    }
    
    private DPJPartition(DPJArray<T> A, int stride, double strided) {
        super();
        this.A = A;
        this.stride = stride;
        this.length = (A.length / stride) + ((A.length % stride == 0) ? 0 : 1);
        this.segs = null;
    }
    
    public static <T>DPJPartition<T> stridedPartition(DPJArray<T> A, int stride) {
        return new DPJPartition<T>(A, stride, 0.0);
    }
    
    public DPJPartition(DPJArray<T> A, int[] idxs) {
        super();
        this.A = A;
        this.length = idxs.length + 1;
        this.segs = (DPJArray<T>[])new DPJArray[length];
        this.stride = 0;
        if (length == 1) segs[0] = (DPJArray<T>)A; else {
            int i = 0;
            int len = 0;
            segs[0] = (DPJArray<T>)A.subarray(0, idxs[0]);
            for (i = 1; i < idxs.length; ++i) {
                len = idxs[i] - idxs[i - 1];
                if (len < 0) throw new ArrayIndexOutOfBoundsException();
                final int j = i;
                segs[j] = (DPJArray<T>)A.subarray(idxs[j - 1], len);
            }
            i = idxs[idxs.length - 1];
            len = A.length - i;
            final int length = idxs.length;
            segs[length] = (DPJArray<T>)A.subarray(i, len);
        }
    }
    
    public DPJArray<T> get(final int idx) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (segs != null) return segs[idx]; else {
            int start = idx * stride;
            int segLength = (start + stride > A.length) ? (A.length - start) : stride;
            return (DPJArray<T>)A.subarray(start, segLength);
        }
    }
}
