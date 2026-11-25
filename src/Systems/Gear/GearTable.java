package Systems.Gear;

/**
 * Describes one family of generated gear (weapons, armors) to the shared generator.
 *
 * Data.weapon and Data.armor use the same entry layout, so the generator reads the
 * pools through the index constants below. The constructor validates the tables, so a
 * data typo (wrong array length, bad affinity index) fails on startup instead of
 * silently producing broken items mid-game.
 *
 * @param affinities      affinity names, e.g. "Blade", "Plate"
 * @param archetypes      per affinity: ideal normalized class stats (HP, MP, SPD, ATK, DEF, CRIT, CRIT_MULT)
 * @param affinityWeights per class-stat weight used when matching a class to an archetype
 * @param forbiddenMap    per affinity: affinities it excludes
 * @param basePool        base items: {name, min, max, affinity, type, bias, ...extras}
 * @param prefixPool      prefixes: {label, stats[], weight, tag, statReq, threshold}
 * @param suffixPool      suffixes: same layout as prefixes
 * @param tierBands       per tier: {minWeight, maxWeight} rarity band
 * @param statReqMap      modifier slot -> index into the normalized class stats
 * @param valueMultiplier gold per point of rolled base value (attack or defense)
 * @param statGoldValues  gold per point of each modifier slot
 */
public record GearTable(
        String[] affinities,
        double[][] archetypes,
        double[] affinityWeights,
        int[][] forbiddenMap,
        Object[][] basePool,
        Object[][] prefixPool,
        Object[][] suffixPool,
        double[][] tierBands,
        int[] statReqMap,
        double valueMultiplier,
        double[] statGoldValues) {

    // Base entry layout
    public static final int BASE_NAME     = 0;
    public static final int BASE_MIN      = 1;
    public static final int BASE_MAX      = 2;
    public static final int BASE_AFFINITY = 3;
    public static final int BASE_TYPE     = 4;
    public static final int BASE_BIAS     = 5;

    // Prefix / suffix entry layout
    public static final int MOD_LABEL     = 0;
    public static final int MOD_STATS     = 1;
    public static final int MOD_WEIGHT    = 2;
    public static final int MOD_TAG       = 3;
    public static final int MOD_STAT_REQ  = 4;
    public static final int MOD_THRESHOLD = 5;

    public GearTable {
        int n = affinities.length;
        int mods = statReqMap.length;

        require(archetypes.length == n,   "archetypes rows (" + archetypes.length + ") != affinities (" + n + ")");
        require(forbiddenMap.length == n, "forbiddenMap rows (" + forbiddenMap.length + ") != affinities (" + n + ")");
        require(statGoldValues.length == mods,
            "statGoldValues length (" + statGoldValues.length + ") != modifier slots (" + mods + ")");

        for (int a = 0; a < n; a++) {
            require(archetypes[a].length == affinityWeights.length,
                "archetype " + a + " has " + archetypes[a].length + " values, expected " + affinityWeights.length);
            for (int f : forbiddenMap[a]) {
                require(f >= 0 && f < n, "forbiddenMap[" + a + "] has out-of-range affinity " + f);
            }
        }

        for (int i = 0; i < basePool.length; i++) {
            Object[] base = basePool[i];
            int affinity = (int) base[BASE_AFFINITY];
            int bias = (int) base[BASE_BIAS];
            require(affinity >= 0 && affinity < n, "base " + base[BASE_NAME] + " has out-of-range affinity " + affinity);
            require(bias >= 0 && bias < mods, "base " + base[BASE_NAME] + " has out-of-range stat bias " + bias);
            require((double) base[BASE_MIN] <= (double) base[BASE_MAX], "base " + base[BASE_NAME] + " has min > max");
        }

        checkModifiers("prefix", prefixPool, mods);
        checkModifiers("suffix", suffixPool, mods);
    }

    public int tierCount()      { return tierBands.length; }
    public int modifierCount()  { return statReqMap.length; }

    private static void checkModifiers(String kind, Object[][] pool, int mods) {
        for (Object[] mod : pool) {
            double[] stats = (double[]) mod[MOD_STATS];
            require(stats.length == mods,
                kind + " " + mod[MOD_LABEL] + " has " + stats.length + " stat values, expected " + mods);
            int req = (int) mod[MOD_STAT_REQ];
            require(req >= -1 && req < mods, kind + " " + mod[MOD_LABEL] + " has out-of-range stat requirement " + req);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Invalid gear table: " + message);
    }
}