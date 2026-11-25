package Systems.Core;

import Data.ClassDef;
import Data.ClassesData;
import Systems.Player.Player;
import Systems.Player.Stat;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Read access to the character-class table plus the one-shot "apply this class to a new player" step.
 * Pure logic: no console I/O (the class menu lives in UI.ClassSelectionMenu).
 * The table is checked once when this class loads, so a bad table fails at startup.
 */
public final class ClassSystem {

    private ClassSystem() {}

    public static final int NORMALIZED_STAT_COUNT = 7;

    private static final List<ClassDef> CLASSES = ClassesData.CLASSES;

    // The seven normalized stats, in this order.
    private static final Stat[] STAT_COLUMNS = {
        Stat.HP,
        Stat.MP,
        Stat.SPEED,
        Stat.ATTACK,
        Stat.DEFENSE,
        Stat.CRIT_CHANCE,
        Stat.CRIT_MULTIPLIER
    };

    /** Best value of each normalized stat across all classes. Computed once, never changes. */
    private static final double[] STAT_MAXES;

    static {
        if (STAT_COLUMNS.length != NORMALIZED_STAT_COUNT) {
            throw new IllegalStateException(
                "NORMALIZED_STAT_COUNT (" + NORMALIZED_STAT_COUNT
                + ") does not match STAT_COLUMNS (" + STAT_COLUMNS.length + ")");
        }
        if (CLASSES.isEmpty()) {
            throw new IllegalStateException("ClassesData.CLASSES is empty");
        }
        Set<String> seen = new HashSet<>();
        for (ClassDef def : CLASSES) {
            if (!seen.add(def.name().trim().toLowerCase(Locale.ROOT))) {
                throw new IllegalStateException("Duplicate class name: " + def.name());
            }
        }
        double[] maxes = new double[NORMALIZED_STAT_COUNT];
        for (ClassDef def : CLASSES) {
            for (int s = 0; s < NORMALIZED_STAT_COUNT; s++) {
                maxes[s] = Math.max(maxes[s], column(def, STAT_COLUMNS[s]));
            }
        }
        STAT_MAXES = maxes;
    }

    public static int count() {
        return CLASSES.size();
    }

    public static boolean isValidIndex(int classIndex) {
        return classIndex >= 0 && classIndex < count();
    }

    public static String name(int classIndex) {
        return get(classIndex).name();
    }

    public static String description(int classIndex) {
        return get(classIndex).description();
    }

    /**
     * One numeric column of a class, chosen with a ClassesData selector (HP, MP, SPEED, ATTACK, DEFENSE,
     * CRIT_CHANCE, CRIT_MULT, EXP, LEVEL, GOLD). Any other value is an IllegalArgumentException.
     */
    public static double stat(int classIndex, int statField) {
        ClassDef def = get(classIndex);
        switch (statField) {
            case ClassesData.HP:          return def.hp();
            case ClassesData.MP:          return def.mp();
            case ClassesData.SPEED:       return def.speed();
            case ClassesData.ATTACK:      return def.attack();
            case ClassesData.DEFENSE:     return def.defense();
            case ClassesData.CRIT_CHANCE: return def.critChance();
            case ClassesData.CRIT_MULT:   return def.critMultiplier();
            case ClassesData.EXP:         return def.startExp();
            case ClassesData.LEVEL:       return def.startLevel();
            case ClassesData.GOLD:        return def.startingGold();
            default:
                throw new IllegalArgumentException("Not a numeric class column: " + statField);
        }
    }

    /** The class value of a player stat (HP, MP, SPEED, ATTACK, DEFENSE, CRIT_CHANCE, CRIT_MULTIPLIER). */
    public static double stat(int classIndex, Stat stat) {
        Objects.requireNonNull(stat, "stat");
        return column(get(classIndex), stat);
    }

    /** Gold a new character of this class starts with. Grant it through the Wallet with GoldReason.START. */
    public static int startingGold(int classIndex) {
        return get(classIndex).startingGold();
    }

    public static int indexOfName(String className) {
        if (className == null) return -1;
        String wanted = className.trim();
        for (int i = 0; i < count(); i++) {
            if (name(i).equalsIgnoreCase(wanted)) return i;
        }
        return -1;
    }

    /**
     * Stamps the class onto the player: class identity, base stats, maximums, level and exp.
     * Starting gold is NOT set here; see {@link #startingGold(int)}.
     */
    public static void applyToPlayer(Player player, int classIndex) {
        Objects.requireNonNull(player, "player");
        ClassDef def = get(classIndex);

        // LevelingSystem.growthFor(player.getClassIndex()) needs this;
        // without it classIndex stays -1 and level-ups give no stat growth.
        player.applyClass(def.name(), classIndex);

        // Class HP/MP are also the class maximums (equipment adds to them via Player.maxHp/maxMp).
        player.setBaseMaxHp(def.hp());
        player.setBaseMaxMp(def.mp());

        for (Stat stat : STAT_COLUMNS) {
            player.set(stat, column(def, stat));
        }

        player.setExp(def.startExp());
        player.setLevel(def.startLevel());
    }

    public static double[] statMaxes() {
        return STAT_MAXES.clone();
    }

    /** Each class stat divided by the best value of that stat across all classes (0..1). */
    public static double[] normalizedStats(int classIndex) {
        ClassDef def = get(classIndex);
        double[] normalized = new double[NORMALIZED_STAT_COUNT];
        for (int s = 0; s < NORMALIZED_STAT_COUNT; s++) {
            double max = STAT_MAXES[s];
            normalized[s] = max == 0 ? 0 : column(def, STAT_COLUMNS[s]) / max;
        }
        return normalized;
    }

    private static ClassDef get(int classIndex) {
        if (!isValidIndex(classIndex)) {
            throw new IllegalArgumentException(
                "Invalid class index " + classIndex + " (0.." + (count() - 1) + ")");
        }
        return CLASSES.get(classIndex);
    }

    private static double column(ClassDef def, Stat stat) {
        switch (stat) {
            case HP:              return def.hp();
            case MP:              return def.mp();
            case SPEED:           return def.speed();
            case ATTACK:          return def.attack();
            case DEFENSE:         return def.defense();
            case CRIT_CHANCE:     return def.critChance();
            case CRIT_MULTIPLIER: return def.critMultiplier();
            default:
                throw new IllegalArgumentException("Classes define no value for stat " + stat);
        }
    }
}