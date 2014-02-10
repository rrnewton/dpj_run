package DPJRuntime;

import jsr166y.forkjoin.ForkJoinPool;

/**
 * This class stores the program state maintained by the DPJ runtime.
 *
 * @author Patrick Simmons
 * @author Rob Bocchino
 */
public class RuntimeState {

    /**
     * Global region for RuntimeState
     */
    public region Global;
       
    /**
     * Flag indicating whether we are inside a task forked by a DPJ
     * {@code cobegin} or {@code foreach} construct.  This is
     * necessary because the {@code ForkJoinTask} framework requires
     * that we handle the outermost invocation of a {@code cobegin} or
     * {@code foreach} differently from one nested inside another.
     *
     * <p>This variable is set only by the DPJ compiler and should
     * never be set by user code.
     */
    public static boolean insideParallelTask in Global = false; 

    /**
     * The {@code ForkJoinPool} that the runtime uses to launch {@code
     * ForkJoinTask}s.
     *
     * <p>This variable is set only by the DPJ compiler and should
     * never be set by user code.
     */
    public static ForkJoinPool pool in Global;

    /**
     * The minimum number of {@code foreach} iterations to be
     * allocated to a single task.  Beyond this point, no more
     * parallel splitting of a {@code foreach} loop occurs.  The
     * default is 128.
     *
     * This variable may be set to value <i>n</i> at the start of
     * program execution by passing {@code
     * --dpj-foreach-cutoff=}<i>n</i> as a command-line argument to
     * the DPJ program.  All DPJ command-line arguments must come
     * first.  This variable may also be set directly in the DPJ
     * program, if a different cutoff is desired for different {@code
     * foreach} loops.
     */
    public static int dpjForeachCutoff in Global = 128;

    /**
     * The number of ways to split a {@code foreach} loop.  The loop
     * is recursively split into this many branches, until the {@code
     * dpjForeachCutoff} is reached.  The default is 2.
     *
     * <p>This variable may be set to value <i>n</i> at the start of
     * program execution by passing {@code
     * --dpj-foreach-split=}<i>n</i> as a command-line argument to the
     * DPJ program.  All DPJ command-line arguments must come first.
     * This variable may also be set directly in the DPJ program, if a
     * different splitting factor is desired for different {@code
     * foreach} loops.
     */
     public static int dpjForeachSplit in Global = 2; 

    /**
     * The number of worker threads.  The default is the number of
     * available processors.
     *
     * <p>This variable may be set to value <i>n</i> at the start of
     * program execution by passing {@code
     * --dpj-foreach-split=}<i>n</i> as a command-line argument to the
     * DPJ program.  Thereafter it may not be changed.
     */
    public static int dpjNumThreads in Global =
        Runtime.getRuntime().availableProcessors();


    // Private helper method
    private static void error(String msg) {
	System.err.println(msg);
	System.exit(1);
    }

    private static void checkIdx(String flag, int idx, int length) {
	if (idx >= length - 1) {
	    error("Missing argument to " + flag);
	}
    }

    /**
     * Processes command-line arguments and initializes the runtime
     * parameters.  The command-line options are as follows:
     *
     * <p><blockquote> {@code --dpj-foreach-split=}<i>n</i>: Set
     * {@link dpjForeachSplit} to <i>n</i>.  <br>{@code
     * --dpj-foreach-cutoff=}<i>n</i>: Set {@link dpjForeachCutoff} to
     * <i>n</i>.  <br>{@code --dpj-num-threads=}<i>n</i>: Set {@link
     * dpjNumThreads} to <i>n</i>.  </blockquote>
     *
     * <p>The DPJ options may appear in any order, but they must
     * precede any command-line arguments to the program.  The rest of
     * the arguments are passed to the DPJ program to be processed by
     * it.
     */
    public static String[] initialize(String[] args) {
	//
	// Process the DPJ arguments, which must come first
	//
	int idx = 0;
	for ( ; idx < args.length; ++idx) {
	    if(args[idx].equals("--dpj-foreach-split")) {
		checkIdx("--dpj-foreach-split", idx, args.length);
		dpjForeachSplit = Integer.parseInt(args[++idx]);
		if (dpjForeachSplit < 1) {
		    error("DPJ foreach split must be greater than 0; " +
			  dpjForeachSplit + " is not valid");
		}
	    } else if (args[idx].equals("--dpj-foreach-cutoff")) {
		checkIdx("--dpj-foreach-cutoff", idx, args.length);
		dpjForeachCutoff = Integer.parseInt(args[++idx]);
	    } else if(args[idx].equals("--dpj-num-threads")) {
		checkIdx("--dpj-num-threads", idx, args.length);
		dpjNumThreads = Integer.parseInt(args[++idx]);
		if (dpjNumThreads < 1) {
		    error("DPJ num threads must be greater than 0; " +
			  dpjNumThreads + "is not valid");
		}
	    } else {
		break;
	    }
	}
	
	if(dpjForeachCutoff < dpjForeachSplit - 1) {
	    error("DPJ foreach cutoff must be greater than or equal to DPJ foreach split\n" +
		  "DPJ foreach cutoff="+dpjForeachCutoff+
		  "\nDPJ foreach split="+dpjForeachSplit);
	}
	
	//
	// Return the rest of the arguments to the program
	//
	String[] newArgs = new String[args.length - idx];
	System.arraycopy(args,idx,newArgs,0,newArgs.length);
	return newArgs;
    }
}
