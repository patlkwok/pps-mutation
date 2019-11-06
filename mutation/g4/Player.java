// deliverable 11-5: merged players, std dev clustering
package mutation.g4;

import java.lang.*;
import java.util.*;
import javafx.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import mutation.sim.Console;
import mutation.sim.Mutagen;
import javafx.util.Pair;

public class Player extends mutation.sim.Player {

    final int MAX_WIDTH = 10;
    final int MAX_CHECK = MAX_WIDTH *2 -1;
    final int MIN_SUPPORT = 100;    
    final String POOL = "acgt";

	private double epsilon = 0.03;
    private Random random;
    private List<Mutation> mutations;
    // private Map<Integer, Map<Pattern, Set<String>>> windowPatternBeforeMap;
    private Map<Pattern, Set<String>> patternBeforeMap;
    private List<Pattern> potentialPatterns;
    private Map<Rule, Set<Mutation>> ruleMutationMap;
    private final int maxRuleLength = 10;
    private final int maxPotentialPatterns = 3;
    private int suspectedOverlappingMutations = 0;
    private final String GENERIC_PATTERN = "acgt;acgt;acgt;acgt;acgt;acgt;acgt;acgt;acgt;acgt";

    public Player() {
        // used in all players
        random = new Random();
        
        // ***** used in ethans's *****
        this.mutations = new ArrayList<>();
        // this.windowPatternBeforeMap = new HashMap<>();
        this.patternBeforeMap = new HashMap<>();
        this.potentialPatterns = new ArrayList<>();
        this.ruleMutationMap = new HashMap<>();
        // ****************************
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
        
        // OLD player
        /*
    	for (int i = 0; i < maxPotentialPatterns; i++) {
    		potentialPatterns.add(new Pattern(GENERIC_PATTERN));
    	}
        Mutagen result = new Mutagen();
        for (int i = 0; i < 1000; i++) {
        	String genome = randomString();
            String mutated = console.Mutate(genome);
    	    Set<Set<Mutation>> mutationsSet = identifyMutations(genome, mutated);
    	    for (Set<Mutation> mutations : mutationsSet) {
    	    	Pattern pattern = assignAMutationToAPattern(mutations);
    	    	if (pattern != null) {
    	    		pattern.recalibrate();
    	    	}
    	    }
        }
        // List<Pattern> topPatterns = getTopPatterns(1);
        // System.out.println("Ending: ");
        for (Pattern topPattern : potentialPatterns) {
        	System.out.println(topPattern + " Evidence: " + topPattern.getEvidenceSize());
        }
        return result;
        */ 
        
        //******* becky's player ******* 
        Mutagen hypothesizedMutagen = new Mutagen();

        ArrayList<String[]> mutations = new ArrayList<>();

        // keep track of nucleotide counts per position
        int[][] counts = getNewCounts(MAX_CHECK);

        for (int k = 0; k < 1000; k++) {
            String genome = getRandomGenome();
            String mutated = console.Mutate(genome);

            ArrayList<Integer> midpoints = findMutationMidpoints(genome, mutated, m);
            for (int midpoint : midpoints) {
                int index_start = getBestStart(midpoint, genome, mutated, counts);
                int index_end = index_start +MAX_CHECK;

                // extract simple pattern and action
                String pattern =  genome.substring(index_start, index_end);
                String action  = mutated.substring(index_start, index_end);

                // modify pattern accordingly (keep track of individual nucleotide counts)
                String[] rule = { pattern, action };
                mutations.add(rule);

                // update counts
                for (int i = 0; i < pattern.length(); i++) {
                    counts[i][POOL.indexOf(pattern.charAt(i))] += 1;
                }
            }

            // only pass this point every few iterations
            if (k % 50 > 0) continue;

            hypothesizedMutagen = new Mutagen();

            // init full patterns array
            ArrayList<String> fullPatterns = new ArrayList<String>();
            for (String[] rule : mutations) fullPatterns.add(rule[0]);

            // extract top rules
            final int num_rules = 3;
            for (int n = 0; n < num_rules; n++) {
                // init pattern to be all unknowns "xxxx..."
                char[] pattern = new char[MAX_CHECK];
                for (int i = 0; i < pattern.length; i++) pattern[i] = 'x';

                // extract pattern and matched-indices
                Object[] match = extractMatchIndices(pattern, fullPatterns);
                String pat        = (String)    match[0];
                int action_offset = (int)       match[1];
                Integer[] indices = (Integer[]) match[2];

                if (indices.length < MIN_SUPPORT) break;

                // remove matched-patterns (so not to use again for next rule); consolidate corresponding actions
                ArrayList<String> actions = new ArrayList<>();
                for (int i : indices) {
                    fullPatterns.set(i, null);
                    String action = mutations.get(i)[1];
                    int action_max = action_offset +MAX_WIDTH;
                    if (action_max > action.length()) action_max = action.length();
                    actions.add(action.substring(action_offset, action_max));
                }

                // get best action
                String action = extractAction(actions, pat);

                // if reversible
                String reverseAction = new StringBuffer(action).reverse().toString();
                if (pat.compareTo(reverseAction) == 0) {
                    action = ""; for (int i = pat.length() -1; i >= 0; i--) action += Integer.toString(i);
                }

                // append rule to mutagen
                if (pat.indexOf('x') != -1) continue;
                pat = String.join(";", pat.split(""));
                hypothesizedMutagen.add(pat, action);
            }
            System.out.println("\n--------------------------------------\n");

            if (console.Guess(hypothesizedMutagen)) break;
        }

        return hypothesizedMutagen;
        //**********************************   
    }
    
