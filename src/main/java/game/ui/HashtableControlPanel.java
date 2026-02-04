package game.ui;

import game.model.VisualHashtable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class HashtableControlPanel extends JPanel {
    private VisualHashtable hashtable;
    private Random random = new Random();
    private JTextField keyField;
    private JTextField valueField;
    private JComboBox<String> keyTypeCombo;
    private JComboBox<String> valueTypeCombo;
    private JLabel statusLabel;

    private static final Color BG_COLOR = new Color(20, 14, 10);
    private static final Color PANEL_BG = new Color(40, 30, 22);
    private static final Color ACCENT = new Color(200, 120, 80);
    private static final Color TEXT_COLOR = new Color(255, 230, 210);
    private static final Color BUTTON_BG = new Color(50, 38, 28);
    private static final Color SUCCESS_COLOR = new Color(150, 255, 150);
    private static final Color WARN_COLOR = new Color(255, 200, 100);
    private static final Color ERROR_COLOR = new Color(255, 100, 100);

    public HashtableControlPanel(VisualHashtable hashtable) {
        this.hashtable = hashtable;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 600));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
    }

    private void initComponents() {
        add(createTitle("PUT ENTRY"));
        add(Box.createVerticalStrut(6));

        add(createLabel("KEY TYPE:"));
        keyTypeCombo = new JComboBox<>(new String[]{"String", "int"});
        styleComboBox(keyTypeCombo);
        add(keyTypeCombo);

        add(createLabel("KEY (no null!):"));
        keyField = new JTextField();
        styleTextField(keyField);
        add(keyField);
        add(Box.createVerticalStrut(4));

        add(createLabel("VALUE TYPE:"));
        valueTypeCombo = new JComboBox<>(new String[]{"String", "int", "double"});
        styleComboBox(valueTypeCombo);
        add(valueTypeCombo);

        add(createLabel("VALUE (no null!):"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(6));

        JButton putBtn = createStyledButton("PUT", ACCENT);
        putBtn.addActionListener(e -> putEntry());
        add(putBtn);
        add(Box.createVerticalStrut(8));

        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(4));

        JPanel quickPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 50));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton strStrBtn = createSmallButton("Str:Str", new Color(255, 180, 120));
        strStrBtn.addActionListener(e -> {
            Object k = VisualHashtable.generateRandomKey("String", random);
            Object v = VisualHashtable.generateRandomValue("String", random);
            hashtable.put(k, v, "String", "String");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(strStrBtn);

        JButton strIntBtn = createSmallButton("Str:Int", new Color(200, 180, 150));
        strIntBtn.addActionListener(e -> {
            Object k = VisualHashtable.generateRandomKey("String", random);
            Object v = VisualHashtable.generateRandomValue("int", random);
            hashtable.put(k, v, "String", "int");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(strIntBtn);

        JButton intStrBtn = createSmallButton("Int:Str", new Color(180, 200, 150));
        intStrBtn.addActionListener(e -> {
            Object k = VisualHashtable.generateRandomKey("int", random);
            Object v = VisualHashtable.generateRandomValue("String", random);
            hashtable.put(k, v, "int", "String");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(intStrBtn);

        JButton intIntBtn = createSmallButton("Int:Int", new Color(150, 200, 200));
        intIntBtn.addActionListener(e -> {
            Object k = VisualHashtable.generateRandomKey("int", random);
            Object v = VisualHashtable.generateRandomValue("int", random);
            hashtable.put(k, v, "int", "int");
            updateStatus(k + " -> " + v, SUCCESS_COLOR);
        });
        quickPanel.add(intIntBtn);
        add(quickPanel);
        add(Box.createVerticalStrut(8));

        add(createTitle("METHODS"));
        add(Box.createVerticalStrut(4));

        JPanel methodPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        methodPanel.setBackground(BG_COLOR);
        methodPanel.setMaximumSize(new Dimension(230, 50));
        methodPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton getBtn = createSmallButton("get(key)", new Color(150, 255, 200));
        getBtn.addActionListener(e -> {
            String keyText = keyField.getText().trim();
            if (!keyText.isEmpty()) {
                String keyType = (String) keyTypeCombo.getSelectedItem();
                Object key = parseValue(keyText, keyType);
                Object value = hashtable.get(key);
                updateStatus("get = " + (value != null ? value : "null"), value != null ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        methodPanel.add(getBtn);

        JButton containsKeyBtn = createSmallButton("containsKey", new Color(150, 200, 255));
        containsKeyBtn.addActionListener(e -> {
            String keyText = keyField.getText().trim();
            if (!keyText.isEmpty()) {
                String keyType = (String) keyTypeCombo.getSelectedItem();
                Object key = parseValue(keyText, keyType);
                boolean found = hashtable.containsKey(key);
                updateStatus("containsKey = " + found, found ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        methodPanel.add(containsKeyBtn);

        JButton removeBtn = createSmallButton("remove(key)", new Color(255, 150, 150));
        removeBtn.addActionListener(e -> {
            String keyText = keyField.getText().trim();
            if (!keyText.isEmpty()) {
                String keyType = (String) keyTypeCombo.getSelectedItem();
                Object key = parseValue(keyText, keyType);
                Object removed = hashtable.remove(key);
                updateStatus(removed != null ? "Removed: " + removed : "Not found", removed != null ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        methodPanel.add(removeBtn);

        JButton containsValBtn = createSmallButton("containsVal", new Color(200, 180, 255));
        containsValBtn.addActionListener(e -> {
            String valueText = valueField.getText().trim();
            if (!valueText.isEmpty()) {
                String valueType = (String) valueTypeCombo.getSelectedItem();
                Object value = parseValue(valueText, valueType);
                boolean found = hashtable.containsValue(value);
                updateStatus("containsValue = " + found, found ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        methodPanel.add(containsValBtn);
        add(methodPanel);
        add(Box.createVerticalStrut(8));

        JButton clearBtn = createStyledButton("clear()", new Color(200, 80, 80));
        clearBtn.addActionListener(e -> {
            hashtable.clear();
            updateStatus("Cleared", WARN_COLOR);
        });
        add(clearBtn);
        add(Box.createVerticalStrut(6));

        JPanel autoPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        autoPanel.setBackground(BG_COLOR);
        autoPanel.setMaximumSize(new Dimension(230, 26));
        autoPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton fill5Btn = createSmallButton("Fill x5", new Color(180, 140, 100));
        fill5Btn.addActionListener(e -> autoFill(5));
        autoPanel.add(fill5Btn);

        JButton fill10Btn = createSmallButton("Fill x10", new Color(200, 160, 120));
        fill10Btn.addActionListener(e -> autoFill(10));
        autoPanel.add(fill10Btn);
        add(autoPanel);
        add(Box.createVerticalStrut(6));

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
        Timer timer = new Timer(200, null);
        final int[] added = {0};
        timer.addActionListener(evt -> {
            if (added[0] < count) {
                Object k = VisualHashtable.generateRandomKey("String", random);
                Object v = VisualHashtable.generateRandomValue("int", random);
                hashtable.put(k, v, "String", "int");
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

        Object key = keyText.isEmpty() ? VisualHashtable.generateRandomKey(keyType, random) : parseValue(keyText, keyType);
        Object value = valueText.isEmpty() ? VisualHashtable.generateRandomValue(valueType, random) : parseValue(valueText, valueType);

        hashtable.put(key, value, keyType, valueType);
        updateStatus(key + " -> " + value, SUCCESS_COLOR);
        keyField.setText("");
        valueField.setText("");
    }

    private Object parseValue(String text, String type) {
        // Note: Hashtable does NOT allow null keys or values - throws NPE
        if (text.equalsIgnoreCase("null")) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "int" -> Integer.parseInt(text);
            case "double" -> Double.parseDouble(text);
            default -> text;
        };
    }

    private void updateStatus(String msg, Color color) {
        statusLabel.setText(msg);
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
        combo.setMaximumSize(new Dimension(250, 24));
        combo.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BUTTON_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("Consolas", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(250, 24));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    }

    private JButton createStyledButton(String text, Color accentColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isPressed() ? accentColor.darker() : getModel().isRollover() ? accentColor : BUTTON_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
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
                g2d.setColor(getModel().isPressed() ? color : getModel().isRollover() ? color.darker() : new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                g2d.setColor(color);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
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
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1), new EmptyBorder(8, 10, 8, 10)));
        panel.setMaximumSize(new Dimension(230, 100));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("DICTIONARY INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {"Hashtable extends Dictionary", "Dictionary is abstract class", "null keys/values NOT allowed", "Use HashMap instead!"};
        for (String line : info) {
            JLabel label = new JLabel(line);
            label.setForeground(line.contains("NOT") || line.contains("instead") ? ERROR_COLOR : new Color(200, 160, 120));
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            panel.add(label);
        }
        return panel;
    }
}
