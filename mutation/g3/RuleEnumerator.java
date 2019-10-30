package mutation.g3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author group3
 */
public class RuleEnumerator {

    public static final byte A = 0b1000;
    public static final byte C = 0b0100;
    public static final byte G = 0b0010;
    public static final byte T = 0b0001;

    private double minProbability = 6E-10;

    /**
     * Given two strings with the same length corresponding to the same portion
     * of the genome, before and after mutation; this method returns a list of
     * possible rules that are consistent with the changes observed.
     *
     * @param original window of DNA before change
     * @param mutated window after change
     * @return a list of consistent rules
     */
    public HashMap<Rule, Double> enumerate(String original, String mutated) {
        // iterate over all possible pattern positions and values they could have
        // and compute their likelihood, keep those with non-zero probability
        List<List<Pair<Byte, Double>>> Pis = getPis(original);
        List<List<Pair<Character, Double>>> Ais = getAis(original, mutated);
        // now get all combinations of 1 choice from each Pis and Ais index to form rule
        // multiply probabilities that they are paired with to get rule prob
        HashMap<Rule, Double> bucket = new HashMap();
        makeRules(bucket, Pis, Ais, 0, "", 1);
        return bucket;
    }

    /**
     * Populates a map of rules recursively
     *
     * @param bucket map where the rules and their probabilities should be added
     * @param Pis possible pattern elements and their probabilities
     * @param Ais possible action elements and their probabilities
     * @param step the element of the pattern currently under construction
     * @param partial a partial rule being constructed
     * @param probability the probability associated with the rule so far
     */
    protected void makeRules(
            HashMap<Rule, Double> bucket,
            List<List<Pair<Byte, Double>>> Pis,
            List<List<Pair<Character, Double>>> Ais,
            int step,
            String partial,
            double probability) {
        if (probability < minProbability) {
            return;
        }
        final int patternL = Pis.size();
        if (step < patternL) {
            // add another pattern element
            List<Pair<Byte, Double>> pi = Pis.get(step);
            for (int j = 0; j < pi.size(); j++) {
                final String nPartial = partial + (step > 0 ? ';' : "") + baseByteToString(pi.get(j).getFirst());
                final double nProb = probability * pi.get(j).getSecond();
                makeRules(bucket, Pis, Ais, step + 1, nPartial, nProb);
            }
        } else if (step == patternL) {
            // add @ to start the action
            makeRules(bucket, Pis, Ais, step + 1, partial + "@", probability);
        } else if (step < patternL + 1 + Ais.size()) {
            // add another action element
            List<Pair<Character, Double>> ai = Ais.get(step - patternL - 1);
            for (int j = 0; j < ai.size(); j++) {
                final String nPartial = partial + ai.get(j).getFirst();
                final double nProb = probability * ai.get(j).getSecond();
                makeRules(bucket, Pis, Ais, step + 1, nPartial, nProb);
            }
        } else {
            // rule is full
            final String pattern = partial.substring(0, partial.indexOf("@"));
            final String action = partial.substring(partial.indexOf("@") + 1);
            bucket.put(simplifyRule(new Rule(pattern, action)), probability);
        }
    }

    private List<List<Pair<Character, Double>>> getAis(String original, String mutated) {
        final List<List<Pair<Character, Double>>> Ais = new ArrayList<>();
        for (int i = 0; i < mutated.length(); i++) {
            // get all possible (ai, prob) pair for each Ti
            final List<Pair<Character, Double>> currentAiList = new ArrayList<>();
            char ti = mutated.charAt(i);
            String actionSet = getActionSet(original.length());
            for (char ai : actionSet.toCharArray()) {
                double prob = probabilityAi(ai, original, ti);
                if (prob > 0) {
                    currentAiList.add(new Pair(ai, prob));
                }
            }
            Ais.add(currentAiList);
        }
        return Ais;
    }

    private String getActionSet(Integer windowSize) {
        String actionSet = "acgt";
        for (int i = 0; i < windowSize; i++) {
            actionSet += i;
        }
        return actionSet;
    }

    protected List<List<Pair<Byte, Double>>> getPis(String original) {
        final List<List<Pair<Byte, Double>>> Pis = new ArrayList<>();
        for (int i = 0; i < original.length(); i++) {
            // get all possible (pi, prob) pair for each Si
            final List<Pair<Byte, Double>> currentPiList = new ArrayList<>();
            final char si = original.charAt(i);
            for (byte pi = 1; pi < 16; pi++) {
                double prob = probabilityPi(pi, baseCharToByte(si));
                if (prob > 0) {
                    currentPiList.add(new Pair(pi, prob));
                }
            }
            Pis.add(currentPiList);
        }
        return Pis;
    }

    protected byte baseCharToByte(char b) {
        switch (b) {
            case 'a':
                return A;
            case 'c':
                return C;
            case 'g':
                return G;
            case 't':
                return T;
            default:
                throw new IllegalArgumentException(b + " is not a valid base");
        }
    }

    protected String baseByteToString(byte b) {
        String pattern = "";
        if ((b & A) > 0) {
            pattern += 'a';
        }
        if ((b & C) > 0) {
            pattern += 'c';
        }
        if ((b & G) > 0) {
            pattern += 'g';
        }
        if ((b & T) > 0) {
            pattern += 't';
        }
        return pattern;
    }

    /**
     * Computes the likelihood of pi being the ith element of the pattern given
     * that the ith position of the original string was si and that the genome
     * was matched at this position
     *
     * @param pi pattern element value
     * @param si character in the ith position of the original string
     * @return the computed probability
     */
    protected double probabilityPi(byte pi, byte si) {
        // if si matches any bit of pi, pi is possible
        // there are 8 pi's that would match any si, and we make them all uniformly probable for now

        // our prior belief distribution about the length of pi.  Index i is our prior for length i.  0.0 is a dummy.
        // we give fairly strong bias to shorter lengths, but can rule these out quickly from experiment if wrong
        Double[] prior = {0.0, .4, .4, .15, .05};

        // number of possible matches at each length
        Integer[] num_matches = {0, 1, 3, 3, 1};

        Integer len_pi = Integer.bitCount(pi);

        return ((pi & si) > 0) ? prior[len_pi] / num_matches[len_pi] : 0;
    }

    /**
     * Computes the likelihood of ai being the ith element of the action given
     * the original string, the ith position of the mutated string, and knowing
     * that the string was matched
     *
     * @param ai action element value
     * @param s the original string
     * @param ti the character at the ith position of the mutated string
     * @return the computed probability
     */
    protected double probabilityAi(char ai, String s, char ti) {  // Juan, why did we have pi as an input here? -John
        // the possibilities for the action are the letter ti itself, or any number that corresponds to
        // the letter ti in S
        int possibleActions = 1;
        for (int i = 0; i < s.length(); i++) {
            char base = s.charAt(i);
            if (base == ti) {
                possibleActions += 1;
            }
        }
        boolean isExactLetter = (ai == ti);
        boolean isDigit = ai >= '0' && ai <= '9';
        if (isExactLetter || (isDigit && (s.charAt((int) (ai - '0')) == ti))) {
            return 1.0 / possibleActions;  // distribute probability uniformly
        } else {
            return 0;
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
        String pattern = rule.getPattern();
        while (pattern.endsWith(";acgt")) {
            pattern = pattern.substring(0, pattern.length() - 5);
        }
        if (rule.getPattern().equals("acgt;acgt;acgt;acgt")) {
            System.out.println("Here");
        }
        if (!pattern.equals(rule.getPattern())) {
            return new Rule(pattern, rule.getAction());
        }
        return rule;
    }
}
