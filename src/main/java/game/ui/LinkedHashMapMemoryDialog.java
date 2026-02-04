package game.ui;

import game.model.VisualLinkedHashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LinkedHashMapMemoryDialog extends JDialog {
    private VisualLinkedHashMap linkedHashMap;

    private static final Color BG_COLOR = new Color(22, 16, 12);
    private static final Color PANEL_BG = new Color(38, 30, 22);
    private static final Color ACCENT = new Color(255, 180, 130);
    private static final Color TEXT_COLOR = new Color(255, 240, 225);

    public LinkedHashMapMemoryDialog(Frame parent, VisualLinkedHashMap linkedHashMap) {
        super(parent, "LinkedHashMap Memory Analysis", true);
        this.linkedHashMap = linkedHashMap;

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

        VisualLinkedHashMap.MemoryInfo mem = linkedHashMap.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "LinkedHashMap overhead:  " + mem.linkedHashMapOverhead() + " bytes",
                "Entry[] table:           " + mem.tableOverhead() + " bytes",
                "Entry nodes (40B each):  " + mem.nodesOverhead() + " bytes",
                "Key objects:             " + mem.keysMemory() + " bytes",
                "Value objects:           " + mem.valuesMemory() + " bytes"
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
        JPanel panel = createSection("LINKEDHASHMAP ENTRY (40 bytes)");

        String[] diagram = {
                "+------------------------------------+",
                "| LINKEDHASHMAP.ENTRY (40 bytes)     |",
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
                "vs HashMap.Node:    32 bytes",
                "Extra overhead:     +8 bytes per entry"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("ENTRY")) color = ACCENT;
            else if (line.contains("insertion order")) color = new Color(255, 200, 100);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("LINKEDHASHMAP vs HASHMAP");

        int n = Math.max(linkedHashMap.getSize(), 10);

        String[] lines = {
                "For " + n + " String->Integer entries:",
                "",
                String.format("HashMap:       ~%d bytes (32B/node)", 48 + 16 + n*4 + n*32 + n*60),
                String.format("LinkedHashMap: ~%d bytes (40B/node)", 56 + 16 + n*4 + n*40 + n*60),
                "",
                "Trade-off:",
                "  +8 bytes per entry",
                "  +Maintains insertion order",
                "  +Predictable iteration",
                "  +Same O(1) performance",
                "",
                "Use LinkedHashMap when:",
                "  - Need iteration in insertion order",
                "  - Building LRU cache (access-order mode)",
                "  - Order matters for JSON/display"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("HashMap:") || line.contains("LinkedHashMap:")) color = ACCENT;
            else if (line.startsWith("  +")) color = new Color(150, 255, 150);
            else if (line.startsWith("  -")) color = new Color(200, 185, 165);
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
