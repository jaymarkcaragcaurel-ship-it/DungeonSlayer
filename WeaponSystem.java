package Weapons;

import Player.Player;
import Player.Stat;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Equipping and unequipping weapons.
 *
 * Silent by design: no printing, no purchasing, no stash handling. The shop charges the gold,
 * the inventory decides what happens to a replaced weapon, and the UI reports what happened.
 *
 * HP and MP are special: a weapon's HP/MP raises the player's *maximum* (see Player.maxHp/maxMp),
 * it never touches the *current* value. Equipping therefore cannot heal or refill mana, and
 * swapping weapons cannot be used to farm it. When the maximum drops, current HP/MP are capped.
 *
 * Both operations are atomic: if anything fails part-way, the player's stats and weapon are
 * restored exactly as they were before the call.
 */
public final class WeaponSystem {

    private WeaponSystem() {}

    /**
     * Equips {@code weapon}, removing the current weapon's bonuses first.
     *
     * @return the weapon that was replaced, or null if the slot was empty
     */
    public static Weapon equip(Player player, Weapon weapon) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(weapon, "weapon");

        Snapshot before = Snapshot.of(player);
        try {
            Weapon previous = removeCurrent(player);
            apply(player, weapon, +1);
            player.setWeapon(weapon);
            clampPools(player);
            return previous;
        } catch (RuntimeException e) {
            before.restore(player);
            throw e;
        }
    }

    /**
     * Removes the equipped weapon and its bonuses.
     *
     * @return the removed weapon, or null if nothing was equipped
     */
    public static Weapon unequip(Player player) {
        Objects.requireNonNull(player, "player");

        Snapshot before = Snapshot.of(player);
        try {
            Weapon removed = removeCurrent(player);
            clampPools(player);
            return removed;
        } catch (RuntimeException e) {
            before.restore(player);
            throw e;
        }
    }

    /**
     * Net change to the player's stats if {@code candidate} replaced the current weapon.
     * Built for shop comparisons ("ATK +12, SPD -5"). Zero changes are omitted.
     * HP and MP entries are changes to the player's maximum, not to the current value.
     */
    public static Map<Stat, Double> statChange(Player player, Weapon candidate) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(candidate, "candidate");

        Weapon current = player.getWeapon();
        Map<Stat, Double> changes = new EnumMap<>(Stat.class);

        for (Stat stat : Stat.values()) {
            double delta = candidate.bonus(stat) - (current == null ? 0.0 : current.bonus(stat));
            delta = round2(delta);
            if (delta != 0.0) changes.put(stat, delta);
        }
        return changes;
    }

    // ── internals ────────────────────────────────────────────────────────────

    private static Weapon removeCurrent(Player player) {
        Weapon current = player.getWeapon();
        if (current == null) return null;

        apply(player, current, -1);
        player.setWeapon(null);
        return current;
    }

    /**
     * Adds (+1) or removes (-1) a weapon's bonuses for every stat except HP and MP, which only
     * change the maximum (handled by Player.maxHp/maxMp and clampPools).
     *
     * Rounding after every change stops 0.1-step attack rolls from drifting
     * (e.g. 35.5 + 17.3 - 17.3 coming back as 35.49999999999999).
     */
    private static void apply(Player player, Weapon weapon, int sign) {
        for (Stat stat : Stat.values()) {
            if (stat == Stat.HP || stat == Stat.MP) continue;

            double bonus = weapon.bonus(stat);
            if (bonus == 0.0) continue;
            player.set(stat, round2(player.get(stat) + sign * bonus));
        }
    }

    /** Current HP/MP may not exceed the maximum now that the weapon has changed. */
    private static void clampPools(Player player) {
        double maxHp = player.maxHp();
        if (player.get(Stat.HP) > maxHp) player.set(Stat.HP, maxHp);
        double maxMp = player.maxMp();
        if (player.get(Stat.MP) > maxMp) player.set(Stat.MP, maxMp);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Stats and weapon at a point in time, used to roll back a failed equip/unequip. */
    private record Snapshot(double[] stats, Weapon weapon) {
        static Snapshot of(Player player) {
            double[] values = new double[Stat.values().length];
            for (Stat stat : Stat.values()) values[stat.ordinal()] = player.get(stat);
            return new Snapshot(values, player.getWeapon());
        }

        void restore(Player player) {
            for (Stat stat : Stat.values()) player.set(stat, stats[stat.ordinal()]);
            player.setWeapon(weapon);
        }
    }
}
