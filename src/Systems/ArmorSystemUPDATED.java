package Armors;

import Player.Player;
import Player.Stat;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Equipping and unequipping armor, one piece per slot.
 *
 * Silent by design: no printing, no purchasing, no stash handling. The shop charges the gold,
 * the inventory decides what happens to a replaced piece, and the UI reports what happened.
 *
 * Stat model: ATTACK, DEFENSE, SPEED and the crit stats are added to the player's stats and
 * removed again on unequip. HP and MP are different: armor raises the player's MAXIMUMS
 * ({@link Player#maxHp()} / {@link Player#maxMp()} already read the worn gear), so this class
 * never adds them to the current values. After every change the current HP/MP are clamped
 * down to the new maximum. Consequences:
 *  - equipping HP/MP armor is not a heal;
 *  - removing it never lowers current HP/MP below what the new maximum allows;
 *  - maxHp() is never below 1, so armor can never kill a living player.
 *
 * A swap applies one net change per stat, so there is no half-removed intermediate state.
 *
 * Precision: every piece's bonus is counted at 2-decimal precision (the same precision
 * {@link Player#add} stores). Because each piece always contributes the same rounded amount,
 * any sequence of equips and unequips returns a stat exactly to where it started, and the
 * shop preview from {@link #statChange} always equals what equipping really does.
 */
public final class ArmorSystem {

    private ArmorSystem() {}

    /**
     * Equips {@code armor} in its own slot, removing the piece already there.
     * Equipping the exact instance that is already worn changes nothing and returns null, so a
     * caller can never receive the worn piece back as a "replaced" item.
     *
     * @return the piece that was replaced, or null if the slot was empty (or already held this piece)
     */
    public static Armor equip(Player player, Armor armor) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(armor, "armor");

        Armor previous = player.getArmor(armor.slot());
        if (previous == armor) return null;

        applySwap(player, previous, armor);
        player.setArmor(armor.slot(), armor);
        clampToMax(player);
        return previous;
    }

    /**
     * Removes the piece in {@code slot}.
     *
     * @return the removed piece, or null if the slot was empty
     */
    public static Armor unequip(Player player, ArmorSlot slot) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(slot, "slot");

        Armor current = player.getArmor(slot);
        if (current == null) return null;

        applySwap(player, current, null);
        player.setArmor(slot, null);
        clampToMax(player);
        return current;
    }

    /**
     * Net change to the player's stats if {@code candidate} replaced whatever is in its slot.
     * Built for shop comparisons ("DEF +12, HP -20"). Zero changes are omitted. HP and MP show
     * the change to the maximum.
     */
    public static Map<Stat, Double> statChange(Player player, Armor candidate) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(candidate, "candidate");

        Armor current = player.getArmor(candidate.slot());
        Map<Stat, Double> changes = new EnumMap<>(Stat.class);

        for (Stat stat : Stat.values()) {
            double delta = round2(swapDelta(current, candidate, stat));
            if (delta != 0.0) changes.put(stat, delta);
        }
        return changes;
    }

    /**
     * Applies the net change of removing {@code removed} and adding {@code added} (either may be
     * null) to every stat except HP/MP, which are maximums handled by Player.maxHp()/maxMp().
     * Player.add rounds to 2 decimals, and cannot throw for these stats given finite bonuses.
     */
    private static void applySwap(Player player, Armor removed, Armor added) {
        for (Stat stat : Stat.values()) {
            if (stat == Stat.HP || stat == Stat.MP) continue;
            double delta = swapDelta(removed, added, stat);
            if (delta != 0.0) player.add(stat, delta);
        }
    }

    /** Current HP/MP may never exceed the (gear-aware) maximum. Call after the slot is updated. */
    private static void clampToMax(Player player) {
        double maxHp = player.maxHp();
        if (player.get(Stat.HP) > maxHp) player.set(Stat.HP, maxHp);

        double maxMp = player.maxMp();
        if (player.get(Stat.MP) > maxMp) player.set(Stat.MP, maxMp);
    }

    /**
     * Net change for one stat. Each piece is rounded on its own BEFORE subtracting, so a piece
     * always contributes the identical amount when it goes on and when it comes off.
     */
    private static double swapDelta(Armor removed, Armor added, Stat stat) {
        double gained = added == null ? 0.0 : round2(added.bonus(stat));
        double lost   = removed == null ? 0.0 : round2(removed.bonus(stat));
        return gained - lost;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}