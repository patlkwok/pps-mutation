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

      for(int i = 0; i < 1; ++ i){
        Mutagen result = new Mutagen();
        String genome = randomString();
        String mutated = console.Mutate(genome);
        List<Change> changes = Utilities.diff(genome, mutated);
        System.out.println(changes);
      }
      return new Mutagen();
    }

    // public Mutagen Play(Console console, int m){
    //
    //   HashMap<String, Integer> evidence = new HashMap<>();
    //   Mutagen result = new Mutagen();;
    //   for (int i = 0; i < 10; ++ i){
    //     result = new Mutagen();
    //     // run a random experiment
    //     String genome = randomString();
    //     String mutated = console.Mutate(genome);
    //     List<Change> changes = Utilities.diff(genome, mutated);
    //     System.out.println("RULES:");
    //     List<Rule> rules = Utilities.generateRules(changes);
    //     for(Rule r: rules) {
    //         System.out.println(r.formatBefore());
    //         System.out.println(r.after);
    //         result.add(r.formatBefore(), r.after);
    //     }
    //     boolean guess = console.Guess(result);
    //     if(guess){
    //         Utilities.alert("Correct!");
    //         break;
    //     }
    //   }
    //
    //   Utilities.alert(evidence);
    //   return result;
    // }

}
