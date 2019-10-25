package mutation.g3;

import java.util.ArrayList;
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
    public List<Rule> enumerate(String original, String mutated) {
        final int length = original.length();
        List<char[]> possibleActions = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            char outcome = mutated.charAt(i);
            // it is always possible that the action in this position consists of
            // exactly the same character that we observe after change
            String possibleAction = "" + outcome;

            possibleActions.add(possibleAction.toCharArray());
        }
        return null;
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
