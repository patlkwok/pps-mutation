// deliverable 10-28
package mutation.g4Ethan;

import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;
import mutation.sim.Console;
import mutation.sim.Mutagen;
import javafx.util.Pair;

public class Player extends mutation.sim.Player {
    private Console console;
    private Random random;
    private List<Mutation> mutations = new ArrayList<>();
    private ArrayList<String[]> mutationsRebecca = new ArrayList<>();
    private static final int MAX_RULE_LENGTH = 10;
    private static final int MIN_EVIDENCE = 10;
    private int suspectedOverlappingMutations = 0;
    private List<MutationGroup> mutationGroups = new ArrayList<>();
    private final int MAX_WIDTH = 10;
    private final int MAX_CHECK = MAX_WIDTH *2 -1;
    private final int MIN_SUPPORT = 100;
    private final String POOL = "acgt";
    private List<String> beforeWindows = new ArrayList<>();
    private List<String> afterWindows = new ArrayList<>();

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

//  START ETHAN'S CODE
    @Override
    public Mutagen Play(Console console, int m) {
        this.console = console;
        Mutagen result = new Mutagen();
        // write an outer for loop on this for random restarts
        for (int j = 0; j < 100; j++) {
            mutationGroups = new ArrayList<>();
            for (int i = 0; i < 101; i++) {
            	String genome = randomString();
                String mutated = console.Mutate(genome);
    	        Set<List<Mutation>> mutationsSet = identifyMutations(genome, mutated);
                // System.out.println("PlayEthan");
                if (j==0 && i < 2) {
                    // System.out.println("PlayTanmay");
                    PlayTanmay(mutationsSet, genome, mutated);
                }
                if (i % 50 == 0) {
                    // System.out.println("PlayRebecca");
                    PlayRebecca(m);
                }
                PlayEthan(mutationsSet, i);
            }
            if (console.isCorrect()) System.exit(0);
        }
        // This is written brute force, assuming mutagens have at most 3 rules.
        // for (MutationGroup mg : mutationGroups) {
        //     System.out.println("Mutation Group Size: " + mg.getNumMutations());
        //     System.out.println("Pattern: " + mg.getRule().getKey());
        //     System.out.println("Action: " + mg.getRule().getValue());
        // }
        return result;
    }

    public void PlayEthan(Set<List<Mutation>> mutationsSet, int iter) {
        System.out.println("" + iter);
        for (List<Mutation> mutations : mutationsSet) {
            assignAMutationToAMutationGroup(mutations);
        }
        guessTopMutations(Math.min(mutationGroups.size(), 1));
        if (iter%10==0) {
            guessTopMutations(Math.min(mutationGroups.size(), 3));
        }
        if (iter%100==0) {
            guessTopMutations(Math.min(mutationGroups.size(), 5));
            mutationGroups = new ArrayList<>();
        }
    }

    public void mergeMutations() {
        System.out.println("Size Before: " + mutationGroups.size());
        List<MutationGroup> newMutationGroups = new ArrayList<>();
        for (int i = 0; i < mutationGroups.size(); i++) {
            boolean isContained = false;
            String[] oldPattern = mutationGroups.get(i).getPattern().split(";");
            for (int j = 0; j < newMutationGroups.size(); j++) {
                String[] newPattern = newMutationGroups.get(j).getPattern().split(";");
                if (contains(newPattern, oldPattern)) {
                    isContained = true;
                    newMutationGroups.get(j).addAll(mutationGroups.get(i).getMutations());
                    break;
                } 
            }
            if (!isContained) newMutationGroups.add(mutationGroups.get(i));
        }
        mutationGroups = newMutationGroups;
        System.out.println("Size After: " + mutationGroups.size());
    }

    private boolean contains(String[] p1, String[] p2) {
        if (p1.length != p2.length) return false;
        for (int i = 0; i < p1.length; i++) {
            Set<Character> p1Chars = new HashSet<>();
            Set<Character> p2Chars = new HashSet<>();
            for (char c1 : p1Chars) {
                p1Chars.add(c1);
            }
            for (char c2 : p2Chars) {
                p2Chars.add(c2);
            }
            p2Chars.removeAll(p1Chars);
            if (p2Chars.size() > 0) return false;
        }
        return true;
    }

