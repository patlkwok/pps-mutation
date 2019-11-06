package mutation.g3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import static mutation.g3.LogProbability.*;
import static mutation.g3.RuleInferenceEngine.simplifyRule;

/**
 *
 * @author group 3
 */
public class MutationBasedDistribution implements RuleDistribution {

    protected final List<Map<Byte, Double>> pis;
    protected final List<Map<Character, Double>> ais;
    protected final List<Map<Byte, Double>> topPis;
    protected final List<Map<Character, Double>> topAis;
    private int[] optimalConfig;
    protected double highestLogLikelihood = Double.NEGATIVE_INFINITY;
    private double[] topProbabilities;
    private double[] secondTopProbabilities;
    private double sameAsZeroThreshold = -100;

    public MutationBasedDistribution(
            List<Map<Byte, Double>> pis,
            List<Map<Character, Double>> ais) {
        this.pis = pis;
        this.ais = ais;
        this.topPis = new ArrayList<>(pis.size());
        this.topAis = new ArrayList<>(ais.size());
        filterTopRuleSpace();
        computeHighestLogLikelihood();
    }

    @Override
    public double getLogLikelihood(Rule r) {
        double lp = 0; //probability 1
        final byte[] pattern = r.getPattern();
        final char[] action = r.getAction().toCharArray();
        for (int i = 0; i < action.length; i++) {
            final byte piByte;
            if (i > pattern.length - 1) {
                piByte = 0b1111;
            } else {
                piByte = pattern[i];
            }
            Double prob = pis.get(i).get(piByte);
            if (prob == null) {
                return LOG_ZERO_PROB;
            }
            lp += prob; //multiplying in log space
        }
        for (int i = 0; i < action.length; i++) {
            Double prob = ais.get(i).get(action[i]);
            if (prob == null) {
                return LOG_ZERO_PROB;
            }
            lp += prob; //multiplying in log space
        }
        return lp;
    }

    @Override
    public double getPiLikelihood(int i, byte pi) {
        return pis.get(i).getOrDefault(pi, LOG_ZERO_PROB);
    }

    @Override
    public double getAiLikelihood(int i, char ai) {
        return ais.get(i).getOrDefault(ai, LOG_ZERO_PROB);
    }

    @Override
    public double getHighestLogLikelihood() {
        return highestLogLikelihood;
    }

    @Override
    public Set<Rule> getMostLikelyRules() {
        if (optimalConfig == null) {
            optimalConfig = new int[pis.size() + ais.size()];
            for (int i = 0; i < optimalConfig.length; i++) {
                optimalConfig[i] = 0;
            }
        }
        return getMostLikelyRules(optimalConfig);
    }

    protected Set<Rule> getMostLikelyRules(int[] suboptimalConfig) {
        Set<Rule> rules = new HashSet<>();
        if (highestLogLikelihood == Double.NEGATIVE_INFINITY) {
            return rules;
        }
        makeRules(rules, 0, new byte[pis.size()], "", suboptimalConfig);
        return rules;
    }

    @Override
    public Set<Rule> getMostLikelyRules(Set<Rule> exceptions) {
        if (exceptions.isEmpty()) {
            return getMostLikelyRules();
        }
        int[] suboptimalConfig = new int[pis.size() + ais.size()];
        for (int i = 0; i < suboptimalConfig.length; i++) {
            suboptimalConfig[i] = 0;
        }
        Set<Rule> rules = getMostLikelyRules(suboptimalConfig);
        if (highestLogLikelihood == Double.NEGATIVE_INFINITY) {
            return rules;
        }
        rules.removeIf((r) -> exceptions.contains(r));
        if (!rules.isEmpty()) {
            return rules;
        }
        int[] diffValueCounts = getDiffValueCounts();
        while (rules.isEmpty() && nextConfiguration(suboptimalConfig, diffValueCounts)) {
            rules = getMostLikelyRules(suboptimalConfig);
            rules.removeIf((r) -> exceptions.contains(r));
        }
        return rules;
    }

