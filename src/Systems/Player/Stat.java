package Systems.Player;

import java.util.List;

public enum Stat {
    HP,
    MP,
    SPEED,
    ATTACK,
    DEFENSE,
    CRIT_CHANCE,
    CRIT_MULTIPLIER;

    /** Order gear modifiers are listed in. Immutable. */
    public static final List<Stat> MODIFIER_ORDER =
        List.of(ATTACK, CRIT_CHANCE, SPEED, DEFENSE, MP, HP, CRIT_MULTIPLIER);

    /** Compact label for one-line summaries: ATK, CRIT, SPD, DEF, MP, HP, CMULT. */
    public String shortLabel() {
        switch (this) {
            case ATTACK:          return "ATK";
            case CRIT_CHANCE:     return "CRIT";
            case SPEED:           return "SPD";
            case DEFENSE:         return "DEF";
            case MP:              return "MP";
            case HP:              return "HP";
            case CRIT_MULTIPLIER: return "CMULT";
            default:              return name();
        }
    }
}