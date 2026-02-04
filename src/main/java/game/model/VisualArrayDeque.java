package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualArrayDeque {
    private List<VisualElement> elements = new ArrayList<>();
    private int capacity = 16;
    private int head = 0;
    private int tail = 0;
    private int size = 0;
    private int operationsCount = 0;
    private String lastOperation = "";

    private static final String[] SAMPLE_STRINGS = {"alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta"};

    public VisualArrayDeque() {
    }

    public synchronized void addFirst(Object value, String type) {
        ensureCapacity();
        head = (head - 1 + capacity) % capacity;

        VisualElement element = new VisualElement(value, type);
        element.setIndex(head);
        elements.add(0, element);
        size++;
        operationsCount++;
        lastOperation = "addFirst(" + value + ")";
        updatePositions();
    }

    public synchronized void addLast(Object value, String type) {
        ensureCapacity();

        VisualElement element = new VisualElement(value, type);
        element.setIndex(tail);
        elements.add(element);
        tail = (tail + 1) % capacity;
        size++;
        operationsCount++;
        lastOperation = "addLast(" + value + ")";
        updatePositions();
    }

    public synchronized Object removeFirst() {
        if (size == 0) {
            lastOperation = "removeFirst() -> empty";
            return null;
        }

        VisualElement removed = elements.remove(0);
        removed.startRemoving();
        Object value = removed.getValue();
        head = (head + 1) % capacity;
        size--;
        operationsCount++;
        lastOperation = "removeFirst() -> " + value;
        updatePositions();
        return value;
    }

    public synchronized Object removeLast() {
        if (size == 0) {
            lastOperation = "removeLast() -> empty";
            return null;
        }

        VisualElement removed = elements.remove(elements.size() - 1);
        removed.startRemoving();
        Object value = removed.getValue();
        tail = (tail - 1 + capacity) % capacity;
        size--;
        operationsCount++;
        lastOperation = "removeLast() -> " + value;
        updatePositions();
        return value;
    }

    public synchronized Object peekFirst() {
        if (size == 0) {
            lastOperation = "peekFirst() -> null";
            return null;
        }
        Object value = elements.get(0).getValue();
        lastOperation = "peekFirst() -> " + value;
        operationsCount++;
        return value;
    }

    public synchronized Object peekLast() {
        if (size == 0) {
            lastOperation = "peekLast() -> null";
            return null;
        }
        Object value = elements.get(elements.size() - 1).getValue();
        lastOperation = "peekLast() -> " + value;
        operationsCount++;
        return value;
    }

    public synchronized boolean contains(Object value) {
        operationsCount++;
        for (VisualElement e : elements) {
            if (e.getValue().equals(value)) {
                lastOperation = "contains(" + value + ") -> true";
                return true;
            }
        }
        lastOperation = "contains(" + value + ") -> false";
        return false;
    }

    public synchronized void clear() {
        for (VisualElement e : elements) {
            e.startRemoving();
        }
        elements.clear();
        head = 0;
        tail = 0;
        size = 0;
        operationsCount++;
        lastOperation = "clear()";
    }

    private void ensureCapacity() {
        if (size >= capacity) {
            capacity *= 2;
            lastOperation += " (resized to " + capacity + ")";
        }
    }

    private void updatePositions() {
        for (int i = 0; i < elements.size(); i++) {
            elements.get(i).setIndex((head + i) % capacity);
        }
    }

    public synchronized void update(double deltaTime) {
        for (VisualElement e : elements) {
            e.update(deltaTime);
        }
    }

    public int getSize() { return size; }
    public int getCapacity() { return capacity; }
    public int getHead() { return head; }
    public int getTail() { return tail; }
    public int getOperationsCount() { return operationsCount; }
    public String getLastOperation() { return lastOperation; }
    public List<VisualElement> getElements() { return new ArrayList<>(elements); }

    public MemoryInfo getMemoryInfo() {
        long dequeOverhead = 32;
        long arrayOverhead = 16 + capacity * 4;
        long elementsMemory = 0;

        for (VisualElement e : elements) {
            elementsMemory += estimateObjectSize(e.getValue(), e.getType());
        }

        long totalUsed = dequeOverhead + arrayOverhead + elementsMemory;
        long totalCapacity = dequeOverhead + arrayOverhead + capacity * 24;

        return new MemoryInfo(totalUsed, totalCapacity, dequeOverhead, arrayOverhead, elementsMemory, capacity, size);
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

    public record MemoryInfo(long totalUsed, long totalCapacity, long dequeOverhead, long arrayOverhead,
                             long elementsMemory, int capacity, int size) {
        public String formatTotal() { return formatBytes(totalUsed); }
        public String formatCapacity() { return formatBytes(totalCapacity); }
        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }

    public static Object generateRandomValue(String type, Random random) {
        return switch (type.toLowerCase()) {
            case "int" -> random.nextInt(1000);
            case "double" -> Math.round(random.nextDouble() * 1000) / 10.0;
            case "string" -> SAMPLE_STRINGS[random.nextInt(SAMPLE_STRINGS.length)];
            default -> "item" + random.nextInt(100);
        };
    }
}
