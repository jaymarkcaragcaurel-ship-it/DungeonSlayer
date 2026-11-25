package Systems.Player;

import Systems.Armors.Armor;
import Systems.Armors.ArmorSlot;
import Systems.Weapons.Weapon;

import java.util.EnumMap;
import java.util.Objects;

/**
 * The player's character sheet: identity, stats, level, exp and worn gear.
 * Plain state only. Items live in Inventory, gold lives in Wallet (the Player holds no gold),
 * statistics live in Analytics. Setters reject impossible values (negative level/exp/HP/MP, NaN stats) so a bad caller
 * fails at the source instead of corrupting combat or the report.
 */
public final class Player {

    private String name = "Name";
    private String className = "Class";
    private int classIndex = -1;            // -1 until a class is applied (see applyClass)

    private final double[] stats = new double[Stat.values().length];

    // Class-level maximums. The real maximum also includes equipment: see maxHp() / maxMp().
    private double baseMaxHp = 100.0;
    private double baseMaxMp = 100.0;

    private int level = 1;
    private int exp = 0;

    private Weapon weapon = null;
    private final EnumMap<ArmorSlot, Armor> armor = new EnumMap<>(ArmorSlot.class);

    /**
     * A blank character with neutral stats. A class (ClassSystem.applyToPlayer) must be applied
     * before play; these values are deliberately weak so a missed call is obvious, not powerful.
     */
    public Player() {
        set(Stat.HP, 100.0);
        set(Stat.MP, 100.0);
        set(Stat.SPEED, 10.0);
        set(Stat.ATTACK, 10.0);
        set(Stat.DEFENSE, 0.0);
        set(Stat.CRIT_CHANCE, 0.0);
        set(Stat.CRIT_MULTIPLIER, 1.0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getClassName() {
        return className;
    }

    /** Index into Data.classes, or -1 if no class has been applied. */
    public int getClassIndex() {
        return classIndex;
    }

    /**
     * Records the player's class. Name and index are always set together so they cannot drift apart.
     *
     * @param classIndex index into Data.classes, must be >= 0
     */
    public void applyClass(String className, int classIndex) {
        Objects.requireNonNull(className, "className");
        if (classIndex < 0) {
            throw new IllegalArgumentException("classIndex must be >= 0, got " + classIndex);
        }
        this.className = className;
        this.classIndex = classIndex;
    }

    public double get(Stat stat) {
        return stats[stat.ordinal()];
    }

    /**
     * @throws IllegalArgumentException if value is NaN or infinite, or if HP/MP would be negative
     */
    public void set(Stat stat, double value) {
        requireFinite(value, stat.name());
        if ((stat == Stat.HP || stat == Stat.MP) && value < 0) {
            throw new IllegalArgumentException(stat.name() + " must be >= 0, got " + value);
        }
        stats[stat.ordinal()] = value;
    }

    /**
     * Adds to a stat. The result is rounded to 2 decimals so repeated gear equip/unequip cannot
     * accumulate floating-point drift.
     *
     * @throws IllegalArgumentException if amount, or the result, is NaN or infinite, or if HP/MP
     *                                  would become negative
     */
    public void add(Stat stat, double amount) {
        requireFinite(amount, stat.name() + " change");
        set(stat, round2(stats[stat.ordinal()] + amount));
    }

    // ── Maximum HP / MP ──────────────────────────────────────────────────────

    public double getBaseMaxHp() { return baseMaxHp; }
    public double getBaseMaxMp() { return baseMaxMp; }

    public void setBaseMaxHp(double value) {
        requirePositiveFinite(value, "baseMaxHp");
        baseMaxHp = value;
    }

    /** May be 0 (a class with no MP pool); never negative. */
    public void setBaseMaxMp(double value) {
        requireFinite(value, "baseMaxMp");
        if (value < 0) throw new IllegalArgumentException("baseMaxMp must be >= 0, got " + value);
        baseMaxMp = value;
    }

    /**
     * Class max HP plus HP from the weapon and worn armor, rounded to 2 decimals. Never below 1,
     * so negative gear modifiers cannot make a living player's maximum zero. Healing never goes
     * past this.
     */
    public double maxHp() {
        double total = baseMaxHp;
        if (weapon != null) total += weapon.bonus(Stat.HP);
        for (Armor piece : armor.values()) total += piece.bonus(Stat.HP);
        return Math.max(1.0, round2(total));
    }

    /** Class max MP plus MP from the weapon and worn armor, rounded to 2 decimals. Never below 0. */
    public double maxMp() {
        double total = baseMaxMp;
        if (weapon != null) total += weapon.bonus(Stat.MP);
        for (Armor piece : armor.values()) total += piece.bonus(Stat.MP);
        return Math.max(0.0, round2(total));
    }

    public int getLevel() {
        return level;
    }

    /** The level cap itself is LevelingSystem's rule; this only rejects levels below 1. */
    public void setLevel(int level) {
        if (level < 1) throw new IllegalArgumentException("level must be >= 1, got " + level);
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        if (exp < 0) throw new IllegalArgumentException("exp must be >= 0, got " + exp);
        this.exp = exp;
    }

    // ── Equipment ────────────────────────────────────────────────────────────

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public Armor getArmor(ArmorSlot slot) {
        return armor.get(slot);
    }

    public void setArmor(ArmorSlot slot, Armor item) {
        if (item == null) armor.remove(slot);
        else armor.put(slot, item);
    }

    // ── validation / helpers ─────────────────────────────────────────────────

    private static void requireFinite(double value, String what) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(what + " must be a finite number, got " + value);
        }
    }

    private static void requirePositiveFinite(double value, String what) {
        requireFinite(value, what);
        if (value <= 0) throw new IllegalArgumentException(what + " must be > 0, got " + value);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}