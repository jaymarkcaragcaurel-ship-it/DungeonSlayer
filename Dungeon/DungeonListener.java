package Systems.Dungeon;

/** Upward notifications (Analytics etc.). Both methods are optional to implement. */
public interface DungeonListener {
    DungeonListener NONE = new DungeonListener() {};

    /** Published only when the floor boss was WON; a fled boss floor is passable but not "cleared". */
    default void floorCleared(String dungeonName, int floorNumber) {}

    /** Published exactly once per run, for every ending. */
    default void dungeonEnded(String dungeonName, DungeonRun.Outcome outcome) {}
}