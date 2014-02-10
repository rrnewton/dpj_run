package DPJRuntime;

import jsr166y.forkjoin.ForkJoinPool;

public class RuntimeState {
    
    public RuntimeState() {
        super();
    }
    public static boolean insideParallelTask = false;
    public static ForkJoinPool pool;
    public static int dpjForeachCutoff = 128;
    public static int dpjForeachSplit = 2;
    public static int dpjNumThreads = Runtime.getRuntime().availableProcessors();
    
    private static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
    
    private static void checkIdx(String flag, int idx, int length) {
        if (idx >= length - 1) {
            error("Missing argument to " + flag);
        }
    }
    
    public static String[] initialize(String[] args) {
        int idx = 0;
        for (; idx < args.length; ++idx) {
            if (args[idx].equals("--dpj-foreach-split")) {
                checkIdx("--dpj-foreach-split", idx, args.length);
                dpjForeachSplit = Integer.parseInt(args[++idx]);
                if (dpjForeachSplit < 1) {
                    error("DPJ foreach split must be greater than 0; " + dpjForeachSplit + " is not valid");
                }
            } else if (args[idx].equals("--dpj-foreach-cutoff")) {
                checkIdx("--dpj-foreach-cutoff", idx, args.length);
                dpjForeachCutoff = Integer.parseInt(args[++idx]);
            } else if (args[idx].equals("--dpj-num-threads")) {
                checkIdx("--dpj-num-threads", idx, args.length);
                dpjNumThreads = Integer.parseInt(args[++idx]);
                if (dpjNumThreads < 1) {
                    error("DPJ num threads must be greater than 0; " + dpjNumThreads + "is not valid");
                }
            } else {
                break;
            }
        }
        if (dpjForeachCutoff < dpjForeachSplit - 1) {
            error("DPJ foreach cutoff must be greater than or equal to DPJ foreach split\n" + "DPJ foreach cutoff=" + dpjForeachCutoff + "\nDPJ foreach split=" + dpjForeachSplit);
        }
        String[] newArgs = new String[args.length - idx];
        System.arraycopy(args, idx, newArgs, 0, newArgs.length);
        return newArgs;
    }
}
