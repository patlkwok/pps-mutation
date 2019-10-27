package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

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

    private ArrayList<String> getPossiblePattern(String window_str){
        ArrayList<String> pattern = new ArrayList<>();
        for (int i = 0; i < window_str.length(); i++){
            for (int j = i; j < window_str.length(); j++){
                pattern.add(window_str.substring(i, j+1));
            }
        }
        return pattern;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        Mutagen result = new Mutagen();
        result.add("a;c;c", "att");
        result.add("g;c;c", "gtt");
        for (int i = 0; i < 10; ++ i) {
            String genome = defaultString();
            String mutated = console.Mutate(genome);
            console.Guess(result);
        }
        return result;
    }
}
