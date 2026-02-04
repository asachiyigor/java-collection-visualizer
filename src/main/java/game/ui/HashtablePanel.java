package game.ui;

import game.model.VisualHashtable;
import game.model.VisualBucket;
import game.model.VisualEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class HashtablePanel extends JPanel {
    private VisualHashtable hashtable;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(20, 14, 10);
    private static final Color GRID_COLOR = new Color(45, 35, 28);
    private static final Color ACCENT = new Color(200, 120, 80);
    private static final Color TEXT_COLOR = new Color(255, 230, 210);
    private static final Color KEY_COLOR = new Color(255, 180, 120);
    private static final Color VALUE_COLOR = new Color(180, 220, 255);

    public HashtablePanel(VisualHashtable hashtable) {
        this.hashtable = hashtable;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            hashtable.update(delta);
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
            HashtableMemoryDialog dialog = new HashtableMemoryDialog((Frame) window, hashtable);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int buckets = hashtable.getCapacity();
        int bucketsPerRow = 8;
        int rows = (buckets + bucketsPerRow - 1) / bucketsPerRow;
        int maxChain = hashtable.getMaxChainLength();
        int width = Math.max(900, 50 + bucketsPerRow * 88);
        int height = Math.max(600, 120 + rows * (50 + maxChain * 58 + 80));
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawBuckets(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(0, 0, BG_COLOR, 0, getHeight(), new Color(30, 22, 16));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        int gridSize = 40;
        for (int x = 0; x < getWidth(); x += gridSize) g2d.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += gridSize) g2d.drawLine(0, y, getWidth(), y);
    }

    private void drawBuckets(Graphics2D g2d) {
        List<VisualBucket> buckets = hashtable.getBuckets();
        int bucketsPerRow = 8;
        int bucketWidth = 75;
        int bucketHeight = 35;
        int startX = 40;
        int startY = 95;
        int gapX = 8;
        int gapY = 110;

        for (int i = 0; i < buckets.size(); i++) {
            VisualBucket bucket = buckets.get(i);
            int row = i / bucketsPerRow;
            int col = i % bucketsPerRow;
            int x = startX + col * (bucketWidth + gapX);
            int y = startY + row * gapY;

            boolean isHighlighted = bucket.isHighlighted();

            // Bucket glow
            if (isHighlighted) {
                float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase * 4));
                g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(80 * glow)));
                g2d.fillRoundRect(x - 3, y - 3, bucketWidth + 6, bucketHeight + 6, 10, 10);
            }

            // Bucket background
            g2d.setColor(new Color(50, 38, 30, isHighlighted ? 255 : 200));
            g2d.fillRoundRect(x, y, bucketWidth, bucketHeight, 8, 8);

            // Bucket border
            g2d.setColor(isHighlighted ? ACCENT : ACCENT.darker());
            g2d.setStroke(new BasicStroke(isHighlighted ? 2f : 1.5f));
            g2d.drawRoundRect(x, y, bucketWidth, bucketHeight, 8, 8);

            // Index
            g2d.setFont(new Font("Consolas", Font.BOLD, 10));
            g2d.setColor(isHighlighted ? ACCENT : new Color(180, 150, 120));
            g2d.drawString("[" + i + "]", x + 5, y + 14);

            // Entry count
            int count = bucket.getEntries().size();
            if (count > 0) {
                g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
                g2d.setColor(count > 1 ? new Color(255, 150, 100) : new Color(150, 200, 150));
                g2d.drawString(count + " entry" + (count > 1 ? "s" : ""), x + 5, y + 28);
            }

            // Draw entries in chain
            List<VisualEntry> entries = bucket.getEntries();
            int entryY = y + bucketHeight + 8;
            int entryWidth = 70;
            int entryHeight = 45;

            for (int j = 0; j < entries.size(); j++) {
                VisualEntry entry = entries.get(j);
                double alpha = entry.getAlpha();
                double scale = entry.getScale();

                if (alpha <= 0) continue;

                int ew = (int)(entryWidth * scale);
                int eh = (int)(entryHeight * scale);
                int ex = x + (bucketWidth - ew) / 2;
                int ey = entryY + j * (entryHeight + 5);

                // Connector line
                if (j == 0) {
                    g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(150 * alpha)));
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawLine(x + bucketWidth/2, y + bucketHeight, x + bucketWidth/2, ey);
                }

                // Entry glow if highlighted
                if (entry.isHighlighted()) {
                    float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase * 4));
                    g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(100 * glow * alpha)));
                    g2d.fillRoundRect(ex - 4, ey - 4, ew + 8, eh + 8, 12, 12);
                }

                // Entry background
                g2d.setColor(new Color(60, 45, 35, (int)(220 * alpha)));
                g2d.fillRoundRect(ex, ey, ew, eh, 8, 8);

                // Entry border
                g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(255 * alpha)));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(ex, ey, ew, eh, 8, 8);

                // Key
                g2d.setColor(new Color(KEY_COLOR.getRed(), KEY_COLOR.getGreen(), KEY_COLOR.getBlue(), (int)(255 * alpha)));
                g2d.setFont(new Font("Consolas", Font.BOLD, 9));
                String keyDisplay = entry.getDisplayKey();
                if (keyDisplay.length() > 8) keyDisplay = keyDisplay.substring(0, 6) + "..";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(keyDisplay, ex + (ew - fm.stringWidth(keyDisplay))/2, ey + 14);

                // Value
                g2d.setColor(new Color(VALUE_COLOR.getRed(), VALUE_COLOR.getGreen(), VALUE_COLOR.getBlue(), (int)(255 * alpha)));
                g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
                String valueDisplay = entry.getDisplayValue();
                if (valueDisplay.length() > 8) valueDisplay = valueDisplay.substring(0, 6) + "..";
                fm = g2d.getFontMetrics();
                g2d.drawString(valueDisplay, ex + (ew - fm.stringWidth(valueDisplay))/2, ey + 28);

                // Chain connector
                if (j < entries.size() - 1) {
                    g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(120 * alpha)));
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawLine(ex + ew/2, ey + eh, ex + ew/2, ey + eh + 5);
                }
            }
        }
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("DICTIONARY / HASHTABLE", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Hashtable extends abstract Dictionary class ]", 20, 55);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(new Color(255, 150, 100));
        g2d.drawString("Synchronized - Thread-safe but slower", 20, 72);
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 140;

        g2d.setColor(new Color(40, 30, 22, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("HASHTABLE STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + hashtable.getSize(), panelX + 15, y);
        y += 15;
        g2d.drawString("Capacity: " + hashtable.getCapacity(), panelX + 15, y);
        y += 15;
        g2d.drawString(String.format("Load: %.2f / %.2f", hashtable.getCurrentLoad(), hashtable.getLoadFactor()), panelX + 15, y);
        y += 15;
        g2d.drawString("Max chain: " + hashtable.getMaxChainLength(), panelX + 15, y);
        y += 15;
        g2d.drawString("Rehashes: " + hashtable.getRehashCount(), panelX + 15, y);
        y += 15;
        g2d.setColor(new Color(255, 150, 100));
        g2d.drawString("null NOT allowed", panelX + 15, y);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(40, 30, 22, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + hashtable.getMemoryInfo().formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(200, 160, 120));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
