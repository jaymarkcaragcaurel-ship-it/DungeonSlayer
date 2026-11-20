package Systems.Dungeon;

import java.util.List;
import java.util.Objects;

/** Immutable dungeon layout. Runs never change it. */
public record DungeonBlueprint(String id, String name, List<FloorBlueprint> floors) {

    public DungeonBlueprint {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        floors = List.copyOf(floors);
        if (floors.isEmpty()) throw new IllegalArgumentException("dungeon has no floors: " + name);
        for (int i = 0; i < floors.size(); i++) {
            FloorBlueprint f = floors.get(i);
            if (f.number() != i + 1)
                throw new IllegalArgumentException(name + ": floor numbers must run 1..n in order");
            if (f.last() != (i == floors.size() - 1))
                throw new IllegalArgumentException(name + ": only the final floor may be marked last");
        }
    }

    public int floorCount() { return floors.size(); }
}