    @Override
    public void ruleOut(Set<Rule> rules) {
        if (rules.isEmpty()) {
            return;
        }
        Set<Rule> mostLikely = getMostLikelyRules(rules);
        while (mostLikely.isEmpty() && highestLogLikelihood > Double.NEGATIVE_INFINITY) {
            double lessDiff = Double.MAX_VALUE;
            int lessDiffIdx = -1;
            for (int i = 0; i < topProbabilities.length; i++) {
                final double iDiff = topProbabilities[i] - secondTopProbabilities[i];
                if (iDiff < lessDiff) {
                    lessDiff = iDiff;
                    lessDiffIdx = i;
                }
            }
            if (lessDiffIdx != -1) {
                if (lessDiffIdx < pis.size()) {
                    double remValue = topPis.get(lessDiffIdx).values().iterator().next();
                    pis.get(lessDiffIdx).entrySet().removeIf(e -> e.getValue().equals(remValue));
                    filterTopPattern(lessDiffIdx);
                } else if (lessDiffIdx < pis.size() + ais.size()) {
                    final int pisSize = pis.size();
                    double remValue = topAis.get(lessDiffIdx - pisSize).values().iterator().next();
                    ais.get(lessDiffIdx - pisSize).entrySet().removeIf(e -> e.getValue().equals(remValue));
                    filterTopAction(lessDiffIdx - pisSize);
                }
            }
            computeHighestLogLikelihood();
            mostLikely = getMostLikelyRules(rules);
        }
    }

    protected final void filterTopRuleSpace() {
        this.topProbabilities = new double[pis.size() + ais.size()];
        this.secondTopProbabilities = new double[pis.size() + ais.size()];
        if (topPis.isEmpty()) {
            for (int i = 0; i < pis.size(); i++) {
                topPis.add(null);
            }
        }
        if (topAis.isEmpty()) {
            for (int i = 0; i < ais.size(); i++) {
                topAis.add(null);
            }
        }
        for (int i = 0; i < pis.size(); i++) {
            filterTopPattern(i);
        }

        for (int i = 0; i < ais.size(); i++) {
            filterTopAction(i);
        }
    }

    private void filterTopPattern(int idx) {
        double hP = LOG_ZERO_PROB;
        topProbabilities[idx] = LOG_ZERO_PROB;
        secondTopProbabilities[idx] = LOG_ZERO_PROB;
        Map<Byte, Double> tpi = new LinkedHashMap<>();
        for (Entry<Byte, Double> pi : pis.get(idx).entrySet()) {
            if (pi.getValue() >= hP) {
                tpi.put(pi.getKey(), pi.getValue());
                hP = pi.getValue();
                topProbabilities[idx] = hP;
            } else {
                secondTopProbabilities[idx] = pi.getValue();
                break;
            }
        }
        topPis.set(idx, tpi);
    }

    private void filterTopAction(int idx) {
        final int pisSize = pis.size();
        double hP = LOG_ZERO_PROB;
        topProbabilities[idx + pisSize] = LOG_ZERO_PROB;
        secondTopProbabilities[idx + pisSize] = LOG_ZERO_PROB;
        Map<Character, Double> tai = new LinkedHashMap<>();
        for (Entry<Character, Double> ai : ais.get(idx).entrySet()) {
            if (ai.getValue() >= hP) {
                tai.put(ai.getKey(), ai.getValue());
                hP = ai.getValue();
                topProbabilities[idx + pisSize] = hP;
            } else {
                secondTopProbabilities[idx + pisSize] = ai.getValue();
                break;
            }
        }
        topAis.set(idx, tai);
    }

