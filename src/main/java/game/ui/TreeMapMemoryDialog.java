package game.ui;

import game.model.VisualTreeMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TreeMapMemoryDialog extends JDialog {
    private VisualTreeMap treeMap;

    private static final Color BG_COLOR = new Color(10, 16, 14);
    private static final Color PANEL_BG = new Color(20, 32, 28);
    private static final Color ACCENT = new Color(100, 220, 180);
    private static final Color TEXT_COLOR = new Color(210, 250, 235);

    public TreeMapMemoryDialog(Frame parent, VisualTreeMap treeMap) {
        super(parent, "TreeMap Memory Analysis", true);
        this.treeMap = treeMap;

        setSize(650, 600);
        setLocationRelativeTo(parent);
        setBackground(BG_COLOR);

        JPanel content = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        add(scrollPane);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSummaryPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createEntryStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualTreeMap.MemoryInfo mem = treeMap.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "TreeMap overhead:        " + mem.treeMapOverhead() + " bytes",
                "Entry nodes (40B each):  " + mem.entriesOverhead() + " bytes",
                "Key objects:             " + mem.keysMemory() + " bytes",
                "Value objects:           " + mem.valuesMemory() + " bytes",
                "",
                "Tree height:             " + mem.height()
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

    private JPanel createEntryStructurePanel() {
        JPanel panel = createSection("TREEMAP ENTRY (40 bytes)");

        String[] diagram = {
                "+------------------------------------+",
                "|     TREEMAP.ENTRY (40 bytes)       |",
                "+------------------------------------+",
                "| Object header     | 12 bytes       |",
                "| Object key ref    |  4 bytes       |",
                "| Object value ref  |  4 bytes       |",
                "| Entry left ref    |  4 bytes       |",
                "| Entry right ref   |  4 bytes       |",
                "| Entry parent ref  |  4 bytes       |",
                "| boolean color     |  1 byte        |  <- RED or BLACK",
                "| padding           |  7 bytes       |",
                "+------------------------------------+",
                "",
                "Tree is self-balancing (Red-Black)",
                "Guarantees O(log n) height"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("ENTRY")) color = ACCENT;
            else if (line.contains("RED") || line.contains("BLACK")) color = new Color(255, 150, 150);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("TREEMAP vs HASHMAP");

        int n = Math.max(treeMap.getSize(), 10);
        int h = treeMap.getHeight();

        String[] lines = {
                "For " + n + " String->Integer entries:",
                "",
                String.format("TreeMap:   ~%d bytes (40B/entry)", 32 + n*40 + n*60),
                String.format("  - O(log n) operations"),
                String.format("  - Sorted by keys"),
                String.format("  - Height: %d", h),
                "",
                String.format("HashMap:   ~%d bytes (32B/node)", 48 + 16 + n*4 + n*32 + n*60),
                String.format("  - O(1) operations"),
                String.format("  - No order guarantee"),
                "",
                "Use TreeMap when:",
                "  - Need sorted iteration by keys",
                "  - Need firstKey()/lastKey()",
                "  - Need range queries (subMap, headMap, tailMap)",
                "",
                "Use HashMap when:",
                "  - Only need put/get/remove",
                "  - Order doesn't matter",
                "  - Maximum speed is priority"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("TreeMap:") || line.contains("HashMap:")) color = ACCENT;
            else if (line.contains("O(log n)") || line.contains("O(1)")) color = new Color(150, 200, 255);
            else if (line.startsWith("  -")) color = new Color(150, 200, 180);
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
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1), new EmptyBorder(12, 15, 12, 15)));
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
