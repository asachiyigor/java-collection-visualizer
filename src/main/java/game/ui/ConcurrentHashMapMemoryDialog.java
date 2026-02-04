package game.ui;

import game.model.VisualConcurrentHashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ConcurrentHashMapMemoryDialog extends JDialog {
    private VisualConcurrentHashMap map;

    private static final Color ACCENT = new Color(100, 150, 220);

    public ConcurrentHashMapMemoryDialog(Frame parent, VisualConcurrentHashMap map) {
        super(parent, "ConcurrentHashMap Memory Analysis", true);
        this.map = map;

        setSize(650, 700);
        setLocationRelativeTo(parent);
        setBackground(ThemeManager.get().getDialogBg());

        JPanel content = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.get().getDialogBg());
        add(scrollPane);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.get().getDialogBg());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSummaryPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createSegmentArchitecturePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createConcurrencyModelPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createComparisonPanel());

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createSection("MEMORY SUMMARY");

        VisualConcurrentHashMap.MemoryInfo mem = map.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "Map overhead:        " + mem.mapOverhead() + " bytes",
                "Segment[] array:     " + mem.segmentsOverhead() + " bytes",
                "Node[] table:        " + mem.tableOverhead() + " bytes",
                "Node entries:        " + mem.nodesOverhead() + " bytes",
                "Key objects:         " + mem.keysMemory() + " bytes",
                "Value objects:       " + mem.valuesMemory() + " bytes"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("Total") ? ACCENT : ThemeManager.get().getTextColor());
            label.setFont(new Font("Consolas", line.contains("Total") ? Font.BOLD : Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createSegmentArchitecturePanel() {
        JPanel panel = createSection("SEGMENT ARCHITECTURE");

        String[] diagram = {
                "ConcurrentHashMap (concurrencyLevel=4)",
                "",
                "+------------------------------------------+",
                "|           Segment[] (4 segments)         |",
                "+----------+----------+----------+---------+",
                "| Seg 0    | Seg 1    | Seg 2    | Seg 3   |",
                "| lock     | lock     | lock     | lock    |",
                "| count    | count    | count    | count   |",
                "+----------+----------+----------+---------+",
                "     |          |          |          |",
                "     v          v          v          v",
                "+--------+ +--------+ +--------+ +--------+",
                "|Node[]  | |Node[]  | |Node[]  | |Node[]  |",
                "|[0]->[1]| |[4]->[5]| |[8]->[9]| |[C]->[D]|",
                "|[2]->[3]| |[6]->[7]| |[A]->[B]| |[E]->[F]|",
                "+--------+ +--------+ +--------+ +--------+",
                "",
                "Each Segment has its own lock + Node[] table",
                "Segment size: ~36 bytes (header + lock + count + ref)"
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color;
            if (line.contains("ConcurrentHashMap") || line.contains("Segment[]")) {
                color = ACCENT;
            } else if (line.contains("Seg ") || line.contains("Node[]")) {
                color = new Color(150, 200, 255);
            } else if (line.contains("Each") || line.contains("Segment size")) {
                color = new Color(150, 255, 200);
            } else {
                color = new Color(180, 190, 210);
            }
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createConcurrencyModelPanel() {
        JPanel panel = createSection("CONCURRENCY MODEL");

        String[] lines = {
                "Per-Segment Locking:",
                "  - Each segment has its own ReentrantLock",
                "  - Threads accessing different segments",
                "    can operate in parallel",
                "  - Only same-segment writes block each other",
                "",
                "CAS (Compare-And-Swap) for Reads:",
                "  - get() does NOT acquire locks",
                "  - Uses volatile reads for visibility",
                "  - Lock-free read path -> high throughput",
                "",
                "Volatile Writes:",
                "  - Node.value and Node.next are volatile",
                "  - Ensures visibility across threads",
                "  - No stale reads without locking",
                "",
                "Key Guarantee:",
                "  - Thread-safe without external sync",
                "  - Weakly consistent iterators",
                "  - No ConcurrentModificationException"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color;
            if (line.endsWith(":") && !line.startsWith(" ")) {
                color = ACCENT;
            } else if (line.contains("volatile") || line.contains("CAS") || line.contains("Lock")) {
                color = new Color(150, 200, 255);
            } else if (line.contains("Thread-safe") || line.contains("No Concurrent")) {
                color = new Color(150, 255, 200);
            } else {
                color = ThemeManager.get().getTextColor();
            }
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("CONCURRENTHASHMAP vs HASHMAP vs HASHTABLE");

        String[] lines = {
                "Feature            | ConcurrentHM | HashMap   | Hashtable",
                "-------------------+--------------+-----------+----------",
                "Thread-safe        | Yes          | No        | Yes",
                "Sync model         | Segments     | None      | Full lock",
                "Null keys          | No           | Yes (1)   | No",
                "Null values        | No           | Yes       | No",
                "Read performance   | High (CAS)   | Highest   | Low",
                "Write performance  | Good (segm.) | Highest   | Low",
                "Concurrent reads   | Yes          | No        | No",
                "Concurrent writes  | Partial      | No        | No",
                "Iterator           | Weakly cons. | Fail-fast | Fail-fast",
                "",
                "Performance comparison (relative):",
                "",
                "  Single-threaded:",
                "    HashMap:          100%  (fastest)",
                "    ConcurrentHashMap: 95%  (slight overhead)",
                "    Hashtable:         70%  (sync overhead)",
                "",
                "  Multi-threaded (4 threads):",
                "    ConcurrentHashMap: 100% (segment parallelism)",
                "    HashMap + sync:    35%  (full contention)",
                "    Hashtable:         30%  (full contention)"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color;
            if (line.contains("Feature") || line.contains("---")) {
                color = ACCENT;
            } else if (line.contains("ConcurrentHM") || line.contains("ConcurrentHashMap:")) {
                color = ACCENT;
            } else if (line.contains("100%") || line.contains("fastest")) {
                color = new Color(150, 255, 200);
            } else if (line.contains("Performance") || line.contains("Single-threaded") || line.contains("Multi-threaded")) {
                color = new Color(150, 200, 255);
            } else {
                color = ThemeManager.get().getTextColor();
            }
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.get().getDialogPanelBg());
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
