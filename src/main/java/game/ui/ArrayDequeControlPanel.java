package game.ui;

import game.model.VisualArrayDeque;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class ArrayDequeControlPanel extends JPanel {
    private VisualArrayDeque deque;
    private Random random = new Random();
    private JTextField valueField;
    private JComboBox<String> typeCombo;
    private JLabel statusLabel;

    private static final Color BG_COLOR = new Color(18, 12, 28);
    private static final Color PANEL_BG = new Color(35, 25, 50);
    private static final Color ACCENT = new Color(180, 100, 255);
    private static final Color TEXT_COLOR = new Color(230, 210, 255);
    private static final Color BUTTON_BG = new Color(45, 30, 60);
    private static final Color SUCCESS_COLOR = new Color(150, 255, 150);
    private static final Color WARN_COLOR = new Color(255, 200, 100);
    private static final Color ERROR_COLOR = new Color(255, 100, 100);

    public ArrayDequeControlPanel(VisualArrayDeque deque) {
        this.deque = deque;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 600));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
    }

    private void initComponents() {
        add(createTitle("ADD ELEMENT"));
        add(Box.createVerticalStrut(6));

        add(createLabel("TYPE:"));
        typeCombo = new JComboBox<>(new String[]{"int", "String", "double"});
        styleComboBox(typeCombo);
        add(typeCombo);
        add(Box.createVerticalStrut(4));

        add(createLabel("VALUE (empty = random):"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(8));

        JPanel addPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        addPanel.setBackground(BG_COLOR);
        addPanel.setMaximumSize(new Dimension(230, 28));
        addPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton addFirstBtn = createSmallButton("addFirst()", new Color(100, 255, 150));
        addFirstBtn.addActionListener(e -> addFirst());
        addPanel.add(addFirstBtn);

        JButton addLastBtn = createSmallButton("addLast()", new Color(255, 100, 150));
        addLastBtn.addActionListener(e -> addLast());
        addPanel.add(addLastBtn);
        add(addPanel);
        add(Box.createVerticalStrut(10));

        add(createTitle("REMOVE"));
        add(Box.createVerticalStrut(4));

        JPanel removePanel = new JPanel(new GridLayout(1, 2, 4, 0));
        removePanel.setBackground(BG_COLOR);
        removePanel.setMaximumSize(new Dimension(230, 28));
        removePanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton removeFirstBtn = createSmallButton("removeFirst()", new Color(100, 255, 150));
        removeFirstBtn.addActionListener(e -> {
            Object val = deque.removeFirst();
            updateStatus(val != null ? "Removed: " + val : "Empty deque", val != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        removePanel.add(removeFirstBtn);

        JButton removeLastBtn = createSmallButton("removeLast()", new Color(255, 100, 150));
        removeLastBtn.addActionListener(e -> {
            Object val = deque.removeLast();
            updateStatus(val != null ? "Removed: " + val : "Empty deque", val != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        removePanel.add(removeLastBtn);
        add(removePanel);
        add(Box.createVerticalStrut(10));

        add(createTitle("PEEK"));
        add(Box.createVerticalStrut(4));

        JPanel peekPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        peekPanel.setBackground(BG_COLOR);
        peekPanel.setMaximumSize(new Dimension(230, 28));
        peekPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton peekFirstBtn = createSmallButton("peekFirst()", new Color(150, 200, 255));
        peekFirstBtn.addActionListener(e -> {
            Object val = deque.peekFirst();
            updateStatus("First: " + (val != null ? val : "null"), val != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        peekPanel.add(peekFirstBtn);

        JButton peekLastBtn = createSmallButton("peekLast()", new Color(150, 200, 255));
        peekLastBtn.addActionListener(e -> {
            Object val = deque.peekLast();
            updateStatus("Last: " + (val != null ? val : "null"), val != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        peekPanel.add(peekLastBtn);
        add(peekPanel);
        add(Box.createVerticalStrut(10));

        add(createTitle("OTHER"));
        add(Box.createVerticalStrut(4));

        JButton containsBtn = createStyledButton("contains(value)", new Color(150, 200, 255));
        containsBtn.addActionListener(e -> {
            String text = valueField.getText().trim();
            if (!text.isEmpty()) {
                String type = (String) typeCombo.getSelectedItem();
                Object value = parseValue(text, type);
                boolean found = deque.contains(value);
                updateStatus("contains = " + found, found ? SUCCESS_COLOR : WARN_COLOR);
            }
        });
        add(containsBtn);
        add(Box.createVerticalStrut(4));

        JButton clearBtn = createStyledButton("clear()", new Color(200, 80, 80));
        clearBtn.addActionListener(e -> {
            deque.clear();
            updateStatus("Cleared", WARN_COLOR);
        });
        add(clearBtn);
        add(Box.createVerticalStrut(10));

        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(4));

        JPanel quickPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 26));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton fill5Btn = createSmallButton("Fill x5", new Color(100, 200, 180));
        fill5Btn.addActionListener(e -> autoFill(5));
        quickPanel.add(fill5Btn);

        JButton fill10Btn = createSmallButton("Fill x10", new Color(120, 220, 200));
        fill10Btn.addActionListener(e -> autoFill(10));
        quickPanel.add(fill10Btn);
        add(quickPanel);
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

    private void addFirst() {
        String type = (String) typeCombo.getSelectedItem();
        String text = valueField.getText().trim();
        Object value = text.isEmpty() ? VisualArrayDeque.generateRandomValue(type, random) : parseValue(text, type);
        deque.addFirst(value, type);
        updateStatus("Added first: " + value, SUCCESS_COLOR);
        valueField.setText("");
    }

    private void addLast() {
        String type = (String) typeCombo.getSelectedItem();
        String text = valueField.getText().trim();
        Object value = text.isEmpty() ? VisualArrayDeque.generateRandomValue(type, random) : parseValue(text, type);
        deque.addLast(value, type);
        updateStatus("Added last: " + value, SUCCESS_COLOR);
        valueField.setText("");
    }

    private void autoFill(int count) {
        Timer timer = new Timer(150, null);
        final int[] added = {0};
        timer.addActionListener(evt -> {
            if (added[0] < count) {
                Object value = VisualArrayDeque.generateRandomValue("int", random);
                if (random.nextBoolean()) {
                    deque.addFirst(value, "int");
                } else {
                    deque.addLast(value, "int");
                }
                added[0]++;
                updateStatus("Adding... " + added[0] + "/" + count, SUCCESS_COLOR);
            } else {
                timer.stop();
                updateStatus("Added " + count + " elements", SUCCESS_COLOR);
            }
        });
        timer.start();
    }

    private Object parseValue(String text, String type) {
        // Note: ArrayDeque does NOT allow null elements - throws NPE
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

        JLabel title = new JLabel("ARRAYDEQUE INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {"Resizable circular array", "O(1) add/remove both ends", "No capacity restrictions", "Faster than Stack/LinkedList"};
        for (String line : info) {
            JLabel label = new JLabel(line);
            label.setForeground(new Color(180, 150, 220));
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            panel.add(label);
        }
        return panel;
    }
}
