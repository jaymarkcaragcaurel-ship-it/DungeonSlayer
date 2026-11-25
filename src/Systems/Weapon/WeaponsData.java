package Data;

import Gear.GearTable;

/**
 * Weapon tables. Entry layouts are defined by {@link GearTable}; the constants below are aliases
 * to those so the two can never drift apart.
 *
 * Modifier slot order for every stats[] array: ATK, CRIT, SPD, DEF, MP.
 */
public final class WeaponsData {

    private WeaponsData() {}

    // Base entry layout: {name, minAtk, maxAtk, affinity, type, bias}
    public static final int BASE_NAME     = GearTable.BASE_NAME;
    public static final int BASE_MIN_ATK  = GearTable.BASE_MIN;
    public static final int BASE_MAX_ATK  = GearTable.BASE_MAX;
    public static final int BASE_AFFINITY = GearTable.BASE_AFFINITY;
    public static final int BASE_TYPE     = GearTable.BASE_TYPE;
    public static final int BASE_BIAS     = GearTable.BASE_BIAS;

    // Prefix / suffix entry layout: {label, stats[], weight, tag, statReq, threshold}
    public static final int MOD_LABEL     = GearTable.MOD_LABEL;
    public static final int MOD_STATS     = GearTable.MOD_STATS;
    public static final int MOD_WEIGHT    = GearTable.MOD_WEIGHT;
    public static final int MOD_TAG       = GearTable.MOD_TAG;
    public static final int MOD_STAT_REQ  = GearTable.MOD_STAT_REQ;
    public static final int MOD_THRESHOLD = GearTable.MOD_THRESHOLD;

    public static final int MIN_WEAPONS = 5;
    public static final int MAX_WEAPONS = 10;

    public static final String[] STAT_LABELS = { "ATK", "CRIT", "SPD", "DEF", "MP" };

    /** Modifier slot -> index into the normalized class stats (HP, MP, SPD, ATK, DEF, CRIT, CRIT_MULT). */
    public static final int[] STAT_REQ_MAP = { 3, 5, 2, 4, 1 };

    /** Per tier: {minWeight, maxWeight}. Rarer tiers use smaller weights. */
    public static final double[][] TIER_WEIGHT_BANDS = {
        { 0.40, 1.00 },
        { 0.15, 0.39 },
        { 0.05, 0.14 },
        { 0.02, 0.04 },
        { 0.00, 0.01 },
    };

    public static final double[] AFFINITY_WEIGHTS = { 0.8, 0.9, 0.8, 1.0, 0.8, 1.0, 1.0 };

    public static final String[] AFFINITIES = {
        "Blade",     // 0
        "Staff",     // 1
        "Grimoire",  // 2
        "Totem",     // 3
        "Orb",       // 4
        "Bow",       // 5
        "Crossbow",  // 6
        "Fist",      // 7
        "Shield",    // 8
        "Dagger",    // 9
        "Scythe",    // 10
        "Spear",     // 11
    };

    public static final double[][] AFFINITY_ARCHETYPES = {
        { 0.3,  0.1,  0.8,  0.6,  0.2,  0.8,  0.7 },
        { 0.2,  0.9,  0.3,  0.8,  0.1,  0.3,  0.4 },
        { 0.1,  1.0,  0.2,  0.6,  0.1,  0.4,  0.5 },
        { 0.5,  0.7,  0.3,  0.3,  0.4,  0.2,  0.3 },
        { 0.1,  1.0,  0.1,  0.3,  0.1,  0.5,  0.6 },
        { 0.4,  0.2,  0.9,  0.6,  0.1,  0.7,  0.5 },
        { 0.3,  0.1,  0.7,  0.7,  0.1,  0.6,  0.6 },
        { 0.7,  0.1,  0.5,  1.0,  0.3,  0.4,  0.9 },
        { 0.9,  0.2,  0.2,  0.4,  1.0,  0.1,  0.2 },
        { 0.2,  0.3,  0.9,  0.5,  0.1,  0.9,  0.8 },
        { 0.3,  0.5,  0.4,  0.8,  0.1,  0.5,  1.0 },
        { 0.5,  0.1,  0.6,  0.7,  0.4,  0.4,  0.5 },
    };

    public static final int[][] FORBIDDEN_MAP = {
        { 3, 4, 2, 1 },
        { 10, 6, 8, 7 },
        { 10, 11, 8, 7 },
        { 10, 7, 6 },
        { 7, 10, 11 },
        { 8, 7 },
        { 8, 7, 3 },
        { 4, 3, 2, 1, 5 },
        { 9, 4, 2, 1, 5, 6 },
        { 8, 11, 10 },
        { 8, 3, 4 },
        { 2, 4, 3 },
    };

