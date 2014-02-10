package DPJRuntime;

import java.util.Stack;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collections;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Instrument {
    
    public Instrument() {
        super();
    }
    private static Freelist allocator = new Freelist();
    
    private static class Freelist {
        
        private Freelist() {
            super();
        }
        LinkedList<Environment> freelist = new LinkedList<Environment>();
        
        public Environment newEnv() {
            Environment result;
            if (freelist.size() > 0) {
                result = freelist.removeLast();
            } else {
                result = new Environment();
            }
            result.ID = Environment.numIDs++;
            return result;
        }
        
        public Environment newEnv(Environment oldEnv) {
            Environment result = newEnv();
            result.parallelStartTime = oldEnv.parallelStartTime + oldEnv.parallelBranchTime;
            return result;
        }
        
        public void freeEnv(Environment e) {
            e.clear();
            freelist.addLast(e);
        }
    }
    
    private static class Environment {
        private Timer timer = new Timer();
        public int ID = 0;
        public static int numIDs = 0;
        private long serialTime;
        private long pureSerialTime;
        private long parallelTime;
        private long parallelStartTime;
        private long parallelBranchTime;
        
        public Environment() {
            super();
        }
        
        public Environment(Environment prevEnv) {
            super();
            parallelStartTime = prevEnv.parallelStartTime + prevEnv.parallelTime;
        }
        
        public void clear() {
            serialTime = 0;
            parallelTime = 0;
            parallelBranchTime = 0;
        }
        
        public void startTiming() {
            timer.start();
        }
        
        public void stopTiming() {
            timer.stop();
            serialTime += timer.getElapsedTime();
            if (envStack.size() == 1) {
                pureSerialTime += timer.getElapsedTime();
            }
            parallelBranchTime += timer.getElapsedTime();
        }
    }
    
    private static class Timer {
        
        private Timer() {
            super();
        }
        private boolean on = false;
        private long startTime = 0;
        private long endTime = 0;
        private long elapsedTime = 0;
        
        public void start() {
            elapsedTime = 0;
            startTime = System.nanoTime();
        }
        
        public void stop() {
            endTime = System.nanoTime();
            long difference = endTime - startTime;
            if (difference > 0) elapsedTime = difference;
        }
        
        public long getElapsedTime() {
            return elapsedTime;
        }
    }
    private static Stack<Environment> envStack = new Stack<Environment>();
    private static long serialTime;
    private static long pureSerialTime;
    private static long parallelTime;
    private static boolean on;
    private static Map<Integer, Long> enterTimes = new HashMap<Integer, Long>();
    private static Map<Integer, Long> exitTimes = new HashMap<Integer, Long>();
    private static TreeMap<Long, Integer> tasksDeltaAtTime = new TreeMap<Long, Integer>();
    private static TreeMap<Long, Integer> numOfTasksAtTime = null;
    
    private static void addToMapEntry(Map<Long, Integer> map, Long key, Integer delta) {
        if (!map.containsKey(key)) map.put(key, 0);
        map.put(key, map.get(key) + delta);
    }
    
    private static Map<Long, Integer> partialSum(TreeMap<Long, Integer> map) {
        if (numOfTasksAtTime != null) return numOfTasksAtTime;
        Long[] taskTimes = map.navigableKeySet().toArray(new Long[]{});
        numOfTasksAtTime = new TreeMap<Long, Integer>();
        numOfTasksAtTime.put(taskTimes[0], map.get(taskTimes[0]));
        for (int i = 1; i < taskTimes.length; ++i) {
            numOfTasksAtTime.put(taskTimes[i], map.get(taskTimes[i]) + numOfTasksAtTime.get(taskTimes[i - 1]));
        }
        return numOfTasksAtTime;
    }
    
    public static void start() {
        tasksDeltaAtTime = new TreeMap<Long, Integer>();
        on = true;
        envStack.clear();
        Environment env = allocator.newEnv();
        envStack.push(env);
        addToMapEntry(tasksDeltaAtTime, 0L, 1);
        env.startTiming();
    }
    
    public static void enterForeach(int numIters) {
        if (on) {
            Environment env = envStack.peek();
            env.stopTiming();
            Environment newEnv = allocator.newEnv(env);
            addToMapEntry(tasksDeltaAtTime, newEnv.parallelStartTime, numIters);
            envStack.push(newEnv);
        }
    }
    
    public static void enterForeachIter() {
        if (on) {
            Environment env = envStack.peek();
            env.parallelBranchTime = 0;
            env.startTiming();
        }
    }
    
    public static void exitForeachIter() {
        if (on) {
            Environment env = envStack.peek();
            env.stopTiming();
            if (env.parallelBranchTime > env.parallelTime) env.parallelTime = env.parallelBranchTime;
            addToMapEntry(tasksDeltaAtTime, env.parallelStartTime + env.parallelBranchTime, -1);
        }
    }
    
    public static void exitForeach() {
        if (on) {
            Environment foreachEnv = envStack.pop();
            Environment env = envStack.peek();
            env.serialTime += foreachEnv.serialTime;
            env.parallelBranchTime += foreachEnv.parallelTime;
            allocator.freeEnv(foreachEnv);
            env.startTiming();
        }
    }
    
    public static void enterCobegin() {
        if (on) {
            Environment env = envStack.peek();
            env.stopTiming();
            env = allocator.newEnv(env);
            envStack.push(env);
            env.startTiming();
        }
    }
    
    public static void cobeginSeparator() {
        if (on) {
            Environment env = envStack.peek();
            env.stopTiming();
            if (env.parallelBranchTime > env.parallelTime) env.parallelTime = env.parallelBranchTime;
            if (env.parallelBranchTime > 0) {
                addToMapEntry(tasksDeltaAtTime, env.parallelStartTime, 1);
                addToMapEntry(tasksDeltaAtTime, env.parallelStartTime + env.parallelBranchTime, -1);
            }
            env.parallelBranchTime = 0;
            env.startTiming();
        }
    }
    
    public static void exitCobegin() {
        if (on) {
            Environment cobeginEnv = envStack.pop();
            Environment env = envStack.peek();
            env.serialTime += cobeginEnv.serialTime;
            env.parallelBranchTime += cobeginEnv.parallelTime;
            allocator.freeEnv(cobeginEnv);
            env.startTiming();
        }
    }
    
    public static void enterFinish() {
        if (on) {
            Environment env = envStack.peek();
            env.timer.stop();
            env = allocator.newEnv(env);
            envStack.push(env);
            env.timer.start();
        }
    }
    
    public static void exitFinish() {
        if (on) {
            Environment finishEnv = envStack.pop();
            finishEnv.stopTiming();
            if (finishEnv.parallelBranchTime > finishEnv.parallelTime) finishEnv.parallelTime = finishEnv.parallelBranchTime;
            Environment env = envStack.peek();
            env.serialTime += finishEnv.serialTime;
            env.parallelBranchTime += finishEnv.parallelTime;
            allocator.freeEnv(finishEnv);
            env.timer.start();
        }
    }
    
    public static void enterSpawn() {
        if (on) {
            Environment env = envStack.peek();
            env.stopTiming();
            Environment oldEnv = env;
            env = allocator.newEnv(env);
            enterTimes.put(env.ID, env.parallelStartTime);
            envStack.push(env);
            addToMapEntry(tasksDeltaAtTime, env.parallelStartTime, 1);
            env.startTiming();
        }
    }
    
    public static void exitSpawn() {
        if (on) {
            Environment spawnEnv = envStack.pop();
            spawnEnv.stopTiming();
            if (spawnEnv.parallelBranchTime > spawnEnv.parallelTime) spawnEnv.parallelTime = spawnEnv.parallelBranchTime;
            Environment env = envStack.peek();
            env.serialTime += spawnEnv.serialTime;
            long spawnTime = env.parallelBranchTime + spawnEnv.parallelTime;
            if (spawnTime > env.parallelTime) env.parallelTime = spawnTime;
            addToMapEntry(tasksDeltaAtTime, env.parallelStartTime + spawnTime, -1);
            exitTimes.put(spawnEnv.ID, env.parallelStartTime + spawnTime);
            allocator.freeEnv(spawnEnv);
            env.startTiming();
        }
    }
    
    public static void end() {
        Environment env = envStack.pop();
        env.stopTiming();
        on = false;
        parallelTime = env.parallelBranchTime;
        serialTime = env.serialTime;
        pureSerialTime = env.pureSerialTime;
        addToMapEntry(tasksDeltaAtTime, parallelTime, 0);
    }
    
    public static Map<Long, Integer> getTasksMap() {
        return Collections.unmodifiableMap(partialSum(tasksDeltaAtTime));
    }
    
    public static void printTasksMap(String filepath) throws IOException {
        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileWriter(filepath));
            Map<Long, Integer> tasksMap = getTasksMap();
            for (Long time : tasksMap.keySet()) {
                outputStream.println(time + "\t" + tasksMap.get(time));
            }
        } finally {
            if (outputStream != null) outputStream.close();
        }
    }
    
    public static void printTaskIntervals(String filepath) throws IOException {
        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileWriter(filepath));
            for (Integer taskID : enterTimes.keySet()) {
                outputStream.println(taskID + "\t" + enterTimes.get(taskID) + "\t" + exitTimes.get(taskID));
            }
        } finally {
            if (outputStream != null) outputStream.close();
        }
    }
    
    public static double averageWidth() {
        Long[] taskTimes = Instrument.getTasksMap().keySet().toArray(new Long[]{});
        long totalWidth = 0;
        for (int i = 1; i < taskTimes.length; ++i) {
            totalWidth += (taskTimes[i] - taskTimes[i - 1]) * numOfTasksAtTime.get(taskTimes[i - 1]);
        }
        return ((double)totalWidth / taskTimes[taskTimes.length - 1]);
    }
    
    public static double idealSpeedup() {
        return ((double)serialTime) / parallelTime;
    }
    
    public static long getSerialTime() {
        return serialTime;
    }
    
    public static long getParallelTime() {
        return parallelTime;
    }
    
    public static double amdahlBound() {
        return ((double)serialTime) / pureSerialTime;
    }
}
