package game.ui;

import game.model.VisualArrayList;
import game.model.VisualElement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import game.ui.ThemeManager;

public class MemoryInfoDialog extends JDialog {
    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(0, 200, 255);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color HEADER_COLOR = new Color(0, 255, 200);
    private static Color WARN_COLOR = ThemeManager.get().getWarnColor();

    private VisualArrayList arrayList;
    private Timer animationTimer;
    private double glowPhase = 0;

    public MemoryInfoDialog(JFrame parent, VisualArrayList arrayList) {
        super(parent, "MEMORY ANALYSIS", true);
        this.arrayList = arrayList;

        setSize(700, 600);
        setLocationRelativeTo(parent);
        setBackground(BG_COLOR);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(15, 20, 35),
                    0, getHeight(), new Color(10, 15, 25)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(30, 40, 55, 50));
                for (int i = 0; i < getWidth(); i += 30) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 30) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBackground(BG_COLOR);
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

        var memInfo = arrayList.getMemoryInfo();
        List<VisualElement> elements = arrayList.getElements();
        int capacity = arrayList.getCapacity();
        int size = arrayList.getSize();

        content.add(createSection("MEMORY LAYOUT ANALYSIS", null));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("SUMMARY", createSummaryPanel(memInfo, capacity, size)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("ARRAYLIST OBJECT STRUCTURE", createArrayListStructurePanel()));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("OBJECT[] ARRAY STRUCTURE", createArrayStructurePanel(capacity)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("ELEMENTS BY TYPE", createTypeBreakdownPanel(elements)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("WRAPPER TYPES MEMORY (64-bit JVM, Compressed OOPs)", createWrapperTypesPanel()));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("EFFICIENCY METRICS", createEfficiencyPanel(memInfo, capacity, size)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("COMPARISON: ArrayList vs primitive[]", createComparisonPanel(elements)));

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

    private JPanel createSummaryPanel(VisualArrayList.MemoryInfo memInfo, int capacity, int size) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 8));

        addLabelPair(panel, "Total Memory Used:", memInfo.formatTotal(), ACCENT);
        addLabelPair(panel, "Total Allocated:", memInfo.formatAllocated(), TEXT_COLOR);
        addLabelPair(panel, "Elements Memory:", memInfo.formatElements(), new Color(0, 200, 255));
        addLabelPair(panel, "Array Overhead:", formatBytes(memInfo.arrayOverhead()), new Color(255, 200, 100));
        addLabelPair(panel, "Wasted (unused slots):", memInfo.formatWasted(), new Color(255, 100, 100));

        return panel;
    }

    private JPanel createArrayListStructurePanel() {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] diagram = {
            "+---------------------------------------------------+",
            "|            ArrayList<E> Object                    |",
            "+---------------------------------------------------+",
            "|  Object Header      | 12 bytes                    |",
            "|    +- Mark Word     |  8 bytes (identity hash,    |",
            "|    |                |          locking, GC)       |",
            "|    +- Class Pointer |  4 bytes (compressed)       |",
            "+---------------------------------------------------+",
            "|  int size           |  4 bytes                    |",
            "|  int modCount       |  4 bytes                    |",
            "|  Object[] elementData (ref) |  4 bytes            |",
            "+---------------------------------------------------+",
            "|  TOTAL              | 24 bytes                    |",
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

    private JPanel createArrayStructurePanel(int capacity) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        long arraySize = 16 + (long) capacity * 4;

        String[] diagram = {
            "+---------------------------------------------------+",
            "|            Object[] elementData                   |",
            "+---------------------------------------------------+",
            "|  Array Header       | 16 bytes                    |",
            "|    +- Mark Word     |  8 bytes                    |",
            "|    +- Class Pointer |  4 bytes                    |",
            "|    +- Array Length  |  4 bytes                    |",
            "+---------------------------------------------------+",
            String.format("|  References         |  4 x %d = %d bytes%s|",
                capacity, capacity * 4, spaces(16 - String.valueOf(capacity * 4).length())),
            "|    +- Each ref      |  4 bytes (compressed OOP)   |",
            "+---------------------------------------------------+",
            String.format("|  TOTAL              | %d bytes%s|",
                arraySize, spaces(22 - String.valueOf(arraySize).length())),
            "+---------------------------------------------------+"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setForeground(TEXT_COLOR);
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        panel.add(Box.createVerticalStrut(10));
        JLabel note = new JLabel("* Compressed OOPs enabled (heap < 32GB): references are 4 bytes");
        note.setFont(new Font("Consolas", Font.ITALIC, 10));
        note.setForeground(new Color(150, 170, 200));
        note.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(note);

        return panel;
    }

    private String spaces(int count) {
        return " ".repeat(Math.max(0, count));
    }

    private JPanel createTypeBreakdownPanel(List<VisualElement> elements) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Map<String, Integer> typeCounts = new HashMap<>();
        Map<String, Long> typeMemory = new HashMap<>();

        for (VisualElement e : elements) {
            if (!e.isRemoving()) {
                String type = e.getType().toLowerCase();
                typeCounts.merge(type, 1, Integer::sum);
                long mem = estimateObjectSize(e.getValue(), type);
                typeMemory.merge(type, mem, Long::sum);
            }
        }

        if (typeCounts.isEmpty()) {
            JLabel empty = new JLabel("No elements in ArrayList");
            empty.setFont(new Font("Consolas", Font.ITALIC, 12));
            empty.setForeground(new Color(150, 170, 200));
            panel.add(empty);
            return panel;
        }

        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(600, 25));
        headerRow.setAlignmentX(LEFT_ALIGNMENT);
        addHeaderLabel(headerRow, "TYPE");
        addHeaderLabel(headerRow, "COUNT");
        addHeaderLabel(headerRow, "PER ITEM");
        addHeaderLabel(headerRow, "TOTAL");
        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(5));

        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            long totalMem = typeMemory.get(type);
            long perItem = totalMem / count;

            JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(600, 22));
            row.setAlignmentX(LEFT_ALIGNMENT);

            Color typeColor = getColorForType(type);
            addDataLabel(row, type.toUpperCase(), typeColor);
            addDataLabel(row, String.valueOf(count), TEXT_COLOR);
            addDataLabel(row, formatBytes(perItem), TEXT_COLOR);
            addDataLabel(row, formatBytes(totalMem), ACCENT);

            panel.add(row);
        }

        panel.add(Box.createVerticalStrut(8));
        long totalElements = typeMemory.values().stream().mapToLong(Long::longValue).sum();
        JLabel totalLabel = new JLabel("TOTAL ELEMENTS MEMORY: " + formatBytes(totalElements));
        totalLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        totalLabel.setForeground(HEADER_COLOR);
        totalLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(totalLabel);

        return panel;
    }

    private JPanel createWrapperTypesPanel() {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[][] types = {
            {"Integer", "16 bytes", "header(12) + int(4)"},
            {"Long", "24 bytes", "header(12) + long(8) + padding(4)"},
            {"Double", "24 bytes", "header(12) + double(8) + padding(4)"},
            {"Float", "16 bytes", "header(12) + float(4)"},
            {"Boolean", "16 bytes", "header(12) + boolean(1) + padding(3)"},
            {"Character", "16 bytes", "header(12) + char(2) + padding(2)"},
            {"Byte", "16 bytes", "header(12) + byte(1) + padding(3)"},
            {"Short", "16 bytes", "header(12) + short(2) + padding(2)"},
            {"String", "24+ bytes", "header(12) + fields(12) + byte[] array"},
        };

        JPanel headerRow = new JPanel(new GridLayout(1, 3, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(600, 25));
        headerRow.setAlignmentX(LEFT_ALIGNMENT);
        addHeaderLabel(headerRow, "WRAPPER TYPE");
        addHeaderLabel(headerRow, "SIZE");
        addHeaderLabel(headerRow, "BREAKDOWN");
        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(5));

        for (String[] type : types) {
            JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(600, 20));
            row.setAlignmentX(LEFT_ALIGNMENT);

            addDataLabel(row, type[0], getColorForType(type[0].toLowerCase()));
            addDataLabel(row, type[1], ACCENT);
            addDataLabel(row, type[2], new Color(150, 170, 200));

            panel.add(row);
        }

        panel.add(Box.createVerticalStrut(10));
        JLabel note = new JLabel("* String: 24 bytes object + byte[] with ~(16 + length) bytes");
        note.setFont(new Font("Consolas", Font.ITALIC, 10));
        note.setForeground(new Color(150, 170, 200));
        note.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(note);

        return panel;
    }

    private JPanel createEfficiencyPanel(VisualArrayList.MemoryInfo memInfo, int capacity, int size) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        double fillRatio = size == 0 ? 0 : (double) size / capacity * 100;
        double wasteRatio = memInfo.totalAllocated() == 0 ? 0 :
            (double) memInfo.wastedMemory() / memInfo.totalAllocated() * 100;
        double overheadRatio = memInfo.totalUsed() == 0 ? 0 :
            (double) memInfo.arrayOverhead() / memInfo.totalUsed() * 100;

        panel.add(createMetricBar("Array Fill Ratio", fillRatio,
            size + " / " + capacity + " slots",
            fillRatio > 75 ? new Color(100, 255, 100) :
            fillRatio > 50 ? new Color(255, 200, 100) : new Color(255, 100, 100)));

        panel.add(Box.createVerticalStrut(15));

        panel.add(createMetricBar("Wasted Space", wasteRatio,
            memInfo.formatWasted() + " unused",
            wasteRatio < 25 ? new Color(100, 255, 100) :
            wasteRatio < 50 ? new Color(255, 200, 100) : new Color(255, 100, 100)));

        panel.add(Box.createVerticalStrut(15));

        panel.add(createMetricBar("Array Overhead", overheadRatio,
            formatBytes(memInfo.arrayOverhead()) + " for Object[]",
            new Color(255, 200, 100)));

        panel.add(Box.createVerticalStrut(15));

        JLabel resizeLabel = new JLabel(String.format(
            "Resize count: %d  |  Growth: newCap = oldCap + (oldCap >> 1)",
            arrayList.getResizeCount()));
        resizeLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        resizeLabel.setForeground(WARN_COLOR);
        resizeLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(resizeLabel);

        if (size < capacity) {
            int nextResize = capacity;
            int afterResize = capacity + (capacity >> 1);
            JLabel prediction = new JLabel(String.format(
                "Next resize at: %d elements -> capacity grows to %d",
                nextResize, afterResize));
            prediction.setFont(new Font("Consolas", Font.PLAIN, 11));
            prediction.setForeground(new Color(150, 170, 200));
            prediction.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(prediction);
        }

        return panel;
    }

    private JPanel createMetricBar(String label, double percentage, String detail, Color barColor) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setAlignmentX(LEFT_ALIGNMENT);
        container.setMaximumSize(new Dimension(600, 45));

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);
        labelRow.setMaximumSize(new Dimension(600, 20));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        nameLabel.setForeground(TEXT_COLOR);

        JLabel valueLabel = new JLabel(String.format("%.1f%% - %s", percentage, detail));
        valueLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        valueLabel.setForeground(barColor);

        labelRow.add(nameLabel, BorderLayout.WEST);
        labelRow.add(valueLabel, BorderLayout.EAST);
        container.add(labelRow);
        container.add(Box.createVerticalStrut(3));

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(40, 50, 65));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                int fillWidth = (int) (getWidth() * Math.min(percentage, 100) / 100);
                if (fillWidth > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, barColor, fillWidth, 0, barColor.darker());
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, fillWidth, getHeight(), 6, 6);
                }

                g2d.setColor(new Color(80, 100, 130));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            }
        };
        bar.setPreferredSize(new Dimension(580, 16));
        bar.setMaximumSize(new Dimension(580, 16));
        bar.setAlignmentX(LEFT_ALIGNMENT);
        container.add(bar);

        return container;
    }

    private JPanel createComparisonPanel(List<VisualElement> elements) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        int intCount = 0;
        for (VisualElement e : elements) {
            if (!e.isRemoving() && e.getType().equalsIgnoreCase("int")) {
                intCount++;
            }
        }

        if (intCount == 0) intCount = 10;

        long arrayListMem = 24 + 16 + intCount * 4 + intCount * 16L;
        long intArrayMem = 16 + intCount * 4L;
        double ratio = (double) arrayListMem / intArrayMem;

        String[] comparison = {
            String.format("Example: storing %d integers", intCount),
            "",
            String.format("ArrayList<Integer>:  %s", formatBytes(arrayListMem)),
            "  +- ArrayList object:  24 bytes",
            String.format("  +- Object[] array:    %s", formatBytes(16 + intCount * 4L)),
            String.format("  +- Integer objects:   %s (%d x 16 bytes)", formatBytes(intCount * 16L), intCount),
            "",
            String.format("int[] primitive:     %s", formatBytes(intArrayMem)),
            "  +- Array header:      16 bytes",
            String.format("  +- int values:        %s (%d x 4 bytes)", formatBytes(intCount * 4L), intCount),
            "",
            String.format("Memory overhead: %.1fx more with ArrayList<Integer>", ratio),
        };

        for (String line : comparison) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setForeground(line.contains("overhead") ? WARN_COLOR : TEXT_COLOR);
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        panel.add(Box.createVerticalStrut(10));
        JLabel advice = new JLabel("TIP: Use primitive arrays or specialized collections (IntArrayList) for performance");
        advice.setFont(new Font("Consolas", Font.ITALIC, 10));
        advice.setForeground(HEADER_COLOR);
        advice.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(advice);

        return panel;
    }

    private JPanel createStyledPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(25, 35, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase));
                g2d.setColor(new Color(0, 200, 255, (int)(glow * 80)));
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

                if (getModel().isPressed()) {
                    g2d.setColor(ACCENT);
                } else if (getModel().isRollover()) {
                    g2d.setColor(ACCENT.darker());
                } else {
                    g2d.setColor(PANEL_BG);
                }

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
            case "double", "long", "float" -> new Color(255, 100, 200);
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
                int bytesLength = s.length();
                int arraySize = 16 + ((bytesLength + 7) / 8) * 8;
                yield 24 + arraySize;
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
