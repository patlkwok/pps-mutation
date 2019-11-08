// deliverable 10-28
package mutation.g4Ethan;

import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;
import mutation.sim.Console;
import mutation.sim.Mutagen;
import javafx.util.Pair;

public class Player extends mutation.sim.Player {
    private Console console;
    private Random random;
    private List<Mutation> mutations = new ArrayList<>();
    private static final int MAX_RULE_LENGTH = 10;
    private static final int MIN_EVIDENCE = 10;
    private int suspectedOverlappingMutations = 0;
    private List<MutationGroup> mutationGroups = new ArrayList<>();

    public Player() {
        random = new Random();
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
        this.console = console;
        Mutagen result = new Mutagen();
        // write an outer for loop on this for random restarts
        for (int i = 0; i < 1001; i++) {
        	String genome = randomString();
            String mutated = console.Mutate(genome);
    	    Set<List<Mutation>> mutationsSet = identifyMutations(genome, mutated);
            System.out.println("" + i);
    	    for (List<Mutation> mutations : mutationsSet) {
                assignAMutationToAMutationGroup(mutations);
    	    }
            if (i%10==0) {
                guessTopMutations(Math.min(mutationGroups.size(), 3));
            }
            if (i%100==0) {
                guessTopMutations(Math.min(mutationGroups.size(), 5));
            }
            if (i%1000==0) {
                guessTopMutations(Math.min(mutationGroups.size(), 10));
                // mergeMutations();
            }
        }
        // This is written brute force, assuming mutagens have at most 3 rules.
        // for (MutationGroup mg : mutationGroups) {
        //     System.out.println("Mutation Group Size: " + mg.getNumMutations());
        //     System.out.println("Pattern: " + mg.getRule().getKey());
        //     System.out.println("Action: " + mg.getRule().getValue());
        // }
        return result;
    }

    public void mergeMutations() {
        System.out.println("Size Before: " + mutationGroups.size());
        List<MutationGroup> newMutationGroups = new ArrayList<>();
        for (int i = 0; i < mutationGroups.size(); i++) {
            boolean isContained = false;
            String[] oldPattern = mutationGroups.get(i).getPattern().split(";");
            for (int j = 0; j < newMutationGroups.size(); j++) {
                String[] newPattern = newMutationGroups.get(j).getPattern().split(";");
                if (contains(newPattern, oldPattern)) {
                    isContained = true;
                    newMutationGroups.get(j).addAll(mutationGroups.get(i).getMutations());
                    break;
                } 
            }
            if (!isContained) newMutationGroups.add(mutationGroups.get(i));
        }
        mutationGroups = newMutationGroups;
        System.out.println("Size After: " + mutationGroups.size());
    }

    private boolean contains(String[] p1, String[] p2) {
        if (p1.length != p2.length) return false;
        for (int i = 0; i < p1.length; i++) {
            Set<Character> p1Chars = new HashSet<>();
            Set<Character> p2Chars = new HashSet<>();
            for (char c1 : p1Chars) {
                p1Chars.add(c1);
            }
            for (char c2 : p2Chars) {
                p2Chars.add(c2);
            }
            p2Chars.removeAll(p1Chars);
            if (p2Chars.size() > 0) return false;
        }
        return true;
    }

    public void guessTopMutations(int n) {
        List<MutationGroup> topMutationGroups = new ArrayList<>(mutationGroups);
        // topMutationGroups = topMutationGroups.stream().filter(mg -> mg.getNumMutations() > MIN_EVIDENCE).collect(Collectors.toList());
        Collections.sort(topMutationGroups, new Comparator<MutationGroup>() {
            @Override
            public int compare(MutationGroup mg1, MutationGroup mg2) {
                return mg2.getNumMutations() - mg1.getNumMutations();
            }
        });
        topMutationGroups = topMutationGroups.subList(0, n);
        guessOneRule(topMutationGroups);
        guessTwoRules(topMutationGroups);
        guessThreeRules(topMutationGroups);
    }

    public void guessOneRule(List<MutationGroup> topMutationGroups) {
        // System.out.println("Guessing 1 rule");
        Mutagen result;
        for (int i = 0; i < topMutationGroups.size(); i++) {
            result = new Mutagen();
            MutationGroup mutationGroup = topMutationGroups.get(i);
            Pair<String, String> patternAction = mutationGroup.getRule();
            result.add(patternAction.getKey(), patternAction.getValue());
            console.testEquiv(result);
            if (console.isCorrect()) System.exit(0);
        }
    }

