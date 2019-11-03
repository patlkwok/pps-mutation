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

		HashMap<String, Integer> evidence = new HashMap<>();
		//index array of evidences
		HashMap<String, List<Integer>> indexes = new HashMap<>();
		Set<String> discard = new HashSet<>();
		String[] ignore = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "a", "t", "c", "g"};
		for(String s: ignore)
			discard.add(s);
		Mutagen result = new Mutagen();;
		for (int i = 0; i < 10; ++ i){
			result = new Mutagen();
			// run a random experiment
			String genome = randomString();
			String mutated = console.Mutate(genome);
			List<Change> changes = Utilities.diff(genome, mutated);
			List<Mutation> mutations = new ArrayList<>();
			// convert changes to mutations composed of sets
			// TODO: find the minimum possible length of a mutation according to the consolidateChanges length
			for(Change c: changes) {
				mutations.add(Utilities.toSets(c.before, c.after, c.location));
			}
			List<Change> changesWithNumber = new ArrayList<>();
			System.out.println("Start Combination...");
			// try all the two combination of mutations, a mutation can be considered only if it appears more than twice
			for(int j = 0; j < mutations.size(); j++) {
				for(int k = j+1; k < mutations.size(); k++) {
					List<String> combine = Utilities.compareTwoMutations(mutations.get(j), mutations.get(k), genome, mutated);
					if(combine.size() == 0)
						continue;
					for(String c: combine) {
						//save mutation's index
						indexes.putIfAbsent(c, new ArrayList<Integer>());
						if(!indexes.get(c).contains(mutations.get(j).location))
							evidence.put(c, evidence.getOrDefault(c, 0)+1);
						if(!indexes.get(c).contains(mutations.get(k).location))
							evidence.put(c, evidence.getOrDefault(c, 0)+1);
						indexes.get(c).add(mutations.get(j).location);
						indexes.get(c).add(mutations.get(k).location);				
//						System.out.println("Combination: " + c);
					}
				}
			}
			boolean find = false;
			while(!find) {
				// if one mutation appears too much, it should not be a normal mutation, the upper bound is 2*(i+1)*m (can be further discussed)
				String maxString = Utilities.argMax(evidence, discard, 2*(i+1)*m);
				List<Integer> locations = indexes.get(maxString);
				// a string is only considered once
				discard.add(maxString);
				for(int location: locations) {
					Change maxEvidence;
					// only consider same length of the strings before and after mutation
					// TODO: find correct "before" length
					if(location+maxString.length() > 1000)
						maxEvidence = new Change(genome.substring(location, 1000) + genome.substring(0, location+maxString.length()-1000), maxString, location);
					else
						maxEvidence = new Change(genome.substring(location, location+maxString.length()), maxString, location);
					// ignore no change ones
					if(maxEvidence.before.equals(maxEvidence.after))
						continue;
					changesWithNumber.add(maxEvidence);
					find = true;
				}
			}

			System.out.println("RULES:");
			List<Rule> rules = Utilities.generateRules(changesWithNumber);
			for(Rule r: rules) {
				System.out.println(r.formatBefore());
				System.out.println(r.after);
				result.add(r.formatBefore(), r.after);
			}
			boolean guess = console.Guess(result);
			if(guess){
				Utilities.alert("Correct!");
				break;
			}
			Utilities.alert(evidence);
			System.out.println(evidence.size());
			// collect evidence
			//        for(Change c: changes){
			//          String key = c.getChange();
			//          if(!evidence.containsKey(key)){
			//            evidence.put(key, 0);
			//          }
			//          evidence.put(key, evidence.get(key)+1);
			//        }

			//guess strongest evidence
			//        String maxString = Utilities.argMax(evidence);
			//        if(maxString != null || evidence.get(maxString) == 1){
			//          Change maxEvidence = Change.fromChangeString(maxString);
			//
			//
			//          result.add(Utilities.formatPattern(maxEvidence.before),maxEvidence.after);
			//          boolean guess = console.Guess(result);
			//          if(guess){
			//            Utilities.alert("Correctly guessed");
			//            break;
			//          }
			//        }
		}
		return result;
	}

}
