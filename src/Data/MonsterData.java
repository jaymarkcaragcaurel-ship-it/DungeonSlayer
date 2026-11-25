package Data;

/**
 * Raw monster tables. Read by MonsterSystem and DataMonsterSource.
 * Ranges are inclusive {min, max}.
 */
public class MonsterData {

    // Column order for the stat ranges below.
    public static final int HEALTH  = 0;
    public static final int ATTACK  = 1;
    public static final int DEFENSE = 2;
    public static final int SPEED   = 3;
    public static final int EXP     = 4;
    public static final int GOLD    = 5;
    public static final int STAT_COUNT = 6;

    public static final int[][] REGULAR = {
        {  30, 120 },   // HEALTH
        {   5,  25 },   // ATTACK
        {   2,  15 },   // DEFENSE
        {   3,  12 },   // SPEED
        {  10,  50 },   // EXP
        {   5,  30 },   // GOLD
    };

    public static final int[][] FLOOR_BOSS = {
        { 150, 750 },
        {  35,  90 },
        {  20,  55 },
        {   5,  18 },
        { 150, 500 },
        {  75, 300 },
    };

    public static final String[] REGULAR_NAMES = {
        "Verndunoz", "Grimfang", "Zarvok", "Morthul", "Skelrith", "Dreadmaw",
        "Vexthorn", "Kragnor", "Blightclaw", "Tharzul", "Nyxeron", "Frostmaw",
        "Gorvath", "Hexdrake", "Ravengore", "Zulkreth", "Voidfang", "Morgrin",
        "Ashripper", "Terrorix", "Skarnok", "Bonegrinder", "Vilethorn", "Drakmar",
        "Cindermaw", "Nethrak", "Ghoulfang", "Wraithclaw", "Sablefang", "Korvuth",
        "Mireling", "Grimscar", "Ashenvex", "Thornjaw", "Duskreaver", "Hollowmaw",
        "Gravelurk", "Rotclaw", "Shadecrawler", "Vorlak", "Bloodmire", "Cragtooth",
        "Emberfang", "Fellshade", "Gloomhide", "Harrowick", "Ironmaw", "Jaggerith",
        "Krelthos", "Lurkmaw", "Mordrake", "Netherclaw", "Oozeling", "Pyrefang",
        "Quillfang", "Rendclaw", "Stonegnash", "Tombcrawler", "Umbrath", "Venomscale"
    };

    /** Floor boss names. Must not overlap DUNGEON_BOSS_POOL names. */
    public static final String[] FLOOR_BOSS_NAMES = {
        "Shadowfang Rex", "Gorvanus the Blighted", "Thornback Tyrant", "Abyssal Warlord",
        "Skarn the Hollow", "Ulgrath Bonecrown", "Veyra the Plaguewing", "Grommash Ironmaw",
        "Sylthara Webqueen", "Kharzul the Ashen", "Ironjaw Behemoth", "Morvane the Hollow King",
        "Lady Venomthorn", "Karthus Gravebinder", "Ysolde the Pale", "Balgrim Stonefist",
        "Vexara Nightbloom", "Zorathul the Burning", "Draugr Warchief", "Ebonmaw the Devourer",
        "Hexen Bloodwitch", "Torvald Grimshield", "Nalthazar the Frozen", "Crimson Matriarch",
        "Ossuarius the Bonelord", "Gloomhowl Alpha", "Sepulchra the Weeping", "Malgor's Herald",
        "Rotfather Pestilax", "Skullcrusher Brak", "Umbra the Faceless", "Voidcaller Neth",
        "Ashen Colossus", "Tidebreaker Krull", "Wyrmtongue Sethis", "Duskblade Verran",
        "Cinderqueen Aurex", "Mourning Titan", "Hollow Prophet Zhul", "Gravewarden Ostrik"
    };

    // Dungeon boss template layout.
    public static final int BOSS_NAME      = 0;
    public static final int BOSS_TITLE     = 1;
    public static final int BOSS_HP        = 2;
    public static final int BOSS_ATTACK    = 3;
    public static final int BOSS_DEFENSE   = 4;
    public static final int BOSS_SPEED     = 5;
    public static final int BOSS_EXP       = 6;
    public static final int BOSS_GOLD      = 7;
    public static final int BOSS_PHASE2_AT = 8;   // enrages when HP fraction falls to this
    public static final int BOSS_SPECIAL   = 9;   // special move name

    public static final Object[][] DUNGEON_BOSS_POOL = {
        { "Malachar",           "The Undying",         850.0,  75.0, 45.0,  8.0, 500, 250, 0.50, "Soul Drain"   },
        { "Vorgath",            "Soulreaper",          720.0,  90.0, 35.0, 12.0, 480, 230, 0.45, "Reap"         },
        { "Zythera",            "The Cursed",          680.0,  85.0, 30.0, 14.0, 460, 210, 0.40, "Hex Burst"    },
        { "Dreadlord Karnox",   "Destroyer of Realms", 950.0, 100.0, 50.0,  7.0, 550, 300, 0.55, "Void Slam"    },
        { "Xelthar",            "Doomcaller",          760.0,  80.0, 40.0, 11.0, 510, 270, 0.50, "Death Knell"  },
        { "Nightshade Empress", "Queen of Shadows",    700.0,  95.0, 25.0, 18.0, 490, 260, 0.35, "Umbral Strike" },
    };

    /** Dungeon boss scaling per depth beyond the first: HP and gold, then attack. */
    public static final double BOSS_HP_GOLD_SCALE_PER_DEPTH = 0.08;
    public static final double BOSS_ATTACK_SCALE_PER_DEPTH  = 0.05;
}