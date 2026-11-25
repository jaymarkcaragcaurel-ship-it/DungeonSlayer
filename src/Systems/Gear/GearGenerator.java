package Systems.Gear;

import Systems.Core.ClassSystem;
import Systems.Player.Stat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static Systems.Gear.GearTable.*;

/**
 * The generation pipeline shared by weapons and armors.
 *
 *   1. profileFor   class stats -> nearest two affinities + forbidden affinities
 *   2. eligible*    drop forbidden bases; keep prefixes/suffixes in the tier's rarity band and
 *                   above their class-stat gate (with fallbacks, so pools never come up empty)
 *   3. score*       bias each candidate toward the class
 *   4. pick         weighted random choice of prefix, base, suffix
 *   5. roll         roll the base value, sum the modifiers, price it
 *
 * Pure logic: no printing, no input. All randomness comes from the Random passed in, and the
 * order of calls on it is fixed (prefix, base, suffix, value), so a seed reproduces results.
 */
public final class GearGenerator {

    private GearGenerator() {}

    /** Smallest score a candidate can have, so heavily penalised entries stay pickable. */
    private static final double MIN_SCORE = 1e-6;

    // ── Class profile ────────────────────────────────────────────────────────

    public static GearProfile profileFor(GearTable table, int classIndex) {
        double[] normalized = ClassSystem.normalizedStats(classIndex);
        int[] nearest = nearestAffinities(table, normalized);

        double[] classMods = new double[table.modifierCount()];
        for (int s = 0; s < classMods.length; s++) {
            classMods[s] = normalized[table.statReqMap()[s]];
        }

        return new GearProfile(
            nearest[0],
            nearest[1],
            table.affinities()[nearest[0]],
            table.affinities()[nearest[1]],
            forbiddenFor(table, nearest[0], nearest[1]),
            normalized,
            classMods);
    }

    /** Weighted euclidean distance to every archetype; returns {closest, second closest}. */
    private static int[] nearestAffinities(GearTable table, double[] normalized) {
        int primary = -1, secondary = -1;
        double best = Double.POSITIVE_INFINITY, second = Double.POSITIVE_INFINITY;

        for (int a = 0; a < table.archetypes().length; a++) {
            double sum = 0.0;
            for (int s = 0; s < normalized.length; s++) {
                double diff = normalized[s] - table.archetypes()[a][s];
                sum += table.affinityWeights()[s] * diff * diff;
            }
            double distance = Math.sqrt(sum);

            if (distance < best) {
                secondary = primary;
                second = best;
                primary = a;
                best = distance;
            } else if (distance < second) {
                secondary = a;
                second = distance;
            }
        }
        return new int[] { primary, secondary };
    }

    private static Set<Integer> forbiddenFor(GearTable table, int primary, int secondary) {
        Set<Integer> forbidden = new LinkedHashSet<>();
        for (int a : table.forbiddenMap()[primary])   forbidden.add(a);
        for (int a : table.forbiddenMap()[secondary]) forbidden.add(a);
        forbidden.remove(primary);
        forbidden.remove(secondary);
        return forbidden;
    }

    // ── Rolling ──────────────────────────────────────────────────────────────

