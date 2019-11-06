package mutation.g7;

import java.util.*;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;

import java.util.Map;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;


public class Player extends mutation.sim.Player {

    private Random random;
    private Map<String, Double> rules = new HashMap<>();

    private Map<Integer, Double> lengthMap;
    private double numPerm = 0.0;
    private String[] genes = "acgt".split("");

    private int numTrials = 100;
    private Double rulesLength = 0.0;
    private Double takeAChanceWithLength = 0.05;

    // Distribution increase parameters
    private Double modifierStep = 1.5;
    private Double initialStep = 1.0;

    // Time limit for when to break (in milliseconds)
    private long timeLimit = 1000;

    // An array to record the wrong guesses so we don't repeat them
    private Vector<Mutagen> wrongMutagens = new Vector<>();

    private int maxMutagenLength = 10;

    // An array of all patterns that happened
    private HashSet<String> allPatterns = new HashSet<>();

    public Player() {
        random = new Random();
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
        int numberOfRulesToCombine = allPatterns.size();
        if (numberOfRulesToCombine == 0) {
            numberOfRulesToCombine = 1;
        }
        Vector<String> previousPatterns = new Vector<String>();
        for (int i = 0; i < numberOfRulesToCombine; i++) {
            for (int j = 0; j < 2000; j++) {
                String pair = samplePair();
                if (!previousPatterns.contains(pair)) {
                    String[] patternAction = pair.split("@");
                    String pattern = patternAction[0];
                    String action = patternAction[1];
                    mutagen.add(pattern, action);
                    previousPatterns.add(pair);
                    break;
                }
            }
        }
        return mutagen;
    }

