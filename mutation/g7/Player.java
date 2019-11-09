package mutation.g7;

import java.util.*;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;

import java.util.Map;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;


public class Player extends mutation.sim.Player {

    // START CONFIG ==================================

    // Time limit for when to break (in milliseconds)
    private long timeLimit = 1000;

    // Number of trials to run the algorithm for
    private int numTrials = 10000;

    // Maximum length of a mutagen
    private int maxMutagenLength = 10;
    private int overlap = 9;

    // Distribution increase parameters
    private Double modifierStep = 1.0;
    private Double initialStep = 1.0;

    // END CONFIG ====================================

    // START VARS ====================================

    private Random random;

    // A map to remember the occurance of rules
    private Map<String, Double> rules = new HashMap<>();
    private Map<String, Double> changeRules = new HashMap<>();

    // An array of all patterns that happened
    private HashSet<String> allPatterns = new HashSet<>();

    // An array to record the wrong guesses so we don't repeat them
    private HashSet<Mutagen> wrongMutagens = new HashSet<>();

    private Map<Integer, Double> lengthMap;
    private double numPerm = 0.0;
    private Double rulesLength = 0.0;
    private Double changeRulesLength = 0.0;

    private HashSet<String> discardedRules = new HashSet<>();
    private HashMap<Integer, HashSet<String>> buckets = new HashMap<>();

    // END VARS ======================================

    private String[] genes = "acgt".split("");
    private int iteration = 0;

    // A list of continually seen mutation lengths
    private LinkedList<Integer> mutationLengths = new LinkedList<>();

