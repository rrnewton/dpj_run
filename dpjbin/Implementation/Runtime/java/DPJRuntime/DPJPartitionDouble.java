package DPJRuntime;

public class DPJPartitionDouble {
    private final DPJArrayDouble A;
    private final DPJArrayDouble[] segs;
    public final int length;
    private final int stride;
    
    public DPJPartitionDouble(DPJArrayDouble A, final int idx) {
        super();
        this.A = A;
        this.length = 2;
        this.stride = 0;
        segs = (DPJArrayDouble[])new DPJArrayDouble[length];
        segs[0] = (DPJArrayDouble)A.subarray(0, idx);
        segs[1] = (DPJArrayDouble)A.subarray(idx, A.length - idx);
    }
    
    public DPJPartitionDouble(DPJArrayDouble A, final int idx, boolean exclude) {
        super();
        this.A = A;
        this.length = 2;
        this.stride = 0;
        this.segs = (DPJArrayDouble[])new DPJArrayDouble[length];
        segs[0] = (DPJArrayDouble)A.subarray(0, idx);
        if (exclude) {
            segs[1] = (DPJArrayDouble)A.subarray(idx + 1, A.length - idx - 1);
        } else {
            segs[1] = (DPJArrayDouble)A.subarray(idx, A.length - idx);
        }
    }
    
    private DPJPartitionDouble(DPJArrayDouble A, int stride, double strided) {
        super();
        this.A = A;
        this.stride = stride;
        this.length = (A.length / stride) + ((A.length % stride == 0) ? 0 : 1);
        this.segs = null;
    }
    
    public static DPJPartitionDouble stridedPartition(DPJArrayDouble A, int stride) {
        return new DPJPartitionDouble(A, stride, 0.0);
    }
    
    public DPJPartitionDouble(DPJArrayDouble A, int[] idxs) {
        super();
        this.A = A;
        this.length = idxs.length + 1;
        this.segs = (DPJArrayDouble[])new DPJArrayDouble[length];
        this.stride = 0;
        if (length == 1) segs[0] = (DPJArrayDouble)A; else {
            int i = 0;
            int len = 0;
            segs[0] = (DPJArrayDouble)A.subarray(0, idxs[0]);
            for (i = 1; i < idxs.length; ++i) {
                len = idxs[i] - idxs[i - 1];
                if (len < 0) throw new ArrayIndexOutOfBoundsException();
                final int j = i;
                segs[j] = (DPJArrayDouble)A.subarray(idxs[j - 1], len);
            }
            i = idxs[idxs.length - 1];
            len = A.length - i;
            final int length = idxs.length;
            segs[length] = (DPJArrayDouble)A.subarray(i, len);
        }
    }
    
    public DPJArrayDouble get(final int idx) {
        if (idx < 0 || idx > length - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (segs != null) return segs[idx]; else {
            int start = idx * stride;
            int segLength = (start + stride > A.length) ? (A.length - start) : stride;
            return (DPJArrayDouble)A.subarray(start, segLength);
        }
    }
}
