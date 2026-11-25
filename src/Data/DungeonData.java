package Data;

/** Raw dungeon tables and generation limits. Constants only, no logic. */
public final class DungeonData {

    public static final int DUNGEON_COUNT = 3;
    public static final int MIN_FLOORS = 2;
    public static final int MAX_FLOORS = 5;
    public static final int MIN_MONSTERS_PER_FLOOR = 1;
    public static final int MAX_MONSTERS_PER_FLOOR = 5;

    /** 20 dungeon names; a game picks DUNGEON_COUNT of them without repeats. */
    public static final String[] NAMES = {
        "Caverns of the Damned", "Ashveil Catacombs", "The Rotting Sanctum",
        "Thornspire Depths", "Tomb of the Forgotten King", "Shadowmere Abyss",
        "The Blighted Warrens", "Crypts of Zalthorath", "The Sunken Fortress",
        "Malachar's Domain", "Voidrift Dungeon", "The Festering Pit",
        "Dreadspire Keep", "Ruins of Nyxevara", "The Obsidian Labyrinth",
        "Gravehold Depths", "The Shattered Vault", "Bonechill Cavern",
        "Temple of the Cursed Flame", "The Abyssal Maw"
    };

    private DungeonData() {}
}