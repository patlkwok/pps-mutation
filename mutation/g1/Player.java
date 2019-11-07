package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
import java.util.*;

import javafx.util.Pair;


public class Player extends mutation.sim.Player {
    private Random random;
    public final ArrayList<Character> bases = new ArrayList<Character>(Arrays.asList('a', 'c', 'g', 't'));
    public HashMap<Pair<String, String>, Integer> guessCounts = new HashMap<>();
    /*
    HYPER-PARAMETERS
     */
    public int startOverFreq = 100;
    public double rareFactorThreshold = 0.02;
    public double guessNumericProbability = 0.75; // percentage of the time we allow numbers in guesses
    // if we've seen the most popular rule 10 times,
    // we only accept other multiple rules if we've seen them 10 * supportCoeff or greater
    public double supportCoeff = 0.75;
    public boolean ignoreAmiguousCasesWhenMerging = false;
    // measure of how confident we need to be in this numeric action for it to be included in guess
    public double numericConfidence = 0.75;

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

    public double rareFactor(String pattern) {
        double rf = 1.0;
        for(String s : pattern.split(";")) {
            if(s.length() < 4)
                rf *= 1.0 / (4 - s.length());
        }

        return rf;
    }

    public void mergeTrees(HashMap<String, MyTree> trees) {
        // Merge tree a into tree b iff a --> <x>;(acgt)@<y> and b --> <x>@<y>(acgt)

        // a@tc  if you see "ac" you're going to incorrectly guess a;c@t.  Assume we are looking a@tc and not a;c@t
        ArrayList<String>actionsToRemove = new ArrayList<>();

        for(Map.Entry<String, MyTree> entry : trees.entrySet()) {
            String action = entry.getKey();
            MyTree t = entry.getValue();
            for(Map.Entry<String, MyTree> otherEntry : trees.entrySet()) {
                String otherAction = otherEntry.getKey();
                MyTree otherT = otherEntry.getValue();

                if(otherAction.equals(action)) {
                    continue;
                }

                int alignIdx = otherAction.indexOf(action);
                // TODO figure out why I had this
                boolean ambiguousAlignIdx = this.ignoreAmiguousCasesWhenMerging && otherAction.length() > 1 && otherAction.substring(1).contains(action);
                if(alignIdx == 0 && ! ambiguousAlignIdx) {
                    boolean isValid = true;
                    for(int i = action.length(); i < otherAction.length(); i++) {
                        isValid = t.matchesCharAtIdx(otherAction.charAt(i), i);
                        if(!isValid) {
                            break;
                        }
                    }

                    if(isValid) {
                        // merge entry into otherEntry
                        otherT.mergeTree(t, alignIdx);
                        actionsToRemove.add(action);
                    }
                }
            }
        }

        for(String action: actionsToRemove) {
            trees.remove(action);
        }
    }

    public Integer getWrappedIdx(int idx, int n) {
        if(idx < 0) {
            return n + idx;
        } else if(idx >= n) {
            return idx - n;
        } else {
            return idx;
        }
    }

    private ArrayList<ArrayList<Integer>> getPossibleWindows(String beforeMutation, String afterMutation, int m) {
        int n = beforeMutation.length();
        ArrayList<ArrayList<Integer>>possibleWindows = new ArrayList<>();
        int startIdx = 9999999;
        int endIdx = -9999999;
        Set<Integer>accountedFor = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            if (beforeMutation.charAt(i) != afterMutation.charAt(i) && ! accountedFor.contains(i)) {
                accountedFor.add(i);
                startIdx = Math.min(startIdx, i);
                endIdx = Math.max(endIdx, i);
                for(int windowIdx = i - 9; windowIdx <= i + 9; windowIdx++) {
                    int wrappedIdx = getWrappedIdx(windowIdx, n);
                    if (beforeMutation.charAt(wrappedIdx) != afterMutation.charAt(wrappedIdx)) {
                        accountedFor.add(wrappedIdx);
                        startIdx = Math.min(startIdx, windowIdx);
                        endIdx = Math.max(endIdx, windowIdx);
                    }
                }
                ArrayList<Integer>newW = new ArrayList<>();
                for(int windowStart = endIdx - 9; windowStart <= startIdx; windowStart++) {
                    newW.add(this.getWrappedIdx(windowStart, n));
                }
                possibleWindows.add(newW);
                startIdx = 9999999;
                endIdx = -9999999;
            }
        }

