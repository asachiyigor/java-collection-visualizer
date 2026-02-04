package game.ui;

import game.model.VisualEntry;
import game.model.VisualLinkedHashMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LinkedHashMapPanel extends JPanel {
    private VisualLinkedHashMap linkedHashMap;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(28, 20, 15);
    private static final Color GRID_COLOR = new Color(50, 40, 32);
    private static final Color ACCENT = new Color(255, 180, 130);
    private static final Color TEXT_COLOR = new Color(255, 240, 225);
    private static final Color ORDER_COLOR = new Color(255, 200, 100);

    public LinkedHashMapPanel(VisualLinkedHashMap linkedHashMap) {
        this.linkedHashMap = linkedHashMap;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            linkedHashMap.update(delta);
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
            LinkedHashMapMemoryDialog dialog = new LinkedHashMapMemoryDialog((Frame) window, linkedHashMap);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int buckets = linkedHashMap.getCapacity();
        int bucketsPerRow = 8;
        int rows = (buckets + bucketsPerRow - 1) / bucketsPerRow;
        int maxChain = linkedHashMap.getMaxChainLength();
        int width = Math.max(900, 60 + bucketsPerRow * 85);
        int height = Math.max(600, 120 + rows * (50 + maxChain * 60 + 80));
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawInsertionOrderLinks(g2d);
        drawBuckets(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(0, 0, BG_COLOR, 0, getHeight(), new Color(38, 28, 20));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        int gridSize = 40;
        for (int x = 0; x < getWidth(); x += gridSize) g2d.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += gridSize) g2d.drawLine(0, y, getWidth(), y);
    }

    private void drawInsertionOrderLinks(Graphics2D g2d) {
        List<VisualEntry> order = linkedHashMap.getInsertionOrder();
        if (order.size() < 2) return;

        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase * 2));

        for (int i = 0; i < order.size() - 1; i++) {
            VisualEntry from = order.get(i);
            VisualEntry to = order.get(i + 1);

            if (from.isRemoving() || to.isRemoving()) continue;
            if (from.getAlpha() < 0.5 || to.getAlpha() < 0.5) continue;

            g2d.setColor(new Color(255, 200, 100, (int)(80 * glow)));
            g2d.drawLine((int)from.getX(), (int)from.getY(), (int)to.getX(), (int)to.getY());

            double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
            double midX = (from.getX() + to.getX()) / 2;
            double midY = (from.getY() + to.getY()) / 2;
            g2d.setColor(new Color(255, 200, 100, (int)(150 * glow)));
            int ax1 = (int)(midX - 8 * Math.cos(angle - Math.PI/6));
            int ay1 = (int)(midY - 8 * Math.sin(angle - Math.PI/6));
            int ax2 = (int)(midX - 8 * Math.cos(angle + Math.PI/6));
            int ay2 = (int)(midY - 8 * Math.sin(angle + Math.PI/6));
            g2d.drawLine((int)midX, (int)midY, ax1, ay1);
            g2d.drawLine((int)midX, (int)midY, ax2, ay2);
        }
    }

    private void drawBuckets(Graphics2D g2d) {
        List<VisualEntry>[] buckets = linkedHashMap.getBuckets();
        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;

        for (int i = 0; i < buckets.length; i++) {
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * cellWidth + cellWidth / 2.0;
            double y = startY + row * 200;
            drawBucket(g2d, buckets[i], (int)x, (int)y, i);
        }
    }

    private void drawBucket(Graphics2D g2d, List<VisualEntry> chain, int x, int y, int index) {
        int bucketWidth = 60;
        int bucketHeight = 35;

        Color bucketColor = chain.isEmpty() ? new Color(45, 38, 30) : new Color(70, 58, 45);
        g2d.setColor(bucketColor);
        g2d.fillRoundRect(x - bucketWidth/2, y - bucketHeight/2, bucketWidth, bucketHeight, 8, 8);

        g2d.setColor(new Color(100, 85, 65));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRoundRect(x - bucketWidth/2, y - bucketHeight/2, bucketWidth, bucketHeight, 8, 8);

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        String indexStr = "[" + index + "]";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(indexStr, x - fm.stringWidth(indexStr)/2, y + 4);

        for (VisualEntry entry : chain) {
            if (!entry.isFullyRemoved()) {
                drawEntry(g2d, entry);
            }
        }
    }

    private void drawEntry(Graphics2D g2d, VisualEntry entry) {
        double x = entry.getX();
        double y = entry.getY();
        double alpha = entry.getAlpha();
        double scale = entry.getScale();

        if (alpha <= 0 || scale <= 0) return;

        int entryWidth = (int)(60 * scale);
        int entryHeight = (int)(55 * scale);

        g2d.setColor(new Color(45, 38, 28, (int)(220 * alpha)));
        g2d.fillRoundRect((int)x - entryWidth/2, (int)y - entryHeight/2, entryWidth, entryHeight, 8, 8);

        g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(255 * alpha)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect((int)x - entryWidth/2, (int)y - entryHeight/2, entryWidth, entryHeight, 8, 8);

        g2d.setColor(new Color(entry.getKeyColor().getRed(), entry.getKeyColor().getGreen(),
                entry.getKeyColor().getBlue(), (int)(200 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 9));
        String keyDisplay = entry.getDisplayKey();
        if (keyDisplay.length() > 8) keyDisplay = keyDisplay.substring(0, 6) + "..";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(keyDisplay, (int)x - fm.stringWidth(keyDisplay)/2, (int)y - 10);

        g2d.setColor(new Color(entry.getValueColor().getRed(), entry.getValueColor().getGreen(),
                entry.getValueColor().getBlue(), (int)(255 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        String valueDisplay = entry.getDisplayValue();
        if (valueDisplay.length() > 7) valueDisplay = valueDisplay.substring(0, 5) + "..";
        fm = g2d.getFontMetrics();
        g2d.drawString(valueDisplay, (int)x - fm.stringWidth(valueDisplay)/2, (int)y + 12);

        List<VisualEntry> order = linkedHashMap.getInsertionOrder();
        int orderIdx = order.indexOf(entry);
        if (orderIdx >= 0) {
            g2d.setColor(new Color(255, 200, 100, (int)(200 * alpha)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 8));
            String orderStr = "#" + (orderIdx + 1);
            g2d.drawString(orderStr, (int)x - entryWidth/2 + 3, (int)y - entryHeight/2 + 10);
        }
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("LINKEDHASHMAP VISUALIZER", 20, 35);

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
        int panelHeight = 120;

        g2d.setColor(new Color(45, 38, 28, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("LINKEDHASHMAP STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + linkedHashMap.getSize() + " / Cap: " + linkedHashMap.getCapacity(), panelX + 15, y);
        y += 15;
        g2d.drawString(String.format("Load: %.1f%%", linkedHashMap.getCurrentLoad() * 100), panelX + 15, y);
        y += 15;
        g2d.drawString("Collisions: " + linkedHashMap.getCollisionCount(), panelX + 15, y);
        y += 15;
        g2d.setColor(ORDER_COLOR);
        g2d.drawString("Order preserved: YES", panelX + 15, y);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(45, 38, 28, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + linkedHashMap.getMemoryInfo().formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(200, 185, 165));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
