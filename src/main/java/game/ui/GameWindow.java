package game.ui;

import game.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameWindow extends JFrame {
    private VisualArrayList arrayList;
    private VisualLinkedList linkedList;
    private VisualArray array;
    private VisualArrayDeque arrayDeque;
    private VisualHashSet hashSet;
    private VisualLinkedHashSet linkedHashSet;
    private VisualTreeSet treeSet;
    private VisualHashMap hashMap;
    private VisualLinkedHashMap linkedHashMap;
    private VisualTreeMap treeMap;
    private VisualHashtable hashtable;

    private JPanel mainContainer;
    private CardLayout cardLayout;
    private JPanel controlContainer;
    private CardLayout controlCardLayout;
    private List<JButton> tabButtons = new ArrayList<>();

    public GameWindow() {
        super("Java Collection Visualizer");

        this.arrayList = new VisualArrayList();
        this.linkedList = new VisualLinkedList();
        this.array = new VisualArray(20, "int", true);
        this.arrayDeque = new VisualArrayDeque();
        this.hashSet = new VisualHashSet();
        this.linkedHashSet = new VisualLinkedHashSet();
        this.treeSet = new VisualTreeSet();
        this.hashMap = new VisualHashMap();
        this.linkedHashMap = new VisualLinkedHashMap();
        this.treeMap = new VisualTreeMap();
        this.hashtable = new VisualHashtable();

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(new Color(10, 15, 25));

        mainContainer.add(wrapInScrollPane(new ArrayPanel(arrayList)), "ARRAYLIST");
        mainContainer.add(wrapInScrollPane(new LinkedListPanel(linkedList)), "LINKEDLIST");
        mainContainer.add(wrapInScrollPane(new ArrayDisplayPanel(array)), "ARRAY");
        mainContainer.add(wrapInScrollPane(new ArrayDequePanel(arrayDeque)), "ARRAYDEQUE");
        mainContainer.add(wrapInScrollPane(new HashSetPanel(hashSet)), "HASHSET");
        mainContainer.add(wrapInScrollPane(new LinkedHashSetPanel(linkedHashSet)), "LINKEDHASHSET");
        mainContainer.add(wrapInScrollPane(new TreeSetPanel(treeSet)), "TREESET");
        mainContainer.add(wrapInScrollPane(new HashMapPanel(hashMap)), "HASHMAP");
        mainContainer.add(wrapInScrollPane(new LinkedHashMapPanel(linkedHashMap)), "LINKEDHASHMAP");
        mainContainer.add(wrapInScrollPane(new TreeMapPanel(treeMap)), "TREEMAP");
        mainContainer.add(wrapInScrollPane(new HashtablePanel(hashtable)), "HASHTABLE");

        add(mainContainer, BorderLayout.CENTER);

        controlCardLayout = new CardLayout();
        controlContainer = new JPanel(controlCardLayout);

        controlContainer.add(new ControlPanel(arrayList), "ARRAYLIST");
        controlContainer.add(new LinkedListControlPanel(linkedList), "LINKEDLIST");
        controlContainer.add(new ArrayControlPanel(array), "ARRAY");
        controlContainer.add(new ArrayDequeControlPanel(arrayDeque), "ARRAYDEQUE");
        controlContainer.add(new HashSetControlPanel(hashSet), "HASHSET");
        controlContainer.add(new LinkedHashSetControlPanel(linkedHashSet), "LINKEDHASHSET");
        controlContainer.add(new TreeSetControlPanel(treeSet), "TREESET");
        controlContainer.add(new HashMapControlPanel(hashMap), "HASHMAP");
        controlContainer.add(new LinkedHashMapControlPanel(linkedHashMap), "LINKEDHASHMAP");
        controlContainer.add(new TreeMapControlPanel(treeMap), "TREEMAP");
        controlContainer.add(new HashtableControlPanel(hashtable), "HASHTABLE");

        // Wrap control panel in scroll pane
        JScrollPane controlScrollPane = new JScrollPane(controlContainer);
        controlScrollPane.setBorder(null);
        controlScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        controlScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        controlScrollPane.setPreferredSize(new Dimension(290, 0));
        add(controlScrollPane, BorderLayout.EAST);

        pack();
        setMinimumSize(new Dimension(1350, 700));
        setLocationRelativeTo(null);

        try {
            setIconImage(createIcon());
        } catch (Exception ignored) {}
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 25, 40), 0, getHeight(), new Color(15, 20, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(0, 200, 255, 100));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        header.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 10));
        header.setPreferredSize(new Dimension(0, 55));

        // List structures
        JButton arrayListBtn = createTabButton("ArrayList", new Color(0, 200, 255), true);
        JButton linkedListBtn = createTabButton("LinkedList", new Color(255, 100, 150), false);
        JButton arrayBtn = createTabButton("Array", new Color(100, 255, 150), false);

        // Deque
        JButton arrayDequeBtn = createTabButton("ArrayDeque", new Color(180, 100, 255), false);

        // Set structures
        JButton hashSetBtn = createTabButton("HashSet", new Color(255, 150, 80), false);
        JButton linkedHashSetBtn = createTabButton("LnkHashSet", new Color(255, 120, 120), false);
        JButton treeSetBtn = createTabButton("TreeSet", new Color(80, 200, 200), false);

        // Map structures
        JButton hashMapBtn = createTabButton("HashMap", new Color(255, 200, 80), false);
        JButton linkedHashMapBtn = createTabButton("LnkHashMap", new Color(255, 180, 130), false);
        JButton treeMapBtn = createTabButton("TreeMap", new Color(100, 220, 180), false);

        // Legacy (Dictionary)
        JButton hashtableBtn = createTabButton("Dictionary", new Color(200, 120, 80), false);

        tabButtons.add(arrayListBtn);
        tabButtons.add(linkedListBtn);
        tabButtons.add(arrayBtn);
        tabButtons.add(arrayDequeBtn);
        tabButtons.add(hashSetBtn);
        tabButtons.add(linkedHashSetBtn);
        tabButtons.add(treeSetBtn);
        tabButtons.add(hashMapBtn);
        tabButtons.add(linkedHashMapBtn);
        tabButtons.add(treeMapBtn);
        tabButtons.add(hashtableBtn);

        arrayListBtn.addActionListener(e -> switchTo("ARRAYLIST", arrayListBtn));
        linkedListBtn.addActionListener(e -> switchTo("LINKEDLIST", linkedListBtn));
        arrayBtn.addActionListener(e -> switchTo("ARRAY", arrayBtn));
        arrayDequeBtn.addActionListener(e -> switchTo("ARRAYDEQUE", arrayDequeBtn));
        hashSetBtn.addActionListener(e -> switchTo("HASHSET", hashSetBtn));
        linkedHashSetBtn.addActionListener(e -> switchTo("LINKEDHASHSET", linkedHashSetBtn));
        treeSetBtn.addActionListener(e -> switchTo("TREESET", treeSetBtn));
        hashMapBtn.addActionListener(e -> switchTo("HASHMAP", hashMapBtn));
        linkedHashMapBtn.addActionListener(e -> switchTo("LINKEDHASHMAP", linkedHashMapBtn));
        treeMapBtn.addActionListener(e -> switchTo("TREEMAP", treeMapBtn));
        hashtableBtn.addActionListener(e -> switchTo("HASHTABLE", hashtableBtn));

        // Add separator labels
        JLabel listLabel = createCategoryLabel("LIST:");
        JLabel dequeLabel = createCategoryLabel("DEQUE:");
        JLabel setLabel = createCategoryLabel("SET:");
        JLabel mapLabel = createCategoryLabel("MAP:");
        JLabel legacyLabel = createCategoryLabel("LEGACY:");

        header.add(listLabel);
        header.add(arrayListBtn);
        header.add(linkedListBtn);
        header.add(arrayBtn);
        header.add(Box.createHorizontalStrut(6));
        header.add(dequeLabel);
        header.add(arrayDequeBtn);
        header.add(Box.createHorizontalStrut(6));
        header.add(setLabel);
        header.add(hashSetBtn);
        header.add(linkedHashSetBtn);
        header.add(treeSetBtn);
        header.add(Box.createHorizontalStrut(6));
        header.add(mapLabel);
        header.add(hashMapBtn);
        header.add(linkedHashMapBtn);
        header.add(treeMapBtn);
        header.add(Box.createHorizontalStrut(6));
        header.add(legacyLabel);
        header.add(hashtableBtn);

        return header;
    }

    private JLabel createCategoryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(180, 200, 230));
        label.setFont(new Font("Consolas", Font.BOLD, 12));
        return label;
    }

    private JScrollPane wrapInScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.setBackground(new Color(10, 15, 25));
        scrollPane.getViewport().setBackground(new Color(10, 15, 25));
        return scrollPane;
    }

    private void switchTo(String name, JButton activeBtn) {
        cardLayout.show(mainContainer, name);
        controlCardLayout.show(controlContainer, name);
        setActiveButton(activeBtn);
    }

    private void setActiveButton(JButton active) {
        for (JButton btn : tabButtons) {
            btn.putClientProperty("active", btn == active);
            btn.repaint();
        }
    }

    private JButton createTabButton(String text, Color accentColor, boolean active) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = Boolean.TRUE.equals(getClientProperty("active"));

                if (isActive) {
                    g2d.setColor(accentColor.darker());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                g2d.setColor(isActive ? accentColor : accentColor.darker());
                g2d.setStroke(new BasicStroke(isActive ? 2 : 1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2d.setColor(isActive ? Color.WHITE : new Color(200, 220, 255));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        button.setFont(new Font("Consolas", Font.BOLD, 11));
        button.setPreferredSize(new Dimension(88, 28));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("active", active);

        return button;
    }

    private Image createIcon() {
        int size = 32;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(10, 15, 25));
        g2d.fillRect(0, 0, size, size);

        g2d.setColor(new Color(0, 200, 255));
        for (int i = 0; i < 3; i++) {
            int x = 4 + i * 9;
            int y = 10;
            g2d.drawRoundRect(x, y, 8, 12, 2, 2);
        }

        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(14, 26, 14, 30);
        g2d.drawLine(12, 28, 16, 28);

        g2d.dispose();
        return img;
    }
}
