package game.ui;

import game.model.VisualHashSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.ui.ThemeManager;

public class HashSetMemoryDialog extends JDialog {
    private VisualHashSet hashSet;
    private Timer animationTimer;
    private double glowPhase = 0;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(255, 150, 80);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();

    public HashSetMemoryDialog(Frame parent, VisualHashSet hashSet) {
        super(parent, "HashSet Memory Analysis", true);
        this.hashSet = hashSet;

        setSize(650, 700);
        setLocationRelativeTo(parent);
        setBackground(BG_COLOR);

        JPanel content = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        animationTimer = new Timer(50, e -> {
            glowPhase += 0.1;
            repaint();
        });
        animationTimer.start();
    }

    @Override
    public void dispose() {
        animationTimer.stop();
        super.dispose();
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSummaryPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createHashSetStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createNodeStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createBucketAnalysisPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualHashSet.MemoryInfo mem = hashSet.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "HashSet overhead:  " + mem.hashSetOverhead() + " bytes",
                "Node[] table:      " + mem.tableOverhead() + " bytes",
                "HashSet.Node:      " + mem.formatNodes(),
                "Element objects:   " + mem.formatElements(),
                "",
                "Empty buckets:     " + mem.emptyBuckets() + " (" + mem.wastedMemory() + " bytes wasted)"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("Total") ? ACCENT : TEXT_COLOR);
            label.setFont(new Font("Consolas", line.contains("Total") ? Font.BOLD : Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createHashSetStructurePanel() {
        JPanel panel = createSection("HASHSET OBJECT STRUCTURE (32 bytes)");

        String[] diagram = {
                "+--------------------------------+",
                "|     HASHSET OBJECT (32B)       |",
                "+--------------------------------+",
                "| Object header      | 12 bytes  |",
                "| int size           |  4 bytes  |",
                "| float loadFactor   |  4 bytes  |",
                "| int threshold      |  4 bytes  |",
                "| Node[] table ref   |  4 bytes  |",
                "| padding            |  4 bytes  |",
                "+--------------------------------+",
                "",
                "  table ref",
                "     |",
                "     v",
                "+------------------------------------------+",
                "| NODE[] ARRAY (16 + capacity*4 bytes)     |",
                "+------------------------------------------+",
                "| header (16B) | [0] | [1] | [2] | ... |   |",
                "+------------------------------------------+"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("HASHSET") || line.contains("NODE[]") ?
                    ACCENT : new Color(180, 160, 130));
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createNodeStructurePanel() {
        JPanel panel = createSection("HASHSET.NODE STRUCTURE (32 bytes each)");

        String[] diagram = {
                "+--------------------------------+",
                "|    HASHSET.NODE (32 bytes)     |",
                "+--------------------------------+",
                "| Object header     | 12 bytes   |",
                "| int hash          |  4 bytes   |  <- cached hashCode()",
                "| Object key ref    |  4 bytes   |  -> actual element",
                "| Object value ref  |  4 bytes   |  -> PRESENT (dummy)",
                "| Node next ref     |  4 bytes   |  -> collision chain",
                "| padding           |  4 bytes   |",
                "+--------------------------------+",
                "",
                "Collision chain example:",
                "",
                "bucket[5] -> Node(42) -> Node(58) -> null",
                "                |           |",
                "                v           v",
                "            Integer      Integer",
                "            (16 B)       (16 B)"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("NODE") ? ACCENT :
                    line.contains("->") ? new Color(150, 200, 255) :
                            new Color(180, 160, 130));
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createBucketAnalysisPanel() {
        JPanel panel = createSection("BUCKET ANALYSIS");

        int capacity = hashSet.getCapacity();
        int size = hashSet.getSize();
        int collisions = hashSet.getCollisionCount();
        int maxChain = hashSet.getMaxChainLength();
        double load = hashSet.getCurrentLoad();

        String[] lines = {
                "Capacity:       " + capacity + " buckets",
                "Size:           " + size + " elements",
                "Load factor:    " + String.format("%.1f%%", load * 100) + " (threshold: 75%)",
                "",
                "Collisions:     " + collisions,
                "Max chain:      " + maxChain,
                "",
                "Distribution quality:"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("Capacity") || line.contains("Size") ? ACCENT : TEXT_COLOR);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        JPanel barPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int barWidth = getWidth() - 40;
                int barHeight = 20;
                int x = 20;
                int y = 10;

                g2d.setColor(new Color(40, 30, 20));
                g2d.fillRoundRect(x, y, barWidth, barHeight, 5, 5);

                double loadPercent = Math.min(load, 1.0);
                int fillWidth = (int)(barWidth * loadPercent);
                Color loadColor = load > 0.75 ? new Color(255, 100, 100) :
                        load > 0.5 ? new Color(255, 200, 100) : new Color(100, 255, 150);
                g2d.setColor(loadColor);
                g2d.fillRoundRect(x, y, fillWidth, barHeight, 5, 5);

                int thresholdX = x + (int)(barWidth * 0.75);
                g2d.setColor(new Color(255, 100, 100));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine(thresholdX, y - 3, thresholdX, y + barHeight + 3);

                g2d.setColor(TEXT_COLOR);
                g2d.setFont(new Font("Consolas", Font.PLAIN, 9));
                g2d.drawString("0%", x, y + barHeight + 12);
                g2d.drawString("75%", thresholdX - 10, y + barHeight + 12);
                g2d.drawString("100%", x + barWidth - 20, y + barHeight + 12);
            }
        };
        barPanel.setBackground(PANEL_BG);
        barPanel.setPreferredSize(new Dimension(580, 50));
        barPanel.setMaximumSize(new Dimension(580, 50));
        barPanel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(barPanel);

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("HASHSET vs OTHER COLLECTIONS");

        int n = Math.max(hashSet.getSize(), 10);

        String[] lines = {
                "For " + n + " Integer elements:",
                "",
                String.format("HashSet:     ~%d bytes", 32 + 16 + n*4 + n*32 + n*16),
                String.format("  - O(1) add, remove, contains"),
                String.format("  - No order guarantee"),
                "",
                String.format("ArrayList:   ~%d bytes", 24 + 16 + n*4 + n*16),
                String.format("  - O(1) add, O(n) contains"),
                String.format("  - Maintains insertion order"),
                "",
                String.format("TreeSet:     ~%d bytes", 80 + n*40 + n*16),
                String.format("  - O(log n) add, remove, contains"),
                String.format("  - Sorted order"),
                "",
                "HashSet is best when:",
                "  - Need fast contains() check",
                "  - No duplicates allowed",
                "  - Order doesn't matter"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("HashSet:") || line.contains("ArrayList:") || line.contains("TreeSet:")) {
                color = ACCENT;
            } else if (line.contains("O(1)") || line.contains("O(log n)") || line.contains("O(n)")) {
                color = new Color(150, 200, 255);
            } else if (line.startsWith("  -")) {
                color = new Color(180, 160, 130);
            }
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                new EmptyBorder(12, 15, 12, 15)
        ));
        panel.setMaximumSize(new Dimension(600, 2000));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(ACCENT);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }
}
