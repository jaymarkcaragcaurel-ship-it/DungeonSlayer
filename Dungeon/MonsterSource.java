package Systems.Dungeon;

import java.util.Random;

/** Where the dungeon generator gets monster specs (the M1 side). All randomness comes from the Random passed in. */
public interface MonsterSource {
    MonsterSpec regular(Random rng);
    MonsterSpec floorBoss(Random rng);
    /** @param floors number of floors in the dungeon (drives boss scaling) */
    MonsterSpec dungeonBoss(Random rng, int floors);
}