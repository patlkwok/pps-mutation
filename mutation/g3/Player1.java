package mutation.g3;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

/**
 * A player for the Mutation game
 *
 * @author group3
 */
public class Player1 extends mutation.sim.Player {

    private final Random random;
    private final List<Pair<String, String>> expHistory;
    private final RuleEnumerator enumerator;

    public Player1() {
        random = new Random();
        expHistory = new ArrayList<>();
        enumerator = new RuleEnumerator();
    }

    @Override
    public Mutagen Play(Console console, int m) {
        final int numExps = console.getNumExps();
        for (int i = 0; i < numExps; ++i) {
            String genome = designExperiment(numExps - i);
            String mutated = console.Mutate(genome);
            int q = console.getNumberOfMutations();
            recordMutations(genome, mutated, m, q);
            Mutagen guess = makeGuess();
            if (console.Guess(guess)) {
                // we guessed right!
                return guess;
            }
        }
        return makeGuess();
    }

    /**
     * Sort a hashmap based on scores of rules
     *
     * @param scores original hashmap
     * @return a hashmap sorted with scores in descending order
     * @see
     */
    public static HashMap<Rule, Double> sortRules(HashMap<Rule, Double> scores) {
        // Sort a hash map containing (pattern, action) pairs
        // Step 1: Create a list from elements of hash map
        List<Map.Entry<Rule, Double>> list = new ArrayList<>(scores.entrySet());
        // Step 2: Sort the list (in descending order of scores)
        Collections.sort(list, (Map.Entry<Rule, Double> o1, Map.Entry<Rule, Double> o2)
                -> (o2.getValue()).compareTo(o1.getValue()));
        // Step 3: Put data from sorted list to hash map
        HashMap<Rule, Double> newScores = new LinkedHashMap<>();
        for (Map.Entry<Rule, Double> s : list) {
            newScores.put(s.getKey(), s.getValue());
        }
        return newScores;
    }

    /**
     * Based on the internal state of the player generates the best experiment
     *
     * @param expsLeft number of available experiments left
     * @return a string to be submitted to the mutagen's effect
     */
    protected String designExperiment(int expsLeft) {
        return generateRandomGenome();
    }

    /**
     * Records an observation of the effect of <i>q</i> mutations and updates
     * the internal state of the player accordingly
     *
     * @param original the genome before mutation
     * @param mutated genome after <i>q</i> mutations
     * @param m number of intended mutations
     * @param q actual number of mutations that were possible to apply
     */
    protected void recordMutations(String original, String mutated, int m, int q) {
        expHistory.add(new Pair<>(original, mutated));
    }

    /**
     * Returns the best guess (Mutagen) based on the current state of the player
     *
     * @return the guessed Mutagen
     */
    protected Mutagen makeGuess() {
        HashMap<Rule, Double> scores = new HashMap<>();
        Pair<String, String> lastExperiment = expHistory.get(expHistory.size() - 1);
        String original = lastExperiment.getFirst();
        String mutated = lastExperiment.getSecond();
        // integrate here
        HashSet<Pair<String, String>> mutations = getPossibleMutations(original, mutated, 2);
        //result.add("a;c;c", "att");
        //result.add("g;c;c", "gtt");
        for (Pair<String, String> m : mutations) {
            Map<Rule, Double> possibleRules = enumerator.enumerate(m.getFirst(), m.getSecond()); // black box
            for (Map.Entry<Rule, Double> pr : possibleRules.entrySet()) {
                Double pVal = scores.get(pr.getKey());
                scores.put(pr.getKey(), (pVal != null ? pVal : 0) + pr.getValue());
            }
        }
        HashMap<Rule, Double> sortedRules = sortRules(scores);
        int maxRules = 1;
        int rules = 0;
        Mutagen result = new Mutagen();
        for (Map.Entry<Rule, Double> s : sortedRules.entrySet()) {
            if (rules < maxRules) {
                result.add(s.getKey().getPattern(), s.getKey().getAction());
                rules++;
            }
        }
        return result;
    }

    /**
     * Returns a randomly generated genome string
     *
     * @return a random genome
     */
    private String generateRandomGenome() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++i) {
            result += pool[Math.abs(random.nextInt() % 4)];
        }
        return result;
    }

    /**
     * List all changes in the strings within a windows of the given length
     *
     * @param original the original string
     * @param mutated changed string
     * @param windowSize size of the window
     * @return list of all changed pieces
     */
    private HashSet<Pair<String, String>> getPossibleMutations(String original, String mutated, int windowSize) {
        HashSet<Pair<String, String>> set = new HashSet<>();
        String genome_new = original.concat(original.substring(0, windowSize));
        String mutated_new = mutated.concat(mutated.substring(0, windowSize));
        for (int j = 0; j < original.length(); ++j) {
            String gPart = genome_new.substring(j, j + windowSize);
            String mPart = mutated_new.substring(j, j + windowSize);
            boolean wasMutated = !gPart.equals(mPart);
            if (wasMutated) {
                set.add(new Pair(gPart, mPart));
            }
        }
        return set;
    }
}
