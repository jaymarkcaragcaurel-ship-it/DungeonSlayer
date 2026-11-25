package Data;

import java.util.List;

/** The five playable classes. Read them through Core.ClassSystem, not directly. */
public final class ClassesData {

    private ClassesData() {}

    /**
     * Selectors for Core.ClassSystem.stat(classIndex, field). They name numeric columns only;
     * the numbers are unchanged from the old Object[] table, so existing callers still compile.
     */
    public static final int HP          = 2;
    public static final int MP          = 3;
    public static final int SPEED       = 4;
    public static final int ATTACK      = 5;
    public static final int DEFENSE     = 6;
    public static final int CRIT_CHANCE = 7;
    public static final int CRIT_MULT   = 8;
    public static final int EXP         = 9;
    public static final int LEVEL       = 10;
    public static final int GOLD        = 11;

    public static final int WARRIOR  = 0;
    public static final int MAGE     = 1;
    public static final int PALADIN  = 2;
    public static final int ARCHER   = 3;
    public static final int ASSASSIN = 4;

    // Starting gold is 100 for every class: a PLACEHOLDER, set it to your balance numbers.
    public static final List<ClassDef> CLASSES = List.of(
        new ClassDef("Warrior",
            "A battle-hardened frontliner built to absorb punishment and deal steady damage.",
            220.0, 40.0, 9.0, 35.5, 90.0, 12.0, 1.5, 0, 1, 100),
        new ClassDef("Mage",
            "A glass cannon that channels destructive arcane power at the cost of fragility.",
            80.0, 200.0, 8.0, 72.0, 18.0, 22.0, 2.0, 0, 1, 100),
        new ClassDef("Paladin",
            "A holy warrior who balances strong defense with divine healing and smite power.",
            200.0, 90.0, 7.0, 30.0, 80.0, 10.0, 1.6, 0, 1, 100),
        new ClassDef("Archer",
            "A ranged expert who picks off enemies from safety with precise, rapid shots.",
            100.0, 55.0, 15.0, 48.0, 22.0, 40.0, 1.9, 0, 1, 100),
        new ClassDef("Assassin",
            "A cold-blooded killer who vanishes into shadow and delivers lethal precision strikes.",
            88.0, 75.0, 22.0, 55.0, 18.0, 70.0, 3.0, 0, 1, 100)
    );
}