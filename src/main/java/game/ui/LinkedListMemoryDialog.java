package game.ui;

import game.model.VisualLinkedList;
import game.model.VisualNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkedListMemoryDialog extends JDialog {
    private static final Color BG_COLOR = new Color(15, 10, 25);
    private static final Color PANEL_BG = new Color(30, 25, 45);
    private static final Color ACCENT = new Color(255, 100, 150);
    private static final Color TEXT_COLOR = new Color(220, 200, 230);
    private static final Color HEADER_COLOR = new Color(255, 150, 200);

    private VisualLinkedList linkedList;
    private Timer animationTimer;
    private double glowPhase = 0;

    public LinkedListMemoryDialog(JFrame parent, VisualLinkedList linkedList) {
        super(parent, "LINKEDLIST MEMORY ANALYSIS", true);
        this.linkedList = linkedList;

        setSize(700, 700);
        setLocationRelativeTo(parent);
        setBackground(BG_COLOR);

        initUI();

        animationTimer = new Timer(50, e -> {
            glowPhase += 0.1;
            repaint();
        });
        animationTimer.start();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 15, 30),
                        0, getHeight(), new Color(15, 10, 25));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = createStyledButton("CLOSE");
        closeBtn.addActionListener(e -> {
            animationTimer.stop();
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        var memInfo = linkedList.getMemoryInfo();
        List<VisualNode> nodes = linkedList.getNodes();
        int size = linkedList.getSize();

        content.add(createSection("LINKEDLIST MEMORY ANALYSIS", null));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("SUMMARY", createSummaryPanel(memInfo, size)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("LINKEDLIST OBJECT STRUCTURE", createLinkedListStructurePanel()));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("NODE STRUCTURE", createNodeStructurePanel()));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("ELEMENTS BY TYPE", createTypeBreakdownPanel(nodes)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("MEMORY OVERHEAD ANALYSIS", createOverheadPanel(memInfo, size)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("COMPARISON: LinkedList vs ArrayList", createComparisonPanel(size)));

        return content;
    }

    private JPanel createSection(String title, JPanel contentPanel) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(650, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(HEADER_COLOR);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        section.add(titleLabel);

        if (contentPanel != null) {
            section.add(Box.createVerticalStrut(8));
            contentPanel.setAlignmentX(LEFT_ALIGNMENT);
            section.add(contentPanel);
        }

        return section;
    }

    private JPanel createSummaryPanel(VisualLinkedList.MemoryInfo memInfo, int size) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 8));

        addLabelPair(panel, "Total Memory Used:", memInfo.formatTotal(), ACCENT);
        addLabelPair(panel, "LinkedList Object:", "32 B", new Color(255, 200, 100));
        addLabelPair(panel, "Node Objects:", memInfo.formatNodes() + " (" + size + " nodes)", new Color(255, 150, 100));
        addLabelPair(panel, "Element Objects:", memInfo.formatElements(), new Color(0, 200, 255));

        return panel;
    }

    private JPanel createLinkedListStructurePanel() {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] diagram = {
            "+---------------------------------------------------+",
            "|         LinkedList<E> Object                      |",
            "+---------------------------------------------------+",
            "|  Object Header      | 12 bytes                    |",
            "+---------------------------------------------------+",
            "|  int size           |  4 bytes                    |",
            "|  int modCount       |  4 bytes                    |",
            "|  Node<E> first      |  4 bytes (ref)              |",
            "|  Node<E> last       |  4 bytes (ref)              |",
            "+---------------------------------------------------+",
            "|  Subtotal           | 28 bytes                    |",
            "|  Padding            |  4 bytes (to 8-byte align)  |",
            "+---------------------------------------------------+",
            "|  TOTAL              | 32 bytes                    |",
            "+---------------------------------------------------+"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setForeground(TEXT_COLOR);
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createNodeStructurePanel() {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] diagram = {
            "+---------------------------------------------------+",
            "|         Node<E> (inner class)                     |",
            "+---------------------------------------------------+",
            "|  Object Header      | 12 bytes                    |",
            "+---------------------------------------------------+",
            "|  E item             |  4 bytes (ref to element)   |",
            "|  Node<E> next       |  4 bytes (ref to next node) |",
            "|  Node<E> prev       |  4 bytes (ref to prev node) |",
            "+---------------------------------------------------+",
            "|  TOTAL per Node     | 24 bytes                    |",
            "+---------------------------------------------------+",
            "",
            "  +------+------+------+",
            "  | prev | item | next |  <- Each node structure",
            "  +------+------+------+",
            "     |      |      |",
            "     v      v      v",
            "   Node   Object  Node",
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setForeground(TEXT_COLOR);
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createTypeBreakdownPanel(List<VisualNode> nodes) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Map<String, Integer> typeCounts = new HashMap<>();
        Map<String, Long> typeMemory = new HashMap<>();

        for (VisualNode node : nodes) {
            if (!node.isRemoving()) {
                String type = node.getType().toLowerCase();
                typeCounts.merge(type, 1, Integer::sum);
                long mem = estimateObjectSize(node.getValue(), type);
                typeMemory.merge(type, mem, Long::sum);
            }
        }

        if (typeCounts.isEmpty()) {
            JLabel empty = new JLabel("No elements in LinkedList");
            empty.setFont(new Font("Consolas", Font.ITALIC, 12));
            empty.setForeground(new Color(150, 130, 170));
            panel.add(empty);
            return panel;
        }

        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(600, 25));
        headerRow.setAlignmentX(LEFT_ALIGNMENT);
        addHeaderLabel(headerRow, "TYPE");
        addHeaderLabel(headerRow, "COUNT");
        addHeaderLabel(headerRow, "ELEMENT SIZE");
        addHeaderLabel(headerRow, "TOTAL (with node)");
        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(5));

        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            long elemMem = typeMemory.get(type);
            long totalWithNodes = elemMem + count * 24L; // 24 bytes per node

            JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(600, 22));
            row.setAlignmentX(LEFT_ALIGNMENT);

            addDataLabel(row, type.toUpperCase(), getColorForType(type));
            addDataLabel(row, String.valueOf(count), TEXT_COLOR);
            addDataLabel(row, formatBytes(elemMem / count) + " each", TEXT_COLOR);
            addDataLabel(row, formatBytes(totalWithNodes), ACCENT);

            panel.add(row);
        }

        return panel;
    }

    private JPanel createOverheadPanel(VisualLinkedList.MemoryInfo memInfo, int size) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        long nodeOverhead = size * 24L;
        long totalMem = memInfo.totalUsed();
        double overheadPercent = totalMem > 0 ? (double) nodeOverhead / totalMem * 100 : 0;

        JLabel overheadLabel = new JLabel(String.format(
            "Node overhead: %s (%.1f%% of total memory)",
            formatBytes(nodeOverhead), overheadPercent));
        overheadLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        overheadLabel.setForeground(new Color(255, 200, 100));
        overheadLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(overheadLabel);

        panel.add(Box.createVerticalStrut(10));

        // Per-element cost
        if (size > 0) {
            long perElementCost = totalMem / size;
            JLabel perElemLabel = new JLabel(String.format(
                "Average memory per element: %s (including node)", formatBytes(perElementCost)));
            perElemLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            perElemLabel.setForeground(TEXT_COLOR);
            perElemLabel.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(perElemLabel);
        }

        panel.add(Box.createVerticalStrut(10));

        // Key insight
        JLabel insight = new JLabel("Each element requires 24 extra bytes for Node wrapper!");
        insight.setFont(new Font("Consolas", Font.BOLD, 11));
        insight.setForeground(new Color(255, 100, 100));
        insight.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(insight);

        return panel;
    }

    private JPanel createComparisonPanel(int size) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (size == 0) size = 10;

        // LinkedList memory: 32 + size * (24 + 16) for Integer elements
        long linkedListMem = 32 + size * (24L + 16);

        // ArrayList memory: 24 + 16 + size * 4 + size * 16
        int capacity = Math.max(10, size + size / 2);
        long arrayListMem = 24 + 16 + capacity * 4L + size * 16L;

        String[] comparison = {
            String.format("Storing %d Integer elements:", size),
            "",
            String.format("LinkedList<Integer>:  %s", formatBytes(linkedListMem)),
            "  +- LinkedList object: 32 bytes",
            String.format("  +- Node objects:      %s (%d x 24 bytes)", formatBytes(size * 24L), size),
            String.format("  +- Integer objects:   %s (%d x 16 bytes)", formatBytes(size * 16L), size),
            "",
            String.format("ArrayList<Integer>:   %s", formatBytes(arrayListMem)),
            "  +- ArrayList object:  24 bytes",
            String.format("  +- Object[] array:    %s (%d refs)", formatBytes(16 + capacity * 4L), capacity),
            String.format("  +- Integer objects:   %s (%d x 16 bytes)", formatBytes(size * 16L), size),
            "",
            String.format("LinkedList uses %.1fx more memory than ArrayList!",
                    (double) linkedListMem / arrayListMem),
        };

        for (String line : comparison) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setForeground(line.contains("more memory") ? new Color(255, 100, 100) : TEXT_COLOR);
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        panel.add(Box.createVerticalStrut(10));

        String[] tradeoffs = {
            "WHEN TO USE LINKEDLIST:",
            "  + Frequent insertions/deletions at beginning: O(1)",
            "  + No capacity management needed",
            "  + Implements Deque interface (addFirst, addLast)",
            "",
            "WHEN TO USE ARRAYLIST:",
            "  + Random access by index: O(1) vs O(n)",
            "  + Better memory efficiency",
            "  + Better cache locality (faster iteration)"
        };

        for (String line : tradeoffs) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setForeground(line.startsWith("WHEN") ? HEADER_COLOR : new Color(150, 170, 200));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createStyledPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 25, 45, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase));
                g2d.setColor(new Color(255, 100, 150, (int)(glow * 80)));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));
        panel.setOpaque(false);
        return panel;
    }

    private void addLabelPair(JPanel panel, String label, String value, Color valueColor) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Consolas", Font.PLAIN, 12));
        labelComp.setForeground(TEXT_COLOR);
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Consolas", Font.BOLD, 12));
        valueComp.setForeground(valueColor);
        panel.add(labelComp);
        panel.add(valueComp);
    }

    private void addHeaderLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Consolas", Font.BOLD, 11));
        label.setForeground(HEADER_COLOR);
        panel.add(label);
    }

    private void addDataLabel(JPanel panel, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Consolas", Font.PLAIN, 11));
        label.setForeground(color);
        panel.add(label);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2d.setColor(ACCENT);
                else if (getModel().isRollover()) g2d.setColor(ACCENT.darker());
                else g2d.setColor(PANEL_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.setColor(ACCENT);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        button.setFont(new Font("Consolas", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 35));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private Color getColorForType(String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> new Color(0, 200, 255);
            case "double" -> new Color(255, 100, 200);
            case "string" -> new Color(100, 255, 150);
            case "boolean" -> new Color(255, 200, 50);
            case "char", "character" -> new Color(200, 100, 255);
            default -> new Color(255, 150, 100);
        };
    }

    private long estimateObjectSize(Object value, String type) {
        return switch (type.toLowerCase()) {
            case "int", "integer" -> 16;
            case "double" -> 24;
            case "boolean" -> 16;
            case "char", "character" -> 16;
            case "string" -> {
                String s = (String) value;
                yield 24 + 16 + ((s.length() + 7) / 8) * 8;
            }
            default -> 24;
        };
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
