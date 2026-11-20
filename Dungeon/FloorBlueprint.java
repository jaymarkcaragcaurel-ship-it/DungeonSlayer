package Systems.Dungeon;

import java.util.List;
import java.util.Objects;

/** One floor: regular monsters in order, then the boss. Immutable. */
public record FloorBlueprint(int number, List<MonsterSpec> monsters, MonsterSpec boss, boolean last) {

    public FloorBlueprint {
        if (number < 1) throw new IllegalArgumentException("floor number must be >= 1");
        Objects.requireNonNull(boss, "boss");
        monsters = List.copyOf(monsters);
        if (monsters.isEmpty()) throw new IllegalArgumentException("floor " + number + " has no regular monsters");
        for (MonsterSpec m : monsters)
            if (m.kind() != MonsterSpec.Kind.NORMAL)
                throw new IllegalArgumentException("floor " + number + ": non-regular monster in regular list");
        MonsterSpec.Kind expected = last ? MonsterSpec.Kind.DUNGEON_BOSS : MonsterSpec.Kind.FLOOR_BOSS;
        if (boss.kind() != expected)
            throw new IllegalArgumentException("floor " + number + ": boss must be " + expected);
    }

    /** Regular monsters plus the boss. */
    public int encounterCount() { return monsters.size() + 1; }
}