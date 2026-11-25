package Systems.Leveling;

import Data.ClassGrowth;
import Data.LevelsData;
import Systems.Core.ClassSystem;
import Systems.Player.Player;
import Systems.Player.Stat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Experience and levels. The only place EXP is added or a level is gained.
 *
 * Silent by design: it prints nothing and returns an {@link ExpResult} for the UI to report.
 *
 * Rules
 *   - {@code Data.levels.EXP_TO_NEXT[L-1]} is the TOTAL EXP needed to go from level L to L+1
 *     (thresholds are cumulative; EXP is never spent). The final 0 marks "no next level", so
 *     the level cap is EXP_TO_NEXT.length (30).
 *   - One award can pass several thresholds; every level is granted in order.
 *   - On each level-up the class's {@link ClassGrowth} row is added to the player's base stats.
 *     HP and MP growth raises the class maximum AND the current value by the same amount
 *     (no free full heal; Player.maxHp()/maxMp() still add equipment on top).
 *   - At the cap, EXP is clamped to the level-30 threshold and further awards are ignored.
 *   - A player with no class (Player.getClassIndex() < 0) levels up without stat growth.
 *   - Gear and potion buffs are additive on top of base stats, so growth never disturbs them.
 */
public final class LevelingSystem {

    private LevelingSystem() {}

    /** Stat for each ClassGrowth column. */
    private static final Stat[] GROWTH_STATS = {
        Stat.HP, Stat.MP, Stat.SPEED, Stat.ATTACK, Stat.DEFENSE, Stat.CRIT_CHANCE, Stat.CRIT_MULTIPLIER
    };

    private static final int MAX_LEVEL = LevelsData.EXP_TO_NEXT.length;

    static {
        validateTables();
    }

    // ── Level / EXP queries ──────────────────────────────────────────────────

    public static int maxLevel() { return MAX_LEVEL; }

    /** Total EXP at which {@code level} is reached (level 1 = 0). */
    public static int expForLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("Level must be 1.." + MAX_LEVEL + ", got " + level);
        }
        return level == 1 ? 0 : LevelsData.EXP_TO_NEXT[level - 2];
    }

    public static boolean isMaxLevel(Player player) {
        return player.getLevel() >= MAX_LEVEL;
    }

    /** EXP still needed for the next level; 0 at the cap. */
    public static int expToNext(Player player) {
        requireValidLevel(player);
        if (isMaxLevel(player)) return 0;
        return Math.max(0, expForLevel(player.getLevel() + 1) - player.getExp());
    }

    /** Progress through the current level, 0.0 .. 1.0 (1.0 at the cap). */
    public static double progress(Player player) {
        requireValidLevel(player);
        if (isMaxLevel(player)) return 1.0;

        int from = expForLevel(player.getLevel());
        int to   = expForLevel(player.getLevel() + 1);
        double done = (double) (player.getExp() - from) / (to - from);
        return Math.max(0.0, Math.min(done, 1.0));
    }

    /** For profile screens: "40 / 100", or "371000 (MAX)" at the cap. */
    public static String expText(Player player) {
        requireValidLevel(player);
        if (isMaxLevel(player)) return player.getExp() + " (MAX)";
        return player.getExp() + " / " + expForLevel(player.getLevel() + 1);
    }

    /** What one level-up gives this class (zero entries omitted). Handy for a "next level" preview. */
    public static Map<Stat, Double> growthFor(int classIndex) {
        Map<Stat, Double> gains = new EnumMap<>(Stat.class);
        if (classIndex < 0) return gains;                       // no class -> no growth

        double[] row = growthRow(classIndex);
        for (int i = 0; i < row.length; i++) {
            if (row[i] != 0.0) gains.put(GROWTH_STATS[i], row[i]);
        }
        return gains;
    }

    // ── Awarding EXP ─────────────────────────────────────────────────────────

    /**
     * Adds EXP and applies every level-up it earns.
     *
     * @param amount EXP to add; 0 is a no-op
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public static ExpResult gainExp(Player player, int amount) {
        Objects.requireNonNull(player, "player");
        if (amount < 0) throw new IllegalArgumentException("EXP gain cannot be negative: " + amount);
        requireValidLevel(player);

        int startExp = player.getExp();

        if (isMaxLevel(player)) {
            return new ExpResult(0, startExp, player.getLevel(), List.of(), true);
        }

        long total = (long) startExp + amount;
        List<LevelUp> levelUps = new ArrayList<>();

        while (player.getLevel() < MAX_LEVEL && total >= expForLevel(player.getLevel() + 1)) {
            levelUps.add(levelUp(player, player.getLevel() + 1));
        }

        if (player.getLevel() >= MAX_LEVEL) {
            total = Math.min(total, expForLevel(MAX_LEVEL));
        }
        int newExp = (int) Math.min(total, Integer.MAX_VALUE);
        player.setExp(newExp);

        return new ExpResult(newExp - startExp, newExp, player.getLevel(), levelUps, isMaxLevel(player));
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private static LevelUp levelUp(Player player, int newLevel) {
        Map<Stat, Double> gains = growthFor(player.getClassIndex());

        for (Map.Entry<Stat, Double> e : gains.entrySet()) {
            Stat stat = e.getKey();
            double gain = e.getValue();

            player.set(stat, round2(player.get(stat) + gain));
            if (stat == Stat.HP) player.setBaseMaxHp(round2(player.getBaseMaxHp() + gain));
            if (stat == Stat.MP) player.setBaseMaxMp(round2(player.getBaseMaxMp() + gain));
        }

        player.setLevel(newLevel);
        return new LevelUp(newLevel, gains);
    }

    private static double[] growthRow(int classIndex) {
        return ClassGrowth.PER_LEVEL[classIndex];
    }

    private static void requireValidLevel(Player player) {
        int level = player.getLevel();
        if (level < 1 || level > MAX_LEVEL) {
            throw new IllegalStateException("Player level " + level + " is outside 1.." + MAX_LEVEL);
        }
    }

    /** Fails on startup, not mid-game, if the data tables are inconsistent. */
    private static void validateTables() {
        int[] t = LevelsData.EXP_TO_NEXT;
        if (t.length < 2) throw new IllegalStateException("Invalid level table: needs at least 2 entries");
        if (t[t.length - 1] != 0) {
            throw new IllegalStateException("Invalid level table: last entry must be 0 (no next level)");
        }
        for (int i = 0; i < t.length - 1; i++) {
            if (t[i] <= 0 || (i > 0 && t[i] <= t[i - 1])) {
                throw new IllegalStateException("Invalid level table: thresholds must be positive and increasing (index " + i + ")");
            }
        }

        if (ClassGrowth.PER_LEVEL.length != ClassSystem.count()) {
            throw new IllegalStateException("Invalid growth table: " + ClassGrowth.PER_LEVEL.length
                + " rows for " + ClassSystem.count() + " classes");
        }
        for (int c = 0; c < ClassGrowth.PER_LEVEL.length; c++) {
            double[] row = ClassGrowth.PER_LEVEL[c];
            if (row.length != GROWTH_STATS.length) {
                throw new IllegalStateException("Invalid growth table: row " + c + " has " + row.length
                    + " values, expected " + GROWTH_STATS.length);
            }
            for (double v : row) {
                if (v < 0) throw new IllegalStateException("Invalid growth table: row " + c + " has a negative value");
            }
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}