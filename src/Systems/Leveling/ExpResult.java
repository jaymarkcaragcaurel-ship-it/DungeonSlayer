package Systems.Leveling;

import java.util.List;

/**
 * What happened when EXP was awarded.
 *
 * @param gained     EXP actually added (less than requested if the level cap cut it off, 0 at the cap)
 * @param totalExp   the player's total EXP afterwards
 * @param level      the player's level afterwards
 * @param levelUps   every level gained, in order (empty if none)
 * @param atMaxLevel true if the player is at the level cap afterwards
 */
public record ExpResult(int gained, int totalExp, int level, List<LevelUp> levelUps, boolean atMaxLevel) {

    public ExpResult {
        levelUps = List.copyOf(levelUps);
    }

    public boolean leveledUp() { return !levelUps.isEmpty(); }
}