    /**
     * Populates a map of rules recursively
     *
     * @param bucket map where the rules and their probabilities should be added
     * @param step the element of the pattern currently under construction
     * @param partialPattern a partial pattern being constructed
     * @param partialAction the probability associated with the rule so far
     * @param suboptimalConfig indicates at each position of the pattern if
     * instead of the elements with the highest probability the seconds, thirds,
     * etc should be taken instead
     */
    protected void makeRules(
            Set<Rule> bucket,
            int step,
            byte[] partialPattern,
            String partialAction,
            int[] suboptimalConfig) {
        final int patternL = topPis.size();
        if (step < patternL) {
            // add another pattern element
            Set<Byte> options;
            if (suboptimalConfig[step] == 0) {
                options = topPis.get(step).keySet();
            } else {
                options = getSuboptimal(pis.get(step), suboptimalConfig[step]);
            }
            for (Byte pi : options) {
                partialPattern[step] = pi;
                makeRules(bucket, step + 1, partialPattern, partialAction, suboptimalConfig);
            }
        } else if (step < patternL + topAis.size()) {
            // add another action element
            Set<Character> options;
            if (suboptimalConfig[step] == 0) {
                options = topAis.get(step - patternL).keySet();
            } else {
                options = getSuboptimal(ais.get(step - patternL), suboptimalConfig[step]);
            }
            for (Character ai : options) {
                makeRules(bucket, step + 1, partialPattern, partialAction + ai, suboptimalConfig);
            }
        } else {
            // rule is full
            byte[] pattern = new byte[partialPattern.length];
            System.arraycopy(partialPattern, 0, pattern, 0, pattern.length);
            bucket.add(simplifyRule(new Rule(pattern, partialAction)));
        }
    }

    protected final void computeHighestLogLikelihood() {
        double hLL = 1;
        for (Map<Byte, Double> pi : pis) {
            if (pi.isEmpty()) {
                hLL = LOG_ZERO_PROB;
                break;
            }
            hLL = logPMult(hLL, pi.values().iterator().next());
        }
        for (Map<Character, Double> ai : ais) {
            if (ai.isEmpty()) {
                hLL = LOG_ZERO_PROB;
                break;
            }
            hLL = logPMult(hLL, ai.values().iterator().next());
        }
        if (Double.isNaN(hLL)) {
            System.out.println("Here");
        }
        highestLogLikelihood = hLL;
    }

    private <T> Set<T> getSuboptimal(Map<T, Double> pMap, int suboptimal) {
        double p = 1;
        Set<T> values = new HashSet<>();
        for (Entry<T, Double> entry : pMap.entrySet()) {
            if (suboptimal == 0) {
                values.add(entry.getKey());
            }
            if (entry.getValue() < p) {
                p = entry.getValue();
                suboptimal--;
            }
            if (suboptimal < 0) {
                break;
            }

        }
        return values;
    }

    private boolean nextConfiguration(int[] subOptConfig, int[] diffValCounts) {
        for (int i = 0; i < subOptConfig.length; i++) {
            if (subOptConfig[i] < diffValCounts[i] - 1) {
                subOptConfig[i]++;
                for (int j = 0; j < i; j++) {
                    subOptConfig[j] = 0;
                }
                return true;
            }
        }
        return false;
    }

    private int[] getDiffValueCounts() {
        int[] count = new int[pis.size() + ais.size()];
        for (int i = 0; i < pis.size(); i++) {
            count[i] = countNonZeroDiffValues(pis.get(i));
        }
        final int patternSize = pis.size();
        for (int i = 0; i < ais.size(); i++) {
            count[patternSize + i] = countNonZeroDiffValues(ais.get(i));
        }
        return count;
    }

    private <T> int countNonZeroDiffValues(Map<T, Double> pMap) {
        double p = 1;
        int count = 0;
        for (Entry<T, Double> entry : pMap.entrySet()) {
            if (entry.getValue() < sameAsZeroThreshold) {
                return count;
            }
            if (entry.getValue() < p) {
                count++;
                p = entry.getValue();
            }
        }
        return count;
    }

}
