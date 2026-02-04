package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualConcurrentHashMap {
    private List<VisualEntry>[] buckets;
    private int capacity = 16;
    private int size = 0;
    private double loadFactor = 0.75;
    private int concurrencyLevel = 4;
    private int operationsCount = 0;
    private int rehashCount = 0;
    private boolean justRehashed = false;
    private long lastRehashTime = 0;
    private String lastOperation = "";
    private int lastBucketAccessed = -1;
    private int lockedSegment = -1;
    private long lockTime = 0;

    @SuppressWarnings("unchecked")
    public VisualConcurrentHashMap() {
        buckets = new ArrayList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new ArrayList<>();
        }
    }

    public synchronized Object put(Object key, Object value, String keyType, String valueType) {
        if (key == null || value == null) {
            lastOperation = "PUT: NullPointerException - null keys/values not allowed";
            operationsCount++;
            return null;
        }

        if ((double)(size + 1) / capacity > loadFactor) {
            rehash();
        }

        int hash = key.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;
        lastBucketAccessed = bucketIndex;
        lockedSegment = getSegmentForBucket(bucketIndex);
        lockTime = System.currentTimeMillis();

        for (VisualEntry entry : buckets[bucketIndex]) {
            if (!entry.isRemoving() && entry.getKey().equals(key)) {
                Object oldValue = entry.getValue();
                entry.setValue(value);
                operationsCount++;
                lastOperation = "PUT: " + key + " -> " + value + " (updated, segment " + lockedSegment + ")";
                return oldValue;
            }
        }

        VisualEntry entry = new VisualEntry(key, value, keyType, valueType);
        entry.setPosition(400, -50);
        entry.setBucketIndex(bucketIndex);
        entry.setChainIndex(buckets[bucketIndex].size());
        buckets[bucketIndex].add(entry);
        size++;
        operationsCount++;
        updateBucketPositions(bucketIndex);
        lastOperation = "PUT: " + key + " -> " + value + " (bucket[" + bucketIndex + "], segment " + lockedSegment + ")";

        return null;
    }

    public synchronized Object get(Object key) {
        if (key == null) {
            lastOperation = "GET: NullPointerException - null key not allowed";
            operationsCount++;
            return null;
        }

        int hash = key.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;
        lastBucketAccessed = bucketIndex;
        lockedSegment = getSegmentForBucket(bucketIndex);
        lockTime = System.currentTimeMillis();
        operationsCount++;

        for (VisualEntry entry : buckets[bucketIndex]) {
            if (!entry.isRemoving() && entry.getKey().equals(key)) {
                lastOperation = "GET: " + key + " -> " + entry.getValue() + " (segment " + lockedSegment + ")";
                return entry.getValue();
            }
        }
        lastOperation = "GET: " + key + " -> null (not found)";
        return null;
    }

    public synchronized Object remove(Object key) {
        if (key == null) {
            lastOperation = "REMOVE: NullPointerException - null key not allowed";
            operationsCount++;
            return null;
        }

        int hash = key.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;
        lastBucketAccessed = bucketIndex;
        lockedSegment = getSegmentForBucket(bucketIndex);
        lockTime = System.currentTimeMillis();

        for (VisualEntry entry : buckets[bucketIndex]) {
            if (!entry.isRemoving() && entry.getKey().equals(key)) {
                entry.startRemoving();
                size--;
                operationsCount++;
                lastOperation = "REMOVE: " + key + " (segment " + lockedSegment + ")";
                return entry.getValue();
            }
        }
        lastOperation = "REMOVE: " + key + " (not found)";
        return null;
    }

    public synchronized boolean containsKey(Object key) {
        if (key == null) {
            lastOperation = "CONTAINS KEY: NullPointerException - null key not allowed";
            operationsCount++;
            return false;
        }

        int hash = key.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;
        lastBucketAccessed = bucketIndex;
        lockedSegment = getSegmentForBucket(bucketIndex);
        lockTime = System.currentTimeMillis();
        operationsCount++;

        for (VisualEntry entry : buckets[bucketIndex]) {
            if (!entry.isRemoving() && entry.getKey().equals(key)) {
                lastOperation = "CONTAINS KEY: " + key + " -> true (segment " + lockedSegment + ")";
                return true;
            }
        }
        lastOperation = "CONTAINS KEY: " + key + " -> false";
        return false;
    }

    public synchronized void clear() {
        for (List<VisualEntry> bucket : buckets) {
            for (VisualEntry entry : bucket) {
                entry.startRemoving();
            }
        }
        size = 0;
        operationsCount++;
        lastOperation = "CLEAR: removed all entries";
    }

    public synchronized void update(double deltaTime) {
        for (List<VisualEntry> bucket : buckets) {
            bucket.removeIf(VisualEntry::isFullyRemoved);
            for (VisualEntry entry : bucket) {
                entry.update(deltaTime);
            }
        }

        if (justRehashed && System.currentTimeMillis() - lastRehashTime > 1500) {
            justRehashed = false;
        }

        if (lockedSegment >= 0 && System.currentTimeMillis() - lockTime > 500) {
            lockedSegment = -1;
        }
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        int oldCapacity = capacity;
        capacity = capacity * 2;
        List<VisualEntry>[] oldBuckets = buckets;
        buckets = new ArrayList[capacity];

        for (int i = 0; i < capacity; i++) {
            buckets[i] = new ArrayList<>();
        }

        size = 0;
        for (List<VisualEntry> oldBucket : oldBuckets) {
            for (VisualEntry entry : oldBucket) {
                if (!entry.isRemoving()) {
                    int hash = entry.getKey().hashCode();
                    int newIndex = (hash & 0x7FFFFFFF) % capacity;
                    VisualEntry newEntry = new VisualEntry(entry.getKey(), entry.getValue(),
                            entry.getKeyType(), entry.getValueType());
                    newEntry.setPosition(entry.getX(), entry.getY());
                    newEntry.setBucketIndex(newIndex);
                    newEntry.setChainIndex(buckets[newIndex].size());
                    buckets[newIndex].add(newEntry);
                    size++;
                }
            }
        }

        for (int i = 0; i < capacity; i++) {
            updateBucketPositions(i);
        }

        rehashCount++;
        justRehashed = true;
        lastRehashTime = System.currentTimeMillis();
        lastOperation = "REHASH: " + oldCapacity + " -> " + capacity;
    }

    private void updateBucketPositions(int bucketIndex) {
        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;

        int col = bucketIndex % cols;
        int row = bucketIndex / cols;
        double bucketX = startX + col * cellWidth + cellWidth / 2.0;
        double bucketY = startY + row * 200;

        List<VisualEntry> chain = buckets[bucketIndex];
        double chainY = bucketY + 70;
        int visibleIndex = 0;
        for (VisualEntry entry : chain) {
            if (!entry.isRemoving()) {
                entry.setTarget(bucketX, chainY + visibleIndex * 65);
                entry.setChainIndex(visibleIndex);
                visibleIndex++;
            }
        }
    }

    private int getSegmentForBucket(int bucketIndex) {
        int bucketsPerSegment = capacity / concurrencyLevel;
        if (bucketsPerSegment == 0) bucketsPerSegment = 1;
        return Math.min(bucketIndex / bucketsPerSegment, concurrencyLevel - 1);
    }

    // ── Getters ───────────────────────────────────────────────────

    public int getSize() { return size; }
    public int getCapacity() { return capacity; }
    public double getLoadFactor() { return loadFactor; }
    public double getCurrentLoad() { return (double) size / capacity; }
    public int getOperationsCount() { return operationsCount; }
    public int getRehashCount() { return rehashCount; }
    public boolean wasJustRehashed() { return justRehashed; }
    public String getLastOperation() { return lastOperation; }
    public int getLastBucketAccessed() { return lastBucketAccessed; }
    public List<VisualEntry>[] getBuckets() { return buckets; }
    public int getConcurrencyLevel() { return concurrencyLevel; }
    public int getLockedSegment() { return lockedSegment; }

    public int getMaxChainLength() {
        int max = 0;
        for (List<VisualEntry> bucket : buckets) {
            int count = 0;
            for (VisualEntry e : bucket) if (!e.isRemoving()) count++;
            max = Math.max(max, count);
        }
        return max;
    }

    public int getCollisionCount() {
        int collisions = 0;
        for (List<VisualEntry> bucket : buckets) {
            int active = 0;
            for (VisualEntry e : bucket) {
                if (!e.isRemoving()) active++;
            }
            if (active > 1) collisions += active - 1;
        }
        return collisions;
    }

    public MemoryInfo getMemoryInfo() {
        long mapOverhead = 48;
        long segmentsOverhead = 16 + (long) concurrencyLevel * 36;
        long tableOverhead = 16 + (long) capacity * 4;
        long nodesOverhead = (long) size * 32;

        long keysMemory = 0;
        long valuesMemory = 0;
        for (List<VisualEntry> bucket : buckets) {
            for (VisualEntry entry : bucket) {
                if (!entry.isRemoving()) {
                    keysMemory += estimateObjectSize(entry.getKey(), entry.getKeyType());
                    valuesMemory += estimateObjectSize(entry.getValue(), entry.getValueType());
                }
            }
        }

        long totalUsed = mapOverhead + segmentsOverhead + tableOverhead + nodesOverhead + keysMemory + valuesMemory;

        return new MemoryInfo(totalUsed, mapOverhead, segmentsOverhead, tableOverhead, nodesOverhead, keysMemory, valuesMemory);
    }

    private long estimateObjectSize(Object value, String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> 16;
            case "double" -> 24;
            case "boolean" -> 16;
            case "char", "character" -> 16;
            case "string" -> {
                String s = (String) value;
                int bytesLength = s.length();
                int arraySize = 16 + ((bytesLength + 7) / 8) * 8;
                yield 24 + arraySize;
            }
            default -> 24;
        };
    }

    public record MemoryInfo(
            long totalUsed,
            long mapOverhead,
            long segmentsOverhead,
            long tableOverhead,
            long nodesOverhead,
            long keysMemory,
            long valuesMemory
    ) {
        public String formatTotal() { return formatBytes(totalUsed); }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }

    public static Object generateRandomKey(String type, Random random) {
        return switch (type.toLowerCase()) {
            case "int" -> random.nextInt(1000);
            case "string" -> {
                String[] keys = {"name", "age", "city", "code", "id", "type", "status", "level"};
                yield keys[random.nextInt(keys.length)] + random.nextInt(10);
            }
            default -> "key" + random.nextInt(100);
        };
    }

    public static Object generateRandomValue(String type, Random random) {
        return switch (type.toLowerCase()) {
            case "int" -> random.nextInt(1000);
            case "double" -> Math.round(random.nextDouble() * 100 * 100.0) / 100.0;
            case "string" -> {
                String[] values = {"Alice", "Bob", "Charlie", "Data", "Echo", "Fox"};
                yield values[random.nextInt(values.length)];
            }
            case "boolean" -> random.nextBoolean();
            default -> "val" + random.nextInt(100);
        };
    }
}
