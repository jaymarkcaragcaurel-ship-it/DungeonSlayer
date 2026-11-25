package Systems.Core;

/**
 * Rules for character names. Pure logic: no Swing, no console I/O.
 * The UI asks {@link #validate(String)} on every keystroke and stores {@link #normalize(String)}.
 */
public final class NameRules {

    private NameRules() {}

    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 16;

    /** Trims the ends and collapses runs of whitespace into a single space. Null becomes "". */
    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("\\s+", " ");
    }

    /** Returns a player-facing error message, or null if the name is acceptable. */
    public static String validate(String raw) {
        String name = normalize(raw);
        if (name.length() < MIN_LENGTH) {
            return "Name must be at least " + MIN_LENGTH + " characters.";
        }
        if (name.length() > MAX_LENGTH) {
            return "Name can be at most " + MAX_LENGTH + " characters.";
        }
        if (!isAsciiLetter(name.charAt(0))) {
            return "Name must start with a letter.";
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(isAsciiLetter(c) || (c >= '0' && c <= '9') || c == ' ' || c == '\'' || c == '-')) {
                return "Only letters, digits, spaces, ' and - are allowed.";
            }
        }
        return null;
    }

    public static boolean isValid(String raw) {
        return validate(raw) == null;
    }

    private static boolean isAsciiLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}