    public Player() {
        random = new Random();
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
            Double newProbability = prevProbability + modifierStep;
            Double diff = newProbability - prevProbability;
            rulesLength += diff;
            rules.put(rule, newProbability);
        } else {
            rulesLength += initialStep;
            rules.put(rule, initialStep);
        }
    }

    private void modifyChangeRuleDistribution(String pattern, String action) {
        String[] patternArr = pattern.split("");
        String patternKey = patternArr[0];
        for (int i = 1; i < patternArr.length; i++) {
            patternKey += ";" + patternArr[i];
        }
        String rule = patternKey + "@" + action;
        if (changeRules.containsKey(rule)) {
            Double prevProbability = changeRules.get(rule);
            Double newProbability = prevProbability + 1.0;
            Double diff = newProbability - prevProbability;
            changeRulesLength += diff;
            changeRules.put(rule, newProbability);
        } else {
            changeRulesLength += initialStep;
            changeRules.put(rule, initialStep);
        }
    }

    private void createBuckets(List<Map.Entry<String, Double>> rules) {
        for (Map.Entry<String, Double> key : rules) {
            if (buckets.containsKey(key.getKey().length())) {
                buckets.get(key.getKey().length()).add(key.getKey());
            } else {
                buckets.put(key.getKey().length(), new HashSet<>());
                buckets.get(key.getKey().length()).add(key.getKey());
            }
        }
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

    
    private HashMap<Integer, HashMap<Integer, Set<String>>> findSimilarRules(List<Map.Entry<String, Double>> pairs){
        Set<String> patterns = new HashSet<>(); 
        for (Map.Entry<String, Double> entry : pairs){
           String pattern = entry.getKey().split("@")[0];
           patterns.add(pattern);
        }
        Map<Pair<String, Integer>, Set<Map.Entry<String, Double>>> groups = new HashMap<>();
        
        for(String pattern : patterns){
            for (Map.Entry<String, Double> entry_2 : pairs){
                String pattern_2 = entry_2.getKey().split("@")[0];
                if(pattern.length() != pattern_2.length()) continue;
                int diff = 0; 
                int index = -1; 
                for (int i = 0; i < pattern.length(); i++){
                     if (pattern.charAt(i)!=pattern_2.charAt(i)) {
                         diff++; 
                         index = i; 
                     }
                     if (diff > 2) break; 
                }
                if (diff == 1){
                    Pair<String, Integer> sim = new Pair<>(pattern, index);
                    if(groups.containsKey(sim)){
                        groups.get(sim).add(entry_2);
                    } 
                    else{
                        Set<Map.Entry<String, Double>> entries = new HashSet<>(); 
                        entries.add(entry_2);
                        groups.put(sim, entries);
                    }
                }
            }
        }
        HashMap<Integer, HashMap<Integer, Set<String>>> megaMap = new HashMap<>();
        for(Pair<String, Integer> pair : groups.keySet()){
            String base = pair.getKey();
            String rule = pair.getKey();
            for(Map.Entry<String, Double> entry : groups.get(pair)){
                String pattern = entry.getKey().split("@")[0];
                if(base.charAt(pair.getValue())!=pattern.charAt(pair.getValue())){
                    rule = rule.substring(0,pair.getValue()) + pattern.charAt(pair.getValue()) + rule.substring(pair.getValue(), rule.length());
                }
            }

            String[] rule_arr = rule.split(";");
            String[] sorted_rule = new String[rule_arr.length];
            int ind = 0; 
            for (String s : rule_arr){
                char[] temp_s = s.toCharArray();
                Arrays.sort(temp_s);
                s = new String(temp_s);
                sorted_rule[ind++] = s; 
            }
            rule = String.join(";", sorted_rule);

            int length = (int) Math.ceil((double)base.length() / 2.0) ;
            int rules_combined = groups.get(pair).size() + 1; 
            
            if (megaMap.containsKey(length)){
                HashMap<Integer, Set<String>> map = megaMap.get(length);
                if (map.containsKey(rules_combined)){
                    map.get(rules_combined).add(rule);
                }
                else{
                    Set<String> rules = new HashSet<>();
                    rules.add(rule);
                    map.put(rules_combined, rules);
                }
            }
            else{
                HashMap<Integer, Set<String>> map = new HashMap<>();
                Set<String> rules = new HashSet<>();
                rules.add(rule);
                map.put(rules_combined, rules);
                megaMap.put(length, map);
            }
            
        
        }
        // for (Integer key : megaMap.keySet()){
        //     System.out.println("------" + key + "---------");
        //     for (Integer key_2 : megaMap.get(key).keySet()){
        //         System.out.println("child------" + key_2 + "---------");
        //         System.out.println(megaMap.get(key).get(key_2));
        //     }
        // }
        return megaMap;
    }

    private Mutagen getFinalMutagen() {
        Mutagen mutagen = new Mutagen();
        int numberOfRulesToCombine = allPatterns.size();
        if (numberOfRulesToCombine == 0) {
            numberOfRulesToCombine = 1;
        }
        purgeRules();
        List<Map.Entry<String, Double>> pairs = Utils.findGreatest(rules, 20, true);
        createBuckets(pairs);
        Collections.sort(pairs, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        discardRules(pairs);
        System.out.println(pairs);
        if (pairs.size() > 0) pairs = purge(pairs);
        for (Map.Entry<String, Double> patternActionPair : pairs) {
            String pair = patternActionPair.getKey();
            String[] patternAction = pair.split("@");
            String pattern = patternAction[0];
            String action = patternAction[1];
            mutagen.add(pattern, action);
        }
        return mutagen;
    }

    private boolean isExtension(Map.Entry<String, Double> p1, Map.Entry<String, Double> p2) {
        if(p1.getKey().length() == p2.getKey().length()) return false;
        Map.Entry<String, Double> longP = p1.getKey().length() > p2.getKey().length() ? p1 : p2;
        Map.Entry<String, Double> shortP = p1.getKey().length() < p2.getKey().length() ? p1 : p2;
        String l = longP.getKey().split("@")[0];
        String s = shortP.getKey().split("@")[0];
        return l.contains(s);
    }

    private void discardRules(List<Map.Entry<String, Double>> rules) {
        if (rules.size() == 0) return;
        List<Map.Entry<String, Double>> sameDist = new ArrayList<>();
        sameDist.add(rules.get(0));
        for (int i = 1; i < rules.size(); i++) {
            double a = rules.get(i - 1).getValue();
            double b = rules.get(i).getValue();
            double c = Math.abs(a - b);
            if (a > 10 && c < 0.01) {
                sameDist.add(rules.get(i));
            } else {
                if (sameDist.size() > 1) {
                    for (int j = 0; j < sameDist.size(); j++) {
                        for (int k = j + 1; k < sameDist.size(); k++) {
                            Map.Entry<String, Double> p1 = rules.get(j);
                            Map.Entry<String, Double> p2 = rules.get(k);
                            if (isExtension(p1, p2)) {
                                if (p1.getKey().length() > p2.getKey().length()) {
                                    discardedRules.add(p2.getKey());
                                } else discardedRules.add(p1.getKey());
                            }
                        }
                    }
                }
                sameDist.clear();
                sameDist.add(rules.get(i));
            }
        }
    }

    private List<Map.Entry<String, Double>> purge(List<Map.Entry<String, Double>> pairs) {
        Collections.sort(pairs, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        int i = 1;
        for (; i < pairs.size(); i++) {
            if (pairs.get(i - 1).getValue() != pairs.get(i).getValue()) break;
        }
        Collections.sort(pairs.subList(0, i), new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                if (o1.getKey().length() == o2.getKey().length()) return 0;
                if (o1.getKey().length() < o2.getKey().length()) return 1;
                return -1;
            }
        });
        i = 1;
        for (; i < pairs.size(); i++) {
            if (pairs.get(i - 1).getKey().length() != pairs.get(i).getKey().length()) break;
        }
        return pairs.subList(0, i);
    }

    private void purgeRules(){
        for(String pattern : discardedRules){
            if(rules.containsKey(pattern)) rules.remove(pattern);
        }
    }

    private String createString() {
        int n = allPatterns.size() != 0 ? allPatterns.size() : 1;
        List<Map.Entry<String, Double>> greatest = Utils.findGreatest(rules, n, true);
        List<Map.Entry<String, Double>> lowest = Utils.findGreatest(rules, n, false);
        String result = "";
        int counter = 0;
        while (result.length() < 1000) {
            String pair;
            if (counter % 3 == 0) {
                pair = greatest.get((int) Math.floor(Math.random() * n)).getKey();
                pair = pair.split("@")[0];
                pair = String.join("", pair.split(";"));
            } else if (counter % 2 == 0) {
                pair = lowest.get((int) Math.floor(Math.random() * n)).getKey();
                pair = pair.split("@")[0];
                pair = String.join("", pair.split(";"));
            } else {
                pair = Utils.randomString(20);
            }
            result += pair;
            counter++;
        }
        return result.substring(0, 1000);
    }

    private void updateProbabilities(Vector<Pair<Integer, Integer>> changeWindows, Vector<Pair<Integer, Integer>> possibleWindows, String genome, String mutated) {
        for(Pair<Integer, Integer> window : changeWindows) {
            int start = window.getKey();
            int finish = window.getValue();
            // Get the string from
            String before = genome.substring(start, finish + 1);
            // Get the string after
            String after = mutated.substring(start, finish + 1);

            // TO REMOVE vvvvvv USE
            allPatterns.add(before + "@" + after);
//            System.out.println(before + "@" + after);

            modifyChangeRuleDistribution(before, after);
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
    }

    private Mutagen guessSingleRule(int numberOfDisplacements) {
        return getFinalMutagen();
    }

    private int getNumberOfRules() {
        int initialNumberOfRules = changeRules.size();
        if (initialNumberOfRules == 0) {
            return  1;
        }
        int numberOfProbableOverlaps = 0;
        if(initialNumberOfRules > 0 && initialNumberOfRules <= 10) {
            List<Map.Entry<String, Double>> pairs = Utils.findGreatest(changeRules, initialNumberOfRules, false);
            Collections.reverse(pairs);
            Double prevCount = 1.0;
//            System.out.println(pairs);
            for (int j = 0; j < pairs.size(); j++) {
                Double thisCount = pairs.get(j).getValue();
                Double change = thisCount / prevCount;
//                System.out.println(thisCount + " / " + prevCount + " ? " + j);
                if(change > 3 + iteration / 100) {
                    numberOfProbableOverlaps = j;
                    break;
                }
                prevCount = thisCount;
            }
        }
        if(initialNumberOfRules > 10) {
            boolean changeFound = false;
            for (int i = 10; i < initialNumberOfRules; i+=10) {
                if(changeFound) {
                    break;
                }
                List<Map.Entry<String, Double>> pairs = Utils.findGreatest(changeRules, i, false);
                Collections.reverse(pairs);
                Double prevCount = 1.0;
                for (int j = 0; j < pairs.size(); j++) {
                    Double thisCount = pairs.get(j).getValue();
                    Double change = thisCount / prevCount;
                    if(change > 3 + iteration / 100) {
                        numberOfProbableOverlaps = j;
                        changeFound = true;
                        break;
                    }
                    prevCount = thisCount;
                }
            }
        }
//        System.out.println(initialNumberOfRules + " / " + numberOfProbableOverlaps + " / iteration " + iteration);
        return initialNumberOfRules - numberOfProbableOverlaps;
    }

    private Mutagen guessSimpleMultiple(int numberOfRules) {
        Mutagen mutagen = new Mutagen();
        int numberOfRulesToCombine = getNumberOfRules();
        List<Map.Entry<String, Double>> pairs = Utils.findGreatest(rules, numberOfRulesToCombine, true);
        for (Map.Entry<String, Double> patternActionPair : pairs) {
            String pair = patternActionPair.getKey();
            String[] patternAction = pair.split("@");
            String pattern = patternAction[0];
            String action = patternAction[1];
            mutagen.add(pattern, action);
        }
        return mutagen;
    }

    private Mutagen guessComplexMultiple(int numberOfRules) {
        return getFinalMutagen();
    }

    private Mutagen guessBest() {
        return getFinalMutagen();
    }

    @Override
    public Mutagen Play(Console console, int m) {
        for (int i = 0; i < numTrials; ++i) {
            iteration = i;
            // Check if we should terminate and return the final mutagen
            System.out.println(i);
            int numExpsLeft = console.getNumExpsLeft();
            long timeLeft = console.getTimeLeft();
            if ((numExpsLeft < 2 || timeLeft < timeLimit)) {
                System.out.println("Returning final Mutagen");
                return guessBest();
            }

            // Get the genome
            String genome;
            if (i == 0 || i > 50) genome = Utils.randomString(1000);
            else genome = Utils.randomString(1000);
            String mutated = console.Mutate(genome);

            Vector<Pair<Integer, Integer>> changeWindows = Window.getWindows(genome, mutated, console.getNumberOfMutations());

            int genome_length = genome.length();
            genome = genome.substring(genome_length - overlap, genome_length) + genome + genome.substring(0, overlap);
            mutated = mutated.substring(mutated.length() - overlap, mutated.length()) + mutated + mutated.substring(0, overlap);

            Vector<Pair<Integer, Integer>> possibleWindows = Window.getPossibleWindows(changeWindows, genome, mutated);

            updateProbabilities(changeWindows, possibleWindows, genome, mutated);

            Mutagen guess;

            guess = guessSimpleMultiple(2);
            System.out.println("Guessing " + guess.getPatterns() + " / " + guess.getActions());
//            if(0 <= i && i < 100) {
//                guess = guessSingleRule(0);
//            } else if (100 <= i && i < 200) {
//                guess = guessSingleRule(1);
//            } else if (200 <= i && i < 300) {
//                guess = guessSimpleMultiple(2);
//            } else if (300 <= i && i < 400) {
//                guess = guessComplexMultiple(2);
//            } else {
//                guess = guessSingleRule(2);
//            }

            boolean isCorrect = console.testEquiv(guess);
            if (isCorrect) {
                return guess;
            } else {
                // Record that this is not a correct mutagen
                wrongMutagens.add(guess);
            }
        }
        return getFinalMutagen();
    }
}
