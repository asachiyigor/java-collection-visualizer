package game.ui;

import game.model.VisualEntry;
import game.model.VisualConcurrentHashMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ConcurrentHashMapPanel extends JPanel {
    private VisualConcurrentHashMap map;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color ACCENT = new Color(100, 150, 220);
    private static final Color LOCK_COLOR = new Color(255, 80, 80);
    private static final Color[] SEGMENT_COLORS = {
            new Color(100, 150, 220), new Color(150, 100, 220),
            new Color(100, 220, 150), new Color(220, 150, 100)
    };

    public ConcurrentHashMapPanel(VisualConcurrentHashMap map) {
        this.map = map;
        setBackground(ThemeManager.get().getBgColor());
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            map.update(delta);
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
            ConcurrentHashMapMemoryDialog dialog = new ConcurrentHashMapMemoryDialog((Frame) window, map);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int buckets = map.getCapacity();
        int bucketsPerRow = 8;
        int rows = (buckets + bucketsPerRow - 1) / bucketsPerRow;
        int maxChain = map.getMaxChainLength();
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
        drawSegmentLabels(g2d);
        drawBuckets(g2d);
        drawLockIndicator(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        ThemeManager tm = ThemeManager.get();
        GradientPaint gradient = new GradientPaint(
                0, 0, tm.getBgColor(),
                0, getHeight(), tm.getBgGradientEnd()
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(tm.getGridColor());
        g2d.setStroke(new BasicStroke(0.5f));
        int gridSize = 40;
        for (int x = 0; x < getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += gridSize) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawSegmentLabels(Graphics2D g2d) {
        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;
        int bucketsPerSegment = map.getCapacity() / map.getConcurrencyLevel();

        for (int seg = 0; seg < map.getConcurrencyLevel(); seg++) {
            int firstBucket = seg * bucketsPerSegment;
            int lastBucket = Math.min(firstBucket + bucketsPerSegment - 1, map.getCapacity() - 1);

            int firstCol = firstBucket % cols;
            int firstRow = firstBucket / cols;
            int lastCol = lastBucket % cols;

            int segColorIdx = seg % SEGMENT_COLORS.length;
            Color segColor = SEGMENT_COLORS[segColorIdx];

            if (firstRow == lastBucket / cols) {
                int labelX = startX + firstCol * cellWidth + cellWidth / 2;
                int labelY = startY + firstRow * 200 - 25;

                g2d.setFont(new Font("Consolas", Font.BOLD, 10));
                g2d.setColor(segColor);

                int segWidth = (lastCol - firstCol + 1) * cellWidth;
                int centerX = labelX + (segWidth - cellWidth) / 2;
                String label = "SEGMENT " + seg;
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(label, centerX - fm.stringWidth(label) / 2, labelY);

                g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
                g2d.setColor(new Color(segColor.getRed(), segColor.getGreen(), segColor.getBlue(), 60));
                g2d.drawRoundRect(
                        startX + firstCol * cellWidth - 5,
                        startY + firstRow * 200 - 35,
                        (lastCol - firstCol + 1) * cellWidth + 10,
                        45,
                        8, 8
                );
            } else {
                for (int b = firstBucket; b <= lastBucket; b++) {
                    int col = b % cols;
                    int row = b / cols;
                    if (col == firstBucket % cols || b == firstBucket) {
                        int labelX = startX + col * cellWidth + cellWidth / 2;
                        int labelY = startY + row * 200 - 25;
                        g2d.setFont(new Font("Consolas", Font.BOLD, 9));
                        g2d.setColor(segColor);
                        String label = "SEG " + seg;
                        FontMetrics fm = g2d.getFontMetrics();
                        g2d.drawString(label, labelX - fm.stringWidth(label) / 2, labelY);
                    }
                }
            }
        }
    }

    private void drawBuckets(Graphics2D g2d) {
        List<VisualEntry>[] buckets = map.getBuckets();
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

        boolean isHighlighted = index == map.getLastBucketAccessed();
        int segment = getSegmentForBucket(index);
        Color segColor = SEGMENT_COLORS[segment % SEGMENT_COLORS.length];

        ThemeManager tm = ThemeManager.get();
        Color bucketColor = chain.isEmpty() ? tm.getEmptyCellColor() : new Color(
                segColor.getRed() / 6, segColor.getGreen() / 6, segColor.getBlue() / 6);
        g2d.setColor(bucketColor);
        g2d.fillRoundRect(x - bucketWidth / 2, y - bucketHeight / 2, bucketWidth, bucketHeight, 8, 8);

        Color borderColor = isHighlighted ? ACCENT : segColor;
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(isHighlighted ? 2f : 1f));
        g2d.drawRoundRect(x - bucketWidth / 2, y - bucketHeight / 2, bucketWidth, bucketHeight, 8, 8);

        g2d.setColor(tm.getTextColor());
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        String indexStr = "[" + index + "]";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(indexStr, x - fm.stringWidth(indexStr) / 2, y + 4);

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
            g2d.setColor(new Color(segColor.getRed(), segColor.getGreen(), segColor.getBlue(), 150));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(x, y + bucketHeight / 2, x, y + bucketHeight / 2 + 20);
            int arrowY = y + bucketHeight / 2 + 20;
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

        int entryWidth = (int) (60 * scale);
        int entryHeight = (int) (55 * scale);

        Color keyColor = entry.getKeyColor();
        Color valueColor = entry.getValueColor();

        ThemeManager tm = ThemeManager.get();
        int bgBase = tm.isDark() ? 30 : 230;
        Color bgColor = new Color(bgBase, bgBase + 5, bgBase + 15, (int) (220 * alpha));
        g2d.setColor(bgColor);
        g2d.fillRoundRect((int) x - entryWidth / 2, (int) y - entryHeight / 2, entryWidth, entryHeight, 8, 8);

        g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (255 * alpha)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect((int) x - entryWidth / 2, (int) y - entryHeight / 2, entryWidth, entryHeight, 8, 8);

        g2d.setColor(new Color(keyColor.getRed(), keyColor.getGreen(), keyColor.getBlue(), (int) (200 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 9));
        String keyDisplay = entry.getDisplayKey();
        if (keyDisplay.length() > 8) keyDisplay = keyDisplay.substring(0, 6) + "..";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(keyDisplay, (int) x - fm.stringWidth(keyDisplay) / 2, (int) y - 10);

        g2d.setColor(new Color(100, 110, 140, (int) (150 * alpha)));
        g2d.drawLine((int) x - entryWidth / 2 + 5, (int) y - 2, (int) x + entryWidth / 2 - 5, (int) y - 2);

        g2d.setColor(new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), (int) (255 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        String valueDisplay = entry.getDisplayValue();
        if (valueDisplay.length() > 7) valueDisplay = valueDisplay.substring(0, 5) + "..";
        fm = g2d.getFontMetrics();
        g2d.drawString(valueDisplay, (int) x - fm.stringWidth(valueDisplay) / 2, (int) y + 12);

        g2d.setColor(new Color(150, 160, 190, (int) (150 * alpha)));
        g2d.setFont(new Font("Consolas", Font.PLAIN, 7));
        String typeInfo = entry.getKeyType().substring(0, 1).toUpperCase() + ":" +
                entry.getValueType().substring(0, 1).toUpperCase();
        fm = g2d.getFontMetrics();
        g2d.drawString(typeInfo, (int) x - fm.stringWidth(typeInfo) / 2, (int) y + entryHeight / 2 - 3);
    }

    private void drawLockIndicator(Graphics2D g2d) {
        int lockedSeg = map.getLockedSegment();
        if (lockedSeg < 0) return;

        int cols = 8;
        int cellWidth = 80;
        int startX = 60;
        int startY = 100;
        int bucketsPerSegment = map.getCapacity() / map.getConcurrencyLevel();

        int firstBucket = lockedSeg * bucketsPerSegment;
        int lastBucket = Math.min(firstBucket + bucketsPerSegment - 1, map.getCapacity() - 1);

        int firstCol = firstBucket % cols;
        int firstRow = firstBucket / cols;
        int lastCol = lastBucket % cols;
        int lastRow = lastBucket / cols;

        float pulse = (float) (0.3 + 0.3 * Math.sin(glowPhase * 6));

        if (firstRow == lastRow) {
            int rx = startX + firstCol * cellWidth - 10;
            int ry = startY + firstRow * 200 - 40;
            int rw = (lastCol - firstCol + 1) * cellWidth + 20;
            int rh = 230;

            g2d.setColor(new Color(LOCK_COLOR.getRed(), LOCK_COLOR.getGreen(), LOCK_COLOR.getBlue(), (int) (40 * pulse * 2)));
            g2d.fillRoundRect(rx, ry, rw, rh, 12, 12);

            g2d.setColor(new Color(LOCK_COLOR.getRed(), LOCK_COLOR.getGreen(), LOCK_COLOR.getBlue(), (int) (180 * (0.5 + pulse))));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(rx, ry, rw, rh, 12, 12);

            g2d.setFont(new Font("Consolas", Font.BOLD, 12));
            g2d.setColor(new Color(LOCK_COLOR.getRed(), LOCK_COLOR.getGreen(), LOCK_COLOR.getBlue(), (int) (255 * (0.5 + pulse))));
            String lockLabel = "LOCKED";
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = rx + rw / 2 - fm.stringWidth(lockLabel) / 2;
            int labelY = ry + rh + 16;
            g2d.drawString(lockLabel, labelX, labelY);
        } else {
            for (int b = firstBucket; b <= lastBucket; b++) {
                int col = b % cols;
                int row = b / cols;
                int bx = startX + col * cellWidth - 5;
                int by = startY + row * 200 - 25;
                g2d.setColor(new Color(LOCK_COLOR.getRed(), LOCK_COLOR.getGreen(), LOCK_COLOR.getBlue(), (int) (30 * pulse * 2)));
                g2d.fillRoundRect(bx, by, cellWidth + 10, 200, 8, 8);
                g2d.setColor(new Color(LOCK_COLOR.getRed(), LOCK_COLOR.getGreen(), LOCK_COLOR.getBlue(), (int) (120 * (0.5 + pulse))));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(bx, by, cellWidth + 10, 200, 8, 8);
            }
        }
    }

    private void drawHeader(Graphics2D g2d) {
        ThemeManager tm = ThemeManager.get();

        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("CONCURRENTHASHMAP VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(tm.getTextColor());
        g2d.drawString("[ Segment-Based Thread-Safe Map ]", 20, 55);

        if (map.wasJustRehashed()) {
            float flash = (float) (0.5 + 0.5 * Math.sin(glowPhase * 8));
            g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (255 * flash)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 14));
            g2d.drawString("REHASH!", 420, 35);
        }
    }

    private void drawStats(Graphics2D g2d) {
        ThemeManager tm = ThemeManager.get();
        int panelX = getWidth() - 240;
        int panelY = 20;
        int panelWidth = 220;
        int panelHeight = 160;

        g2d.setColor(tm.getStatsBg());
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("CONCURRENTHASHMAP STATUS", panelX + 12, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(tm.getTextColor());
        int y = panelY + 38;
        g2d.drawString("Size: " + map.getSize() + " / Capacity: " + map.getCapacity(), panelX + 12, y);
        y += 15;
        g2d.drawString(String.format("Load: %.1f%%", map.getCurrentLoad() * 100), panelX + 12, y);
        y += 15;
        g2d.drawString("Concurrency: " + map.getConcurrencyLevel() + " segments", panelX + 12, y);
        y += 15;
        g2d.drawString("Collisions: " + map.getCollisionCount(), panelX + 12, y);
        y += 15;
        g2d.drawString("Rehashes: " + map.getRehashCount(), panelX + 12, y);
        y += 15;

        int lockedSeg = map.getLockedSegment();
        if (lockedSeg >= 0) {
            g2d.setColor(LOCK_COLOR);
            g2d.drawString("Locked: Segment " + lockedSeg, panelX + 12, y);
        } else {
            g2d.setColor(new Color(100, 200, 100));
            g2d.drawString("All segments free", panelX + 12, y);
        }

        int loadBarX = panelX + 12;
        int loadBarY = panelY + panelHeight - 20;
        int loadBarWidth = panelWidth - 24;
        int loadBarHeight = 8;

        g2d.setColor(new Color(20, 25, 40));
        g2d.fillRoundRect(loadBarX, loadBarY, loadBarWidth, loadBarHeight, 4, 4);

        double load = map.getCurrentLoad();
        int fillWidth = (int) (loadBarWidth * Math.min(load, 1.0));
        Color loadColor = load > 0.75 ? new Color(255, 100, 100) :
                load > 0.5 ? new Color(255, 200, 100) : new Color(100, 255, 150);
        g2d.setColor(loadColor);
        g2d.fillRoundRect(loadBarX, loadBarY, fillWidth, loadBarHeight, 4, 4);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        ThemeManager tm = ThemeManager.get();
        int width = 220;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(tm.getStatsBg());
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        VisualConcurrentHashMap.MemoryInfo mem = map.getMemoryInfo();
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(tm.getTextColor());
        g2d.drawString("Total: " + mem.formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 170, 210));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }

    private int getSegmentForBucket(int bucketIndex) {
        int bucketsPerSegment = map.getCapacity() / map.getConcurrencyLevel();
        if (bucketsPerSegment == 0) bucketsPerSegment = 1;
        return Math.min(bucketIndex / bucketsPerSegment, map.getConcurrencyLevel() - 1);
    }
}
