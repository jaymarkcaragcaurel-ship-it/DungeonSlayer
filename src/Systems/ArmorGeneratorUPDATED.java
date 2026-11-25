package Armors;

import Data.ArmorData;
import Gear.GearGenerator;
import Gear.GearProfile;
import Gear.GearRoll;
import Gear.GearTable;
import Player.Stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Generates class-tailored armor. The pipeline itself lives in {@link GearGenerator};
 * this class supplies the armor tables, handles slots, and turns rolls into {@link Armor}.
 * {@link ArmorData} only holds the constant tables.
 */
public final class ArmorGenerator {

    private ArmorGenerator() {}

    /** Armor tables. Gold values per modifier slot: ATK, CRIT, SPD, DEF, MP, HP. */
    private static final GearTable TABLE = new GearTable(
        ArmorData.AFFINITIES,
        ArmorData.AFFINITY_ARCHETYPES,
        ArmorData.AFFINITY_WEIGHTS,
        ArmorData.FORBIDDEN_MAP,
        ArmorData.BASE_POOL,
        ArmorData.PREFIX_POOL,
        ArmorData.SUFFIX_POOL,
        ArmorData.TIER_WEIGHT_BANDS,
        ArmorData.STAT_REQ_MAP,
        4.0,
        new double[] { 1.0, 1.5, 0.8, 1.8, 0.7, 1.2 });

    public static final int TIER_COUNT = TABLE.tierCount();

    /** Rerolls allowed per piece before the DEF floor is applied directly (see rollChecked). */
    private static final int MAX_DEF_REROLLS = 20;

    /** Rerolls allowed per stock slot to avoid two identically named pieces in one shop. */
    private static final int MAX_DISTINCT_REROLLS = 8;

    // Fail at class load, not mid-game when a bad row happens to be rolled.
    static { validateData(); }

    /**
     * Forces this class to load, which runs the data validation. Call once at game startup so a
     * bad table fails immediately instead of on the first shop visit (class loading is lazy).
     */
    public static void verifyData() { }

    public static GearProfile profileFor(int classIndex) {
        return GearGenerator.profileFor(TABLE, classIndex);
    }

    /**
     * A shop's worth of armor: one piece of each tier plus random extras (total MIN..MAX_ARMORS).
     *
     * Pieces in one stock get different display names whenever the tables allow it (a few
     * rerolls are tried, then a repeat is accepted rather than looping forever).
     *
     * Every slot is guaranteed to be stocked. Bases are chosen without regard to slot, and the
     * class filters thin the 12 head / 13 body bases unevenly, so a plain roll can occasionally
     * leave the shop with no helmet (or no body piece). If a slot is missing, one random-tier
     * extra is replaced by a piece of that slot (same tier as the piece it replaces). Only extras
     * whose slot appears more than once are candidates, so a repair can never remove the last
     * piece of another slot. A stock with no replaceable extra gains one piece instead.
     */
    public static List<Armor> generateStock(int classIndex, Random rng) {
        Objects.requireNonNull(rng, "rng");
        GearProfile profile = profileFor(classIndex);
        Set<String> seen = new HashSet<>();

        List<Armor> stock = GearGenerator.buildStock(
            ArmorData.MIN_ARMORS, ArmorData.MAX_ARMORS, TIER_COUNT, rng,
            tier -> rollDistinct(profile, tier, null, rng, seen));

        for (ArmorSlot slot : ArmorSlot.values()) {
            if (hasSlot(stock, slot)) continue;

            List<Integer> replaceable = new ArrayList<>();
            for (int i = TIER_COUNT; i < stock.size(); i++) {
                if (countSlot(stock, stock.get(i).slot()) > 1) replaceable.add(i);
            }

            if (!replaceable.isEmpty()) {
                int index = replaceable.get(rng.nextInt(replaceable.size()));
                int tier = stock.get(index).tier();
                stock.set(index, rollDistinct(profile, tier, slot, rng, seen));
            } else {
                stock.add(rollDistinct(profile, 1 + rng.nextInt(TIER_COUNT), slot, rng, seen));
            }
        }
        return stock;
    }

    public static List<Armor> generateStock(int classIndex) {
        return generateStock(classIndex, new Random());
    }

