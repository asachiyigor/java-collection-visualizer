package game.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VisualBucket {
    private int index;
    private List<VisualElement> chain = new ArrayList<>();
    private List<VisualEntry> entries = new ArrayList<>();
    private double x, y;
    private double targetX, targetY;
    private double alpha = 1.0;
    private boolean highlighted = false;
    private long highlightTime = 0;

    public VisualBucket(int index) {
        this.index = index;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
    }

    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void update(double deltaTime) {
        double speed = 8.0;
        x += (targetX - x) * speed * deltaTime;
        y += (targetY - y) * speed * deltaTime;

        if (highlighted && System.currentTimeMillis() - highlightTime > 500) {
            highlighted = false;
        }

        for (VisualElement elem : chain) {
            elem.update(deltaTime);
        }
        chain.removeIf(VisualElement::isFullyRemoved);

        for (VisualEntry entry : entries) {
            entry.update(deltaTime);
        }
        entries.removeIf(VisualEntry::isFullyRemoved);
    }

    public void addElement(VisualElement element) {
        int chainIndex = chain.size();
        element.setPosition(x, y - 50);
        chain.add(element);
        updateChainPositions();
    }

    public boolean removeElement(Object value) {
        for (VisualElement elem : chain) {
            if (!elem.isRemoving() && elem.getValue().equals(value)) {
                elem.startRemoving();
                return true;
            }
        }
        return false;
    }

    public boolean contains(Object value) {
        for (VisualElement elem : chain) {
            if (!elem.isRemoving() && elem.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void updateChainPositions() {
        double chainY = y + 60;
        int visibleIndex = 0;
        for (VisualElement elem : chain) {
            if (!elem.isRemoving()) {
                elem.setTarget(x, chainY + visibleIndex * 55);
                visibleIndex++;
            }
        }
    }

    public void highlight() {
        highlighted = true;
        highlightTime = System.currentTimeMillis();
    }

    public void clear() {
        for (VisualElement elem : chain) {
            elem.startRemoving();
        }
        for (VisualEntry entry : entries) {
            entry.startRemoving();
        }
    }

    // Entry-based methods for Map structures
    public void addEntry(VisualEntry entry) {
        entry.setBucketIndex(index);
        entry.setChainIndex(entries.size());
        entry.setPosition(x, y - 50);
        entries.add(entry);
        updateEntryPositions();
    }

    public void removeEntry(VisualEntry entry) {
        entries.remove(entry);
        updateEntryPositions();
    }

    public List<VisualEntry> getEntries() {
        return entries;
    }

    public void updateEntryPositions() {
        double entryY = y + 60;
        int visibleIndex = 0;
        for (VisualEntry entry : entries) {
            if (!entry.isRemoving()) {
                entry.setTarget(x, entryY + visibleIndex * 55);
                visibleIndex++;
            }
        }
    }

    public void setHighlighted(boolean h) {
        this.highlighted = h;
        if (h) highlightTime = System.currentTimeMillis();
    }

    public int getIndex() { return index; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isHighlighted() { return highlighted; }
    public List<VisualElement> getChain() { return chain; }
    public int getChainSize() {
        int count = 0;
        for (VisualElement elem : chain) {
            if (!elem.isRemoving()) count++;
        }
        return count;
    }
    public boolean isEmpty() { return getChainSize() == 0; }
}
