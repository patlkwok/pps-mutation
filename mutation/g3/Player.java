package mutation.g3;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

/**
 * A player for the Mutation game
 *
 * @author group3
 */
public class Player extends mutation.sim.Player {

    private final Random random;

    public Player() {
        random = new Random();
    }

    @Override
    public Mutagen Play(Console console, int m) {
        final int numExps = console.getNumExps();
        for (int i = 0; i < numExps; ++i) {
            String genome = this.designExperiment(numExps - i);
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

    }

    /**
     * Returns the best guess (Mutagen) based on the current state of the player
     *
     * @return the guessed Mutagen
     */
    protected Mutagen makeGuess() {
        Mutagen result = new Mutagen();
        result.add("a;c;c", "att");
        result.add("g;c;c", "gtt");
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
}
