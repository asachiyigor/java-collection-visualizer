package game.ui;

import game.model.VisualBucket;
import game.model.VisualElement;
import game.model.VisualLinkedHashSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LinkedHashSetPanel extends JPanel {
    private VisualLinkedHashSet linkedHashSet;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(25, 15, 18);
    private static final Color GRID_COLOR = new Color(50, 35, 40);
    private static final Color ACCENT = new Color(255, 120, 120);
    private static final Color TEXT_COLOR = new Color(255, 220, 225);
    private static final Color ORDER_COLOR = new Color(255, 180, 100);

    public LinkedHashSetPanel(VisualLinkedHashSet linkedHashSet) {
        this.linkedHashSet = linkedHashSet;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            linkedHashSet.update(delta);
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
    }

    private void showMemoryDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Frame) {
            LinkedHashSetMemoryDialog dialog = new LinkedHashSetMemoryDialog((Frame) window, linkedHashSet);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int buckets = linkedHashSet.getCapacity();
        int bucketsPerRow = 8;
        int rows = (buckets + bucketsPerRow - 1) / bucketsPerRow;
        int maxChain = linkedHashSet.getMaxChainLength();
        int width = Math.max(900, 60 + bucketsPerRow * 80);
        int height = Math.max(600, 120 + rows * (60 + maxChain * 55 + 80));
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2d);
        drawInsertionOrderLinks(g2d);
        drawBuckets(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, BG_COLOR,
                0, getHeight(), new Color(35, 20, 25)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        int gridSize = 40;
        for (int x = 0; x < getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += gridSize) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawInsertionOrderLinks(Graphics2D g2d) {
        List<VisualElement> order = linkedHashSet.getInsertionOrder();
        if (order.size() < 2) return;

        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 2));

        for (int i = 0; i < order.size() - 1; i++) {
            VisualElement from = order.get(i);
            VisualElement to = order.get(i + 1);

            if (from.isRemoving() || to.isRemoving()) continue;
            if (from.getAlpha() < 0.5 || to.getAlpha() < 0.5) continue;

            double x1 = from.getX();
            double y1 = from.getY();
            double x2 = to.getX();
            double y2 = to.getY();

            g2d.setColor(new Color(255, 180, 100, (int)(80 * glow)));
            g2d.drawLine((int)x1, (int)y1, (int)x2, (int)y2);

            double angle = Math.atan2(y2 - y1, x2 - x1);
            double arrowLen = 8;
            double midX = (x1 + x2) / 2;
            double midY = (y1 + y2) / 2;

            int ax1 = (int)(midX - arrowLen * Math.cos(angle - Math.PI/6));
            int ay1 = (int)(midY - arrowLen * Math.sin(angle - Math.PI/6));
            int ax2 = (int)(midX - arrowLen * Math.cos(angle + Math.PI/6));
            int ay2 = (int)(midY - arrowLen * Math.sin(angle + Math.PI/6));

            g2d.setColor(new Color(255, 180, 100, (int)(150 * glow)));
            g2d.drawLine((int)midX, (int)midY, ax1, ay1);
            g2d.drawLine((int)midX, (int)midY, ax2, ay2);
        }
    }

    private void drawBuckets(Graphics2D g2d) {
        VisualBucket[] buckets = linkedHashSet.getBuckets();
        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;

        for (int i = 0; i < buckets.length; i++) {
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * cellWidth + cellWidth / 2.0;
            double y = startY + row * 180;

            drawBucket(g2d, buckets[i], (int) x, (int) y, i);
        }
    }

    private void drawBucket(Graphics2D g2d, VisualBucket bucket, int x, int y, int index) {
        int bucketWidth = 60;
        int bucketHeight = 35;

        boolean isHighlighted = bucket.isHighlighted();

        Color bucketColor = bucket.isEmpty() ? new Color(45, 35, 40) : new Color(70, 50, 55);
        g2d.setColor(bucketColor);
        g2d.fillRoundRect(x - bucketWidth/2, y - bucketHeight/2, bucketWidth, bucketHeight, 8, 8);

        g2d.setColor(isHighlighted ? ACCENT : new Color(100, 70, 80));
        g2d.setStroke(new BasicStroke(isHighlighted ? 2f : 1f));
        g2d.drawRoundRect(x - bucketWidth/2, y - bucketHeight/2, bucketWidth, bucketHeight, 8, 8);

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        String indexStr = "[" + index + "]";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(indexStr, x - fm.stringWidth(indexStr)/2, y + 4);

        for (VisualElement elem : bucket.getChain()) {
            if (!elem.isFullyRemoved()) {
                drawElement(g2d, elem);
            }
        }

        if (!bucket.isEmpty()) {
            g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(x, y + bucketHeight/2, x, y + bucketHeight/2 + 15);
        }
    }

    private void drawElement(Graphics2D g2d, VisualElement elem) {
        double x = elem.getX();
        double y = elem.getY();
        double alpha = elem.getAlpha();
        double scale = elem.getScale();

        if (alpha <= 0 || scale <= 0) return;

        int cellWidth = 50;
        int cellHeight = 45;
        int scaledWidth = (int)(cellWidth * scale);
        int scaledHeight = (int)(cellHeight * scale);

        Color elemColor = elem.getColor();
        Color bgColor = new Color(elemColor.getRed()/4, elemColor.getGreen()/4, elemColor.getBlue()/4, (int)(200 * alpha));
        Color borderColor = new Color(elemColor.getRed(), elemColor.getGreen(), elemColor.getBlue(), (int)(255 * alpha));

        g2d.setColor(bgColor);
        g2d.fillRoundRect((int)x - scaledWidth/2, (int)y - scaledHeight/2, scaledWidth, scaledHeight, 8, 8);

        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect((int)x - scaledWidth/2, (int)y - scaledHeight/2, scaledWidth, scaledHeight, 8, 8);

        g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        String display = elem.getDisplayValue();
        if (display.length() > 6) display = display.substring(0, 5) + "..";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(display, (int)x - fm.stringWidth(display)/2, (int)y + 2);

        g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), (int)(180 * alpha)));
        g2d.setFont(new Font("Consolas", Font.PLAIN, 8));
        String typeStr = elem.getType().toUpperCase();
        fm = g2d.getFontMetrics();
        g2d.drawString(typeStr, (int)x - fm.stringWidth(typeStr)/2, (int)y + 14);

        List<VisualElement> order = linkedHashSet.getInsertionOrder();
        int orderIdx = order.indexOf(elem);
        if (orderIdx >= 0) {
            g2d.setColor(new Color(255, 180, 100, (int)(200 * alpha)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 8));
            String orderStr = "#" + (orderIdx + 1);
            g2d.drawString(orderStr, (int)x - scaledWidth/2 + 3, (int)y - scaledHeight/2 + 10);
        }
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("LINKEDHASHSET VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Hash Table + Insertion Order Chain ]", 20, 55);

        g2d.setColor(ORDER_COLOR);
        g2d.drawString("Orange arrows show insertion order", 20, 75);
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 140;

        g2d.setColor(new Color(40, 28, 32, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("LINKEDHASHSET STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + linkedHashSet.getSize() + " / Capacity: " + linkedHashSet.getCapacity(), panelX + 15, y);
        y += 15;
        g2d.drawString(String.format("Load: %.1f%%", linkedHashSet.getCurrentLoad() * 100), panelX + 15, y);
        y += 15;
        g2d.drawString("Collisions: " + linkedHashSet.getCollisionCount(), panelX + 15, y);
        y += 15;
        g2d.drawString("Rehashes: " + linkedHashSet.getRehashCount(), panelX + 15, y);
        y += 15;
        g2d.setColor(ORDER_COLOR);
        g2d.drawString("Order preserved: YES", panelX + 15, y);

        int loadBarX = panelX + 15;
        int loadBarY = panelY + panelHeight - 20;
        int loadBarWidth = panelWidth - 30;
        int loadBarHeight = 8;

        g2d.setColor(new Color(30, 22, 25));
        g2d.fillRoundRect(loadBarX, loadBarY, loadBarWidth, loadBarHeight, 4, 4);

        double load = linkedHashSet.getCurrentLoad();
        int fillWidth = (int)(loadBarWidth * Math.min(load, 1.0));
        g2d.setColor(ACCENT);
        g2d.fillRoundRect(loadBarX, loadBarY, fillWidth, loadBarHeight, 4, 4);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(40, 28, 32, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        VisualLinkedHashSet.MemoryInfo mem = linkedHashSet.getMemoryInfo();
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + mem.formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(200, 180, 185));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
