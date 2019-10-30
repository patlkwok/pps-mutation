package mutation.g8;

import java.util.*;
import javafx.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {
    private Random random;
    int HIGHCOUNT = 3;

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
        HashMap<Pair<String,String>, Integer> rules = new HashMap<>();
        Mutagen result = new Mutagen();
        for (int i = 0; i < 10; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            ArrayList<Integer> mutationPositions = getMutationPositions(genome, mutated);
            int lastMutationPosition = -11;
            int mutationRange = 10;
            for (int j = 0; j < mutationPositions.size(); j++){
                //if the next mutation position is outside the range of things we've already checked
                if (lastMutationPosition+mutationRange < mutationPositions.get(j)){
                    rules = addMutationChange(rules, genome, mutated, mutationPositions, j);
                    lastMutationPosition = mutationPositions.get(j);
                }
            }
            result = rulesToMutagen(rules);
            console.Guess(result);
            if (console.isCorrect()){
                return result;
            }
        }
        return result;
    }

    //Take a hashmap of pairs of strings (pattern and action) that make up a rule to integers, 
    //and return a mutagen containing only the rules that have a high count.
    //eventually this should be changed from a count to a percentage. 
    public Mutagen rulesToMutagen(HashMap<Pair<String,String>, Integer> rules){
        Mutagen guess = new Mutagen();
        Set<Pair<String, String>> patternActions = rules.keySet();
        for(Pair<String, String> patternAction: patternActions){
            //arbitrary value of a count such that we're happy with the rule
            if (rules.get(patternAction) > HIGHCOUNT){
                guess.add(patternAction.getKey(), patternAction.getValue());
            }
        }
        return guess;
    }


    //get all positions where a string was mutated
    public ArrayList<Integer> getMutationPositions(String original, String mutated){
        //just get the places at which the original string and the mutated string differ.
        ArrayList<Integer> mutationIndices = new ArrayList<Integer>();
        for (int j = 0; j < original.length(); j++){
            if (original.charAt(j) != mutated.charAt(j)){
                mutationIndices.add(j);
            }
        }
        return mutationIndices;
    }


    //Given original string, mutated string, and a position of a mutation, modify the hashmap so it contains new rules. 
    public HashMap<Pair<String, String>, Integer> addMutationChange(HashMap<Pair<String,String>, Integer> rules, String original, String mutated, ArrayList<Integer> mutationPositions, int start){
        //index of the mutation we are looking at in the string
        int index = mutationPositions.get(start);
        //right now, only worrying about 2 mutations that are right next to each other
        boolean moreMutations = true;
        int range = 0;
        while (moreMutations){
            if (range > 10){
                moreMutations = false;
            } else {
                if (original.charAt((index + range)%1000) != mutated.charAt((index + range)%1000)){
                    range++;
                } else {
                    moreMutations = false;
                }
            }
        }
        Pair<String, String> newRule = getRuleFromString(original, mutated, index, range);
        if (!rules.containsKey(newRule)){
            rules.put(newRule, 1);
        } else {
            int count = rules.get(newRule) + 1;
            rules.replace(newRule, count);
        }
        return rules;
    }


    //given an original string and a mutated string, create a rule from the area between index and index + range
    Pair<String, String> getRuleFromString(String original, String mutated, int index, int range){
        String pattern = "";
        String action = "";
        for (int i = 0; i < range; i++){
            pattern += original.charAt((index + i)%1000) + ";";
            action += mutated.charAt((index + i)%1000);
        }
        Pair<String,String> rule = new Pair<String,String>(pattern, action);
        return rule;
    }

    //determine whether a mutagen contains a specific rule
    public boolean mutagenContains(Mutagen mutagen, String pattern, String action){
        List<String> patterns = mutagen.getPatterns();
        List<String> actions = mutagen.getActions();
        for (int i = 0; i < patterns.size(); i++){
            if (patterns.get(i).equals(pattern)){
                if (actions.get(i).equals(action)){
                    return true;
                }
            }
        }
        return false;
    }
}
