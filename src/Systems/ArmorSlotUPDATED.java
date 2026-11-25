package Armors;

/** The places armor can be worn. Each base item in ArmorData names its slot ("head" / "body"). */
public enum ArmorSlot {
    HEAD("Head"),
    BODY("Body");

    private final String label;

    ArmorSlot(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Parses the slot string stored in ArmorData base entries, ignoring case and surrounding
     * spaces; fails loudly on a typo or a missing value.
     *
     * @throws IllegalArgumentException if the value is null or names no slot
     */
    public static ArmorSlot fromData(String value) {
        if (value != null) {
            String wanted = value.strip();
            for (ArmorSlot slot : values()) {
                if (slot.name().equalsIgnoreCase(wanted)) return slot;
            }
        }
        throw new IllegalArgumentException("Unknown armor slot in data: \"" + value + "\"");
    }
}
