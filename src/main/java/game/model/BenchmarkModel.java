package game.model;

import javax.swing.*;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BenchmarkModel {

    public static class BenchmarkResult {
        private final String dataStructureName;
        private final String operationName;
        private final int elementCount;
        private final long timeNanos;

        public BenchmarkResult(String dataStructureName, String operationName,
                               int elementCount, long timeNanos) {
            this.dataStructureName = dataStructureName;
            this.operationName = operationName;
            this.elementCount = elementCount;
            this.timeNanos = timeNanos;
        }

        public String getDataStructureName() { return dataStructureName; }
        public String getOperationName() { return operationName; }
        public int getElementCount() { return elementCount; }
        public long getTimeNanos() { return timeNanos; }
        public double getTimeMs() { return timeNanos / 1_000_000.0; }
    }

    public enum Operation {
        ADD("ADD"),
        GET("GET / CONTAINS"),
        REMOVE("REMOVE");

        private final String displayName;
        Operation(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public static final LinkedHashMap<String, Color> DS_COLORS = new LinkedHashMap<>();
    static {
        DS_COLORS.put("ArrayList",         new Color(0, 200, 255));
        DS_COLORS.put("LinkedList",        new Color(255, 100, 150));
        DS_COLORS.put("ArrayDeque",        new Color(180, 100, 255));
        DS_COLORS.put("PriorityQueue",     new Color(255, 100, 100));
        DS_COLORS.put("HashSet",           new Color(255, 150, 80));
        DS_COLORS.put("LinkedHashSet",     new Color(255, 120, 120));
        DS_COLORS.put("TreeSet",           new Color(80, 200, 200));
        DS_COLORS.put("HashMap",           new Color(255, 200, 80));
        DS_COLORS.put("LinkedHashMap",     new Color(255, 180, 130));
        DS_COLORS.put("TreeMap",           new Color(100, 220, 180));
        DS_COLORS.put("Hashtable",         new Color(200, 120, 80));
        DS_COLORS.put("ConcurrentHashMap", new Color(100, 150, 220));
    }

    private List<BenchmarkResult> results = new ArrayList<>();
    private volatile boolean running = false;
    private SwingWorker<Void, Double> currentWorker;

    public List<BenchmarkResult> getResults() { return Collections.unmodifiableList(results); }
    public boolean isRunning() { return running; }

    public void cancel() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
            running = false;
        }
    }

    public void runBenchmarks(int elementCount, Set<Operation> operations,
                              Consumer<Double> progressCallback, Runnable onComplete) {
        if (running) return;

        currentWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                running = true;
                List<BenchmarkResult> newResults = new ArrayList<>();
                List<String> dsNames = new ArrayList<>(DS_COLORS.keySet());
                int totalTasks = dsNames.size() * operations.size();
                int completed = 0;

                Random random = new Random(42);

                for (String dsName : dsNames) {
                    if (isCancelled()) break;

                    for (Operation op : Operation.values()) {
                        if (isCancelled()) break;
                        if (!operations.contains(op)) continue;

                        // Warmup 2x at 1/10 size
                        int warmupSize = Math.max(100, elementCount / 10);
                        for (int w = 0; w < 2; w++) {
                            runSingleBenchmark(dsName, op, warmupSize, new Random(random.nextLong()));
                        }

                        // GC before measurement
                        System.gc();
                        try { Thread.sleep(50); } catch (InterruptedException e) { return null; }

                        // Actual measurement
                        long nanos = runSingleBenchmark(dsName, op, elementCount, new Random(42));
                        newResults.add(new BenchmarkResult(dsName, op.getDisplayName(), elementCount, nanos));

                        completed++;
                        double progress = (double) completed / totalTasks;
                        SwingUtilities.invokeLater(() -> progressCallback.accept(progress));
                    }
                }

                results = newResults;
                running = false;
                return null;
            }

            @Override
            protected void done() {
                running = false;
                if (!isCancelled()) {
                    SwingUtilities.invokeLater(onComplete);
                }
            }
        };

        currentWorker.execute();
    }

    private long runSingleBenchmark(String dsName, Operation op, int count, Random random) {
        boolean isMap = dsName.contains("Map") || dsName.equals("Hashtable");

        switch (op) {
            case ADD:    return isMap ? benchmarkMapAdd(dsName, count, random) : benchmarkCollectionAdd(dsName, count, random);
            case GET:    return isMap ? benchmarkMapGet(dsName, count, random) : benchmarkCollectionGet(dsName, count, random);
            case REMOVE: return isMap ? benchmarkMapRemove(dsName, count, random) : benchmarkCollectionRemove(dsName, count, random);
            default:     return 0;
        }
    }

    // ── Collection benchmarks (List, Set, Queue, Deque) ──────────

    private long benchmarkCollectionAdd(String dsName, int count, Random random) {
        int[] values = randomInts(count, random);
        Collection<Integer> coll = createCollection(dsName);
        long start = System.nanoTime();
        for (int v : values) coll.add(v);
        return System.nanoTime() - start;
    }

    private long benchmarkCollectionGet(String dsName, int count, Random random) {
        Collection<Integer> coll = createCollection(dsName);
        int[] data = randomInts(count, random);
        for (int v : data) coll.add(v);

        int[] lookups = randomInts(count, new Random(99));

        if (coll instanceof List) {
            List<Integer> list = (List<Integer>) coll;
            int size = list.size();
            long start = System.nanoTime();
            for (int i = 0; i < count; i++) {
                list.get(Math.abs(lookups[i]) % size);
            }
            return System.nanoTime() - start;
        } else {
            long start = System.nanoTime();
            for (int v : lookups) coll.contains(v);
            return System.nanoTime() - start;
        }
    }

    private long benchmarkCollectionRemove(String dsName, int count, Random random) {
        Collection<Integer> coll = createCollection(dsName);
        int[] data = randomInts(count, random);
        for (int v : data) coll.add(v);

        if (coll instanceof Deque) {
            Deque<Integer> deque = (Deque<Integer>) coll;
            long start = System.nanoTime();
            while (!deque.isEmpty()) deque.pollFirst();
            return System.nanoTime() - start;
        } else if (coll instanceof Queue) {
            Queue<Integer> queue = (Queue<Integer>) coll;
            long start = System.nanoTime();
            while (!queue.isEmpty()) queue.poll();
            return System.nanoTime() - start;
        } else if (coll instanceof List) {
            List<Integer> list = (List<Integer>) coll;
            long start = System.nanoTime();
            while (!list.isEmpty()) list.remove(list.size() - 1);
            return System.nanoTime() - start;
        } else {
            long start = System.nanoTime();
            Iterator<Integer> it = coll.iterator();
            while (it.hasNext()) { it.next(); it.remove(); }
            return System.nanoTime() - start;
        }
    }

    // ── Map benchmarks ───────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private long benchmarkMapAdd(String dsName, int count, Random random) {
        int[] keys = randomInts(count, random);
        int[] vals = randomInts(count, random);
        Map<Integer, Integer> map = (Map<Integer, Integer>) createMap(dsName);
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) map.put(keys[i], vals[i]);
        return System.nanoTime() - start;
    }

    @SuppressWarnings("unchecked")
    private long benchmarkMapGet(String dsName, int count, Random random) {
        Map<Integer, Integer> map = (Map<Integer, Integer>) createMap(dsName);
        int[] keys = randomInts(count, random);
        for (int i = 0; i < count; i++) map.put(keys[i], i);

        int[] lookups = randomInts(count, new Random(99));
        long start = System.nanoTime();
        for (int v : lookups) map.get(v);
        return System.nanoTime() - start;
    }

    @SuppressWarnings("unchecked")
    private long benchmarkMapRemove(String dsName, int count, Random random) {
        Map<Integer, Integer> map = (Map<Integer, Integer>) createMap(dsName);
        int[] keys = randomInts(count, random);
        for (int i = 0; i < count; i++) map.put(keys[i], i);

        long start = System.nanoTime();
        for (int k : keys) map.remove(k);
        return System.nanoTime() - start;
    }

    // ── Factories ────────────────────────────────────────────────

    private Collection<Integer> createCollection(String name) {
        switch (name) {
            case "ArrayList":     return new java.util.ArrayList<>();
            case "LinkedList":    return new java.util.LinkedList<>();
            case "ArrayDeque":    return new java.util.ArrayDeque<>();
            case "PriorityQueue": return new java.util.PriorityQueue<>();
            case "HashSet":       return new java.util.HashSet<>();
            case "LinkedHashSet": return new java.util.LinkedHashSet<>();
            case "TreeSet":       return new java.util.TreeSet<>();
            default: throw new IllegalArgumentException("Not a collection: " + name);
        }
    }

    private Map<?, ?> createMap(String name) {
        switch (name) {
            case "HashMap":           return new java.util.HashMap<>();
            case "LinkedHashMap":     return new java.util.LinkedHashMap<>();
            case "TreeMap":           return new java.util.TreeMap<>();
            case "Hashtable":         return new java.util.Hashtable<>();
            case "ConcurrentHashMap": return new ConcurrentHashMap<>();
            default: throw new IllegalArgumentException("Not a map: " + name);
        }
    }

    private int[] randomInts(int count, Random random) {
        int[] arr = new int[count];
        for (int i = 0; i < count; i++) arr[i] = random.nextInt(count * 2);
        return arr;
    }
}
