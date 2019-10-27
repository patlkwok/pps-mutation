package mutation.g7;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {
    private Random random;
    private Map<String, Double> lhs;
    private Map<String, Double> rhs;
    private Set<String> combs;
    private int count = 0;
    private String[] genes = "acgt".split("");

    public Player() {
        random = new Random();
        lhs = new HashMap<>();
        rhs = new HashMap<>();
        combs = new HashSet<>();
        generateCombinations("", 4);
        generateDistributionMap("", 2);
        System.out.println(count);
    }

    private void generateDistributionMap(String result, int n){
        if(n == 0){
            count++;
            return;
        }
        String tmp = result;
        for(String c : combs){
            if(!result.equals("")) tmp = result + ";";
            generateDistributionMap(tmp + c, n - 1);
        }
        if(!result.equals("")) {
            count++;
        }
    }

    private void generateCombinations(String result, int n){
        if(n == 0){
            combs.add(result);
            return;
        }
        for(int i = 0; i < 4; i++){
            generateCombinations(result + genes[i], n - 1);
        }
        if(!result.equals("")) combs.add(result);
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
