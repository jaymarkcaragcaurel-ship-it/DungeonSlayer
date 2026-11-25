package Data;

/**
 * One character class, as typed fields instead of an Object[] row.
 * Validated on construction, so a bad table entry fails when the game starts.
 * critChance is a percentage (0..100); critMultiplier is a factor (>= 1).
 */
public record ClassDef(
        String name,
        String description,
        double hp,
        double mp,
        double speed,
        double attack,
        double defense,
        double critChance,
        double critMultiplier,
        int startExp,
        int startLevel,
        int startingGold) {

    public ClassDef {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Class name must not be blank");
        }
        if (description == null) {
            throw new IllegalArgumentException("Class description must not be null (" + name + ")");
        }
        requireFinite(hp, "hp", name);
        requireFinite(mp, "mp", name);
        requireFinite(speed, "speed", name);
        requireFinite(attack, "attack", name);
        requireFinite(defense, "defense", name);
        requireFinite(critChance, "critChance", name);
        requireFinite(critMultiplier, "critMultiplier", name);
        if (hp <= 0)                         throw bad("hp must be > 0", name);
        if (mp < 0)                          throw bad("mp must be >= 0", name);
        if (speed < 0)                       throw bad("speed must be >= 0", name);
        if (attack < 0)                      throw bad("attack must be >= 0", name);
        if (defense < 0)                     throw bad("defense must be >= 0", name);
        if (critChance < 0 || critChance > 100) throw bad("critChance must be 0..100", name);
        if (critMultiplier < 1)              throw bad("critMultiplier must be >= 1", name);
        if (startExp < 0)                    throw bad("startExp must be >= 0", name);
        if (startLevel < 1)                  throw bad("startLevel must be >= 1", name);
        if (startingGold < 0)                throw bad("startingGold must be >= 0", name);
    }

    private static void requireFinite(double value, String field, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw bad(field + " must be a finite number", name);
        }
    }

    private static IllegalArgumentException bad(String message, String name) {
        return new IllegalArgumentException("Class " + name + ": " + message);
    }
}