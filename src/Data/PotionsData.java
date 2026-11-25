package Data;

public class PotionsData {

    public static final int NAME        = 0;
    public static final int DESCRIPTION = 1;
    public static final int STAT        = 2;
    public static final int AMOUNT      = 3;
    public static final int DURATION    = 4;
    public static final int PRICE       = 5;

    public static final int STAT_HP     = 2;
    public static final int STAT_MP     = 3;
    public static final int STAT_SPEED  = 4;
    public static final int STAT_ATTACK = 5;
    public static final int STAT_DEFENSE = 6;
    public static final int STAT_CRIT   = 7;

    public static final int MINOR_HEALING    = 0;
    public static final int HEALING          = 1;
    public static final int GREATER_HEALING  = 2;
    public static final int SUPERIOR_HEALING = 3;
    public static final int MINOR_MANA       = 4;
    public static final int MANA             = 5;
    public static final int GREATER_MANA     = 6;
    public static final int STRENGTH_TONIC   = 7;
    public static final int IRON_SKIN        = 8;
    public static final int QUICKNESS        = 9;
    public static final int EAGLE_EYE        = 10;

    public static final Object[][] TABLE = {
        { "Minor Healing Potion",    "Restores a small amount of HP.",           STAT_HP,      40.0, 0,  15 },
        { "Healing Potion",          "Restores a moderate amount of HP.",        STAT_HP,      90.0, 0,  35 },
        { "Greater Healing Potion",  "Restores a large amount of HP.",           STAT_HP,     160.0, 0,  75 },
        { "Superior Healing Potion", "Restores a massive amount of HP.",         STAT_HP,     260.0, 0, 150 },

        { "Minor Mana Potion",       "Restores a small amount of MP.",           STAT_MP,      30.0, 0,  15 },
        { "Mana Potion",             "Restores a moderate amount of MP.",        STAT_MP,      70.0, 0,  35 },
        { "Greater Mana Potion",     "Restores a large amount of MP.",           STAT_MP,     130.0, 0,  75 },

        { "Strength Tonic",          "Raises attack for 5 turns.",               STAT_ATTACK,  10.0, 5,  40 },
        { "Iron Skin Draught",       "Raises defense for 5 turns.",              STAT_DEFENSE, 15.0, 5,  40 },
        { "Quickness Elixir",        "Raises speed for 5 turns.",                STAT_SPEED,    5.0, 5,  40 },
        { "Eagle Eye Brew",          "Raises critical hit chance for 5 turns.",  STAT_CRIT,    15.0, 5,  45 },
    };
}