        return possibleWindows;
    }

    public String getWrappedSubstring(int s, int e, String string) {
        if(s < 0) {
            int realStart = getWrappedIdx(s, string.length());
            return string.substring(realStart) + string.substring(0, e);
        } else if (e >= string.length()) {
            int realEnd = getWrappedIdx(e, string.length());
            return string.substring(s) + string.substring(0, realEnd);
        } else {
            return string.substring(s, e);
        }
    }

    public String getAction(String beforeMutation, String afterMutation) {
        int lastChangeIdx = -1;
        for(int i = 0; i < afterMutation.length(); i++) {
            if(beforeMutation.charAt(i) != afterMutation.charAt(i)) {
                lastChangeIdx = i;
            }
        }

        String action = afterMutation.substring(0, lastChangeIdx + 1);
        return action;
    }

    public boolean consumes(Pair<String, String>p1, Pair<String, String> p2) {
        /*
        p1 consumes p2 if of the form: p1= a;<x>@a<y> and p2=<x>@<y>
         */

        List<String> pp1 = Arrays.asList(p1.getKey().split(";"));
        String pa1 = p1.getValue();

        List<String> pp2 = Arrays.asList(p2.getKey().split(";"));
        String pa2 = p2.getValue();

        int alignIdx = pa1.indexOf(pa2);
        if(alignIdx > 0) {
            for(int i = 0; i < alignIdx; i++) {
                if(i >= pp1.size() || !Character.toString(pa1.charAt(i)).equals(pp1.get(i))) {
                    return false;
                }
            }

            for(int i = alignIdx; i < pp1.size(); i++) {
                String base = pp1.get(i);
                if(i - alignIdx < pp2.size()) {
                    String compare = pp2.get(i - alignIdx);
                    if(! base.contains(compare)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Pair<String, ArrayList<MyTree>> proposeAlphaNumericAction(ArrayList<MyTree> trees) {
        // group according to the length of the predicted action
        HashMap<Integer, ArrayList<MyTree>> numericActionToTrees = new HashMap<>();
        // some patterns could be the same because of spuriousness.
        // pick the one that is the most likely on basis of action length
        HashMap<Integer, Integer> combinedSupport = new HashMap<>();
        int biggestLengthSupport = -1;
        int targetActionLen = -1;
        for(MyTree t : trees) {
            int key = t.action.length();
            if(numericActionToTrees.containsKey(key)) {
                numericActionToTrees.get(key).add(t);
                combinedSupport.put(key, combinedSupport.get(key) + t.support);
            } else {
                ArrayList<MyTree>tmp = new ArrayList<>();
                tmp.add(t);
                numericActionToTrees.put(key, tmp);
                combinedSupport.put(key, t.support);
            }

            if(combinedSupport.get(key) >= biggestLengthSupport) {
                targetActionLen = key;
                biggestLengthSupport = combinedSupport.get(key);
            }
        }

        ArrayList<MyTree> candidateTrees = numericActionToTrees.get(targetActionLen);
        String newAction = this.attemptNumericMerge(candidateTrees);

        if(newAction.matches(".*\\d.*")) {
            double n = (double) candidateTrees.size();
            double score = 0.0;
            for(MyTree t : candidateTrees) {
                for(int a = 0; a < newAction.length(); a++) {
                    char c = newAction.charAt(a);
                    int charIdx = this.bases.indexOf(c);
                    if(charIdx == -1) {
                        score += t.actionIndexCounts.get(a).get(Character.getNumericValue(c)) / (double) t.support;
                    } else {
                        if(t.action.charAt(a) == c) {
                            score += 1.0;
                        }
                    }
                }
            }

            double confidence = score / (n * newAction.length());
            if(confidence >= this.numericConfidence) {
                return new Pair(newAction, numericActionToTrees.get(targetActionLen));
            } else {
                return new Pair(null, null);
            }
        } else {
            return new Pair(null, null);
        }
    }

    public String attemptNumericMerge(ArrayList<MyTree> trees) {
        int n = trees.get(0).action.length();
        ArrayList<ArrayList<Integer>> combinedActionIndexCounts = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            ArrayList<Integer>ac = new ArrayList<>();
            for(int j = 0; j < 10; j++) {
                ac.add(0);
            }
            combinedActionIndexCounts.add(ac);
        }

        ArrayList<ArrayList<Integer>>currentActionCharCounts = new ArrayList<>();
        for(int z = 0; z < n; z++) {
            ArrayList<Integer>tmp = new ArrayList<>();
            for(int p = 0; p < 4; p++) {
                tmp.add(0);
            }
            currentActionCharCounts.add(tmp);
        }

        for(MyTree t : trees) {
            for(int z = 0; z < n; z++) {
                char actionChar = t.action.charAt(z);
                int charIdx = this.bases.indexOf(actionChar);
                currentActionCharCounts.get(z).set(charIdx, currentActionCharCounts.get(z).get(charIdx) + t.support);
            }

            for(int r = 0; r <  t.actionIndexCounts.size(); r++) {
                for(int c = 0; c < t.actionIndexCounts.get(0).size(); c++) {
                    combinedActionIndexCounts.get(r).set(
                            c, combinedActionIndexCounts.get(r).get(c) + t.actionIndexCounts.get(r).get(c));
                }
            }
        }

        // for each action index --> have we seen a number or a character more often?
        // map action indices in pattern and action indices themselves (* support)
        // trees are keyed by actual action we see. increment support by 1
        // which is biggest?

        String newAction = "";
        for(int i=0; i < n; i++) {
            String maxChar = null;
            int maxCount = -1;
            for(int actionIdx = 0; actionIdx < 10; actionIdx++) {
                int count = combinedActionIndexCounts.get(i).get(actionIdx);
                if(count >= maxCount) {
                    maxChar = Integer.toString(actionIdx);
                    maxCount = count;
                }
            }

            for(int charIdx = 0; charIdx < 4; charIdx++) {
                int count = currentActionCharCounts.get(i).get(charIdx);
                if(count >= maxCount) {
                    maxChar = Character.toString(this.bases.get(charIdx));
                    maxCount = count;
                }
            }

            newAction += maxChar;
        }

        return newAction;
    }

    public Mutagen generateGuess(ArrayList<Pair<String, String>>guesses) {
        Mutagen m = new Mutagen();
        for(Pair p : guesses) {
            m.add((String) p.getKey(), (String) p.getValue());
        }
        return m;
    }

    public String proposeNewPattern(ArrayList<MyTree> trees) {
        MyTree firstTree = trees.get(0);
        MyTree mergedTree = new MyTree(firstTree.patternCounts, firstTree.actionIndexCounts, firstTree.action);
        for(int i=1; i < trees.size(); i++) {
            mergedTree.mergeTree(trees.get(i), 0);
        }
        return mergedTree.computeBestPattern(new HashSet<>());
    }

    //a function that clean up trailing acgt in the pattern
    public String cleanPattern(String pat){
        String[] colonArray = pat.split(";");
        int stop = colonArray.length-1;
        while (stop >= 0){
            if (colonArray[stop].length() == 4) {
                stop-=1;
            }
            else{
                break;
            }
        }
        String out = "";
        for (int i = 0; i < stop+1; i++){
            out+=colonArray[i];
            if (i!=stop && stop!=0){
                out+=";";
            }
        }
        return out;
    }


    public ArrayList<Pair<String, String>>addRoundofGuesses(ArrayList<Pair<String, String>>currentGuesses) {
        ArrayList<Pair<String, String>>filteredGuesses = new ArrayList<>();
        int count;

        for (int i  = 0; i<currentGuesses.size();i++){
        count = guessCounts.containsKey(currentGuesses.get(i)) ? guessCounts.get(currentGuesses.get(i)) : 0;
        guessCounts.put(currentGuesses.get(i), count + 1);

        }
        //this is a parameter we can tune
        double keep_top = 0.75;
        //find max count
        Map.Entry<Pair<String,String>, Integer> maxEntry = null;

        for (Map.Entry<Pair<String,String>, Integer> entry : guessCounts.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        //find patterns that are in current guesses and occurs frequently enough
        for(Map.Entry<Pair<String,String>, Integer> other_entry : guessCounts.entrySet()){
            Pair<String,String> pa_ac_pair = other_entry.getKey();
            int num = other_entry.getValue();
            if (num > maxEntry.getValue() * keep_top && currentGuesses.contains(pa_ac_pair)){
                filteredGuesses.add(pa_ac_pair);
            }
        }

        return filteredGuesses;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        // key=action, value: MyTree --> 4x10 matrix of every pattern that generated that action
        HashMap<String,MyTree>trees = new HashMap<>();
        boolean isCorrect;
        // Pair = pattern, action
        ArrayList<Pair<String, String>>guesses = new ArrayList<>();
        Set<String>incorrectGuesses = new HashSet<String>();

        for (int iter = 0; iter < 299; iter++) {
            String genome = randomString();
            String mutated = console.Mutate(genome);

            ArrayList<ArrayList<Integer>> possibleWindows = this.getPossibleWindows(genome, mutated, m);
            if(possibleWindows.size() == 0) {
                System.out.println("No mutations possible.");
                continue;
            }

            for(ArrayList<Integer> windowSet : possibleWindows) {
                for(int startIdx : windowSet) {
                    String beforeMutation = getWrappedSubstring(startIdx, startIdx + 10, genome);
                    String afterMutation = getWrappedSubstring(startIdx, startIdx + 10, mutated);
                    String action = this.getAction(beforeMutation, afterMutation);
                    if(trees.containsKey(action)) {
                        MyTree tree = trees.get(action);
                        tree.addPattern(beforeMutation);
                        tree.addActionIndices(beforeMutation);
                    } else {
                        MyTree tree = new MyTree(beforeMutation, action);
                        trees.put(action, tree);
                    }
                }
            }

            this.mergeTrees(trees);
            int maxSupport = 0;
            for(MyTree t: trees.values()) {
                maxSupport = Math.max(t.support, maxSupport);
            }

            // supportCoeff = 0.8
            // if we've seen a rule 80% of the time that we've seen the most frequent rule, include it
            // a@t, a;c;g;t;g;t;t;g;t@a --> second rule is going to be less
            int supportThreshold = (int) Math.round(maxSupport * this.supportCoeff);
            guesses = new ArrayList<>();

            // only the non numeric patterns and actions - only observed things
            ArrayList<String> actionCandidates = new ArrayList<>();
            ArrayList<String> patternCandidates = new ArrayList<>();
            ArrayList<Integer> supportCandidates = new ArrayList<>();

            // c@9 --> c@a, c@g, c@t
            // c --> <c@a>, <c@g>, <c@t> --> attempt to merge them into alphanumeric
            HashMap<String, ArrayList<MyTree>> patternToTrees = new HashMap<>();
            HashSet<String> repeatedPatterns = new HashSet<>();

            for(MyTree t: trees.values()) {
                // pattern counts for each and pick latest index which has an entropy < 0.8
                // truly random (acgt) --> [25 25 25 25] --> [0.25, 0.25, 0.25] --> H(p) / log(2) [0, 1]
                String pattern = t.computeBestPattern(incorrectGuesses);

                double rareFactor = this.rareFactor(pattern);
                if(patternToTrees.containsKey(pattern)) {
                    patternToTrees.get(pattern).add(t);
                    repeatedPatterns.add(pattern);
                } else {
                    ArrayList<MyTree> tmp = new ArrayList<>();
                    tmp.add(t);
                    patternToTrees.put(pattern, tmp);
                }
                if(t.support >= supportThreshold || rareFactor < rareFactorThreshold) {
                    actionCandidates.add(t.action);
                    patternCandidates.add(pattern);
                    supportCandidates.add(t.support);
                }
            }

            /*
            * minor edge case - can ignore for you
             */
            for(int cIdx = 0; cIdx < actionCandidates.size(); cIdx++) {
                boolean isSubset = false;
                for(int otherIdx = 0; otherIdx < actionCandidates.size(); otherIdx++) {
                    Pair<String, String> p1 = new Pair<>(patternCandidates.get(otherIdx), actionCandidates.get(otherIdx));
                    Pair<String, String> p2 = new Pair<>(patternCandidates.get(cIdx), actionCandidates.get(cIdx));
                    if(otherIdx != cIdx && this.consumes(p1, p2)) {
                        isSubset = true;
                        break;
                    }
                }
                if(!isSubset) {
                    guesses.add(new Pair(patternCandidates.get(cIdx), actionCandidates.get(cIdx)));
                }
            }

            // 1. have we predicted the same pattern more than once? we might to merge these into alphanumeric action
            // c@9 --> c@g, c@t, c@a is there a value that is longer than 1 in this hashmap?
            ArrayList<Pair<String, String>>alphaNumericGuesses = new ArrayList<>();
            ArrayList<Integer>numericSupports = new ArrayList<>();
            ArrayList<String>guessesToRemove = new ArrayList<>();

            // flip a coin and attempt to guess numeric if less than guessNumericProbability
            boolean guessNumeric = Math.random() < guessNumericProbability;
            int maxNumericSupport = 0;
            if(guessNumeric) {
                // guessed pattern --> all trees that have predicted this patterrn
                for(String pattern : patternToTrees.keySet()) {
                    ArrayList<MyTree> potentialNumericTrees = patternToTrees.get(pattern);
                    // if more than one tree has predicted a pattern, try to merge into a numeric
                    if(potentialNumericTrees.size() > 1) {
                        Pair<String, ArrayList<MyTree>> tmp = this.proposeAlphaNumericAction(potentialNumericTrees);
                        String alphaAction = tmp.getKey();
                        ArrayList<MyTree>contributingTrees = tmp.getValue();
                        int combinedSupport = 0;
                        if(contributingTrees != null) {
                            for(MyTree ct : contributingTrees) {
                                combinedSupport += ct.support;
                            }
                            maxNumericSupport = Math.max(maxNumericSupport, combinedSupport);
                            String proposedNewPattern = this.proposeNewPattern(contributingTrees);
                            double rareFactor = this.rareFactor(proposedNewPattern);
                            if(combinedSupport >= supportThreshold || rareFactor < rareFactorThreshold) {
                                Pair<String, String>p = new Pair<>(proposedNewPattern, alphaAction);
                                alphaNumericGuesses.add(p);
                                for(MyTree ct : contributingTrees) {
                                    guessesToRemove.add(pattern + "@" + ct.action);
                                }
                                numericSupports.add(combinedSupport);
                            }
                        }
                    }
                }
            }

            String resultStr = "\n";
            guesses.addAll(alphaNumericGuesses);
            supportCandidates.addAll(numericSupports);
            int updatedSupportThreshold = (int) Math.round(Math.max(maxNumericSupport, maxSupport) * this.supportCoeff);

            // Merge guesses of the form 4x <acgt>;x@<acgt>y
            HashMap<String, ArrayList<Pair<String, String>>> weirdCases = new HashMap<>();
            HashMap<String, Integer> weirdCaseSupport = new HashMap<>();
            for(int gIdx = 0; gIdx < guesses.size(); gIdx++) {
                Pair<String, String> guess = guesses.get(gIdx);
                int support = supportCandidates.get(gIdx);
                String pattern = guess.getKey();
                String action = guess.getValue();

                String key = pattern.substring(1) + "|" + action.substring(1);
                if(pattern.length() > 1 && pattern.charAt(1) == ';') {
                    key = key.substring(1);
                }

                if(action.length() == 1) {
                    key += action;
                }

                if(weirdCases.containsKey(key)) {
                    weirdCases.get(key).add(guess);
                    weirdCaseSupport.put(key, weirdCaseSupport.get(key) + support);
                } else {
                    ArrayList<Pair<String, String>> tmp = new ArrayList<>();
                    tmp.add(guess);
                    weirdCases.put(key, tmp);
                    weirdCaseSupport.put(key, support);
                }
            }

            ArrayList<Pair<String, String>>mergedGuesses = new ArrayList<>();
            ArrayList<Integer> mergedSupportCandidates = new ArrayList<>();
            for(String key : weirdCases.keySet()) {
                ArrayList<Pair<String, String>> wc = weirdCases.get(key);
                mergedSupportCandidates.add(weirdCaseSupport.get(key));
                if(wc.size() == 1) {
                    mergedGuesses.addAll(wc);
                } else if(wc.size() < 4) {
                    // Merge
                    String [] split = key.split("[|]");
                    String pattern = split[0];
                    String action = split[1];

                    String firstPosChars = "";
                    for(Pair<String, String> p : wc) {
                        firstPosChars += p.getKey().substring(0, 1);
                    }
                    if(pattern.length() == 0) {
                        pattern = firstPosChars;
                    } else {
                        pattern = firstPosChars + ";" + pattern;
                    }
                    action = "0" + action;
                    Pair<String, String> newGuess = new Pair<>(pattern, action);
                    mergedGuesses.add(newGuess);
                } else {
                    // Merge
                    String [] split = key.split("[|]");
                    String pattern = split[0];
                    String action = split[1];
                    String adjustedAction = "";
                    for(int cIdx = 0; cIdx < action.length(); cIdx++) {
                        String subString = action.substring(cIdx, cIdx + 1);
                        if(subString.matches(".*\\d.*")) {
                            int newInt = Math.max(Integer.parseInt(subString) - 1, 0);
                            adjustedAction += Integer.toString(newInt);
                        } else {
                            adjustedAction += subString;
                        }
                    }

                    if(pattern.length() == 0) {
                        pattern = "acgt";
                    }
                    Pair<String, String> newGuess = new Pair<>(pattern, adjustedAction);
                    mergedGuesses.add(newGuess);
                }
            }

            ArrayList<Pair<String, String>>finalGuesses = new ArrayList<>();
            int mostConfidentGuessSupport = -1;
            Pair<String, String> mostConfidentGuess = null;

            for(int gIdx=0; gIdx < mergedGuesses.size(); gIdx++) {
                String concise_guess = cleanPattern(mergedGuesses.get(gIdx).getKey());
                String guess = concise_guess + "@" + mergedGuesses.get(gIdx).getValue();
                boolean rare = this.rareFactor(concise_guess) < this.rareFactorThreshold;
                if(! guessesToRemove.contains(guess) &&
                        (rare || mergedSupportCandidates.get(gIdx) >= updatedSupportThreshold)) {
                    resultStr += guess;
                    if(gIdx < guesses.size() - 1) {
                        resultStr += "\n";
                    }
                    finalGuesses.add(mergedGuesses.get(gIdx));
                }

                if(supportCandidates.get(gIdx) >= mostConfidentGuessSupport) {
                    mostConfidentGuessSupport = supportCandidates.get(gIdx);
                    mostConfidentGuess = mergedGuesses.get(gIdx);
                }
            }

            if(finalGuesses.size() == 0) {
                ArrayList<Pair<String, String>>tmp = new ArrayList<>();
                tmp.add(mostConfidentGuess);
                finalGuesses = tmp;
            }

            ArrayList<Pair<String, String>> sampledGuesses = this.addRoundofGuesses(finalGuesses);
            if(Math.random() < 0.5) {
                guesses = sampledGuesses;
            } else {
                guesses = finalGuesses;
            }
            isCorrect = console.testEquiv(this.generateGuess(guesses));
            String numericMsg = guessNumeric ? "(allow numeric)" : "(no numeric)";
            if(isCorrect) {
                System.out.println("Correct: " + numericMsg + resultStr);
                break;
            } else {
                System.out.println("Incorrect: " + numericMsg + resultStr);
                incorrectGuesses.add(resultStr);
            }

            if(Math.floorMod(iter + 1, this.startOverFreq) == 0) {
                System.out.println("Starting over!");
                this.guessCounts = new HashMap<>();
                trees = new HashMap<>();
            }
        }
        return this.generateGuess(guesses);
    }
}
