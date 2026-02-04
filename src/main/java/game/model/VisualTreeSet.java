package game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualTreeSet {
    private VisualTreeNode root;
    private int size = 0;
    private int operationsCount = 0;
    private int rotationCount = 0;
    private String lastOperation = "";
    private List<VisualTreeNode> highlightedPath = new ArrayList<>();

    public VisualTreeSet() {
    }

    public synchronized boolean add(Object value, String type) {
        highlightedPath.clear();

        if (root == null) {
            root = new VisualTreeNode(value, type);
            root.setRed(false);
            root.setPosition(400, 80);
            root.setLevel(0);
            size++;
            operationsCount++;
            lastOperation = "ADD: " + value + " (root)";
            updateLayout();
            return true;
        }

        if (contains(value)) {
            lastOperation = "DUPLICATE: " + value;
            return false;
        }

        VisualTreeNode newNode = new VisualTreeNode(value, type);
        newNode.setPosition(400, -50);
        insertNode(root, newNode);
        size++;
        operationsCount++;

        fixAfterInsert(newNode);
        updateLayout();
        lastOperation = "ADD: " + value + " (height: " + getHeight() + ")";

        return true;
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
        if (right.getLeft() != null) {
            right.getLeft().setParent(node);
        }

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
        if (left.getRight() != null) {
            left.getRight().setParent(node);
        }

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

    public synchronized boolean contains(Object value) {
        highlightedPath.clear();
        operationsCount++;
        VisualTreeNode node = findNode(root, value);
        boolean found = node != null;
        lastOperation = "CONTAINS: " + value + " -> " + found;
        return found;
    }

    private VisualTreeNode findNode(VisualTreeNode node, Object value) {
        if (node == null) return null;

        highlightedPath.add(node);

        int cmp = ((Comparable) value).compareTo(node.getKey());
        if (cmp == 0) return node;
        if (cmp < 0) return findNode(node.getLeft(), value);
        return findNode(node.getRight(), value);
    }

    public synchronized Object first() {
        if (root == null) return null;
        VisualTreeNode node = root;
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        lastOperation = "FIRST: " + node.getKey();
        return node.getKey();
    }

    public synchronized Object last() {
        if (root == null) return null;
        VisualTreeNode node = root;
        while (node.getRight() != null) {
            node = node.getRight();
        }
        lastOperation = "LAST: " + node.getKey();
        return node.getKey();
    }

    public synchronized void clear() {
        if (root != null) {
            clearNode(root);
        }
        root = null;
        size = 0;
        operationsCount++;
        lastOperation = "CLEAR: removed all elements";
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
        int startY = 80;
        int levelHeight = 70;
        layoutNode(root, 50, width - 50, startY, levelHeight, 0);
    }

    private void layoutNode(VisualTreeNode node, double left, double right, double y, int levelHeight, int level) {
        if (node == null) return;

        double x = (left + right) / 2;
        node.setTarget(x, y);
        node.setLevel(level);

        double childWidth = (right - left) / 2;
        if (node.getLeft() != null) {
            layoutNode(node.getLeft(), left, x, y + levelHeight, levelHeight, level + 1);
        }
        if (node.getRight() != null) {
            layoutNode(node.getRight(), x, right, y + levelHeight, levelHeight, level + 1);
        }
    }

    public synchronized void update(double deltaTime) {
        if (root != null) {
            root.update(deltaTime);
        }
    }

    public int getSize() { return size; }
    public int getHeight() { return root == null ? 0 : root.getHeight(); }
    public int getOperationsCount() { return operationsCount; }
    public int getRotationCount() { return rotationCount; }
    public String getLastOperation() { return lastOperation; }
    public VisualTreeNode getRoot() { return root; }
    public List<VisualTreeNode> getHighlightedPath() { return highlightedPath; }

    public MemoryInfo getMemoryInfo() {
        long treeSetOverhead = 48;
        long treeMapOverhead = 32;
        long entriesOverhead = (long) size * 40;

        long elementsMemory = 0;
        if (root != null) {
            elementsMemory = calculateElementsMemory(root);
        }

        long totalUsed = treeSetOverhead + treeMapOverhead + entriesOverhead + elementsMemory;

        return new MemoryInfo(totalUsed, treeSetOverhead + treeMapOverhead, entriesOverhead, elementsMemory, getHeight());
    }

    private long calculateElementsMemory(VisualTreeNode node) {
        if (node == null) return 0;
        long mem = estimateObjectSize(node.getKey(), node.getKeyType());
        mem += calculateElementsMemory(node.getLeft());
        mem += calculateElementsMemory(node.getRight());
        return mem;
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
            long treeOverhead,
            long entriesOverhead,
            long elementsMemory,
            int height
    ) {
        public String formatTotal() { return formatBytes(totalUsed); }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }

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
}
