package mutation.g6;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {
    private Random random;
    private int[] beforeCounter;
    private int[] afterCounter;
    private Map<Character, Integer> hash;
    private Map<Integer, Character> antiHash;
    
    public Player() {
        random = new Random();
        hash = new HashMap<>({{put('a', 0); put('c', 1); put('g', 2); put('t', 3);}});
        antiHash = new HashMap<>({{put(0, 'a'); put(1, 'c'); put(2, 'g'); put(3, 't');}});
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
        //result.add("a;c;c", "att");
        //result.add("g;c;c", "gtt");
        for (int i = 0; i < 10; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            char[] input = genome.toCharArray();
            char[] output = mutated.toCharArray();
            Element[] diff = checkDifference(input, output);
            result = getNaive(diff);
            console.Guess(result);
        }
        return result;
    }

    public Mutagen getNaive(Element[] diff) {
        Mutagen result = new Mutagen();
        int[][] set = new int[4][4];

        for(Element e : diff) {
            if(e.isMutated) {
                set[hash.get(e.getOG())][hash.get(e.getAfter())]++;
            }
        }

        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(set[i][j] > 0) {
                    result.add(Character.toString(antiHash.get(i), Character.toString(antiHash.get(j))));
                }
            }
        }
        return result;
    }

    public Element[] checkDifference(char[] input, char[] output) {
        Element[] diff = new Element[1000];
        beforeCounter = new int[4];
        afterCounter = new int[4];
        for(int i = 0; i < input.length; i++) {
            if(input[i] != output[i]) {
                diff[i] = new Element(true, input[i], output[i]);
                beforeCounter[hash.get(input[i])]++;
                afterCounter[hash.get(output[i])]++;
            }
            else diff[i] = new Element(input[i]);
        }
        return diff;
    }

    public class Element {
        private boolean mutated;
        private char og;
        private char after;

        public Element() {
            mutated = false;
        }

        public Element(char og) {
            mutated = false;
            this.og = og;
            this.after = og;
        }

        public Element(boolean mutated, char og, char after) {
            this.mutated = mutated;
            this.og = og;
            this.after = after;
        }

        public boolean isMutated() {
            return mutated;
        }

        public char getOG() {
            return og;
        }

        public char getAfter() {
            return after;
        }

        public void putOG(char og) {
            this.og = og;
        }

        public void putAfter(char after) {
            this.after = after;
        }
    }


}
