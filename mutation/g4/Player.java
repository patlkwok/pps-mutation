// deliverable 10-28
package mutation.g4;

import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;
import mutation.sim.Console;
import mutation.sim.Mutagen;
import javafx.util.Pair;

public class Player extends mutation.sim.Player {
	private double epsilon = 0.03;
    private Random random;
    private List<Mutation> mutations;
    // private Map<Integer, Map<Pattern, Set<String>>> windowPatternBeforeMap;
    private Map<Pattern, Set<String>> patternBeforeMap;
    private List<Pattern> potentialPatterns;
    private Map<Rule, Set<Mutation>> ruleMutationMap;
    private final int maxRuleLength = 10;
    private final int maxPotentialPatterns = 3;
    private int suspectedOverlappingMutations = 0;
    private final String GENERIC_PATTERN = "acgt;acgt;acgt;acgt;acgt;acgt;acgt;acgt;acgt;acgt";

    public Player() {
        random = new Random();
        this.mutations = new ArrayList<>();
        // this.windowPatternBeforeMap = new HashMap<>();
        this.patternBeforeMap = new HashMap<>();
        this.potentialPatterns = new ArrayList<>();
        this.ruleMutationMap = new HashMap<>();
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
    	for (int i = 0; i < maxPotentialPatterns; i++) {
    		potentialPatterns.add(new Pattern(GENERIC_PATTERN));
    	}
        Mutagen result = new Mutagen();
        for (int i = 0; i < 1000; i++) {
        	String genome = randomString();
            String mutated = console.Mutate(genome);
    	    Set<Set<Mutation>> mutationsSet = identifyMutations(genome, mutated);
    	    for (Set<Mutation> mutations : mutationsSet) {
    	    	Pattern pattern = assignAMutationToAPattern(mutations);
    	    	if (pattern != null) {
    	    		pattern.recalibrate();
    	    	}
    	    }
        }
        // List<Pattern> topPatterns = getTopPatterns(1);
        // System.out.println("Ending: ");
        for (Pattern topPattern : potentialPatterns) {
        	System.out.println(topPattern + " Evidence: " + topPattern.getEvidenceSize());
        }
        return result;
    }

    private List<Pattern> getTopPatterns(int n) {
    	Collections.sort(potentialPatterns, new Comparator<Pattern>() {
    		@Override
    		public int compare(Pattern p1, Pattern p2) {
    			return p1.getEvidenceSize() - p2.getEvidenceSize();
    		}
    	});
    	Pattern genericPattern = new Pattern(GENERIC_PATTERN);
    	return potentialPatterns.stream().filter(p -> p.equals(genericPattern)).limit(n).collect(Collectors.toList());
    }

    private void printMutations(List<Mutation> mutations) {
    	System.out.println("Printing Mutations: " + mutations.size());
    	// for (Mutation mutation : mutations) {
    	// 	System.out.println(mutation.toString());
    	// }
    }

    public void initializePotentialPatterns() {
    	for (int i = 0; i < maxPotentialPatterns; i++) {
        	potentialPatterns.add(new Pattern(GENERIC_PATTERN));
        }
    }

    public Pattern assignAMutationToAPattern(Set<Mutation> mutations) {
    	System.out.println("Mutations size: " + mutations.size()); 
    	Pattern bestPattern = null;
    	Mutation bestMutation = null;
    	double bestScore = 0.0;
    	for (Mutation mutation : mutations) {
    		String before = mutation.getBefore();
        	for (int i = 0; i < potentialPatterns.size(); i++) {
        		Pattern potentialPattern = potentialPatterns.get(i);
        		double score = potentialPattern.similarityScore(mutation.getBefore());
            	if (score > bestScore) {
            		bestPattern = potentialPattern;
            		bestMutation = mutation;
            		bestScore = score;
            	}
          	}
        }
        System.out.println("Best Pattern: " + bestPattern);
        System.out.println("Best Mutation: " + bestMutation);
        System.out.println("Best Score: " + bestScore);
        if (bestPattern != null) {
        	bestPattern.addEvidence(bestMutation);
        	System.out.println("Adding Pattern: " + bestPattern.toString());
        	System.out.println("Mutation: Before: " + bestMutation.getBefore() + " After: " + bestMutation.getAfter());
        } else {
        	System.out.println("No Pattern with similarity above 0.0");
        }
        return bestPattern;
    }

    private void printPotentialPatterns() {
    	for (Pattern potentialPattern : potentialPatterns) {
    		System.out.println(potentialPattern.toString());
    	}
    }

    private void reducePotentialPatterns() {
    	// for each pattern, check if any other pattern is the same. If so, merge them and their mutations.
    }

    private Set<Set<Mutation>> identifyMutations(String before, String after) {
    	Set<Set<Mutation>> mutationsSet = new HashSet<>();
    	if (before.length() != after.length()) {
    		throw new RuntimeException("There can be neither deletions nor insertions");
    	}
    	int length = before.length();
    	// we do not want the before to differ from after in the first 10 or the last 10, but we do not want index out of bounds when computing windows.
    	// this is hacky but it accomplishes the desired effect.
    	before = after.substring(maxRuleLength) + before + after.substring(length-maxRuleLength, length);
    	after = after.substring(maxRuleLength) + after + after.substring(length-maxRuleLength, length);
    	int i = 0;
    	while (i < before.length()) {
    		if (!(before.charAt(i) == after.charAt(i))) {
    			int start = i;
    			int end = i;
    			int next;
    			while ((next = getNextDifferenceWithinMaxRuleLength(before, after, end)) != end) {
    				end = next;
    			}
    			if (end < start + maxRuleLength) {
    				mutationsSet.add(generateSlidingWindows(before, after, start, end, maxRuleLength));
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
    	for (int i = 1; i < maxRuleLength; i++) {
    		if (before.charAt(index + i) != after.charAt(index + i)) return index + i;
    	}
    	return index;
    }

    private Set<Mutation> generateSlidingWindows(String before, String after, int start, int end, int maxRuleLength) {
    	Set<Mutation> mutations = new HashSet<>();
    	for (int i = end-maxRuleLength+1; i <= start; i++) {
    		Mutation mutation = new Mutation(before.substring(i, i+maxRuleLength), after.substring(i, i+maxRuleLength));
    		mutations.add(mutation);
    	}
    	return mutations;
    }
}
