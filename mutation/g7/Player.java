package mutation.g7;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;


public class Player extends mutation.sim.Player {

    private Random random;
    private Map<String, Double> lhs;
    private Map<String, Double> rhs;
    private Set<String> combs;
    private int count = 0;
    private double lhsPerm = 0.0;
    private double rhsPerm = 0.0;
    private String[] genes = "acgt".split("");

    // An array to record the wrong guesses so we don't repeat them
    private Vector<Mutagen> wrongMutagens = new Vector<Mutagen>();

    private int maxMutagenLength = 2;

    public Player() {
        random = new Random();
        lhs = new HashMap<>();
        rhs = new HashMap<>();
        combs = new HashSet<>();
        setLHSPerm(2);
        setRHSPerm(2);
        generateCombinations("", 4);
        generateLHSMap("", 2);
        generateRHSMap("", 2);
        System.out.println(count);
    }

    private void setLHSPerm(int n){
        for(int i = 1; i <= n; i++){
            lhsPerm += Math.pow(340, i);
        }
    }

    private void setRHSPerm(int n){
        for(int i = 1; i <= n; i++){
            rhsPerm += Math.pow(4, i);
        }
    }

    private void generateLHSMap(String result, int n){
        if(n == 0){
            lhs.put(result, 1 / lhsPerm);
            return;
        }
        String tmp = result;
        for(String c : combs){
            if(!result.equals("")) tmp = result + ";";
            generateLHSMap(tmp + c, n - 1);
        }
        if(!result.equals("")) {
            lhs.put(result, 1 / lhsPerm);
        }
    }

    private void generateRHSMap(String result, int n){
        if(n == 0){
            rhs.put(result, 1 / rhsPerm);
            count++;
            return;
        }
        String tmp = result;
        for(String c : genes){
            if(!result.equals("")) tmp = result + ";";
            generateRHSMap(tmp + c, n - 1);
        }
        if(!result.equals("")) {
            count++;
            rhs.put(result, 1 / rhsPerm);
        }
    }

    private void generateCombinations(String result, int n){
        if(n == 0){
            combs.add(result);
            return;
        }
        for(int i = 0; i < 4; i++){
            generateCombinations(result + genes[i], n - 1);
        }
        if(!result.equals("")) combs.add(result);
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
        for (int i = 0; i < 10; ++ i) {
            // Get the genome
            String genome = randomString();
            String mutated = console.Mutate(genome);
            // Collect the change windows
            Vector<Pair<int, int>> changeWindows = new Vector<Pair<int, int>>();
            for (int j = 0; j < genome.length(); j++) {
                char before = genome.charAt(j);
                char after = mutated.charAt(j);
                if(before != after) {
                    int start = j;
                    int finish = j;
                    for(int forwardIndex = j + 1; forwardIndex < j + 10; forwardIndex++) {
                        // TODO: Handle the what to do at the end of the genome (i.e. when j is 999)
                        if(genome.charAt(forwardIndex) == mutagen.charAt(forwardIndex)) {
                            finish = forwardIndex - 1;
                            break;
                        }
                    }
                    changeWindows.add(new Pair<int, int>(start, finish));
                }
            }
            // Get the window sizes distribution and generate all possible windows
            int[] windowSizesCounts = new int[maxMutagenLength];
            Vector<Pair<int, int>> possibleWindows = new Vector<Pair<int, int>>();
            for (Pair<int, int> window: changeWindows) {
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
                            possibleWindows.add(new Pair<int, int>(newStart, newFinish));
                        }
                    }
                }
            }
            // Modify the distributions for length
            for (int j = 0; j < maxMutagenLength; j++) {
                float modifier = windowSizesCounts[j] / changeWindows.size();
                // TODO: Modify the distribution
                // distribution.modifyLength(j, modifier);
            }
            // Modify the distributions for pattens and actions
            for (Pair<int, int> window: possibleWindows) {
                // TODO: Get the string from
                // String before = ;
                // TODO: Get the string after
                // String after = ;
                // TODO: Modify the distribution
                // distribution.modifyPatter(before, modifier);
                // distribution.modifyAction(after, modifier);
            }

            // Sample a mutagen
            boolean foundGuess = false;
            Mutagen guess;
            while (!foundGuess) {
                // TODO: Sample
                // guess = ;
                if(!wrongMutagens.contains(guess)) {
                    foundGuess = true;
                }
            }
            boolean isCorrect = console.Guess(result);
            if(isCorrect) {
                return result;
            } else {
                // Record that this is not a correct mutagen
                wrongMutagens.add(guess);
            }
        }
    }
}
