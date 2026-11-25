package Systems.Gear;

/**
 * Raw result of rolling one piece of gear, before it is turned into a Weapon or Armor.
 *
 * @param prefix    the chosen prefix entry
 * @param base      the chosen base entry
 * @param suffix    the chosen suffix entry
 * @param tier      rarity tier, 1-based
 * @param value     rolled base value: attack for weapons, defense for armor (0.1 precision)
 * @param modifiers prefix + suffix modifiers summed per slot
 * @param affinity  affinity index of the base
 * @param price     shop price in gold
 */
public record GearRoll(
        Object[] prefix,
        Object[] base,
        Object[] suffix,
        int tier,
        double value,
        double[] modifiers,
        int affinity,
        int price) {

    public String prefixLabel() { return (String) prefix[GearTable.MOD_LABEL]; }
    public String baseName()    { return (String) base[GearTable.BASE_NAME]; }
    public String suffixLabel() { return (String) suffix[GearTable.MOD_LABEL]; }
}