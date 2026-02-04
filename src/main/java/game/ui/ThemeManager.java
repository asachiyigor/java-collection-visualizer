package game.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public enum Theme { DARK, LIGHT }

    private static ThemeManager instance;
    private Theme currentTheme = Theme.DARK;
    private final List<Runnable> listeners = new ArrayList<>();

    private ThemeManager() {}

    public static ThemeManager get() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public Theme getTheme() { return currentTheme; }
    public boolean isDark() { return currentTheme == Theme.DARK; }

    public void toggle() {
        currentTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        for (Runnable r : listeners) r.run();
    }

    public void addListener(Runnable listener) { listeners.add(listener); }

    // ── Base colors ──────────────────────────────────────────────

    public Color getBgColor() {
        return isDark() ? new Color(10, 15, 25) : new Color(240, 242, 245);
    }

    public Color getBgGradientEnd() {
        return isDark() ? new Color(5, 10, 20) : new Color(225, 230, 238);
    }

    public Color getPanelBg() {
        return isDark() ? new Color(25, 35, 50) : new Color(250, 250, 255);
    }

    public Color getTextColor() {
        return isDark() ? new Color(200, 220, 255) : new Color(30, 30, 50);
    }

    public Color getGridColor() {
        return isDark() ? new Color(30, 40, 60) : new Color(220, 225, 235);
    }

    public Color getButtonBg() {
        return isDark() ? new Color(35, 50, 70) : new Color(230, 235, 245);
    }

    public Color getEmptyCellColor() {
        return isDark() ? new Color(20, 30, 45) : new Color(235, 238, 245);
    }

    public Color getStatsBg() {
        return isDark() ? new Color(20, 30, 45, 200) : new Color(255, 255, 255, 230);
    }

    public Color getStatsBorder() {
        return isDark() ? new Color(60, 80, 120) : new Color(180, 190, 210);
    }

    // ── Header colors ────────────────────────────────────────────

    public Color getHeaderBgStart() {
        return isDark() ? new Color(20, 25, 40) : new Color(245, 248, 252);
    }

    public Color getHeaderBgEnd() {
        return isDark() ? new Color(15, 20, 30) : new Color(230, 235, 245);
    }

    public Color getHeaderLine() {
        return isDark() ? new Color(0, 200, 255, 100) : new Color(0, 150, 200, 80);
    }

    public Color getCategoryLabelColor() {
        return isDark() ? new Color(180, 200, 230) : new Color(80, 100, 130);
    }

    public Color getTabTextInactive() {
        return isDark() ? new Color(200, 220, 255) : new Color(60, 70, 90);
    }

    // ── Dialog colors ────────────────────────────────────────────

    public Color getDialogBg() {
        return isDark() ? new Color(15, 20, 30) : new Color(245, 248, 252);
    }

    public Color getDialogPanelBg() {
        return isDark() ? new Color(25, 35, 50) : new Color(250, 250, 255);
    }

    // ── Utility colors (same for both themes) ────────────────────

    public Color getSuccessColor() { return new Color(100, 255, 150); }
    public Color getWarnColor() { return new Color(255, 200, 100); }
    public Color getErrorColor() { return new Color(255, 100, 100); }
}
