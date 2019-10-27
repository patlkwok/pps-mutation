package mutation.g3;

import java.util.HashMap;
import java.util.List;
import javafx.util.Pair;

/**
 *
 * @author group3
 */
public class RuleEnumerator {

    public static final byte A = 0b1000;
    public static final byte C = 0b0100;
    public static final byte G = 0b0010;
    public static final byte T = 0b0001;

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
        final int length = original.length();
        List<HashMap<Byte, Double>> patternElements;
        List<HashMap<Character, Double>> actionElements;
        // iterate over all possible pattern positions and values they could have
        // and compute their likelihood, keep those with non-zero probability
        ArrayList<ArrayList<Pair<byte, double>>> Pis = getPis(original);
        ArrayList<ArrayList<Pair<char, double>>> Ais = getAis(original, mutated);
        // now get all combinations of 1 choice from each Pis and Ais index to form rule
        // multiply probabilities that they are paired with to get rule prob

        return null;
    }

    private ArrayList<ArrayList<Pair<char, double>>> getAis(String original, String mutated) {
        ArrayList<ArrayList<Pair<char, double>>> Ais = new ArrayList<ArrayList<Pair<char, double>>>();
        ArrayList<Pair<char, double>> currentAiList = new ArrayList<Pair<char, double>>();
        for (int i=0; i < mutated.length(); i++) {
            // get all possible (ai, prob) pair for each Ti
            char ti = mutated.charAt(i);
            String actionSet = getActionSet(original.length());
            for (char ai : actionSet) {
                prob = probabilityAi(ai, original, ti);
                if (prob > 0) {
                    currentAiList.add(Pair<ai, prob>)
                }
            }
        }
    }

    private String getActionSet(Integer windowSize) {
        String actionSet = "acgt";
        for (int i=0; i < windowSize; i++) {
            actionSet += i;
        }
        return actionSet;
    }

    private ArrayList<ArrayList<Pair<byte, double>>> getPis(String original) {
        ArrayList<ArrayList<Pair<byte, double>>> Pis = new ArrayList<ArrayList<Pair<byte, double>>>();
        ArrayList<Pair<byte, double>> currentPiList = new ArrayList<Pair<byte, double>>()
        for (int i=0; i < original.length(); i++) {
            // get all possible (pi, prob) pair for each Si
            char si = original.charAt(i);
            for (int j=1; j < 16; j++) {
                byte pi = j;
                prob = probabilityPi(pi, si);
                if (prob > 0) {
                    currentPiList.add(new Pair<pi, prob>)
                }
            }
            Pis.add(currentPiList);
            currentPiList = new ArrayList<Pair<byte, double>>()
        }
        return Pis
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
        return (pi & si > 0) ? 1.0/8 : 0;
    }

    /**
     * Computes the likelihood of ai being the ith element of the action given
     * the original string, the ith position of the mutated string, and knowing
     * that the string was matched
     *
     * @param pi pattern element value
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
        boolean condition1 = (ai == ti);
        boolean condition2 = (s[int(ai)] == ti)
        if (condition1 || condition2) {
            return 1.0 / possibleActions  // distribute probability uniformly
        }
        else {
            return 0
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
        return rule;
    }
}
