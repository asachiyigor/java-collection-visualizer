package game.ui;

import game.model.VisualArrayDeque;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.ui.ThemeManager;

public class ArrayDequeMemoryDialog extends JDialog {
    private VisualArrayDeque deque;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(180, 100, 255);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();

    public ArrayDequeMemoryDialog(Frame parent, VisualArrayDeque deque) {
        super(parent, "ArrayDeque Memory Analysis", true);
        this.deque = deque;

        setSize(650, 550);
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
        panel.add(createStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualArrayDeque.MemoryInfo mem = deque.getMemoryInfo();

        String[] lines = {
                "Total Memory Used:  " + mem.formatTotal(),
                "Total Capacity:     " + mem.formatCapacity(),
                "",
                "ArrayDeque overhead:   " + mem.dequeOverhead() + " bytes",
                "Object[] array:        " + mem.arrayOverhead() + " bytes",
                "Elements memory:       " + mem.elementsMemory() + " bytes",
                "",
                "Array capacity:        " + mem.capacity(),
                "Elements stored:       " + mem.size()
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("Total Memory") ? ACCENT : TEXT_COLOR);
            label.setFont(new Font("Consolas", line.contains("Total") ? Font.BOLD : Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createStructurePanel() {
        JPanel panel = createSection("ARRAYDEQUE STRUCTURE");

        String[] diagram = {
                "+-----------------------------------------------+",
                "|         ARRAYDEQUE (circular buffer)          |",
                "+-----------------------------------------------+",
                "| Object header         | 12 bytes              |",
                "| Object[] elements ref |  4 bytes              |",
                "| int head              |  4 bytes              |",
                "| int tail              |  4 bytes              |",
                "| modCount, etc.        |  8 bytes              |",
                "+-----------------------------------------------+",
                "|                 ~32 bytes                     |",
                "+-----------------------------------------------+",
                "",
                "+-----------------------------------------------+",
                "|     CIRCULAR ARRAY (Object[capacity])         |",
                "+-----------------------------------------------+",
                "| Array header          | 16 bytes              |",
                "| References            | capacity * 4 bytes    |",
                "+-----------------------------------------------+",
                "",
                "Head and Tail wrap around: (index + 1) % capacity"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("ARRAYDEQUE") || line.contains("CIRCULAR")) color = ACCENT;
            else if (line.contains("wrap around")) color = new Color(100, 255, 150);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("ARRAYDEQUE vs LINKEDLIST vs STACK");

        int n = Math.max(deque.getSize(), 10);

        String[] lines = {
                "For " + n + " Integer elements:",
                "",
                String.format("ArrayDeque:  ~%d bytes", 32 + 16 + 16 * 4 + n * 16),
                "  + O(1) add/remove at both ends",
                "  + Better cache locality",
                "  + Less memory overhead",
                "  - No random access",
                "",
                String.format("LinkedList:  ~%d bytes", 32 + n * 24 + n * 16),
                "  + O(1) add/remove anywhere (with iterator)",
                "  - 24 bytes per node overhead",
                "  - Poor cache locality",
                "",
                String.format("Stack:       ~%d bytes (Vector-based)", 48 + 16 + 10 * 4 + n * 16),
                "  - Synchronized (slower)",
                "  - Only LIFO operations",
                "",
                "ArrayDeque is preferred for stack/queue operations!"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.startsWith("ArrayDeque:") || line.startsWith("LinkedList:") || line.startsWith("Stack:")) color = ACCENT;
            else if (line.contains("O(1)")) color = new Color(150, 255, 200);
            else if (line.startsWith("  +")) color = new Color(150, 255, 150);
            else if (line.startsWith("  -")) color = new Color(255, 150, 150);
            else if (line.contains("preferred")) color = new Color(100, 255, 150);
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
