package mutation.g3;

import java.util.List;
import java.util.Set;
import static mutation.g3.LogProbability.*;

/**
 *
 * @author group3
 */
public class ChangeBasedDistribution implements RuleDistribution {

    private final List<RuleDistribution> distributions;
    private RuleDistribution hLLDistribution;

    public ChangeBasedDistribution(List<RuleDistribution> distributions) throws ZeroMassProbabilityException {
        if (distributions == null || distributions.isEmpty()) {
            throw new IllegalArgumentException("A non empty list of distributions must be provided");
        }
        this.distributions = distributions;
        this.distributions.removeIf((d) -> d.getHighestLogLikelihood() == LOG_ZERO_PROB);
        if (this.distributions.isEmpty()) {
            throw new ZeroMassProbabilityException();
        }
        findDistHighestLikelihood();
    }

    @Override
    public double getLogLikelihood(Rule r) {
        double l = Double.NEGATIVE_INFINITY;
        for (RuleDistribution d : distributions) {
            double dl = d.getLogLikelihood(r);
            if (dl > l) {
                l = dl;
            }
        }
        return l;
    }

    @Override
    public double getPiLikelihood(int i, byte pi) {
        double total = LOG_ZERO_PROB;
        for (RuleDistribution d : distributions) {
            total = logPAdd(total, d.getPiLikelihood(i, pi));
        }
        return total - Math.log(distributions.size());
    }

    @Override
    public double getAiLikelihood(int i, char ai) {
        double total = LOG_ZERO_PROB;
        for (RuleDistribution d : distributions) {
            total = logPAdd(total, d.getAiLikelihood(i, ai));
        }
        return total - Math.log(distributions.size());
    }

    @Override
    public double getHighestLogLikelihood() {
        return hLLDistribution.getHighestLogLikelihood();
    }

    @Override
    public Set<Rule> getMostLikelyRules() {
        return hLLDistribution.getMostLikelyRules();
    }

    @Override
    public Set<Rule> getMostLikelyRules(Set<Rule> exceptions) {
        Set<Rule> rules = getMostLikelyRules();
        rules.removeIf((r) -> exceptions.contains(r));
        return rules;
    }

    @Override
    public void ruleOut(Set<Rule> rules) {
        for (RuleDistribution d : distributions) {
            d.ruleOut(rules);
        }
    }

    private void findDistHighestLikelihood() {
        double hLL = Double.NEGATIVE_INFINITY;
        RuleDistribution rl = null;
        for (RuleDistribution d : distributions) {
            final double dHLL = d.getHighestLogLikelihood();
            if (dHLL > hLL) {
                hLL = dHLL;
                rl = d;
            }
        }
        hLLDistribution = rl;
    }
}
