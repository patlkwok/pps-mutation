package mutation.g8;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

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

    @Override
    public Mutagen Play(Console console, int m) {
        Mutagen result = new Mutagen();
        for (int i = 0; i < 10; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            ArrayList<Integer> mutationPositions = getMutationPositions(genome, mutated);
            Mutagen changes = new Mutagen();
            if (mutationPositions.size() > 0){
                for (int j = 0; j < mutationPositions.size(); j++){
                    changes = addMutationChange(changes, genome, mutated, mutationPositions.get(j));
                }
            }
            result = changes;
            console.Guess(result);
        }
        return result;
    }

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

    public Mutagen addMutationChange(Mutagen changes, String original, String mutated, int index){
        System.out.println("in here");
        if (!mutagenContains(changes, ""+original.charAt(index), ""+mutated.charAt(index))){
            changes.add("" + original.charAt(index), ""+mutated.charAt(index));
        }
                System.out.println("patterns: " + changes.getPatterns());
        return changes;
    }

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
