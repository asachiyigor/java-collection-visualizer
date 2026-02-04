package game.model;

import java.awt.*;

public class VisualTreeNode {
    private Object key;
    private Object value;  // null for TreeSet, used for TreeMap
    private String keyType;
    private String valueType;
    private Color color;
    private boolean isRed;

    private VisualTreeNode parent;
    private VisualTreeNode left;
    private VisualTreeNode right;

    private double x, y;
    private double targetX, targetY;
    private double alpha = 0.0;
    private double scale = 0.0;
    private boolean removing = false;
    private int level = 0;

    private static final Color INT_COLOR = new Color(0, 200, 255);
    private static final Color DOUBLE_COLOR = new Color(255, 100, 200);
    private static final Color STRING_COLOR = new Color(100, 255, 150);
    private static final Color BOOLEAN_COLOR = new Color(255, 200, 50);
    private static final Color CHAR_COLOR = new Color(200, 100, 255);
    private static final Color OBJECT_COLOR = new Color(255, 150, 100);

    public static final Color RED_NODE_COLOR = new Color(255, 80, 80);
    public static final Color BLACK_NODE_COLOR = new Color(60, 60, 60);

    public VisualTreeNode(Object key, String keyType) {
        this(key, null, keyType, null);
    }

    public VisualTreeNode(Object key, Object value, String keyType, String valueType) {
        this.key = key;
        this.value = value;
        this.keyType = keyType;
        this.valueType = valueType;
        this.isRed = true;  // New nodes are red in Red-Black tree
        this.color = getColorForType(keyType);
    }

    private Color getColorForType(String type) {
        if (type == null) return OBJECT_COLOR;
        return switch (type.toLowerCase()) {
            case "int", "integer" -> INT_COLOR;
            case "double" -> DOUBLE_COLOR;
            case "string" -> STRING_COLOR;
            case "boolean" -> BOOLEAN_COLOR;
            case "char", "character" -> CHAR_COLOR;
            default -> OBJECT_COLOR;
        };
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
    }

    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void update(double deltaTime) {
        double speed = 6.0;
        x += (targetX - x) * speed * deltaTime;
        y += (targetY - y) * speed * deltaTime;

        if (removing) {
            alpha -= deltaTime * 4;
            scale -= deltaTime * 3;
            if (alpha < 0) alpha = 0;
            if (scale < 0) scale = 0;
        } else {
            if (alpha < 1.0) alpha += deltaTime * 3;
            if (scale < 1.0) scale += deltaTime * 5;
            if (alpha > 1.0) alpha = 1.0;
            if (scale > 1.0) scale = 1.0;
        }

        if (left != null) left.update(deltaTime);
        if (right != null) right.update(deltaTime);
    }

    public void startRemoving() {
        removing = true;
    }

    public boolean isRemoving() { return removing; }
    public boolean isFullyRemoved() { return removing && alpha <= 0.01; }

    // Tree structure
    public VisualTreeNode getParent() { return parent; }
    public void setParent(VisualTreeNode parent) { this.parent = parent; }
    public VisualTreeNode getLeft() { return left; }
    public void setLeft(VisualTreeNode left) {
        this.left = left;
        if (left != null) left.setParent(this);
    }
    public VisualTreeNode getRight() { return right; }
    public void setRight(VisualTreeNode right) {
        this.right = right;
        if (right != null) right.setParent(this);
    }

    // Red-Black tree color
    public boolean isRed() { return isRed; }
    public void setRed(boolean red) { this.isRed = red; }
    public Color getNodeColor() { return isRed ? RED_NODE_COLOR : BLACK_NODE_COLOR; }

    // Values
    public Object getKey() { return key; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    public String getKeyType() { return keyType; }
    public String getValueType() { return valueType; }
    public Color getTypeColor() { return color; }

    // Position
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAlpha() { return alpha; }
    public double getScale() { return scale; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getDisplayKey() {
        if (key == null) return "null";
        if (keyType.equalsIgnoreCase("string")) {
            String s = key.toString();
            return s.length() > 6 ? s.substring(0, 5) + ".." : "\"" + s + "\"";
        } else if (keyType.equalsIgnoreCase("char")) {
            return "'" + key + "'";
        }
        return key.toString();
    }

    public String getDisplayValue() {
        if (value == null) return null;
        if (valueType == null) return value.toString();
        if (valueType.equalsIgnoreCase("string")) {
            String s = value.toString();
            return s.length() > 6 ? s.substring(0, 5) + ".." : "\"" + s + "\"";
        } else if (valueType.equalsIgnoreCase("char")) {
            return "'" + value + "'";
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    public int compareTo(Object otherKey) {
        if (key instanceof Comparable && otherKey instanceof Comparable) {
            return ((Comparable<Object>) key).compareTo(otherKey);
        }
        return key.toString().compareTo(otherKey.toString());
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public int getHeight() {
        int leftHeight = (left != null) ? left.getHeight() : 0;
        int rightHeight = (right != null) ? right.getHeight() : 0;
        return 1 + Math.max(leftHeight, rightHeight);
    }

    public int getSize() {
        int leftSize = (left != null) ? left.getSize() : 0;
        int rightSize = (right != null) ? right.getSize() : 0;
        return 1 + leftSize + rightSize;
    }
}
