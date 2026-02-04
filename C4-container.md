# C4 Container Diagram: Java Collection Visualizer

## Обзор системы
Desktop GUI приложение для визуализации работы Java коллекций в sci-fi стиле.

## Диаграмма контейнеров

```mermaid
C4Context
    title C4 Container Diagram - Java Collection Visualizer

    Person(user, "Пользователь", "Изучает Java коллекции")

    System_Boundary(app, "Java Collection Visualizer") {
        Container(main_window, "Game Window", "Java Swing JFrame", "Главное окно с TabLayout навигацией")

        Container(list_panels, "List Visualizers", "Swing JPanels", "ArrayPanel, LinkedListPanel, ArrayDisplayPanel")
        Container(set_panels, "Set Visualizers", "Swing JPanels", "HashSetPanel, LinkedHashSetPanel, TreeSetPanel")
        Container(map_panels, "Map Visualizers", "Swing JPanels", "HashMapPanel, LinkedHashMapPanel, TreeMapPanel")
        Container(deque_panel, "Deque Visualizer", "Swing JPanel", "ArrayDequePanel")
        Container(legacy_panel, "Legacy Visualizer", "Swing JPanel", "HashtablePanel")

        Container(models, "Visual Models", "Java Classes", "VisualArrayList, VisualLinkedList, VisualHashMap, etc.")
        Container(animation, "Animation Engine", "Swing Timer", "60fps рендеринг, tweening анимации")
        Container(dialogs, "Memory Dialogs", "Swing JDialog", "Детальная информация о памяти")
    }

    Rel(user, main_window, "Взаимодействует с UI")
    Rel(main_window, list_panels, "CardLayout переключение")
    Rel(main_window, set_panels, "CardLayout переключение")
    Rel(main_window, map_panels, "CardLayout переключение")
    Rel(main_window, deque_panel, "CardLayout переключение")
    Rel(main_window, legacy_panel, "CardLayout переключение")
    Rel(list_panels, models, "Использует для визуализации")
    Rel(set_panels, models, "Использует для визуализации")
    Rel(map_panels, models, "Использует для визуализации")
    Rel(animation, list_panels, "Timer callbacks")
    Rel(list_panels, dialogs, "Показывает информацию")
```

## Основные контейнеры

| Контейнер | Технология | Ответственность |
|-----------|------------|-----------------|
| Game Window | Swing JFrame | Главное окно, навигация |
| List Visualizers | Swing JPanels | ArrayList, LinkedList, Array |
| Set Visualizers | Swing JPanels | HashSet, LinkedHashSet, TreeSet |
| Map Visualizers | Swing JPanels | HashMap, LinkedHashMap, TreeMap |
| Deque Visualizer | Swing JPanel | ArrayDeque |
| Legacy Visualizer | Swing JPanel | Hashtable (Dictionary) |
| Visual Models | Java Classes | Симуляция структур данных |
| Animation Engine | Swing Timer | 60fps анимация элементов |
| Memory Dialogs | Swing JDialog | Информация о памяти |

## Визуализируемые структуры

| Тип | Структуры |
|-----|-----------|
| List | ArrayList, LinkedList, Array |
| Set | HashSet, LinkedHashSet, TreeSet |
| Map | HashMap, LinkedHashMap, TreeMap |
| Queue | ArrayDeque |
| Legacy | Hashtable |

## Особенности архитектуры

- **Standalone приложение** - никаких внешних зависимостей
- **MVC паттерн** - Model (Visual*), View (*Panel), Controller (*ControlPanel)
- **Анимация** - Timer-based с delta time для плавности
- **Type coloring** - int→Cyan, String→Green, double→Pink

## Запуск

```bash
javac -d out -sourcepath src/main/java src/main/java/game/**/*.java
java -cp out game.Main
```
