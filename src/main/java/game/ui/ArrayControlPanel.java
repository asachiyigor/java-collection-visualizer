package game.ui;

import game.model.VisualArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class ArrayControlPanel extends JPanel {
    private VisualArray array;
    private Random random = new Random();
    private JTextField valueField;
    private JTextField indexField;
    private JLabel statusLabel;

    private static final Color BG_COLOR = new Color(15, 25, 20);
    private static final Color PANEL_BG = new Color(30, 50, 40);
    private static final Color ACCENT = new Color(100, 255, 150);
    private static final Color TEXT_COLOR = new Color(200, 230, 210);
    private static final Color BUTTON_BG = new Color(40, 60, 50);
    private static final Color WARN_COLOR = new Color(255, 200, 100);
    private static final Color SUCCESS_COLOR = new Color(150, 255, 200);
    private static final Color ERROR_COLOR = new Color(255, 100, 100);

    public ArrayControlPanel(VisualArray array) {
        this.array = array;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 600));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
    }

    private void initComponents() {
        // Add Element Section
        add(createTitle("ADD ELEMENT"));
        add(Box.createVerticalStrut(8));

        JLabel typeInfo = new JLabel("Type: " + array.getElementType() +
                (array.isPrimitive() ? " (primitive)" : " (object)"));
        typeInfo.setForeground(ACCENT);
        typeInfo.setFont(new Font("Consolas", Font.PLAIN, 10));
        typeInfo.setAlignmentX(LEFT_ALIGNMENT);
        add(typeInfo);
        add(Box.createVerticalStrut(8));

        add(createLabel("VALUE:"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(8));

        JButton addBtn = createStyledButton("+ ADD NEXT", ACCENT);
        addBtn.addActionListener(e -> addElement());
        add(addBtn);
        add(Box.createVerticalStrut(12));

        // Set at Index
        add(createTitle("SET AT INDEX"));
        add(Box.createVerticalStrut(8));

        add(createLabel("INDEX (0-" + (array.getCapacity()-1) + "):"));
        indexField = new JTextField();
        styleTextField(indexField);
        add(indexField);
        add(Box.createVerticalStrut(4));

        JButton setBtn = createStyledButton("SET VALUE", new Color(100, 200, 255));
        setBtn.addActionListener(e -> setAtIndex());
        add(setBtn);
        add(Box.createVerticalStrut(12));

        // Array Methods
        add(createTitle("ARRAY METHODS"));
        add(Box.createVerticalStrut(8));

        JPanel methodPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        methodPanel.setBackground(BG_COLOR);
        methodPanel.setMaximumSize(new Dimension(230, 56));
        methodPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton lengthBtn = createSmallButton("length", new Color(150, 200, 255));
        lengthBtn.addActionListener(e -> updateStatus("length = " + array.getCapacity(), TEXT_COLOR));
        methodPanel.add(lengthBtn);

        JButton usedBtn = createSmallButton("used slots", new Color(150, 200, 255));
        usedBtn.addActionListener(e -> updateStatus("used = " + array.getSize() + "/" + array.getCapacity(), TEXT_COLOR));
        methodPanel.add(usedBtn);

        JButton getBtn = createSmallButton("get[idx]", new Color(150, 255, 200));
        getBtn.addActionListener(e -> {
            String idxText = indexField.getText().trim();
            if (!idxText.isEmpty()) {
                try {
                    int idx = Integer.parseInt(idxText);
                    if (idx >= 0 && idx < array.getSize()) {
                        updateStatus("get[" + idx + "] = value", SUCCESS_COLOR);
                    } else {
                        updateStatus("Index out of range", ERROR_COLOR);
                    }
                } catch (Exception ex) {
                    updateStatus("Invalid index", ERROR_COLOR);
                }
            } else {
                updateStatus("Enter index first", WARN_COLOR);
            }
        });
        methodPanel.add(getBtn);

        JButton cloneBtn = createSmallButton("clone()", new Color(150, 200, 255));
        cloneBtn.addActionListener(e -> updateStatus("clone() - creates copy", TEXT_COLOR));
        methodPanel.add(cloneBtn);
        add(methodPanel);
        add(Box.createVerticalStrut(12));

        // Quick Add
        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(8));

        JPanel quickPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 56));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton addRandom = createSmallButton("Random", ACCENT);
        addRandom.addActionListener(e -> {
            Object val = VisualArray.generateRandomValue(array.getElementType(), random);
            if (array.add(val)) {
                updateStatus("Added: " + val, SUCCESS_COLOR);
            } else {
                updateStatus("Array is FULL!", ERROR_COLOR);
            }
        });
        quickPanel.add(addRandom);

        JButton addZero = createSmallButton("Zero", new Color(150, 200, 255));
        addZero.addActionListener(e -> {
            Object val = getDefaultValue(array.getElementType());
            if (array.add(val)) {
                updateStatus("Added: " + val, SUCCESS_COLOR);
            } else {
                updateStatus("Array is FULL!", ERROR_COLOR);
            }
        });
        quickPanel.add(addZero);

        JButton fill5 = createSmallButton("Fill +5", new Color(200, 255, 150));
        fill5.addActionListener(e -> {
            int added = 0;
            for (int i = 0; i < 5; i++) {
                Object val = VisualArray.generateRandomValue(array.getElementType(), random);
                if (array.add(val)) added++;
            }
            if (added < 5) {
                updateStatus("Added " + added + " (full)", WARN_COLOR);
            } else {
                updateStatus("Added 5 elements", SUCCESS_COLOR);
            }
        });
        quickPanel.add(fill5);

        JButton fillAll = createSmallButton("Fill All", new Color(255, 200, 150));
        fillAll.addActionListener(e -> autoFill());
        quickPanel.add(fillAll);
        add(quickPanel);
        add(Box.createVerticalStrut(12));

        // Operations
        add(createTitle("OPERATIONS"));
        add(Box.createVerticalStrut(8));

        JButton removeLastBtn = createStyledButton("remove last", new Color(255, 100, 100));
        removeLastBtn.addActionListener(e -> {
            if (array.getSize() > 0) {
                array.removeLast();
                updateStatus("Removed last", WARN_COLOR);
            } else {
                updateStatus("Array is empty!", ERROR_COLOR);
            }
        });
        add(removeLastBtn);
        add(Box.createVerticalStrut(4));

        JButton clearBtn = createStyledButton("clear all", new Color(255, 80, 80));
        clearBtn.addActionListener(e -> {
            array.clear();
            updateStatus("Cleared all", WARN_COLOR);
        });
        add(clearBtn);
        add(Box.createVerticalStrut(10));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(230, 20));
        add(statusLabel);

        add(Box.createVerticalGlue());
        add(createInfoPanel());
    }

    private void autoFill() {
        Timer timer = new Timer(100, null);
        timer.addActionListener(evt -> {
            Object val = VisualArray.generateRandomValue(array.getElementType(), random);
            if (!array.add(val)) {
                timer.stop();
                updateStatus("Array is FULL!", WARN_COLOR);
            } else {
                updateStatus("Filling... " + array.getSize() + "/" + array.getCapacity(), SUCCESS_COLOR);
            }
        });
        timer.start();
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void addElement() {
        String valueText = valueField.getText().trim();
        Object value;

        if (valueText.isEmpty()) {
            value = VisualArray.generateRandomValue(array.getElementType(), random);
        } else {
            try {
                value = parseValue(valueText, array.getElementType());
            } catch (Exception e) {
                updateStatus("Invalid value!", ERROR_COLOR);
                return;
            }
        }

        if (array.add(value)) {
            updateStatus("Added: " + value, SUCCESS_COLOR);
            valueField.setText("");
        } else {
            updateStatus("Array is FULL!", ERROR_COLOR);
        }
    }

    private void setAtIndex() {
        String indexText = indexField.getText().trim();
        String valueText = valueField.getText().trim();

        if (indexText.isEmpty()) {
            updateStatus("Enter an index!", WARN_COLOR);
            return;
        }

        int index;
        try {
            index = Integer.parseInt(indexText);
        } catch (Exception e) {
            updateStatus("Invalid index!", ERROR_COLOR);
            return;
        }

        if (index < 0 || index >= array.getCapacity()) {
            updateStatus("Index 0-" + (array.getCapacity()-1) + "!", ERROR_COLOR);
            return;
        }

        Object value;
        if (valueText.isEmpty()) {
            value = VisualArray.generateRandomValue(array.getElementType(), random);
        } else {
            try {
                value = parseValue(valueText, array.getElementType());
            } catch (Exception e) {
                updateStatus("Invalid value!", ERROR_COLOR);
                return;
            }
        }

        array.set(index, value);
        updateStatus("[" + index + "] = " + value, SUCCESS_COLOR);
        valueField.setText("");
        indexField.setText("");
    }

    private Object parseValue(String text, String type) {
        if (text.equalsIgnoreCase("null")) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "int" -> Integer.parseInt(text);
            case "double" -> Double.parseDouble(text);
            case "boolean" -> Boolean.parseBoolean(text);
            case "char" -> text.isEmpty() ? '\0' : text.charAt(0);
            case "byte" -> Byte.parseByte(text);
            case "short" -> Short.parseShort(text);
            case "long" -> Long.parseLong(text);
            case "float" -> Float.parseFloat(text);
            default -> text;
        };
    }

    private Object getDefaultValue(String type) {
        return switch (type.toLowerCase()) {
            case "int", "byte", "short" -> 0;
            case "long" -> 0L;
            case "double" -> 0.0;
            case "float" -> 0.0f;
            case "boolean" -> false;
            case "char" -> '0';
            default -> "";
        };
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

    private void styleTextField(JTextField field) {
        field.setBackground(BUTTON_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("Consolas", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(250, 28));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
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
        panel.setMaximumSize(new Dimension(230, 120));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("ARRAY INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {
                "FIXED size: " + array.getCapacity(),
                "Cannot resize!",
                "O(1) get/set by index",
                "",
                "Memory: 16 + n*" + getPrimitiveSize() + " B",
                "(no object overhead)"
        };

        for (String line : info) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(line.contains("Cannot") ? WARN_COLOR : new Color(160, 200, 180));
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            panel.add(label);
        }

        return panel;
    }

    private int getPrimitiveSize() {
        return switch (array.getElementType().toLowerCase()) {
            case "byte", "boolean" -> 1;
            case "char", "short" -> 2;
            case "int", "float" -> 4;
            case "long", "double" -> 8;
            default -> 4;
        };
    }
}
