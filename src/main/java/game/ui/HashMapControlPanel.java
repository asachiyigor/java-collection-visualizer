package game.ui;

import game.model.VisualHashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import game.ui.ThemeManager;

public class HashMapControlPanel extends JPanel {
    private VisualHashMap hashMap;
    private Random random = new Random();
    private JTextField keyField;
    private JTextField valueField;
    private JComboBox<String> keyTypeCombo;
    private JComboBox<String> valueTypeCombo;
    private JLabel statusLabel;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(255, 200, 80);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static Color BUTTON_BG = ThemeManager.get().getButtonBg();
    private static Color SUCCESS_COLOR = ThemeManager.get().getSuccessColor();
    private static Color WARN_COLOR = ThemeManager.get().getWarnColor();
    private static Color ERROR_COLOR = ThemeManager.get().getErrorColor();

    public HashMapControlPanel(VisualHashMap hashMap) {
        this.hashMap = hashMap;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 600));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
        ThemeManager.get().addListener(() -> { updateThemeColors(); repaint(); });
    }

    private void initComponents() {
        add(createTitle("PUT ENTRY"));
        add(Box.createVerticalStrut(8));

        add(createLabel("KEY TYPE:"));
        keyTypeCombo = new JComboBox<>(new String[]{"int", "String"});
        styleComboBox(keyTypeCombo);
        add(keyTypeCombo);
        add(Box.createVerticalStrut(4));

        add(createLabel("KEY:"));
        keyField = new JTextField();
        styleTextField(keyField);
        add(keyField);
        add(Box.createVerticalStrut(6));

        add(createLabel("VALUE TYPE:"));
        valueTypeCombo = new JComboBox<>(new String[]{"String", "int", "double", "boolean"});
        styleComboBox(valueTypeCombo);
        add(valueTypeCombo);
        add(Box.createVerticalStrut(4));

        add(createLabel("VALUE:"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(8));

        JButton putBtn = createStyledButton("PUT", ACCENT);
        putBtn.addActionListener(e -> putEntry());
        add(putBtn);
        add(Box.createVerticalStrut(10));

        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(6));

        JPanel quickPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 56));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton intIntBtn = createSmallButton("Int:Int", new Color(0, 200, 255));
        intIntBtn.addActionListener(e -> {
            Object k = VisualHashMap.generateRandomKey("int", random);
            Object v = VisualHashMap.generateRandomValue("int", random);
            hashMap.put(k, v, "int", "int");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(intIntBtn);

        JButton strStrBtn = createSmallButton("Str:Str", new Color(100, 255, 150));
        strStrBtn.addActionListener(e -> {
            Object k = VisualHashMap.generateRandomKey("String", random);
            Object v = VisualHashMap.generateRandomValue("String", random);
            hashMap.put(k, v, "String", "String");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(strStrBtn);

        JButton strIntBtn = createSmallButton("Str:Int", new Color(255, 200, 80));
        strIntBtn.addActionListener(e -> {
            Object k = VisualHashMap.generateRandomKey("String", random);
            Object v = VisualHashMap.generateRandomValue("int", random);
            hashMap.put(k, v, "String", "int");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(strIntBtn);

        JButton intStrBtn = createSmallButton("Int:Str", new Color(255, 100, 200));
        intStrBtn.addActionListener(e -> {
            Object k = VisualHashMap.generateRandomKey("int", random);
            Object v = VisualHashMap.generateRandomValue("String", random);
            hashMap.put(k, v, "int", "String");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(intStrBtn);
        add(quickPanel);
        add(Box.createVerticalStrut(10));

        add(createTitle("METHODS"));
        add(Box.createVerticalStrut(6));

        JPanel methodPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        methodPanel.setBackground(BG_COLOR);
        methodPanel.setMaximumSize(new Dimension(230, 56));
        methodPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton getBtn = createSmallButton("get(key)", new Color(150, 255, 200));
        getBtn.addActionListener(e -> {
            String keyText = keyField.getText().trim();
            if (!keyText.isEmpty()) {
                String keyType = (String) keyTypeCombo.getSelectedItem();
                Object key = parseValue(keyText, keyType);
                Object value = hashMap.get(key);
                updateStatus("get = " + (value != null ? value : "null"), value != null ? SUCCESS_COLOR : WARN_COLOR);
            } else {
                updateStatus("Enter key first", WARN_COLOR);
            }
        });
        methodPanel.add(getBtn);

        JButton containsBtn = createSmallButton("containsKey", new Color(150, 200, 255));
        containsBtn.addActionListener(e -> {
            String keyText = keyField.getText().trim();
            if (!keyText.isEmpty()) {
                String keyType = (String) keyTypeCombo.getSelectedItem();
                Object key = parseValue(keyText, keyType);
                boolean found = hashMap.containsKey(key);
                updateStatus("containsKey = " + found, found ? SUCCESS_COLOR : WARN_COLOR);
            } else {
                updateStatus("Enter key first", WARN_COLOR);
            }
        });
        methodPanel.add(containsBtn);

        JButton sizeBtn = createSmallButton("size()", new Color(150, 200, 255));
        sizeBtn.addActionListener(e -> updateStatus("size() = " + hashMap.getSize(), TEXT_COLOR));
        methodPanel.add(sizeBtn);

        JButton removeBtn = createSmallButton("remove()", ERROR_COLOR);
        removeBtn.addActionListener(e -> {
            String keyText = keyField.getText().trim();
            if (!keyText.isEmpty()) {
                String keyType = (String) keyTypeCombo.getSelectedItem();
                Object key = parseValue(keyText, keyType);
                Object removed = hashMap.remove(key);
                if (removed != null) {
                    updateStatus("Removed: " + key, SUCCESS_COLOR);
                    keyField.setText("");
                } else {
                    updateStatus("Key not found", ERROR_COLOR);
                }
            } else {
                updateStatus("Enter key first", WARN_COLOR);
            }
        });
        methodPanel.add(removeBtn);
        add(methodPanel);
        add(Box.createVerticalStrut(10));

        add(createTitle("OPERATIONS"));
        add(Box.createVerticalStrut(6));

        JButton clearBtn = createStyledButton("clear()", new Color(200, 80, 80));
        clearBtn.addActionListener(e -> {
            hashMap.clear();
            updateStatus("Cleared all entries", WARN_COLOR);
        });
        add(clearBtn);
        add(Box.createVerticalStrut(8));

        JPanel autoPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        autoPanel.setBackground(BG_COLOR);
        autoPanel.setMaximumSize(new Dimension(230, 28));
        autoPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton fill10Btn = createSmallButton("Fill x10", new Color(200, 180, 80));
        fill10Btn.addActionListener(e -> autoFill(10));
        autoPanel.add(fill10Btn);

        JButton fill20Btn = createSmallButton("Fill x20", new Color(220, 200, 100));
        fill20Btn.addActionListener(e -> autoFill(20));
        autoPanel.add(fill20Btn);
        add(autoPanel);
        add(Box.createVerticalStrut(8));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(230, 20));
        add(statusLabel);

        add(Box.createVerticalGlue());
        add(createInfoPanel());
    }

    private void autoFill(int count) {
        Timer timer = new Timer(150, null);
        final int[] added = {0};
        timer.addActionListener(evt -> {
            if (added[0] < count) {
                Object k = VisualHashMap.generateRandomKey("String", random);
                Object v = VisualHashMap.generateRandomValue("int", random);
                hashMap.put(k, v, "String", "int");
                added[0]++;
                updateStatus("Adding... " + added[0] + "/" + count, SUCCESS_COLOR);
            } else {
                timer.stop();
                updateStatus("Added " + count + " entries", SUCCESS_COLOR);
            }
        });
        timer.start();
    }

    private void putEntry() {
        String keyType = (String) keyTypeCombo.getSelectedItem();
        String valueType = (String) valueTypeCombo.getSelectedItem();
        String keyText = keyField.getText().trim();
        String valueText = valueField.getText().trim();

        Object key = keyText.isEmpty() ?
                VisualHashMap.generateRandomKey(keyType, random) :
                parseValue(keyText, keyType);
        Object value = valueText.isEmpty() ?
                VisualHashMap.generateRandomValue(valueType, random) :
                parseValue(valueText, valueType);

        hashMap.put(key, value, keyType, valueType);
        updateStatus("Put: " + key + " -> " + value, SUCCESS_COLOR);
        keyField.setText("");
        valueField.setText("");
    }

    private Object parseValue(String text, String type) {
        if (text.equalsIgnoreCase("null")) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "int" -> Integer.parseInt(text);
            case "double" -> Double.parseDouble(text);
            case "boolean" -> Boolean.parseBoolean(text);
            default -> text;
        };
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
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
        combo.setMaximumSize(new Dimension(250, 26));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        combo.setBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1));
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BUTTON_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("Consolas", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(250, 26));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
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

    private JButton createSmallButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(color);
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.darker());
                } else {
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);

                g2d.setColor(TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };

        button.setFont(new Font("Consolas", Font.PLAIN, 11));
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
        panel.setMaximumSize(new Dimension(230, 100));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("HASHMAP INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {
                "O(1) put/get/remove",
                "Key-Value pairs",
                "Load factor: 0.75",
                "Null keys allowed"
        };

        for (String line : info) {
            JLabel label = new JLabel(line);
            label.setForeground(new Color(200, 190, 150));
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
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
