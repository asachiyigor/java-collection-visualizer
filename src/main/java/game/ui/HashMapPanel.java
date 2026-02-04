package game.ui;

import game.model.VisualEntry;
import game.model.VisualHashMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class HashMapPanel extends JPanel {
    private VisualHashMap hashMap;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(25, 20, 10);
    private static final Color GRID_COLOR = new Color(50, 45, 25);
    private static final Color ACCENT = new Color(255, 200, 80);
    private static final Color TEXT_COLOR = new Color(255, 245, 220);

    public HashMapPanel(VisualHashMap hashMap) {
        this.hashMap = hashMap;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            hashMap.update(delta);
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
            HashMapMemoryDialog dialog = new HashMapMemoryDialog((Frame) window, hashMap);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int buckets = hashMap.getCapacity();
        int bucketsPerRow = 8;
        int rows = (buckets + bucketsPerRow - 1) / bucketsPerRow;
        int maxChain = hashMap.getMaxChainLength();
        int width = Math.max(900, 60 + bucketsPerRow * 85);
        int height = Math.max(600, 120 + rows * (50 + maxChain * 60 + 80));
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2d);
        drawBuckets(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, BG_COLOR,
                0, getHeight(), new Color(35, 30, 15)
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

    private void drawBuckets(Graphics2D g2d) {
        List<VisualEntry>[] buckets = hashMap.getBuckets();
        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;

        for (int i = 0; i < buckets.length; i++) {
            int col = i % cols;
            int row = i / cols;
            double x = startX + col * cellWidth + cellWidth / 2.0;
            double y = startY + row * 200;

            drawBucket(g2d, buckets[i], (int) x, (int) y, i);
        }
    }

    private void drawBucket(Graphics2D g2d, List<VisualEntry> chain, int x, int y, int index) {
        int bucketWidth = 60;
        int bucketHeight = 35;

        boolean isHighlighted = index == hashMap.getLastBucketAccessed();

        Color bucketColor = chain.isEmpty() ? new Color(45, 40, 25) : new Color(70, 60, 35);
        g2d.setColor(bucketColor);
        g2d.fillRoundRect(x - bucketWidth/2, y - bucketHeight/2, bucketWidth, bucketHeight, 8, 8);

        g2d.setColor(isHighlighted ? ACCENT : new Color(100, 90, 50));
        g2d.setStroke(new BasicStroke(isHighlighted ? 2f : 1f));
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

        int activeCount = 0;
        for (VisualEntry e : chain) {
            if (!e.isRemoving()) activeCount++;
        }
        if (activeCount > 0) {
            g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(x, y + bucketHeight/2, x, y + bucketHeight/2 + 20);
            int arrowY = y + bucketHeight/2 + 20;
            g2d.drawLine(x - 4, arrowY - 4, x, arrowY);
            g2d.drawLine(x + 4, arrowY - 4, x, arrowY);
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

        Color keyColor = entry.getKeyColor();
        Color valueColor = entry.getValueColor();

        Color bgColor = new Color(40, 35, 20, (int)(220 * alpha));
        g2d.setColor(bgColor);
        g2d.fillRoundRect((int)x - entryWidth/2, (int)y - entryHeight/2, entryWidth, entryHeight, 8, 8);

        g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(255 * alpha)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect((int)x - entryWidth/2, (int)y - entryHeight/2, entryWidth, entryHeight, 8, 8);

        g2d.setColor(new Color(keyColor.getRed(), keyColor.getGreen(), keyColor.getBlue(), (int)(200 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 9));
        String keyDisplay = entry.getDisplayKey();
        if (keyDisplay.length() > 8) keyDisplay = keyDisplay.substring(0, 6) + "..";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(keyDisplay, (int)x - fm.stringWidth(keyDisplay)/2, (int)y - 10);

        g2d.setColor(new Color(100, 90, 60, (int)(150 * alpha)));
        g2d.drawLine((int)x - entryWidth/2 + 5, (int)y - 2, (int)x + entryWidth/2 - 5, (int)y - 2);

        g2d.setColor(new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), (int)(255 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        String valueDisplay = entry.getDisplayValue();
        if (valueDisplay.length() > 7) valueDisplay = valueDisplay.substring(0, 5) + "..";
        fm = g2d.getFontMetrics();
        g2d.drawString(valueDisplay, (int)x - fm.stringWidth(valueDisplay)/2, (int)y + 12);

        g2d.setColor(new Color(150, 140, 100, (int)(150 * alpha)));
        g2d.setFont(new Font("Consolas", Font.PLAIN, 7));
        String typeInfo = entry.getKeyType().substring(0, 1).toUpperCase() + ":" +
                entry.getValueType().substring(0, 1).toUpperCase();
        fm = g2d.getFontMetrics();
        g2d.drawString(typeInfo, (int)x - fm.stringWidth(typeInfo)/2, (int)y + entryHeight/2 - 3);
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("HASHMAP VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Hash Table with Key-Value Pairs ]", 20, 55);

        if (hashMap.wasJustRehashed()) {
            float flash = (float) (0.5 + 0.5 * Math.sin(glowPhase * 8));
            g2d.setColor(new Color(255, 200, 100, (int)(255 * flash)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 14));
            g2d.drawString("REHASH!", 280, 35);
        }
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 130;

        g2d.setColor(new Color(40, 35, 18, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("HASHMAP STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + hashMap.getSize() + " / Capacity: " + hashMap.getCapacity(), panelX + 15, y);
        y += 15;
        g2d.drawString(String.format("Load: %.1f%%", hashMap.getCurrentLoad() * 100), panelX + 15, y);
        y += 15;
        g2d.drawString("Collisions: " + hashMap.getCollisionCount(), panelX + 15, y);
        y += 15;
        g2d.drawString("Rehashes: " + hashMap.getRehashCount(), panelX + 15, y);

        int loadBarX = panelX + 15;
        int loadBarY = panelY + panelHeight - 20;
        int loadBarWidth = panelWidth - 30;
        int loadBarHeight = 8;

        g2d.setColor(new Color(30, 28, 15));
        g2d.fillRoundRect(loadBarX, loadBarY, loadBarWidth, loadBarHeight, 4, 4);

        double load = hashMap.getCurrentLoad();
        int fillWidth = (int)(loadBarWidth * Math.min(load, 1.0));
        Color loadColor = load > 0.75 ? new Color(255, 100, 100) :
                load > 0.5 ? new Color(255, 200, 100) : new Color(100, 255, 150);
        g2d.setColor(loadColor);
        g2d.fillRoundRect(loadBarX, loadBarY, fillWidth, loadBarHeight, 4, 4);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(40, 35, 18, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        VisualHashMap.MemoryInfo mem = hashMap.getMemoryInfo();
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + mem.formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(200, 190, 150));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
