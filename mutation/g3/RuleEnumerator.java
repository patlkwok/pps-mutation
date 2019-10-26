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
        return null;
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
        return 0;
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
    protected double probabilityAi(char pi, String s, char ti) {
        return 0;
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
