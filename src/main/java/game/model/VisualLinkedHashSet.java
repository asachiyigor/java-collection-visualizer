package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualLinkedHashSet {
    private VisualBucket[] buckets;
    private List<VisualElement> insertionOrder = new ArrayList<>();
    private int capacity = 16;
    private int size = 0;
    private double loadFactor = 0.75;
    private int operationsCount = 0;
    private int rehashCount = 0;
    private boolean justRehashed = false;
    private long lastRehashTime = 0;
    private String lastOperation = "";
    private int lastBucketAccessed = -1;

    public VisualLinkedHashSet() {
        buckets = new VisualBucket[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new VisualBucket(i);
        }
        updateBucketPositions();
    }

    public synchronized boolean add(Object value, String type) {
        if (contains(value)) {
            lastOperation = "DUPLICATE: " + value + " already exists";
            return false;
        }

        if ((double)(size + 1) / capacity > loadFactor) {
            rehash();
        }

        int hash = value.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;

        VisualElement element = new VisualElement(value, type);
        element.setPosition(400, -50);
        buckets[bucketIndex].addElement(element);
        buckets[bucketIndex].highlight();
        insertionOrder.add(element);
        lastBucketAccessed = bucketIndex;

        size++;
        operationsCount++;
        lastOperation = "ADD: " + value + " -> bucket[" + bucketIndex + "] (order: " + insertionOrder.size() + ")";

        return true;
    }

    public synchronized boolean remove(Object value) {
        int hash = value.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;

        for (VisualElement elem : buckets[bucketIndex].getChain()) {
            if (!elem.isRemoving() && elem.getValue().equals(value)) {
                elem.startRemoving();
                insertionOrder.remove(elem);
                size--;
                operationsCount++;
                lastBucketAccessed = bucketIndex;
                buckets[bucketIndex].highlight();
                lastOperation = "REMOVE: " + value + " from bucket[" + bucketIndex + "]";
                return true;
            }
        }
        lastOperation = "REMOVE FAILED: " + value + " not found";
        return false;
    }

    public synchronized boolean contains(Object value) {
        int hash = value.hashCode();
        int bucketIndex = (hash & 0x7FFFFFFF) % capacity;
        operationsCount++;
        lastBucketAccessed = bucketIndex;
        buckets[bucketIndex].highlight();

        boolean found = buckets[bucketIndex].contains(value);
        lastOperation = "CONTAINS: " + value + " -> " + found + " (bucket[" + bucketIndex + "])";
        return found;
    }

    public synchronized void clear() {
        for (VisualBucket bucket : buckets) {
            bucket.clear();
        }
        insertionOrder.clear();
        size = 0;
        operationsCount++;
        lastOperation = "CLEAR: removed all elements";
    }

    private void rehash() {
        int oldCapacity = capacity;
        capacity = capacity * 2;
        VisualBucket[] oldBuckets = buckets;
        buckets = new VisualBucket[capacity];

        for (int i = 0; i < capacity; i++) {
            buckets[i] = new VisualBucket(i);
        }

        List<VisualElement> oldOrder = new ArrayList<>(insertionOrder);
        insertionOrder.clear();
        size = 0;

        for (VisualElement elem : oldOrder) {
            if (!elem.isRemoving()) {
                int hash = elem.getValue().hashCode();
                int newIndex = (hash & 0x7FFFFFFF) % capacity;
                VisualElement newElem = new VisualElement(elem.getValue(), elem.getType());
                newElem.setPosition(elem.getX(), elem.getY());
                buckets[newIndex].addElement(newElem);
                insertionOrder.add(newElem);
                size++;
            }
        }

        updateBucketPositions();
        rehashCount++;
        justRehashed = true;
        lastRehashTime = System.currentTimeMillis();
        lastOperation = "REHASH: " + oldCapacity + " -> " + capacity;
    }

    private void updateBucketPositions() {
        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;

        for (int i = 0; i < capacity; i++) {
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * cellWidth + cellWidth / 2.0;
            double y = startY + row * 180;
            buckets[i].setTarget(x, y);
        }

        for (VisualBucket bucket : buckets) {
            bucket.updateChainPositions();
        }
    }

    public synchronized void update(double deltaTime) {
        for (VisualBucket bucket : buckets) {
            bucket.update(deltaTime);
        }

        insertionOrder.removeIf(VisualElement::isFullyRemoved);

        if (justRehashed && System.currentTimeMillis() - lastRehashTime > 1500) {
            justRehashed = false;
        }
    }

    public int getSize() { return size; }
    public int getCapacity() { return capacity; }
    public double getLoadFactor() { return loadFactor; }
    public double getCurrentLoad() { return (double) size / capacity; }
    public int getThreshold() { return (int)(capacity * loadFactor); }
    public int getOperationsCount() { return operationsCount; }
    public int getRehashCount() { return rehashCount; }
    public boolean wasJustRehashed() { return justRehashed; }
    public String getLastOperation() { return lastOperation; }
    public int getLastBucketAccessed() { return lastBucketAccessed; }
    public VisualBucket[] getBuckets() { return buckets; }
    public List<VisualElement> getInsertionOrder() { return insertionOrder; }

    public int getCollisionCount() {
        int collisions = 0;
        for (VisualBucket bucket : buckets) {
            int chainSize = bucket.getChainSize();
            if (chainSize > 1) {
                collisions += chainSize - 1;
            }
        }
        return collisions;
    }

    public int getMaxChainLength() {
        int max = 0;
        for (VisualBucket bucket : buckets) {
            max = Math.max(max, bucket.getChainSize());
        }
        return max;
    }

    public MemoryInfo getMemoryInfo() {
        long linkedHashSetOverhead = 40;
        long tableOverhead = 16 + (long) capacity * 4;
        long nodesOverhead = (long) size * 40;

        long elementsMemory = 0;
        for (VisualElement elem : insertionOrder) {
            if (!elem.isRemoving()) {
                elementsMemory += estimateObjectSize(elem.getValue(), elem.getType());
            }
        }

        int emptyBuckets = 0;
        for (VisualBucket bucket : buckets) {
            if (bucket.isEmpty()) emptyBuckets++;
        }

        long totalUsed = linkedHashSetOverhead + tableOverhead + nodesOverhead + elementsMemory;

        return new MemoryInfo(totalUsed, linkedHashSetOverhead, tableOverhead, nodesOverhead, elementsMemory, emptyBuckets);
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
            long linkedHashSetOverhead,
            long tableOverhead,
            long nodesOverhead,
            long elementsMemory,
            int emptyBuckets
    ) {
        public String formatTotal() { return formatBytes(totalUsed); }
        public String formatNodes() { return formatBytes(nodesOverhead); }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }

    public static Object generateRandomValue(String type, Random random) {
        return VisualHashSet.generateRandomValue(type, random);
    }
}
