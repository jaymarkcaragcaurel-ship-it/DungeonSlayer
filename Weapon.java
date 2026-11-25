package Weapons;

import Player.Stat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable, fully-rolled weapon.
 *
 * @param prefix    prefix label, e.g. "Burning"
 * @param base      base weapon name, e.g. "Dagger"
 * @param suffix    suffix label, e.g. "of Ruin"
 * @param affinity  weapon family, e.g. "Blade"
 * @param tier      rarity tier, 1 (Common) .. 5 (Mythic)
 * @param attack    rolled base attack (prefix/suffix ATK is NOT included)
 * @param modifiers combined prefix + suffix stat modifiers (zero entries omitted)
 * @param price     shop price in gold
 */
public record Weapon(
        String prefix,
        String base,
        String suffix,
        String affinity,
        int tier,
        double attack,
        Map<Stat, Double> modifiers,
        int price) {

    public static final String[] TIER_NAMES =
        { "Common", "Uncommon", "Rare", "Legendary", "Mythic" };

    public Weapon {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(suffix, "suffix");
        Objects.requireNonNull(affinity, "affinity");
        Objects.requireNonNull(modifiers, "modifiers");

        if (tier < 1 || tier > TIER_NAMES.length) {
            throw new IllegalArgumentException("tier must be 1.." + TIER_NAMES.length + ", got " + tier);
        }
        if (Double.isNaN(attack) || Double.isInfinite(attack) || attack < 0) {
            throw new IllegalArgumentException("attack must be a finite number >= 0, got " + attack);
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must be >= 0, got " + price);
        }

        EnumMap<Stat, Double> copy = new EnumMap<>(Stat.class);
        for (Map.Entry<Stat, Double> e : modifiers.entrySet()) {
            Double v = e.getValue();
            if (v == null || v.isNaN() || v.isInfinite()) {
                throw new IllegalArgumentException("modifier " + e.getKey() + " must be a finite number, got " + v);
            }
            copy.put(e.getKey(), v);
        }
        modifiers = Collections.unmodifiableMap(copy);
    }

    public String displayName() {
        return prefix + " " + base + " " + suffix;
    }

    public String tierName() {
        return TIER_NAMES[tier - 1];
    }

    /** Total amount this weapon adds to the given player stat when equipped. */
    public double bonus(Stat stat) {
        double total = modifiers.getOrDefault(stat, 0.0);
        if (stat == Stat.ATTACK) total += attack;
        return total;
    }

    /**
     * One-line summary for shops and logs, stats in {@link Stat#MODIFIER_ORDER}, e.g.
     * "Burning Dagger of Ruin [Rare]  ATK +31.4  SPD +10  (120g)".
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(displayName()).append(" [").append(tierName()).append("]");

        for (Stat stat : Stat.MODIFIER_ORDER) {
            double value = bonus(stat);
            if (value == 0.0) continue;
            sb.append("  ").append(stat.shortLabel()).append(' ')
              .append(value > 0 ? "+" : "")
              .append(trim(value));
        }

        sb.append("  (").append(price).append("g)");
        return sb.toString();
    }

    private static String trim(double value) {
        return value == Math.rint(value)
            ? String.valueOf((long) value)
            : String.format(Locale.ROOT, "%.1f", value);
    }
}
