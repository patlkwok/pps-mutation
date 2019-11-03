package mutation.g3;

import java.util.Set;

/**
 *
 * @author group3
 */
public interface RuleDistribution {

    /**
     * Returns the log of the probability of this rule according to this
     * distribution
     *
     * @param r a rule
     * @return the log-likelihood for the rule according to this distribution
     */
    double getLogLikelihood(Rule r);

    double getPiLikelihood(int i, byte pi);

    double getAiLikelihood(int i, char ai);

    /**
     * Returns the max log-likelihood of any rule in this distribution
     *
     * @return the highest log-likelihood
     */
    double getHighestLogLikelihood();

    /**
     * Returns a set with the most likely rules according to this distribution
     *
     * @return a set of most likely rules
     */
    Set<Rule> getMostLikelyRules();

    /**
     * Returns a set with the most likely rules according to this distribution
     *
     * @param exceptions rules in this list will not be included
     * @return a set of most likely rules not in the exceptions list
     */
    Set<Rule> getMostLikelyRules(Set<Rule> exceptions);

    /**
     * Discard the given rules from the distribution
     *
     * @param rules rules to be ruled out
     */
    void ruleOut(Set<Rule> rules);
}
