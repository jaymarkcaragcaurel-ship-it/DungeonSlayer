package Weapons;

import Data.WeaponsData;
import Gear.GearGenerator;
import Gear.GearProfile;
import Gear.GearRoll;
import Gear.GearTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Generates class-tailored weapons. The roll pipeline itself lives in {@link GearGenerator};
 * this class supplies the weapon tables, validates them at startup, prices the result, and
 * builds shop stocks.
 */
public final class WeaponGenerator {

    private WeaponGenerator() {}

    // ── Tuning ───────────────────────────────────────────────────────────────

    /** Gold per point of rolled base attack. */
    private static final double GOLD_PER_ATTACK = 3.5;

    /**
     * Gold per point of each modifier slot: ATK, CRIT, SPD, DEF, MP. Modifier ATK is worth the
     * same as base ATK, so a weapon's price follows its real power.
     */
    private static final double[] STAT_GOLD_VALUES = { 3.5, 4.0, 2.5, 2.5, 1.5 };

    /** Price multiplier per tier (Common .. Mythic). */
    private static final double[] TIER_PRICE_MULTIPLIERS = { 1.0, 1.25, 1.6, 2.2, 3.0 };

    /** Markup when the weapon's family is the class's closest / second-closest affinity. */
    private static final double PRIMARY_DEMAND   = 1.10;
    private static final double SECONDARY_DEMAND = 1.05;

    private static final int MIN_PRICE  = 15;
    private static final int PRICE_STEP = 5;

    /** Stock: tiers 1..GUARANTEED_TIERS always appear; the rest of the shelf is drawn by weight. */
    private static final int GUARANTEED_TIERS = 3;
    private static final int[] STOCK_TIER_WEIGHTS = { 45, 30, 15, 8, 2 };

    /** Re-roll limits. Both keep the same Random, so a seed still reproduces results. */
    private static final int MAX_DUPLICATE_RETRIES = 10;
    private static final int MAX_NAME_RETRIES = 20;

    /** Words that don't count when checking a name for repeats. */
    private static final Set<String> NAME_STOPWORDS = Set.of("of", "the");

    // ── Table (validated before it is built) ─────────────────────────────────

    private static final GearTable TABLE = buildTable();

    public static final int TIER_COUNT = TABLE.tierCount();

    private static GearTable buildTable() {
        validateData();
        return new GearTable(
            WeaponsData.AFFINITIES,
            WeaponsData.AFFINITY_ARCHETYPES,
            WeaponsData.AFFINITY_WEIGHTS,
            WeaponsData.FORBIDDEN_MAP,
            WeaponsData.BASE_POOL,
            WeaponsData.PREFIX_POOL,
            WeaponsData.SUFFIX_POOL,
            WeaponsData.TIER_WEIGHT_BANDS,
            WeaponsData.STAT_REQ_MAP,
            GOLD_PER_ATTACK,
            STAT_GOLD_VALUES);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public static GearProfile profileFor(int classIndex) {
        if (classIndex < 0) {
            throw new IllegalArgumentException(
                "classIndex must be >= 0 (has a class been applied to the player?), got " + classIndex);
        }
        return GearGenerator.profileFor(TABLE, classIndex);
    }

    /**
     * A shop's worth of weapons, sorted by tier then price. Tiers 1..3 are always stocked;
     * the remaining slots (total MIN..MAX_WEAPONS) are drawn by rarity, so Legendary and Mythic
     * pieces are occasional finds rather than permanent fixtures.
     * Identical weapons are re-rolled a few times so a stock rarely shows the same item twice.
     */
    public static List<Weapon> generateStock(int classIndex, Random rng) {
        Objects.requireNonNull(rng, "rng");
        GearProfile profile = profileFor(classIndex);

        int count = WeaponsData.MIN_WEAPONS
            + rng.nextInt(WeaponsData.MAX_WEAPONS - WeaponsData.MIN_WEAPONS + 1);

        Set<String> seen = new HashSet<>();
        List<Weapon> stock = new ArrayList<>(count);

        for (int tier = 1; tier <= GUARANTEED_TIERS; tier++) {
            stock.add(rollUnique(profile, tier, rng, seen));
        }
        while (stock.size() < count) {
            stock.add(rollUnique(profile, randomStockTier(rng), rng, seen));
        }

        stock.sort(Comparator.comparingInt(Weapon::tier).thenComparingInt(Weapon::price));
        return stock;
    }

    public static List<Weapon> generateStock(int classIndex) {
        return generateStock(classIndex, new Random());
    }

    /** Rolls one weapon of the given tier (1..TIER_COUNT) for the given class profile. */
    public static Weapon roll(GearProfile profile, int tier, Random rng) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(rng, "rng");

        GearRoll r = rollWithoutRepeatedWords(profile, tier, rng);

        return new Weapon(
            r.prefixLabel(),
            r.baseName(),
            r.suffixLabel(),
            TABLE.affinities()[r.affinity()],
            r.tier(),
            r.value(),
            GearGenerator.modifierMap(r.modifiers()),
            priceFor(profile, r));
    }

