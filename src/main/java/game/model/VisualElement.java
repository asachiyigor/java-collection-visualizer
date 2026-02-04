package game.model;

import java.awt.*;

public class VisualElement {
    private Object value;
    private String type;
    private Color color;
    private double x, y;
    private double targetX, targetY;
    private double alpha = 0.0;
    private double scale = 0.0;
    private boolean removing = false;
    private int index = 0;

    private static final Color INT_COLOR = new Color(0, 200, 255);      // Cyan
    private static final Color DOUBLE_COLOR = new Color(255, 100, 200); // Pink
    private static final Color STRING_COLOR = new Color(100, 255, 150); // Green
    private static final Color BOOLEAN_COLOR = new Color(255, 200, 50); // Yellow
    private static final Color CHAR_COLOR = new Color(200, 100, 255);   // Purple
    private static final Color OBJECT_COLOR = new Color(255, 150, 100); // Orange
    private static final Color NULL_COLOR = new Color(128, 128, 128);   // Gray for null

    public VisualElement(Object value, String type) {
        this.value = value;
        this.type = type;
        this.color = (value == null) ? NULL_COLOR : getColorForType(type);
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
    }

    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void update(double deltaTime) {
        double speed = 8.0;
        x += (targetX - x) * speed * deltaTime;
        y += (targetY - y) * speed * deltaTime;

        if (!removing) {
            alpha = Math.min(1.0, alpha + deltaTime * 4);
            scale = Math.min(1.0, scale + deltaTime * 6);
        } else {
            alpha = Math.max(0.0, alpha - deltaTime * 4);
            scale = Math.max(0.0, scale - deltaTime * 6);
        }
    }

    public boolean isFullyRemoved() {
        return removing && alpha <= 0.01;
    }

    public void startRemoving() {
        this.removing = true;
    }

    // Getters
    public Object getValue() { return value; }
    public String getType() { return type; }
    public Color getColor() { return color; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAlpha() { return alpha; }
    public double getScale() { return scale; }
    public boolean isRemoving() { return removing; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getDisplayValue() {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            String s = (String) value;
            return s.length() > 6 ? s.substring(0, 5) + "..." : "\"" + s + "\"";
        }
        return String.valueOf(value);
    }
}
