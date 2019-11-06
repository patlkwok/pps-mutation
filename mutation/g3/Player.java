package mutation.g3;

import java.util.*;
import java.util.Map.Entry;
import static mutation.g3.LogProbability.LOG_ZERO_PROB;
import mutation.sim.Console;
import mutation.sim.Mutagen;

/**
 * A player for the Mutation game
 *
 * @author group3
 */
public class Player extends mutation.sim.Player {

    private final static double GUESSING_THRESHOLD = -150;

    private final Random random;
    private final List<ExperimentResult> expHistory;
    private final RuleInferenceEngine inferenceEngine;
    private final Set<Rule> ruledOutRules;
    private final List<ChangeBasedDistribution> changeDists;
    private List<RunningDistribution> distributions;
    private int consideredWindowSize = 1;
    private int numberOfRulesConsidered = 1;
    private int windowCleared = 0;

    public Player() {
        random = new Random();
        expHistory = new ArrayList<>();
        inferenceEngine = new RuleInferenceEngine();
        ruledOutRules = new HashSet<>();
        changeDists = new ArrayList<>();
        distributions = new ArrayList<>();
        for (int i = 0; i < numberOfRulesConsidered; i++) {
            distributions.add(new RunningDistribution(consideredWindowSize));
        }
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
        Collections.sort(list, (Entry<Rule, Double> o1, Entry<Rule, Double> o2) -> {
            int cmp = o2.getValue().compareTo(o1.getValue());
            if (cmp == 0) {
                return Rule.countActionDigits(o1.getKey()) - Rule.countActionDigits(o2.getKey());
            } else {
                return cmp;
            }
        });
        // Step 3: Put data from sorted list to hash map
        HashMap<Rule, Double> newScores = new LinkedHashMap<>();
        for (Entry<Rule, Double> s : list) {
            newScores.put(s.getKey(), s.getValue());
        }
        return newScores;
    }

    /**
     * Based on the internal state, the player generates the best experiment
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
        ExperimentResult exp = new ExperimentResult(mutation, m, q);
        expHistory.add(exp);
        try {
            updateBelieves(exp);
        } catch (ZeroMassProbabilityException ex) {
            increaseWindowSize();
        }
    }

    /**
     * Given a whole mutation genome and the number of mutations applied try to
     * guess the window size of the rule and skip / advance global considered
     * window size
     *
     * @param experiment the observed experiment
     */
    private void skipBadWindowSizes(ExperimentResult experiment) {
        // the length of getPossibleMutations is the number of times we propose a mutation happened
        // we now check this against q, the number of actual mutations.  If q is smaller, we need to
        // bump up our window size; we might be counting 1 mutation as 2 or 3.
        int localWindowSize = consideredWindowSize;
        while (localWindowSize < 10) {
            List<HashSet<Mutation>> mutations
                    = getPossibleMutations(experiment, localWindowSize, false);
            if (mutations.size() > experiment.appliedMutations) {
                localWindowSize += 1;  // increment global window size to save time considering too small windows
            } else {
                break;
            }
        }
        if (localWindowSize != consideredWindowSize) {
            setConsideredWindowSize(localWindowSize);
            System.out.println("Skipped to window size" + localWindowSize);
        }
    }

    protected void setConsideredWindowSize(int size) {
        this.consideredWindowSize = size;
        ruledOutRules.clear();
        changeDists.clear();
        distributions = new ArrayList<>();
        for (int i = 0; i < numberOfRulesConsidered; i++) {
            distributions.add(new RunningDistribution(consideredWindowSize));
        }
    }

    private List<HashSet<Mutation>> filterCollisions(List<HashSet<Mutation>> mutations, int q) {
        if (mutations.size() >= q) {
            return mutations;
        }

        while (true) {  
            // we may have a collision of mutations counting as 1
            // check if there is a mutation set with significantly more windows than average and eliminate
            int indexToRemove = 0;
            int largestSize = 0;
            double sumSizes = 0;
            for (int i=0; i < mutations.size(); i++) {
                int curSize = mutations.get(i).size();
                sumSizes += curSize;
                if (curSize > largestSize) {
                    largestSize = curSize;
                    indexToRemove = i;
                }
            }
            double meanSize = sumSizes / mutations.size();
            if (largestSize >= meanSize + 2) {  // not sure how to best define "significantly more than average", but let's say 2 for now
                mutations.remove(indexToRemove);
            }
            else {
                break;
            }
        }

        return mutations;
    }

    /**
     * Given a mutation (of the whole genome), updates the believes on the
     * possible rules that could generate such mutation
     *
     * @param experiment the observed experiment
     * @throws ZeroMassProbabilityException if such mutation is not possible
     * under the current constraints
     */
    protected void updateBelieves(ExperimentResult experiment) throws ZeroMassProbabilityException {
        // skip window sizes that are too small to explain the number of mutations actually applied
        skipBadWindowSizes(experiment);
        // now we getPossibleMutations with ignoreCollisions = true so we don't soil our evidence with compositions of rules
        List<HashSet<Mutation>> mutations
                = getPossibleMutations(experiment, consideredWindowSize, true);
        mutations = filterCollisions(mutations, experiment.appliedMutations);
        for (HashSet<Mutation> world : mutations) {
            final ChangeBasedDistribution changeDist = getChangeDistribution(world);
            changeDists.add(changeDist);
            int ruleNumber = chooseRuleToAggregrate(changeDist);
            distributions.get(ruleNumber).aggregate(changeDist);
        }
    }

