package Systems.Leveling;

import Systems.Player.Stat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * One level gained: the new level and what each stat went up by (zero gains omitted).
 *
 * @param newLevel the level just reached
 * @param gains    stat -> amount added to the player's base stat
 */
public record LevelUp(int newLevel, Map<Stat, Double> gains) {

    public LevelUp {
        EnumMap<Stat, Double> copy = new EnumMap<>(Stat.class);
        copy.putAll(gains);
        gains = Collections.unmodifiableMap(copy);
    }

    /** e.g. "HP +18  MP +2  SPD +0.2  ATK +2  DEF +1  CRIT +0.1". */
    public String gainsText() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Stat, Double> e : gains.entrySet()) {
            if (sb.length() > 0) sb.append("  ");
            sb.append(e.getKey().shortLabel()).append(" +").append(format(e.getValue()));
        }
        return sb.toString();
    }

    /** e.g. "Level 5!  HP +18  MP +2 ...". */
    public String describe() {
        return "Level " + newLevel + "!  " + gainsText();
    }

    /** Up to two decimals, trailing zeros dropped: 2.0 -> "2", 0.02 -> "0.02". */
    static String format(double value) {
        String s = String.format(Locale.ROOT, "%.2f", value);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }
}