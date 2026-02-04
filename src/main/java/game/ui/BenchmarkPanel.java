package game.ui;

import game.model.BenchmarkModel;
import game.model.BenchmarkModel.BenchmarkResult;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BenchmarkPanel extends JPanel {

    private static final Color ACCENT = new Color(240, 200, 40);
    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color GRID_COLOR = ThemeManager.get().getGridColor();
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();

    private static final int BAR_HEIGHT = 28;
    private static final int BAR_GAP = 6;
    private static final int LABEL_WIDTH = 155;
    private static final int TIME_WIDTH = 80;
    private static final int SECTION_GAP = 30;
    private static final double BAR_ANIM_SPEED = 2.5;

    private BenchmarkModel model;
    private Timer animationTimer;
    private double glowPhase = 0;
    private long lastTime;

    private Map<String, Double> barAnimProgress = new HashMap<>();
    private double benchmarkProgress = 0.0;
    private boolean showProgress = false;

    public BenchmarkPanel(BenchmarkModel model) {
        this.model = model;
        setBackground(BG_COLOR);
        setAutoscrolls(true);
        lastTime = System.nanoTime();

        animationTimer = new Timer(16, e -> {
            long now = System.nanoTime();
            double delta = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            glowPhase += delta;

            boolean needsRepaint = false;
            for (Map.Entry<String, Double> entry : barAnimProgress.entrySet()) {
                if (entry.getValue() < 1.0) {
                    entry.setValue(Math.min(1.0, entry.getValue() + delta * BAR_ANIM_SPEED));
                    needsRepaint = true;
                }
            }
            if (showProgress || needsRepaint || !barAnimProgress.isEmpty()) {
                repaint();
            }
        });
        animationTimer.start();

        ThemeManager.get().addListener(() -> {
            updateThemeColors();
            repaint();
        });
    }

    public void setProgress(double progress) {
        this.benchmarkProgress = progress;
    }

    public void setShowProgress(boolean show) {
        this.showProgress = show;
        if (!show) benchmarkProgress = 0;
    }

    public void onResultsUpdated() {
        barAnimProgress.clear();
        for (BenchmarkResult r : model.getResults()) {
            String key = r.getDataStructureName() + "|" + r.getOperationName();
            barAnimProgress.put(key, 0.0);
        }
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        List<BenchmarkResult> results = model.getResults();
        if (results.isEmpty()) return new Dimension(900, 600);

        Map<String, Integer> groups = new LinkedHashMap<>();
        for (BenchmarkResult r : results) {
            groups.merge(r.getOperationName(), 1, Integer::sum);
        }
        int totalBars = results.size();
        int numGroups = groups.size();
        int height = 130 + numGroups * 55 + totalBars * (BAR_HEIGHT + BAR_GAP) + numGroups * SECTION_GAP + 40;
        return new Dimension(900, Math.max(600, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2d);
        drawHeader(g2d);

        if (showProgress) {
            drawProgressBar(g2d);
        }

        if (!model.getResults().isEmpty()) {
            drawResults(g2d);
        } else if (!showProgress) {
            drawEmptyState(g2d);
        }

        g2d.dispose();
    }

    private void drawBackground(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, BG_COLOR,
                0, h, new Color(
                Math.max(0, BG_COLOR.getRed() - 10),
                Math.max(0, BG_COLOR.getGreen() - 10),
                Math.max(0, BG_COLOR.getBlue() - 5)));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(GRID_COLOR);
        for (int x = 0; x < w; x += 40) g2d.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 40) g2d.drawLine(0, y, w, y);
    }

    private void drawHeader(Graphics2D g2d) {
        int cx = getWidth() / 2;

        float glow = (float) (0.6 + 0.3 * Math.sin(glowPhase * 2));
        g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (glow * 30)));
        g2d.fillRoundRect(cx - 250, 15, 500, 50, 20, 20);

        g2d.setFont(new Font("Consolas", Font.BOLD, 28));
        String title = "PERFORMANCE BENCHMARK";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = cx - fm.stringWidth(title) / 2;

        g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), (int) (glow * 80)));
        g2d.drawString(title, tx, 48);
        g2d.setColor(ACCENT);
        g2d.drawString(title, tx, 47);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        String sub = "[ Real java.util.* Collections ]";
        fm = g2d.getFontMetrics();
        g2d.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), 150));
        g2d.drawString(sub, cx - fm.stringWidth(sub) / 2, 68);
    }

    private void drawProgressBar(Graphics2D g2d) {
        int barX = 60, barY = 85, barW = getWidth() - 120, barH = 18;

        g2d.setColor(new Color(BG_COLOR.getRed() + 15, BG_COLOR.getGreen() + 15, BG_COLOR.getBlue() + 15));
        g2d.fillRoundRect(barX, barY, barW, barH, 8, 8);

        int fillW = (int) (barW * benchmarkProgress);
        if (fillW > 0) {
            GradientPaint gp = new GradientPaint(barX, barY, ACCENT.darker(), barX + fillW, barY, ACCENT);
            g2d.setPaint(gp);
            g2d.fillRoundRect(barX, barY, fillW, barH, 8, 8);
        }

        g2d.setColor(ACCENT.darker());
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(barX, barY, barW, barH, 8, 8);

        g2d.setFont(new Font("Consolas", Font.BOLD, 10));
        String pct = String.format("%.0f%%", benchmarkProgress * 100);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(pct, barX + (barW - fm.stringWidth(pct)) / 2, barY + 13);
    }

    private void drawEmptyState(Graphics2D g2d) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        float pulse = (float) (0.4 + 0.3 * Math.sin(glowPhase * 1.5));

        g2d.setFont(new Font("Consolas", Font.BOLD, 18));
        String msg = "Run a benchmark to see results";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), (int) (pulse * 255)));
        g2d.drawString(msg, cx - fm.stringWidth(msg) / 2, cy - 20);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
        String hint = "Select operations and element count, then click RUN BENCHMARK";
        fm = g2d.getFontMetrics();
        g2d.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), (int) (pulse * 150)));
        g2d.drawString(hint, cx - fm.stringWidth(hint) / 2, cy + 10);
    }

    private void drawResults(Graphics2D g2d) {
        List<BenchmarkResult> results = model.getResults();

        Map<String, List<BenchmarkResult>> groups = new LinkedHashMap<>();
        for (BenchmarkResult r : results) {
            groups.computeIfAbsent(r.getOperationName(), k -> new ArrayList<>()).add(r);
        }

        int yOffset = 120;
        int marginX = 40;
        int availableBarWidth = getWidth() - marginX * 2 - LABEL_WIDTH - TIME_WIDTH - 20;

        for (Map.Entry<String, List<BenchmarkResult>> entry : groups.entrySet()) {
            String opName = entry.getKey();
            List<BenchmarkResult> opResults = new ArrayList<>(entry.getValue());
            opResults.sort(Comparator.comparingDouble(BenchmarkResult::getTimeMs));

            int count = opResults.isEmpty() ? 0 : opResults.get(0).getElementCount();
            String sectionTitle = opName + "  (" + formatCount(count) + " elements)";

            // Section title
            g2d.setFont(new Font("Consolas", Font.BOLD, 16));
            g2d.setColor(ACCENT);
            g2d.drawString(sectionTitle, marginX, yOffset);
            yOffset += 5;

            // Separator line
            g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(marginX, yOffset, getWidth() - marginX, yOffset);
            yOffset += 12;

            double maxTime = opResults.stream().mapToDouble(BenchmarkResult::getTimeMs).max().orElse(1);

            for (int i = 0; i < opResults.size(); i++) {
                BenchmarkResult r = opResults.get(i);
                int barY = yOffset + i * (BAR_HEIGHT + BAR_GAP);
                Color dsColor = BenchmarkModel.DS_COLORS.getOrDefault(r.getDataStructureName(), ACCENT);

                String animKey = r.getDataStructureName() + "|" + r.getOperationName();
                double animProgress = barAnimProgress.getOrDefault(animKey, 1.0);

                // Structure name label
                g2d.setFont(new Font("Consolas", Font.BOLD, 11));
                g2d.setColor(dsColor);
                FontMetrics fm = g2d.getFontMetrics();
                int labelX = marginX;
                int labelY = barY + (BAR_HEIGHT + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(r.getDataStructureName(), labelX, labelY);

                // Bar background
                int barX = marginX + LABEL_WIDTH;
                g2d.setColor(new Color(BG_COLOR.getRed() + 12, BG_COLOR.getGreen() + 12, BG_COLOR.getBlue() + 12));
                g2d.fillRoundRect(barX, barY, availableBarWidth, BAR_HEIGHT, 6, 6);

                // Filled bar
                double targetWidth = (r.getTimeMs() / maxTime) * availableBarWidth;
                int fillW = (int) (targetWidth * animProgress);
                if (fillW > 0) {
                    GradientPaint gp = new GradientPaint(barX, barY, dsColor.darker().darker(),
                            barX + fillW, barY, dsColor);
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(barX, barY, fillW, BAR_HEIGHT, 6, 6);
                }

                // Bar border
                g2d.setColor(new Color(dsColor.getRed(), dsColor.getGreen(), dsColor.getBlue(), 60));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(barX, barY, availableBarWidth, BAR_HEIGHT, 6, 6);

                // Time label
                g2d.setFont(new Font("Consolas", Font.BOLD, 11));
                String timeStr = formatTime(r.getTimeMs());
                fm = g2d.getFontMetrics();
                int timeX = barX + availableBarWidth + 8;
                g2d.setColor(TEXT_COLOR);
                g2d.drawString(timeStr, timeX, labelY);

                // Fastest badge
                if (i == 0 && opResults.size() > 1) {
                    g2d.setFont(new Font("Consolas", Font.BOLD, 9));
                    g2d.setColor(new Color(100, 255, 150));
                    g2d.drawString("FASTEST", timeX + fm.stringWidth(timeStr) + 6, labelY);
                }
            }

            yOffset += opResults.size() * (BAR_HEIGHT + BAR_GAP) + SECTION_GAP;
        }
    }

    private String formatTime(double ms) {
        if (ms < 1) return String.format("%.2f ms", ms);
        if (ms < 100) return String.format("%.1f ms", ms);
        if (ms < 1000) return String.format("%.0f ms", ms);
        return String.format("%.1f s", ms / 1000);
    }

    private String formatCount(int count) {
        if (count >= 1_000_000) return String.format("%,d", count);
        if (count >= 1_000) return String.format("%,d", count);
        return String.valueOf(count);
    }

    private void updateThemeColors() {
        BG_COLOR = ThemeManager.get().getBgColor();
        GRID_COLOR = ThemeManager.get().getGridColor();
        TEXT_COLOR = ThemeManager.get().getTextColor();
        setBackground(BG_COLOR);
    }
}
