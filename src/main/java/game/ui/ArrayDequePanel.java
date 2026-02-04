package game.ui;

import game.model.VisualArrayDeque;
import game.model.VisualElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import game.ui.ThemeManager;

public class ArrayDequePanel extends JPanel {
    private VisualArrayDeque deque;
    private Timer timer;
    private double glowPhase = 0;
    private long lastTime;
    private Rectangle memoryPanelBounds = new Rectangle();

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color GRID_COLOR = ThemeManager.get().getGridColor();
    private static final Color ACCENT = new Color(180, 100, 255);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color HEAD_COLOR = new Color(100, 255, 150);
    private static final Color TAIL_COLOR = new Color(255, 100, 150);

    public ArrayDequePanel(VisualArrayDeque deque) {
        this.deque = deque;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;
            deque.update(delta);
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
            ArrayDequeMemoryDialog dialog = new ArrayDequeMemoryDialog((Frame) window, deque);
            dialog.setVisible(true);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int capacity = deque.getCapacity();
        int radius = 180 + (capacity > 16 ? (capacity - 16) * 8 : 0);
        int size = Math.max(600, radius * 2 + 200);
        return new Dimension(size, size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawCircularArray(g2d);
        drawHeader(g2d);
        drawStats(g2d);

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(0, 0, BG_COLOR, 0, getHeight(), new Color(28, 18, 40));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        int gridSize = 40;
        for (int x = 0; x < getWidth(); x += gridSize) g2d.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += gridSize) g2d.drawLine(0, y, getWidth(), y);
    }

    private void drawCircularArray(Graphics2D g2d) {
        int centerX = getWidth() / 2 - 50;
        int centerY = getHeight() / 2 + 20;
        int radius = 180;
        int capacity = deque.getCapacity();
        int head = deque.getHead();
        int tail = deque.getTail();
        List<VisualElement> elements = deque.getElements();

        // Draw circular slots
        double angleStep = 2 * Math.PI / capacity;
        int slotWidth = 55;
        int slotHeight = 40;

        for (int i = 0; i < capacity; i++) {
            double angle = -Math.PI / 2 + i * angleStep;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));

            // Slot background
            boolean isHead = (i == head && deque.getSize() > 0);
            boolean isTail = (i == tail);

            Color slotColor = new Color(40, 30, 55, 180);
            if (isHead) slotColor = new Color(HEAD_COLOR.getRed()/4, HEAD_COLOR.getGreen()/4, HEAD_COLOR.getBlue()/4, 200);
            else if (isTail && deque.getSize() > 0) slotColor = new Color(TAIL_COLOR.getRed()/4, TAIL_COLOR.getGreen()/4, TAIL_COLOR.getBlue()/4, 200);

            g2d.setColor(slotColor);
            g2d.fillRoundRect(x - slotWidth/2, y - slotHeight/2, slotWidth, slotHeight, 8, 8);

