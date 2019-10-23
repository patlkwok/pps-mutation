package mutation.g7;

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
        // result.add("a;c;c", "att");
        // result.add("g;c;c", "gtt");
        result.add("c", "t");
        result.add("cg;at;gta","cat");
        result.add("a;atcg;atcg;cg","2a31");
        for (int i = 0; i < 10; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            console.Guess(result);
        }
        return result;
    }
}
