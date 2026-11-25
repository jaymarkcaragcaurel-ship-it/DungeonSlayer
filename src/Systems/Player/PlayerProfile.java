package Systems.Player;

import Systems.Weapons.Weapon;
import Systems.Armors.Armor;
import Systems.Armors.ArmorSlot;
import Systems.Leveling.LevelingSystem;

/**
 * Turns a {@link Player} into the profile screen text. Returns a String; printing (or showing it
 * in a Swing component) is the UI layer's job. Gold is not part of the Player; the caller passes
 * the Wallet balance.
 */
public final class PlayerProfile {

    private static final String DIVIDER =
        "──────────────────────────────────────────────────";

    private PlayerProfile() {}

    /** @param gold the current Wallet balance, e.g. {@code wallet.balance()} */
    public static String render(Player player, int gold) {
        StringBuilder sb = new StringBuilder();

        sb.append(DIVIDER).append('\n');
        sb.append("                   PLAYER PROFILE").append('\n');
        sb.append(DIVIDER).append('\n');

        sb.append("  Name       : ").append(player.getName()).append('\n');
        sb.append("  Class      : ").append(player.getClassName()).append('\n');
        sb.append("  Level      : ").append(player.getLevel()).append('\n');
        sb.append("  EXP        : ").append(LevelingSystem.expText(player)).append('\n');
        sb.append("  Gold       : ").append(gold).append('\n');

        sb.append(DIVIDER).append('\n');
        sb.append("                     STATS").append('\n');
        sb.append(DIVIDER).append('\n');

        sb.append("  HP         : ").append(num(player.get(Stat.HP)))
          .append(" / ").append(num(player.maxHp())).append('\n');
        sb.append("  MP         : ").append(num(player.get(Stat.MP)))
          .append(" / ").append(num(player.maxMp())).append('\n');
        sb.append("  Speed      : ").append(num(player.get(Stat.SPEED))).append('\n');
        sb.append("  Attack     : ").append(num(player.get(Stat.ATTACK))).append('\n');
        sb.append("  Defense    : ").append(num(player.get(Stat.DEFENSE))).append('\n');
        sb.append("  Crit Chance: ").append(num(player.get(Stat.CRIT_CHANCE))).append("%").append('\n');
        sb.append("  Crit Multi : ").append(num(player.get(Stat.CRIT_MULTIPLIER))).append("x").append('\n');

        sb.append(DIVIDER).append('\n');
        sb.append("                   EQUIPMENT").append('\n');
        sb.append(DIVIDER).append('\n');

        sb.append("  Weapon     : ").append(weaponName(player.getWeapon())).append('\n');
        sb.append("  Head Armor : ").append(armorName(player.getArmor(ArmorSlot.HEAD))).append('\n');
        sb.append("  Body Armor : ").append(armorName(player.getArmor(ArmorSlot.BODY))).append('\n');

        sb.append(DIVIDER);
        return sb.toString();
    }

    /**
     * Display only: rounds to 2 decimals so summed gear bonuses never print as 123.45000000000002,
     * and drops a pointless ".0" ("100", "2.5", "0.05").
     */
    private static String num(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        return rounded == Math.rint(rounded)
            ? String.valueOf((long) rounded)
            : String.valueOf(rounded);
    }

    private static String weaponName(Weapon weapon) {
        return weapon == null ? "None" : weapon.displayName();
    }

    private static String armorName(Armor armor) {
        return armor == null ? "None" : armor.displayName();
    }
}