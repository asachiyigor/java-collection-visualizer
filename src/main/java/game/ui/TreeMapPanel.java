package game.ui;

import game.model.VisualTreeNode;
import game.model.VisualTreeMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TreeMapPanel extends JPanel {
    private VisualTreeMap treeMap;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static final Color BG_COLOR = new Color(12, 20, 18);
    private static final Color GRID_COLOR = new Color(28, 45, 40);
    private static final Color ACCENT = new Color(100, 220, 180);
    private static final Color TEXT_COLOR = new Color(210, 250, 235);
    private static final Color RED_NODE = new Color(255, 100, 100);
    private static final Color BLACK_NODE = new Color(50, 50, 50);

    public TreeMapPanel(VisualTreeMap treeMap) {
        this.treeMap = treeMap;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            treeMap.update(delta);
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
            TreeMapMemoryDialog dialog = new TreeMapMemoryDialog((Frame) window, treeMap);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int height = treeMap.getHeight();
        int size = treeMap.getSize();
        int width = Math.max(900, 100 + size * 50);
        int panelHeight = Math.max(600, 120 + height * 85);
        return new Dimension(width, panelHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawTree(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(0, 0, BG_COLOR, 0, getHeight(), new Color(18, 32, 28));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        int gridSize = 40;
        for (int x = 0; x < getWidth(); x += gridSize) g2d.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += gridSize) g2d.drawLine(0, y, getWidth(), y);
    }

    private void drawTree(Graphics2D g2d) {
        VisualTreeNode root = treeMap.getRoot();
        if (root == null) {
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Consolas", Font.ITALIC, 14));
            g2d.drawString("Empty tree - add entries to visualize", 250, 250);
            return;
        }

        List<VisualTreeNode> highlighted = treeMap.getHighlightedPath();
        drawEdges(g2d, root, highlighted);
        drawNodes(g2d, root, highlighted);
    }

    private void drawEdges(Graphics2D g2d, VisualTreeNode node, List<VisualTreeNode> highlighted) {
        if (node == null || node.getAlpha() <= 0) return;

        boolean isHighlighted = highlighted.contains(node);
        double alpha = node.getAlpha();

        if (node.getLeft() != null && node.getLeft().getAlpha() > 0) {
            boolean childHighlighted = highlighted.contains(node.getLeft());
            g2d.setColor(new Color(80, 150, 130, (int)((isHighlighted && childHighlighted ? 200 : 100) * alpha)));
            g2d.setStroke(new BasicStroke(isHighlighted && childHighlighted ? 3f : 2f));
            g2d.drawLine((int)node.getX(), (int)node.getY() + 25, (int)node.getLeft().getX(), (int)node.getLeft().getY() - 25);
        }

        if (node.getRight() != null && node.getRight().getAlpha() > 0) {
            boolean childHighlighted = highlighted.contains(node.getRight());
            g2d.setColor(new Color(80, 150, 130, (int)((isHighlighted && childHighlighted ? 200 : 100) * alpha)));
            g2d.setStroke(new BasicStroke(isHighlighted && childHighlighted ? 3f : 2f));
            g2d.drawLine((int)node.getX(), (int)node.getY() + 25, (int)node.getRight().getX(), (int)node.getRight().getY() - 25);
        }

        drawEdges(g2d, node.getLeft(), highlighted);
        drawEdges(g2d, node.getRight(), highlighted);
    }

    private void drawNodes(Graphics2D g2d, VisualTreeNode node, List<VisualTreeNode> highlighted) {
        if (node == null) return;

        double x = node.getX();
        double y = node.getY();
        double alpha = node.getAlpha();
        double scale = node.getScale();

        if (alpha <= 0 || scale <= 0) return;

        int nodeWidth = (int)(65 * scale);
        int nodeHeight = (int)(50 * scale);

        boolean isHighlighted = highlighted.contains(node);

        if (isHighlighted) {
            float glow = (float) (0.5 + 0.5 * Math.sin(glowPhase * 4));
            g2d.setColor(new Color(100, 220, 180, (int)(100 * glow * alpha)));
            g2d.fillRoundRect((int)x - nodeWidth/2 - 5, (int)y - nodeHeight/2 - 5, nodeWidth + 10, nodeHeight + 10, 15, 15);
        }

        Color nodeColor = node.isRed() ? RED_NODE : BLACK_NODE;
        g2d.setColor(new Color(nodeColor.getRed()/3, nodeColor.getGreen()/3, nodeColor.getBlue()/3, (int)(220 * alpha)));
        g2d.fillRoundRect((int)x - nodeWidth/2, (int)y - nodeHeight/2, nodeWidth, nodeHeight, 10, 10);

        Color borderColor = node.isRed() ? new Color(255, 140, 140, (int)(255 * alpha)) :
                new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(255 * alpha));
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(node.isRed() ? 2.5f : 2f));
        g2d.drawRoundRect((int)x - nodeWidth/2, (int)y - nodeHeight/2, nodeWidth, nodeHeight, 10, 10);

        g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        String keyDisplay = node.getDisplayKey();
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(keyDisplay, (int)x - fm.stringWidth(keyDisplay)/2, (int)y - 5);

        String valueDisplay = node.getDisplayValue();
        if (valueDisplay != null) {
            g2d.setColor(new Color(180, 230, 210, (int)(200 * alpha)));
            g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
            if (valueDisplay.length() > 8) valueDisplay = valueDisplay.substring(0, 6) + "..";
            fm = g2d.getFontMetrics();
            g2d.drawString(valueDisplay, (int)x - fm.stringWidth(valueDisplay)/2, (int)y + 10);
        }

        String colorStr = node.isRed() ? "R" : "B";
        g2d.setColor(node.isRed() ? new Color(255, 150, 150, (int)(200 * alpha)) : new Color(150, 200, 180, (int)(200 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 8));
        g2d.drawString(colorStr, (int)x + nodeWidth/2 - 10, (int)y - nodeHeight/2 + 12);

        drawNodes(g2d, node.getLeft(), highlighted);
        drawNodes(g2d, node.getRight(), highlighted);
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("TREEMAP VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Red-Black Tree with Key-Value Pairs ]", 20, 55);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(RED_NODE);
        g2d.drawString("R = Red", 20, 75);
        g2d.setColor(new Color(150, 200, 180));
        g2d.drawString("B = Black", 80, 75);
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 120;

        g2d.setColor(new Color(20, 35, 30, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("TREEMAP STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + treeMap.getSize(), panelX + 15, y);
        y += 15;
        g2d.drawString("Height: " + treeMap.getHeight(), panelX + 15, y);
        y += 15;
        g2d.drawString("Rotations: " + treeMap.getRotationCount(), panelX + 15, y);
        y += 15;
        g2d.drawString("Sorted by keys: YES", panelX + 15, y);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(20, 35, 30, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + treeMap.getMemoryInfo().formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 200, 180));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
