package Systems.Dungeon;

import Data.MonstersData;
import Systems.Dungeon.MonsterSpec.Kind;

import java.util.Random;

/**
 * Rolls specs from Data.MonstersData using the spec's rules:
 *  regular / floor boss : each stat uniform in its {min, max}, inclusive
 *  dungeon boss         : fixed template; HP and ATK scaled by floor count
 *                         (HP, ATK rounded to nearest, gold rounded down,
 *                          DEF / SPD / EXP unscaled)
 */
public final class DataMonsterSource implements MonsterSource {

    private int nextId = 1;   // per-instance, so a seeded run repeats exactly

    @Override
    public MonsterSpec regular(Random rng) {
        return rolled(MonstersData.REGULAR, pick(MonstersData.REGULAR_NAMES, rng), Kind.NORMAL, rng);
    }

    @Override
    public MonsterSpec floorBoss(Random rng) {
        return rolled(MonstersData.FLOOR_BOSS, pick(MonstersData.FLOOR_BOSS_NAMES, rng), Kind.FLOOR_BOSS, rng);
    }

    @Override
    public MonsterSpec dungeonBoss(Random rng, int floors) {
        if (floors < 1) throw new IllegalArgumentException("floors must be >= 1");
        Object[] t = MonstersData.DUNGEON_BOSS_POOL[rng.nextInt(MonstersData.DUNGEON_BOSS_POOL.length)];
        double hpScale  = 1 + MonstersData.BOSS_HP_GOLD_SCALE_PER_DEPTH * (floors - 1);
        double atkScale = 1 + MonstersData.BOSS_ATTACK_SCALE_PER_DEPTH * (floors - 1);

        double phase = num(t[MonstersData.BOSS_PHASE2_AT]);
        if (phase > 1) phase /= 100.0;   // accept 50 or 0.5

        return new MonsterSpec(
                newId(),
                (String) t[MonstersData.BOSS_NAME],
                (String) t[MonstersData.BOSS_TITLE],
                Kind.DUNGEON_BOSS,
                (int) Math.round(num(t[MonstersData.BOSS_HP]) * hpScale),
                (int) Math.round(num(t[MonstersData.BOSS_ATTACK]) * atkScale),
                (int) num(t[MonstersData.BOSS_DEFENSE]),
                (int) num(t[MonstersData.BOSS_SPEED]),
                (int) num(t[MonstersData.BOSS_EXP]),
                (int) Math.floor(num(t[MonstersData.BOSS_GOLD]) * hpScale),
                (String) t[MonstersData.BOSS_SPECIAL],
                phase);
    }

    // ---- helpers -------------------------------------------------------

    private MonsterSpec rolled(int[][] table, String name, Kind kind, Random rng) {
        int hp  = roll(table[MonstersData.HEALTH],  rng);
        int atk = roll(table[MonstersData.ATTACK],  rng);
        int def = roll(table[MonstersData.DEFENSE], rng);
        int spd = roll(table[MonstersData.SPEED],   rng);
        int exp = roll(table[MonstersData.EXP],     rng);
        int gld = roll(table[MonstersData.GOLD],    rng);
        return new MonsterSpec(newId(), name, null, kind, hp, atk, def, spd, exp, gld, null, 0);
    }

    private static int roll(int[] range, Random rng) {
        return range[0] + rng.nextInt(range[1] - range[0] + 1);
    }

    private static String pick(String[] names, Random rng) {
        return names[rng.nextInt(names.length)];
    }

    private static double num(Object o) {
        return ((Number) o).doubleValue();
    }

    private String newId() {
        return "mon-" + nextId++;
    }
}