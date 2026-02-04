package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualLinkedList {
    private List<VisualNode> nodes = new ArrayList<>();
    private int operationsCount = 0;

    public synchronized void addLast(Object value, String type) {
        VisualNode node = new VisualNode(value, type, nodes.size());
        node.setPosition(800, -50);
        nodes.add(node);
        operationsCount++;
        updateTargetPositions();
    }

    public synchronized void addFirst(Object value, String type) {
        VisualNode node = new VisualNode(value, type, 0);
        node.setPosition(-50, 200);
        nodes.add(0, node);
        // Update indices
        for (int i = 1; i < nodes.size(); i++) {
            nodes.get(i).setIndex(i);
        }
        operationsCount++;
        updateTargetPositions();
    }

    public synchronized void removeLast() {
        if (!nodes.isEmpty()) {
            nodes.get(nodes.size() - 1).startRemoving();
            operationsCount++;
        }
    }

    public synchronized void removeFirst() {
        if (!nodes.isEmpty()) {
            nodes.get(0).startRemoving();
            operationsCount++;
        }
    }

    public synchronized void clear() {
        for (VisualNode node : nodes) {
            node.startRemoving();
        }
        operationsCount++;
    }

    private void updateTargetPositions() {
        int cols = 5;
        int cellWidth = 140;
        int cellHeight = 90;
        int startX = 50;
        int startY = 120;

        for (int i = 0; i < nodes.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            double tx = startX + col * cellWidth + 60;
            double ty = startY + row * cellHeight + 35;
            nodes.get(i).setTarget(tx, ty);
            nodes.get(i).setIndex(i);
        }
    }

    public synchronized void update(double deltaTime) {
        for (VisualNode node : nodes) {
            node.update(deltaTime);
        }
        nodes.removeIf(VisualNode::isFullyRemoved);
        // Re-index after removal
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setIndex(i);
        }
        updateTargetPositions();
    }

    public synchronized List<VisualNode> getNodes() {
        return new ArrayList<>(nodes);
    }

    public int getSize() { return nodes.size(); }
    public int getOperationsCount() { return operationsCount; }

    // Memory calculation for LinkedList
    public MemoryInfo getMemoryInfo() {
        // LinkedList object: 24 bytes (header 12 + size 4 + modCount 4 + first ref 4 + last ref 4 - but aligned)
        // Actually: header(12) + size(4) + modCount(4) + first(4) + last(4) = 28 -> padded to 32
        long linkedListOverhead = 32;

        // Each Node: header(12) + item ref(4) + next ref(4) + prev ref(4) = 24 bytes
        long nodeOverhead = (long) nodes.size() * 24;

        // Element objects
        long elementsMemory = 0;
        for (VisualNode node : nodes) {
            if (!node.isRemoving()) {
                elementsMemory += estimateObjectSize(node.getValue(), node.getType());
            }
        }

        long totalUsed = linkedListOverhead + nodeOverhead + elementsMemory;

        return new MemoryInfo(totalUsed, nodeOverhead, elementsMemory, linkedListOverhead);
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
            long nodesOverhead,
            long elementsMemory,
            long linkedListOverhead
    ) {
        public String formatTotal() {
            return formatBytes(totalUsed);
        }

        public String formatNodes() {
            return formatBytes(nodesOverhead);
        }

        public String formatElements() {
            return formatBytes(elementsMemory);
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
