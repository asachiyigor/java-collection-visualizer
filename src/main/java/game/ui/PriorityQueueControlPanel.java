package game.ui;

import game.model.VisualPriorityQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class PriorityQueueControlPanel extends JPanel {
    private VisualPriorityQueue queue;
    private Random random = new Random();
    private JTextField valueField;
    private JComboBox<String> typeCombo;
    private JLabel statusLabel;

    private static final Color BG_COLOR = new Color(25, 12, 15);
    private static final Color PANEL_BG = new Color(50, 25, 30);
    private static final Color ACCENT = new Color(255, 100, 100);
    private static final Color TEXT_COLOR = new Color(255, 220, 220);
    private static final Color BUTTON_BG = new Color(55, 30, 35);
    private static final Color SUCCESS_COLOR = new Color(150, 255, 150);
    private static final Color WARN_COLOR = new Color(255, 200, 100);
    private static final Color ERROR_COLOR = new Color(255, 100, 100);

    public PriorityQueueControlPanel(VisualPriorityQueue queue) {
        this.queue = queue;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(280, 600));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initComponents();
    }

    private void initComponents() {
        add(createTitle("ADD ELEMENT"));
        add(Box.createVerticalStrut(10));

        add(createLabel("TYPE:"));
        typeCombo = new JComboBox<>(new String[]{"int", "double", "String", "char"});
        styleComboBox(typeCombo);
        add(typeCombo);
        add(Box.createVerticalStrut(8));

        add(createLabel("VALUE (optional):"));
        valueField = new JTextField();
        styleTextField(valueField);
        add(valueField);
        add(Box.createVerticalStrut(10));

        JButton offerBtn = createStyledButton("offer(element)", ACCENT);
        offerBtn.addActionListener(e -> offerElement());
        add(offerBtn);
        add(Box.createVerticalStrut(10));

        // Poll and Peek
        JPanel pollPeekPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        pollPeekPanel.setBackground(BG_COLOR);
        pollPeekPanel.setMaximumSize(new Dimension(230, 28));
        pollPeekPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton pollBtn = createSmallButton("poll()", new Color(255, 150, 100));
        pollBtn.addActionListener(e -> {
            Object val = queue.poll();
            updateStatus(val != null ? "Polled: " + val : "Empty queue", val != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        pollPeekPanel.add(pollBtn);

        JButton peekBtn = createSmallButton("peek()", new Color(150, 200, 255));
        peekBtn.addActionListener(e -> {
            Object val = queue.peek();
            updateStatus("Peek: " + (val != null ? val : "null"), val != null ? SUCCESS_COLOR : WARN_COLOR);
        });
        pollPeekPanel.add(peekBtn);
        add(pollPeekPanel);
        add(Box.createVerticalStrut(10));

        // Contains and Size
        JPanel methodPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        methodPanel.setBackground(BG_COLOR);
        methodPanel.setMaximumSize(new Dimension(230, 28));
        methodPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton containsBtn = createSmallButton("contains()", new Color(200, 200, 150));
        containsBtn.addActionListener(e -> {
            String val = valueField.getText().trim();
            if (!val.isEmpty()) {
                String type = (String) typeCombo.getSelectedItem();
                try {
                    Object value = parseValue(val, type);
                    boolean found = queue.contains(value);
                    updateStatus("contains = " + found, found ? SUCCESS_COLOR : WARN_COLOR);
                } catch (Exception ex) {
                    updateStatus("Invalid value", ERROR_COLOR);
                }
            } else {
                updateStatus("Enter value first", WARN_COLOR);
            }
        });
        methodPanel.add(containsBtn);

        JButton sizeBtn = createSmallButton("size()", new Color(150, 200, 255));
        sizeBtn.addActionListener(e -> updateStatus("size() = " + queue.getSize(), TEXT_COLOR));
        methodPanel.add(sizeBtn);
        add(methodPanel);
        add(Box.createVerticalStrut(12));

        add(createTitle("QUICK ADD"));
        add(Box.createVerticalStrut(8));

        JPanel quickPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        quickPanel.setBackground(BG_COLOR);
        quickPanel.setMaximumSize(new Dimension(230, 56));
        quickPanel.setAlignmentX(LEFT_ALIGNMENT);

        String[] types = {"int", "double", "String", "char"};
        Color[] colors = {
                new Color(0, 200, 255), new Color(255, 100, 200),
                new Color(100, 255, 150), new Color(200, 100, 255)
        };

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            JButton btn = createSmallButton(type, colors[i]);
            btn.addActionListener(e -> {
                Object value = VisualPriorityQueue.generateRandomValue(type, random);
                queue.offer(value, type);
                updateStatus("Offered: " + value, SUCCESS_COLOR);
            });
            quickPanel.add(btn);
        }
        add(quickPanel);
        add(Box.createVerticalStrut(12));

        add(createTitle("OPERATIONS"));
        add(Box.createVerticalStrut(8));

        JButton clearBtn = createStyledButton("clear()", new Color(200, 80, 80));
        clearBtn.addActionListener(e -> {
            queue.clear();
            updateStatus("Cleared all elements", WARN_COLOR);
        });
        add(clearBtn);
        add(Box.createVerticalStrut(12));

        add(createTitle("AUTO MODE"));
        add(Box.createVerticalStrut(8));

        JPanel autoPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        autoPanel.setBackground(BG_COLOR);
        autoPanel.setMaximumSize(new Dimension(230, 30));
        autoPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton fill10Btn = createSmallButton("Fill x10", new Color(200, 100, 100));
        fill10Btn.addActionListener(e -> autoFill(10));
        autoPanel.add(fill10Btn);

        JButton fill20Btn = createSmallButton("Fill x20", new Color(220, 120, 120));
        fill20Btn.addActionListener(e -> autoFill(20));
        autoPanel.add(fill20Btn);
        add(autoPanel);
        add(Box.createVerticalStrut(10));

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
                Object value = VisualPriorityQueue.generateRandomValue("int", random);
                queue.offer(value, "int");
                added[0]++;
                updateStatus("Adding... " + added[0] + "/" + count, SUCCESS_COLOR);
            } else {
                timer.stop();
                updateStatus("Added " + count + " elements", SUCCESS_COLOR);
            }
        });
        timer.start();
    }

    private void offerElement() {
        String type = (String) typeCombo.getSelectedItem();
        String valueText = valueField.getText().trim();
        Object value;

        if (valueText.isEmpty()) {
            value = VisualPriorityQueue.generateRandomValue(type, random);
        } else {
            try {
                value = parseValue(valueText, type);
            } catch (Exception e) {
                updateStatus("Invalid value!", ERROR_COLOR);
                return;
            }
        }

        queue.offer(value, type);
        updateStatus("Offered: " + value + " (size=" + queue.getSize() + ")", SUCCESS_COLOR);
        valueField.setText("");
    }

    private Object parseValue(String text, String type) {
        if (text.equalsIgnoreCase("null")) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "int" -> Integer.parseInt(text);
            case "double" -> Double.parseDouble(text);
            case "char" -> text.isEmpty() ? '\0' : text.charAt(0);
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
        panel.setMaximumSize(new Dimension(230, 130));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("PRIORITYQUEUE INFO");
        title.setForeground(ACCENT);
        title.setFont(new Font("Consolas", Font.BOLD, 12));
        panel.add(title);

        String[] info = {
                "Binary min-heap",
                "O(log n) offer/poll",
                "O(1) peek",
                "Array-backed: Object[]",
                "Default capacity: 11",
                "Not synchronized"
        };

        for (String line : info) {
            JLabel label = new JLabel(line);
            label.setForeground(line.contains("O(1)") ? SUCCESS_COLOR : new Color(200, 150, 150));
            label.setFont(new Font("Consolas", Font.PLAIN, 11));
            panel.add(label);
        }

        return panel;
    }
}
