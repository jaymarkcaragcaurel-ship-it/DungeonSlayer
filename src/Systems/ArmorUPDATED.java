package Armors;

import Player.Stat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An immutable, fully-rolled piece of armor.
 *
 * Invariant: the total DEF bonus ({@code defense} plus any DEF modifier) is never negative, so
 * wearing armor can never make the player less defended. A piece that breaks this is rejected
 * at construction instead of failing later in combat.
 *
 * @param prefix    prefix label, e.g. "Iron"
 * @param base      base item name, e.g. "Plate Cuirass"
 * @param suffix    suffix label, e.g. "of the Fortress"
 * @param affinity  armor family, e.g. "Plate"
 * @param slot      where it is worn
 * @param tier      rarity tier, 1 (Common) .. 5 (Mythic)
 * @param defense   rolled base defense (prefix/suffix DEF is NOT included)
 * @param modifiers combined prefix + suffix stat modifiers (zero entries omitted)
 * @param price     shop price in gold
 */
public record Armor(
        String prefix,
        String base,
        String suffix,
        String affinity,
        ArmorSlot slot,
        int tier,
        double defense,
        Map<Stat, Double> modifiers,
        int price) {

    /** Display names by tier, index 0 = tier 1. Immutable. */
    public static final List<String> TIER_NAMES =
        List.of("Common", "Uncommon", "Rare", "Legendary", "Mythic");

    /** @throws RuntimeException (NPE / IAE) if a field is null, blank, out of range or not finite */
    public Armor {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(suffix, "suffix");
        Objects.requireNonNull(affinity, "affinity");
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(modifiers, "modifiers");

        if (base.isBlank()) {
            throw new IllegalArgumentException("base name must not be blank");
        }
        if (tier < 1 || tier > TIER_NAMES.size()) {
            throw new IllegalArgumentException(
                "tier must be 1.." + TIER_NAMES.size() + ", got " + tier);
        }
        if (!Double.isFinite(defense) || defense < 0) {
            throw new IllegalArgumentException("defense must be finite and >= 0, got " + defense);
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must be >= 0, got " + price);
        }

        EnumMap<Stat, Double> copy = new EnumMap<>(Stat.class);
        for (Map.Entry<Stat, Double> entry : modifiers.entrySet()) {
            Stat stat = Objects.requireNonNull(entry.getKey(), "modifier stat");
            Double value = Objects.requireNonNull(entry.getValue(), "modifier value for " + stat);
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException(
                    "modifier for " + stat + " must be finite, got " + value);
            }
            copy.put(stat, value);
        }

        double totalDefense = defense + copy.getOrDefault(Stat.DEFENSE, 0.0);
        if (round2(totalDefense) < 0.0) {
            throw new IllegalArgumentException(
                "total DEF bonus must be >= 0, got " + totalDefense);
        }
        modifiers = Collections.unmodifiableMap(copy);
    }

    /** Prefix, base and suffix joined by single spaces; blank parts are skipped. */
    public String displayName() {
        return Stream.of(prefix, base, suffix)
            .map(String::strip)
            .filter(part -> !part.isEmpty())
            .collect(Collectors.joining(" "));
    }

    public String tierName() {
        return TIER_NAMES.get(tier - 1);
    }

    /**
     * Total amount this armor adds to the given stat when worn. For HP and MP this raises the
     * player's maximum (Player.maxHp / maxMp), not the current value.
     */
    public double bonus(Stat stat) {
        double total = modifiers.getOrDefault(stat, 0.0);
        if (stat == Stat.DEFENSE) total += defense;
        return total;
    }

    /**
     * One-line summary for shops and logs, e.g.
     * "Iron Plate Cuirass of the Fortress [Body] [Rare]  SPD -5  DEF +62  HP +25  (310g)".
     * Values are shown at the same 2-decimal precision the player's stats use, so the text
     * matches what equipping really does; a bonus that rounds to zero is not listed.
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(displayName())
          .append(" [").append(slot.getLabel()).append("]")
          .append(" [").append(tierName()).append("]");

        for (Stat stat : Stat.MODIFIER_ORDER) {
            double value = round2(bonus(stat));
            if (value == 0.0) continue;
            sb.append("  ").append(stat.shortLabel()).append(' ')
              .append(value > 0 ? "+" : "")
              .append(trim(value));
        }

        sb.append("  (").append(price).append("g)");
        return sb.toString();
    }

    /** Whole numbers without decimals, otherwise at most 2 decimals with no trailing zero. */
    private static String trim(double value) {
        if (value == Math.rint(value)) return String.valueOf((long) value);
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.endsWith("0") ? text.substring(0, text.length() - 1) : text;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
