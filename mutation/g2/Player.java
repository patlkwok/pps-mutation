/*
TODO:
- Implement Wrap Around
- dont guess the same thing twice
- implement context checking

NEEDS:
Make smarter test cases
handle more complicated switches? 

*/

package mutation.g2;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {
    private Random random;

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

    public Mutagen Play(Console console, int m){
      Mutagen result = new Mutagen();
      HashMap<String, Integer> evidence = new HashMap<>();

      for (int i = 0; i < 10; ++ i){

        // run a random experiment
        String genome = randomString();
        String mutated = console.Mutate(genome);
        List<Change> changes = Utilities.diff(genome, mutated);

        // collect evidence
        for(Change c: changes){
          String key = c.getChange();
          if(!evidence.containsKey(key)){
            evidence.put(key, 0);
          }
          evidence.put(key, evidence.get(key)+1);
        }

        //guess strongest evidence
        String maxString = Utilities.argMax(evidence);
        if(maxString != null){
          Change maxEvidence = Change.fromChangeString(maxString);

          result.add(Utilities.formatPattern(maxEvidence.before),maxEvidence.after);
          boolean guess = console.Guess(result);
          if(guess){
            Utilities.alert("Correctly guessed");
            break;
          }
        }
      }

      Utilities.alert(evidence);
      return result;
    }

}
