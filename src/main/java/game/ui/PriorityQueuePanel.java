package game.ui;

import game.model.VisualElement;
import game.model.VisualPriorityQueue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PriorityQueuePanel extends JPanel {
    private VisualPriorityQueue queue;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(25, 12, 15);
    private static final Color GRID_COLOR = new Color(50, 30, 35);
    private static final Color ACCENT = new Color(255, 100, 100);
    private static final Color TEXT_COLOR = new Color(255, 220, 220);
    private static final Color NODE_BG = new Color(50, 25, 30);
    private static final Color EDGE_COLOR = new Color(120, 60, 60);

    public PriorityQueuePanel(VisualPriorityQueue queue) {
        this.queue = queue;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            queue.update(delta);
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
            PriorityQueueMemoryDialog dialog = new PriorityQueueMemoryDialog((Frame) window, queue);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int size = queue.getSize();
        int levels = size > 0 ? (int) (Math.log(size) / Math.log(2)) + 1 : 1;
        int width = Math.max(900, 100 + (1 << levels) * 50);
        int panelHeight = Math.max(700, 120 + levels * 80 + 250);
        return new Dimension(width, panelHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2d);
        drawHeader(g2d);
        drawTree(g2d);
        drawRelationshipLabels(g2d);
        drawArray(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, BG_COLOR,
                0, getHeight(), new Color(35, 18, 22)
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

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("PRIORITYQUEUE VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Binary Min-Heap ]", 20, 55);
    }

    private void drawTree(Graphics2D g2d) {
        List<VisualElement> elements = queue.getElements();
        if (elements.isEmpty()) {
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Consolas", Font.ITALIC, 14));
            g2d.drawString("Empty heap - offer elements to visualize", 250, 200);
            return;
        }

        List<Integer> highlighted = queue.getHighlightedIndices();
        int size = elements.size();
        int levels = (int) (Math.log(size) / Math.log(2)) + 1;

        int treeTop = 90;
        int levelHeight = 70;
        int totalWidth = getWidth() - 100;
        int startX = 50;

        // Draw edges first
        for (int i = 0; i < size; i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;

            int level = (int) (Math.log(i + 1) / Math.log(2));
            int posInLevel = i - (1 << level) + 1;
            int nodesInLevel = 1 << level;
            double nodeSpacing = (double) totalWidth / nodesInLevel;
            int nodeX = startX + (int) (nodeSpacing * posInLevel + nodeSpacing / 2);
            int nodeY = treeTop + level * levelHeight;

            if (left < size) {
                int childLevel = (int) (Math.log(left + 1) / Math.log(2));
                int childPosInLevel = left - (1 << childLevel) + 1;
                int childNodesInLevel = 1 << childLevel;
                double childSpacing = (double) totalWidth / childNodesInLevel;
                int childX = startX + (int) (childSpacing * childPosInLevel + childSpacing / 2);
                int childY = treeTop + childLevel * levelHeight;

                boolean bothHighlighted = highlighted.contains(i) && highlighted.contains(left);
                Color edgeCol = bothHighlighted ?
                        new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 200) :
                        new Color(EDGE_COLOR.getRed(), EDGE_COLOR.getGreen(), EDGE_COLOR.getBlue(), 150);
                g2d.setColor(edgeCol);
                g2d.setStroke(new BasicStroke(bothHighlighted ? 2.5f : 1.5f));
                g2d.drawLine(nodeX, nodeY + 18, childX, childY - 18);
            }

            if (right < size) {
                int childLevel = (int) (Math.log(right + 1) / Math.log(2));
                int childPosInLevel = right - (1 << childLevel) + 1;
                int childNodesInLevel = 1 << childLevel;
                double childSpacing = (double) totalWidth / childNodesInLevel;
                int childX = startX + (int) (childSpacing * childPosInLevel + childSpacing / 2);
                int childY = treeTop + childLevel * levelHeight;

                boolean bothHighlighted = highlighted.contains(i) && highlighted.contains(right);
                Color edgeCol = bothHighlighted ?
                        new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 200) :
                        new Color(EDGE_COLOR.getRed(), EDGE_COLOR.getGreen(), EDGE_COLOR.getBlue(), 150);
                g2d.setColor(edgeCol);
                g2d.setStroke(new BasicStroke(bothHighlighted ? 2.5f : 1.5f));
                g2d.drawLine(nodeX, nodeY + 18, childX, childY - 18);
            }
        }

        // Draw nodes
        for (int i = 0; i < size; i++) {
            VisualElement element = elements.get(i);
            double alpha = element.getAlpha();
            if (alpha <= 0) continue;

            int level = (int) (Math.log(i + 1) / Math.log(2));
            int posInLevel = i - (1 << level) + 1;
            int nodesInLevel = 1 << level;
            double nodeSpacing = (double) totalWidth / nodesInLevel;
            int nodeX = startX + (int) (nodeSpacing * posInLevel + nodeSpacing / 2);
            int nodeY = treeTop + level * levelHeight;

            int nodeWidth = 55;
            int nodeHeight = 36;
            boolean isHighlighted = highlighted.contains(i);

            // Glow effect for highlighted nodes
            if (isHighlighted) {
                float glow = (float) (0.5 + 0.5 * Math.sin(glowPhase * 4));
                g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (100 * glow * alpha)));
                g2d.fillRoundRect(nodeX - nodeWidth / 2 - 5, nodeY - nodeHeight / 2 - 5,
                        nodeWidth + 10, nodeHeight + 10, 14, 14);
            }

            // Node background
            Color bgColor = new Color(NODE_BG.getRed(), NODE_BG.getGreen(), NODE_BG.getBlue(), (int) (220 * alpha));
            g2d.setColor(bgColor);
            g2d.fillRoundRect(nodeX - nodeWidth / 2, nodeY - nodeHeight / 2, nodeWidth, nodeHeight, 10, 10);

            // Node border
            Color borderColor = isHighlighted ?
                    new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (255 * alpha)) :
                    new Color(EDGE_COLOR.getRed(), EDGE_COLOR.getGreen(), EDGE_COLOR.getBlue(), (int) (200 * alpha));
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(isHighlighted ? 2.5f : 1.5f));
            g2d.drawRoundRect(nodeX - nodeWidth / 2, nodeY - nodeHeight / 2, nodeWidth, nodeHeight, 10, 10);

            // Value text
            g2d.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 12));
            String display = element.getDisplayValue();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(display, nodeX - fm.stringWidth(display) / 2, nodeY + 4);

            // Index label below node
            g2d.setColor(new Color(200, 150, 150, (int) (180 * alpha)));
            g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
            String indexStr = "[" + i + "]";
            fm = g2d.getFontMetrics();
            g2d.drawString(indexStr, nodeX - fm.stringWidth(indexStr) / 2, nodeY + nodeHeight / 2 + 12);
        }
    }

    private void drawRelationshipLabels(Graphics2D g2d) {
        List<VisualElement> elements = queue.getElements();
        int size = elements.size();
        if (size == 0) return;

        int levels = (int) (Math.log(size) / Math.log(2)) + 1;
        int formulaY = 90 + levels * 70 + 25;

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("INDEX FORMULAS:", 20, formulaY);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("parent=(i-1)/2    left=2i+1    right=2i+2", 20, formulaY + 15);
    }

    private void drawArray(Graphics2D g2d) {
        List<VisualElement> elements = queue.getElements();
        int size = elements.size();
        int levels = size > 0 ? (int) (Math.log(size) / Math.log(2)) + 1 : 1;

        int arrayStartY = 90 + levels * 70 + 50;
        int capacity = queue.getCapacity();
        List<Integer> highlighted = queue.getHighlightedIndices();

        // Section label
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("ARRAY VIEW: Object[" + capacity + "]", 20, arrayStartY);
        arrayStartY += 15;

        int cellWidth = 60;
        int cellHeight = 45;
        int cols = Math.min(capacity, 11);
        int startX = 20;

        for (int i = 0; i < capacity; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * cellWidth;
            int y = arrayStartY + row * (cellHeight + 20);

            boolean isHighlighted = highlighted.contains(i);
            boolean hasElement = i < size;

            // Cell background
            Color cellBg = hasElement ?
                    new Color(50, 25, 30, 200) :
                    new Color(30, 15, 18, 150);
            g2d.setColor(cellBg);
            g2d.fillRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 6, 6);

            // Highlighted glow
            if (isHighlighted && hasElement) {
                float glow = (float) (0.5 + 0.5 * Math.sin(glowPhase * 4));
                g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (80 * glow)));
                g2d.fillRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 6, 6);
            }

            // Cell border
            Color borderColor = isHighlighted && hasElement ? ACCENT : EDGE_COLOR;
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(isHighlighted && hasElement ? 2f : 1f));
            g2d.drawRoundRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4, 6, 6);

            // Index label
            g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
            g2d.setColor(new Color(180, 130, 130));
            g2d.drawString("[" + i + "]", x + 5, y + 14);

            // Value
            if (hasElement) {
                VisualElement element = elements.get(i);
                double alpha = element.getAlpha();
                g2d.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
                g2d.setFont(new Font("Consolas", Font.BOLD, 11));
                String display = element.getDisplayValue();
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (cellWidth - fm.stringWidth(display)) / 2;
                g2d.drawString(display, textX, y + cellHeight / 2 + 8);
            }
        }
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 130;

        g2d.setColor(new Color(40, 20, 25, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("HEAP STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + queue.getSize(), panelX + 15, y);
        y += 15;
        g2d.drawString("Capacity: " + queue.getCapacity(), panelX + 15, y);
        y += 15;
        int levels = queue.getSize() > 0 ? (int) (Math.log(queue.getSize()) / Math.log(2)) + 1 : 0;
        g2d.drawString("Levels: " + levels, panelX + 15, y);
        y += 15;
        g2d.drawString("Operations: " + queue.getOperationsCount(), panelX + 15, y);
        y += 15;

        String op = queue.getLastOperation();
        if (op.length() > 25) op = op.substring(0, 22) + "...";
        g2d.setColor(new Color(200, 150, 150));
        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.drawString(op, panelX + 15, y);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(40, 20, 25, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        VisualPriorityQueue.MemoryInfo mem = queue.getMemoryInfo();
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + mem.formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(200, 150, 150));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
