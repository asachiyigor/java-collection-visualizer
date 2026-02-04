package game.ui;

import game.model.VisualHashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.ui.ThemeManager;

public class HashMapMemoryDialog extends JDialog {
    private VisualHashMap hashMap;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(255, 200, 80);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();

    public HashMapMemoryDialog(Frame parent, VisualHashMap hashMap) {
        super(parent, "HashMap Memory Analysis", true);
        this.hashMap = hashMap;

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
        panel.add(createKeyValuePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualHashMap.MemoryInfo mem = hashMap.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "HashMap overhead:    " + mem.hashMapOverhead() + " bytes",
                "Node[] table:        " + mem.tableOverhead() + " bytes",
                "Node entries:        " + mem.nodesOverhead() + " bytes",
                "Key objects:         " + mem.keysMemory() + " bytes",
                "Value objects:       " + mem.valuesMemory() + " bytes"
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
        JPanel panel = createSection("HASHMAP.NODE STRUCTURE (32 bytes)");

        String[] diagram = {
                "+--------------------------------+",
                "|     HASHMAP.NODE (32 bytes)    |",
                "+--------------------------------+",
                "| Object header     | 12 bytes   |",
                "| int hash          |  4 bytes   |  <- cached hashCode()",
                "| Object key ref    |  4 bytes   |  -> key object",
                "| Object value ref  |  4 bytes   |  -> value object",
                "| Node next ref     |  4 bytes   |  -> collision chain",
                "| padding           |  4 bytes   |",
                "+--------------------------------+",
                "",
                "Each entry = Node (32B) + Key object + Value object"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("NODE") ? ACCENT : new Color(200, 190, 150));
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createKeyValuePanel() {
        JPanel panel = createSection("KEY-VALUE MEMORY");

        String[] diagram = {
                "Common key-value pair sizes:",
                "",
                "Integer -> Integer:    32B + 16B + 16B = 64 bytes",
                "String -> Integer:     32B + ~40B + 16B = ~88 bytes",
                "String -> String:      32B + ~40B + ~40B = ~112 bytes",
                "",
                "Note: String size depends on length",
                "  String object: 24 bytes",
                "  + byte[] array: 16 + length bytes",
                "",
                "Example 'name' -> 'Alice':",
                "  Node: 32B",
                "  Key 'name': 24 + 16 + 4 = 44B",
                "  Value 'Alice': 24 + 16 + 5 = 45B",
                "  Total: ~121 bytes per entry"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("->") && line.contains(":")) color = ACCENT;
            else if (line.contains("Note") || line.contains("Example")) color = new Color(150, 200, 255);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("HASHMAP vs OTHER MAPS");

        int n = Math.max(hashMap.getSize(), 10);

        String[] lines = {
                "For " + n + " String->Integer entries:",
                "",
                String.format("HashMap:       ~%d bytes", 48 + 16 + n*4 + n*32 + n*44 + n*16),
                String.format("  - O(1) put/get/remove"),
                String.format("  - No order guarantee"),
                "",
                String.format("LinkedHashMap: ~%d bytes (+8B/entry)", 56 + 16 + n*4 + n*40 + n*44 + n*16),
                String.format("  - O(1) operations"),
                String.format("  - Maintains insertion order"),
                "",
                String.format("TreeMap:       ~%d bytes", 32 + n*40 + n*44 + n*16),
                String.format("  - O(log n) operations"),
                String.format("  - Sorted by keys"),
                "",
                "Choose HashMap when:",
                "  - Need fastest key-value lookup",
                "  - Order doesn't matter",
                "  - Keys are well-distributed"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("HashMap:") || line.contains("LinkedHashMap:") || line.contains("TreeMap:")) {
                color = ACCENT;
            } else if (line.contains("O(1)") || line.contains("O(log n)")) {
                color = new Color(150, 200, 255);
            } else if (line.startsWith("  -")) {
                color = new Color(200, 190, 150);
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
