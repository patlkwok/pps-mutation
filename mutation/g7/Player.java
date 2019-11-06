package mutation.g7;

import java.util.*;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;


public class Player extends mutation.sim.Player {

    private Random random;
    private Map<String, Double> rules = new HashMap<String, Double>();

    private Map<Integer, Double> lengthMap;
    private double numPerm = 0.0;
    private String[] genes = "acgt".split("");

    private int numTrials = 1000;
    private Double modifierStep = 0.05;
    private Double rulesLength = 0.0;
    private Double momentum = 0.01;
    private Double takeAChanceWithLength = 0.05;

    // An array to record the wrong guesses so we don't repeat them
    private Vector<Mutagen> wrongMutagens = new Vector<>();

    private int maxMutagenLength = 10;

    // An array of all patterns that happened
    private Vector<HashSet<String>> allPatterns = new Vector<>();

    public Player() {
        random = new Random();
        for (int i = 0; i < maxMutagenLength; i++) {
            allPatterns.add(new HashSet<>());
        }
    }

    private String samplePair() {
        Double random = Math.random() * rulesLength;
        Double cummulative = 0.0;
        Iterator it = rules.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = (Map.Entry<String, Double>) it.next();
            cummulative += pair.getValue();
            if (random < cummulative) {
                return pair.getKey();
            }
        }
        it = rules.entrySet().iterator();
        Map.Entry<String, Double> pair = (Map.Entry<String, Double>) it.next();
        return pair.getKey();
    }

    private String samplePairOfLength(int length) {
        for (int i = 0; i < 1000; i++) {
            Double random = Math.random() * rulesLength;
            Double cummulative = 0.0;
            Iterator it = rules.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Double> pair = (Map.Entry<String, Double>) it.next();
                cummulative += pair.getValue();
                if (random < cummulative) {
                    String actionPatternPair = pair.getKey();
                    if (actionPatternPair.length() == length) {
                        return actionPatternPair;
                    } else {
                        break;
                    }
                }
            }
        }
        return samplePair();
    }

    private Mutagen sampleMutagen() {
        Mutagen mutagen = new Mutagen();
        // Find the most probable length
        int maxValue = 0;
        int maxIndex = 0;
        for (int i = 0; i < maxMutagenLength; i++) {
            if(allPatterns.get(i).size() > maxValue) {
                maxIndex = i;
                maxValue = allPatterns.get(i).size();
            }
        }
        // Sample first pattern action pair
        String pair;
        if(Math.random() < takeAChanceWithLength) {
            pair = samplePair();
        } else {
            pair = samplePairOfLength(maxIndex + 1);
        }
        String[] patternAction = pair.split("@");
        String pattern = patternAction[0];
        String action = patternAction[1];
        mutagen.add(pattern, action);
        // Check if we need more pairs
        int ruleLength = action.length();
        int numberOfRulesToCombine = allPatterns.get(ruleLength - 1).size();
        if (numberOfRulesToCombine == 0) {
            // If there were no such rules try randomly the number for other length
            // And check go with it 'takeAChanceWithLength' precent of the times
            int result = random.nextInt(maxMutagenLength-1);
            numberOfRulesToCombine = allPatterns.get(result).size();
            if(numberOfRulesToCombine == 0 || Math.random() > takeAChanceWithLength) {
                return sampleMutagen();
            }
        }
        // Add some randomness in the number of rules
        // Only minus and a little up, because the probability of underestimating is low
        int low = -(numberOfRulesToCombine/3) - 1;
        int high = numberOfRulesToCombine/3 + 1;
        int randomDelta = random.nextInt(high-low) + low;
        numberOfRulesToCombine += randomDelta;
        // Sample pattern actions of same length as the first
        Vector<String> previousPatterns = new Vector<String>();
        previousPatterns.add(pair);
        for (int i = 1; i < numberOfRulesToCombine; i++) {
            for (int j = 0; j < 2000; j++) {
                pair = samplePairOfLength(pair.length());
                if (!previousPatterns.contains(pair)) {
                    patternAction = pair.split("@");
                    pattern = patternAction[0];
                    action = patternAction[1];
                    mutagen.add(pattern, action);
                    previousPatterns.add(pair);
                    break;
                }
            }
        }
        return mutagen;
    }

    private void modifyRuleDistribution(String pattern, String action, Double modifier) {
        String[] patternArr = pattern.split("");
        String patternKey = patternArr[0];
        for (int i = 1; i < patternArr.length; i++) {
            patternKey += ";" + patternArr[i];
        }
        rulesLength += modifier;
        rules.put(patternKey + "@" + action, rules.getOrDefault(patternKey + "@" + action, 0.0) + modifier);
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        for (int i = 0; i < numTrials; ++i) {
            // Get the genome
            String genome = randomString();
            String mutated = console.Mutate(genome);
            int genome_length = genome.length();

            // Add 10 last characters to beginning and 10 first characters to end of genome before / after mutation 
            // To simulate wrapping around 
            genome = genome.substring(genome_length - 9, genome_length) + genome + genome.substring(0, 9);
            mutated = mutated.substring(mutated.length() - 9, mutated.length()) + mutated + mutated.substring(0, 9);
            // Collect the change windows
            Vector<Pair<Integer, Integer>> changeWindows = new Vector<Pair<Integer, Integer>>();
            for (int j = 9; j < genome_length; j++) {
                char before = genome.charAt(j);
                char after = mutated.charAt(j);
                if (before != after) {
                    int finish = j;
                    for (int forwardIndex = j + 1; forwardIndex < j + 10; forwardIndex++) {
                        if (genome.charAt(forwardIndex) == mutated.charAt(forwardIndex)) {
                            finish = forwardIndex - 1;
                            break;
                        }
                    }
                    changeWindows.add(new Pair<Integer, Integer>(j, finish));
                    j = finish;
                }
            }
            // Get the window sizes distribution and generate all possible windows
            int[] windowSizesCounts = new int[maxMutagenLength];
            Vector<Pair<Integer, Integer>> possibleWindows = new Vector<Pair<Integer, Integer>>();
            for (Pair<Integer, Integer> window : changeWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                String before = genome.substring(start, finish + 1);
                if (before.length() - 1 >= maxMutagenLength) continue;
                allPatterns.get(before.length() - 1).add(before);
                int windowLength = finish - start + 1;
                windowSizesCounts[windowLength - 1]++;
                possibleWindows.add(window);
                possibleWindows.add(window);
                if (windowLength == maxMutagenLength) {
                    // TODO: Handle the case if two smaller mutations occured side by side
                    possibleWindows.add(window);
                } else {
                    for (int proposedWindowLength = windowLength; proposedWindowLength <= maxMutagenLength; proposedWindowLength++) {
                        int diff = proposedWindowLength - windowLength;
                        // TODO: Handle the edge cases (i.e. start = 0 || 999)
                        for (int offset = -diff; offset <= 0; offset++) {
                            int newStart = start + offset;
                            int newFinish = newStart + proposedWindowLength - 1;
                            possibleWindows.add(new Pair<Integer, Integer>(newStart, newFinish));
                        }
                    }
                }
            }

            // Modify the distributions for length
            // for (int j = 0; j < maxMutagenLength; j++) {
            //    Double modifier = windowSizesCounts[j] * 1.0 / (changeWindows.size() / maxMutagenLength);
            //    modifyMutagenLengthDistribution(j + 1, modifier);
            //}

            // Modify the distributions for pattens and actions
            for (Pair<Integer, Integer> window : possibleWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                // Get the string from
                String before = genome.substring(start, finish + 1);
                // Get the string after
                String after = mutated.substring(start, finish + 1);
                // Modify the distribution
                modifyRuleDistribution(before, after, modifierStep + momentum * rulesLength);
            }

            // Sample a mutagen
            boolean foundGuess = false;
            Mutagen guess = new Mutagen();
            while (!foundGuess) {
                guess = sampleMutagen();
                System.out.println("[>] Guessing");
                if (!wrongMutagens.contains(guess)) {
                    foundGuess = true;
                }
            }
            boolean isCorrect = console.Guess(guess);
            if (isCorrect) {
                return guess;
            } else {
                // Record that this is not a correct mutagen
                wrongMutagens.add(guess);
            }
        }
        return sampleMutagen();
    }
}
