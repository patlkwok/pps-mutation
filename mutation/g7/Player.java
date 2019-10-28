package mutation.g7;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;


public class Player extends mutation.sim.Player {

    private Random random;
    private Map<String, Double> lhs;
    private Map<String, Double> rhs;
    private double numPerm = 0.0;
    private String[] genes = "acgt".split("");

    private int numTrials = 1000;
    private Double modifierStep = 1.05;

    // An array to record the wrong guesses so we don't repeat them
    private Vector<Mutagen> wrongMutagens = new Vector<Mutagen>();

    private int maxMutagenLength = 2;
    private Vector<HashSet<String>> allPatterns = new Vector<HashSet<String>>();

    public Player() {
        random = new Random();
        lhs = new HashMap<>();
        rhs = new HashMap<>();
        setNumPerm(2);
        generateDistributionMap("", 2);
        for (int i = 0; i < maxMutagenLength; i++) {
            allPatterns.add(new HashSet<String>());
        }
    }

    private void setNumPerm(int n){
        for(int i = 1; i <= n; i++){
            numPerm += Math.pow(4, i);
        }
    }

    private void generateDistributionMap(String result, int n){
        if(n == 0){
            lhs.put(result, 1 / numPerm);
            rhs.put(result, 1 / numPerm);
            return;
        }
        String tmp = result;
        for(String c : genes){
            if(!result.equals("")) tmp = result + ";";
            generateDistributionMap(tmp + c, n - 1);
        }
        if(!result.equals("")) {
            lhs.put(result, 1 / numPerm);
            rhs.put(result, 1 / numPerm);
        }
    }

    private String samplePattern(int length) {
        while(true) {
            Double random = Math.random();
            Double cummulative = 0.0;
            Iterator it = lhs.entrySet().iterator();
            while (it.hasNext()) {
                Pair<String, Double> pair = (Pair<String, Double>)it.next();
                cummulative += pair.getValue();
                if(random < cummulative) {
                    String pattern = pair.getKey();
                    if(pattern.length() == length) {
                        return pattern;
                    } else {
                        break;
                    }
                }
                it.remove();
            }
        }
    }

    private String sampleAction() {
        Double random = Math.random();
        Double cummulative = 0.0;
        Iterator it = rhs.entrySet().iterator();
        while (it.hasNext()) {
            Pair<String, Double> pair = (Pair<String, Double>)it.next();
            cummulative += pair.getValue();
            if(random < cummulative) {
                return pair.getKey();
            }
            it.remove();
        }
        it = rhs.entrySet().iterator();
        Pair<String, Double> pair = (Pair<String, Double>)it.next();
        return pair.getKey();
    }

    private Mutagen sampleMutagen() {
        Mutagen mutagen = new Mutagen();
        // Sample action
        String action = sampleAction();
        int ruleLength = action.length();
        int numberOfRulesToCombine = allPatterns.get(ruleLength).size();
        // Sample patterns of same length as action
        for (int i = 0; i < numberOfRulesToCombine; i++) {
            String pattern = samplePattern(ruleLength);
            mutagen.add(pattern, action);
        }
        return mutagen;
    }

    private void modifyPatternDistribution(String pattern, Double modifier) {
        // TODO: Implement a pattern distribution modification
    }

    private void modifyActionDistribution(String action, Double modifier) {
        // TODO: Implement a action distribution modification
    }

    private void modifyMutagenLengthDistribution(Integer mutagenLength, Double modifier) {
        // TODO: Implement a mutagen length distribution modification
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++ i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        for (int i = 0; i < numTrials; ++ i) {
            // Get the genome
            String genome = randomString();
            String mutated = console.Mutate(genome);
            // Collect the change windows
            Vector<Pair<Integer, Integer>> changeWindows = new Vector<Pair<Integer, Integer>>();
            for (int j = 0; j < genome.length(); j++) {
                char before = genome.charAt(j);
                char after = mutated.charAt(j);
                if(before != after) {
                    int finish = j;
                    for(int forwardIndex = j + 1; forwardIndex < j + 10; forwardIndex++) {
                        // TODO: Handle the case when we are at the end of the genome (i.e. when j is 999)
                        if(genome.charAt(forwardIndex) == mutated.charAt(forwardIndex)) {
                            finish = forwardIndex - 1;
                            break;
                        }
                    }
                    changeWindows.add(new Pair<Integer, Integer>(j, finish));
                }
            }

            // Get the window sizes distribution and generate all possible windows
            int[] windowSizesCounts = new int[maxMutagenLength];
            Vector<Pair<Integer, Integer>> possibleWindows = new Vector<Pair<Integer, Integer>>();
            for (Pair<Integer, Integer> window: changeWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                int windowLength = finish - start + 1;
                windowSizesCounts[windowLength]++;
                if(windowLength == maxMutagenLength) {
                    // TODO: Handle the case if two smaller mutations occured side by side
                    possibleWindows.add(window);
                } else {
                    for (int proposedWindowLength = windowLength; proposedWindowLength <= maxMutagenLength; proposedWindowLength++) {
                        int diff = proposedWindowLength - windowLength;
                        // TODO: Handle the edge cases (i.e. start = 0 || 999)
                        for(int offset = -diff; offset <= 0; offset++) {
                            int newStart = start + offset;
                            int newFinish = newStart + proposedWindowLength;
                            possibleWindows.add(new Pair<Integer, Integer>(newStart, newFinish));
                        }
                    }
                }
            }

            // Modify the distributions for length
            for (int j = 0; j < maxMutagenLength; j++) {
                Double modifier = windowSizesCounts[j] * 1.0 / (changeWindows.size() / maxMutagenLength);
                modifyMutagenLengthDistribution(j + 1, modifier);
            }

            // Modify the distributions for pattens and actions
            for (Pair<Integer, Integer> window: possibleWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                // Get the string from
                String before = genome.substring(start, finish + 1);
                allPatterns.get(before.length()).add(before);
                // Get the string after
                String after = mutated.substring(start, finish + 1);
                // Modify the distribution
                modifyPatternDistribution(before, modifierStep);
                modifyActionDistribution(after, modifierStep);
            }

            // Sample a mutagen
            boolean foundGuess = false;
            Mutagen guess = new Mutagen();
            while (!foundGuess) {
                guess = sampleMutagen();
                if(!wrongMutagens.contains(guess)) {
                    foundGuess = true;
                }
            }
            boolean isCorrect = console.Guess(guess);
            if(isCorrect) {
                return guess;
            } else {
                // Record that this is not a correct mutagen
                wrongMutagens.add(guess);
            }
        }
        return sampleMutagen();
    }
}
