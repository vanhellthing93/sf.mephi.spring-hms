package sf.mephi.common.constants;

/**
 * Типы номеров в отеле
 */
public enum RoomType {
    SINGLE("Одноместный", 1),
    DOUBLE("Двухместный", 2),
    TWIN("Двухместный с раздельными кроватями", 2),
    SUITE("Люкс", 2),
    DELUXE("Делюкс", 2),
    FAMILY("Семейный", 4),
    PRESIDENTIAL("Президентский", 4);

    private final String displayName;
    private final int capacity;

    RoomType(String displayName, int capacity) {
        this.displayName = displayName;
        this.capacity = capacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }
}