    // ── Rolling helpers ──────────────────────────────────────────────────────

    private static Weapon rollUnique(GearProfile profile, int tier, Random rng, Set<String> seen) {
        Weapon weapon = roll(profile, tier, rng);
        for (int attempt = 0; attempt < MAX_DUPLICATE_RETRIES && !seen.add(weapon.displayName()); attempt++) {
            weapon = roll(profile, tier, rng);
        }
        seen.add(weapon.displayName());
        return weapon;
    }

    private static int randomStockTier(Random rng) {
        int total = 0;
        for (int w : STOCK_TIER_WEIGHTS) total += w;

        int pick = rng.nextInt(total);
        for (int tier = 1; tier <= STOCK_TIER_WEIGHTS.length; tier++) {
            pick -= STOCK_TIER_WEIGHTS[tier - 1];
            if (pick < 0) return tier;
        }
        return STOCK_TIER_WEIGHTS.length;
    }

    /**
     * Re-rolls names like "Swift Dagger of Swiftness" or "Storm Staff of the Storm". The shared
     * generator already avoids prefix/base clashes; this also covers prefix/suffix and base/suffix.
     */
    private static GearRoll rollWithoutRepeatedWords(GearProfile profile, int tier, Random rng) {
        GearRoll r = null;
        for (int attempt = 0; attempt <= MAX_NAME_RETRIES; attempt++) {
            r = GearGenerator.roll(TABLE, profile, tier, base -> true, rng);
            if (!hasRepeatedWord(r)) break;
        }
        return r;
    }

    private static boolean hasRepeatedWord(GearRoll r) {
        List<String> prefix = words(r.prefixLabel());
        List<String> base   = words(r.baseName());
        List<String> suffix = words(r.suffixLabel());
        return overlaps(prefix, base) || overlaps(prefix, suffix) || overlaps(base, suffix);
    }

    private static boolean overlaps(List<String> a, List<String> b) {
        for (String x : a) {
            for (String y : b) {
                if (similar(x, y)) return true;
            }
        }
        return false;
    }

    /** Equal, or sharing a 5+ letter stem ("swift"/"swiftness", "ancient"/"ancients"). */
    private static boolean similar(String a, String b) {
        if (a.equals(b)) return true;
        int common = 0;
        int max = Math.min(a.length(), b.length());
        while (common < max && a.charAt(common) == b.charAt(common)) common++;
        return common >= 5;
    }

    private static List<String> words(String label) {
        List<String> result = new ArrayList<>();
        // Locale.ROOT: under a Turkish locale "Iron".toLowerCase() gives a dotless i and breaks the split.
        for (String word : label.toLowerCase(Locale.ROOT).split("[^a-z]+")) {
            if (!word.isEmpty() && !NAME_STOPWORDS.contains(word)) result.add(word);
        }
        return result;
    }

    // ── Pricing ──────────────────────────────────────────────────────────────

    /**
     * Price from real power: (base attack + every modifier, each at its gold value) * tier
     * multiplier * class demand, rounded to PRICE_STEP. Replaces the shared generator's price,
     * which capped the stat bonus at 40 gold and made high tiers cheap.
     */
    private static int priceFor(GearProfile profile, GearRoll r) {
        double raw = r.value() * GOLD_PER_ATTACK;
        double[] modifiers = r.modifiers();
        for (int i = 0; i < modifiers.length; i++) raw += modifiers[i] * STAT_GOLD_VALUES[i];

        raw *= TIER_PRICE_MULTIPLIERS[r.tier() - 1];

        if (r.affinity() == profile.primary())        raw *= PRIMARY_DEMAND;
        else if (r.affinity() == profile.secondary()) raw *= SECONDARY_DEMAND;

        int rounded = (int) (Math.round(raw / PRICE_STEP) * PRICE_STEP);
        return Math.max(MIN_PRICE, rounded);
    }

    // ── Startup validation ───────────────────────────────────────────────────

