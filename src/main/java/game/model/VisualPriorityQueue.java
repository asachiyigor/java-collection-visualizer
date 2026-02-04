package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualPriorityQueue {
    private List<VisualElement> elements = new ArrayList<>();
    private int capacity = 11;
    private int operationsCount = 0;
    private String lastOperation = "";
    private List<Integer> highlightedIndices = new ArrayList<>();
    private String lastSiftDirection = "";

    public VisualPriorityQueue() {
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean offer(Object value, String type) {
        highlightedIndices.clear();

        if (elements.size() >= capacity) {
            int oldCapacity = capacity;
            capacity = capacity + (capacity < 64 ? capacity + 2 : (capacity >> 1));
            lastOperation = "RESIZE: " + oldCapacity + " -> " + capacity;
        }

        VisualElement element = new VisualElement(value, type);
        element.setPosition(400, -50);
        elements.add(element);
        int index = elements.size() - 1;

        siftUp(index);
        operationsCount++;
        lastOperation = "OFFER: " + value + " (size=" + elements.size() + ")";
        updateTargetPositions();
        return true;
    }

    @SuppressWarnings("unchecked")
    public synchronized Object poll() {
        highlightedIndices.clear();

        if (elements.isEmpty()) {
            lastOperation = "POLL: empty queue";
            operationsCount++;
            return null;
        }

        Object result = elements.get(0).getValue();
        elements.get(0).startRemoving();

        int lastIndex = elements.size() - 1;
        if (lastIndex > 0) {
            VisualElement last = elements.get(lastIndex);
            elements.set(0, last);
            elements.remove(lastIndex);
            siftDown(0);
        } else {
            elements.remove(0);
        }

        operationsCount++;
        lastOperation = "POLL: " + result;
        updateTargetPositions();
        return result;
    }

    public synchronized Object peek() {
        operationsCount++;
        if (elements.isEmpty()) {
            lastOperation = "PEEK: empty queue";
            return null;
        }
        Object result = elements.get(0).getValue();
        lastOperation = "PEEK: " + result;
        highlightedIndices.clear();
        highlightedIndices.add(0);
        return result;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean contains(Object value) {
        highlightedIndices.clear();
        operationsCount++;

        for (int i = 0; i < elements.size(); i++) {
            VisualElement e = elements.get(i);
            if (!e.isRemoving()) {
                highlightedIndices.add(i);
                if (e.getValue() != null && e.getValue().toString().equals(value.toString())) {
                    lastOperation = "CONTAINS: " + value + " -> true (index " + i + ")";
                    return true;
                }
            }
        }
        lastOperation = "CONTAINS: " + value + " -> false";
        return false;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean remove(Object value) {
        highlightedIndices.clear();
        operationsCount++;

        int removeIndex = -1;
        for (int i = 0; i < elements.size(); i++) {
            VisualElement e = elements.get(i);
            if (!e.isRemoving() && e.getValue() != null
                    && e.getValue().toString().equals(value.toString())) {
                removeIndex = i;
                break;
            }
        }

        if (removeIndex < 0) {
            lastOperation = "REMOVE: " + value + " -> not found";
            return false;
        }

        int lastIndex = elements.size() - 1;
        if (removeIndex == lastIndex) {
            elements.remove(lastIndex);
        } else {
            VisualElement last = elements.get(lastIndex);
            elements.set(removeIndex, last);
            elements.remove(lastIndex);
            siftDown(removeIndex);
            if (highlightedIndices.size() <= 1) {
                siftUp(removeIndex);
            }
        }

        lastOperation = "REMOVE: " + value + " -> removed";
        updateTargetPositions();
        return true;
    }

    public synchronized void clear() {
        highlightedIndices.clear();
        for (VisualElement e : elements) {
            e.startRemoving();
        }
        operationsCount++;
        lastOperation = "CLEAR: removed all elements";
    }

    public synchronized void update(double deltaTime) {
        for (VisualElement e : elements) {
            e.update(deltaTime);
        }
        elements.removeIf(VisualElement::isFullyRemoved);
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int index) {
        highlightedIndices.clear();
        highlightedIndices.add(index);
        lastSiftDirection = "UP";

        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            highlightedIndices.add(parentIndex);

            Comparable<Object> child = (Comparable<Object>) elements.get(index).getValue();
            Comparable<Object> parent = (Comparable<Object>) elements.get(parentIndex).getValue();

            if (child.compareTo((Object) parent) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int index) {
        highlightedIndices.clear();
        highlightedIndices.add(index);
        lastSiftDirection = "DOWN";

        int size = elements.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size) {
                highlightedIndices.add(left);
                Comparable<Object> leftVal = (Comparable<Object>) elements.get(left).getValue();
                Comparable<Object> smallestVal = (Comparable<Object>) elements.get(smallest).getValue();
                if (leftVal.compareTo((Object) smallestVal) < 0) {
                    smallest = left;
                }
            }

            if (right < size) {
                highlightedIndices.add(right);
                Comparable<Object> rightVal = (Comparable<Object>) elements.get(right).getValue();
                Comparable<Object> smallestVal = (Comparable<Object>) elements.get(smallest).getValue();
                if (rightVal.compareTo((Object) smallestVal) < 0) {
                    smallest = right;
                }
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        VisualElement temp = elements.get(i);
        elements.set(i, elements.get(j));
        elements.set(j, temp);
    }

    private void updateTargetPositions() {
        int cols = 10;
        int cellWidth = 70;
        int cellHeight = 70;
        int startX = 50;
        int startY = 100;

        for (int i = 0; i < elements.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            double tx = startX + col * cellWidth + cellWidth / 2.0;
            double ty = startY + row * cellHeight + cellHeight / 2.0;
            elements.get(i).setTarget(tx, ty);
        }
    }

    // Memory calculation
    public MemoryInfo getMemoryInfo() {
        // PriorityQueue overhead: header(12) + Object[] ref(4) + size(4) + modCount(4) + comparator ref(4) = ~28, padded to 32
        long pqOverhead = 32;

        // Object[] array: 16 bytes header + 4 bytes per reference (compressed oops)
        long arrayOverhead = 16 + (long) capacity * 4;

        // Elements memory
        long elementsMemory = 0;
        for (VisualElement e : elements) {
            if (!e.isRemoving()) {
                elementsMemory += estimateObjectSize(e.getValue(), e.getType());
            }
        }

        // Wasted space (unused slots)
        int unusedSlots = capacity - elements.size();
        long wastedMemory = (long) unusedSlots * 4;

        long totalUsed = pqOverhead + arrayOverhead + elementsMemory;

        return new MemoryInfo(totalUsed, arrayOverhead, elementsMemory, wastedMemory);
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
            long arrayOverhead,
            long elementsMemory,
            long wastedMemory
    ) {
        public String formatTotal() { return formatBytes(totalUsed); }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }

    // Generate random values
    public static Object generateRandomValue(String type, Random random) {
        return switch (type.toLowerCase()) {
            case "int" -> random.nextInt(1000);
            case "double" -> Math.round(random.nextDouble() * 100 * 100.0) / 100.0;
            case "boolean" -> random.nextBoolean();
            case "char" -> (char) ('A' + random.nextInt(26));
            case "string" -> {
                String[] words = {"Alpha", "Beta", "Gamma", "Delta", "Echo", "Foxtrot"};
                yield words[random.nextInt(words.length)];
            }
            default -> "obj" + random.nextInt(100);
        };
    }

    // Getters
    public int getSize() { return elements.size(); }
    public int getCapacity() { return capacity; }
    public int getOperationsCount() { return operationsCount; }
    public String getLastOperation() { return lastOperation; }
    public List<Integer> getHighlightedIndices() { return highlightedIndices; }
    public String getLastSiftDirection() { return lastSiftDirection; }

    public synchronized List<VisualElement> getElements() {
        return new ArrayList<>(elements);
    }
}
