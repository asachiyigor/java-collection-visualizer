package game.ui;

import game.model.VisualPriorityQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PriorityQueueMemoryDialog extends JDialog {
    private VisualPriorityQueue queue;

    private static final Color BG_COLOR = new Color(20, 10, 12);
    private static final Color PANEL_BG = new Color(40, 20, 25);
    private static final Color ACCENT = new Color(255, 100, 100);
    private static final Color TEXT_COLOR = new Color(255, 220, 220);

    public PriorityQueueMemoryDialog(Frame parent, VisualPriorityQueue queue) {
        super(parent, "PriorityQueue Memory Analysis", true);
        this.queue = queue;

        setSize(650, 700);
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
        panel.add(createHeapStructurePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createIndexPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualPriorityQueue.MemoryInfo mem = queue.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "PriorityQueue overhead:   32 bytes",
                "Object[] array:           " + mem.arrayOverhead() + " bytes",
                "Element objects:          " + mem.elementsMemory() + " bytes",
                "Wasted (unused slots):    " + mem.wastedMemory() + " bytes",
                "",
                "Size:     " + queue.getSize(),
                "Capacity: " + queue.getCapacity()
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

    private JPanel createHeapStructurePanel() {
        JPanel panel = createSection("HEAP STRUCTURE");

        String[] diagram = {
                "Binary Min-Heap stored as Object[]:",
                "",
                "Tree view:          Array view:",
                "",
                "       [10]         [10|15|20|30|25| | | | | | ]",
                "      /    \\          0  1  2  3  4  5  6  7  8  9 10",
                "   [15]    [20]",
                "   /  \\",
                " [30] [25]",
                "",
                "+----------------------------------------------+",
                "|     PRIORITYQUEUE (array-backed heap)        |",
                "+----------------------------------------------+",
                "| Object header          | 12 bytes            |",
                "| Object[] queue ref     |  4 bytes            |",
                "| int size               |  4 bytes            |",
                "| Comparator ref         |  4 bytes            |",
                "| int modCount           |  4 bytes            |",
                "| padding                |  4 bytes            |",
                "+----------------------------------------------+",
                "|                  ~32 bytes                   |",
                "+----------------------------------------------+",
                "",
                "+----------------------------------------------+",
                "|        BACKING ARRAY (Object[capacity])      |",
                "+----------------------------------------------+",
                "| Array header           | 16 bytes            |",
                "| References             | capacity * 4 bytes  |",
                "+----------------------------------------------+",
                "",
                "Heap property: parent <= children (min-heap)",
                "No wasted space for tree pointers!"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("PRIORITYQUEUE") || line.contains("BACKING ARRAY")) color = ACCENT;
            else if (line.contains("Heap property") || line.contains("No wasted")) color = new Color(100, 255, 150);
            else if (line.contains("[10]") || line.contains("[15]") || line.contains("[20]") || line.contains("[25]") || line.contains("[30]")) color = new Color(255, 180, 180);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createIndexPanel() {
        JPanel panel = createSection("INDEX RELATIONSHIPS");

        String[] lines = {
                "For element at index i (0-based):",
                "",
                "  Parent:      (i - 1) / 2",
                "  Left child:   2 * i + 1",
                "  Right child:  2 * i + 2",
                "",
                "Example for index 1:",
                "  Parent:      (1-1)/2 = 0",
                "  Left child:   2*1+1  = 3",
                "  Right child:  2*1+2  = 4",
                "",
                "Sift-up:   compare with parent, swap if smaller",
                "Sift-down: compare with children, swap with smallest",
                "",
                "offer():  add at end, sift-up     O(log n)",
                "poll():   remove root, sift-down  O(log n)",
                "peek():   return root              O(1)",
                "contains(): linear scan            O(n)"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("Parent:") || line.contains("Left child:") || line.contains("Right child:")) color = ACCENT;
            else if (line.contains("O(log n)") || line.contains("O(1)") || line.contains("O(n)")) color = new Color(150, 200, 255);
            else if (line.contains("Sift-up") || line.contains("Sift-down")) color = new Color(255, 200, 150);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("PRIORITYQUEUE vs TREESET vs SORTED ARRAYLIST");

        int n = Math.max(queue.getSize(), 10);

        String[] lines = {
                "For " + n + " Integer elements:",
                "",
                String.format("PriorityQueue:   ~%d bytes", 32 + 16 + 11 * 4 + n * 16),
                "  + O(log n) offer/poll",
                "  + O(1) peek at minimum",
                "  + Low memory overhead",
                "  - O(n) contains/remove",
                "  - No sorted iteration",
                "",
                String.format("TreeSet:         ~%d bytes", 80 + n * 40 + n * 16),
                "  + O(log n) all operations",
                "  + Sorted iteration",
                "  + first()/last()/floor()/ceiling()",
                "  - 40 bytes per entry overhead",
                "",
                String.format("Sorted ArrayList: ~%d bytes", 24 + 16 + n * 4 + n * 16),
                "  + O(1) peek at min/max",
                "  + O(log n) contains (binary search)",
                "  + Sorted iteration",
                "  - O(n) insert (shift elements)",
                "",
                "Choose PriorityQueue when:",
                "  - Only need min (or max) element",
                "  - Frequent add/remove of extremes",
                "  - Task scheduling, event processing"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.startsWith("PriorityQueue:") || line.startsWith("TreeSet:") || line.startsWith("Sorted ArrayList:")) color = ACCENT;
            else if (line.contains("O(log n)") || line.contains("O(1)") || line.contains("O(n)")) color = new Color(150, 200, 255);
            else if (line.startsWith("  +")) color = new Color(150, 255, 150);
            else if (line.startsWith("  -")) color = new Color(255, 150, 150);
            else if (line.contains("Choose")) color = new Color(100, 255, 150);
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
