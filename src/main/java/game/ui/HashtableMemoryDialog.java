package game.ui;

import game.model.VisualHashtable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.ui.ThemeManager;

public class HashtableMemoryDialog extends JDialog {
    private VisualHashtable hashtable;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(200, 120, 80);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();

    public HashtableMemoryDialog(Frame parent, VisualHashtable hashtable) {
        super(parent, "Hashtable Memory Analysis", true);
        this.hashtable = hashtable;

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

        VisualHashtable.MemoryInfo mem = hashtable.getMemoryInfo();

        String[] lines = {
                "Total Memory Used: " + mem.formatTotal(),
                "",
                "Hashtable overhead:      " + mem.hashtableOverhead() + " bytes",
                "Entry[] array:           " + mem.arrayOverhead() + " bytes",
                "Entry nodes (36B each):  " + mem.entriesOverhead() + " bytes",
                "Key objects:             " + mem.keysMemory() + " bytes",
                "Value objects:           " + mem.valuesMemory() + " bytes",
                "",
                "Capacity: " + mem.capacity() + "   Size: " + mem.size()
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
        JPanel panel = createSection("HASHTABLE.ENTRY (36 bytes)");

        String[] diagram = {
                "+------------------------------------+",
                "|      HASHTABLE.ENTRY (36 bytes)    |",
                "+------------------------------------+",
                "| Object header     | 12 bytes       |",
                "| int hash          |  4 bytes       |  <- cached hashCode",
                "| Object key ref    |  4 bytes       |",
                "| Object value ref  |  4 bytes       |",
                "| Entry next ref    |  4 bytes       |  <- collision chain",
                "| padding           |  8 bytes       |  <- sync overhead",
                "+------------------------------------+",
                "",
                "Note: Hashtable is synchronized,",
                "which adds overhead to every operation."
        };

        for (String line : diagram) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("ENTRY")) color = ACCENT;
            else if (line.contains("synchronized")) color = new Color(255, 150, 100);
            label.setForeground(color);
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = createSection("HASHTABLE vs HASHMAP");

        int n = Math.max(hashtable.getSize(), 10);
        int cap = hashtable.getCapacity();

        String[] lines = {
                "For " + n + " String->Integer entries:",
                "",
                String.format("Hashtable:  ~%d bytes", 48 + 16 + cap*4 + n*36 + n*60),
                "  - Synchronized (thread-safe)",
                "  - null keys/values NOT allowed",
                "  - Legacy class (since Java 1.0)",
                "  - Default capacity: 11",
                "  - Grows by: 2n + 1",
                "",
                String.format("HashMap:    ~%d bytes", 48 + 16 + 16*4 + n*32 + n*60),
                "  - Not synchronized (faster)",
                "  - null keys/values allowed",
                "  - Modern class (since Java 1.2)",
                "  - Default capacity: 16",
                "  - Grows by: 2n (power of 2)",
                "",
                "RECOMMENDATION:",
                "  Use HashMap for single-threaded code",
                "  Use ConcurrentHashMap for multi-threaded",
                "  Avoid Hashtable in new code!"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            Color color = TEXT_COLOR;
            if (line.contains("Hashtable:") || line.contains("HashMap:")) color = ACCENT;
            else if (line.contains("RECOMMENDATION")) color = new Color(255, 200, 100);
            else if (line.contains("Avoid") || line.contains("NOT allowed")) color = new Color(255, 100, 100);
            else if (line.startsWith("  -")) color = new Color(200, 180, 150);
            else if (line.contains("Use ")) color = new Color(150, 255, 150);
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