    protected int chooseRuleToAggregrate(RuleDistribution changeDist) throws ZeroMassProbabilityException {
        int chosenRule = -1;
        double increase = Double.NEGATIVE_INFINITY;
        int zeroProbCount = 0;
        try {
            for (int i = 0; i < numberOfRulesConsidered; i++) {
                RunningDistribution d = distributions.get(i).clone();
                double hLBefore = d.getHighestLogLikelihood();
                d.aggregate(changeDist);
                double hLAfter = d.getHighestLogLikelihood();
                if (hLAfter == LOG_ZERO_PROB) zeroProbCount++;
                if (increase < hLAfter - hLBefore) { // better
                    chosenRule = i;
                    increase = hLAfter - hLBefore;
                }
            }
            if (zeroProbCount == numberOfRulesConsidered)
                throw new ZeroMassProbabilityException("No rule distribution is consistent with this change");
        } catch (CloneNotSupportedException e) {
            System.out.println("This should not happen");
        }
        return chosenRule;
    }

    /**
     * Formulates a probabilistic theory (a distribution) for the rules that
     * could have caused any of the mutations in the given world (change)
     * setting
     *
     * @param world a set of possible mutations
     * @return a theory for the possible mutations
     * @throws ZeroMassProbabilityException if the given world is not possible
     * under current constraints
     */
    protected ChangeBasedDistribution getChangeDistribution(HashSet<Mutation> world) throws ZeroMassProbabilityException {
        List<RuleDistribution> dists = new ArrayList<>();
        int ruleNumber = 0;
        for (Mutation mut : world) {
            dists.add(inferenceEngine.getDistribution(
                    mut.getOriginal(),
                    mut.getMutated(),
                    distributions.get(ruleNumber)));
        }
        return new ChangeBasedDistribution(dists);
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
                    increaseWindowSize();
                } else if (ex instanceof NotConfidentEnoughException) {
                    guess = ((NotConfidentEnoughException) ex).getMostLikely();
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
            ruledOutRules.add(Rule.fromString(
                    guess.getPatterns().get(0), guess.getActions().get(0)));
        }
    }

    /**
     * Returns a Mutagen that is most likely based on the current state of the
     * player
     *
     * @return the guessed Mutagen
     * @throws NotConfidentEnoughException if the confidence in the most likely
     * rule is too low
     * @throws NoMoreCandidateRulesException if we run out of rules to guess
     */
    protected Mutagen getMostLikelyMutagen() throws NotConfidentEnoughException, NoMoreCandidateRulesException {
        Mutagen result = new Mutagen();

        for (int i = 0; i < numberOfRulesConsidered; i++) {
            Set<Rule> candidateRules = new HashSet<>();
            //distribution.ruleOut(ruledOutRules);
            candidateRules.addAll(distributions.get(0).getMostLikelyRules(ruledOutRules));
            if (candidateRules.isEmpty()) {
                throw new NoMoreCandidateRulesException();
            }
            Rule mlRule = candidateRules.iterator().next();
            double mlRuleLL = distributions.get(0).getLogLikelihood(mlRule);

            result.add(mlRule.getPatternString(), mlRule.getAction());
            if (mlRuleLL < GUESSING_THRESHOLD) {
                throw new NotConfidentEnoughException(result);
            }
        }
        return result;
    }

    protected void increaseWindowSize() {
        if (consideredWindowSize < 10) {
            setConsideredWindowSize(consideredWindowSize + 1);
            try {
                for (ExperimentResult er : expHistory) {
                    updateBelieves(er);
                }
            } catch (ZeroMassProbabilityException ex) {
                increaseWindowSize();
            }
        } else {
            // restart
            expHistory.clear();
            setConsideredWindowSize(1);
        }
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
     * @param experiment the experiment
     * @param windowSize size of the window
     * @param ignoreCollisions if true, only return sets of mutations far enough
     * away to avoid composition of mutations
     * @return list of all changed pieces
     */
    private List<HashSet<Mutation>> getPossibleMutations(ExperimentResult experiment, int windowSize, boolean ignoreCollisions) {
        String original = experiment.mutation.getOriginal();
        String mutated = experiment.mutation.getMutated();
        // try checking size of list with m value, which would now need to be passed in
        // eliminate two colliding mutations from the list
        // we can also run this function with higher and higher window sizes to try to figure out
        // the best window size, taking m into account
        List<HashSet<Mutation>> mutations = new ArrayList<>();

        LinkedList<Integer> changes = new LinkedList<>();

        for (int j = 0; j < original.length(); j++) {
            if (original.charAt(j) != mutated.charAt(j)) {
                changes.add(j);
            }
        }

        final int numChanges = changes.size();
        int safeStart = -1; // where to start looking at changes
        int lastChange = changes.getLast() - original.length();
        for (Integer change : changes) {
            if (safeStart == -1 && change - lastChange > windowSize) {
                safeStart = change;
            }
            lastChange = change;
        }

        // rotate the genome to start at safeStart
        original = original.substring(safeStart) + original.substring(0, safeStart);
        mutated = mutated.substring(safeStart) + mutated.substring(0, safeStart);
        // adjust positions
        final int strLen = original.length();
        for (int j = 0; j < numChanges; j++) {
            changes.set(j, (strLen + changes.get(j) - safeStart) % strLen);
        }
        // restore order after adjustment
        while (changes.peek() > 0) {
            changes.add(changes.pop());
        }

        for (int i = 0; i < numChanges;) {
            int curChangePos = changes.get(i);
            int nextChangePos;
            int j = i;
            do {
                j++;
                nextChangePos = changes.get(j % numChanges);
                if (nextChangePos <= curChangePos) {
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
            if (ignoreCollisions) {
                i = j + windowSize;  // look for more mutations far enough away to avoid composition of mutations
            } else {
                i = j + 1;
            }
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
