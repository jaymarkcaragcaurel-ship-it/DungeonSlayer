package Data;

import java.util.List;

/**
 * Raw item tables. Typed rows, no logic. Percentages are of the player's MAX (0..100),
 * so potions stay fair for classes with 80 to 220 HP. All values are meant to be tuned here.
 * ItemRegistry validates every row at startup and names the table and row on bad data.
 */
public final class ItemData {

    /**
     * One consumable row.
     * @param tier 1 Common .. 5 Mythic
     * @param hpPercent / mpPercent  percent of max restored (0 = none)
     * @param hpFlat / mpFlat        flat points restored (0 = none)
     */
    public record Row(String id, String name, int tier,
                      double hpPercent, double hpFlat,
                      double mpPercent, double mpFlat,
                      boolean usableOutsideBattle, int price) {}

    public static final List<Row> CONSUMABLES = List.of(
        new Row("hp_minor",   "Minor Health Potion",   1,  25, 0, 0, 0, true, 25),
        new Row("hp_std",     "Health Potion",         2,  50, 0, 0, 0, true, 60),
        new Row("hp_greater", "Greater Health Potion", 3, 100, 0, 0, 0, true, 140),
        new Row("mp_minor",   "Minor Mana Potion",     1,   0, 0, 25, 0, true, 20),
        new Row("mp_std",     "Mana Potion",           2,   0, 0, 50, 0, true, 50)
    );

    private ItemData() {}
}