package game.model;

import java.awt.*;

public class VisualNode {
    private Object value;
    private String type;
    private int index;
    private Color color;
    private double x, y;
    private double targetX, targetY;
    private double alpha = 0.0;
    private double scale = 0.0;
    private boolean removing = false;

    private static final Color INT_COLOR = new Color(0, 200, 255);
    private static final Color DOUBLE_COLOR = new Color(255, 100, 200);
    private static final Color STRING_COLOR = new Color(100, 255, 150);
    private static final Color BOOLEAN_COLOR = new Color(255, 200, 50);
    private static final Color CHAR_COLOR = new Color(200, 100, 255);
    private static final Color OBJECT_COLOR = new Color(255, 150, 100);

    public VisualNode(Object value, String type, int index) {
        this.value = value;
        this.type = type;
        this.index = index;
        this.color = getColorForType(type);
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

    public void setIndex(int index) {
        this.index = index;
    }

    public void update(double deltaTime) {
        double speed = 6.0;
        x += (targetX - x) * speed * deltaTime;
        y += (targetY - y) * speed * deltaTime;

        if (!removing) {
            alpha = Math.min(1.0, alpha + deltaTime * 3);
            scale = Math.min(1.0, scale + deltaTime * 5);
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
    public int getIndex() { return index; }
    public Color getColor() { return color; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAlpha() { return alpha; }
    public double getScale() { return scale; }
    public boolean isRemoving() { return removing; }

    public String getDisplayValue() {
        if (value instanceof String) {
            String s = (String) value;
            return s.length() > 5 ? s.substring(0, 4) + ".." : "\"" + s + "\"";
        }
        return String.valueOf(value);
    }
}