    /**
     * Rolls one piece of gear.
     *
     * @param baseFilter extra restriction on bases (e.g. armor slot); use {@code b -> true} for none
     * @throws IllegalStateException if no base satisfies the filter at all
     */
    public static GearRoll roll(GearTable table, GearProfile profile, int tier,
                                Predicate<Object[]> baseFilter, Random rng) {
        if (tier < 1 || tier > table.tierCount()) {
            throw new IllegalArgumentException("Tier must be 1.." + table.tierCount() + ", got " + tier);
        }

        List<Object[]> bases    = eligibleBases(table, profile, baseFilter);
        List<Object[]> prefixes = eligibleModifiers(table, table.prefixPool(), profile, tier);
        List<Object[]> suffixes = eligibleModifiers(table, table.suffixPool(), profile, tier);

        Object[] prefix = pick(prefixes, scoreModifiers(prefixes, profile), rng);
        Object[] base   = pick(bases,    scoreBases(bases, profile),        rng);
        Object[] suffix = pick(suffixes, scoreModifiers(suffixes, profile), rng);

        // Avoid names like "Iron Iron Helm": if the prefix repeats a word of the base name,
        // re-pick the prefix from those that don't. (Draws from rng only when a clash occurs.)
        if (repeatsBaseName(prefix, base)) {
            List<Object[]> alternatives = new ArrayList<>(prefixes);
            alternatives.removeIf(p -> repeatsBaseName(p, base));
            if (!alternatives.isEmpty()) {
                prefix = pick(alternatives, scoreModifiers(alternatives, profile), rng);
            }
        }

        double min = (double) base[BASE_MIN];
        double max = (double) base[BASE_MAX];
        double value = round1(min + rng.nextDouble() * (max - min));

        double[] prefixMods = (double[]) prefix[MOD_STATS];
        double[] suffixMods = (double[]) suffix[MOD_STATS];
        double[] combined = new double[table.modifierCount()];
        for (int i = 0; i < combined.length; i++) combined[i] = prefixMods[i] + suffixMods[i];

        int affinity = (int) base[BASE_AFFINITY];
        int price = price(table, profile, value, prefix, suffix, affinity);

        return new GearRoll(prefix, base, suffix, tier, value, combined, affinity, price);
    }

    private static boolean repeatsBaseName(Object[] prefix, Object[] base) {
        String label = ((String) prefix[MOD_LABEL]).toLowerCase();
        for (String word : ((String) base[BASE_NAME]).toLowerCase().split("\\s+")) {
            if (word.equals(label)) return true;
        }
        return false;
    }

    /**
     * Builds a stock: one item of each tier, then random-tier extras up to a random total in
     * [minCount, maxCount].
     */
    public static <T> List<T> buildStock(int minCount, int maxCount, int tierCount,
                                         Random rng, IntFunction<T> rollForTier) {
        int count = minCount + rng.nextInt(maxCount - minCount + 1);
        count = Math.max(count, tierCount);

        List<T> stock = new ArrayList<>(count);
        for (int tier = 1; tier <= tierCount; tier++) {
            stock.add(rollForTier.apply(tier));
        }
        while (stock.size() < count) {
            stock.add(rollForTier.apply(1 + rng.nextInt(tierCount)));
        }
        return stock;
    }