    // {name, minAtk, maxAtk, affinity, type, statBias}
    public static final Object[][] BASE_POOL = {
        { "Dagger",         10.0,  25.0,  9,  "dagger",   1 },
        { "Greatsword",     30.0,  60.0,  0,  "blade",    0 },
        { "Shortsword",     18.0,  38.0,  0,  "blade",    2 },
        { "Staff",          20.0,  50.0,  1,  "staff",    4 },
        { "Wand",           15.0,  40.0,  1,  "staff",    1 },
        { "Shortbow",       15.0,  40.0,  5,  "bow",      2 },
        { "Longbow",        25.0,  55.0,  5,  "bow",      0 },
        { "Tome",           25.0,  55.0,  2,  "grimoire", 4 },
        { "Gauntlets",      20.0,  45.0,  7,  "fist",     0 },
        { "Katar",          18.0,  38.0,  7,  "fist",     1 },
        { "Tower Shield",    5.0,  15.0,  8,  "shield",   3 },
        { "Stiletto",       14.0,  32.0,  9,  "dagger",   1 },
        { "Spirit Totem",   15.0,  38.0,  3,  "totem",    4 },
        { "Ward Totem",     10.0,  28.0,  3,  "totem",    3 },
        { "Crystal Orb",    18.0,  45.0,  4,  "orb",      4 },
        { "Scrying Orb",    12.0,  35.0,  4,  "orb",      1 },
        { "Light Crossbow", 20.0,  42.0,  6,  "crossbow", 2 },
        { "Heavy Crossbow", 30.0,  58.0,  6,  "crossbow", 0 },
        { "Scythe",         28.0,  58.0, 10,  "scythe",   0 },
        { "Sickle",         16.0,  38.0, 10,  "scythe",   1 },
        { "Spear",          22.0,  48.0, 11,  "spear",    0 },
        { "Javelin",        14.0,  34.0, 11,  "spear",    2 },
    };

    // {label, stats[ATK, CRIT, SPD, DEF, MP], weight, tag, statReq, threshold}
    public static final Object[][] PREFIX_POOL = {
        // Tier 1
        { "Burning",     new double[]{ 10,  0,   0,   0,  0 },  0.65,  "fire",    -1,  0.0  },
        { "Swift",       new double[]{  0,  5,  20,   0,  0 },  0.60,  "wind",    -1,  0.0  },
        { "Iron",        new double[]{  5,  0,   0,  15,  0 },  0.60,  "heavy",    3,  0.56 },
        { "Gilded",      new double[]{  8,  0,   0,   5,  0 },  0.70,  "noble",   -1,  0.0  },
        { "Jagged",      new double[]{ 12,  8,   0,  -5,  0 },  0.55,  "brutal",  -1,  0.0  },
        { "Tempered",    new double[]{ 10,  0,   0,  10,  0 },  0.55,  "heavy",   -1,  0.0  },
        { "Stalwart",    new double[]{  0,  0,  -5,  22,  0 },  0.55,  "iron",     3,  0.44 },
        { "Wicked",      new double[]{ 14,  6,   0,   0,  0 },  0.50,  "dark",    -1,  0.0  },

        // Tier 2
        { "Ancient",     new double[]{  8,  8,   5,   0,  0 },  0.20,  "relic",   -1,  0.0  },
        { "Arcane",      new double[]{  0, 10,   0,   0, 25 },  0.30,  "arcane",   4,  0.48 },
        { "Void",        new double[]{  5, 15,   0,   0,  0 },  0.30,  "dark",     1,  0.43 },
        { "Frost",       new double[]{  0,  8,  12,   5,  0 },  0.35,  "frost",    2,  0.40 },
        { "Storm",       new double[]{  5, 10,  15,   0,  0 },  0.30,  "wind",     2,  0.50 },
        { "Venomous",    new double[]{  5, 12,   8,   0,  0 },  0.35,  "poison",   1,  0.35 },
        { "Runic",       new double[]{  0, 12,   0,   0, 18 },  0.30,  "arcane",   4,  0.40 },
        { "Phantom",     new double[]{  0,  8,  18,   0,  0 },  0.35,  "shadow",   2,  0.60 },
        { "Radiant",     new double[]{  0,  0,   0,  18, 15 },  0.25,  "holy",     3,  0.44 },
        { "Ethereal",    new double[]{  0,  5,  12,   0, 20 },  0.25,  "arcane",   4,  0.45 },

        // Tier 3
        { "Savage",      new double[]{ 25,  0,  -5,  -5,  0 },  0.14,  "brutal",   0,  0.77 },
        { "Cursed",      new double[]{ 20,  0,   0, -10,  0 },  0.10,  "dark",    -1,  0.0  },
        { "Obsidian",    new double[]{ 20,  5,   0,   8,  0 },  0.12,  "heavy",    0,  0.60 },
        { "Soulbound",   new double[]{ 10, 18,   0,   0, 10 },  0.10,  "relic",    1,  0.55 },
        { "Bloodforged", new double[]{ 30, 10, -10,   0,  0 },  0.08,  "brutal",   0,  0.70 },

        // Tier 4
        { "Divine",      new double[]{ 20, 15,  10,  15, 20 },  0.04,  "holy",    -1,  0.0  },
        { "Abyssal",     new double[]{ 35, 20,   0, -10,  0 },  0.03,  "dark",    -1,  0.0  },
        { "Celestial",   new double[]{  0, 25,  20,   0, 30 },  0.04,  "arcane",  -1,  0.0  },
        { "Primordial",  new double[]{ 30,  0,   0,  25,  0 },  0.03,  "relic",    3,  0.50 },

        // Tier 5
        { "Godforged",   new double[]{ 60, 20, -10,  10,  0 },  0.01,  "brutal",  -1,  0.0  },
        { "Voidborn",    new double[]{  0, 55,  15, -20,  0 },  0.008, "shadow",  -1,  0.0  },
        { "Eternal",     new double[]{ 25, 25,  15,  20, 25 },  0.005, "relic",   -1,  0.0  },
    };

