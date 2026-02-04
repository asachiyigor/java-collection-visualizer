package game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VisualArrayList {
    private List<VisualElement> elements = new ArrayList<>();
    private int capacity = 10;
    private int operationsCount = 0;
    private int resizeCount = 0;
    private boolean justResized = false;
    private long lastResizeTime = 0;

    public synchronized void add(Object value, String type) {
        if (elements.size() >= capacity) {
            resize();
        }

        VisualElement element = new VisualElement(value, type);
        element.setPosition(800, -50); // Start from top-right, off screen
        elements.add(element);
        operationsCount++;
        updateTargetPositions();
    }

    public synchronized void addAt(int index, Object value, String type) {
        if (index < 0 || index > elements.size()) return;

        if (elements.size() >= capacity) {
            resize();
        }

        VisualElement element = new VisualElement(value, type);
        element.setPosition(400, -50);
        elements.add(index, element);
        operationsCount++;
        updateTargetPositions();
    }

    public synchronized void remove(int index) {
        if (index >= 0 && index < elements.size()) {
            elements.get(index).startRemoving();
            operationsCount++;
        }
    }

    public synchronized void removeRemoved() {
        elements.removeIf(VisualElement::isFullyRemoved);
        updateTargetPositions();
    }

    public synchronized void clear() {
        for (VisualElement e : elements) {
            e.startRemoving();
        }
        operationsCount++;
    }

    private void resize() {
        int oldCapacity = capacity;
        capacity = capacity + (capacity >> 1); // Grow by 50%
        resizeCount++;
        justResized = true;
        lastResizeTime = System.currentTimeMillis();
        lastOperation = "RESIZE: " + oldCapacity + " -> " + capacity;
    }

    // trimToSize() - reduces capacity to match size
    public synchronized void trimToSize() {
        int oldCapacity = capacity;
        int size = elements.size();
        if (size < capacity) {
            capacity = Math.max(size, 1); // At least 1
            resizeCount++;
            justResized = true;
            lastResizeTime = System.currentTimeMillis();
            lastOperation = "TRIM: " + oldCapacity + " -> " + capacity;
            operationsCount++;
        }
    }

    // ensureCapacity(minCapacity) - ensures minimum capacity
    public synchronized void ensureCapacity(int minCapacity) {
        int oldCapacity = capacity;
        if (minCapacity > capacity) {
            // Grow to at least minCapacity
            int newCapacity = capacity + (capacity >> 1);
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            capacity = newCapacity;
            resizeCount++;
            justResized = true;
            lastResizeTime = System.currentTimeMillis();
            lastOperation = "ENSURE: " + oldCapacity + " -> " + capacity;
            operationsCount++;
        }
    }

    // Get element at index (for highlighting)
    public synchronized VisualElement get(int index) {
        if (index >= 0 && index < elements.size()) {
            operationsCount++;
            lastOperation = "GET [" + index + "]";
            return elements.get(index);
        }
        return null;
    }

    // indexOf simulation
    public synchronized int indexOf(Object value) {
        operationsCount++;
        for (int i = 0; i < elements.size(); i++) {
            if (!elements.get(i).isRemoving()) {
                Object elemValue = elements.get(i).getValue();
                if (elemValue != null && elemValue.toString().equals(value.toString())) {
                    lastOperation = "INDEXOF: found at [" + i + "]";
                    return i;
                }
            }
        }
        lastOperation = "INDEXOF: not found (-1)";
        return -1;
    }

    // contains check
    public synchronized boolean contains(Object value) {
        int idx = indexOf(value);
        lastOperation = "CONTAINS: " + (idx >= 0);
        return idx >= 0;
    }

    // set(index, element) - replace element at index
    public synchronized Object set(int index, Object value, String type) {
        if (index < 0 || index >= elements.size()) {
            lastOperation = "SET: index out of bounds";
            return null;
        }
        VisualElement old = elements.get(index);
        Object oldValue = old.getValue();
        old.startRemoving();

        VisualElement newElem = new VisualElement(value, type);
        double x = old.getX();
        double y = old.getY();
        newElem.setPosition(x, y - 50);
        newElem.setTarget(x, y);
        elements.set(index, newElem);

        operationsCount++;
        lastOperation = "SET [" + index + "]: " + oldValue + " -> " + value;
        return oldValue;
    }

    // addAll - add multiple elements (uses System.arraycopy internally)
    public synchronized boolean addAll(java.util.Collection<?> collection, String type) {
        if (collection == null || collection.isEmpty()) return false;

        int numNew = collection.size();
        while (elements.size() + numNew > capacity) {
            resize();
        }

        int startIndex = elements.size();
        for (Object value : collection) {
            VisualElement element = new VisualElement(value, type);
            element.setPosition(800, -50);
            elements.add(element);
        }

        operationsCount++;
        lastOperation = "ADDALL: +" + numNew + " elements (arraycopy)";
        updateTargetPositions();
        return true;
    }

    // addAll at index - insert collection at position (uses System.arraycopy for shift)
    public synchronized boolean addAllAt(int index, java.util.Collection<?> collection, String type) {
        if (collection == null || collection.isEmpty()) return false;
        if (index < 0 || index > elements.size()) {
            lastOperation = "ADDALL: index out of bounds";
            return false;
        }

        int numNew = collection.size();
        while (elements.size() + numNew > capacity) {
            resize();
        }

        // Elements after index need to shift right
        int shift = elements.size() - index;

        int i = index;
        for (Object value : collection) {
            VisualElement element = new VisualElement(value, type);
            element.setPosition(400, -50);
            elements.add(i++, element);
        }

        operationsCount++;
        lastOperation = "ADDALL [" + index + "]: +" + numNew + " (shift " + shift + ")";
        updateTargetPositions();
        return true;
    }

    // removeRange - remove elements in range (uses System.arraycopy for shift)
    public synchronized void removeRange(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > elements.size() || fromIndex > toIndex) {
            lastOperation = "REMOVERANGE: invalid range";
            return;
        }

        int numRemoved = toIndex - fromIndex;
        for (int i = fromIndex; i < toIndex; i++) {
            elements.get(i).startRemoving();
        }

        operationsCount++;
        lastOperation = "REMOVERANGE [" + fromIndex + "," + toIndex + "): -" + numRemoved + " (arraycopy shift)";
    }

    // subList view - highlights elements in range
    public synchronized java.util.List<VisualElement> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > elements.size() || fromIndex > toIndex) {
            lastOperation = "SUBLIST: invalid range";
            return java.util.Collections.emptyList();
        }

        operationsCount++;
        lastOperation = "SUBLIST [" + fromIndex + "," + toIndex + "): view of " + (toIndex - fromIndex) + " elements";
        return elements.subList(fromIndex, toIndex);
    }

    // lastIndexOf - find last occurrence
    public synchronized int lastIndexOf(Object value) {
        operationsCount++;
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (!elements.get(i).isRemoving()) {
                Object elemValue = elements.get(i).getValue();
                if (elemValue != null && elemValue.toString().equals(value.toString())) {
                    lastOperation = "LASTINDEXOF: found at [" + i + "]";
                    return i;
                }
            }
        }
        lastOperation = "LASTINDEXOF: not found (-1)";
        return -1;
    }

    // clone simulation - shows copy operation
    public synchronized VisualArrayList cloneList() {
        VisualArrayList copy = new VisualArrayList();
        copy.capacity = this.capacity;
        for (VisualElement e : elements) {
            if (!e.isRemoving()) {
                copy.elements.add(new VisualElement(e.getValue(), e.getType()));
            }
        }
        copy.updateTargetPositions();
        operationsCount++;
        lastOperation = "CLONE: created copy with " + copy.elements.size() + " elements";
        return copy;
    }

    // toArray simulation - copy to new array
    public synchronized Object[] toArray() {
        Object[] array = new Object[elements.size()];
        int i = 0;
        for (VisualElement e : elements) {
            if (!e.isRemoving()) {
                array[i++] = e.getValue();
            }
        }
        operationsCount++;
        lastOperation = "TOARRAY: System.arraycopy to Object[" + i + "]";
        return array;
    }

    // getFirst / getLast (Java 21+)
    public synchronized VisualElement getFirst() {
        if (elements.isEmpty()) {
            lastOperation = "GETFIRST: empty list";
            return null;
        }
        operationsCount++;
        lastOperation = "GETFIRST: " + elements.get(0).getValue();
        return elements.get(0);
    }

    public synchronized VisualElement getLast() {
        if (elements.isEmpty()) {
            lastOperation = "GETLAST: empty list";
            return null;
        }
        operationsCount++;
        int last = elements.size() - 1;
        lastOperation = "GETLAST: " + elements.get(last).getValue();
        return elements.get(last);
    }

    // removeFirst / removeLast (Java 21+)
    public synchronized VisualElement removeFirst() {
        if (elements.isEmpty()) {
            lastOperation = "REMOVEFIRST: empty list";
            return null;
        }
        VisualElement first = elements.get(0);
        first.startRemoving();
        operationsCount++;
        lastOperation = "REMOVEFIRST: " + first.getValue() + " (arraycopy shift)";
        return first;
    }

    public synchronized VisualElement removeLast() {
        if (elements.isEmpty()) {
            lastOperation = "REMOVELAST: empty list";
            return null;
        }
        int lastIdx = elements.size() - 1;
        VisualElement last = elements.get(lastIdx);
        last.startRemoving();
        operationsCount++;
        lastOperation = "REMOVELAST: " + last.getValue();
        return last;
    }

    private String lastOperation = "";

    public String getLastOperation() {
        return lastOperation;
    }

    public boolean wasJustResized() {
        if (justResized && System.currentTimeMillis() - lastResizeTime > 1500) {
            justResized = false;
        }
        return justResized;
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

    public synchronized void update(double deltaTime) {
        for (VisualElement e : elements) {
            e.update(deltaTime);
        }
        removeRemoved();
    }

    public synchronized List<VisualElement> getElements() {
        return new ArrayList<>(elements);
    }

    public int getSize() { return elements.size(); }
    public int getCapacity() { return capacity; }
    public int getOperationsCount() { return operationsCount; }
    public int getResizeCount() { return resizeCount; }

    // Memory calculation (approximate, based on 64-bit JVM with compressed oops)
    public MemoryInfo getMemoryInfo() {
        // ArrayList object overhead: 24 bytes (header 12 + size 4 + modCount 4 + elementData ref 4)
        long arrayListOverhead = 24;

        // Object[] array: 16 bytes header + 4 bytes per reference (compressed oops)
        long arrayOverhead = 16 + (long) capacity * 4;

        // Calculate memory for each element
        long elementsMemory = 0;
        for (VisualElement e : elements) {
            if (!e.isRemoving()) {
                elementsMemory += estimateObjectSize(e.getValue(), e.getType());
            }
        }

        // Wasted space (unused capacity)
        int unusedSlots = capacity - elements.size();
        long wastedMemory = (long) unusedSlots * 4; // empty references

        long totalUsed = arrayListOverhead + arrayOverhead + elementsMemory;
        long totalAllocated = arrayListOverhead + arrayOverhead + elementsMemory + wastedMemory;

        return new MemoryInfo(totalUsed, totalAllocated, elementsMemory, arrayOverhead, wastedMemory);
    }

    private long estimateObjectSize(Object value, String type) {
        // Object header: 12 bytes (mark word 8 + class pointer 4 with compressed oops)
        // Padding to 8-byte boundary
        return switch (type.toLowerCase()) {
            case "int", "integer" -> 16;      // header(12) + int(4) = 16
            case "double" -> 24;               // header(12) + double(8) + padding(4) = 24
            case "boolean" -> 16;              // header(12) + boolean(1) + padding(3) = 16
            case "char", "character" -> 16;   // header(12) + char(2) + padding(2) = 16
            case "string" -> {
                String s = (String) value;
                // String object: header(12) + hash(4) + coder(1) + padding(3) + byte[] ref(4) = 24
                // byte[] for Latin1: header(16) + length bytes, rounded to 8
                int bytesLength = s.length(); // Latin1 encoding
                int arraySize = 16 + ((bytesLength + 7) / 8) * 8;
                yield 24 + arraySize;
            }
            default -> 24; // Generic object estimate
        };
    }

    // Memory info record
    public record MemoryInfo(
            long totalUsed,      // Actual memory used
            long totalAllocated, // Total allocated (including wasted)
            long elementsMemory, // Memory for element objects
            long arrayOverhead,  // Object[] overhead
            long wastedMemory    // Unused capacity slots
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

    // Generate random values for demo
    public static Object generateRandomValue(String type, Random random) {
        return switch (type.toLowerCase()) {
            case "int" -> random.nextInt(1000);
            case "double" -> Math.round(random.nextDouble() * 100 * 100.0) / 100.0;
            case "boolean" -> random.nextBoolean();
            case "char" -> (char) ('A' + random.nextInt(26));
            case "string" -> generateRandomString(random);
            default -> "obj" + random.nextInt(100);
        };
    }

    private static String generateRandomString(Random random) {
        String[] words = {"Node", "Data", "Lab", "Core", "Sys", "Net", "Hub", "Link", "Port", "Flow"};
        return words[random.nextInt(words.length)];
    }

    // ── Animation support ────────────────────────────────────────

    public enum AnimationState { COMPARING, SWAPPING, FOUND, ELIMINATED, SORTED, CURRENT }

    public static class AnimationStep {
        private final Map<Integer, AnimationState> indexStates;
        private final String description;

        public AnimationStep(Map<Integer, AnimationState> indexStates, String description) {
            this.indexStates = indexStates;
            this.description = description;
        }

        public Map<Integer, AnimationState> getIndexStates() { return indexStates; }
        public String getDescription() { return description; }
    }

    private List<AnimationStep> animationSteps = new ArrayList<>();
    private int currentAnimationStep = -1;
    private boolean animating = false;

    public boolean isAnimating() { return animating; }

    public AnimationStep getCurrentStep() {
        if (!animating || currentAnimationStep < 0 || currentAnimationStep >= animationSteps.size()) return null;
        return animationSteps.get(currentAnimationStep);
    }

    public int getCurrentStepIndex() { return currentAnimationStep; }
    public int getTotalSteps() { return animationSteps.size(); }

    public void startAnimation(List<AnimationStep> steps) {
        this.animationSteps = steps;
        this.currentAnimationStep = 0;
        this.animating = true;
    }

    public boolean advanceAnimation() {
        if (!animating) return false;
        currentAnimationStep++;
        if (currentAnimationStep >= animationSteps.size()) {
            animating = false;
            currentAnimationStep = -1;
            return false;
        }

        // Apply swaps if the current step is a SWAPPING step
        AnimationStep step = animationSteps.get(currentAnimationStep);
        List<Integer> swapIndices = new ArrayList<>();
        for (Map.Entry<Integer, AnimationState> entry : step.getIndexStates().entrySet()) {
            if (entry.getValue() == AnimationState.SWAPPING) {
                swapIndices.add(entry.getKey());
            }
        }
        if (swapIndices.size() == 2) {
            swapElements(swapIndices.get(0), swapIndices.get(1));
        }
        return true;
    }

    public void stopAnimation() {
        animating = false;
        currentAnimationStep = -1;
        animationSteps.clear();
    }

    public synchronized void swapElements(int i, int j) {
        if (i >= 0 && i < elements.size() && j >= 0 && j < elements.size() && i != j) {
            VisualElement temp = elements.get(i);
            elements.set(i, elements.get(j));
            elements.set(j, temp);
            updateTargetPositions();
        }
    }

    public List<AnimationStep> generateBubbleSortSteps() {
        List<AnimationStep> steps = new ArrayList<>();
        int n = elements.size();
        if (n <= 1) return steps;

        // Work on a copy of values to pre-compute steps
        List<Comparable> values = new ArrayList<>();
        for (VisualElement e : elements) {
            Object v = e.getValue();
            if (v instanceof Comparable) {
                values.add((Comparable) v);
            } else {
                return steps; // Can't sort non-comparable
            }
        }

        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - pass; j++) {
                // Compare step
                Map<Integer, AnimationState> compareMap = new HashMap<>();
                compareMap.put(j, AnimationState.COMPARING);
                compareMap.put(j + 1, AnimationState.COMPARING);
                // Mark already sorted
                for (int s = n - pass; s < n; s++) compareMap.put(s, AnimationState.SORTED);
                steps.add(new AnimationStep(compareMap, "Comparing [" + j + "] and [" + j + 1 + "]"));

                @SuppressWarnings("unchecked")
                int cmp = values.get(j).compareTo(values.get(j + 1));
                if (cmp > 0) {
                    // Swap step
                    Map<Integer, AnimationState> swapMap = new HashMap<>();
                    swapMap.put(j, AnimationState.SWAPPING);
                    swapMap.put(j + 1, AnimationState.SWAPPING);
                    for (int s = n - pass; s < n; s++) swapMap.put(s, AnimationState.SORTED);
                    steps.add(new AnimationStep(swapMap, "Swapping [" + j + "] and [" + (j + 1) + "]"));

                    Comparable temp = values.get(j);
                    values.set(j, values.get(j + 1));
                    values.set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }

        // Final step: all sorted
        Map<Integer, AnimationState> finalMap = new HashMap<>();
        for (int i = 0; i < n; i++) finalMap.put(i, AnimationState.SORTED);
        steps.add(new AnimationStep(finalMap, "Sort complete!"));

        return steps;
    }

    public List<AnimationStep> generateLinearSearchSteps(Object target) {
        List<AnimationStep> steps = new ArrayList<>();
        String targetStr = String.valueOf(target);
        int n = elements.size();

        for (int i = 0; i < n; i++) {
            Map<Integer, AnimationState> map = new HashMap<>();
            // Mark previously checked as eliminated
            for (int p = 0; p < i; p++) map.put(p, AnimationState.ELIMINATED);
            map.put(i, AnimationState.CURRENT);

            Object val = elements.get(i).getValue();
            boolean match = val != null && val.toString().equals(targetStr);
            if (match) {
                map.put(i, AnimationState.FOUND);
                steps.add(new AnimationStep(map, "Found " + target + " at index [" + i + "]!"));
                return steps;
            }
            steps.add(new AnimationStep(map, "Checking [" + i + "]: " + val));
        }

        // Not found
        Map<Integer, AnimationState> notFoundMap = new HashMap<>();
        for (int i = 0; i < n; i++) notFoundMap.put(i, AnimationState.ELIMINATED);
        steps.add(new AnimationStep(notFoundMap, target + " not found"));
        return steps;
    }

    public List<AnimationStep> generateBinarySearchSteps(Object target) {
        List<AnimationStep> steps = new ArrayList<>();
        int n = elements.size();
        if (n == 0) return steps;

        int targetInt;
        try {
            targetInt = Integer.parseInt(String.valueOf(target));
        } catch (NumberFormatException e) {
            return steps; // Binary search only for integers
        }

        int low = 0, high = n - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Map<Integer, AnimationState> map = new HashMap<>();

            // Mark out-of-range as eliminated
            for (int i = 0; i < low; i++) map.put(i, AnimationState.ELIMINATED);
            for (int i = high + 1; i < n; i++) map.put(i, AnimationState.ELIMINATED);
            // Mark active range
            for (int i = low; i <= high; i++) map.put(i, AnimationState.COMPARING);
            map.put(mid, AnimationState.CURRENT);

            Object midVal = elements.get(mid).getValue();
            int midInt;
            try {
                midInt = Integer.parseInt(String.valueOf(midVal));
            } catch (NumberFormatException e) {
                break;
            }

            if (midInt == targetInt) {
                map.put(mid, AnimationState.FOUND);
                steps.add(new AnimationStep(map, "Found " + target + " at [" + mid + "]!"));
                return steps;
            } else if (midInt < targetInt) {
                steps.add(new AnimationStep(map, "mid=[" + mid + "]=" + midInt + " < " + target + ", go right"));
                low = mid + 1;
            } else {
                steps.add(new AnimationStep(map, "mid=[" + mid + "]=" + midInt + " > " + target + ", go left"));
                high = mid - 1;
            }
        }

        Map<Integer, AnimationState> notFoundMap = new HashMap<>();
        for (int i = 0; i < n; i++) notFoundMap.put(i, AnimationState.ELIMINATED);
        steps.add(new AnimationStep(notFoundMap, target + " not found"));
        return steps;
    }
}
