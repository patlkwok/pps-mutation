package mutation.g0;

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
    public Mutagen Play(Console console) {
        Mutagen result = new Mutagen();
        result.add("a", "c");
        for (int i = 0; i < 100; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            console.Guess(result);
        }
        return result;
    }
}
