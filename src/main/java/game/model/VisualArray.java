package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualArray {
    private VisualElement[] elements;
    private int size = 0;
    private int capacity;
    private int operationsCount = 0;
    private String elementType; // Array stores single type
    private boolean isPrimitive;

    public VisualArray(int capacity, String elementType, boolean isPrimitive) {
        this.capacity = capacity;
        this.elementType = elementType;
        this.isPrimitive = isPrimitive;
        this.elements = new VisualElement[capacity];
    }

    public synchronized boolean add(Object value) {
        if (size >= capacity) {
            return false; // Array is full
        }

        VisualElement element = new VisualElement(value, elementType);
        element.setPosition(800, -50);
        elements[size] = element;
        size++;
        operationsCount++;
        updateTargetPositions();
        return true;
    }

    public synchronized boolean set(int index, Object value) {
        if (index < 0 || index >= capacity) {
            return false;
        }

        if (elements[index] != null) {
            elements[index].startRemoving();
        }

        VisualElement element = new VisualElement(value, elementType);
        element.setPosition(elements[index] != null ? elements[index].getX() : 400, -50);
        elements[index] = element;
        if (index >= size) {
            size = index + 1;
        }
        operationsCount++;
        updateTargetPositions();
        return true;
    }

    public synchronized void removeLast() {
        if (size > 0) {
            size--;
            if (elements[size] != null) {
                elements[size].startRemoving();
            }
            operationsCount++;
        }
    }

    public synchronized void clear() {
        for (int i = 0; i < size; i++) {
            if (elements[i] != null) {
                elements[i].startRemoving();
            }
        }
        size = 0;
        operationsCount++;
    }

    private void updateTargetPositions() {
        int cols = 10;
        int cellWidth = 70;
        int cellHeight = 70;
        int startX = 50;
        int startY = 100;

        for (int i = 0; i < capacity; i++) {
            if (elements[i] != null && !elements[i].isRemoving()) {
                int col = i % cols;
                int row = i / cols;
                double tx = startX + col * cellWidth + cellWidth / 2.0;
                double ty = startY + row * cellHeight + cellHeight / 2.0;
                elements[i].setTarget(tx, ty);
            }
        }
    }

    public synchronized void update(double deltaTime) {
        for (int i = 0; i < capacity; i++) {
            if (elements[i] != null) {
                elements[i].update(deltaTime);
                if (elements[i].isFullyRemoved()) {
                    elements[i] = null;
                }
            }
        }
    }

    public synchronized List<VisualElement> getElements() {
        List<VisualElement> list = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            if (elements[i] != null) {
                list.add(elements[i]);
            }
        }
        return list;
    }

    public synchronized List<Integer> getFilledIndices() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (elements[i] != null && !elements[i].isRemoving()) {
                indices.add(i);
            }
        }
        return indices;
    }

    public int getSize() { return size; }
    public int getCapacity() { return capacity; }
    public int getOperationsCount() { return operationsCount; }
    public String getElementType() { return elementType; }
    public boolean isPrimitive() { return isPrimitive; }

    // Memory calculation
    public MemoryInfo getMemoryInfo() {
        if (isPrimitive) {
            // Primitive array: header(16) + size * primitiveSize
            int primitiveSize = getPrimitiveSize(elementType);
            long arrayMemory = 16 + (long) capacity * primitiveSize;
            long usedMemory = 16 + (long) size * primitiveSize;
            long wastedMemory = (long) (capacity - size) * primitiveSize;
            return new MemoryInfo(usedMemory, arrayMemory, 0, arrayMemory, wastedMemory, true);
        } else {
            // Object array: header(16) + capacity * 4 (refs) + element objects
            long arrayOverhead = 16 + (long) capacity * 4;
            long elementsMemory = 0;
            for (int i = 0; i < size; i++) {
                if (elements[i] != null && !elements[i].isRemoving()) {
                    elementsMemory += estimateObjectSize(elements[i].getValue(), elementType);
                }
            }
            long wastedMemory = (long) (capacity - size) * 4;
            long totalUsed = arrayOverhead + elementsMemory;
            return new MemoryInfo(totalUsed, arrayOverhead + elementsMemory + wastedMemory,
                                  elementsMemory, arrayOverhead, wastedMemory, false);
        }
    }

    private int getPrimitiveSize(String type) {
        return switch (type.toLowerCase()) {
            case "byte" -> 1;
            case "boolean" -> 1;
            case "char" -> 2;
            case "short" -> 2;
            case "int" -> 4;
            case "float" -> 4;
            case "long" -> 8;
            case "double" -> 8;
            default -> 4;
        };
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
            long totalAllocated,
            long elementsMemory,
            long arrayOverhead,
            long wastedMemory,
            boolean isPrimitive
    ) {
        public String formatTotal() {
            return formatBytes(totalUsed);
        }

        public String formatAllocated() {
            return formatBytes(totalAllocated);
        }

        public String formatElements() {
            return formatBytes(elementsMemory);
        }

        public String formatWasted() {
            return formatBytes(wastedMemory);
        }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }

    public static Object generateRandomValue(String type, Random random) {
        return VisualArrayList.generateRandomValue(type, random);
    }
}