    /** Converts a modifier array (ATK, CRIT, SPD, DEF, MP[, HP]) into a Stat map, dropping zeros. */
    public static Map<Stat, Double> modifierMap(double[] modifiers) {
        Map<Stat, Double> map = new EnumMap<>(Stat.class);
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] != 0.0) map.put(Stat.MODIFIER_ORDER.get(i), modifiers[i]);
        }
        return map;
    }

    // ── Filtering ────────────────────────────────────────────────────────────

    /**
     * Bases that pass the filter and whose affinity isn't forbidden. If that leaves none, the
     * forbidden list is ignored (the filter never is).
     */
    private static List<Object[]> eligibleBases(GearTable table, GearProfile profile,
                                                Predicate<Object[]> baseFilter) {
        List<Object[]> allowed = new ArrayList<>();
        List<Object[]> filteredOnly = new ArrayList<>();

        for (Object[] base : table.basePool()) {
            if (!baseFilter.test(base)) continue;
            filteredOnly.add(base);
            if (!profile.forbidden().contains((int) base[BASE_AFFINITY])) allowed.add(base);
        }

        if (!allowed.isEmpty()) return allowed;
        if (!filteredOnly.isEmpty()) return filteredOnly;
        throw new IllegalStateException("No base item satisfies the requested filter");
    }

    /**
     * Modifiers inside the tier's rarity band and above their class-stat gate.
     * If the gate removes everything, retries with the rarity band only.
     */
    private static List<Object[]> eligibleModifiers(GearTable table, Object[][] pool,
                                                    GearProfile profile, int tier) {
        double tierMin = table.tierBands()[tier - 1][0];
        double tierMax = table.tierBands()[tier - 1][1];

        List<Object[]> gated = new ArrayList<>();
        List<Object[]> inBand = new ArrayList<>();

        for (Object[] mod : pool) {
            double weight = (double) mod[MOD_WEIGHT];
            if (weight < tierMin || weight > tierMax) continue;
            inBand.add(mod);

            int req = (int) mod[MOD_STAT_REQ];
            double threshold = (double) mod[MOD_THRESHOLD];
            if (req == -1 || profile.classMods()[req] >= threshold) gated.add(mod);
        }

        List<Object[]> result = gated.isEmpty() ? inBand : gated;
        if (result.isEmpty()) {
            throw new IllegalStateException("No modifier has a rarity weight inside tier " + tier + "'s band");
        }
        return result;
    }

    // ── Scoring ──────────────────────────────────────────────────────────────

    /** score = rarity weight * (1 + alignment with the class). */
    private static double[] scoreModifiers(List<Object[]> mods, GearProfile profile) {
        double[] scores = new double[mods.size()];
        for (int i = 0; i < scores.length; i++) {
            double[] stats = (double[]) mods.get(i)[MOD_STATS];
            double weight  = (double) mods.get(i)[MOD_WEIGHT];

            double maxMod = 0;
            for (double m : stats) maxMod = Math.max(maxMod, Math.abs(m));

            double alignment = 0;
            for (int s = 0; s < stats.length; s++) {
                double scaled = maxMod > 0 ? stats[s] / maxMod : 0;
                alignment += scaled * profile.classMods()[s];
            }
            scores[i] = Math.max(MIN_SCORE, weight * (1 + alignment));
        }
        return scores;
    }

    private static double[] scoreBases(List<Object[]> bases, GearProfile profile) {
        double[] scores = new double[bases.size()];
        for (int i = 0; i < scores.length; i++) {
            int bias = (int) bases.get(i)[BASE_BIAS];
            scores[i] = 1.0 + profile.classMods()[bias];
        }
        return scores;
    }

    private static <T> T pick(List<T> items, double[] scores, Random rng) {
        double total = 0;
        for (double s : scores) total += s;

        double roll = rng.nextDouble() * total;
        double running = 0;
        for (int i = 0; i < scores.length; i++) {
            running += scores[i];
            if (running > roll) return items.get(i);
        }
        return items.get(items.size() - 1);   // floating-point edge case
    }

    // ── Pricing ──────────────────────────────────────────────────────────────

    private static int price(GearTable table, GearProfile profile, double value,
                             Object[] prefix, Object[] suffix, int affinity) {
        double[] prefixMods = (double[]) prefix[MOD_STATS];
        double[] suffixMods = (double[]) suffix[MOD_STATS];
        double prefixWeight = (double) prefix[MOD_WEIGHT];
        double suffixWeight = (double) suffix[MOD_WEIGHT];

        double basePrice   = value * table.valueMultiplier();
        double rarityScore = ((1.0 - prefixWeight) + (1.0 - suffixWeight)) / 2.0;
        double rarityMult  = 1.0 + rarityScore * 0.45;

        double statBonus = 0;
        for (int s = 0; s < prefixMods.length; s++) {
            statBonus += (prefixMods[s] + suffixMods[s]) * table.statGoldValues()[s];
        }
        statBonus = Math.max(-20.0, Math.min(statBonus, 40.0));

        double demand = 1.0;
        if (affinity == profile.primary())        demand = 1.10;
        else if (affinity == profile.secondary()) demand = 1.05;

        double raw = Math.max(15.0, (basePrice + statBonus) * rarityMult * demand);
        return (int) (Math.round(raw / 5.0) * 5);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}