    /**
     * Checks the weapon tables so a data typo fails on class load with a clear message, instead
     * of a ClassCastException or an empty pool in the middle of a game.
     */
    private static void validateData() {
        int slots = WeaponsData.STAT_REQ_MAP.length;
        int tiers = WeaponsData.TIER_WEIGHT_BANDS.length;

        check(WeaponsData.STAT_LABELS.length == slots, "STAT_LABELS length != STAT_REQ_MAP length");
        check(STAT_GOLD_VALUES.length == slots, "STAT_GOLD_VALUES length != modifier slots");
        check(tiers == Weapon.TIER_NAMES.length,
            "tier bands (" + tiers + ") != Weapon.TIER_NAMES (" + Weapon.TIER_NAMES.length + ")");
        check(TIER_PRICE_MULTIPLIERS.length == tiers, "TIER_PRICE_MULTIPLIERS length != tier count");
        check(STOCK_TIER_WEIGHTS.length == tiers, "STOCK_TIER_WEIGHTS length != tier count");
        check(GUARANTEED_TIERS >= 1 && GUARANTEED_TIERS <= tiers, "GUARANTEED_TIERS out of range");
        check(WeaponsData.MIN_WEAPONS <= WeaponsData.MAX_WEAPONS, "MIN_WEAPONS > MAX_WEAPONS");
        check(WeaponsData.MIN_WEAPONS >= GUARANTEED_TIERS, "MIN_WEAPONS < GUARANTEED_TIERS");

        for (int t = 0; t < tiers; t++) {
            double[] band = WeaponsData.TIER_WEIGHT_BANDS[t];
            check(band.length == 2 && band[0] <= band[1], "tier " + (t + 1) + " band must be {min, max} with min <= max");
        }

        boolean[] affinityHasBase = new boolean[WeaponsData.AFFINITIES.length];
        for (Object[] base : WeaponsData.BASE_POOL) {
            check(base.length >= 6, "base entry too short: " + describeEntry(base));
            check(base[0] instanceof String, "base name must be a String: " + describeEntry(base));
            check(base[1] instanceof Double && base[2] instanceof Double,
                "base " + base[0] + ": min/max attack must be doubles (write 10.0, not 10)");
            check(base[3] instanceof Integer && base[5] instanceof Integer,
                "base " + base[0] + ": affinity and bias must be ints");
            int affinity = (Integer) base[3];
            if (affinity >= 0 && affinity < affinityHasBase.length) affinityHasBase[affinity] = true;
        }
        for (int a = 0; a < affinityHasBase.length; a++) {
            check(affinityHasBase[a], "affinity " + WeaponsData.AFFINITIES[a] + " has no base weapon");
        }

        validateModifiers("prefix", WeaponsData.PREFIX_POOL, slots, tiers);
        validateModifiers("suffix", WeaponsData.SUFFIX_POOL, slots, tiers);
    }

    private static void validateModifiers(String kind, Object[][] pool, int slots, int tiers) {
        int[] perTier = new int[tiers];

        for (Object[] mod : pool) {
            check(mod.length >= 6, kind + " entry too short: " + describeEntry(mod));
            check(mod[0] instanceof String, kind + " label must be a String: " + describeEntry(mod));
            String label = (String) mod[0];
            check(mod[1] instanceof double[] && ((double[]) mod[1]).length == slots,
                kind + " " + label + ": stats must be a double[" + slots + "]");
            check(mod[2] instanceof Double, kind + " " + label + ": weight must be a double");
            check(mod[4] instanceof Integer, kind + " " + label + ": statReq must be an int");
            check(mod[5] instanceof Double, kind + " " + label + ": threshold must be a double (write 0.0, not 0)");

            double weight = (Double) mod[2];
            int bands = 0;
            for (int t = 0; t < tiers; t++) {
                double[] band = WeaponsData.TIER_WEIGHT_BANDS[t];
                if (weight >= band[0] && weight <= band[1]) {
                    perTier[t]++;
                    bands++;
                }
            }
            check(bands >= 1, kind + " " + label + ": weight " + weight + " falls inside no tier band, so it can never roll");
        }

        for (int t = 0; t < tiers; t++) {
            check(perTier[t] > 0, "no " + kind + " has a weight inside tier " + (t + 1) + "'s band");
        }
    }

    private static String describeEntry(Object[] entry) {
        return entry.length > 0 ? String.valueOf(entry[0]) : "(empty)";
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Invalid weapon data: " + message);
    }
}
