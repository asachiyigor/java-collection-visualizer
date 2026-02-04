package game.ui;

import game.model.VisualLinkedList;
import game.model.VisualNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import game.ui.ThemeManager;

public class LinkedListPanel extends JPanel {
    private VisualLinkedList linkedList;
    private long lastTime = System.nanoTime();
    private double glowPhase = 0;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color GRID_COLOR = ThemeManager.get().getGridColor();
    private static Color EMPTY_CELL = ThemeManager.get().getEmptyCellColor();
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color ACCENT_COLOR = new Color(255, 100, 150);
    private static final Color LINK_COLOR = new Color(255, 200, 100);

    public LinkedListPanel(VisualLinkedList linkedList) {
        this.linkedList = linkedList;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 500));

        Timer timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            glowPhase += delta;
            linkedList.update(delta);
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
        LinkedListMemoryDialog dialog = new LinkedListMemoryDialog(parentFrame, linkedList);
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        drawBackground(g2d);
        drawNodes(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(15, 10, 20),
                0, getHeight(), new Color(10, 5, 15)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(35, 25, 45));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < getWidth(); i += 50) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 50) {
            g2d.drawLine(0, i, getWidth(), i);
        }
    }

    private void drawNodes(Graphics2D g2d) {
        List<VisualNode> nodes = linkedList.getNodes();

        // Draw links first (behind nodes)
        for (int i = 0; i < nodes.size() - 1; i++) {
            VisualNode current = nodes.get(i);
            VisualNode next = nodes.get(i + 1);

            if (current.getAlpha() > 0.1 && next.getAlpha() > 0.1) {
                drawLink(g2d, current, next);
            }
        }

        // Draw nodes
        for (VisualNode node : nodes) {
            drawNode(g2d, node);
        }
    }

    private void drawLink(Graphics2D g2d, VisualNode from, VisualNode to) {
        double alpha = Math.min(from.getAlpha(), to.getAlpha());

        int x1 = (int) from.getX() + 50;
        int y1 = (int) from.getY();
        int x2 = (int) to.getX() - 50;
        int y2 = (int) to.getY();

        // If on different rows
        if (Math.abs(y1 - y2) > 20) {
            // Draw curved path
            g2d.setColor(new Color(LINK_COLOR.getRed(), LINK_COLOR.getGreen(), LINK_COLOR.getBlue(), (int)(alpha * 150)));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int midX = (x1 + x2) / 2;
            g2d.drawLine(x1, y1, x1 + 30, y1);
            g2d.drawLine(x1 + 30, y1, x1 + 30, y2);
            g2d.drawLine(x1 + 30, y2, x2, y2);

            // Arrow at end
            drawArrow(g2d, x2 - 10, y2, x2, y2, alpha);
        } else {
            // Straight line with arrow
            g2d.setColor(new Color(LINK_COLOR.getRed(), LINK_COLOR.getGreen(), LINK_COLOR.getBlue(), (int)(alpha * 150)));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);

            // Arrow
            drawArrow(g2d, x1, y1, x2, y2, alpha);
        }

        // "next" label
        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(200, 180, 150, (int)(alpha * 180)));
        g2d.drawString("next", (x1 + x2) / 2 - 10, Math.min(y1, y2) - 5);
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, double alpha) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 8;

        int ax1 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int ay1 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int ax2 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int ay2 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.setColor(new Color(LINK_COLOR.getRed(), LINK_COLOR.getGreen(), LINK_COLOR.getBlue(), (int)(alpha * 200)));
        g2d.fillPolygon(new int[]{x2, ax1, ax2}, new int[]{y2, ay1, ay2}, 3);
    }

    private void drawNode(Graphics2D g2d, VisualNode node) {
        double x = node.getX();
        double y = node.getY();
        double alpha = node.getAlpha();
        double scale = node.getScale();

        if (alpha <= 0) return;

        int width = (int) (100 * scale);
        int height = (int) (60 * scale);
        int ex = (int) (x - width / 2.0);
        int ey = (int) (y - height / 2.0);

        // Glow effect
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(node.getColor().getRed(), node.getColor().getGreen(),
                    node.getColor().getBlue(), (int)(alpha * 15 * i)));
            g2d.fillRoundRect(ex - i * 3, ey - i * 3, width + i * 6, height + i * 6, 12, 12);
        }

        // Node background with sections
        GradientPaint gp = new GradientPaint(
                ex, ey, new Color(node.getColor().getRed(), node.getColor().getGreen(),
                        node.getColor().getBlue(), (int)(alpha * 200)),
                ex, ey + height, node.getColor().darker()
        );
        g2d.setPaint(gp);
        g2d.fillRoundRect(ex, ey, width, height, 10, 10);

        // Dividers for prev | data | next structure
        g2d.setColor(new Color(0, 0, 0, (int)(alpha * 100)));
        g2d.drawLine(ex + 20, ey + 5, ex + 20, ey + height - 5);
        g2d.drawLine(ex + width - 20, ey + 5, ex + width - 20, ey + height - 5);

        // Border
        g2d.setColor(new Color(255, 255, 255, (int)(alpha * 150)));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(ex, ey, width, height, 10, 10);

        // Prev/Next indicators
        g2d.setFont(new Font("Consolas", Font.PLAIN, 8));
        g2d.setColor(new Color(200, 200, 200, (int)(alpha * 150)));
        g2d.drawString("<", ex + 7, ey + height / 2 + 3);
        g2d.drawString(">", ex + width - 13, ey + height / 2 + 3);

        // Value
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
        String value = node.getDisplayValue();
        FontMetrics fm = g2d.getFontMetrics();
        int textX = ex + (width - fm.stringWidth(value)) / 2;
        int textY = ey + height / 2 + 4;
        g2d.drawString(value, textX, textY);

        // Type label
        g2d.setFont(new Font("Consolas", Font.PLAIN, 8));
        g2d.setColor(new Color(200, 200, 200, (int)(alpha * 180)));
        String type = node.getType().toUpperCase();
        textX = ex + (width - fm.stringWidth(type)) / 2 + 5;
        g2d.drawString(type, textX, ey + 12);

        // Index
        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 150, 150, (int)(alpha * 150)));
        g2d.drawString("[" + node.getIndex() + "]", ex + width / 2 - 8, ey + height - 5);
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 28));

        g2d.setColor(new Color(255, 100, 150, 50));
        g2d.drawString("LINKEDLIST VISUALIZER", 52, 52);

        g2d.setColor(ACCENT_COLOR);
        g2d.drawString("LINKEDLIST VISUALIZER", 50, 50);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(new Color(200, 150, 180));
        g2d.drawString("[ Doubly Linked Nodes ]", 50, 70);

        float lineGlow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 3));
        g2d.setColor(new Color(255, 100, 150, (int)(lineGlow * 150)));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(50, 80, 350, 80);
    }

    private void drawStats(Graphics2D g2d) {
        int boxWidth = 195;
        int x = getWidth() - boxWidth - 5;
        int y = 30;

        g2d.setColor(new Color(25, 20, 35, 200));
        g2d.fillRoundRect(x - 10, y - 20, boxWidth, 60, 10, 10);
        g2d.setColor(ACCENT_COLOR);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x - 10, y - 20, boxWidth, 60, 10, 10);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("SIZE: " + linkedList.getSize(), x, y);
        g2d.drawString("OPERATIONS: " + linkedList.getOperationsCount(), x, y + 20);

        drawMemoryPanel(g2d);
    }

    private void drawMemoryPanel(Graphics2D g2d) {
        var memInfo = linkedList.getMemoryInfo();

        int panelWidth = 200;
        int panelHeight = 120;
        int x = getWidth() - panelWidth - 10;
        int y = 100;

        memoryPanelBounds.setBounds(x, y, panelWidth, panelHeight);

        g2d.setColor(new Color(20, 15, 30, 230));
        g2d.fillRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 2));
        g2d.setColor(new Color(255, 150, 200, (int)(glow * 150)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, panelWidth, panelHeight, 12, 12);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(new Color(255, 150, 200));
        g2d.drawString("MEMORY USAGE", x + 10, y + 18);

        g2d.setColor(new Color(255, 150, 200, 50));
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
        g2d.drawString("NODES:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(memInfo.formatNodes() + " (" + linkedList.getSize() + "x24B)", x + 100, textY);

        textY += lineHeight;
        g2d.setColor(new Color(0, 200, 255));
        g2d.drawString("ELEMENTS:", x + 10, textY);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(memInfo.formatElements(), x + 100, textY);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 200, 255, (int)(100 + 50 * Math.sin(glowPhase * 3))));
        g2d.drawString("[CLICK FOR DETAILS]", x + 10, y + panelHeight - 5);
    }

    public VisualLinkedList getLinkedList() {
        return linkedList;
    }
}
