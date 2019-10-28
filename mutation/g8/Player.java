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
        for (int i = 0; i < 1; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            ArrayList<Integer> mutationPositions = getMutationPositions(genome, mutated);
            if (mutationPositions.size() > 0){
                Mutagen change = getMutationChange(genome, mutated, mutationPositions.get(0));
                result = change;
            }
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

    public Mutagen getMutationChange(String original, String mutated, int index){
        Mutagen change = new Mutagen();
        change.add("" + original.charAt(index), ""+mutated.charAt(index));
        return change;
    }
}