    // ******* becky's functions *******
    private String getRandomGenome() {
        final int len = POOL.length();
        String result = "";
        for (int i = 0; i < 1000; ++ i) {
            int nextInt = random.nextInt() % len;
            if (nextInt < 0) nextInt += len;
            result += POOL.charAt(nextInt);
        }
        return result;
    }
    
    private int[][] getNewCounts( int width ) { return getNewCounts(width, 4); }
    private int[][] getNewCounts( int width, int height ) {
        int[][] counts = new int[width][height];
        for (int i = 0; i < counts.length; i++) {
            for (int j = 0; j < height; j++) counts[i][j] = 0;
        }
        return counts;
    }
    
    private double[] getStds( int[][] counts ) {
        final int len = counts[0].length;

        // find sums for each position
        int[] sums = new int[counts.length];
        for (int i = 0; i < counts.length; i++) {
            int sum = 0;
            for (int j = 0; j < len; j++) sum += counts[i][j];
            sums[i] = sum;
        }
        System.out.println();
        System.out.print("+: ");
        for (int s : sums) System.out.print(String.format("% 3d ", s));
        System.out.println();

        // find std-dev for each position
        double[] stds = new double[counts.length];
        for (int i = 0; i < counts.length; i++) {
            int sum = 0;
            for (int j = 0; j < len; j++) {
                int diff = counts[i][j] *len - sums[i];
                sum += diff*diff;
            }
            stds[i] = Math.sqrt(((double) sum) /(len*len)) /sums[i];
        }
        System.out.println();
        System.out.print("s: ");
        for (int i = 0; i < stds.length; i++) System.out.print(String.format("%.2f ", stds[i]));
        System.out.println();

        String ref = ""; for (int i = 0; i < len -4; i++) ref += Integer.toString(i); ref += POOL;

        System.out.println();
        for (int j = 0; j < len; j++) {
            System.out.print(ref.charAt(j) + ": ");
            for (int i = 0; i < counts.length; i++) {
                int x = counts[i][j];
                System.out.print(String.format("% 4d ", x));
            }
            System.out.println();
        }
        System.out.println();

        return stds;
    }
     
    private String extractAction( ArrayList<String> actions, String pattern ) {
        if (actions.size() == 0) return "";

        int length = actions.get(0).length();
        int[][] counts = getNewCounts(length);
        for (int i = 0; i < counts.length; i++) {
            for (String action : actions) {
                counts[i][POOL.indexOf(action.charAt(i))] += 1;
            }
        }
        System.out.println("ACTIONS:");
        double[] stds = getStds(counts);

        String action = "";
        for (int i = 0; i < stds.length; i++) {
            if (!isGood(stds[i])) break;
            int max_val = 0, max_j = 0;
            for (int j = 0; j < counts[i].length; j++) {
                if (max_val < counts[i][j]) {
                    max_val = counts[i][j];
                    max_j = j;
                }
            }
            action += POOL.charAt(max_j);
        }
        System.out.println(action);

        return action;
    }

    // this function takes cares of numeric actions as well (doesn't work yet...)
    private String extractAction1( ArrayList<String> actions, String pattern ) {
        // append nucleotides
        String reference = POOL;
        for (int i = 0; i < 10; i++) reference += Integer.toString(i);
        
        int length = actions.get(0).length();
        int[][] counts = getNewCounts(length, reference.length());
        for (String action : actions) {
            for (int i = 0; i < counts.length; i++) {
                counts[i][POOL.indexOf(action.charAt(i))] += 1;
                for (int j = 0; j < 10; j++) {
                    if (false) counts[i +4][j] += 1;
                }
            }
        }
        System.out.println("ACTIONS:");
        double[] stds = getStds(counts);

        String action = "";
        for (int i = 0; i < stds.length; i++) {
            if (!isGood(stds[i])) break;
            int max_val = 0, max_j = 0;
            for (int j = 0; j < counts[i].length; j++) {
                if (max_val < counts[i][j]) {
                    max_val = counts[i][j];
                    max_j = j;
                }
            }
            action += reference.charAt(max_j);
        }
        System.out.println(action);

        return action;
    }

