package game.model;

public enum DataStructureType {
    ARRAY("Array", "Fixed-size, direct access O(1)"),
    ARRAYLIST("ArrayList", "Dynamic size, amortized O(1) add"),
    LINKEDLIST("LinkedList", "O(1) add/remove at ends, O(n) access"),
    HASHSET("HashSet", "Hash table, O(1) add/remove/contains"),
    LINKEDHASHSET("LinkedHashSet", "Hash table + insertion order"),
    TREESET("TreeSet", "Red-Black tree, O(log n) sorted"),
    HASHMAP("HashMap", "Hash table, O(1) key-value operations"),
    LINKEDHASHMAP("LinkedHashMap", "Hash table + insertion order"),
    TREEMAP("TreeMap", "Red-Black tree, O(log n) sorted by key");

    private final String displayName;
    private final String description;

    DataStructureType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
