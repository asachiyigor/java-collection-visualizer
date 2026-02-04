# Java Collection Visualizer

An interactive educational tool for visualizing **Java Collections Framework** data structures with real-time memory analysis and sci-fi inspired UI.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk)
![Swing](https://img.shields.io/badge/GUI-Swing-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

## Features

- **11 Data Structures** - Lists, Sets, Maps, Deque, and legacy collections
- **Real-time Visualization** - See how elements are stored internally
- **Memory Analysis** - Byte-level breakdown of memory usage
- **Animated Operations** - Smooth transitions for add/remove
- **Type Support** - int, double, String, char, boolean, null

## Supported Data Structures

| Category | Structures | Description |
|----------|------------|-------------|
| **List** | `ArrayList`, `LinkedList`, `Array` | Ordered collections with index access |
| **Deque** | `ArrayDeque` | Double-ended queue operations |
| **Set** | `HashSet`, `LinkedHashSet`, `TreeSet` | Unique elements, various orderings |
| **Map** | `HashMap`, `LinkedHashMap`, `TreeMap` | Key-value pairs with different orderings |
| **Legacy** | `Hashtable` | Thread-safe dictionary |

## Quick Start

### Windows
```batch
run.bat
```

### Manual
```bash
# Compile
javac -d out -sourcepath src/main/java src/main/java/game/Main.java src/main/java/game/model/*.java src/main/java/game/ui/*.java

# Run
java -cp out game.Main
```

## Requirements

- Java 17+ (JDK)
- No external dependencies

## Screenshots

### ArrayList Visualization
```
+-------+-------+-------+-------+-------+-------+
|  42   | 3.14  | "Hi"  | true  |  'A'  | null  |
|  int  |double |String | bool  | char  | null  |
+-------+-------+-------+-------+-------+-------+
[###############............] SIZE: 6 / CAPACITY: 10
```

### LinkedList Visualization
```
[HEAD]-->[42|int]<==>[3.14|dbl]<==>[Hi|str]-->[TAIL]
```

### HashSet Visualization
```
Bucket[0]: empty
Bucket[1]: [42] -> [99]    (collision chain)
Bucket[2]: ["Hello"]
```

### TreeSet Visualization
```
         [42]
        /    \
     [15]    [67]
     /  \      \
   [8]  [23]  [89]
```

## Memory Comparison

| Structure | 10 Integers | Overhead |
|-----------|-------------|----------|
| `int[]` | 56 B | Minimal - no wrappers |
| `ArrayList<Integer>` | 200 B | Array + Integer wrappers |
| `LinkedList<Integer>` | 432 B | Node objects (24B each) |
| `HashSet<Integer>` | 480 B | Hash table + entries |
| `TreeSet<Integer>` | 400 B | Tree nodes (40B each) |

## What You'll Learn

1. **Internal Structure** - How each collection stores data
2. **Time Complexity** - Why operations have different costs
3. **Memory Overhead** - Object headers, wrappers, node pointers
4. **Null Handling** - Which collections allow null and why
5. **Trade-offs** - When to use each data structure

## Color Coding

| Type | Color |
|------|-------|
| `int` | Cyan |
| `double` | Pink |
| `String` | Green |
| `boolean` | Yellow |
| `char` | Purple |
| `Object` | Orange |
| `null` | Gray |

## Project Structure

```
java-collection-visualizer/
├── src/main/java/game/
│   ├── Main.java
│   ├── model/           # Data structure implementations
│   │   ├── Visual*.java # 11 data structure models
│   │   ├── VisualElement.java
│   │   ├── VisualNode.java
│   │   └── ...
│   └── ui/              # User interface
│       ├── GameWindow.java
│       ├── *Panel.java       # Visualization panels
│       ├── *ControlPanel.java
│       └── *MemoryDialog.java
├── build.bat
├── run.bat
└── README.md
```

## Contributing

Contributions welcome! Some ideas:

- Add `PriorityQueue`, `ConcurrentHashMap`
- Operation animations (sorting, searching)
- Performance benchmarking mode
- Dark/light theme toggle

## License

MIT License - free to use, modify, and distribute.

---

*Visual style inspired by sci-fi interfaces and retro-futuristic design.*
