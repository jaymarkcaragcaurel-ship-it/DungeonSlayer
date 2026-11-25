package Data;

public class ArmorData {

    /** Index of the slot string ("head" / "body") in a BASE_POOL entry. */
    public static final int BASE_SLOT = 6;

    public static final int MIN_ARMORS = 5;
    public static final int MAX_ARMORS = 10;


    public static final int[] STAT_REQ_MAP = { 3, 5, 2, 4, 1, 0 };

    public static final double[][] TIER_WEIGHT_BANDS = {
        { 0.40, 1.00 },
        { 0.15, 0.39 },
        { 0.05, 0.14 },
        { 0.02, 0.04 },
        { 0.00, 0.01 },
    };

    public static final String[] AFFINITIES = {
        "Plate",
        "Mail",
        "Leather",
        "Cloth",
        "Scale",
        "Bone",
        "Crystal",
        "Shadow",
        "Runed",
        "Beast",
        "Warden",
        "Celestial",
    };

    public static final double[][] AFFINITY_ARCHETYPES = {
        { 0.9, 0.1, 0.2, 0.4, 1.0, 0.1, 0.2 },
        { 0.6, 0.2, 0.4, 0.6, 0.7, 0.3, 0.3 },
        { 0.3, 0.2, 0.9, 0.4, 0.2, 0.6, 0.5 },
        { 0.1, 1.0, 0.2, 0.2, 0.1, 0.4, 0.5 },
        { 0.6, 0.1, 0.4, 0.8, 0.8, 0.3, 0.4 },
        { 0.3, 0.2, 0.5, 0.7, 0.3, 0.8, 0.9 },
        { 0.2, 0.9, 0.3, 0.3, 0.2, 0.7, 0.6 },
        { 0.2, 0.3, 1.0, 0.5, 0.1, 0.9, 0.8 },
        { 0.3, 0.8, 0.2, 0.3, 0.5, 0.4, 0.4 },
        { 0.4, 0.2, 0.8, 0.7, 0.2, 0.7, 0.6 },
        { 0.7, 0.5, 0.2, 0.4, 0.9, 0.1, 0.2 },
        { 0.6, 0.7, 0.2, 0.3, 0.8, 0.2, 0.3 },
    };

    public static final double[] AFFINITY_WEIGHTS = { 0.9, 0.9, 0.8, 0.9, 1.0, 0.9, 0.8 };

    public static final int[][] FORBIDDEN_MAP = {
        { 3, 6, 7, 8 },
        { 3, 6 },
        { 0, 4, 10, 11 },
        { 0, 4, 5, 10 },
        { 3, 6, 7, 8 },
        { 0, 10, 11, 3 },
        { 0, 4, 5, 10 },
        { 0, 4, 10, 11 },
        { 0, 4, 5 },
        { 0, 3, 10, 11 },
        { 3, 6, 7, 8 },
        { 5, 7, 6, 3 },
    };

    public static final Object[][] BASE_POOL = {
        { "Iron Helm",       15.0, 35.0,  0, "plate",     3, "head" },
        { "Plate Cuirass",   35.0, 70.0,  0, "plate",     5, "body" },
        { "Chainmail Vest",  20.0, 45.0,  1, "mail",      0, "body" },
        { "Ringmail Coif",   12.0, 28.0,  1, "mail",      3, "head" },
        { "Leather Armor",   10.0, 25.0,  2, "leather",   2, "body" },
        { "Mage Robes",       5.0, 15.0,  3, "cloth",     4, "body" },
        { "Tome Vestment",    8.0, 18.0,  3, "cloth",     4, "body" },
        { "Scale Hauberk",   28.0, 55.0,  4, "scale",     3, "body" },
        { "Bone Armor",      14.0, 32.0,  5, "bone",      1, "body" },
        { "Crystal Shroud",   6.0, 18.0,  6, "crystal",   4, "body" },
        { "Shadow Wraps",     8.0, 20.0,  7, "shadow",    2, "body" },
        { "Runed Coat",      10.0, 24.0,  8, "runed",     4, "body" },
        { "Beast Hide",      14.0, 30.0,  9, "beast",     2, "body" },
        { "Warden Plate",    30.0, 60.0, 10, "warden",    3, "body" },
        { "Celestial Mail",  18.0, 38.0, 11, "celestial", 5, "body" },
        { "Leather Cap",       5.0, 14.0,  2, "leather",   2, "head" },
        { "Wizard Hat",        3.0,  9.0,  3, "cloth",     4, "head" },
        { "Scale Helm",       14.0, 30.0,  4, "scale",     3, "head" },
        { "Bone Mask",         7.0, 17.0,  5, "bone",      1, "head" },
        { "Crystal Circlet",   3.0, 10.0,  6, "crystal",   4, "head" },
        { "Shadow Hood",       4.0, 11.0,  7, "shadow",    2, "head" },
        { "Runed Circlet",     5.0, 13.0,  8, "runed",     4, "head" },
        { "Beast Skull",       7.0, 16.0,  9, "beast",     2, "head" },
        { "Warden Helm",      15.0, 32.0, 10, "warden",    3, "head" },
        { "Celestial Halo",    9.0, 20.0, 11, "celestial", 5, "head" },
    };

