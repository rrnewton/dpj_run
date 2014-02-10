package DPJRuntime;

public class DPJPartitionBoolean {
    private final DPJArrayBoolean A;
    private final DPJArrayBoolean[] segs;
    public final int length;
    private final int stride;
    
    public DPJPartitionBoolean(DPJArrayBoolean A, final int idx) {
        super();
        this.A = A;
        this.length = 2;
        this.stride = 0;
        segs = (DPJArrayBoolean[])new DPJArrayBoolean[length];
        segs[0] = (DPJArrayBoolean)A.subarray(0, idx);
        segs[1] = (DPJArrayBoolean)A.subarray(idx, A.length - idx);
    }
    
    public DPJPartitionBoolean(DPJArrayBoolean A, final int idx, boolean exclude) {
        super();
        this.A = A;
        this.length = 2;
        this.stride = 0;
        this.segs = (DPJArrayBoolean[])new DPJArrayBoolean[length];
        segs[0] = (DPJArrayBoolean)A.subarray(0, idx);
        if (exclude) {
            segs[1] = (DPJArrayBoolean)A.subarray(idx + 1, A.length - idx - 1);
        } else {
            segs[1] = (DPJArrayBoolean)A.subarray(idx, A.length - idx);
        }
    }
    
    private DPJPartitionBoolean(DPJArrayBoolean A, int stride, double strided) {
        super();
        this.A = A;
        this.stride = stride;
        this.length = (A.length / stride) + ((A.length % stride == 0) ? 0 : 1);
        this.segs = null;
    }
    
    public static DPJPartitionBoolean stridedPartition(DPJArrayBoolean A, int stride) {
        return new DPJPartitionBoolean(A, stride, 0.0);
    }
    
    public DPJPartitionBoolean(DPJArrayBoolean A, int[] idxs) {
        super();
        this.A = A;
        this.length = idxs.length + 1;
        this.segs = (DPJArrayBoolean[])new DPJArrayBoolean[length];
        this.stride = 0;
        if (length == 1) segs[0] = (DPJArrayBoolean)A; else {
            int i = 0;
            int len = 0;
            segs[0] = (DPJArrayBoolean)A.subarray(0, idxs[0]);
            for (i = 1; i < idxs.length; ++i) {
                len = idxs[i] - idxs[i - 1];
                if (len < 0) throw new ArrayIndexOutOfBoundsException();
                final int j = i;
                segs[j] = (DPJArrayBoolean)A.subarray(idxs[j - 1], len);
            }
            i = idxs[idxs.length - 1];
            len = A.length - i;
            final int length = idxs.length;
            segs[length] = (DPJArrayBoolean)A.subarray(i, len);
        }
    }
    
    public DPJArrayBoolean get(final int idx) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (segs != null) return segs[idx]; else {
            int start = idx * stride;
            int segLength = (start + stride > A.length) ? (A.length - start) : stride;
            return (DPJArrayBoolean)A.subarray(start, segLength);
        }
    }
}
