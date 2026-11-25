package Systems.Player;

/**
 * Silent stat changes for the player. Never prints: the UI layer shows messages.
 * Gold is not changed here (the Wallet owns it); EXP and level-ups live in LevelingSystem;
 * statistics are gathered by Analytics from events.
 */
public final class PlayerStatus {

    private PlayerStatus() {}

    /**
     * Changes HP, clamped to 0..maxHp(). A result strictly inside the range is rounded to
     * 2 decimals; a result clamped to the maximum is exactly maxHp(), so "HP >= maxHp()" is a
     * reliable full-HP check. A zero amount changes nothing. HP already above the maximum
     * (for example after armor is removed) is never pulled down by a heal.
     * The amount actually applied is {@code returned HP - HP before the call}.
     *
     * @param amount positive to heal, negative to damage; must not be NaN (infinity just clamps)
     * @return the player's HP afterwards
     * @throws IllegalArgumentException if amount is NaN
     */
    public static double changeHp(Player player, double amount) {
        return changeResource(player, Stat.HP, amount, player.maxHp());
    }

    /** Same rules as {@link #changeHp}, for MP and maxMp(). */
    public static double changeMp(Player player, double amount) {
        return changeResource(player, Stat.MP, amount, player.maxMp());
    }

    /** Adds to a stat. HP and MP go through the clamped changes; other stats are added as they are. */
    public static void adjustStat(Player player, Stat stat, double amount) {
        switch (stat) {
            case HP -> changeHp(player, amount);
            case MP -> changeMp(player, amount);
            default -> player.add(stat, amount);
        }
    }

    private static double changeResource(Player player, Stat stat, double amount, double max) {
        if (Double.isNaN(amount)) {
            throw new IllegalArgumentException(stat.name() + " change must be a number, got NaN");
        }
        double before = player.get(stat);
        if (amount == 0.0) return before;

        double ceiling = Math.max(before, max);   // never pull the value down just for being over max
        double raw = Math.max(0.0, Math.min(before + amount, ceiling));
        // Round only values strictly inside the range. A value clamped to the ceiling stays exact,
        // so rounding can never push it above (or just below) the maximum.
        double after = raw >= ceiling ? raw : Math.min(round2(raw), ceiling);
        player.set(stat, after);
        return after;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}