package mutation.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static mutation.g3.LogProbability.*;

/**
 *
 * @author juand.correa
 */
public class RunningDistribution extends MutationBasedDistribution implements Cloneable {

    private static final Comparator<Pair<? extends Object, Double>> PROB_COMPARATOR = (
            Pair<? extends Object, Double> e1,
            Pair<? extends Object, Double> e2) -> {
        return -e1.getSecond().compareTo(e2.getSecond());
    };

    /**
     * Constructs a distribution with defined priors for the elements in the
     * rule with pattern and action with the size of the scope
     *
     * @param scope the size of the pattern and action
     */
    public RunningDistribution(int scope) {
        super(new ArrayList<>(), new ArrayList<>());
        final double[] priorPerLength = {0.0, .4, .4, .15, .05};
        final int[] numMatches = {0, 1, 3, 3, 1};
        for (int i = 0; i < scope; i++) {
            pis.add(new LinkedHashMap<>());
            for (byte x = 0b0001; x <= 0b1111; x++) {
                int nBases = Integer.bitCount(x);
                pis.get(i).put(x, p2log(priorPerLength[nBases] / numMatches[nBases]));
            }
        }
        final double aiUniformLetter = p2log(1.0 / (4 + scope));
        final double aiUniformDigit = p2log(1.0 / (4 + scope));
        for (int i = 0; i < scope; i++) {
            ais.add(new LinkedHashMap<>());
            for (char x = '0'; x < '0' + scope; x++) {
                ais.get(i).put(x, aiUniformDigit);
            }
            ais.get(i).put('a', aiUniformLetter);
            ais.get(i).put('c', aiUniformLetter);
            ais.get(i).put('g', aiUniformLetter);
            ais.get(i).put('t', aiUniformLetter);
        }
        sortProbabilities();
        computeHighestLogLikelihood();
        filterTopRuleSpace();
    }

    public RunningDistribution(List<Map<Byte, Double>> pis, List<Map<Character, Double>> ais) {
        super(pis, ais);
    }

    public void aggregate(RuleDistribution distribution) {
        for (int i = 0; i < pis.size(); i++) {
            aggregatePi(i, pis.get(i), distribution);
        }
        for (int i = 0; i < ais.size(); i++) {
            aggregateAi(i, ais.get(i), distribution);
        }
        reduceDigitsToBases();
        sortProbabilities();
        computeHighestLogLikelihood();
        filterTopRuleSpace();
    }

    protected void aggregatePi(int i, Map<Byte, Double> localDist, RuleDistribution d) {
        double total = LOG_ZERO_PROB;
        for (Entry<Byte, Double> entry : localDist.entrySet()) {
            Byte ei = entry.getKey();
            Double p = entry.getValue();
            final double newProb = logPMult(p, d.getPiLikelihood(i, ei));
            entry.setValue(newProb);
            total = logPAdd(total, newProb);
        }
        // avoid normalizing if the total probability is zero (every element must be
        // 0 probability in log space then
        if (total != LOG_ZERO_PROB) {
            for (Entry<Byte, Double> entry : localDist.entrySet()) {
                Double p = entry.getValue();
                entry.setValue(p - total);
                if (p - total == Double.POSITIVE_INFINITY) {
                    System.out.println("Here");
                }
            }
        }
    }

    protected void aggregateAi(int i, Map<Character, Double> localDist, RuleDistribution d) {
        double total = LOG_ZERO_PROB;
        for (Entry<Character, Double> entry : localDist.entrySet()) {
            Character ei = entry.getKey();
            Double p = entry.getValue();
            final double newProb = logPMult(p, d.getAiLikelihood(i, ei));
            entry.setValue(newProb);
            total = logPAdd(total, newProb);
        }
        // avoid normalizing if the total probability is zero (every element must be
        // 0 probability in log space then
        if (total != LOG_ZERO_PROB) {
            for (Entry<Character, Double> entry : localDist.entrySet()) {
                Double p = entry.getValue();
                entry.setValue(p - total);
            }
        }
    }

    private void sortProbabilities() {
        for (int i = 0; i < pis.size(); i++) {
            sortMapByValue(pis.get(i));
        }
        for (int i = 0; i < ais.size(); i++) {
            sortMapByValue(ais.get(i));
        }
    }

    private <T extends Object> void sortMapByValue(Map<T, Double> map) {
        final List<Pair<T, Double>> pairs = new ArrayList<>();
        map.entrySet().forEach(
                (e) -> pairs.add(new Pair<>(e.getKey(), e.getValue()))
        );
        Collections.sort(pairs, PROB_COMPARATOR);
        map.clear();
        pairs.forEach((p) -> map.put(p.getFirst(), p.getSecond()));
    }

    @Override
    protected RunningDistribution clone() throws CloneNotSupportedException {
        final List<Map<Byte, Double>> nPis = new ArrayList<>();
        final List<Map<Character, Double>> nAis = new ArrayList<>();
        for (int i = 0; i < this.pis.size(); i++) {
            nPis.add(new LinkedHashMap<>());
            for (Entry<Byte, Double> entry : this.pis.get(i).entrySet()) {
                nPis.get(i).put(entry.getKey(), entry.getValue());
            }
        }
        for (int i = 0; i < this.ais.size(); i++) {
            nAis.add(new LinkedHashMap<>());
            for (Entry<Character, Double> entry : this.ais.get(i).entrySet()) {
                nAis.get(i).put(entry.getKey(), entry.getValue());
            }
        }
        return new RunningDistribution(nPis, nAis);
    }
    
    public final void reduceDigitsToBases() {
        for (int i = 0; i < this.ais.size(); i++) {
            for (Entry<Character, Double> entry : this.ais.get(i).entrySet()) {
                char action = entry.getKey();
                // if the action is a digit
                if (entry.getValue() > LOG_ZERO_PROB && action >= '0' && action <= '9') {
                    // if the top pi corresponding to the position of the digit is
                    // a single element
                    for (Entry<Byte, Double> pi : pis.get(action - '0').entrySet()) {
                        Byte key = pi.getKey();
                        Double value = pi.getValue();
                        // if that single element has all the probability and it is a single base
                        if (value == LOG_ONE_PROB && Integer.bitCount(key) == 1) {
                            final char actionBase = Rule.baseByteToString(key).charAt(0);
                            // add the probability of the digit to the corresponding unique base
                            ais.get(i).put(actionBase, logPAdd(
                                    ais.get(i).getOrDefault(actionBase, LOG_ZERO_PROB), value));
                            // set the probability of the digit to zero
                            entry.setValue(LOG_ZERO_PROB);
                        }
                    }
                }
            }
        }
    }

}