    private void modifyRuleDistribution(String pattern, String action) {
        String[] patternArr = pattern.split("");
        String patternKey = patternArr[0];
        for (int i = 1; i < patternArr.length; i++) {
            patternKey += ";" + patternArr[i];
        }
        String rule = patternKey + "@" + action;
        if (rules.containsKey(rule)) {
            Double prevProbability = rules.get(rule);
            Double newProbability = prevProbability * modifierStep;
            Double diff = newProbability - prevProbability;
            rulesLength += diff;
            rules.put(rule, newProbability);
        } else {
            rulesLength += initialStep;
            rules.put(rule, initialStep);
        }
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }


    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> findGreatest(Map<K, V> map, int n, boolean isMinHeap) {
        Comparator<? super Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1) {
                V v0 = e0.getValue();
                V v1 = e1.getValue();
                if(isMinHeap) return v0.compareTo(v1);
                return v1.compareTo(v0);
            }
        };
        PriorityQueue<Map.Entry<K, V>> highest = new PriorityQueue<>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }
        List<Map.Entry<K, V>> result = new ArrayList<>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

    private Mutagen getFinalMutagen() {
        Mutagen mutagen = new Mutagen();
        int numberOfRulesToCombine = allPatterns.size();
        if (numberOfRulesToCombine == 0) {
            numberOfRulesToCombine = 1;
        }
        List<Map.Entry<String, Double>> pairs = findGreatest(rules, numberOfRulesToCombine, true);
        for (Map.Entry<String, Double> patternActionPair : pairs) {
            String pair = patternActionPair.getKey();
            String[] patternAction = pair.split("@");
            String pattern = patternAction[0];
            String action = patternAction[1];
            mutagen.add(pattern, action);
        }
        return mutagen;
    }


    private static void printWindows( Map<Integer, LinkedList<Integer>> mutations, String genome, String mutant){
        Map<Integer, LinkedList<Integer>> sorted = mutations.entrySet().stream().sorted(comparingByKey()).collect( toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
        for (Integer i : sorted.keySet()){
            // I is current centroid
            System.out.println("Mutation " + i);
            for (Integer j : sorted.get(i))
                System.out.println(genome.charAt(j) + " ->" +mutant.charAt(j) );
        }
    }

    private String createString(){
        int n = allPatterns.size() != 0 ? allPatterns.size() : 1;
        List<Map.Entry<String, Double>> greatest = findGreatest(rules, n, true);
        List<Map.Entry<String, Double>> lowest = findGreatest(rules, n, false);
        String result = "";
        int counter = 0;
        while(result.length() < 1000){
            String pair;
            if(counter % 2 == 0){
                pair = greatest.get((int) Math.floor(Math.random() * n)).getKey();
            }
            else{
                pair = lowest.get((int) Math.floor(Math.random() * n)).getKey();
            }
            pair = pair.split("@")[0];
            result += String.join("", pair.split(";"));
            counter++;
        }
        return result.substring(0, 1000);
    }

    @Override
    public Mutagen Play(Console console, int m) {

        for (int i = 0; i < numTrials; ++i) {
            // Check if we should terminate and return the final mutagen
            int numExpsLeft = console.getNumExpsLeft();
            long timeLeft = console.getTimeLeft();
            if ((numExpsLeft < 2 || timeLeft < timeLimit)) {
                System.out.println("Returning final Mutagen");
                return getFinalMutagen();
            }

            // Get the genome
            String genome;
            if(i == 0) genome = randomString();
            else genome = createString();
            String mutated = console.Mutate(genome);

            Cluster cluster = new Cluster(genome, mutated);
            Map<Integer, LinkedList<Integer>> mutations = cluster.findWindows(m);
            // This if statement is just to check if correctly identified windows

            int genome_length = genome.length();

            // Add 10 last characters to beginning and 10 first characters to end of genome before / after mutation 
            // To simulate wrapping around 
            int overlap = 9;
            genome = genome.substring(genome_length - overlap, genome_length) + genome + genome.substring(0, overlap);
            mutated = mutated.substring(mutated.length() - overlap, mutated.length()) + mutated + mutated.substring(0, overlap);
            // Collect the change windows
            Vector<Pair<Integer, Integer>> changeWindows = new Vector<Pair<Integer, Integer>>();

            Map<Integer, LinkedList<Integer>> sorted = mutations.entrySet().stream().sorted(comparingByKey()).collect( toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
            for (Integer centroid : sorted.keySet()){
                int start = 1000;
                int finish = 0;
                for (Integer j : sorted.get(centroid)) {
                    if(j < start) {
                        start = j;
                    }
                    if(j  > finish) {
                        finish = j;
                    }
                }
                changeWindows.add(new Pair<Integer, Integer>(overlap + start, overlap + finish));
            }
            System.out.println(changeWindows);
            // Get the window sizes distribution and generate all possible windows
            int[] windowSizesCounts = new int[maxMutagenLength];
            Vector<Pair<Integer, Integer>> possibleWindows = new Vector<Pair<Integer, Integer>>();
            for (Pair<Integer, Integer> window : changeWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                String before = genome.substring(start, finish + 1);
                if (before.length() - 1 >= maxMutagenLength) continue;
                System.out.println(before);
                allPatterns.add(before);
                int windowLength = finish - start + 1;
                windowSizesCounts[windowLength - 1]++;
                if (windowLength == maxMutagenLength) {
                    possibleWindows.add(window);
                } else {
                    for (int proposedWindowLength = windowLength; proposedWindowLength <= maxMutagenLength; proposedWindowLength++) {
                        int diff = proposedWindowLength - windowLength;
                        for (int offset = -diff; offset <= 0; offset++) {
                            int newStart = start + offset;
                            int newFinish = newStart + proposedWindowLength - 1;
                            possibleWindows.add(new Pair<Integer, Integer>(newStart, newFinish));
                        }
                    }
                }
            }
            // Modify the distributions for pattens and actions
            for (Pair<Integer, Integer> window : possibleWindows) {
                int start = window.getKey();
                int finish = window.getValue();
                // Get the string from
                String before = genome.substring(start, finish + 1);
                // Get the string after
                String after = mutated.substring(start, finish + 1);
                // Modify the distribution
                modifyRuleDistribution(before, after);
            }

            Mutagen guess;
            if(i < 100){
                guess = getFinalMutagen();
            }
            else{
                // Sample a mutagen
                boolean foundGuess = false;
                guess = new Mutagen();
                while (!foundGuess) {
                    guess = sampleMutagen();
                    System.out.println("[>] Guessing");
                    if (!wrongMutagens.contains(guess)) {
                        foundGuess = true;
                    }
                }
            }

            boolean isCorrect = console.testEquiv(guess);
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
