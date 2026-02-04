package game.ui;

import game.model.VisualArrayList;
import game.model.VisualElement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ControlPanel extends JPanel {
    private VisualArrayList arrayList;
    private Random random = new Random();
    private JTextField valueField;
    private JTextField indexField;
    private JComboBox<String> typeCombo;
    private JLabel statusLabel;
    private JTextField capacityField;

    private static final Color BG_COLOR = new Color(15, 20, 30);
    private static final Color PANEL_BG = new Color(25, 35, 50);
    private static final Color ACCENT = new Color(0, 200, 255);
    private static final Color TEXT_COLOR = new Color(200, 220, 255);
    private static final Color BUTTON_BG = new Color(35, 50, 70);
    private static final Color WARN_COLOR = new Color(255, 200, 100);
    private static final Color SUCCESS_COLOR = new Color(100, 255, 150);

    public ControlPanel(VisualArrayList arrayList) {
        this.arrayList = arrayList;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 700));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
    }

    private void initComponents() {
        // Add Element Section
        add(createTitle("ADD ELEMENT"));
        add(Box.createVerticalStrut(6));

        add(createLabel("TYPE:"));
        typeCombo = new JComboBox<>(new String[]{"int", "double", "String", "boolean", "char"});
        styleComboBox(typeCombo);
        add(typeCombo);
        add(Box.createVerticalStrut(4));

        add(createLabel("VALUE (empty=random):"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(4));

        add(createLabel("INDEX (for insert/set):"));
        indexField = new JTextField("0");
        styleTextField(indexField);
        add(indexField);
        add(Box.createVerticalStrut(6));

        // Add buttons row
        JPanel addBtnsPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        addBtnsPanel.setBackground(BG_COLOR);
        addBtnsPanel.setMaximumSize(new Dimension(230, 28));
        addBtnsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton addBtn = createSmallButton("add(e)", ACCENT);
        addBtn.addActionListener(e -> addElement());
        addBtnsPanel.add(addBtn);

        JButton addAtBtn = createSmallButton("add(i,e)", new Color(0, 180, 220));
        addAtBtn.addActionListener(e -> addAtIndex());
        addBtnsPanel.add(addAtBtn);
        add(addBtnsPanel);
        add(Box.createVerticalStrut(4));

        // Set button
        JButton setBtn = createStyledButton("set(index, element)", new Color(255, 180, 80));
        setBtn.addActionListener(e -> setElement());
        add(setBtn);
        add(Box.createVerticalStrut(6));

        // Quick Add
        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(8));

        JPanel quickPanel = new JPanel(new GridLayout(2, 4, 4, 4));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 60));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        String[] types = {"int", "double", "String", "boolean", "char", "Object", "null", "0"};
        Color[] colors = {
                new Color(0, 200, 255), new Color(255, 100, 200),
                new Color(100, 255, 150), new Color(255, 200, 50),
                new Color(200, 100, 255), new Color(255, 150, 100),
                new Color(128, 128, 128), new Color(80, 80, 80)
        };

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            JButton btn = createSmallButton(type, colors[i]);
            btn.addActionListener(e -> {
                if (type.equals("null")) {
                    arrayList.add(null, "null");
                    updateStatus("Added null", SUCCESS_COLOR);
                } else if (type.equals("0")) {
                    arrayList.add(0, "int");
                    updateStatus("Added int 0", SUCCESS_COLOR);
                } else {
                    Object value = VisualArrayList.generateRandomValue(type, random);
                    arrayList.add(value, type);
                    updateStatus("Added " + type, SUCCESS_COLOR);
                }
            });
            quickPanel.add(btn);
        }
        add(quickPanel);
        add(Box.createVerticalStrut(12));

        // ArrayList Methods Section
        add(createTitle("ARRAYLIST METHODS"));
        add(Box.createVerticalStrut(8));

        // trimToSize
        JButton trimBtn = createStyledButton("trimToSize()", new Color(255, 180, 100));
        trimBtn.addActionListener(e -> {
            int oldCap = arrayList.getCapacity();
            arrayList.trimToSize();
            int newCap = arrayList.getCapacity();
            updateStatus("Trimmed: " + oldCap + " -> " + newCap, WARN_COLOR);
        });
        add(trimBtn);
        add(Box.createVerticalStrut(4));

        // ensureCapacity
        JPanel ensurePanel = new JPanel();
        ensurePanel.setLayout(new BoxLayout(ensurePanel, BoxLayout.X_AXIS));
        ensurePanel.setBackground(BG_COLOR);
        ensurePanel.setMaximumSize(new Dimension(230, 30));
        ensurePanel.setAlignmentX(LEFT_ALIGNMENT);

        capacityField = new JTextField("20");
        capacityField.setBackground(BUTTON_BG);
        capacityField.setForeground(TEXT_COLOR);
        capacityField.setCaretColor(ACCENT);
        capacityField.setFont(new Font("Consolas", Font.PLAIN, 11));
        capacityField.setMaximumSize(new Dimension(50, 28));
        capacityField.setPreferredSize(new Dimension(50, 28));
        capacityField.setBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1));

        JButton ensureBtn = createSmallButton("ensureCapacity", new Color(100, 200, 255));
        ensureBtn.setPreferredSize(new Dimension(120, 28));
        ensureBtn.addActionListener(e -> {
            try {
                int minCap = Integer.parseInt(capacityField.getText().trim());
                int oldCap = arrayList.getCapacity();
                arrayList.ensureCapacity(minCap);
                int newCap = arrayList.getCapacity();
                updateStatus("Ensured: " + oldCap + " -> " + newCap, SUCCESS_COLOR);
            } catch (NumberFormatException ex) {
                updateStatus("Invalid capacity!", new Color(255, 100, 100));
            }
        });

        ensurePanel.add(ensureBtn);
        ensurePanel.add(Box.createHorizontalStrut(5));
        ensurePanel.add(capacityField);
        add(ensurePanel);
        add(Box.createVerticalStrut(4));

        // size() and isEmpty()
        JPanel sizePanel = new JPanel(new GridLayout(1, 2, 4, 0));
        sizePanel.setBackground(BG_COLOR);
        sizePanel.setMaximumSize(new Dimension(230, 26));
        sizePanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton sizeBtn = createSmallButton("size()", new Color(150, 200, 255));
        sizeBtn.addActionListener(e -> updateStatus("size() = " + arrayList.getSize(), TEXT_COLOR));
        sizePanel.add(sizeBtn);

        JButton isEmptyBtn = createSmallButton("isEmpty()", new Color(150, 200, 255));
        isEmptyBtn.addActionListener(e -> updateStatus("isEmpty() = " + (arrayList.getSize() == 0), TEXT_COLOR));
        sizePanel.add(isEmptyBtn);
        add(sizePanel);
        add(Box.createVerticalStrut(4));

        // Search methods
        JPanel searchPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(230, 26));
        searchPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton getBtn = createSmallButton("get(i)", new Color(100, 200, 200));
        getBtn.addActionListener(e -> {
            try {
                int idx = Integer.parseInt(indexField.getText().trim());
                VisualElement elem = arrayList.get(idx);
                if (elem != null) {
                    updateStatus("get(" + idx + ") = " + elem.getValue(), SUCCESS_COLOR);
                } else {
                    updateStatus("Index out of bounds!", new Color(255, 100, 100));
                }
            } catch (NumberFormatException ex) {
                updateStatus("Invalid index!", new Color(255, 100, 100));
            }
        });
        searchPanel.add(getBtn);

        JButton indexOfBtn = createSmallButton("indexOf(v)", new Color(100, 180, 200));
        indexOfBtn.addActionListener(e -> {
            String val = valueField.getText().trim();
            if (!val.isEmpty()) {
                int idx = arrayList.indexOf(val);
                updateStatus("indexOf = " + idx, idx >= 0 ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        searchPanel.add(indexOfBtn);
        add(searchPanel);
        add(Box.createVerticalStrut(4));

        JPanel search2Panel = new JPanel(new GridLayout(1, 2, 4, 0));
        search2Panel.setBackground(BG_COLOR);
        search2Panel.setMaximumSize(new Dimension(230, 26));
        search2Panel.setAlignmentX(LEFT_ALIGNMENT);

        JButton lastIdxBtn = createSmallButton("lastIndexOf", new Color(80, 160, 180));
        lastIdxBtn.addActionListener(e -> {
            String val = valueField.getText().trim();
            if (!val.isEmpty()) {
                int idx = arrayList.lastIndexOf(val);
                updateStatus("lastIndexOf = " + idx, idx >= 0 ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        search2Panel.add(lastIdxBtn);

        JButton containsBtn = createSmallButton("contains(v)", new Color(80, 180, 160));
        containsBtn.addActionListener(e -> {
            String val = valueField.getText().trim();
            if (!val.isEmpty()) {
                boolean found = arrayList.contains(val);
                updateStatus("contains = " + found, found ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        search2Panel.add(containsBtn);
        add(search2Panel);
        add(Box.createVerticalStrut(4));

        // getFirst/getLast
        JPanel firstLastPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        firstLastPanel.setBackground(BG_COLOR);
        firstLastPanel.setMaximumSize(new Dimension(230, 26));
        firstLastPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton getFirstBtn = createSmallButton("getFirst()", new Color(100, 220, 180));
        getFirstBtn.addActionListener(e -> {
            VisualElement elem = arrayList.getFirst();
            updateStatus(elem != null ? "first = " + elem.getValue() : "empty!", elem != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        firstLastPanel.add(getFirstBtn);

        JButton getLastBtn = createSmallButton("getLast()", new Color(100, 200, 160));
        getLastBtn.addActionListener(e -> {
            VisualElement elem = arrayList.getLast();
            updateStatus(elem != null ? "last = " + elem.getValue() : "empty!", elem != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        firstLastPanel.add(getLastBtn);
        add(firstLastPanel);
        add(Box.createVerticalStrut(8));

        // Remove Operations (System.arraycopy)
        add(createTitle("REMOVE (arraycopy)"));
        add(Box.createVerticalStrut(4));

        JPanel removeBtnsPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        removeBtnsPanel.setBackground(BG_COLOR);
        removeBtnsPanel.setMaximumSize(new Dimension(230, 26));
        removeBtnsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton removeAtBtn = createSmallButton("remove(i)", new Color(255, 120, 100));
        removeAtBtn.addActionListener(e -> {
            try {
                int idx = Integer.parseInt(indexField.getText().trim());
                if (idx >= 0 && idx < arrayList.getSize()) {
                    arrayList.remove(idx);
                    updateStatus("Removed [" + idx + "] (shift left)", WARN_COLOR);
                } else {
                    updateStatus("Index out of bounds!", new Color(255, 100, 100));
                }
            } catch (NumberFormatException ex) {
                updateStatus("Invalid index!", new Color(255, 100, 100));
            }
        });
        removeBtnsPanel.add(removeAtBtn);

        JButton removeLastBtn = createSmallButton("removeLast()", new Color(255, 100, 100));
        removeLastBtn.addActionListener(e -> {
            if (arrayList.getSize() > 0) {
                arrayList.removeLast();
                updateStatus("Removed last", WARN_COLOR);
            } else {
                updateStatus("List is empty!", new Color(255, 100, 100));
            }
        });
        removeBtnsPanel.add(removeLastBtn);
        add(removeBtnsPanel);
        add(Box.createVerticalStrut(4));

        JPanel remove2Panel = new JPanel(new GridLayout(1, 2, 4, 0));
        remove2Panel.setBackground(BG_COLOR);
        remove2Panel.setMaximumSize(new Dimension(230, 26));
        remove2Panel.setAlignmentX(LEFT_ALIGNMENT);

        JButton removeFirstBtn = createSmallButton("removeFirst()", new Color(255, 130, 100));
        removeFirstBtn.addActionListener(e -> {
            if (arrayList.getSize() > 0) {
                arrayList.removeFirst();
                updateStatus("Removed first (shift all)", WARN_COLOR);
            } else {
                updateStatus("List is empty!", new Color(255, 100, 100));
            }
        });
        remove2Panel.add(removeFirstBtn);

        JButton clearBtn = createSmallButton("clear()", new Color(255, 80, 80));
        clearBtn.addActionListener(e -> {
            arrayList.clear();
            updateStatus("Cleared all", WARN_COLOR);
        });
        remove2Panel.add(clearBtn);
        add(remove2Panel);
        add(Box.createVerticalStrut(4));

        // removeRange
        JButton removeRangeBtn = createStyledButton("removeRange(from, to)", new Color(255, 100, 120));
        removeRangeBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter range (from,to):", "0," + Math.min(3, arrayList.getSize()));
            if (input != null && input.contains(",")) {
                String[] parts = input.split(",");
                try {
                    int from = Integer.parseInt(parts[0].trim());
                    int to = Integer.parseInt(parts[1].trim());
                    arrayList.removeRange(from, to);
                    updateStatus("Removed [" + from + "," + to + ")", WARN_COLOR);
                } catch (Exception ex) {
                    updateStatus("Invalid range!", new Color(255, 100, 100));
                }
            }
        });
        add(removeRangeBtn);
        add(Box.createVerticalStrut(6));

        // Bulk operations
        add(createTitle("BULK (arraycopy)"));
        add(Box.createVerticalStrut(4));

        JPanel bulkPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        bulkPanel.setBackground(BG_COLOR);
        bulkPanel.setMaximumSize(new Dimension(230, 26));
        bulkPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton addAllBtn = createSmallButton("addAll(5)", new Color(100, 220, 150));
        addAllBtn.addActionListener(e -> {
            List<Object> items = new ArrayList<>();
            for (int i = 0; i < 5; i++) items.add(random.nextInt(100));
            arrayList.addAll(items, "int");
            updateStatus("addAll: +5 (arraycopy)", SUCCESS_COLOR);
        });
        bulkPanel.add(addAllBtn);

        JButton addAllAtBtn = createSmallButton("addAll(i,5)", new Color(80, 200, 130));
        addAllAtBtn.addActionListener(e -> {
            try {
                int idx = Integer.parseInt(indexField.getText().trim());
                List<Object> items = new ArrayList<>();
                for (int i = 0; i < 5; i++) items.add(random.nextInt(100));
                arrayList.addAllAt(idx, items, "int");
                updateStatus("addAll[" + idx + "]: +5 (shift)", SUCCESS_COLOR);
            } catch (NumberFormatException ex) {
                updateStatus("Invalid index!", new Color(255, 100, 100));
            }
        });
        bulkPanel.add(addAllAtBtn);
        add(bulkPanel);
        add(Box.createVerticalStrut(4));

        JPanel copyPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        copyPanel.setBackground(BG_COLOR);
        copyPanel.setMaximumSize(new Dimension(230, 26));
        copyPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton toArrayBtn = createSmallButton("toArray()", new Color(180, 150, 255));
        toArrayBtn.addActionListener(e -> {
            Object[] arr = arrayList.toArray();
            updateStatus("toArray(): Object[" + arr.length + "]", new Color(180, 150, 255));
        });
        copyPanel.add(toArrayBtn);

        JButton cloneBtn = createSmallButton("clone()", new Color(150, 180, 255));
        cloneBtn.addActionListener(e -> {
            arrayList.cloneList();
            updateStatus("clone(): shallow copy", new Color(150, 180, 255));
        });
        copyPanel.add(cloneBtn);
        add(copyPanel);
        add(Box.createVerticalStrut(6));

        // Auto Mode
        add(createTitle("AUTO MODE"));
        add(Box.createVerticalStrut(8));

        JPanel autoPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        autoPanel.setBackground(BG_COLOR);
        autoPanel.setMaximumSize(new Dimension(230, 32));
        autoPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton fill10Btn = createSmallButton("Fill x10", new Color(100, 200, 100));
        fill10Btn.addActionListener(e -> autoFill(10));
        autoPanel.add(fill10Btn);

        JButton fill20Btn = createSmallButton("Fill x20", new Color(150, 220, 100));
        fill20Btn.addActionListener(e -> autoFill(20));
        autoPanel.add(fill20Btn);
        add(autoPanel);
        add(Box.createVerticalStrut(10));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(230, 20));
        add(statusLabel);

        add(Box.createVerticalGlue());

        // Info Panel
        add(createInfoPanel());
    }

    private void autoFill(int count) {
        Timer timer = new Timer(150, null);
        final int[] added = {0};
        timer.addActionListener(evt -> {
            if (added[0] < count) {
                String[] types = {"int", "double", "String", "boolean", "char"};
                String type = types[random.nextInt(types.length)];
                Object value = VisualArrayList.generateRandomValue(type, random);
                arrayList.add(value, type);
                added[0]++;
                updateStatus("Adding... " + added[0] + "/" + count, SUCCESS_COLOR);
            } else {
                timer.stop();
                updateStatus("Added " + count + " elements", SUCCESS_COLOR);
            }
        });
        timer.start();
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void addElement() {
        String type = (String) typeCombo.getSelectedItem();
        String valueText = valueField.getText().trim();
        Object value;

        if (valueText.isEmpty()) {
            value = VisualArrayList.generateRandomValue(type, random);
        } else {
            try {
                value = parseValue(valueText, type);
            } catch (Exception e) {
                value = VisualArrayList.generateRandomValue(type, random);
            }
        }

        arrayList.add(value, type);
        valueField.setText("");
        updateStatus("add(" + value + ")", SUCCESS_COLOR);
    }

    private void addAtIndex() {
        String type = (String) typeCombo.getSelectedItem();
        String valueText = valueField.getText().trim();
        Object value;

        if (valueText.isEmpty()) {
            value = VisualArrayList.generateRandomValue(type, random);
        } else {
            try {
                value = parseValue(valueText, type);
            } catch (Exception e) {
                value = VisualArrayList.generateRandomValue(type, random);
            }
        }

        try {
            int idx = Integer.parseInt(indexField.getText().trim());
            arrayList.addAt(idx, value, type);
            valueField.setText("");
            updateStatus("add(" + idx + ", " + value + ") shift right", SUCCESS_COLOR);
        } catch (NumberFormatException e) {
            updateStatus("Invalid index!", new Color(255, 100, 100));
        }
    }

    private void setElement() {
        String type = (String) typeCombo.getSelectedItem();
        String valueText = valueField.getText().trim();
        Object value;

        if (valueText.isEmpty()) {
            value = VisualArrayList.generateRandomValue(type, random);
        } else {
            try {
                value = parseValue(valueText, type);
            } catch (Exception e) {
                value = VisualArrayList.generateRandomValue(type, random);
            }
        }

        try {
            int idx = Integer.parseInt(indexField.getText().trim());
            Object old = arrayList.set(idx, value, type);
            valueField.setText("");
            if (old != null) {
                updateStatus("set(" + idx + "): " + old + " -> " + value, WARN_COLOR);
            } else {
                updateStatus("Index out of bounds!", new Color(255, 100, 100));
            }
        } catch (NumberFormatException e) {
            updateStatus("Invalid index!", new Color(255, 100, 100));
        }
    }

    private Object parseValue(String text, String type) {
        // Support null literal for any type
        if (text.equalsIgnoreCase("null")) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "int" -> Integer.parseInt(text);
            case "double" -> Double.parseDouble(text);
            case "boolean" -> Boolean.parseBoolean(text);
            case "char" -> text.isEmpty() ? '\0' : text.charAt(0);
            default -> text;
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

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BUTTON_BG);
        combo.setForeground(TEXT_COLOR);
        combo.setFont(new Font("Consolas", Font.PLAIN, 13));
        combo.setMaximumSize(new Dimension(250, 30));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        combo.setBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1));
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BUTTON_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("Consolas", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(250, 30));
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
        panel.setMaximumSize(new Dimension(230, 130));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("SYSTEM.ARRAYCOPY");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 10));
        panel.add(title);

        String[] info = {
                "add(i,e) - shift right O(n)",
                "remove(i) - shift left O(n)",
                "addAll(i,c) - shift + copy",
                "removeRange - shift left",
                "toArray/clone - full copy",
                "",
                "Only add/removeLast O(1)"
        };

        for (String line : info) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(new Color(150, 170, 200));
            label.setFont(new Font("Consolas", Font.PLAIN, 9));
            panel.add(label);
        }

        return panel;
    }
}
