package game.ui;

import game.model.VisualArray;
import game.model.VisualElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ArrayDisplayPanel extends JPanel {
    private VisualArray array;
    private long lastTime = System.nanoTime();
    private double glowPhase = 0;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(10, 20, 15);
    private static final Color GRID_COLOR = new Color(30, 50, 40);
    private static final Color EMPTY_CELL = new Color(20, 35, 30);
    private static final Color TEXT_COLOR = new Color(200, 230, 210);
    private static final Color ACCENT_COLOR = new Color(100, 255, 150);

    public ArrayDisplayPanel(VisualArray array) {
        this.array = array;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 500));

        Timer timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            glowPhase += delta;
            array.update(delta);
            repaint();
        });
        timer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (memoryPanelBounds.contains(e.getPoint())) {
                    showMemoryDialog();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (memoryPanelBounds.contains(e.getPoint())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private void showMemoryDialog() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ArrayMemoryDialog dialog = new ArrayMemoryDialog(parentFrame, array);
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        drawBackground(g2d);
        drawGrid(g2d);
        drawElements(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(10, 25, 15),
                0, getHeight(), new Color(5, 15, 10)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(25, 45, 35));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < getWidth(); i += 50) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 50) {
            g2d.drawLine(0, i, getWidth(), i);
        }
    }

    private void drawGrid(Graphics2D g2d) {
        int cols = 10;
        int cellWidth = 70;
        int cellHeight = 70;
        int startX = 50;
        int startY = 100;
        int capacity = array.getCapacity();

        for (int i = 0; i < capacity; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * cellWidth;
            int y = startY + row * cellHeight;

            // Cell background - different shade for filled vs empty
            boolean isFilled = i < array.getSize();
            g2d.setColor(isFilled ? new Color(25, 45, 35) : EMPTY_CELL);
            g2d.fillRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 10, 10);

            // Cell border
            float glow = (float) (0.3 + 0.2 * Math.sin(glowPhase * 2 + i * 0.3));
            g2d.setColor(new Color(GRID_COLOR.getRed(), GRID_COLOR.getGreen(),
                    GRID_COLOR.getBlue(), (int)(glow * 255)));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 10, 10);

            // Index number
            g2d.setColor(new Color(80, 120, 100));
            g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
            g2d.drawString("[" + i + "]", x + 5, y + 15);
        }

        // Draw "FIXED SIZE" indicator
        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString("FIXED SIZE: " + capacity, startX, startY - 10);
    }

    private void drawElements(Graphics2D g2d) {
        List<VisualElement> elements = array.getElements();

        for (VisualElement element : elements) {
            double x = element.getX();
            double y = element.getY();
            double alpha = element.getAlpha();
            double scale = element.getScale();

            if (alpha <= 0) continue;

            int size = (int) (60 * scale);
            int ex = (int) (x - size / 2.0);
            int ey = (int) (y - size / 2.0);

            // Glow effect
            for (int i = 3; i > 0; i--) {
                g2d.setColor(new Color(element.getColor().getRed(), element.getColor().getGreen(),
                        element.getColor().getBlue(), (int)(alpha * 20 * i)));
                g2d.fillRoundRect(ex - i * 3, ey - i * 3, size + i * 6, size + i * 6, 15, 15);
            }

            // Main element
            Color fillColor = new Color(
                    element.getColor().getRed(),
                    element.getColor().getGreen(),
                    element.getColor().getBlue(),
                    (int) (alpha * 255)
            );

            GradientPaint gp = new GradientPaint(ex, ey, fillColor, ex, ey + size, fillColor.darker());
            g2d.setPaint(gp);
            g2d.fillRoundRect(ex, ey, size, size, 12, 12);

            // Border
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 150)));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(ex, ey, size, size, 12, 12);

            // Value text
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 11));
            String value = element.getDisplayValue();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = ex + (size - fm.stringWidth(value)) / 2;
            int textY = ey + size / 2 + 4;
            g2d.drawString(value, textX, textY);

            // Type label
            g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
            g2d.setColor(new Color(200, 200, 200, (int)(alpha * 180)));
            String type = element.getType().toUpperCase();
            if (array.isPrimitive()) {
                type = type.toLowerCase(); // primitive types in lowercase
            }
            textX = ex + (size - fm.stringWidth(type)) / 2 + 5;
            g2d.drawString(type, textX, ey + 12);
        }
    }

    private void drawHeader(Graphics2D g2d) {
        String title = array.isPrimitive() ?
                array.getElementType().toLowerCase() + "[] ARRAY" :
                array.getElementType() + "[] ARRAY";

        g2d.setFont(new Font("Consolas", Font.BOLD, 28));

        g2d.setColor(new Color(100, 255, 150, 50));
        g2d.drawString(title.toUpperCase() + " VISUALIZER", 52, 52);

        g2d.setColor(ACCENT_COLOR);
        g2d.drawString(title.toUpperCase() + " VISUALIZER", 50, 50);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(new Color(150, 200, 170));
        String subtitle = array.isPrimitive() ?
                "[ Primitive Array - Direct Values ]" :
                "[ Object Array - References ]";
        g2d.drawString(subtitle, 50, 70);

        float lineGlow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 3));
        g2d.setColor(new Color(100, 255, 150, (int)(lineGlow * 150)));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(50, 80, 400, 80);
    }

    private void drawStats(Graphics2D g2d) {
        int boxWidth = 195;
        int x = getWidth() - boxWidth - 5;
        int y = 30;

        g2d.setColor(new Color(20, 35, 28, 200));
        g2d.fillRoundRect(x - 10, y - 20, boxWidth, 60, 10, 10);
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x - 10, y - 20, boxWidth, 60, 10, 10);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("SIZE: " + array.getSize() + " / " + array.getCapacity(), x, y);
        g2d.drawString("OPERATIONS: " + array.getOperationsCount(), x, y + 20);

        drawMemoryPanel(g2d);
        drawCapacityBar(g2d);
    }

    private void drawCapacityBar(Graphics2D g2d) {
        int size = array.getSize();
        int capacity = array.getCapacity();

        int barWidth = 700;
        int barHeight = 20;
        int x = 50;
        int y = getHeight() - 50;

        // Background
        g2d.setColor(new Color(30, 50, 40));
        g2d.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        // Size fill
        int fillWidth = (int) ((double) size / capacity * barWidth);
        GradientPaint gp = new GradientPaint(x, y, ACCENT_COLOR, x + fillWidth, y, new Color(150, 255, 200));
        g2d.setPaint(gp);
        g2d.fillRoundRect(x, y, fillWidth, barHeight, 10, 10);

        // Border
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, barWidth, barHeight, 10, 10);

        // Text
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Consolas", Font.BOLD, 12));
        String text = String.format("USED: %d / %d (%.0f%%) - CANNOT RESIZE!",
                size, capacity, (double) size / capacity * 100);
        g2d.drawString(text, x + 10, y + 15);
    }

    private void drawMemoryPanel(Graphics2D g2d) {
        var memInfo = array.getMemoryInfo();

        int panelWidth = 200;
        int panelHeight = 130;
        int x = getWidth() - panelWidth - 10;
        int y = 100;

        memoryPanelBounds.setBounds(x, y, panelWidth, panelHeight);

        g2d.setColor(new Color(15, 30, 22, 230));
        g2d.fillRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 2));
        g2d.setColor(new Color(100, 255, 150, (int)(glow * 150)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(new Color(100, 255, 150));
        g2d.drawString("MEMORY USAGE", x + 10, y + 18);

        g2d.setColor(new Color(100, 255, 150, 50));
        g2d.drawLine(x + 10, y + 25, x + panelWidth - 10, y + 25);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        int textY = y + 42;
        int lineHeight = 16;

        g2d.setColor(new Color(100, 255, 200));
        g2d.drawString("TOTAL:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(memInfo.formatTotal(), x + 100, textY);

        textY += lineHeight;
        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString("ARRAY HDR:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("16 B", x + 100, textY);

        textY += lineHeight;
        if (memInfo.isPrimitive()) {
            g2d.setColor(new Color(0, 200, 255));
            g2d.drawString("VALUES:", x + 10, textY);
            g2d.setColor(TEXT_COLOR);
            g2d.drawString(memInfo.formatAllocated(), x + 100, textY);
        } else {
            g2d.setColor(new Color(0, 200, 255));
            g2d.drawString("ELEMENTS:", x + 10, textY);
            g2d.setColor(TEXT_COLOR);
            g2d.drawString(memInfo.formatElements(), x + 100, textY);
        }

        textY += lineHeight;
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawString("UNUSED:", x + 10, textY);
        g2d.setColor(new Color(255, 150, 150));
        g2d.drawString(memInfo.formatWasted(), x + 100, textY);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 255, 200, (int)(100 + 50 * Math.sin(glowPhase * 3))));
        g2d.drawString("[CLICK FOR DETAILS]", x + 10, y + panelHeight - 5);
    }

    public VisualArray getArray() {
        return array;
    }
}
