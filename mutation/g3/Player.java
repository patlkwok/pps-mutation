package mutation.g3;

import java.util.*;
import java.util.Map.Entry;
import mutation.sim.Console;
import mutation.sim.Mutagen;

/**
 * A player for the Mutation game
 *
 * @author group3
 */
public class Player extends mutation.sim.Player {

    private final static double GUESSING_THRESHOLD = 0;

    private final Random random;
    private final List<Mutation> expHistory;
    private final RuleEnumerator enumerator;
    private final HashMap<Rule, Double> candidateRules;
    private int consideredWindowSize = 4;
    private int windowCleared = 0;

    public Player() {
        random = new Random();
        expHistory = new ArrayList<>();
        enumerator = new RuleEnumerator();
        candidateRules = new HashMap<>();
    }

    @Override
    public Mutagen Play(Console console, int m) {
        //final int numExps = console.getNumExps();
        while (true) {
            String genome = designExperiment();
            String mutated = console.Mutate(genome);
            // check if we ran out of experiments to run
            if (mutated.equals("")) {
                break;
            }
            int q = console.getNumberOfMutations();
            recordMutations(genome, mutated, m, q);
            Mutagen guess = makeGuess();
            if (console.Guess(guess)) {
                // we guessed right!
                return guess;
            } else {
                guessedWrong(guess);
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
        List<Entry<Rule, Double>> list = new ArrayList<>(scores.entrySet());
        // Step 2: Sort the list (in descending order of scores)
        Collections.sort(list, (Entry<Rule, Double> o1, Entry<Rule, Double> o2)
                -> (o2.getValue()).compareTo(o1.getValue()));
        // Step 3: Put data from sorted list to hash map
        HashMap<Rule, Double> newScores = new LinkedHashMap<>();
        for (Entry<Rule, Double> s : list) {
            newScores.put(s.getKey(), s.getValue());
        }
        return newScores;
    }

    /**
     * Based on the internal state of the player generates the best experiment
     *
     * @return a string to be submitted to the mutagen's effect
     */
    protected String designExperiment() {
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
        Mutation mutation = new Mutation(original, mutated);
        expHistory.add(mutation);
        updateBelieves(mutation);
    }

    /**
     * Given a mutation (of the whole genome), updates the believes on the
     * possible rules that could generate such mutation
     *
     * @param mutation the observed mutation
     */
    protected void updateBelieves(Mutation mutation) {
        List<HashSet<Mutation>> mutations
                = getPossibleMutations(mutation.getOriginal(), mutation.getMutated(), consideredWindowSize);
        for (HashSet<Mutation> world : mutations) {
            HashMap<Rule, Double> localRules = updateBelievesPossibleWindows(world);
            for (Entry<Rule, Double> pr : localRules.entrySet()) {
                Double pVal = candidateRules.get(pr.getKey());
                if ((pVal != null && pVal > 0) || windowCleared < pr.getKey().getScopeSize()) {
                    if (pVal == null) {
                        pVal = 1.0;
                    }
                    candidateRules.put(pr.getKey(), pVal * pr.getValue());
                }
            }
            windowCleared = consideredWindowSize;
            // filter out rules that are not consistent with new experiment for at least
            // one window
            candidateRules.entrySet().removeIf((cr) -> (!localRules.containsKey(cr.getKey())));
        }
    }

    /**
     * Get all rules that could produce at least one of the mutations in the
     * given set, and associates the highest probability with any of such
     * mutations to the rule
     *
     * @param world a set of possible mutations
     * @return a map of rules and their probabilities
     */
    protected HashMap<Rule, Double> updateBelievesPossibleWindows(HashSet<Mutation> world) {
        HashMap<Rule, Double> localRules = new HashMap<>();
        for (Mutation mut : world) {
            Map<Rule, Double> possibleRules = enumerator.enumerate(mut.getOriginal(), mut.getMutated()); // black box
            for (Entry<Rule, Double> pr : possibleRules.entrySet()) {
                Double pVal = localRules.get(pr.getKey());
                localRules.put(pr.getKey(), Math.max((pVal != null ? pVal : 0), pr.getValue()));
            }
        }
        return localRules;
    }

    /**
     * Guesses the most likely Mutagen based on the current state of the Player.
     * It may alter the hypothesis space and reevaluate the evidence if the
     * confidence on the current most likely Mutagen is too low
     *
     * @return the guess made
     */
    protected Mutagen makeGuess() {
        Mutagen guess = null;
        do {
            try {
                guess = getMostLikelyMutagen();
            } catch (NotConfidentEnoughException | NoMoreCandidateRulesException ex) {
                // current most likely rule is not likely enough
                // we conclude that the scope size/ window size considered may be
                // too small, we increase it and reevaluate the evidence so far
                // if there is space to grow
                if (consideredWindowSize < 10) {
                    consideredWindowSize++;
                    candidateRules.clear();
                    for (Mutation m : expHistory) {
                        updateBelieves(m);
                    }
                } else if (ex instanceof NotConfidentEnoughException) {
                    guess = ((NotConfidentEnoughException) ex).getMostLikely();
                } else {
                    return null;
                }
            }
        } while (guess == null);
        return guess;
    }

    /**
     * Changes the player state when a guess was made and turned out to be wrong
     *
     * @param guess the guess that failed
     */
    protected void guessedWrong(Mutagen guess) {
        // remove the guessed rule from the candidates
        if (guess.getPatterns().size() == 1) {
            candidateRules.remove(new Rule(guess.getPatterns().get(0), guess.getActions().get(0)));
        }
    }

    /**
     * Returns a Mutagen that is most likely based on the current state of the
     * player
     *
     * @return the guessed Mutagen
     * @throws NotConfidentEnoughException if the confidence in the most likely
     * rule is too low
     */
    protected Mutagen getMostLikelyMutagen() throws NotConfidentEnoughException, NoMoreCandidateRulesException {
        HashMap<Rule, Double> sortedRules = sortRules(candidateRules);
        int maxRules = 1;
        int rules = 0;
        Mutagen result = new Mutagen();
        if (candidateRules.isEmpty()) {
            throw new NoMoreCandidateRulesException();
        }
        boolean notLikelyEnough = false;
        for (Entry<Rule, Double> s : sortedRules.entrySet()) {
            if (s.getValue() < GUESSING_THRESHOLD) {
                notLikelyEnough = true;
            }

            if (rules < maxRules) {
                result.add(s.getKey().getPattern(), s.getKey().getAction());
                rules++;
            } else {
                break;
            }
        }
        if (notLikelyEnough) {
            throw new NotConfidentEnoughException(result);
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
    private List<HashSet<Mutation>> getPossibleMutations(String original, String mutated, int windowSize) {
        List<HashSet<Mutation>> mutations = new ArrayList<>();

        List<Integer> changes = new ArrayList<>();
        for (int j = 0; j < original.length(); j++) {
            if (original.charAt(j) != mutated.charAt(j)) {
                changes.add(j);
            }
        }

        final int numChanges = changes.size();
        for (int i = 0; i < numChanges;) {
            int curChangePos = changes.get(i);
            int nextChangePos;
            int j = i;
            do {
                j++;
                if (j % numChanges < 0) {
                    System.out.println("Here");
                }
                nextChangePos = changes.get(j % numChanges);
                if (nextChangePos < curChangePos) {
                    nextChangePos += original.length();
                }
            } while (nextChangePos - curChangePos < windowSize);
            j--;
            nextChangePos = changes.get(j % numChanges);
            if (nextChangePos < curChangePos) {
                nextChangePos += original.length();
            }
            int margin = windowSize - (nextChangePos - curChangePos + 1);
            mutations.add(extractWindows(original, mutated, curChangePos, margin, windowSize));
            i = j + 1;
        }
        return mutations;
    }

    /**
     * Extract the mutations defined by the pairs of string of margin windows
     * starting at position pos - margin
     *
     * @param genome original genome
     * @param mutated mutated genome
     * @param pos position
     * @param margin margin/number of windows
     * @param windowSize size of the windows
     * @return
     */
    protected HashSet<Mutation> extractWindows(String genome, String mutated, int pos, int margin, int windowSize) {
        HashSet<Mutation> set = new HashSet<>();
        final int size = genome.length();

        for (int j = pos - margin; j <= pos; j++) {
            String gPart;
            String mPart;
            if (j < 0) {
                gPart = genome.substring(size + j) + genome.substring(0, windowSize + j);
                mPart = mutated.substring(size + j) + mutated.substring(0, windowSize + j);
            } else if (j + windowSize > size) {
                gPart = genome.substring(j) + genome.substring(0, windowSize + j - size);
                mPart = mutated.substring(j) + mutated.substring(0, windowSize + j - size);
            } else {
                gPart = genome.substring(j, j + windowSize);
                mPart = mutated.substring(j, j + windowSize);
            }
            set.add(new Mutation(gPart, mPart));
        }
        return set;
    }
}

class NotConfidentEnoughException extends Exception {

    private final Mutagen mostLikely;

    public NotConfidentEnoughException(Mutagen mostLikely, String message) {
        super(message);
        this.mostLikely = mostLikely;
    }

    public NotConfidentEnoughException(Mutagen mostLikely) {
        this.mostLikely = mostLikely;
    }

    public Mutagen getMostLikely() {
        return mostLikely;
    }

}

class NoMoreCandidateRulesException extends Exception {

}
