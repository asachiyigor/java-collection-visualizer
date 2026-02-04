package game.ui;

import game.model.VisualArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.ui.ThemeManager;

public class ArrayMemoryDialog extends JDialog {
    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(100, 255, 150);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color HEADER_COLOR = new Color(150, 255, 200);

    private VisualArray array;
    private Timer animationTimer;
    private double glowPhase = 0;

    public ArrayMemoryDialog(JFrame parent, VisualArray array) {
        super(parent, "ARRAY MEMORY ANALYSIS", true);
        this.array = array;

        setSize(700, 650);
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 30, 22),
                        0, getHeight(), new Color(10, 20, 15));
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

        var memInfo = array.getMemoryInfo();
        int capacity = array.getCapacity();
        int size = array.getSize();
        boolean isPrimitive = array.isPrimitive();
        String elemType = array.getElementType();

        String title = isPrimitive ? elemType.toLowerCase() + "[]" : elemType + "[]";
        content.add(createSection(title.toUpperCase() + " MEMORY ANALYSIS", null));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("SUMMARY", createSummaryPanel(memInfo, capacity, size)));
        content.add(Box.createVerticalStrut(15));

        if (isPrimitive) {
            content.add(createSection("PRIMITIVE ARRAY STRUCTURE", createPrimitiveStructurePanel(elemType, capacity)));
        } else {
            content.add(createSection("OBJECT ARRAY STRUCTURE", createObjectArrayStructurePanel(capacity)));
        }
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("PRIMITIVE TYPES MEMORY", createPrimitiveTypesPanel()));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("ARRAY vs ARRAYLIST COMPARISON", createComparisonPanel(elemType, capacity, isPrimitive)));
        content.add(Box.createVerticalStrut(15));

        content.add(createSection("KEY DIFFERENCES", createKeyDifferencesPanel()));

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

    private JPanel createSummaryPanel(VisualArray.MemoryInfo memInfo, int capacity, int size) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 8));

        addLabelPair(panel, "Array Type:", array.isPrimitive() ?
                array.getElementType().toLowerCase() + "[] (primitive)" :
                array.getElementType() + "[] (object)", ACCENT);
        addLabelPair(panel, "Capacity (fixed):", String.valueOf(capacity), new Color(255, 200, 100));
        addLabelPair(panel, "Used slots:", size + " / " + capacity, TEXT_COLOR);
        addLabelPair(panel, "Total Memory:", memInfo.formatTotal(), new Color(0, 200, 255));
        addLabelPair(panel, "Unused Memory:", memInfo.formatWasted(), new Color(255, 100, 100));

        return panel;
    }

    private JPanel createPrimitiveStructurePanel(String type, int capacity) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        int primitiveSize = getPrimitiveSize(type);
        long totalSize = 16 + (long) capacity * primitiveSize;

        String[] diagram = {
            "+---------------------------------------------------+",
            String.format("|         %s[] Primitive Array%s|",
                type.toLowerCase(), spaces(24 - type.length())),
            "+---------------------------------------------------+",
            "|  Array Header       | 16 bytes                    |",
            "|    +- Mark Word     |  8 bytes                    |",
            "|    +- Class Pointer |  4 bytes                    |",
            "|    +- Array Length  |  4 bytes                    |",
            "+---------------------------------------------------+",
            String.format("|  %s values%s| %d x %d = %d bytes%s|",
                type.toLowerCase(), spaces(13 - type.length()),
                capacity, primitiveSize, capacity * primitiveSize,
                spaces(15 - String.valueOf(capacity * primitiveSize).length())),
            "+---------------------------------------------------+",
            String.format("|  TOTAL              | %d bytes%s|",
                totalSize, spaces(22 - String.valueOf(totalSize).length())),
            "+---------------------------------------------------+",
            "",
            "  Memory Layout (contiguous values):",
            "  +----+----+----+----+----+----+----+----+",
            String.format("  | %s | %s | %s | %s | %s | %s | %s | %s |  ...",
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length())),
                type.substring(0, Math.min(2, type.length()))),
            "  +----+----+----+----+----+----+----+----+",
            "    ^--- Values stored directly (no references)"
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

    private JPanel createObjectArrayStructurePanel(int capacity) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        long refsSize = capacity * 4L;
        long totalSize = 16 + refsSize;

        String[] diagram = {
            "+---------------------------------------------------+",
            "|         Object[] Array                            |",
            "+---------------------------------------------------+",
            "|  Array Header       | 16 bytes                    |",
            "|    +- Mark Word     |  8 bytes                    |",
            "|    +- Class Pointer |  4 bytes                    |",
            "|    +- Array Length  |  4 bytes                    |",
            "+---------------------------------------------------+",
            String.format("|  References         | %d x 4 = %d bytes%s|",
                capacity, refsSize, spaces(15 - String.valueOf(refsSize).length())),
            "+---------------------------------------------------+",
            String.format("|  Array TOTAL        | %d bytes%s|",
                totalSize, spaces(22 - String.valueOf(totalSize).length())),
            "|  + Element objects  | varies per type             |",
            "+---------------------------------------------------+",
            "",
            "  Memory Layout (references + heap objects):",
            "  +-----+-----+-----+-----+",
            "  | ref | ref | ref | ref |  <- Object[] (refs only)",
            "  +--+--+--+--+--+--+--+--+",
            "     |     |     |     |",
            "     v     v     v     v",
            "  [obj] [obj] [obj] [obj]   <- Actual objects on heap"
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

    private JPanel createPrimitiveTypesPanel() {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[][] types = {
            {"byte", "1 byte", "byte[]", "-128 to 127"},
            {"boolean", "1 byte", "boolean[]", "true/false"},
            {"char", "2 bytes", "char[]", "Unicode character"},
            {"short", "2 bytes", "short[]", "-32,768 to 32,767"},
            {"int", "4 bytes", "int[]", "-2^31 to 2^31-1"},
            {"float", "4 bytes", "float[]", "32-bit IEEE 754"},
            {"long", "8 bytes", "long[]", "-2^63 to 2^63-1"},
            {"double", "8 bytes", "double[]", "64-bit IEEE 754"},
        };

        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(600, 25));
        headerRow.setAlignmentX(LEFT_ALIGNMENT);
        addHeaderLabel(headerRow, "PRIMITIVE");
        addHeaderLabel(headerRow, "SIZE");
        addHeaderLabel(headerRow, "ARRAY TYPE");
        addHeaderLabel(headerRow, "RANGE");
        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(5));

        for (String[] type : types) {
            JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(600, 20));
            row.setAlignmentX(LEFT_ALIGNMENT);

            addDataLabel(row, type[0], ACCENT);
            addDataLabel(row, type[1], new Color(0, 200, 255));
            addDataLabel(row, type[2], TEXT_COLOR);
            addDataLabel(row, type[3], new Color(150, 180, 160));

            panel.add(row);
        }

        return panel;
    }

    private JPanel createComparisonPanel(String elemType, int capacity, boolean isPrimitive) {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        int primitiveSize = getPrimitiveSize(elemType);

        // Primitive array memory
        long primitiveArrayMem = 16 + (long) capacity * primitiveSize;

        // Object array (Integer[]) memory
        long objectArrayMem = 16 + capacity * 4L + capacity * 16L; // refs + Integer objects

        // ArrayList<Integer> memory
        long arrayListMem = 24 + 16 + capacity * 4L + capacity * 16L;

        String[] comparison = {
            String.format("Storing %d %s values:", capacity, elemType.toLowerCase()),
            "",
            String.format("%s[] (primitive):   %s", elemType.toLowerCase(), formatBytes(primitiveArrayMem)),
            "  +- Array header:      16 bytes",
            String.format("  +- %s values:       %s (%d x %d bytes)",
                elemType.toLowerCase(), formatBytes((long)capacity * primitiveSize), capacity, primitiveSize),
            "",
            String.format("Integer[] (objects): %s", formatBytes(objectArrayMem)),
            "  +- Array header:      16 bytes",
            String.format("  +- References:        %s (%d x 4 bytes)", formatBytes(capacity * 4L), capacity),
            String.format("  +- Integer objects:   %s (%d x 16 bytes)", formatBytes(capacity * 16L), capacity),
            "",
            String.format("ArrayList<Integer>:  %s", formatBytes(arrayListMem)),
            "  +- ArrayList object:  24 bytes",
            String.format("  +- Object[] + refs:   %s", formatBytes(16 + capacity * 4L)),
            String.format("  +- Integer objects:   %s", formatBytes(capacity * 16L)),
            "",
            String.format("int[] uses %.1fx LESS memory than ArrayList<Integer>!",
                    (double) arrayListMem / primitiveArrayMem),
        };

        for (String line : comparison) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setForeground(line.contains("LESS memory") ? new Color(100, 255, 150) : TEXT_COLOR);
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createKeyDifferencesPanel() {
        JPanel panel = createStyledPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] differences = {
            "ARRAYS:",
            "  + Fixed size - must know capacity at creation",
            "  + Direct memory access - fastest performance",
            "  + No boxing overhead with primitives",
            "  + Contiguous memory - better cache locality",
            "  - Cannot resize (must create new array + copy)",
            "  - No built-in methods (use Arrays utility class)",
            "",
            "ARRAYLIST:",
            "  + Dynamic resizing (grows automatically)",
            "  + Rich API (add, remove, contains, etc.)",
            "  + Works with Collections framework",
            "  - Only stores objects (primitives are boxed)",
            "  - Extra memory overhead (ArrayList + Object[])",
            "  - Resize operation is O(n)",
        };

        for (String line : differences) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            if (line.startsWith("ARRAY")) {
                label.setForeground(HEADER_COLOR);
                label.setFont(new Font("Consolas", Font.BOLD, 11));
            } else if (line.contains("+")) {
                label.setForeground(new Color(100, 255, 150));
            } else if (line.contains("-")) {
                label.setForeground(new Color(255, 150, 150));
            } else {
                label.setForeground(TEXT_COLOR);
            }
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
                g2d.setColor(new Color(25, 45, 35, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                float glow = (float) (0.5 + 0.3 * Math.sin(glowPhase));
                g2d.setColor(new Color(100, 255, 150, (int)(glow * 80)));
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

    private int getPrimitiveSize(String type) {
        return switch (type.toLowerCase()) {
            case "byte", "boolean" -> 1;
            case "char", "short" -> 2;
            case "int", "float" -> 4;
            case "long", "double" -> 8;
            default -> 4;
        };
    }

    private String spaces(int count) {
        return " ".repeat(Math.max(0, count));
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
