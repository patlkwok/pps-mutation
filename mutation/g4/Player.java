// deliverable 10-28
package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;
import javafx.util.Pair;

public class Player extends mutation.sim.Player {
    private Random random;
    private List<Mutation> mutations;
    private Map<Rule, Set<Mutation>> ruleMutationMap;
    private final int maxRuleLength = 10;
    private int suspectedOverlappingMutations = 0;
    private Set<Rule> simpleRules = new HashSet<>();

    public Player() {
        random = new Random();
        this.mutations = new ArrayList<>();
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
        Mutagen result = new Mutagen();
        for (int i = 0; i < 10; i++) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            identifyMutations(genome, mutated);
            
            // Pair<String,String> guess;
            // for (Mutation mutation : this.mutations) {
            //     guess = getSimpleGuess(mutation);
            //     result.add(guess.getKey(), guess.getValue());
                
            //     guess = getSimpleGuessBackOneIndex(mutation);
            //     result.add(guess.getKey(), guess.getValue());
            //     break;
            // }
            Set<Rule> simpleRulesCopy = new HashSet<>(simpleRules);
            System.out.println(simpleRules.size());
            for (Rule simpleRule : simpleRulesCopy) {
            	result = new Mutagen();
            	simpleRules.remove(simpleRule);
            	// System.out.println("Guessing: ");
            	// System.out.println("Pattern: " + simpleRule.getPattern().toString());
            	// System.out.println("Action: " + simpleRule.getAction().toString());
      			result.add(simpleRule.getPattern().toString().toLowerCase(), simpleRule.getAction().toString().toLowerCase());
            	console.Guess(result);
            }
        }
        
        return result;
    }

    private Pair<String,String> getSimpleGuess( Mutation mutation ) {
        String beforeString = mutation.getBefore();
        String afterString  = mutation.getAfter();
        boolean isFirstMutation = true;
        String pattern = "";
        String action = "";
        
        for (int i = 0; i < beforeString.length(); i++) {
            if (!(beforeString.charAt(i) == afterString.charAt(i))) {
                if (isFirstMutation) {
                    pattern += beforeString.charAt(i) + ";";
                    action += afterString.charAt(i);
                    isFirstMutation = false;
                }
                else {
                    pattern += beforeString.charAt(i) + ";";
                    action  += afterString.charAt(i);
                }
            }
        }
        // delete last semi-colon
        pattern = pattern.substring(0, pattern.length() -1);
            
        Pair<String,String> guess = new Pair<>(pattern, action);
        return guess;
    }
    
    private Pair<String,String> getSimpleGuessBackOneIndex( Mutation mutation ) {
        String beforeString = mutation.getBefore();
        String afterString  = mutation.getAfter();
        boolean isFirstMutation = true;
        String pattern = "";
        String action = "";
        
        for (int i = 0; i < beforeString.length(); i++) {
            if (!(beforeString.charAt(i) == afterString.charAt(i))) {
                if (isFirstMutation) {
                    pattern += beforeString.charAt(i -1) + ";";
                    action += afterString.charAt(i -1);
                    pattern += beforeString.charAt(i) + ";";
                    action += afterString.charAt(i);
                    isFirstMutation = false;
                }
                else {
                    pattern += beforeString.charAt(i) + ";";
                    action  += afterString.charAt(i);
                }
            }
        }
        // delete last semi-colon
        pattern = pattern.substring(0, pattern.length() -1);
            
        Pair<String,String> guess = new Pair<>(pattern, action);
        return guess;
    }

    private void identifyMutations(String before, String after) {
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
    			//System.out.println("found new diff");
    			int start = i;
    			int end = i;
    			int next;
    			while ((next = getNextDifferenceWithinMaxRuleLength(before, after, end)) != end) {
    				end = next;
    			}
    			if (end < start + maxRuleLength) {
    				Rule rule = new Rule(before.substring(start, end+1), after.substring(start, end+1));
    				System.out.println("Rule: \n" + rule);
    				simpleRules.add(rule);
    				mutations.addAll(generateSlidingWindows(before, after, start, end));
    			} else {
    				suspectedOverlappingMutations++;
    			}
    			i = end+1;
    		}
    		i++;
    	}
    }

    private int getNextDifferenceWithinMaxRuleLength(String before, String after, int index) {
    	for (int i = 1; i < maxRuleLength; i++) {
    		if (before.charAt(index + i) != after.charAt(index + i)) return index + i;
    	}
    	return index;
    }

    private List<Mutation> generateSlidingWindows(String before, String after, int start, int end) {
    	List<Mutation> mutations = new ArrayList<>();
    	for (int i = end-maxRuleLength+1; i <= start; i++) {
    		Mutation mutation = new Mutation(before.substring(i, i+maxRuleLength), after.substring(i, i+maxRuleLength));
    		mutations.add(mutation);
    	}
    	return mutations;
    }
}
