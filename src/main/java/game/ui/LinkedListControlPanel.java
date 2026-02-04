package game.ui;

import game.model.VisualLinkedList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import game.ui.ThemeManager;

public class LinkedListControlPanel extends JPanel {
    private VisualLinkedList linkedList;
    private Random random = new Random();
    private JTextField valueField;
    private JComboBox<String> typeCombo;
    private JLabel statusLabel;

    private static Color BG_COLOR = ThemeManager.get().getBgColor();
    private static Color PANEL_BG = ThemeManager.get().getPanelBg();
    private static final Color ACCENT = new Color(255, 100, 150);
    private static Color TEXT_COLOR = ThemeManager.get().getTextColor();
    private static Color BUTTON_BG = ThemeManager.get().getButtonBg();
    private static Color SUCCESS_COLOR = ThemeManager.get().getSuccessColor();
    private static Color WARN_COLOR = ThemeManager.get().getWarnColor();

    public LinkedListControlPanel(VisualLinkedList linkedList) {
        this.linkedList = linkedList;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 600));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
        ThemeManager.get().addListener(() -> { updateThemeColors(); repaint(); });
    }

    private void initComponents() {
        // Add Node Section
        add(createTitle("ADD NODE"));
        add(Box.createVerticalStrut(10));

        add(createLabel("TYPE:"));
        typeCombo = new JComboBox<>(new String[]{"int", "double", "String", "boolean", "char"});
        styleComboBox(typeCombo);
        add(typeCombo);
        add(Box.createVerticalStrut(8));

        add(createLabel("VALUE (optional):"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(10));

        // Add buttons in grid
        JPanel addPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        addPanel.setBackground(BG_COLOR);
        addPanel.setMaximumSize(new Dimension(230, 30));
        addPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton addLastBtn = createSmallButton("+ ADD LAST", ACCENT);
        addLastBtn.addActionListener(e -> {
            addElement(false);
            updateStatus("Added to end", SUCCESS_COLOR);
        });
        addPanel.add(addLastBtn);

        JButton addFirstBtn = createSmallButton("+ ADD FIRST", new Color(200, 100, 255));
        addFirstBtn.addActionListener(e -> {
            addElement(true);
            updateStatus("Added to start", SUCCESS_COLOR);
        });
        addPanel.add(addFirstBtn);
        add(addPanel);
        add(Box.createVerticalStrut(12));

        // Quick Add
        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(8));

        JPanel quickPanel = new JPanel(new GridLayout(2, 3, 4, 4));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 60));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        String[] types = {"int", "double", "String", "boolean", "char", "Object"};
        Color[] colors = {
                new Color(0, 200, 255), new Color(255, 100, 200),
                new Color(100, 255, 150), new Color(255, 200, 50),
                new Color(200, 100, 255), new Color(255, 150, 100)
        };

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            JButton btn = createSmallButton(type, colors[i]);
            btn.addActionListener(e -> {
                Object value = VisualLinkedList.generateRandomValue(type, random);
                linkedList.addLast(value, type);
                updateStatus("Added " + type, SUCCESS_COLOR);
            });
            quickPanel.add(btn);
        }
        add(quickPanel);
        add(Box.createVerticalStrut(12));

        // LinkedList Methods
        add(createTitle("LINKEDLIST METHODS"));
        add(Box.createVerticalStrut(8));

        JPanel methodPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        methodPanel.setBackground(BG_COLOR);
        methodPanel.setMaximumSize(new Dimension(230, 56));
        methodPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton sizeBtn = createSmallButton("size()", new Color(150, 200, 255));
        sizeBtn.addActionListener(e -> updateStatus("size() = " + linkedList.getSize(), TEXT_COLOR));
        methodPanel.add(sizeBtn);

        JButton peekFirstBtn = createSmallButton("peekFirst()", new Color(150, 200, 255));
        peekFirstBtn.addActionListener(e -> {
            if (linkedList.getSize() > 0) {
                updateStatus("First node exists", TEXT_COLOR);
            } else {
                updateStatus("List is empty", WARN_COLOR);
            }
        });
        methodPanel.add(peekFirstBtn);

        JButton peekLastBtn = createSmallButton("peekLast()", new Color(150, 200, 255));
        peekLastBtn.addActionListener(e -> {
            if (linkedList.getSize() > 0) {
                updateStatus("Last node exists", TEXT_COLOR);
            } else {
                updateStatus("List is empty", WARN_COLOR);
            }
        });
        methodPanel.add(peekLastBtn);

        JButton isEmptyBtn = createSmallButton("isEmpty()", new Color(150, 200, 255));
        isEmptyBtn.addActionListener(e -> updateStatus("isEmpty() = " + (linkedList.getSize() == 0), TEXT_COLOR));
        methodPanel.add(isEmptyBtn);
        add(methodPanel);
        add(Box.createVerticalStrut(12));

        // Operations
        add(createTitle("OPERATIONS"));
        add(Box.createVerticalStrut(8));

        JPanel removePanel = new JPanel(new GridLayout(1, 2, 4, 0));
        removePanel.setBackground(BG_COLOR);
        removePanel.setMaximumSize(new Dimension(230, 30));
        removePanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton removeLastBtn = createSmallButton("removeLast()", new Color(255, 100, 100));
        removeLastBtn.addActionListener(e -> {
            if (linkedList.getSize() > 0) {
                linkedList.removeLast();
                updateStatus("Removed last", WARN_COLOR);
            } else {
                updateStatus("List is empty!", new Color(255, 100, 100));
            }
        });
        removePanel.add(removeLastBtn);

        JButton removeFirstBtn = createSmallButton("removeFirst()", new Color(255, 100, 100));
        removeFirstBtn.addActionListener(e -> {
            if (linkedList.getSize() > 0) {
                linkedList.removeFirst();
                updateStatus("Removed first", WARN_COLOR);
            } else {
                updateStatus("List is empty!", new Color(255, 100, 100));
            }
        });
        removePanel.add(removeFirstBtn);
        add(removePanel);
        add(Box.createVerticalStrut(4));

        JButton clearBtn = createStyledButton("clear()", new Color(200, 50, 50));
        clearBtn.addActionListener(e -> {
            linkedList.clear();
            updateStatus("Cleared all nodes", WARN_COLOR);
        });
        add(clearBtn);
        add(Box.createVerticalStrut(12));

        // Auto Mode
        add(createTitle("AUTO MODE"));
        add(Box.createVerticalStrut(8));

        JPanel autoPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        autoPanel.setBackground(BG_COLOR);
        autoPanel.setMaximumSize(new Dimension(230, 30));
        autoPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton fill10Btn = createSmallButton("Fill x10", new Color(150, 100, 200));
        fill10Btn.addActionListener(e -> autoFill(10));
        autoPanel.add(fill10Btn);

        JButton fill20Btn = createSmallButton("Fill x20", new Color(180, 120, 220));
        fill20Btn.addActionListener(e -> autoFill(20));
        autoPanel.add(fill20Btn);
        add(autoPanel);
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

    private void autoFill(int count) {
        Timer timer = new Timer(150, null);
        final int[] added = {0};
        timer.addActionListener(evt -> {
            if (added[0] < count) {
                String[] types = {"int", "double", "String", "boolean", "char"};
                String type = types[random.nextInt(types.length)];
                Object value = VisualLinkedList.generateRandomValue(type, random);
                linkedList.addLast(value, type);
                added[0]++;
                updateStatus("Adding... " + added[0] + "/" + count, SUCCESS_COLOR);
            } else {
                timer.stop();
                updateStatus("Added " + count + " nodes", SUCCESS_COLOR);
            }
        });
        timer.start();
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void addElement(boolean addFirst) {
        String type = (String) typeCombo.getSelectedItem();
        String valueText = valueField.getText().trim();
        Object value;

        if (valueText.isEmpty()) {
            value = VisualLinkedList.generateRandomValue(type, random);
        } else {
            try {
                value = parseValue(valueText, type);
            } catch (Exception e) {
                value = VisualLinkedList.generateRandomValue(type, random);
            }
        }

        if (addFirst) {
            linkedList.addFirst(value, type);
        } else {
            linkedList.addLast(value, type);
        }
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
        combo.setMaximumSize(new Dimension(250, 28));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        combo.setBorder(BorderFactory.createLineBorder(ACCENT.darker(), 1));
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

        JLabel title = new JLabel("LINKEDLIST INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {
                "No fixed capacity",
                "O(1) addFirst/addLast",
                "O(1) removeFirst/Last",
                "O(n) get by index",
                "",
                "Node: 24 bytes each"
        };

        for (String line : info) {
            JLabel label = new JLabel(line.isEmpty() ? " " : line);
            label.setForeground(new Color(180, 160, 200));
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