    public static final Object[][] SUFFIX_POOL = {
        // Tier 1
        { "of Ruin",         new double[]{ 20,  0,   0,   0,  0 },  0.45,  "chaos",    -1,  0.0  },
        { "of Swiftness",    new double[]{  0,  0,  25,   0,  0 },  0.50,  "wind",      2,  0.50 },
        { "of the Fallen",   new double[]{  0, 15,   0,   8,  0 },  0.45,  "dark",     -1,  0.0  },
        { "of Valor",        new double[]{  8,  0,   0,  14,  0 },  0.50,  "noble",    -1,  0.0  },
        { "of the Hunt",     new double[]{ 12,  0,  10,   0,  0 },  0.50,  "wind",     -1,  0.0  },
        { "of Fury",         new double[]{ 15,  5,   0,  -5,  0 },  0.45,  "brutal",   -1,  0.0  },
        { "of the North",    new double[]{  0,  0,   8,  18,  0 },  0.45,  "frost",     3,  0.40 },

        // Tier 2
        { "of Shadows",      new double[]{  5, 20,  10,   0,  0 },  0.20,  "shadow",    1,  0.50 },
        { "of the Titan",    new double[]{ 15,  0,   0,  20,  0 },  0.15,  "iron",      3,  0.50 },
        { "of Arcana",       new double[]{  0,  5,   0,   0, 35 },  0.20,  "arcane",    4,  0.50 },
        { "of the Storm",    new double[]{  8, 12,  15,   0,  0 },  0.20,  "wind",      2,  0.55 },
        { "of the Void",     new double[]{ 10, 18,   0,   0,  0 },  0.20,  "dark",      1,  0.43 },
        { "of the Sage",     new double[]{  0,  8,   0,   0, 30 },  0.25,  "arcane",    4,  0.45 },
        { "of the Ancients", new double[]{  8,  8,   5,   8,  5 },  0.15,  "relic",    -1,  0.0  },
        { "of Vengeance",    new double[]{ 14, 14,   0,   0,  0 },  0.20,  "chaos",     1,  0.40 },
        { "of the Phoenix",  new double[]{ 12,  0,   0,   0, 20 },  0.20,  "fire",      4,  0.35 },
        { "of the Wilds",    new double[]{  8,  5,  14,   0,  0 },  0.30,  "wind",      2,  0.40 },

        // Tier 3
        { "of Precision",    new double[]{  0, 30,   5,   0,  0 },  0.12,  "precise",   1,  0.50 },
        { "of Carnage",      new double[]{ 35,  0, -10,   0,  0 },  0.08,  "brutal",    0,  0.50 },
        { "of Malice",       new double[]{  5, 35,   5,   0,  0 },  0.08,  "shadow",    1,  0.65 },
        { "of the Dragon",   new double[]{ 30,  0,   0,  10,  0 },  0.07,  "fire",      0,  0.60 },
        { "of Oblivion",     new double[]{ 20, 20,   0, -10,  0 },  0.05,  "dark",      1,  0.70 },

        // Tier 4
        { "of the Seraph",   new double[]{ 15, 20,  10,  15, 25 },  0.04,  "holy",     -1,  0.0  },
        { "of Ruination",    new double[]{ 40, 10, -10,   0,  0 },  0.03,  "chaos",    -1,  0.0  },
        { "of the Eclipse",  new double[]{ 10, 30,  15,   0, 15 },  0.04,  "shadow",   -1,  0.0  },
        { "of Eternity",     new double[]{ 20,  0,   0,  30, 20 },  0.03,  "relic",    -1,  0.0  },

        // Tier 5
        { "of Armageddon",     new double[]{ 70,  0, -15,   0,  0 },  0.01,  "brutal",  -1,  0.0  },
        { "of the Cosmos",     new double[]{ 20, 30,  20,  20, 30 },  0.008, "arcane",  -1,  0.0  },
        { "of Oblivion's End", new double[]{ 30, 40,   0, -15,  0 },  0.005, "dark",    -1,  0.0  },
    };
}
