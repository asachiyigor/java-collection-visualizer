package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualTreeMap {
    private VisualTreeNode root;
    private int size = 0;
    private int operationsCount = 0;
    private int rotationCount = 0;
    private String lastOperation = "";
    private List<VisualTreeNode> highlightedPath = new ArrayList<>();

    public VisualTreeMap() {
    }

    public synchronized Object put(Object key, Object value, String keyType, String valueType) {
        highlightedPath.clear();

        if (root == null) {
            root = new VisualTreeNode(key, value, keyType, valueType);
            root.setRed(false);
            root.setPosition(400, 80);
            root.setLevel(0);
            size++;
            operationsCount++;
            lastOperation = "PUT: " + key + " -> " + value + " (root)";
            updateLayout();
            return null;
        }

        VisualTreeNode existing = findNode(root, key);
        if (existing != null) {
            Object oldValue = existing.getValue();
            existing.setValue(value);
            operationsCount++;
            lastOperation = "PUT: " + key + " -> " + value + " (updated)";
            return oldValue;
        }

        VisualTreeNode newNode = new VisualTreeNode(key, value, keyType, valueType);
        newNode.setPosition(400, -50);
        insertNode(root, newNode);
        size++;
        operationsCount++;

        fixAfterInsert(newNode);
        updateLayout();
        lastOperation = "PUT: " + key + " -> " + value;

        return null;
    }

    private void insertNode(VisualTreeNode current, VisualTreeNode newNode) {
        highlightedPath.add(current);

        int cmp = newNode.compareTo(current.getKey());
        if (cmp < 0) {
            if (current.getLeft() == null) {
                current.setLeft(newNode);
                newNode.setLevel(current.getLevel() + 1);
            } else {
                insertNode(current.getLeft(), newNode);
            }
        } else {
            if (current.getRight() == null) {
                current.setRight(newNode);
                newNode.setLevel(current.getLevel() + 1);
            } else {
                insertNode(current.getRight(), newNode);
            }
        }
    }

    private void fixAfterInsert(VisualTreeNode node) {
        while (node != root && node.getParent() != null && node.getParent().isRed()) {
            VisualTreeNode parent = node.getParent();
            VisualTreeNode grandparent = parent.getParent();
            if (grandparent == null) break;

            if (parent == grandparent.getLeft()) {
                VisualTreeNode uncle = grandparent.getRight();
                if (uncle != null && uncle.isRed()) {
                    parent.setRed(false);
                    uncle.setRed(false);
                    grandparent.setRed(true);
                    node = grandparent;
                } else {
                    if (node == parent.getRight()) {
                        node = parent;
                        rotateLeft(node);
                        parent = node.getParent();
                    }
                    if (parent != null) {
                        parent.setRed(false);
                        if (grandparent != null) {
                            grandparent.setRed(true);
                            rotateRight(grandparent);
                        }
                    }
                }
            } else {
                VisualTreeNode uncle = grandparent.getLeft();
                if (uncle != null && uncle.isRed()) {
                    parent.setRed(false);
                    uncle.setRed(false);
                    grandparent.setRed(true);
                    node = grandparent;
                } else {
                    if (node == parent.getLeft()) {
                        node = parent;
                        rotateRight(node);
                        parent = node.getParent();
                    }
                    if (parent != null) {
                        parent.setRed(false);
                        if (grandparent != null) {
                            grandparent.setRed(true);
                            rotateLeft(grandparent);
                        }
                    }
                }
            }
        }
        root.setRed(false);
    }

    private void rotateLeft(VisualTreeNode node) {
        VisualTreeNode right = node.getRight();
        if (right == null) return;

        node.setRight(right.getLeft());
        if (right.getLeft() != null) right.getLeft().setParent(node);

        right.setParent(node.getParent());
        if (node.getParent() == null) {
            root = right;
        } else if (node == node.getParent().getLeft()) {
            node.getParent().setLeft(right);
        } else {
            node.getParent().setRight(right);
        }

        right.setLeft(node);
        node.setParent(right);
        rotationCount++;
    }

    private void rotateRight(VisualTreeNode node) {
        VisualTreeNode left = node.getLeft();
        if (left == null) return;

        node.setLeft(left.getRight());
        if (left.getRight() != null) left.getRight().setParent(node);

        left.setParent(node.getParent());
        if (node.getParent() == null) {
            root = left;
        } else if (node == node.getParent().getRight()) {
            node.getParent().setRight(left);
        } else {
            node.getParent().setLeft(left);
        }

        left.setRight(node);
        node.setParent(left);
        rotationCount++;
    }

    public synchronized Object get(Object key) {
        highlightedPath.clear();
        operationsCount++;
        VisualTreeNode node = findNode(root, key);
        if (node != null) {
            lastOperation = "GET: " + key + " -> " + node.getValue();
            return node.getValue();
        }
        lastOperation = "GET: " + key + " -> null";
        return null;
    }

    private VisualTreeNode findNode(VisualTreeNode node, Object key) {
        if (node == null) return null;
        highlightedPath.add(node);

        int cmp = ((Comparable) key).compareTo(node.getKey());
        if (cmp == 0) return node;
        if (cmp < 0) return findNode(node.getLeft(), key);
        return findNode(node.getRight(), key);
    }

    public synchronized boolean containsKey(Object key) {
        highlightedPath.clear();
        operationsCount++;
        boolean found = findNode(root, key) != null;
        lastOperation = "CONTAINS KEY: " + key + " -> " + found;
        return found;
    }

    public synchronized Object firstKey() {
        if (root == null) return null;
        VisualTreeNode node = root;
        while (node.getLeft() != null) node = node.getLeft();
        lastOperation = "FIRST KEY: " + node.getKey();
        return node.getKey();
    }

    public synchronized Object lastKey() {
        if (root == null) return null;
        VisualTreeNode node = root;
        while (node.getRight() != null) node = node.getRight();
        lastOperation = "LAST KEY: " + node.getKey();
        return node.getKey();
    }

    public synchronized void clear() {
        if (root != null) clearNode(root);
        root = null;
        size = 0;
        operationsCount++;
        lastOperation = "CLEAR: removed all entries";
    }

    private void clearNode(VisualTreeNode node) {
        if (node == null) return;
        node.startRemoving();
        clearNode(node.getLeft());
        clearNode(node.getRight());
    }

    private void updateLayout() {
        if (root == null) return;
        int width = 700;
        layoutNode(root, 50, width - 50, 80, 70, 0);
    }

    private void layoutNode(VisualTreeNode node, double left, double right, double y, int levelHeight, int level) {
        if (node == null) return;
        double x = (left + right) / 2;
        node.setTarget(x, y);
        node.setLevel(level);

        double childWidth = (right - left) / 2;
        if (node.getLeft() != null) layoutNode(node.getLeft(), left, x, y + levelHeight, levelHeight, level + 1);
        if (node.getRight() != null) layoutNode(node.getRight(), x, right, y + levelHeight, levelHeight, level + 1);
    }

    public synchronized void update(double deltaTime) {
        if (root != null) root.update(deltaTime);
    }

    public int getSize() { return size; }
    public int getHeight() { return root == null ? 0 : root.getHeight(); }
    public int getOperationsCount() { return operationsCount; }
    public int getRotationCount() { return rotationCount; }
    public String getLastOperation() { return lastOperation; }
    public VisualTreeNode getRoot() { return root; }
    public List<VisualTreeNode> getHighlightedPath() { return highlightedPath; }

    public MemoryInfo getMemoryInfo() {
        long treeMapOverhead = 32;
        long entriesOverhead = (long) size * 40;
        long keysMemory = 0;
        long valuesMemory = 0;

        if (root != null) {
            long[] mem = calculateEntriesMemory(root);
            keysMemory = mem[0];
            valuesMemory = mem[1];
        }

        long totalUsed = treeMapOverhead + entriesOverhead + keysMemory + valuesMemory;

        return new MemoryInfo(totalUsed, treeMapOverhead, entriesOverhead, keysMemory, valuesMemory, getHeight());
    }

    private long[] calculateEntriesMemory(VisualTreeNode node) {
        if (node == null) return new long[]{0, 0};
        long keyMem = estimateObjectSize(node.getKey(), node.getKeyType());
        long valMem = estimateObjectSize(node.getValue(), node.getValueType());
        long[] left = calculateEntriesMemory(node.getLeft());
        long[] right = calculateEntriesMemory(node.getRight());
        return new long[]{keyMem + left[0] + right[0], valMem + left[1] + right[1]};
    }

    private long estimateObjectSize(Object value, String type) {
        if (type == null) return 0;
        return switch (type.toLowerCase()) {
            case "int", "integer" -> 16;
            case "double" -> 24;
            case "string" -> {
                String s = (String) value;
                int bytesLength = s.length();
                yield 24 + 16 + ((bytesLength + 7) / 8) * 8;
            }
            default -> 24;
        };
    }

    public record MemoryInfo(long totalUsed, long treeMapOverhead, long entriesOverhead, long keysMemory, long valuesMemory, int height) {
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
                String[] keys = {"alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta"};
                yield keys[random.nextInt(keys.length)];
            }
            default -> "k" + random.nextInt(100);
        };
    }

    public static Object generateRandomValue(String type, Random random) {
        return VisualHashMap.generateRandomValue(type, random);
    }
}