    public void guessTopMutations(int n) {
        List<MutationGroup> topMutationGroups = new ArrayList<>(mutationGroups);
        // topMutationGroups = topMutationGroups.stream().filter(mg -> mg.getNumMutations() > MIN_EVIDENCE).collect(Collectors.toList());
        Collections.sort(topMutationGroups, new Comparator<MutationGroup>() {
            @Override
            public int compare(MutationGroup mg1, MutationGroup mg2) {
                return mg2.getNumMutations() - mg1.getNumMutations();
            }
        });
        topMutationGroups = topMutationGroups.subList(0, n);
        List<Pair<String, String>> topMutationGroupsPairString = convertMutationGroupToPair(topMutationGroups);
        guessOneRule(topMutationGroupsPairString);
        guessTwoRules(topMutationGroupsPairString);
        guessThreeRules(topMutationGroupsPairString);
    }

    private List<Pair<String, String>> convertMutationGroupToPair(List<MutationGroup> topMutationGroups) {
        List<Pair<String, String>> patternActions = new ArrayList();
        for (int i = 0; i < topMutationGroups.size(); i++) {
            Pair<String, String> patternAction = topMutationGroups.get(i).getRule();
            patternActions.add(patternAction);
        }
        return patternActions;
    }

    public void guessOneRule(List<Pair<String, String>> patternActions) {
        Mutagen result;
        for (int i = 0; i < patternActions.size(); i++) {
            result = new Mutagen();
            Pair<String, String> patternAction = patternActions.get(i);
            result.add(patternAction.getKey(), patternAction.getValue());
            console.testEquiv(result);
            if (console.isCorrect()) System.exit(0);
        }
    }

    public void guessTwoRules(List<Pair<String, String>> patternActions) {
        // System.out.println("Guessing 2 rules");
        Mutagen result;
        for (int i = 0; i < patternActions.size(); i++) {
            for (int j = i+1; j < patternActions.size(); j++) {
                result = new Mutagen();
                Pair<String, String> patternActionI = patternActions.get(i);
                Pair<String, String> patternActionJ = patternActions.get(j);
                result.add(patternActionI.getKey(), patternActionI.getValue());
                result.add(patternActionJ.getKey(), patternActionJ.getValue());
                console.testEquiv(result);
                if (console.isCorrect()) System.exit(0);            
            }
        }
    }

    public void guessThreeRules(List<Pair<String, String>> patternActions) {
        // System.out.println("Guessing 3 rules");
        Mutagen result;
        for (int i = 0; i < patternActions.size(); i++) {
            for (int j = i+1; j < patternActions.size(); j++) {
                for (int k = j+1; k < patternActions.size(); k++) {
                    result = new Mutagen();
                    Pair<String, String> patternActionI = patternActions.get(i);
                    Pair<String, String> patternActionJ = patternActions.get(j);
                    Pair<String, String> patternActionK = patternActions.get(k);
                    result.add(patternActionI.getKey(), patternActionI.getValue());
                    result.add(patternActionJ.getKey(), patternActionJ.getValue());
                    result.add(patternActionK.getKey(), patternActionK.getValue());
                    console.testEquiv(result);
                    if (console.isCorrect()) System.exit(0);            
                }
            }
        }
    }

    public void assignAMutationToAMutationGroup(List<Mutation> mutations) {
        // if not similar enough, create a new mutation group.
        MutationGroup bestMutationGroup = null;
        Mutation bestMutation = null;
        double bestSimilarity = 10.0;
        // double[][] similarityMatrix = new double[mutations.size()][mutationGroups.size()]
        for (int i = 0; i < mutations.size(); i++) {
            Mutation mutation = mutations.get(i);
            for (int j = 0; j < mutationGroups.size(); j++) {
                MutationGroup mutationGroup = mutationGroups.get(j);
                double similarity = mutationGroup.similarity(mutation);
                if (similarity > bestSimilarity) {
                    bestMutationGroup = mutationGroup;
                    bestMutation = mutation;
                    bestSimilarity = similarity;
                }
            }
        }
        if (bestMutationGroup == null) {
            bestMutationGroup = new MutationGroup();
            bestMutation = mutations.get(random.nextInt(mutations.size()));
            mutationGroups.add(bestMutationGroup);
        }
        bestMutationGroup.add(bestMutation);
    }

