package DPJRuntime;

public class DPJUtils {
    
    public DPJUtils() {
        super();
    }
    
    public static int log2(int x) {
        if (x <= 0) throw new ArithmeticException();
        int result = 0;
        while (x > 1) {
            x >>= 1;
            ++result;
        }
        return result;
    }
    
    public static <T>void swap(T[] A, int i, int j) {
        T tmp = A[j];
        A[j] = A[i];
        A[i] = tmp;
    }
    
    public static <T>void permute(T[] A) {
        for (int i = 0; i < A.length; ++i) {
            int j = (int)(Math.random() * A.length);
            int k = (int)(Math.random() * A.length);
            DPJUtils.swap(A, j, k);
        }
    }
    
    public static void permuteInt(int[] A) {
        for (int i = 0; i < A.length; ++i) {
            int j = (int)(Math.random() * A.length);
            int k = (int)(Math.random() * A.length);
            int tmp = A[j];
            A[j] = A[i];
            A[i] = tmp;
        }
    }
}
