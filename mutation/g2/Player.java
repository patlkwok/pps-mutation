package mutation.g2;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

public class Player extends mutation.sim.Player {
    private Random random;
    private int numberExperiments = 100;

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

    public ActionComposite getActionChanges(List<String> uniqueArtifacts, HashMap<String, List<Change>> artifactChanges){
      HashMap<String, List<Change>> actionChanges = new HashMap<>();
      HashMap<String, Integer> actionCount = new HashMap<>();

      // get all possible actions and the changes they're linked to
      for(String artifact: uniqueArtifacts){
        List<Change> relatedChanges = artifactChanges.get(artifact);
        System.out.println(artifact);
        if(relatedChanges.size() == 1){
          continue;
        }
        HashMap<Integer, List<String>> contextsByReference = Utilities.getAfterContextByReferencePoint(relatedChanges);
        HashMap<Integer, String> collapsedWindows = Utilities.collapseContexts(contextsByReference);
        int bestWindowReference = Utilities.bestWindow(collapsedWindows);
        String bestWindow = collapsedWindows.get(bestWindowReference);
        System.out.println(collapsedWindows);
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
      ActionComposite a = new ActionComposite();
      a.actionChanges = actionChanges;
      a.actionCount = actionCount;
      return a;
    }

    public Mutagen Play(Console console, int m){
      List<Change> changes = this.collectData(numberExperiments, console);
      HashMap<String, List<Change>> artifactChanges = Utilities.sortByAfterArtifact(changes);
      //unique artifacts
      List<String> uniqueArtifacts = new ArrayList<String>(artifactChanges.keySet());

      // We'll prune an artifact if we see it less than 10% of the time (m * numberExperiments)
      // this is reasonable as even a very complicated mutation (4 manifestations)
      // in a 2 rule system will come up atleast 12.5% of the time
      double prunePercentage = 0.075;
      for(String artifact: uniqueArtifacts){
        if(artifactChanges.get(artifact).size() < prunePercentage*numberExperiments*m){
          artifactChanges.remove(artifact);
        }
      }
      uniqueArtifacts = new ArrayList<String>(artifactChanges.keySet());
      Utilities.alert("Unique Artifacts Observed "+uniqueArtifacts);


      ActionComposite a = getActionChanges(uniqueArtifacts, artifactChanges);
      HashMap<String, List<Change>> actionChanges = a.actionChanges;
      HashMap<String, Integer> actionCount = a.actionCount;

      // get all the unique actions
      ArrayList<String> uniqueActions = new ArrayList<String>(actionChanges.keySet());
      for(String action: uniqueActions){
        if(actionCount.get(action) <= numberExperiments*0.2){
          actionCount.remove(action);
          actionChanges.remove(action);
        }
      }
      uniqueActions = new ArrayList<String>(actionChanges.keySet());
      System.out.println(actionCount);



      HashMap<List<String>, Integer> solution = new HashMap<>();

      // back solve to look for patterns near those actions
      //for eahc action
      for(String action: uniqueActions){
        //what are the changes
        List<Change> relatedChanges = actionChanges.get(action);
        relatedChanges = Utilities.deduplicateListChanges(relatedChanges);
        //context
        HashMap<Integer, List<String>> patternContext = new HashMap<>();

        // look 10 around thhe action
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

        // collapse context windows generated above
        HashMap<Integer, String> collapsedWindows = Utilities.collapseContexts(patternContext);
        System.out.println(collapsedWindows);

        // find the bst window reference based on the same principle as before
        int bestWindowReference = Utilities.bestWindow(collapsedWindows);

        // find the  best window
        String bestWindow = collapsedWindows.get(bestWindowReference);
        System.out.println(bestWindow);
        // if you dont see this action atleast 10% of the time fuck it
        if(patternContext.get(bestWindowReference).size() < (numberExperiments*0.1)){
          System.out.println(bestWindow);
          continue;
        }

        // find the end of the window
        int lengthOfInterest = Utilities.getLastNondisjunctive(bestWindow)+1;
        List<String> thisSol = new ArrayList<String>();

        HashMap<String, Integer> allPatterns = new HashMap<String, Integer>();
        for(String pattern: patternContext.get(bestWindowReference)){
          String p = pattern.substring(0,lengthOfInterest);
          if(!allPatterns.containsKey(p)){
            allPatterns.put(p,0);
          }
          allPatterns.put(p, allPatterns.get(p)+1);
        }
        for(String pattern: allPatterns.keySet()){
          thisSol.add(Utilities.formatPattern(pattern));
          thisSol.add(action);
          solution.put(thisSol, allPatterns.get(pattern));
          thisSol = new ArrayList<String>();
        }
      }



      solution = Utilities.sortByValue(solution);


      List<Integer> occurences = new ArrayList<Integer>(solution.values());
      int mean = Utilities.mean(occurences);

      List<List<String>> proposedRules = new ArrayList<List<String>>(solution.keySet());
      for(List<String> rule : proposedRules){
        if(solution.get(rule) <= 5){
          solution.remove(rule);
        }
      }

      Mutagen sol = new Mutagen();
      for(List<String> s : solution.keySet()){
        if(solution.get(s) < mean){
          continue;
        }
        sol.add(s.get(0), s.get(1));
        System.out.println(s + " : " + solution.get(s));
        console.testEquiv(sol);
        if(console.isCorrect()){
          Utilities.alert("Correct!");
          return sol;
        }
      }
      System.out.println(solution);
      return sol;
    }

}
