package game.model;

import java.awt.*;

public class VisualEntry {
    private Object key;
    private Object value;
    private String keyType;
    private String valueType;
    private Color keyColor;
    private Color valueColor;
    private double x, y;
    private double targetX, targetY;
    private double alpha = 0.0;
    private double scale = 0.0;
    private boolean removing = false;
    private int bucketIndex = -1;
    private int chainIndex = 0;
    private boolean highlighted = false;
    private long highlightTime = 0;

    private static final Color INT_COLOR = new Color(0, 200, 255);
    private static final Color DOUBLE_COLOR = new Color(255, 100, 200);
    private static final Color STRING_COLOR = new Color(100, 255, 150);
    private static final Color BOOLEAN_COLOR = new Color(255, 200, 50);
    private static final Color CHAR_COLOR = new Color(200, 100, 255);
    private static final Color OBJECT_COLOR = new Color(255, 150, 100);

    public VisualEntry(Object key, Object value, String keyType, String valueType) {
        this.key = key;
        this.value = value;
        this.keyType = keyType;
        this.valueType = valueType;
        this.keyColor = getColorForType(keyType);
        this.valueColor = getColorForType(valueType);
    }

    private Color getColorForType(String type) {
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
        double speed = 8.0;
        x += (targetX - x) * speed * deltaTime;
        y += (targetY - y) * speed * deltaTime;

        if (removing) {
            alpha -= deltaTime * 4;
            scale -= deltaTime * 3;
            if (alpha < 0) alpha = 0;
            if (scale < 0) scale = 0;
        } else {
            if (alpha < 1.0) alpha += deltaTime * 4;
            if (scale < 1.0) scale += deltaTime * 6;
            if (alpha > 1.0) alpha = 1.0;
            if (scale > 1.0) scale = 1.0;
        }

        if (highlighted && System.currentTimeMillis() - highlightTime > 500) {
            highlighted = false;
        }
    }

    public void setHighlighted(boolean h) {
        this.highlighted = h;
        if (h) highlightTime = System.currentTimeMillis();
    }

    public boolean isHighlighted() { return highlighted; }

    public void startRemoving() {
        removing = true;
    }

    public boolean isRemoving() { return removing; }
    public boolean isFullyRemoved() { return removing && alpha <= 0.01; }

    public Object getKey() { return key; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    public String getKeyType() { return keyType; }
    public String getValueType() { return valueType; }
    public Color getKeyColor() { return keyColor; }
    public Color getValueColor() { return valueColor; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAlpha() { return alpha; }
    public double getScale() { return scale; }
    public int getBucketIndex() { return bucketIndex; }
    public void setBucketIndex(int idx) { this.bucketIndex = idx; }
    public int getChainIndex() { return chainIndex; }
    public void setChainIndex(int idx) { this.chainIndex = idx; }

    public String getDisplayKey() {
        if (key == null) return "null";
        if (keyType.equalsIgnoreCase("string")) {
            return "\"" + key + "\"";
        } else if (keyType.equalsIgnoreCase("char")) {
            return "'" + key + "'";
        }
        return key.toString();
    }

    public String getDisplayValue() {
        if (value == null) return "null";
        if (valueType.equalsIgnoreCase("string")) {
            return "\"" + value + "\"";
        } else if (valueType.equalsIgnoreCase("char")) {
            return "'" + value + "'";
        }
        return value.toString();
    }

    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }
}
