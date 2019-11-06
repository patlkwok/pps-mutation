package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
import java.util.*;

import javafx.util.Pair;


public class Player extends mutation.sim.Player {
    private Random random;
    public final ArrayList<Character> bases = new ArrayList<Character>(Arrays.asList('a', 'c', 'g', 't'));

    /*
    HYPER-PARAMETERS
     */
    public double guessNumericProbability = 0.75; // percentage of the time we allow numbers in guesses
    // if we've seen the most popular rule 10 times,
    // we only accept other multiple rules if we've seen them 10 * supportCoeff or greater
    public double supportCoeff = 0.8;
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

    public void mergeTrees(HashMap<String, MyTree> trees) {
        // Merge tree a into tree b iff a --> <x>;(acgt)@<y> and b --> <x>@<y>(acgt)
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

    public Pair<String, ArrayList<MyTree>> proposeAlphaNumericAction(String pattern, ArrayList<MyTree> trees) {
        HashMap<Integer, ArrayList<MyTree>> numericActionToTrees = new HashMap<>();
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

    @Override
    public Mutagen Play(Console console, int m) {
        HashMap<String,MyTree>trees = new HashMap<>();

        boolean isCorrect;
        ArrayList<Pair<String, String>>guesses = new ArrayList<>();
        Set<String>incorrectGuesses = new HashSet<String>();

        for (int iter = 0; iter < 100; iter++) {
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

            int supportThreshold = (int) Math.round(maxSupport * this.supportCoeff);
            guesses = new ArrayList<>();

            ArrayList<String> actionCandidates = new ArrayList<>();
            ArrayList<String> patternCandidates = new ArrayList<>();
            ArrayList<Integer> supportCandidates = new ArrayList<>();

            HashMap<String, ArrayList<MyTree>> patternToTrees = new HashMap<>();
            HashSet<String> repeatedPatterns = new HashSet<>();

            for(MyTree t: trees.values()) {
                String pattern = t.computeBestPattern(incorrectGuesses);

                if(patternToTrees.containsKey(pattern)) {
                    patternToTrees.get(pattern).add(t);
                    repeatedPatterns.add(pattern);
                } else {
                    ArrayList<MyTree> tmp = new ArrayList<>();
                    tmp.add(t);
                    patternToTrees.put(pattern, tmp);
                }
                if(t.support >= supportThreshold) {
                    actionCandidates.add(t.action);
                    patternCandidates.add(pattern);
                    supportCandidates.add(t.support);
                }
            }

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

            ArrayList<Pair<String, String>>alphaNumericGuesses = new ArrayList<>();
            ArrayList<Integer>numericSupports = new ArrayList<>();
            ArrayList<String>guessesToRemove = new ArrayList<>();

            boolean guessNumeric = Math.random() < guessNumericProbability;
            int maxNumericSupport = 0;
            if(guessNumeric) {
                for(String pattern : patternToTrees.keySet()) {
                    ArrayList<MyTree> potentialNumericTrees = patternToTrees.get(pattern);
                    if(potentialNumericTrees.size() > 1) {
                        Pair<String, ArrayList<MyTree>> tmp = this.proposeAlphaNumericAction(pattern, potentialNumericTrees);
                        String alphaAction = tmp.getKey();
                        ArrayList<MyTree>contributingTrees = tmp.getValue();
                        int combinedSupport = 0;
                        if(contributingTrees != null) {
                            for(MyTree ct : contributingTrees) {
                                combinedSupport += ct.support;
                            }
                            maxNumericSupport = Math.max(maxNumericSupport, combinedSupport);
                            if(combinedSupport >= supportThreshold) {
                                String proposedNewPattern = this.proposeNewPattern(contributingTrees);
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
            ArrayList<Pair<String, String>>finalGuesses = new ArrayList<>();

            int mostConfidentGuessSupport = -1;
            Pair<String, String> mostConfidentGuess = null;

            for(int gIdx=0; gIdx < guesses.size(); gIdx++) {
                String guess = guesses.get(gIdx).getKey() + "@" + guesses.get(gIdx).getValue();
                if(! guessesToRemove.contains(guess) && supportCandidates.get(gIdx) >= updatedSupportThreshold) {
                    resultStr += guess;
                    if(gIdx < guesses.size() - 1) {
                        resultStr += "\n";
                    }
                    finalGuesses.add(guesses.get(gIdx));
                }

                if(supportCandidates.get(gIdx) >= mostConfidentGuessSupport) {
                    mostConfidentGuessSupport = supportCandidates.get(gIdx);
                    mostConfidentGuess = guesses.get(gIdx);
                }
            }

            if(finalGuesses.size() == 0) {
                ArrayList<Pair<String, String>>tmp = new ArrayList<>();
                tmp.add(mostConfidentGuess);
                finalGuesses = tmp;
            }

            guesses = finalGuesses;
            isCorrect = console.Guess(this.generateGuess(guesses));
            String numericMsg = guessNumeric ? "(allow numeric)" : "(no numeric)";
            if(isCorrect) {
                System.out.println("Correct: " + numericMsg + resultStr);
                break;
            } else {
                System.out.println("Incorrect: " + numericMsg + resultStr);
                incorrectGuesses.add(resultStr);
            }
        }

        return this.generateGuess(guesses);
    }
}