    public void guessTwoRules(List<MutationGroup> topMutationGroups) {
        // System.out.println("Guessing 2 rules");
        Mutagen result;
        for (int i = 0; i < topMutationGroups.size(); i++) {
            for (int j = i+1; j < topMutationGroups.size(); j++) {
                result = new Mutagen();
                MutationGroup mutationGroupI = topMutationGroups.get(i);
                MutationGroup mutationGroupJ = topMutationGroups.get(j);
                Pair<String, String> patternActionI = mutationGroupI.getRule();
                Pair<String, String> patternActionJ = mutationGroupJ.getRule();
                result.add(patternActionI.getKey(), patternActionI.getValue());
                result.add(patternActionJ.getKey(), patternActionJ.getValue());
                console.testEquiv(result);
                if (console.isCorrect()) System.exit(0);            
            }
        }
    }

    public void guessThreeRules(List<MutationGroup> topMutationGroups) {
        // System.out.println("Guessing 3 rules");
        Mutagen result;
        for (int i = 0; i < topMutationGroups.size(); i++) {
            for (int j = i+1; j < topMutationGroups.size(); j++) {
                for (int k = j+1; k < topMutationGroups.size(); k++) {
                    result = new Mutagen();
                    MutationGroup mutationGroupI = topMutationGroups.get(i);
                    MutationGroup mutationGroupJ = topMutationGroups.get(j);
                    MutationGroup mutationGroupK = topMutationGroups.get(k);
                    Pair<String, String> patternActionI = mutationGroupI.getRule();
                    Pair<String, String> patternActionJ = mutationGroupJ.getRule();
                    Pair<String, String> patternActionK = mutationGroupK.getRule();
                    result.add(patternActionI.getKey(), patternActionI.getValue());
                    result.add(patternActionJ.getKey(), patternActionJ.getValue());
                    result.add(patternActionK.getKey(), patternActionK.getValue());
                    console.testEquiv(result);
                    if (console.isCorrect()) System.exit(0);            
                }
            }
        }
    }

    public void assignAMutationToAMutationGroup(List<Mutation> mutations) {
        // if not similar enough, create a new mutation group.
        MutationGroup bestMutationGroup = null;
        Mutation bestMutation = null;
        double bestSimilarity = 10.0;
        // double[][] similarityMatrix = new double[mutations.size()][mutationGroups.size()]
        for (int i = 0; i < mutations.size(); i++) {
            Mutation mutation = mutations.get(i);
            for (int j = 0; j < mutationGroups.size(); j++) {
                MutationGroup mutationGroup = mutationGroups.get(j);
                double similarity = mutationGroup.similarity(mutation);
                if (similarity > bestSimilarity) {
                    bestMutationGroup = mutationGroup;
                    bestMutation = mutation;
                    bestSimilarity = similarity;
                }
            }
        }
        if (bestMutationGroup == null) {
            bestMutationGroup = new MutationGroup();
            bestMutation = mutations.get(random.nextInt(mutations.size()));
            mutationGroups.add(bestMutationGroup);
        }
        bestMutationGroup.add(bestMutation);
    }

    private Set<List<Mutation>> identifyMutations(String before, String after) {
    	Set<List<Mutation>> mutationsSet = new HashSet<>();
    	if (before.length() != after.length()) {
    		throw new RuntimeException("There can be neither deletions nor insertions");
    	}
    	int length = before.length();
    	// we do not want the before to differ from after in the first 10 or the last 10, but we do not want index out of bounds when computing windows.
    	// this is hacky but it accomplishes the desired effect.
    	before = after.substring(MAX_RULE_LENGTH) + before + after.substring(length-MAX_RULE_LENGTH, length);
    	after = after.substring(MAX_RULE_LENGTH) + after + after.substring(length-MAX_RULE_LENGTH, length);
    	int i = 0;
    	while (i < before.length()) {
    		if (!(before.charAt(i) == after.charAt(i))) {
    			int start = i;
    			int end = i;
    			int next;
    			while ((next = getNextDifferenceWithinMaxRuleLength(before, after, end)) != end) {
    				end = next;
    			}
    			if (end < start + MAX_RULE_LENGTH) {
    				mutationsSet.add(generateSlidingWindows(before, after, start, end));
    			} else {
    				suspectedOverlappingMutations++;
    			}
    			i = end+1;
    		}
    		i++;
    	}
    	return mutationsSet;
    }

    private int getNextDifferenceWithinMaxRuleLength(String before, String after, int index) {
    	for (int i = 1; i < MAX_RULE_LENGTH; i++) {
    		if (before.charAt(index + i) != after.charAt(index + i)) return index + i;
    	}
    	return index;
    }

    private List<Mutation> generateSlidingWindows(String before, String after, int start, int end) {
    	List<Mutation> mutations = new ArrayList<>();
    	for (int i = end-MAX_RULE_LENGTH+1; i <= start; i++) {
    		Mutation mutation = new Mutation(before.substring(i, i+MAX_RULE_LENGTH), after.substring(i, i+MAX_RULE_LENGTH));
    		mutations.add(mutation);
    	}
    	return mutations;
    }
}