    /** Rolls one armor piece of the given tier, in any slot. Its total DEF bonus is never negative. */
    public static Armor roll(GearProfile profile, int tier, Random rng) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(rng, "rng");
        return rollChecked(profile, tier, base -> true, rng);
    }

    /**
     * Rolls one armor piece of the given tier for a specific slot (e.g. for a helmet drop).
     * Its total DEF bonus is never negative.
     */
    public static Armor roll(GearProfile profile, int tier, ArmorSlot slot, Random rng) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(rng, "rng");
        return rollChecked(profile, tier, base -> slotOf(base) == slot, rng);
    }

    /**
     * Rolls a piece (any slot when {@code slot} is null) whose display name is not yet in
     * {@code seen}, and records the name. After {@link #MAX_DISTINCT_REROLLS} attempts the last
     * roll is returned even if it repeats a name.
     */
    private static Armor rollDistinct(GearProfile profile, int tier, ArmorSlot slot,
                                      Random rng, Set<String> seen) {
        Armor piece = null;
        for (int attempt = 0; attempt < MAX_DISTINCT_REROLLS; attempt++) {
            piece = slot == null ? roll(profile, tier, rng) : roll(profile, tier, slot, rng);
            if (seen.add(piece.displayName())) return piece;
        }
        return piece;
    }

    /**
     * Armor must never make the wearer less defended. A low base combined with a prefix and
     * suffix that both carry DEF penalties can total below zero, so such rolls are discarded
     * and rolled again. Still deterministic for a given seed. If every attempt is negative
     * (practically impossible with the current tables) the least negative roll is kept and
     * {@link #build} lifts its base defense to cancel the deficit, so the result is always
     * valid and the game never fails mid-shop.
     */
    private static Armor rollChecked(GearProfile profile, int tier,
                                     Predicate<Object[]> baseFilter, Random rng) {
        GearRoll best = null;
        double bestDefense = Double.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < MAX_DEF_REROLLS; attempt++) {
            GearRoll candidate = GearGenerator.roll(TABLE, profile, tier, baseFilter, rng);
            double def = defenseTotal(candidate);
            if (round2(def) >= 0.0) return build(candidate);
            if (def > bestDefense) {
                best = candidate;
                bestDefense = def;
            }
        }
        return build(best);
    }

    /** Base defense plus any DEF modifier of the roll: what the piece would add to DEFENSE. */
    private static double defenseTotal(GearRoll r) {
        return r.value() + GearGenerator.modifierMap(r.modifiers()).getOrDefault(Stat.DEFENSE, 0.0);
    }

    private static Armor build(GearRoll r) {
        Map<Stat, Double> modifiers = GearGenerator.modifierMap(r.modifiers());

        // Safety net for the "armor never lowers DEF" rule (see rollChecked).
        double defense = r.value();
        double defenseModifier = modifiers.getOrDefault(Stat.DEFENSE, 0.0);
        if (round2(defense + defenseModifier) < 0.0) defense = round2(-defenseModifier);

        return new Armor(
            r.prefixLabel(),
            r.baseName(),
            r.suffixLabel(),
            ArmorData.AFFINITIES[r.affinity()],
            slotOf(r.base()),
            r.tier(),
            defense,
            modifiers,
            r.price());
    }

    private static ArmorSlot slotOf(Object[] base) {
        return ArmorSlot.fromData((String) base[ArmorData.BASE_SLOT]);
    }

    private static boolean hasSlot(List<Armor> stock, ArmorSlot slot) {
        return countSlot(stock, slot) > 0;
    }

    private static int countSlot(List<Armor> stock, ArmorSlot slot) {
        int n = 0;
        for (Armor a : stock) if (a.slot() == slot) n++;
        return n;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ── Data validation (runs once, at class load) ───────────────────────────

    /**
     * Checks only what GearTable cannot know. Its constructor has already validated the table
     * shapes (array lengths, affinity / bias / stat-requirement ranges, min <= max) by the time
     * this runs, so those checks are not repeated here.
     */
    private static void validateData() {
        if (TIER_COUNT != Armor.TIER_NAMES.size()) {
            throw new IllegalStateException("TIER_COUNT (" + TIER_COUNT
                + ") does not match Armor.TIER_NAMES (" + Armor.TIER_NAMES.size() + ")");
        }

        // Rolled modifier arrays are turned into Stat maps through Stat.MODIFIER_ORDER.
        if (TABLE.modifierCount() > Stat.MODIFIER_ORDER.size()) {
            throw new IllegalStateException("Armor tables have " + TABLE.modifierCount()
                + " modifier slots but Stat.MODIFIER_ORDER only names " + Stat.MODIFIER_ORDER.size());
        }

        // A shop must be able to pick a stock size.
        if (ArmorData.MIN_ARMORS < 1 || ArmorData.MAX_ARMORS < ArmorData.MIN_ARMORS) {
            throw new IllegalStateException("Invalid shop stock size: MIN_ARMORS="
                + ArmorData.MIN_ARMORS + ", MAX_ARMORS=" + ArmorData.MAX_ARMORS);
        }

        // Every base must name a real slot, and every slot must have at least one base.
        boolean[] slotStocked = new boolean[ArmorSlot.values().length];
        for (Object[] base : ArmorData.BASE_POOL) {
            slotStocked[slotOf(base).ordinal()] = true;   // throws on an unknown slot string
        }
        for (ArmorSlot slot : ArmorSlot.values()) {
            if (!slotStocked[slot.ordinal()]) {
                throw new IllegalStateException("No base item exists for slot " + slot);
            }
        }

        // GearTable does not check the rarity bands themselves.
        for (int t = 0; t < TIER_COUNT; t++) {
            double[] band = TABLE.tierBands()[t];
            if (band.length != 2 || band[0] > band[1]) {
                throw new IllegalStateException("Tier " + (t + 1)
                    + " needs a rarity band {min, max} with min <= max");
            }
        }

        checkTierCoverage("prefix", TABLE.prefixPool());
        checkTierCoverage("suffix", TABLE.suffixPool());
    }

    /**
     * Every tier needs at least one modifier inside its rarity band. The generator would
     * otherwise throw the first time that tier is rolled, possibly mid-game.
     */
    private static void checkTierCoverage(String kind, Object[][] pool) {
        int[] perTier = new int[TIER_COUNT];

        for (Object[] mod : pool) {
            double weight = (double) mod[GearTable.MOD_WEIGHT];
            for (int t = 0; t < TIER_COUNT; t++) {
                double[] band = TABLE.tierBands()[t];
                if (weight >= band[0] && weight <= band[1]) perTier[t]++;
            }
        }
        for (int t = 0; t < TIER_COUNT; t++) {
            if (perTier[t] == 0) {
                throw new IllegalStateException("No " + kind + " falls inside tier " + (t + 1) + "'s weight band");
            }
        }
    }
}
