package mutation.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static mutation.g3.LogProbability.*;

/**
 *
 * @author group3
 */
public class RuleInferenceEngine {

    private RuleDistribution priorDist;

    private static final Comparator<Pair<? extends Object, Double>> PROB_COMPARATOR = (
            Pair<? extends Object, Double> e1,
            Pair<? extends Object, Double> e2) -> {
        return -e1.getSecond().compareTo(e2.getSecond());
    };

    public RuleInferenceEngine() {

    }

    public void setPriorDistribution(RuleDistribution prior) {
        this.priorDist = prior;
    }

    /**
     * Given two strings with the same length corresponding to the same portion
     * of the genome, before and after mutation; this method returns a
     * distribution over the possible rules that are consistent with the changes
     * observed.
     *
     * @param original window of DNA before change
     * @param mutated window after change
     * @return a distribution over the rule space based on the given evidence
     */
    public RuleDistribution getDistribution(String original, String mutated) {
        // iterate over all possible pattern positions and values they could have
        // and compute their likelihood, keep those with non-zero probability
        List<Map<Byte, Double>> pis = getPis(original);
        List<Map<Character, Double>> ais = getAis(original, mutated);
        return new MutationBasedDistribution(pis, ais);
    }

    private List<Map<Character, Double>> getAis(String original, String mutated) {
        final List<Map<Character, Double>> ais = new ArrayList<>();
        for (int i = 0; i < mutated.length(); i++) {
            // get all possible (ai, prob) pair for each Ti
            final List<Pair<Character, Double>> entries = new ArrayList<>();
            char ti = mutated.charAt(i);
            String actionSet = getActionSet(original.length());
            for (char ai : actionSet.toCharArray()) {
                double prob = probabilityAi(i, ai, original, ti);
                if (prob > LOG_ZERO_PROB) {
                    entries.add(new Pair(ai, prob));
                }
            }
            Collections.sort(entries, PROB_COMPARATOR);
            LinkedHashMap<Character, Double> sortedMap = new LinkedHashMap();
            entries.forEach((p) -> {
                sortedMap.put(p.getFirst(), p.getSecond());
            });
            ais.add(sortedMap);
        }
        return ais;
    }

    private String getActionSet(Integer windowSize) {
        String actionSet = "acgt";
        for (int i = 0; i < windowSize; i++) {
            actionSet += i;
        }
        return actionSet;
    }

    protected List<Map<Byte, Double>> getPis(String original) {
        final List<Map<Byte, Double>> pis = new ArrayList<>();
        for (int i = 0; i < original.length(); i++) {
            // get all possible (pi, prob) pair for each Si
            final List<Pair<Byte, Double>> entries = new ArrayList<>();
            final char si = original.charAt(i);
            for (byte pi = 0b1; pi <= 0b1111; pi++) {
                double prob = probabilityPi(i, pi, Rule.baseCharToByte(si));
                if (prob > LOG_ZERO_PROB) {
                    entries.add(new Pair(pi, prob));
                }
            }
            Collections.sort(entries, PROB_COMPARATOR);
            LinkedHashMap<Byte, Double> sortedMap = new LinkedHashMap();
            entries.forEach((p) -> {
                sortedMap.put(p.getFirst(), p.getSecond());
            });
            pis.add(sortedMap);
        }
        return pis;
    }

    /**
     * Computes the likelihood of pi being the ith element of the pattern given
     * that the ith position of the original string was si and that the genome
     * was matched at this position
     *
     * @param i the index of the pattern element
     * @param pi pattern element value
     * @param si character in the ith position of the original string
     * @return the computed probability (in logarithmic space)
     */
    protected double probabilityPi(int i, byte pi, byte si) {
        // if it pi and si do not have common bits then the probability is zero
        if ((pi & si) == 0) {
            return LOG_ZERO_PROB;
        }
        final double prior;
        if (priorDist != null) {
            double totalPossibleProb = LOG_ZERO_PROB;
            prior = priorDist.getPiLikelihood(i, pi);
            if (prior == LOG_ZERO_PROB) {
                return LOG_ZERO_PROB;
            }
            for (byte x = 0b1; x <= 0b1111; x++) {
                if ((x & si) > 0) {
                    totalPossibleProb = logPAdd(totalPossibleProb, priorDist.getPiLikelihood(i, x));
                }
            }
            return prior - totalPossibleProb; //normalize by dividing (in log space)
        } else {
            // our prior belief distribution about the length of pi.  Index i is our prior for length i.  0.0 is a dummy.
            // we give fairly strong bias to shorter lengths, but can rule these out quickly from experiment if wrong
            double[] priorPerLength = {0.0, .4, .4, .15, .05};
            int len_pi = Integer.bitCount(pi);
            // number of possible matches at each length
            int[] num_matches = {0, 1, 3, 3, 1};
            prior = priorPerLength[len_pi] / num_matches[len_pi];
            return p2log(prior); //return as log probability
        }
    }

    /**
     * Computes the likelihood of ai being the ith element of the action given
     * the original string, the ith position of the mutated string, and knowing
     * that the string was matched
     *
     * @param i index of the action element
     * @param ai action element value
     * @param s the original string
     * @param ti the character at the ith position of the mutated string
     * @return the computed probability (in logarithmic space)
     */
    protected double probabilityAi(int i, char ai, String s, char ti) {
        // the possibilities for the action are the letter ti itself, or any number that corresponds to
        // the letter ti in S
        final double aiUniform = p2log(1.0 / (4 + s.length()));
        // if the prior says that the probability of this ai is zero, return now
        if (priorDist != null && priorDist.getAiLikelihood(i, ai) == LOG_ZERO_PROB) {
            return LOG_ZERO_PROB;
        } 
        double possibleActionsProb = LOG_ZERO_PROB;
        for (int j = 0; j < s.length(); j++) {
            char base = s.charAt(j);
            if (base == ti) {
                possibleActionsProb = logPAdd(possibleActionsProb, priorDist != null ? priorDist.getAiLikelihood(i, (char) ('0' + j)) : aiUniform);
            }
        }
        possibleActionsProb = logPAdd(possibleActionsProb, priorDist != null ? priorDist.getAiLikelihood(i, ti) : aiUniform);
        boolean isExactLetter = (ai == ti);
        boolean isDigit = ai >= '0' && ai <= '9';
        if (isExactLetter || (isDigit && (s.charAt((int) (ai - '0')) == ti))) {
            return (priorDist != null ? priorDist.getAiLikelihood(i, ai) : aiUniform) - possibleActionsProb; //normalize in log-space
        } else {
            return LOG_ZERO_PROB;
        }
    }

    /**
     * Simplifies a given rule to its minimal version consistent with the
     * standards defined
     *
     * @param rule rule to be simplified
     * @return a minimal rule equivalent to the given one (possibly the same)
     */
    public static Rule simplifyRule(Rule rule) {
        byte[] pattern = rule.getPattern();
        if (pattern == null || pattern.length == 0) {
            return rule;
        }
        int idx = pattern.length - 1;
        byte acgt = Rule.A + Rule.C + Rule.G + Rule.T;
        while (idx > 0 && pattern[idx] == acgt) {
            idx--;
        }
        if (idx < pattern.length - 1) {
            byte[] nPattern = new byte[idx + 1];
            System.arraycopy(pattern, 0, nPattern, 0, nPattern.length);
            return new Rule(nPattern, rule.getAction());
        }
        return rule;
    }
}