    public static final Object[][] PREFIX_POOL = {
        { "Iron",        new double[]{  0,  0, -5, 15,  0, 10 }, 0.60,  "heavy",   3, 0.44 },
        { "Reinforced",  new double[]{  0,  0,  0, 12,  0,  8 }, 0.65,  "heavy",  -1, 0.0  },
        { "Padded",      new double[]{  0,  0,  5,  8,  0,  5 }, 0.65,  "comfort",-1, 0.0  },
        { "Gilded",      new double[]{  5,  0,  0,  8,  0,  0 }, 0.60,  "noble",  -1, 0.0  },
        { "Blessed",     new double[]{  0,  0,  0,  8, 10,  0 }, 0.55,  "holy",   -1, 0.0  },
        { "Tempered",    new double[]{  5,  0,  0, 10,  0,  0 }, 0.55,  "heavy",  -1, 0.0  },
        { "Swift",       new double[]{  0,  5, 20, -5,  0,  0 }, 0.60,  "wind",    2, 0.40 },
        { "Stalwart",    new double[]{  0,  0, -5, 22,  0, 15 }, 0.55,  "iron",    3, 0.44 },

        { "Enchanted",   new double[]{  0,  0,  0, 10, 20,  0 }, 0.30,  "arcane",  4, 0.48 },
        { "Warded",      new double[]{  0,  0,  0, 18,  0, 12 }, 0.25,  "iron",    3, 0.50 },
        { "Soulforged",  new double[]{  0, 10,  0, 10, 10,  0 }, 0.25,  "relic",  -1, 0.0  },
        { "Frost",       new double[]{  0,  8, 12,  5,  0,  0 }, 0.35,  "frost",   2, 0.40 },
        { "Radiant",     new double[]{  0,  0,  0, 15, 15,  0 }, 0.25,  "holy",    4, 0.40 },
        { "Ethereal",    new double[]{  0,  5, 12,  0, 20,  0 }, 0.25,  "arcane",  4, 0.45 },
        { "Ancient",     new double[]{  5,  8,  5,  8,  0,  5 }, 0.20,  "relic",  -1, 0.0  },
        { "Phantom",     new double[]{  0,  8, 18, -5,  0,  0 }, 0.30,  "shadow",  2, 0.60 },
        { "Storm",       new double[]{  5, 10, 15,  0,  0,  0 }, 0.30,  "wind",    2, 0.50 },
        { "Void",        new double[]{  5, 15,  0,  0,  0, -5 }, 0.30,  "dark",    1, 0.43 },

        { "Obsidian",    new double[]{  5,  0, -5, 25,  0, 15 }, 0.12,  "heavy",   3, 0.60 },
        { "Soulbound",   new double[]{  0, 18,  0, 10, 10,  5 }, 0.10,  "relic",   1, 0.55 },
        { "Cursed",      new double[]{ 20,  0,  0,-10,  0,  0 }, 0.10,  "dark",   -1, 0.0  },
        { "Bloodforged", new double[]{ 20, 10,-10, 10,  0,  0 }, 0.08,  "brutal",  0, 0.60 },
        { "Dragonscale", new double[]{  5,  0,  0, 30,  0, 20 }, 0.07,  "fire",    3, 0.70 },

        { "Titanic",     new double[]{  0,  0, -5, 40,  0, 35 }, 0.04,  "heavy",  -1, 0.0  },
        { "Sanctified",  new double[]{  0,  0,  0, 25, 25, 20 }, 0.03,  "holy",   -1, 0.0  },
        { "Abyssal",     new double[]{ 25, 20,  0,  5,  0,  0 }, 0.03,  "dark",   -1, 0.0  },
        { "Draconic",    new double[]{ 15,  0,  0, 35,  0, 25 }, 0.02,  "fire",   -1, 0.0  },

        { "Godforged",   new double[]{ 10, 15,  5, 50, 20, 40 }, 0.01,  "relic",  -1, 0.0  },
        { "Wraithbound", new double[]{ 30, 40, 20,  0,  0,  0 }, 0.008, "shadow", -1, 0.0  },
        { "Undying",     new double[]{  0,  0,  0, 30, 30, 80 }, 0.005, "holy",   -1, 0.0  },
    };

