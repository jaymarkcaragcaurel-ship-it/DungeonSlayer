package Data;

/**
 * Stat gained per level, one row per class in the same order as {@link ClassesData#TABLE}
 * (Warrior, Mage, Paladin, Archer, Assassin).
 *
 * Column order matches the class table's stats: HP, MP, SPEED, ATTACK, DEFENSE, CRIT_CHANCE,
 * CRIT_MULT. Fractions are fine; LevelingSystem rounds to 0.01 after each gain.
 *
 * Reaching level 30 is 29 level-ups, so a row's value x 29 is the total gained over a whole
 * run. Tune freely; LevelingSystem checks on startup that every class has a full row.
 */
public class ClassGrowth {

    public static final int HP          = 0;
    public static final int MP          = 1;
    public static final int SPEED       = 2;
    public static final int ATTACK      = 3;
    public static final int DEFENSE     = 4;
    public static final int CRIT_CHANCE = 5;
    public static final int CRIT_MULT   = 6;

    public static final int STAT_COUNT = 7;

    public static final double[][] PER_LEVEL = {
        //   HP    MP   SPD   ATK   DEF   CRIT  CMULT
        {  18.0,  2.0,  0.2,  2.0,  1.0,  0.1,  0.00 },   // Warrior
        {   7.0, 14.0,  0.2,  3.5,  0.5,  0.3,  0.02 },   // Mage
        {  16.0,  6.0,  0.1,  1.8,  1.0,  0.1,  0.01 },   // Paladin
        {   9.0,  4.0,  0.4,  2.5,  0.5,  0.4,  0.02 },   // Archer
        {   7.0,  5.0,  0.5,  2.8,  0.4,  0.2,  0.03 },   // Assassin
    };
}