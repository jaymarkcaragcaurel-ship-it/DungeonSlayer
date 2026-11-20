package Systems.Dungeon;

import Data.DungeonData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Dungeon generation and run creation. Silent: no printing, no input. */
public final class DungeonSystem {

    private DungeonSystem() {}

    /** Generate the game's dungeons (call once at game start): DUNGEON_COUNT, unique names. */
    public static List<DungeonBlueprint> generate(Random rng, MonsterSource monsters) {
        return generate(DungeonData.DUNGEON_COUNT, rng, monsters);
    }

    public static List<DungeonBlueprint> generate(int count, Random rng, MonsterSource monsters) {
        Objects.requireNonNull(rng, "rng");
        Objects.requireNonNull(monsters, "monsters");
        if (count < 1 || count > DungeonData.NAMES.length)
            throw new IllegalArgumentException("count must be 1.." + DungeonData.NAMES.length);

        List<String> names = new ArrayList<>(List.of(DungeonData.NAMES));
        Collections.shuffle(names, rng);

        List<DungeonBlueprint> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(build("dng-" + (i + 1), names.get(i), rng, monsters));
        }
        return List.copyOf(result);
    }

    /** Start a fresh run. Never touches the blueprint, so re-entering always gives full-health monsters. */
    public static DungeonRun start(DungeonBlueprint blueprint, DungeonListener listener) {
        return new DungeonRun(blueprint, listener);
    }

    public static DungeonRun start(DungeonBlueprint blueprint) {
        return new DungeonRun(blueprint, DungeonListener.NONE);
    }

    // ---- internals ------------------------------------------------------

    private static DungeonBlueprint build(String id, String name, Random rng, MonsterSource monsters) {
        int floorCount = between(rng, DungeonData.MIN_FLOORS, DungeonData.MAX_FLOORS);
        List<FloorBlueprint> floors = new ArrayList<>();
        for (int n = 1; n <= floorCount; n++) {
            boolean last = n == floorCount;
            int regulars = between(rng, DungeonData.MIN_MONSTERS_PER_FLOOR, DungeonData.MAX_MONSTERS_PER_FLOOR);
            List<MonsterSpec> list = new ArrayList<>();
            for (int i = 0; i < regulars; i++) list.add(monsters.regular(rng));
            MonsterSpec boss = last ? monsters.dungeonBoss(rng, floorCount) : monsters.floorBoss(rng);
            floors.add(new FloorBlueprint(n, list, boss, last));
        }
        return new DungeonBlueprint(id, name, floors);
    }

    private static int between(Random rng, int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }
}