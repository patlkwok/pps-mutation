/*

right now: random string + mutations, records how many times it has seen something, guesses.
only runs 1/2 experiments. really naive.

TODO:
- Implement Wrap Around
- dont guess the same thing twice
- implement context checking
( a better way to do this might be to record changes as before -> after and
keep a count of how many times we've seen a change. Everytime we see a change
record the possible contexts(sliding window) towards the end pick out common elements
in the context )

goals:
Make smarter test cases
Handle more complicated mutations
Possibly model after the scientific method ( can we model that as a search problem)

TODO:
find original base length
deal with multiple rules
 */

package mutation.g2;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {
    private Random random;
    private int numberExperiments = 200;

    public Player() {
      Utilities.alert("Hello world");
      random = new Random();
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++ i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    public List<Change> collectData(int experiments, Console console){
      List<Change> changes = new ArrayList<Change>();
      //collect experimental data
      for(int i = 0; i < experiments; ++ i){
        Mutagen result = new Mutagen();
        String genome = randomString();
        String mutated = console.Mutate(genome);
        changes.addAll(Utilities.diff(genome, mutated));
      }
      return changes;
    }

    public Mutagen Play(Console console, int m){
      List<Change> changes = this.collectData(numberExperiments, console);
      HashMap<String, List<Change>> artifactChanges = Utilities.sortByAfterArtifact(changes);
      //unique artifacts
      List<String> uniqueArtifacts = new ArrayList<String>(artifactChanges.keySet());

      HashMap<String, List<Change>> actionChanges = new HashMap<>();
      HashMap<String, Integer> actionCount = new HashMap<>();

      // get all possible actions and the changes they're linked to
      for(String artifact: uniqueArtifacts){
        List<Change> relatedChanges = artifactChanges.get(artifact);
        HashMap<Integer, List<String>> contextsByReference = Utilities.getAfterContextByReferencePoint(relatedChanges);
        HashMap<Integer, String> collapsedWindows = Utilities.collapseContexts(contextsByReference);
        int bestWindowReference = Utilities.bestWindow(collapsedWindows);
        String bestWindow = collapsedWindows.get(bestWindowReference);
        int lengthOfInterest = Utilities.getLastNondisjunctive(bestWindow)+1;

        // all the best windows results
        for(String c: contextsByReference.get(bestWindowReference)){
          String action = c.substring(0, lengthOfInterest);
          if(!actionChanges.containsKey(action)){
            actionChanges.put(action, new ArrayList<Change>());
          }
          if(!actionCount.containsKey(action)){
            actionCount.put(action, 0);
          }
          actionCount.put(action, actionCount.get(action)+1);
        }
        for(Change c: relatedChanges){
          String action = c.afterContext.get(bestWindowReference).substring(0, lengthOfInterest);
          c.pointOfInterest = c.location + bestWindowReference;
          c.pointOfInterest %= 1000;
          if(c.pointOfInterest < 0){
            c.pointOfInterest += 1000;
          }
          actionChanges.get(action).add(c);
        }
      }

      ArrayList<String> uniqueActions = new ArrayList<String>(actionChanges.keySet());
      HashMap<List<String>, Integer> solution = new HashMap<>();
      for(String action: uniqueActions){
        List<Change> relatedChanges = actionChanges.get(action);
        relatedChanges = Utilities.deduplicateListChanges(relatedChanges);
        HashMap<Integer, List<String>> patternContext = new HashMap<>();

        int lookupLength = 10 - action.length();
        for(int ctr = 0; ctr <= lookupLength; ctr++){
          patternContext.put(-1*ctr+lookupLength, new ArrayList<String>());
          for(Change c: relatedChanges){
            int pointOfInterest = c.pointOfInterest;
            int startingPoint = pointOfInterest - lookupLength+ctr;
            if(startingPoint < 0){
              startingPoint += 1000;
            }
            String currentString = "";
            for(int i = 0; i < 10; i++){
              int currentPos = startingPoint + i;
              currentPos %= 1000;
              currentString += c.beforeGenome.charAt(currentPos)+"";
            }
            patternContext.get(-1*ctr+lookupLength).add(currentString);
          }
        }
        HashMap<Integer, String> collapsedWindows = Utilities.collapseContexts(patternContext);
        int bestWindowReference = Utilities.bestWindow(collapsedWindows);
        String bestWindow = collapsedWindows.get(bestWindowReference);
        // System.out.println(bestWindow + " -> " + action);
        if(patternContext.get(bestWindowReference).size() < 10){
          continue;
        }
        int lengthOfInterest = Utilities.getLastNondisjunctive(bestWindow)+1;
        // System.out.print(Utilities.formatPattern(patternContext.get(bestWindowReference).get(0).substring(0, lengthOfInterest)) + " -> " + action);
        // System.out.print(" @ ,  " +patternContext.get(bestWindowReference).size() + " \n");
        List<String> thisSol = new ArrayList<String>();
        thisSol.add(Utilities.formatPattern(patternContext.get(bestWindowReference).get(0).substring(0, lengthOfInterest)));
        thisSol.add(action);
        solution.put(thisSol, Utilities.getScore(patternContext, bestWindowReference));
      }
      System.out.println(solution);
      Mutagen sol = new Mutagen();
      for(List<String> s : solution.keySet()){
        sol.add(s.get(0), s.get(1));
      }
      return sol;
    }

}