    public static final Object[][] SUFFIX_POOL = {
        { "of the Fortress", new double[]{  0,  0, -5, 20,  0, 15 }, 0.45,  "iron",    3, 0.40 },
        { "of Swiftness",    new double[]{  0,  0, 25,  0,  0,  0 }, 0.50,  "wind",    2, 0.50 },
        { "of the Fallen",   new double[]{  0, 15,  0,  8,  0,  0 }, 0.45,  "dark",   -1, 0.0  },
        { "of Valor",        new double[]{  8,  0,  0, 14,  0,  8 }, 0.50,  "noble",  -1, 0.0  },
        { "of the Hunt",     new double[]{ 12,  0, 10,  0,  0,  0 }, 0.50,  "wind",   -1, 0.0  },
        { "of the North",    new double[]{  0,  0,  8, 18,  0, 10 }, 0.45,  "frost",   3, 0.40 },
        { "of Fury",         new double[]{ 15,  5,  0, -5,  0,  0 }, 0.45,  "brutal", -1, 0.0  },

        { "of the Titan",    new double[]{  0,  0, -5, 25,  0, 20 }, 0.15,  "iron",    3, 0.50 },
        { "of Arcana",       new double[]{  0,  5,  0,  0, 35,  0 }, 0.20,  "arcane",  4, 0.50 },
        { "of the Sage",     new double[]{  0,  8,  0,  5, 30,  0 }, 0.25,  "arcane",  4, 0.45 },
        { "of the Storm",    new double[]{  8, 12, 15,  0,  0,  0 }, 0.20,  "wind",    2, 0.55 },
        { "of Shadows",      new double[]{  5, 20, 10,  0,  0,  0 }, 0.20,  "shadow",  1, 0.50 },
        { "of the Ancients", new double[]{  5,  8,  5,  8,  5,  8 }, 0.15,  "relic",  -1, 0.0  },
        { "of Vengeance",    new double[]{ 14, 14,  0,  0,  0,  0 }, 0.20,  "chaos",   1, 0.40 },
        { "of the Wilds",    new double[]{  8,  5, 14,  0,  0,  0 }, 0.30,  "wind",    2, 0.40 },
        { "of the Phoenix",  new double[]{  8,  0,  0,  0, 20, 15 }, 0.20,  "fire",    4, 0.35 },
        { "of the Void",     new double[]{ 10, 18,  0,  0,  0,  0 }, 0.20,  "dark",    1, 0.43 },

        { "of the Dragon",   new double[]{ 15,  0,  0, 30,  0, 15 }, 0.07,  "fire",    3, 0.55 },
        { "of Precision",    new double[]{  0, 30,  5,  0,  0,  0 }, 0.12,  "precise", 1, 0.50 },
        { "of Malice",       new double[]{  5, 35,  5,  0,  0,  0 }, 0.08,  "shadow",  1, 0.65 },
        { "of Carnage",      new double[]{ 35,  0,-10,  0,  0,  0 }, 0.08,  "brutal",  0, 0.50 },
        { "of Oblivion",     new double[]{ 20, 20,  0,-10,  0,  0 }, 0.05,  "dark",    1, 0.70 },

        { "of the Colossus", new double[]{  0,  0, -5, 35,  0, 30 }, 0.04,  "iron",   -1, 0.0  },
        { "of the Eternal",  new double[]{  0,  0,  0, 20, 20, 25 }, 0.03,  "holy",   -1, 0.0  },
        { "of Ruin",         new double[]{ 30, 25,  0,  0,  0,  0 }, 0.03,  "dark",   -1, 0.0  },
        { "of the Wyrm",     new double[]{ 15,  0,  0, 30,  0, 20 }, 0.02,  "fire",   -1, 0.0  },

        { "of Divinity",     new double[]{ 10, 10,  5, 40, 30, 50 }, 0.01,  "holy",   -1, 0.0  },
        { "of the Abyss",    new double[]{ 35, 50,  0,  0,  0,  0 }, 0.008, "dark",   -1, 0.0  },
        { "of Eternity",     new double[]{  0,  5,  0, 25, 40, 45 }, 0.005, "arcane", -1, 0.0  },
    };
}