    private boolean isGood( double s ) {
        final double THRESHOLD = 0.3;
        return Math.abs(s) >= THRESHOLD;
    }

    private Object[] extractMatchIndices( char[] pattern, ArrayList<String> fullPatterns ) {
        ArrayList<Integer> usedIndices = new ArrayList<>();

        HashMap<Integer,String> patterns = new HashMap<>();
        for (int i = 0; i < fullPatterns.size(); i++) {
            String pat = fullPatterns.get(i);
            if (pat != null) patterns.put(i, pat);
        }

        int action_offset = 0;

        // loop for up to 5 "recursions"
        for (int k = 0; k < 5; k++) {
            if (patterns.size() < MIN_SUPPORT) break;

            int[][] counts = getNewCounts(pattern.length);
            for (int i = 0; i < counts.length; i++) {
                for (String rule : patterns.values()) {
                    counts[i][POOL.indexOf(rule.charAt(i))] += 1;
                }
            }
            double[] stds = getStds(counts);

            String pat = new String(pattern);
            if (pat.indexOf('x') == -1) {
                System.out.println("Pattern: " + pat);            
                break;
            }

            // find start and end indices based on std
            int index_start = 0;
            while (index_start < stds.length && !isGood(stds[index_start])) index_start++;
            int index_end = index_start;
            while (index_end   < stds.length &&  isGood(stds[index_end  ])) index_end++;
            // update action-offset
            action_offset += index_start;

            // find max val
            int max_val = 1, max_ind = -1, max_j = 0;
            for (int i = index_start; i < index_end; i++) {
                if (pattern[i] != 'x') continue;
                for (int j = 0; j < 4; j++) {
                    if (max_val < counts[i][j]) {
                        max_val = counts[i][j];
                        max_ind = i;
                        max_j = j;
                    }
                }
            }
            char max_nuc = POOL.charAt(max_j);
            // return if nothing else found
            if (max_ind == -1) break;
            pattern[max_ind] = max_nuc;
            System.out.print(new String(pattern));

            // separate matching rules
            for (int i : new HashSet<Integer>(patterns.keySet())) {
                String rule = patterns.get(i);
                String subrule = rule.substring(index_start, index_end);
                if (rule.charAt(max_ind) == max_nuc) patterns.replace(i, subrule);
                else patterns.remove(i);
            }

            pattern = Arrays.copyOfRange(pattern, index_start, index_end);
            System.out.println("\t\t" + new String(pattern));
        }

        // return pattern and used indices
        String pat = new String(pattern);
        Integer[] inds = patterns.keySet().toArray(new Integer[0]);
        Object[] tuple = { pat, action_offset, inds };
        return tuple;
    }

    // get best start by comparing neighboring start indices with the highest so-far correlation
    private int getBestStart( int midpoint, String genome, String mutated, int[][] counts ) {
        // parse start and end indices (ensure they are within bounds)
        int index_start = midpoint -MAX_WIDTH;
        if (index_start < 0) index_start = 0;
        int max_index = genome.length() -MAX_WIDTH *2;
        if (index_start > max_index) index_start = max_index;

        int correlation0 = 0, correlation1 = 0;
        for (int i = 0; i < counts.length; i++) {
            char nuc0 = genome.charAt(i +index_start), nuc1 = genome.charAt(i +index_start +1);
            correlation0 += counts[i][POOL.indexOf(nuc0)];
            correlation1 += counts[i][POOL.indexOf(nuc1)];
        }

        // if corr1 ends up with a higher value, use it!
        if (correlation1 > correlation0) index_start += 1;

        return index_start;
    }

    private ArrayList<Integer> findMutationMidpoints( String before, String after, int m ) {
        int length = before.length();
        if (length != after.length()) throw new RuntimeException("There can be neither deletions nor insertions");

        // init list to store pairs (begin_index, end_index)
        ArrayList<Integer> midpoints = new ArrayList<>();

        int i = 0;
        while (i < length) {
            // move along the strings until a mutation has occured
            if (before.charAt(i) == after.charAt(i)) { i++; continue; }
            int index_start = i;

            // find last connected diff
            int index_end = -1;
            for (int j = index_start; j < index_start +MAX_WIDTH && j < length; j++, i++) {
                if (before.charAt(j) != after.charAt(j)) index_end = j;
            }

            // append connected-mutation to list
            if (index_end < 0) throw new RuntimeException("Index < 0!");
            int index_midpoint = (index_start + index_end)/2;
            midpoints.add(index_midpoint);
        }

        // return int array
        return midpoints;
    }
    //********************************** 

