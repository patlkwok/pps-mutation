package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javafx.util.Pair;

public class Player extends mutation.sim.Player {
    private Random random;

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

    private String defaultString() {
        return "aaaaaaaaaaccccccccccggggggggggttttttttttacacacacacacacacacacagagagagagagagagagagatatatatatatatatatat" +
                "cgcgcgcgcgcgcgcgcgcgctctctctctctctctctctgtgtgtgtgtgtgtgtgtgtacgacgacgaacgacgacgaacgacgacgaacgacgacga" +
                "acgacgacgaacgacgacgaactactactaactactactaactactactaactactactaactactactaactactactaagtagtagtaagtagtagta" +
                "agtagtagtaagtagtagtaagtagtagtaagtagtagtacgtcgtcgtccgtcgtcgtccgtcgtcgtccgtcgtcgtccgtcgtcgtccgtcgtcgtc" +
                "acgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtac" +
                "acgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtac" +
                "acgtacgtacacgtacgtacacgtacgtacacgtacgtacaaaaaaaaaaccccccccccggggggggggttttttttttacacacacacacacacacac" +
                "agagagagagagagagagagatatatatatatatatatatcgcgcgcgcgcgcgcgcgcgctctctctctctctctctctgtgtgtgtgtgtgtgtgtgt" +
                "acgacgacgaacgacgacgaacgacgacgaacgacgacgaacgacgacgaacgacgacgaactactactaactactactaactactactaactactacta" +
                "actactactaactactactaagtagtagtaagtagtagtaagtagtagtaagtagtagtaagtagtagtaagtagtagtacgtcgtcgtccgtcgtcgtc";
    }