    private Set<List<Mutation>> identifyMutations(String before, String after) {
    	Set<List<Mutation>> mutationsSet = new HashSet<>();
    	if (before.length() != after.length()) {
    		throw new RuntimeException("There can be neither deletions nor insertions");
    	}
    	int length = before.length();
    	// we do not want the before to differ from after in the first 10 or the last 10, but we do not want index out of bounds when computing windows.
    	// this is hacky but it accomplishes the desired effect.
    	before = after.substring(MAX_RULE_LENGTH) + before + after.substring(length-MAX_RULE_LENGTH, length);
    	after = after.substring(MAX_RULE_LENGTH) + after + after.substring(length-MAX_RULE_LENGTH, length);
    	
        int i = 0;
    	while (i < before.length()) {
    		if (!(before.charAt(i) == after.charAt(i))) {
    			int start = i;
    			int end = i;
    			int next;
    			while ((next = getNextDifferenceWithinMaxRuleLength(before, after, end)) != end) {
    				end = next;
    			}
    			if (end < start + MAX_RULE_LENGTH) {
                    beforeWindows.add(before.substring(start, end));
                    afterWindows.add(after.substring(start, end));
    				mutationsSet.add(generateSlidingWindows(before, after, start, end));
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
    	for (int i = 1; i < MAX_RULE_LENGTH; i++) {
    		if (before.charAt(index + i) != after.charAt(index + i)) return index + i;
    	}
    	return index;
    }

    private List<Mutation> generateSlidingWindows(String before, String after, int start, int end) {
    	List<Mutation> mutations = new ArrayList<>();
    	for (int i = end-MAX_RULE_LENGTH+1; i <= start; i++) {
    		Mutation mutation = new Mutation(before.substring(i, i+MAX_RULE_LENGTH), after.substring(i, i+MAX_RULE_LENGTH));
    		mutations.add(mutation);
    	}
    	return mutations;
    }
    // END ETHAN'S CODE

    // START REBECCA'S CODE
    public void PlayRebecca(int m) {
        Mutagen hypothesizedMutagen = new Mutagen();
        // keep track of nucleotide counts per position
        int[][] counts = getNewCounts(MAX_CHECK);
        // placeholder for for loop
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
            mutationsRebecca.add(rule);

            // update counts
            for (int i = 0; i < pattern.length(); i++) {
                counts[i][POOL.indexOf(pattern.charAt(i))] += 1;
            }
        }

        // only pass this point every few iterations
        hypothesizedMutagen = new Mutagen();

            // init full patterns array
        ArrayList<String> fullPatterns = new ArrayList<String>();
        for (String[] rule : mutationsRebecca) fullPatterns.add(rule[0]);

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
                String action = mutationsRebecca.get(i)[1];
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
        console.testEquiv(hypothesizedMutagen);
        // placeholder for for loop end
    }

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
    // END REBECCA'S CODE

    // TANMAY'S CODE

    public void PlayTanmay(Set<List<Mutation>> mutations, String before, String after) {
        guessBasicRule();
        guessMultiRules(before, after);
    }

    public String formatRule(String rule){
        String formatted_rule = "";
        if(rule.length()>1){
            for(int ch = 0; ch<rule.length()-1; ch++)
                formatted_rule += rule.charAt(ch) + ";";
                formatted_rule += rule.charAt(rule.length()-1);        
            return formatted_rule;
        }
        else {
        return rule;
        }          
    }

    public void guessMultiRules(String beforeString, String afterString) {
        Mutagen result = new Mutagen();
        //Maybe return an arraylist of mutagens with different combinations of the rules 
        ArrayList<String> rules = new ArrayList<>();
        ArrayList<String> actions = new ArrayList<>();
        boolean inMutation = false;
        String rule  = "";
        String act = "";
        for(int ch = 0; ch<beforeString.length(); ch++)
        {
            if(beforeString.charAt(ch)!= afterString.charAt(ch) && !inMutation)
            {
                inMutation = true;
                rule += beforeString.charAt(ch);
                act += afterString.charAt(ch);
            }
            else if(beforeString.charAt(ch)!= afterString.charAt(ch) && inMutation)
            {
                rule += beforeString.charAt(ch);
                act += afterString.charAt(ch);
            }
            if(beforeString.charAt(ch) == afterString.charAt(ch) && inMutation)
            {
                inMutation = false;
                String to_insert = formatRule(rule);
                int index = rules.indexOf(to_insert);
                if(index<0 || !actions.get(index).equals(act)) {
                    rules.add(to_insert);
                    actions.add(act);
                }
                rule = "";
                act = "";
            }
        }
        List<Pair<String, String>> patternActions = new ArrayList<>();
        if (rules.size() != actions.size()) return;
        for (int i = 0; i < rules.size(); i++) {
            patternActions.add(new Pair<String, String>(rules.get(i), actions.get(i)));
        }
        guessOneRule(patternActions);
        guessTwoRules(patternActions);
        guessThreeRules(patternActions);
        //need to generate all possible combinations from of rules and actions and add each to a mutagen then test
    }

    public void guessBasicRule() {
        //maybe add exclusion list if particular rule and action pair failes
        //Maybe try to get at disjunctive rule by matching common action(non-numeric) by flagging if commmon action found but no cmmon rule
        Mutagen result = new Mutagen();
        boolean commonRuleFound = false;
        boolean commonActionFound = false;
        String commonRule = "";
        String commonAction = "";

        String bef1 = beforeWindows.get(beforeWindows.size()/2);
        ArrayList<String> bef_subs = new ArrayList<>();
        for(int i=1; i<10; i++)
        {
            for(int j=0; j<bef1.length()-i+1; j++)
            {
                String sub_bef = bef1.substring(j,j+i);
                bef_subs.add(sub_bef);
            }
        }
        //go from largest substring to smallest - if in all befwindows - call it the rule
        for(int largest = bef_subs.size()-1; largest>=0; largest--)
        {
            String to_check = bef_subs.get(largest);
            boolean in_all = true;
            for(String window : beforeWindows)
            {
                if(window.indexOf(to_check)<0)
                    {
                        in_all = false;
                        break;
                    }
            
            }
            if(in_all)
            {
                commonRuleFound = true;
                commonRule = to_check;
                break;
            }    
        }
        //repeat process for action - this is for basic one rule --> one action
        String af1 = afterWindows.get(afterWindows.size()/2);
        ArrayList<String> af_subs = new ArrayList<>();
        for(int i=1; i<10; i++) {
            for(int j=0; j<af1.length()-i+1; j++) {
                String sub_af = af1.substring(j,j+i);
                af_subs.add(sub_af);
            }
        }
        //go from largest substring to smallest - if in all afwindows - call it the action
        for(int largest = af_subs.size()-1; largest>=0; largest--) {
            String to_check = af_subs.get(largest);
            boolean in_all = true;
            for(String window : afterWindows) {
                if(window.indexOf(to_check)<0) {
                    in_all = false;
                    break;
                }
            }
            if(in_all) {
                commonActionFound = true;
                commonAction = to_check;
                break;
            }    
        }
        if(commonRuleFound && commonActionFound) {
            //remember to format rule before adding to mutagen
            String formatted_rule = "";
            for(int ch = 0; ch<commonRule.length()-1; ch++) {
                formatted_rule += commonRule.charAt(ch) + ";";
            }
            formatted_rule += commonRule.charAt(commonRule.length()-1);
            result.add(formatted_rule,commonAction);
            // System.out.println("Guessing Pattern: " + formatted_rule + " Action: " + commonAction);
            console.testEquiv(result);
        }
        else
        {
            System.out.println("No Useful Guesses Found.");
            //don't waste your guess, attempt something else
        }
    }

    // END TANMAY'S CODE
}
