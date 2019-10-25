package mutation.g3;

import java.util.*;
import java.lang.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class PAPair {
	// A class for (pattern, action) pair
	public PAPair() {
        this.pattern = new String();
        this.action = new String();
    }

    public PAPair(String pattern, String action) {
        this.pattern = new String(pattern);
        this.action = new String(action);
    }

	public String getPattern() {
        return pattern;
    }

    public String getAction() {
        return action;
    }

	public double patternScore() {
		return 1.0;  // should be changed based on length of pattern
	}
}

public static HashMap<PAPair, Double> sortScores(HashMap<PAPair, Double> scores) {
	// Sort a hash map containing (pattern, action) pairs
	// Step 1: Create a list from elements of hash map
	List<Map.Entry<PAPair, Double>> list = new ArrayList<Map.Entry<PAPair, Double>>(scores.entrySet());
	// Step 2: Sort the list (in descending order of scores)
	Collections.sort(list, new Comparator<Map.Entry<PAPair, Double>>() {
		public int compare(Map.Entry<PAPair, Double> o1, Map.Entry<PAPair, Double> o2) {
			return (o2.getValue()).compareTo(o1.getValue());
		}
	});
    // Step 3: Put data from sorted list to hash map
	HashMap<PAPair, Double> newScores = new LinkedHashMap<PAPair, Double>(); 
	for (Map.Entry<PAPair, Double> s: list) { 
		newScores.put(s.getKey(), s.getValue()); 
	} 
	return newScores; 
} 

public class Player extends mutation.sim.Player {
    private Random random;
	private int windowSize = 2;

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
		HashMap<PAPair, Double> scores = new HashMap<PAPair, Double>();
        //result.add("a;c;c", "att");
        //result.add("g;c;c", "gtt");
        for (int i = 0; i < 5; ++ i) {
			Mutagen result = new Mutagen();
            String genome = randomString();
            String mutated = console.Mutate(genome);
			// Step 1: Add first 10 characters of genome and mutated to the end
			String genome_new = genome.concat(genome.substring(0, 10));
			String mutated_new = mutated.concat(mutated.substring(0, 10));
			// Step 2: Take substrings from genome and mutated, compare
			for (int j = 0; j < 1000; ++ j) {
				String gPart = genome_new.substring(j, j + windowSize);
				String mPart = mutated_new.subString(j, j + windowSize);
				boolean notMutated = gPart.equals(mPart);
				if (!notMutated) {
					// Step 2a: For each mutated substring, get possible mutagens from black box, add to hash map
					List<PAPair> possibles = getPossible(gPart, mPart, windowSize);  // the black box
					for (PAPair p: possibles) {
						if (scores.containsKey(p)) {
							scores.put(p, scores.get(p) + p.patternScore());
						}
						else {
							scores.put(p, p.patternScore());
						}
					}
				}
			}
			HashMap<PAPair, Double> sortedScores = sortScores(scores);
			int maxRules = 3;
			int rules = 0;
			for (Map.Entry<PAPair, Double> s: sortedScores.entrySet()) {
				if (rules < maxRules) {
					result.add(s.getKey().getPattern(), s.getKey().getAction());
					rules++;
				}
        	}
			// Step 3: Evaluate the most possible
            if (console.Guess(result)) {
				return result;
			}
        }
        return result;
    }
}