    private ArrayList<Pair<Integer, Integer>> getPossibleWindows(String beforeMutation, String afterMutation, int m) {
        int n = beforeMutation.length();
        ArrayList<Integer> differenceLocation = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            if (beforeMutation.charAt(i) != afterMutation.charAt(i))
                differenceLocation.add(i);
        }
        ArrayList<Pair<Integer, Integer>> result = new ArrayList<>();
        int prev = differenceLocation.get(0);
        for (int i = 0; i < differenceLocation.size(); ++i) {
            if (i == differenceLocation.size() - 1) {
                if (differenceLocation.get(i) - prev <= 10)
                    result.add(new Pair<>(prev, Math.min(prev + 10, 999)));
                else {
                    int curr = differenceLocation.get(i);
                    result.add(new Pair<>(prev, Math.min(prev + 10, 999)));
                    result.add(new Pair<>(curr, Math.min(curr + 10, 999)));
                }
            }
            else if (differenceLocation.get(i) - prev <= 10)
                continue;
            else {
                result.add(new Pair<>(prev, Math.min(prev + 10, 999)));
                prev = differenceLocation.get(i);
            }
        }
        return result;
    }

    private ArrayList<String> getPossiblePattern(String windowStr){
        ArrayList<String> pattern = new ArrayList<>();
        for (int i = 0; i < windowStr.length(); i++){
            for (int j = i; j < windowStr.length(); j++){
                pattern.add(windowStr.substring(i, j+1));
            }
        }
        return pattern;
    }

    private ArrayList<String> getPossibleActions2(String beforeMutation, String afterMutation, int startIdx) {
        ArrayList<String> actions = new ArrayList<>();
        if(beforeMutation.length() == 0) {
            actions.add("");
            return actions;
        }

        char after = afterMutation.charAt(0);

        ArrayList<String>prefixActionStrings = new ArrayList<String>();
        prefixActionStrings.add(Character.toString(after));

        for(int j=0; j < beforeMutation.length(); j++) {
            if(beforeMutation.charAt(j) == after) {
                prefixActionStrings.add(Integer.toString(j + startIdx));
            }
        }

        ArrayList<String> actionStrings = getPossibleActions2(
                beforeMutation.substring(1), afterMutation.substring(1), startIdx + 1);

        for(String prefixString : prefixActionStrings) {
            for(String actionString : actionStrings) {
                String fullString = prefixString + actionString;
                actions.add(fullString);
            }
        }

        return actions;
    }

    private ArrayList<String> getPossibleActions(String beforeMutation, String afterMutation) {
        int lastChangeIdx = -1;
        for(int i=0; i < beforeMutation.length(); i++) {
            if(beforeMutation.charAt(i) != afterMutation.charAt(i)) {
                lastChangeIdx = i;
            }
        }
        String beforeMutationSub = beforeMutation.substring(0, lastChangeIdx + 1);
        String afterMutationSub = afterMutation.substring(0, lastChangeIdx + 1);
        return getPossibleActions2(beforeMutationSub, afterMutationSub, 0);
    }
    
    /**
     * Returns a boolean if the pattern:action pair matches a possible mutation
     * Assumption: One substitution mutation per window
     * 
     * @param pattern: Pattern to search in window
     * @param action: Action to perform if pattern is found
     * @param window: Original window
     * @param mutatedWindow: Window with isolated mutation
     * @return true if pattern:action pair matches the mutated window
     */
    private boolean explain(String pattern, String action, String window, String mutatedWindow) {
    	char[] actionArr = action.toCharArray();
    	char[] windowArr = window.toCharArray();
    	
    	// Search for pattern in window
    	int index = window.indexOf(pattern);
    	while(index >= 0) {
    		
    		// For every occurrence of the pattern try the mutation
    		char[] windowArrTemp = windowArr.clone();
    		for(int i = 0; i < actionArr.length; i ++) {
    			if(index + i < windowArrTemp.length) {
    				windowArrTemp[index + i] = actionArr[i];
    			}
    		}
    		
    		// Check of mutation at a location matches the mutated window
    		String mutatedTemp = new String(windowArrTemp);
    		if(mutatedTemp.equals(mutatedWindow)) {
    			// Match Found
    			return true;
    		}
    		
    		// Find next pattern in window, starting from next index after the occurrence of the last pattern
    		index = window.indexOf(pattern, index + 1);
    	}
    	// No matches found
    	return false;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        Mutagen result = new Mutagen();
        //result.add("a;c;c", "att");
        //result.add("g;c;c", "gtt");

        Set<String>addedRules = new HashSet<>();

        ArrayList<Pair<String, String>> candidateRules = new ArrayList<>();
        for (int i = 0; i < 10; ++ i) {
            String genome = defaultString();
            String mutated = console.Mutate(genome);

            ArrayList<Pair<Integer, Integer>> possibleWindows = this.getPossibleWindows(genome, mutated, m);
            for(Pair <Integer, Integer> p : possibleWindows) {
                String beforeMutation = genome.substring(p.getKey(), p.getValue());
                String afterMutation = mutated.substring(p.getKey(), p.getValue());
                
                ArrayList<String> possiblePatterns = this.getPossiblePattern(beforeMutation);
                ArrayList<String> possibleActions = this.getPossibleActions(beforeMutation, afterMutation);

                for(String patternString : possiblePatterns) {
                    for (String actionString : possibleActions) {
                        String ruleString = patternString + "=" + actionString;
                        
                        // If pattern is longer than action then continue. This case was not seen in the class
                        if(patternString.length() > actionString.length()) {
                        	continue;
                        }
                        
                        if(! addedRules.contains(ruleString)) {
                            candidateRules.add(new Pair <String,String>(patternString, actionString));
                            addedRules.add(ruleString);
                        }
                        
                        // If pattern:action pair is possible add it to results
                        if(explain(patternString, actionString, beforeMutation, afterMutation)) {
                            //only add to result if haven't seen before
                        	result.add(patternString, actionString);
                        }
                    }
                }
            }

            console.Guess(result);
        }
        //deduplicate result
        //result.patterns
        System.out.println(result.getPatterns());
        return result;
    }
}
