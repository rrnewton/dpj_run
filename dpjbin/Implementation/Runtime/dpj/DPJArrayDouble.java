// WARNING:  THIS FILE IS AUTO-GENERATED

package DPJRuntime;

/**
 * The {@link DPJArray} class, specialized to {@code double}.
 *
 * <p>The {@code DPJArrayDouble} class wraps and provides a "view" of an
 * ordinary Java array of {@code double}.  In addition to array-element
 * access, the {@code DPJArrayDouble} class supports the creation of a
 * <i>subarray</i>, which is a contiguous slice of the original array.
 * The subarray itself is a new {@code DPJArrayDouble} object.  Both the
 * original {@code DPJArrayDouble} and the subarray wrap the same
 * underlying Java array.
 *
 * <p>A {@code DPJArrayDouble} stores a start position (indexed into the
 * underlying array) and a length.  Accesses to the {@code DPJArrayDouble}
 * are translated into accesses into the underlying array, offset by
 * the start position.  They are bounds-checked against the length of
 * the {@code DPJArrayDouble}.  For example:
 *
 * <p><blockquote><pre>
 * // Create a Java array a of 10 double
 * double[] a = new double[10];
 * // Wrap a in a DPJArrayDouble
 * DPJArrayDouble A = new DPJArrayDouble(a);
 * // Create a subarray of A
 * DPJArrayDouble B = A.subarray(5,2);
 * // Store value 1 into position 5 of a
 * B.put(0,1);
 * // Error:  Out of bounds
 * B.put(3,5);
 * </pre></blockquote><p>
 * 
 * @author Rob Bocchino
 * @param <R> The region of a cell of this {@code DPJArrayDouble}
 *
 */
public class DPJArrayDouble<region R> {

    /**
     * The underlying array representation
     */
    private final double[]<R> elts in R;

    /**
     * The start index for indexing into the underlying array
     */
    public final int start in R;

    /**
     * The number of elements in the {@code DPJArrayDouble}
     */
    public final int length in R;

    /**
     * Creates a {@code DPJArrayDouble} of the specified length, wrapping
     * a freshly created Java array with the same length.
     *
     * @param length  The length of the {@code DPJArrayDouble}
     */
    public DPJArrayDouble(int length) pure {
	this.elts = new double[length]<R>;
	this.start = 0;
	this.length = length;
    }

    /**
     * Creates a {@code DPJArrayDouble} that wraps the given Java array.
     *
     * @param elts  The Java array to wrap
     */
    public DPJArrayDouble(double[]<R> elts) pure {
	this.elts = elts;
	this.start = 0;
	this.length = elts.length;
    }

    private DPJArrayDouble(double[]<R> elts, int start, int length) pure {
	this.elts = elts;
	this.start = start;
	this.length = length;
    }

    /**
     * Returns the value stored at index {@code idx} of this {@code
     * DPJArrayDouble}.
     * 
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if {@code idx}
     * is outside the bounds of this {@code DPJArrayDouble} (even if it is
     * in bounds for the underlying array).
     *
     * @param idx Index of value to return
     * @param return Value stored at {@code idx}
     */
    public double get(int idx) reads R { 
	if (idx < 0 || idx > length-1) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return elts[start+idx]; 
    }

    /**
     * Replaces the value at index {@code idx} of this {@code
     * DPJArrayDouble} with value {@code val}.
     * 
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if {@code idx}
     * is outside the bounds of this {@code DPJArrayDouble} (even if it is
     * in bounds for the underlying array).
     *
     * @param idx Index of value to replace
     * @param val New value
     */
    public void put(int idx, double val) writes R {
	if (idx < 0 || idx > length-1) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	elts[start+idx] = val; 
    }

    /**
     * Creates and returns a new {@code DPJArrayDouble} starting at index
     * {@code start} with length {@code length} that wraps the same
     * underlying array as this {@code DPJArrayDouble}.  Index {@code i} of
     * the new {@code DPJArrayDouble} refers to the same cell of the
     * underlying array as index {@code start+i} of {@code this}.
     *
     * <p>Throws {@code ArrayIndexOutOfBoundsException} if the
     * interval {@code start,start+length-1]} is not in bounds for
     * this {@code DPJArrayDouble}.
     *
     * @param start  Start index for the subarray
     * @param length Length of the subarray
     * @return Subarray of this {@code DPJArrayDouble} defined by {@code
     * start} and {@code length}
     */
    public DPJArrayDouble<R> subarray(int start, int length) pure {
	if (start < 0 || length < 0 || 
	    start + length > this.length) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return new DPJArrayDouble<R>(elts, this.start + start, length);
    }

    /**
     * Returns the underlying Java array for this {@code DPJArrayDouble}.
     *
     * @return The underlying Java array
     */
    public double[]<R> toArray() pure { return elts; }

    /**
     * Returns a string representation of this {@code DPJArrayDouble}.
     *
     * @return A string representation
     */
    public String toString() reads R {
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

    /**
     * Swaps the values at indices {@code i} and {@code j} of this
     * {@code DPJArrayDouble}.
     *
     * @param i  First index to swap
     * @param j  Second index to swap
     */
    public void swap(int i, int j) writes R {
	double tmp = elts[start+i];
	elts[start+i] = elts[start+j];
	elts[start+j] = tmp;
    }

}
