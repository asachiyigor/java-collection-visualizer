package game.ui;

import game.model.VisualArrayList;
import game.model.VisualArrayList.AnimationState;
import game.model.VisualArrayList.AnimationStep;
import game.model.VisualElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Map;
import game.ui.ThemeManager;

public class ArrayPanel extends JPanel {
    private VisualArrayList arrayList;
    private long lastTime = System.nanoTime();
    private double glowPhase = 0;
    private Rectangle memoryPanelBounds = new Rectangle();

    // Colors
    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color GRID_COLOR = ThemeManager.get().getGridColor();
    private static final Color GRID_GLOW = new Color(40, 60, 100);
    private static Color EMPTY_CELL = ThemeManager.get().getEmptyCellColor();
    private static final Color CAPACITY_INDICATOR = new Color(255, 100, 100, 100);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color ACCENT_COLOR = new Color(0, 200, 255);

    public ArrayPanel(VisualArrayList arrayList) {
        this.arrayList = arrayList;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 500));

        Timer timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            glowPhase += delta;
            arrayList.update(delta);
            repaint();
        });
        timer.start();

        // Click listener for memory panel
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (memoryPanelBounds.contains(e.getPoint())) {
                    showMemoryDialog();
                }
            }
        });

        // Cursor change on hover
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
        MemoryInfoDialog dialog = new MemoryInfoDialog(parentFrame, arrayList);
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
        drawCapacityIndicator(g2d);
        drawElements(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        if (arrayList.isAnimating()) {
            drawAnimationOverlay(g2d);
            drawAnimationInfo(g2d);
        }

        if (arrayList.wasJustResized()) {
            drawResizeNotification(g2d);
        }

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        // Gradient background
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(10, 15, 25),
                0, getHeight(), new Color(5, 10, 20)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Grid lines (subtle)
        g2d.setColor(new Color(25, 35, 50));
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
        int capacity = arrayList.getCapacity();

        for (int i = 0; i < capacity; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * cellWidth;
            int y = startY + row * cellHeight;

            // Empty cell background
            g2d.setColor(EMPTY_CELL);
            g2d.fillRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 10, 10);

            // Cell border with glow effect
            float glow = (float) (0.3 + 0.2 * Math.sin(glowPhase * 2 + i * 0.3));
            g2d.setColor(new Color(GRID_COLOR.getRed(), GRID_COLOR.getGreen(), GRID_COLOR.getBlue(), (int)(glow * 255)));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 10, 10);

            // Index number
            g2d.setColor(new Color(80, 100, 130));
            g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
            g2d.drawString(String.valueOf(i), x + 5, y + 15);
        }
    }

    private void drawCapacityIndicator(Graphics2D g2d) {
        int size = arrayList.getSize();
        int capacity = arrayList.getCapacity();

        int barWidth = 700;
        int barHeight = 20;
        int x = 50;
        int y = getHeight() - 50;

        // Background
        g2d.setColor(new Color(30, 40, 55));
        g2d.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        // Size fill
        int fillWidth = (int) ((double) size / capacity * barWidth);
        GradientPaint gp = new GradientPaint(
                x, y, ACCENT_COLOR,
                x + fillWidth, y, new Color(100, 255, 200)
        );
        g2d.setPaint(gp);
        g2d.fillRoundRect(x, y, fillWidth, barHeight, 10, 10);

        // Border
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, barWidth, barHeight, 10, 10);

        // Text
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Consolas", Font.BOLD, 12));
        String text = String.format("SIZE: %d / CAPACITY: %d (%.0f%%)", size, capacity, (double) size / capacity * 100);
        g2d.drawString(text, x + 10, y + 15);
    }

    private void drawElements(Graphics2D g2d) {
        List<VisualElement> elements = arrayList.getElements();

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
            Color glowColor = new Color(
                    element.getColor().getRed(),
                    element.getColor().getGreen(),
                    element.getColor().getBlue(),
                    (int) (alpha * 80)
            );
            for (int i = 3; i > 0; i--) {
                g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(alpha * 20 * i)));
                g2d.fillRoundRect(ex - i * 3, ey - i * 3, size + i * 6, size + i * 6, 15, 15);
            }

            // Main element
            Color fillColor = new Color(
                    element.getColor().getRed(),
                    element.getColor().getGreen(),
                    element.getColor().getBlue(),
                    (int) (alpha * 255)
            );

            // Gradient fill
            GradientPaint gp = new GradientPaint(
                    ex, ey, fillColor,
                    ex, ey + size, fillColor.darker()
            );
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
            textX = ex + (size - fm.stringWidth(type)) / 2 + 5;
            g2d.drawString(type, textX, ey + 12);
        }
    }

    private void drawHeader(Graphics2D g2d) {
        // Title with glow
        g2d.setFont(new Font("Consolas", Font.BOLD, 28));

        // Glow effect
        g2d.setColor(new Color(0, 200, 255, 50));
        g2d.drawString("ARRAYLIST VISUALIZER", 52, 52);

        g2d.setColor(ACCENT_COLOR);
        g2d.drawString("ARRAYLIST VISUALIZER", 50, 50);

        // Subtitle
        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(new Color(150, 170, 200));
        g2d.drawString("[ Data Structure Laboratory ]", 50, 70);

        // Decorative line
        float lineGlow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 3));
        g2d.setColor(new Color(0, 200, 255, (int)(lineGlow * 150)));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(50, 80, 350, 80);
    }

    private void drawStats(Graphics2D g2d) {
        int boxWidth = 195;
        int x = getWidth() - boxWidth - 5;
        int y = 30;

        // Stats box
        g2d.setColor(new Color(20, 30, 45, 200));
        g2d.fillRoundRect(x - 10, y - 20, boxWidth, 80, 10, 10);
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x - 10, y - 20, boxWidth, 80, 10, 10);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("OPERATIONS: " + arrayList.getOperationsCount(), x, y);
        g2d.drawString("RESIZES: " + arrayList.getResizeCount(), x, y + 20);

        // Resize formula (smaller font to fit)
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString("cap = old + (old >> 1)", x, y + 45);

        // Draw memory panel
        drawMemoryPanel(g2d);
    }

    private void drawMemoryPanel(Graphics2D g2d) {
        var memInfo = arrayList.getMemoryInfo();

        int panelWidth = 200;
        int panelHeight = 140;
        int x = getWidth() - panelWidth - 10;
        int y = 100;

        // Save bounds for click detection
        memoryPanelBounds.setBounds(x, y, panelWidth, panelHeight);

        // Panel background
        g2d.setColor(new Color(15, 25, 40, 230));
        g2d.fillRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        // Panel border with glow
        float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 2));
        g2d.setColor(new Color(0, 255, 200, (int)(glow * 150)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        // Title with click indicator
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(new Color(0, 255, 200));
        g2d.drawString("MEMORY USAGE", x + 10, y + 18);

        // Click indicator (pulsing)
        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 200, 255, (int)(100 + 50 * Math.sin(glowPhase * 3))));
        g2d.drawString("[CLICK FOR DETAILS]", x + 10, y + panelHeight - 5);

        // Separator line
        g2d.setColor(new Color(0, 255, 200, 50));
        g2d.drawLine(x + 10, y + 25, x + panelWidth - 10, y + 25);

        // Memory stats
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        int textY = y + 42;
        int lineHeight = 16;

        // Total memory used
        g2d.setColor(new Color(100, 255, 200));
        g2d.drawString("TOTAL USED:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(memInfo.formatTotal(), x + 100, textY);

        // Elements memory
        textY += lineHeight;
        g2d.setColor(new Color(0, 200, 255));
        g2d.drawString("ELEMENTS:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(memInfo.formatElements(), x + 100, textY);

        // Array overhead
        textY += lineHeight;
        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString("ARRAY[]:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(formatBytes(memInfo.arrayOverhead()), x + 100, textY);

        // Wasted memory
        textY += lineHeight;
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawString("WASTED:", x + 10, textY);
        g2d.setColor(new Color(255, 150, 150));
        g2d.drawString(memInfo.formatWasted(), x + 100, textY);

        // Memory bar visualization
        textY += lineHeight + 5;
        int barWidth = panelWidth - 20;
        int barHeight = 12;
        int barX = x + 10;

        // Background bar
        g2d.setColor(new Color(30, 40, 55));
        g2d.fillRoundRect(barX, textY, barWidth, barHeight, 4, 4);

        // Calculate proportions
        long total = memInfo.totalAllocated();
        if (total > 0) {
            double elementsRatio = (double) memInfo.elementsMemory() / total;
            double arrayRatio = (double) memInfo.arrayOverhead() / total;
            double wastedRatio = (double) memInfo.wastedMemory() / total;

            int elemWidth = (int) (barWidth * elementsRatio);
            int arrayWidth = (int) (barWidth * arrayRatio);
            int wastedWidth = (int) (barWidth * wastedRatio);

            // Elements portion (cyan)
            g2d.setColor(new Color(0, 200, 255));
            g2d.fillRoundRect(barX, textY, elemWidth, barHeight, 4, 4);

            // Array overhead portion (yellow)
            g2d.setColor(new Color(255, 200, 100));
            g2d.fillRect(barX + elemWidth, textY, arrayWidth, barHeight);

            // Wasted portion (red)
            if (wastedWidth > 0) {
                g2d.setColor(new Color(255, 80, 80, 150));
                g2d.fillRoundRect(barX + elemWidth + arrayWidth, textY,
                        barWidth - elemWidth - arrayWidth, barHeight, 4, 4);
            }
        }

        // Bar border
        g2d.setColor(new Color(100, 120, 150));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(barX, textY, barWidth, barHeight, 4, 4);
    }

    private void drawResizeNotification(Graphics2D g2d) {
        String text = "CAPACITY EXPANDED!";

        // Pulsing effect
        float pulse = (float) (0.7 + 0.3 * Math.sin(glowPhase * 8));

        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2 + 150;

        // Glow
        g2d.setColor(new Color(255, 100, 100, (int)(pulse * 100)));
        g2d.drawString(text, x + 2, y + 2);

        g2d.setColor(new Color(255, (int)(150 * pulse), (int)(150 * pulse)));
        g2d.drawString(text, x, y);
    }

    private void drawAnimationOverlay(Graphics2D g2d) {
        AnimationStep step = arrayList.getCurrentStep();
        if (step == null) return;

        List<VisualElement> elements = arrayList.getElements();
        int cols = 10;
        int cellWidth = 70;
        int cellHeight = 70;
        int startX = 50;
        int startY = 100;

        for (Map.Entry<Integer, AnimationState> entry : step.getIndexStates().entrySet()) {
            int idx = entry.getKey();
            if (idx < 0 || idx >= elements.size()) continue;

            int col = idx % cols;
            int row = idx / cols;
            int x = startX + col * cellWidth + 2;
            int y = startY + row * cellHeight + 2;
            int w = cellWidth - 4;
            int h = cellHeight - 4;

            Color overlay;
            float pulse = (float)(0.6 + 0.4 * Math.sin(glowPhase * 6));
            switch (entry.getValue()) {
                case COMPARING -> overlay = new Color(255, 220, 50, (int)(pulse * 120));
                case SWAPPING -> overlay = new Color(255, 140, 50, (int)(pulse * 150));
                case FOUND -> overlay = new Color(50, 255, 100, (int)(pulse * 160));
                case ELIMINATED -> overlay = new Color(100, 100, 100, 80);
                case SORTED -> overlay = new Color(50, 200, 100, 60);
                case CURRENT -> overlay = new Color(0, 200, 255, (int)(pulse * 140));
                default -> overlay = new Color(255, 255, 255, 30);
            }

            g2d.setColor(overlay);
            g2d.fillRoundRect(x, y, w, h, 10, 10);

            // Border glow
            Color borderColor = switch (entry.getValue()) {
                case COMPARING -> new Color(255, 220, 50, (int)(pulse * 200));
                case SWAPPING -> new Color(255, 140, 50, (int)(pulse * 220));
                case FOUND -> new Color(50, 255, 100, (int)(pulse * 220));
                case CURRENT -> new Color(0, 200, 255, (int)(pulse * 220));
                default -> new Color(0, 0, 0, 0);
            };
            if (borderColor.getAlpha() > 0) {
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(x, y, w, h, 10, 10);
            }
        }
    }

    private void drawAnimationInfo(Graphics2D g2d) {
        AnimationStep step = arrayList.getCurrentStep();
        if (step == null) return;

        int barHeight = 36;
        int y = getHeight() - 95;

        // Banner background
        g2d.setColor(new Color(20, 25, 40, 220));
        g2d.fillRoundRect(50, y, 700, barHeight, 8, 8);

        float glow = (float)(0.6 + 0.4 * Math.sin(glowPhase * 3));
        g2d.setColor(new Color(0, 200, 255, (int)(glow * 150)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(50, y, 700, barHeight, 8, 8);

        // Step description
        g2d.setFont(new Font("Consolas", Font.BOLD, 13));
        g2d.setColor(new Color(255, 255, 255));
        g2d.drawString(step.getDescription(), 65, y + 22);

        // Step counter
        String counter = "Step " + (arrayList.getCurrentStepIndex() + 1) + "/" + arrayList.getTotalSteps();
        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(new Color(150, 200, 255));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(counter, 750 - fm.stringWidth(counter) - 10, y + 22);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
