package game.ui;

import game.model.BenchmarkModel;
import game.model.BenchmarkModel.BenchmarkResult;
import game.model.BenchmarkModel.Operation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BenchmarkControlPanel extends JPanel {

    private static final Color ACCENT = new Color(240, 200, 40);
    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static Color BUTTON_BG = ThemeManager.get().getButtonBg();

    private BenchmarkModel model;
    private BenchmarkPanel benchmarkPanel;

    private JCheckBox addCheckbox;
    private JCheckBox getCheckbox;
    private JCheckBox removeCheckbox;
    private JComboBox<String> countCombo;
    private JButton runButton;
    private JLabel statusLabel;
    private JLabel progressLabel;
    private JPanel summaryPanel;

    public BenchmarkControlPanel(BenchmarkModel model, BenchmarkPanel panel) {
        this.model = model;
        this.benchmarkPanel = panel;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(260, 700));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
        ThemeManager.get().addListener(() -> { updateThemeColors(); repaint(); });
    }

    private void initComponents() {
        // ── OPERATIONS section ────────────────────────────────
        add(createTitle("OPERATIONS"));
        add(Box.createVerticalStrut(8));

        addCheckbox = createStyledCheckbox("ADD", true);
        getCheckbox = createStyledCheckbox("GET / CONTAINS", true);
        removeCheckbox = createStyledCheckbox("REMOVE", true);
        add(addCheckbox);
        add(Box.createVerticalStrut(4));
        add(getCheckbox);
        add(Box.createVerticalStrut(4));
        add(removeCheckbox);
        add(Box.createVerticalStrut(14));

        // ── ELEMENT COUNT section ─────────────────────────────
        add(createTitle("ELEMENT COUNT"));
        add(Box.createVerticalStrut(8));

        countCombo = new JComboBox<>(new String[]{"1,000", "10,000", "100,000", "1,000,000"});
        styleComboBox(countCombo);
        countCombo.setSelectedIndex(1);
        add(countCombo);
        add(Box.createVerticalStrut(14));

        // ── RUN button ────────────────────────────────────────
        runButton = createStyledButton("RUN BENCHMARK", ACCENT);
        runButton.setFont(new Font("Consolas", Font.BOLD, 14));
        runButton.setMaximumSize(new Dimension(250, 40));
        runButton.addActionListener(e -> runBenchmark());
        add(runButton);
        add(Box.createVerticalStrut(8));

        // ── Status + progress ─────────────────────────────────
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(230, 20));
        add(statusLabel);
        add(Box.createVerticalStrut(4));

        progressLabel = new JLabel(" ");
        progressLabel.setFont(new Font("Consolas", Font.PLAIN, 10));
        progressLabel.setForeground(ACCENT);
        progressLabel.setAlignmentX(LEFT_ALIGNMENT);
        progressLabel.setMaximumSize(new Dimension(230, 16));
        add(progressLabel);
        add(Box.createVerticalStrut(14));

        // ── RESULTS SUMMARY section ───────────────────────────
        add(createTitle("RESULTS SUMMARY"));
        add(Box.createVerticalStrut(8));

        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(BG_COLOR);
        summaryPanel.setAlignmentX(LEFT_ALIGNMENT);
        summaryPanel.setMaximumSize(new Dimension(250, 300));

        JLabel emptyLabel = new JLabel("No results yet");
        emptyLabel.setFont(new Font("Consolas", Font.PLAIN, 10));
        emptyLabel.setForeground(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), 100));
        summaryPanel.add(emptyLabel);
        add(summaryPanel);

        add(Box.createVerticalGlue());

        // ── INFO panel ────────────────────────────────────────
        add(createInfoPanel());
    }

    private void runBenchmark() {
        if (model.isRunning()) {
            model.cancel();
            runButton.setText("RUN BENCHMARK");
            statusLabel.setText("Cancelled");
            statusLabel.setForeground(ThemeManager.get().getWarnColor());
            benchmarkPanel.setShowProgress(false);
            return;
        }

        Set<Operation> ops = EnumSet.noneOf(Operation.class);
        if (addCheckbox.isSelected()) ops.add(Operation.ADD);
        if (getCheckbox.isSelected()) ops.add(Operation.GET);
        if (removeCheckbox.isSelected()) ops.add(Operation.REMOVE);

        if (ops.isEmpty()) {
            statusLabel.setText("Select at least one operation!");
            statusLabel.setForeground(ThemeManager.get().getErrorColor());
            return;
        }

        String countStr = (String) countCombo.getSelectedItem();
        int count = Integer.parseInt(countStr.replace(",", ""));

        runButton.setText("CANCEL");
        statusLabel.setText("Running...");
        statusLabel.setForeground(ACCENT);
        benchmarkPanel.setShowProgress(true);

        model.runBenchmarks(count, ops,
            progress -> SwingUtilities.invokeLater(() -> {
                benchmarkPanel.setProgress(progress);
                progressLabel.setText(String.format("Progress: %.0f%%", progress * 100));
            }),
            () -> SwingUtilities.invokeLater(() -> {
                runButton.setText("RUN BENCHMARK");
                statusLabel.setText("Done! " + model.getResults().size() + " benchmarks");
                statusLabel.setForeground(ThemeManager.get().getSuccessColor());
                progressLabel.setText(" ");
                benchmarkPanel.setShowProgress(false);
                benchmarkPanel.onResultsUpdated();
                updateSummary();
            })
        );
    }

    private void updateSummary() {
        summaryPanel.removeAll();

        Map<String, List<BenchmarkResult>> byOp = new LinkedHashMap<>();
        for (BenchmarkResult r : model.getResults()) {
            byOp.computeIfAbsent(r.getOperationName(), k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<BenchmarkResult>> entry : byOp.entrySet()) {
            List<BenchmarkResult> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.comparingDouble(BenchmarkResult::getTimeMs));

            BenchmarkResult fastest = sorted.get(0);
            BenchmarkResult slowest = sorted.get(sorted.size() - 1);

            JLabel opLabel = new JLabel(entry.getKey());
            opLabel.setFont(new Font("Consolas", Font.BOLD, 11));
            opLabel.setForeground(ACCENT);
            opLabel.setAlignmentX(LEFT_ALIGNMENT);
            summaryPanel.add(opLabel);

            JLabel fastLabel = new JLabel("Best: " + fastest.getDataStructureName()
                    + " (" + formatTime(fastest.getTimeMs()) + ")");
            fastLabel.setFont(new Font("Consolas", Font.PLAIN, 10));
            fastLabel.setForeground(ThemeManager.get().getSuccessColor());
            fastLabel.setAlignmentX(LEFT_ALIGNMENT);
            summaryPanel.add(fastLabel);

            JLabel slowLabel = new JLabel("Slow: " + slowest.getDataStructureName()
                    + " (" + formatTime(slowest.getTimeMs()) + ")");
            slowLabel.setFont(new Font("Consolas", Font.PLAIN, 10));
            slowLabel.setForeground(ThemeManager.get().getErrorColor());
            slowLabel.setAlignmentX(LEFT_ALIGNMENT);
            summaryPanel.add(slowLabel);

            double ratio = slowest.getTimeMs() / Math.max(0.001, fastest.getTimeMs());
            JLabel ratioLabel = new JLabel(String.format("Ratio: %.1fx", ratio));
            ratioLabel.setFont(new Font("Consolas", Font.PLAIN, 10));
            ratioLabel.setForeground(new Color(200, 180, 150));
            ratioLabel.setAlignmentX(LEFT_ALIGNMENT);
            summaryPanel.add(ratioLabel);

            summaryPanel.add(Box.createVerticalStrut(8));
        }

        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private String formatTime(double ms) {
        if (ms < 1) return String.format("%.2f ms", ms);
        if (ms < 100) return String.format("%.1f ms", ms);
        if (ms < 1000) return String.format("%.0f ms", ms);
        return String.format("%.1f s", ms / 1000);
    }

    private JCheckBox createStyledCheckbox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int boxSize = 16;
                int boxY = (getHeight() - boxSize) / 2;
                g2d.setColor(BUTTON_BG);
                g2d.fillRoundRect(2, boxY, boxSize, boxSize, 4, 4);
                g2d.setColor(ACCENT);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(2, boxY, boxSize, boxSize, 4, 4);

                if (isSelected()) {
                    g2d.setColor(ACCENT);
                    g2d.setStroke(new BasicStroke(2.5f));
                    g2d.drawLine(6, boxY + 8, 9, boxY + 12);
                    g2d.drawLine(9, boxY + 12, 15, boxY + 4);
                }

                g2d.setColor(TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(), boxSize + 8, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2d.dispose();
            }
        };

        cb.setFont(new Font("Consolas", Font.PLAIN, 12));
        cb.setBackground(BG_COLOR);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setAlignmentX(LEFT_ALIGNMENT);
        cb.setMaximumSize(new Dimension(250, 26));
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT);
        label.setFont(new Font("Consolas", Font.BOLD, 14));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Consolas", Font.PLAIN, 12));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BUTTON_BG);
        combo.setForeground(TEXT_COLOR);
        combo.setFont(new Font("Consolas", Font.PLAIN, 13));
        combo.setMaximumSize(new Dimension(250, 30));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        combo.setBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1));
    }

    private JButton createStyledButton(String text, Color accentColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(accentColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(accentColor);
                } else {
                    g2d.setColor(BUTTON_BG);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                g2d.setColor(TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };

        button.setFont(new Font("Consolas", Font.BOLD, 13));
        button.setMaximumSize(new Dimension(250, 32));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        panel.setMaximumSize(new Dimension(250, 150));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("BENCHMARK INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {
                "Uses real java.util.*",
                "collections (no visual",
                "model overhead).",
                "",
                "JIT warmup: 2 runs",
                "before measurement.",
                "",
                "Results are relative -",
                "absolute times vary by CPU"
        };

        for (String line : info) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("java.util") ? ThemeManager.get().getSuccessColor() : new Color(200, 180, 150));
            label.setFont(new Font("Consolas", Font.PLAIN, 10));
            panel.add(label);
        }

        return panel;
    }

    private void updateThemeColors() {
        BG_COLOR = ThemeManager.get().getBgColor();
        TEXT_COLOR = ThemeManager.get().getTextColor();
        PANEL_BG = ThemeManager.get().getPanelBg();
        BUTTON_BG = ThemeManager.get().getButtonBg();
        setBackground(BG_COLOR);
    }
}