    private List<Pattern> getTopPatterns(int n) {
    	Collections.sort(potentialPatterns, new Comparator<Pattern>() {
    		@Override
    		public int compare(Pattern p1, Pattern p2) {
    			return p1.getEvidenceSize() - p2.getEvidenceSize();
    		}
    	});
    	Pattern genericPattern = new Pattern(GENERIC_PATTERN);
    	return potentialPatterns.stream().filter(p -> p.equals(genericPattern)).limit(n).collect(Collectors.toList());
    }

    private void printMutations(List<Mutation> mutations) {
    	System.out.println("Printing Mutations: " + mutations.size());
    	// for (Mutation mutation : mutations) {
    	// 	System.out.println(mutation.toString());
    	// }
    }

    public void initializePotentialPatterns() {
    	for (int i = 0; i < maxPotentialPatterns; i++) {
        	potentialPatterns.add(new Pattern(GENERIC_PATTERN));
        }
    }

    public Pattern assignAMutationToAPattern(Set<Mutation> mutations) {
    	System.out.println("Mutations size: " + mutations.size()); 
    	Pattern bestPattern = null;
    	Mutation bestMutation = null;
    	double bestScore = 0.0;
    	for (Mutation mutation : mutations) {
    		String before = mutation.getBefore();
        	for (int i = 0; i < potentialPatterns.size(); i++) {
        		Pattern potentialPattern = potentialPatterns.get(i);
        		double score = potentialPattern.similarityScore(mutation.getBefore());
            	if (score > bestScore) {
            		bestPattern = potentialPattern;
            		bestMutation = mutation;
            		bestScore = score;
            	}
          	}
        }
        System.out.println("Best Pattern: " + bestPattern);
        System.out.println("Best Mutation: " + bestMutation);
        System.out.println("Best Score: " + bestScore);
        if (bestPattern != null) {
        	bestPattern.addEvidence(bestMutation);
        	System.out.println("Adding Pattern: " + bestPattern.toString());
        	System.out.println("Mutation: Before: " + bestMutation.getBefore() + " After: " + bestMutation.getAfter());
        } else {
        	System.out.println("No Pattern with similarity above 0.0");
        }
        return bestPattern;
    }

    private void printPotentialPatterns() {
    	for (Pattern potentialPattern : potentialPatterns) {
    		System.out.println(potentialPattern.toString());
    	}
    }

    private void reducePotentialPatterns() {
    	// for each pattern, check if any other pattern is the same. If so, merge them and their mutations.
    }

    private Set<Set<Mutation>> identifyMutations(String before, String after) {
    	Set<Set<Mutation>> mutationsSet = new HashSet<>();
    	if (before.length() != after.length()) {
    		throw new RuntimeException("There can be neither deletions nor insertions");
    	}
    	int length = before.length();
    	// we do not want the before to differ from after in the first 10 or the last 10, but we do not want index out of bounds when computing windows.
    	// this is hacky but it accomplishes the desired effect.
    	before = after.substring(maxRuleLength) + before + after.substring(length-maxRuleLength, length);
    	after = after.substring(maxRuleLength) + after + after.substring(length-maxRuleLength, length);
    	int i = 0;
    	while (i < before.length()) {
    		if (!(before.charAt(i) == after.charAt(i))) {
    			int start = i;
    			int end = i;
    			int next;
    			while ((next = getNextDifferenceWithinMaxRuleLength(before, after, end)) != end) {
    				end = next;
    			}
    			if (end < start + maxRuleLength) {
    				mutationsSet.add(generateSlidingWindows(before, after, start, end, maxRuleLength));
    			} else {
    				suspectedOverlappingMutations++;
    			}
    			i = end+1;
    		}
    		i++;
    	}
    	return mutationsSet;
    }

    private int getNextDifferenceWithinMaxRuleLength(String before, String after, int index) {
    	for (int i = 1; i < maxRuleLength; i++) {
    		if (before.charAt(index + i) != after.charAt(index + i)) return index + i;
    	}
    	return index;
    }

    private Set<Mutation> generateSlidingWindows(String before, String after, int start, int end, int maxRuleLength) {
    	Set<Mutation> mutations = new HashSet<>();
    	for (int i = end-maxRuleLength+1; i <= start; i++) {
    		Mutation mutation = new Mutation(before.substring(i, i+maxRuleLength), after.substring(i, i+maxRuleLength));
    		mutations.add(mutation);
    	}
    	return mutations;
    }
}
