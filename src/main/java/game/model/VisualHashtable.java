package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualHashtable {
    private List<VisualBucket> buckets;
    private int capacity = 11; // Hashtable default is 11
    private int size = 0;
    private float loadFactor = 0.75f;
    private int operationsCount = 0;
    private String lastOperation = "";
    private int rehashCount = 0;
    private List<VisualEntry> insertionOrder = new ArrayList<>();

    private static final String[] SAMPLE_KEYS = {"name", "age", "city", "country", "email", "phone", "id", "type"};
    private static final String[] SAMPLE_VALUES = {"Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry"};

    public VisualHashtable() {
        buckets = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            buckets.add(new VisualBucket(i));
        }
        updateBucketPositions();
    }

    public synchronized Object put(Object key, Object value, String keyType, String valueType) {
        if (key == null || value == null) {
            lastOperation = "put() -> null not allowed";
            return null;
        }

        int hash = Math.abs(key.hashCode()) % capacity;
        VisualBucket bucket = buckets.get(hash);

        // Check for existing key
        for (VisualEntry entry : bucket.getEntries()) {
            if (entry.getKey().equals(key)) {
                Object oldValue = entry.getValue();
                entry.setValue(value);
                operationsCount++;
                lastOperation = "put(" + key + ", " + value + ") replaced " + oldValue;
                return oldValue;
            }
        }

        // Add new entry
        VisualEntry entry = new VisualEntry(key, value, keyType, valueType);
        bucket.addEntry(entry);
        insertionOrder.add(entry);
        size++;
        operationsCount++;
        lastOperation = "put(" + key + ", " + value + ") hash=" + hash;

        if ((float) size / capacity > loadFactor) {
            rehash();
        }

        updateBucketPositions();
        return null;
    }

    public synchronized Object get(Object key) {
        if (key == null) {
            lastOperation = "get(null) -> null not allowed";
            return null;
        }

        int hash = Math.abs(key.hashCode()) % capacity;
        VisualBucket bucket = buckets.get(hash);

        bucket.setHighlighted(true);
        operationsCount++;

        for (VisualEntry entry : bucket.getEntries()) {
            if (entry.getKey().equals(key)) {
                entry.setHighlighted(true);
                lastOperation = "get(" + key + ") -> " + entry.getValue() + " [bucket " + hash + "]";
                return entry.getValue();
            }
        }

        lastOperation = "get(" + key + ") -> null [bucket " + hash + "]";
        return null;
    }

    public synchronized Object remove(Object key) {
        if (key == null) {
            lastOperation = "remove(null) -> null not allowed";
            return null;
        }

        int hash = Math.abs(key.hashCode()) % capacity;
        VisualBucket bucket = buckets.get(hash);

        operationsCount++;

        for (VisualEntry entry : bucket.getEntries()) {
            if (entry.getKey().equals(key)) {
                Object value = entry.getValue();
                entry.startRemoving();
                bucket.removeEntry(entry);
                insertionOrder.remove(entry);
                size--;
                lastOperation = "remove(" + key + ") -> " + value;
                updateBucketPositions();
                return value;
            }
        }

        lastOperation = "remove(" + key + ") -> null";
        return null;
    }

    public synchronized boolean containsKey(Object key) {
        if (key == null) {
            lastOperation = "containsKey(null) -> false";
            return false;
        }

        int hash = Math.abs(key.hashCode()) % capacity;
        VisualBucket bucket = buckets.get(hash);
        bucket.setHighlighted(true);
        operationsCount++;

        for (VisualEntry entry : bucket.getEntries()) {
            if (entry.getKey().equals(key)) {
                entry.setHighlighted(true);
                lastOperation = "containsKey(" + key + ") -> true";
                return true;
            }
        }

        lastOperation = "containsKey(" + key + ") -> false";
        return false;
    }

    public synchronized boolean containsValue(Object value) {
        operationsCount++;
        for (VisualBucket bucket : buckets) {
            for (VisualEntry entry : bucket.getEntries()) {
                if (entry.getValue().equals(value)) {
                    entry.setHighlighted(true);
                    lastOperation = "containsValue(" + value + ") -> true";
                    return true;
                }
            }
        }
        lastOperation = "containsValue(" + value + ") -> false";
        return false;
    }

    public synchronized void clear() {
        for (VisualBucket bucket : buckets) {
            for (VisualEntry entry : bucket.getEntries()) {
                entry.startRemoving();
            }
            bucket.clear();
        }
        insertionOrder.clear();
        size = 0;
        operationsCount++;
        lastOperation = "clear()";
    }

    private void rehash() {
        int oldCapacity = capacity;
        capacity = capacity * 2 + 1; // Hashtable grows by 2n+1
        rehashCount++;

        List<VisualBucket> oldBuckets = buckets;
        buckets = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            buckets.add(new VisualBucket(i));
        }

        for (VisualBucket oldBucket : oldBuckets) {
            for (VisualEntry entry : oldBucket.getEntries()) {
                int hash = Math.abs(entry.getKey().hashCode()) % capacity;
                buckets.get(hash).addEntry(entry);
            }
        }

        lastOperation += " [rehashed " + oldCapacity + " -> " + capacity + "]";
        updateBucketPositions();
    }

    private void updateBucketPositions() {
        int bucketsPerRow = 8;
        int bucketWidth = 80;
        int bucketHeight = 40;
        int startX = 50;
        int startY = 100;
        int gapX = 10;
        int gapY = 120;

        for (int i = 0; i < buckets.size(); i++) {
            int row = i / bucketsPerRow;
            int col = i % bucketsPerRow;
            double x = startX + col * (bucketWidth + gapX);
            double y = startY + row * gapY;
            buckets.get(i).setTarget(x, y);
        }
    }

    public synchronized void update(double deltaTime) {
        for (VisualBucket bucket : buckets) {
            bucket.update(deltaTime);
            bucket.setHighlighted(false);
        }
    }

    public int getSize() { return size; }
    public int getCapacity() { return capacity; }
    public float getLoadFactor() { return loadFactor; }
    public float getCurrentLoad() { return (float) size / capacity; }
    public int getOperationsCount() { return operationsCount; }
    public int getRehashCount() { return rehashCount; }
    public String getLastOperation() { return lastOperation; }
    public List<VisualBucket> getBuckets() { return buckets; }
    public List<VisualEntry> getInsertionOrder() { return insertionOrder; }

    public int getMaxChainLength() {
        int max = 0;
        for (VisualBucket bucket : buckets) {
            max = Math.max(max, bucket.getEntries().size());
        }
        return max;
    }

    public MemoryInfo getMemoryInfo() {
        long hashtableOverhead = 48; // Hashtable object + sync overhead
        long arrayOverhead = 16 + capacity * 4;
        long entriesOverhead = (long) size * 36; // Entry with sync
        long keysMemory = 0;
        long valuesMemory = 0;

        for (VisualBucket bucket : buckets) {
            for (VisualEntry entry : bucket.getEntries()) {
                keysMemory += estimateObjectSize(entry.getKey(), entry.getKeyType());
                valuesMemory += estimateObjectSize(entry.getValue(), entry.getValueType());
            }
        }

        long totalUsed = hashtableOverhead + arrayOverhead + entriesOverhead + keysMemory + valuesMemory;

        return new MemoryInfo(totalUsed, hashtableOverhead, arrayOverhead, entriesOverhead, keysMemory, valuesMemory, capacity, size);
    }

    private long estimateObjectSize(Object value, String type) {
        if (type == null) return 0;
        return switch (type.toLowerCase()) {
            case "int", "integer" -> 16;
            case "double" -> 24;
            case "string" -> {
                String s = (String) value;
                yield 24 + 16 + ((s.length() + 7) / 8) * 8;
            }
            default -> 24;
        };
    }

    public record MemoryInfo(long totalUsed, long hashtableOverhead, long arrayOverhead, long entriesOverhead,
                             long keysMemory, long valuesMemory, int capacity, int size) {
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
            case "string" -> SAMPLE_KEYS[random.nextInt(SAMPLE_KEYS.length)] + random.nextInt(100);
            default -> "key" + random.nextInt(100);
        };
    }

    public static Object generateRandomValue(String type, Random random) {
        return switch (type.toLowerCase()) {
            case "int" -> random.nextInt(1000);
            case "double" -> Math.round(random.nextDouble() * 1000) / 10.0;
            case "string" -> SAMPLE_VALUES[random.nextInt(SAMPLE_VALUES.length)];
            default -> "val" + random.nextInt(100);
        };
    }
}
