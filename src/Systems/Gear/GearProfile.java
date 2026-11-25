package Systems.Gear;

import java.util.Set;

/**
 * What the generator knows about one class for one gear family.
 *
 * @param primary       index of the closest affinity
 * @param secondary     index of the second-closest affinity
 * @param primaryName   name of the primary affinity
 * @param secondaryName name of the secondary affinity
 * @param forbidden     affinities this class never rolls (primary/secondary excluded)
 * @param normalized    class stats scaled 0..1 (HP, MP, SPD, ATK, DEF, CRIT, CRIT_MULT)
 * @param classMods     class strength per modifier slot (same order as the modifier arrays)
 */
public record GearProfile(
        int primary,
        int secondary,
        String primaryName,
        String secondaryName,
        Set<Integer> forbidden,
        double[] normalized,
        double[] classMods) {
}