            // Border
            Color borderColor = ACCENT.darker();
            if (isHead) borderColor = HEAD_COLOR;
            else if (isTail && deque.getSize() > 0) borderColor = TAIL_COLOR;

            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(isHead || isTail ? 2.5f : 1.5f));
            g2d.drawRoundRect(x - slotWidth/2, y - slotHeight/2, slotWidth, slotHeight, 8, 8);

            // Index label
            g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
            g2d.setColor(new Color(150, 130, 180));
            String indexStr = "[" + i + "]";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(indexStr, x - fm.stringWidth(indexStr)/2, y + slotHeight/2 + 12);

            // Head/Tail markers
            if (isHead) {
                g2d.setColor(HEAD_COLOR);
                g2d.setFont(new Font("Consolas", Font.BOLD, 9));
                g2d.drawString("HEAD", x - fm.stringWidth("HEAD")/2, y - slotHeight/2 - 5);
            }
            if (isTail && deque.getSize() > 0 && i != head) {
                g2d.setColor(TAIL_COLOR);
                g2d.setFont(new Font("Consolas", Font.BOLD, 9));
                g2d.drawString("TAIL", x - fm.stringWidth("TAIL")/2, y - slotHeight/2 - 5);
            }
        }

        // Draw elements
        for (int i = 0; i < elements.size(); i++) {
            VisualElement element = elements.get(i);
            int index = (head + i) % capacity;
            double angle = -Math.PI / 2 + index * angleStep;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));

            double alpha = element.getAlpha();
            double scale = element.getScale();
            if (alpha <= 0) continue;

            int w = (int)(50 * scale);
            int h = (int)(35 * scale);

            // Glow for first/last
            if (i == 0 || i == elements.size() - 1) {
                float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase * 3));
                Color glowColor = i == 0 ? HEAD_COLOR : TAIL_COLOR;
                g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(60 * glow * alpha)));
                g2d.fillRoundRect(x - w/2 - 4, y - h/2 - 4, w + 8, h + 8, 12, 12);
            }

            // Element background
            g2d.setColor(new Color(ACCENT.getRed()/3, ACCENT.getGreen()/3, ACCENT.getBlue()/3, (int)(220 * alpha)));
            g2d.fillRoundRect(x - w/2, y - h/2, w, h, 8, 8);

            // Element border
            g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int)(255 * alpha)));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(x - w/2, y - h/2, w, h, 8, 8);

            // Value
            g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
            g2d.setFont(new Font("Consolas", Font.BOLD, 11));
            String display = element.getDisplayValue();
            if (display.length() > 6) display = display.substring(0, 5) + "..";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(display, x - fm.stringWidth(display)/2, y + fm.getAscent()/2 - 2);
        }

        // Center info
        g2d.setColor(ACCENT);
        g2d.setFont(new Font("Consolas", Font.BOLD, 14));
        String centerText = "ArrayDeque";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(centerText, centerX - fm.stringWidth(centerText)/2, centerY - 10);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        String sizeText = "size: " + deque.getSize() + "/" + deque.getCapacity();
        g2d.drawString(sizeText, centerX - g2d.getFontMetrics().stringWidth(sizeText)/2, centerY + 10);
    }

    private void drawHeader(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.setColor(ACCENT);
        g2d.drawString("ARRAYDEQUE VISUALIZER", 20, 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("[ Double-ended Queue - Circular Array ]", 20, 55);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(HEAD_COLOR);
        g2d.drawString("HEAD = front", 20, 75);
        g2d.setColor(TAIL_COLOR);
        g2d.drawString("TAIL = back", 120, 75);
    }

    private void drawStats(Graphics2D g2d) {
        int panelX = getWidth() - 220;
        int panelY = 20;
        int panelWidth = 200;
        int panelHeight = 120;

        g2d.setColor(new Color(35, 25, 50, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        g2d.setColor(ACCENT);
        g2d.drawString("DEQUE STATUS", panelX + 15, panelY + 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        int y = panelY + 40;
        g2d.drawString("Size: " + deque.getSize(), panelX + 15, y);
        y += 15;
        g2d.drawString("Capacity: " + deque.getCapacity(), panelX + 15, y);
        y += 15;
        g2d.drawString("Head index: " + deque.getHead(), panelX + 15, y);
        y += 15;
        g2d.drawString("Tail index: " + deque.getTail(), panelX + 15, y);
        y += 15;
        g2d.drawString("Operations: " + deque.getOperationsCount(), panelX + 15, y);

        drawMemoryPanel(g2d, panelX, panelY + panelHeight + 10);
    }

    private void drawMemoryPanel(Graphics2D g2d, int x, int y) {
        int width = 200;
        int height = 60;
        memoryPanelBounds.setBounds(x, y, width, height);

        g2d.setColor(new Color(35, 25, 50, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        g2d.setColor(ACCENT);
        g2d.drawString("MEMORY USAGE", x + 15, y + 18);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Total: " + deque.getMemoryInfo().formatTotal(), x + 15, y + 35);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2d.setColor(new Color(180, 150, 220));
        g2d.drawString("[CLICK FOR DETAILS]", x + 15, y + 50);
    }
}
