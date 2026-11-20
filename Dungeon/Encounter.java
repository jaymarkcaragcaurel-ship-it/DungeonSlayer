package Systems.Dungeon;

import java.util.Objects;

/**
 * One fight offered to the player. The app spawns a fresh live monster from
 * {@link #spec()} each time, so a monster is never reused between visits.
 *
 * @param position 1-based place on the floor (boss is last)
 * @param total    encounters on the floor, boss included
 */
public record Encounter(MonsterSpec spec, int position, int total) {

    public Encounter {
        Objects.requireNonNull(spec, "spec");
        if (position < 1 || position > total) throw new IllegalArgumentException("bad position " + position + "/" + total);
    }

    public boolean isFloorBoss()   { return spec.kind() == MonsterSpec.Kind.FLOOR_BOSS; }
    public boolean isDungeonBoss() { return spec.kind() == MonsterSpec.Kind.DUNGEON_BOSS; }
    public boolean isBoss()        { return spec.isBoss(); }
    /** Fleeing is allowed against everything except the dungeon boss. */
    public boolean canFlee()       { return !isDungeonBoss(); }
}