package game.ui;

import game.model.VisualTreeSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TreeSetMemoryDialog extends JDialog {
    private VisualTreeSet treeSet;

    private static final Color BG_COLOR = new Color(10, 18, 18);
    private static final Color PANEL_BG = new Color(22, 35, 35);
    private static final Color ACCENT = new Color(80, 200, 200);
    private static final Color TEXT_COLOR = new Color(200, 240, 240);

    public TreeSetMemoryDialog(Frame parent, VisualTreeSet treeSet) {
        super(parent, "TreeSet Memory Analysis", true);
        this.treeSet = treeSet;

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
        panel.add(createTreeStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createEntryStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualTreeSet.MemoryInfo mem = treeSet.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "TreeSet + TreeMap overhead:  " + mem.treeOverhead() + " bytes",
                "Entry nodes (40B each):      " + mem.entriesOverhead() + " bytes",
                "Element objects:             " + mem.elementsMemory() + " bytes",
                "",
                "Tree height:                 " + mem.height(),
                "Balanced: height <= log2(n)+1"
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

    private JPanel createTreeStructurePanel() {
        JPanel panel = createSection("RED-BLACK TREE PROPERTIES");

        String[] diagram = {
                "Red-Black Tree Rules:",
                "",
                "1. Every node is RED or BLACK",
                "2. Root is always BLACK",
                "3. No two adjacent RED nodes",
                "4. Every path from root to null has same BLACK count",
                "",
                "Example balanced tree:",
                "",
                "              [50]B           <- BLACK root",
                "             /     \\",
                "         [25]R     [75]R      <- RED children",
                "        /    \\    /    \\",
                "     [12]B  [30]B [60]B [90]B <- BLACK leaves",
                "",
                "Max height: 2 * log2(n+1)",
                "This guarantees O(log n) operations"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("RED")) color = new Color(255, 120, 120);
            else if (line.contains("BLACK") || line.contains("]B")) color = new Color(150, 200, 200);
            else if (line.startsWith("  ") && line.contains("[")) color = new Color(180, 220, 220);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createEntryStructurePanel() {
        JPanel panel = createSection("TREEMAP ENTRY (40 bytes each)");

        String[] diagram = {
                "+--------------------------------+",
                "|    TREEMAP.ENTRY (40 bytes)    |",
                "+--------------------------------+",
                "| Object header     | 12 bytes   |",
                "| Object key ref    |  4 bytes   |",
                "| Object value ref  |  4 bytes   |  <- PRESENT for TreeSet",
                "| Entry left ref    |  4 bytes   |",
                "| Entry right ref   |  4 bytes   |",
                "| Entry parent ref  |  4 bytes   |",
                "| boolean color     |  1 byte    |  <- RED or BLACK",
                "| padding           |  7 bytes   |",
                "+--------------------------------+",
                "",
                "TreeSet uses TreeMap internally",
                "Value is always a dummy PRESENT object"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = line.contains("ENTRY") ? ACCENT : new Color(180, 220, 220);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("TREESET vs HASHSET");

        int n = Math.max(treeSet.getSize(), 10);
        int height = treeSet.getHeight();

        String[] lines = {
                "For " + n + " Integer elements:",
                "",
                String.format("TreeSet:  ~%d bytes (40B/entry)", 80 + n*40 + n*16),
                String.format("  - O(log n) operations"),
                String.format("  - Sorted iteration"),
                String.format("  - Height: %d", height),
                "",
                String.format("HashSet:  ~%d bytes (32B/node)", 32 + 16 + n*4 + n*32 + n*16),
                String.format("  - O(1) operations"),
                String.format("  - No order guarantee"),
                "",
                "Choose TreeSet when:",
                "  - Need sorted order",
                "  - Need first()/last()/ceiling()/floor()",
                "  - Need range queries",
                "",
                "Choose HashSet when:",
                "  - Only need contains/add/remove",
                "  - Order doesn't matter",
                "  - Maximum speed is priority"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("TreeSet:") || line.contains("HashSet:")) color = ACCENT;
            else if (line.contains("O(log n)") || line.contains("O(1)")) color = new Color(150, 200, 255);
            else if (line.startsWith("  -")) color = new Color(180, 220, 220);
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
