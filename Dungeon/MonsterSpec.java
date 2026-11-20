package Systems.Dungeon;

import java.util.Objects;

/**
 * Immutable monster recipe. A live monster is spawned from a spec when the
 * encounter starts, so spawning one spec twice gives two independent monsters.
 * Identity is the id, never the name.
 */
public record MonsterSpec(
        String id, String name, String title, Kind kind,
        int hp, int attack, int defense, int speed,
        int exp, int gold,
        String specialName,     // null unless the monster has a special move
        double phase2At) {      // 0 = no phase 2, else HP fraction (0..1]

    public enum Kind { NORMAL, FLOOR_BOSS, DUNGEON_BOSS }

    public MonsterSpec {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(kind, "kind");
        if (hp < 1) throw new IllegalArgumentException("hp must be >= 1: " + name);
        if (attack < 0 || defense < 0 || speed < 0 || exp < 0 || gold < 0)
            throw new IllegalArgumentException("negative stat or reward: " + name);
        if (phase2At < 0 || phase2At > 1)
            throw new IllegalArgumentException("phase2At must be 0..1: " + name);
    }

    public boolean isBoss()    { return kind != Kind.NORMAL; }
    public boolean hasPhases() { return phase2At > 0; }

    public String displayName() {
        return title == null || title.isBlank() ? name : name + ", " + title;
    }
}