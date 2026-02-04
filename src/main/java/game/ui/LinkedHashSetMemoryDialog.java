package game.ui;

import game.model.VisualLinkedHashSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.ui.ThemeManager;

public class LinkedHashSetMemoryDialog extends JDialog {
    private VisualLinkedHashSet linkedHashSet;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(255, 120, 120);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static final Color ORDER_COLOR = new Color(255, 180, 100);

    public LinkedHashSetMemoryDialog(Frame parent, VisualLinkedHashSet linkedHashSet) {
        super(parent, "LinkedHashSet Memory Analysis", true);
        this.linkedHashSet = linkedHashSet;

        setSize(650, 650);
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
        panel.add(createNodeStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createOrderChainPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualLinkedHashSet.MemoryInfo mem = linkedHashSet.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "LinkedHashSet overhead:  " + mem.linkedHashSetOverhead() + " bytes",
                "Entry[] table:           " + mem.tableOverhead() + " bytes",
                "Entry nodes:             " + mem.formatNodes(),
                "Element objects:         " + mem.elementsMemory() + " bytes",
                "",
                "Empty buckets:           " + mem.emptyBuckets()
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

    private JPanel createNodeStructurePanel() {
        JPanel panel = createSection("LINKEDHASHSET ENTRY (40 bytes each)");

        String[] diagram = {
                "+------------------------------------+",
                "| LINKEDHASHSET.ENTRY (40 bytes)     |",
                "+------------------------------------+",
                "| Object header     | 12 bytes       |",
                "| int hash          |  4 bytes       |",
                "| Object key ref    |  4 bytes       |",
                "| Object value ref  |  4 bytes       |",
                "| Entry next ref    |  4 bytes       |  <- hash chain",
                "| Entry before ref  |  4 bytes       |  <- insertion order",
                "| Entry after ref   |  4 bytes       |  <- insertion order",
                "| padding           |  4 bytes       |",
                "+------------------------------------+",
                "",
                "vs HashSet.Node:    32 bytes",
                "Extra overhead:     +8 bytes per entry (before/after refs)"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("ENTRY")) color = ACCENT;
            else if (line.contains("insertion order")) color = ORDER_COLOR;
            else if (line.contains("vs") || line.contains("Extra")) color = new Color(150, 200, 255);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createOrderChainPanel() {
        JPanel panel = createSection("INSERTION ORDER CHAIN");

        String[] diagram = {
                "Hash table provides O(1) lookup:",
                "",
                "  bucket[0] -> Entry(42)",
                "  bucket[1] -> null",
                "  bucket[2] -> Entry(\"Hi\") -> Entry(58)",
                "  ...",
                "",
                "Doubly-linked list maintains order:",
                "",
                "  head <-> Entry(42) <-> Entry(\"Hi\") <-> Entry(58) <-> tail",
                "           (first)       (second)       (third)",
                "",
                "Iteration follows insertion order, not hash order!",
                "",
                "Use cases:",
                "  - LRU cache implementation",
                "  - Predictable iteration order",
                "  - Ordered JSON serialization"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("<->")) color = ORDER_COLOR;
            else if (line.contains("->") && !line.contains("<->")) color = ACCENT;
            else if (line.startsWith("  -")) color = new Color(180, 160, 170);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("LINKEDHASHSET vs HASHSET");

        int n = Math.max(linkedHashSet.getSize(), 10);

        String[] lines = {
                "For " + n + " elements:",
                "",
                String.format("HashSet:       ~%d bytes (32 bytes/node)", 32 + 16 + n*4 + n*32 + n*16),
                String.format("LinkedHashSet: ~%d bytes (40 bytes/node)", 40 + 16 + n*4 + n*40 + n*16),
                "",
                "Trade-off:",
                "  +8 bytes per element",
                "  +Maintains insertion order",
                "  +Predictable iteration",
                "  +Same O(1) performance",
                "",
                "Choose LinkedHashSet when:",
                "  - Need consistent iteration order",
                "  - Building caches (access-order mode)",
                "  - Order matters for output/display"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("HashSet:") || line.contains("LinkedHashSet:")) color = ACCENT;
            else if (line.startsWith("  +")) color = new Color(150, 255, 150);
            else if (line.startsWith("  -")) color = new Color(180, 160, 170);
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
