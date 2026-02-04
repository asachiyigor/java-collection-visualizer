package game.ui;

import game.model.VisualTreeNode;
import game.model.VisualTreeSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import game.ui.ThemeManager;

public class TreeSetPanel extends JPanel {
    private VisualTreeSet treeSet;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color GRID_COLOR = ThemeManager.get().getGridColor();
    private static final Color ACCENT = new Color(80, 200, 200);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color RED_NODE = new Color(255, 80, 80);
    private static final Color BLACK_NODE = new Color(60, 60, 60);

    public TreeSetPanel(VisualTreeSet treeSet) {
        this.treeSet = treeSet;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            treeSet.update(delta);
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
            TreeSetMemoryDialog dialog = new TreeSetMemoryDialog((Frame) window, treeSet);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int height = treeSet.getHeight();
        int size = treeSet.getSize();
        int width = Math.max(900, 100 + size * 40);
        int panelHeight = Math.max(600, 120 + height * 80);
        return new Dimension(width, panelHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2d);
        drawTree(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, BG_COLOR,
                0, getHeight(), new Color(18, 35, 35)
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

    private void drawTree(Graphics2D g2d) {
        VisualTreeNode root = treeSet.getRoot();
        if (root == null) {
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Consolas", Font.ITALIC, 14));
            g2d.drawString("Empty tree - add elements to visualize", 250, 250);
            return;
        }

        List<VisualTreeNode> highlighted = treeSet.getHighlightedPath();
        drawEdges(g2d, root, highlighted);
        drawNodes(g2d, root, highlighted);
    }

    private void drawEdges(Graphics2D g2d, VisualTreeNode node, List<VisualTreeNode> highlighted) {
        if (node == null) return;

        double alpha = node.getAlpha();
        if (alpha <= 0) return;

        boolean isHighlighted = highlighted.contains(node);

        if (node.getLeft() != null && node.getLeft().getAlpha() > 0) {
            boolean childHighlighted = highlighted.contains(node.getLeft());
            Color edgeColor = (isHighlighted && childHighlighted) ?
                    new Color(80, 200, 200, (int)(200 * alpha)) :
                    new Color(60, 100, 100, (int)(150 * alpha));
            g2d.setColor(edgeColor);
            g2d.setStroke(new BasicStroke(isHighlighted && childHighlighted ? 3f : 2f));
            g2d.drawLine((int) node.getX(), (int) node.getY() + 20,
                    (int) node.getLeft().getX(), (int) node.getLeft().getY() - 20);
        }

        if (node.getRight() != null && node.getRight().getAlpha() > 0) {
            boolean childHighlighted = highlighted.contains(node.getRight());
            Color edgeColor = (isHighlighted && childHighlighted) ?
                    new Color(80, 200, 200, (int)(200 * alpha)) :
                    new Color(60, 100, 100, (int)(150 * alpha));
            g2d.setColor(edgeColor);
            g2d.setStroke(new BasicStroke(isHighlighted && childHighlighted ? 3f : 2f));
            g2d.drawLine((int) node.getX(), (int) node.getY() + 20,
                    (int) node.getRight().getX(), (int) node.getRight().getY() - 20);
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

        int nodeWidth = (int)(55 * scale);
        int nodeHeight = (int)(40 * scale);

        boolean isHighlighted = highlighted.contains(node);

        if (isHighlighted) {
            float glow = (float) (0.5 + 0.5 * Math.sin(glowPhase * 4));
            g2d.setColor(new Color(80, 200, 200, (int)(100 * glow * alpha)));
            g2d.fillRoundRect((int) x - nodeWidth / 2 - 5, (int) y - nodeHeight / 2 - 5,
                    nodeWidth + 10, nodeHeight + 10, 15, 15);
        }

        Color nodeColor = node.isRed() ? RED_NODE : BLACK_NODE;
        Color bgColor = new Color(nodeColor.getRed()/3, nodeColor.getGreen()/3, nodeColor.getBlue()/3, (int)(220 * alpha));
        g2d.setColor(bgColor);
        g2d.fillRoundRect((int) x - nodeWidth / 2, (int) y - nodeHeight / 2, nodeWidth, nodeHeight, 10, 10);

        Color borderColor = node.isRed() ?
                new Color(255, 120, 120, (int)(255 * alpha)) :
                new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(255 * alpha));
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(node.isRed() ? 2.5f : 2f));
        g2d.drawRoundRect((int) x - nodeWidth / 2, (int) y - nodeHeight / 2, nodeWidth, nodeHeight, 10, 10);

        g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 12));
        String display = node.getDisplayKey();
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(display, (int) x - fm.stringWidth(display) / 2, (int) y + 4);

        String colorStr = node.isRed() ? "R" : "B";
        g2d.setColor(node.isRed() ?
                new Color(255, 150, 150, (int)(200 * alpha)) :
                new Color(150, 200, 200, (int)(200 * alpha)));
        g2d.setFont(new Font("Consolas", Font.BOLD, 9));
        g2d.drawString(colorStr, (int) x + nodeWidth / 2 - 10, (int) y - nodeHeight / 2 + 12);

        drawNodes(g2d, node.getLeft(), highlighted);
        drawNodes(g2d, node.getRight(), highlighted);
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("TREESET VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Self-Balancing Red-Black Tree ]", 20, 55);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(RED_NODE);
        g2d.drawString("R = Red node", 20, 75);
        g2d.setColor(new Color(150, 200, 200));
        g2d.drawString("B = Black node", 100, 75);
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 130;

        g2d.setColor(new Color(20, 35, 35, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("TREE STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + treeSet.getSize(), panelX + 15, y);
        y += 15;
        g2d.drawString("Height: " + treeSet.getHeight(), panelX + 15, y);
        y += 15;
        g2d.drawString("Rotations: " + treeSet.getRotationCount(), panelX + 15, y);
        y += 15;
        g2d.drawString("Operations: " + treeSet.getOperationsCount(), panelX + 15, y);
        y += 15;

        String op = treeSet.getLastOperation();
        if (op.length() > 25) op = op.substring(0, 22) + "...";
        g2d.setColor(new Color(150, 200, 200));
        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.drawString(op, panelX + 15, y);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(20, 35, 35, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        VisualTreeSet.MemoryInfo mem = treeSet.getMemoryInfo();
        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + mem.formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(150, 200, 200));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
