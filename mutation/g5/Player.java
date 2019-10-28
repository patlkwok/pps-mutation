package mutation.g5;

import java.awt.Point;
import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {

	private Random random;
	private Console console;
	private int numMutations;
	private List<Map<Character, Integer>> patternTracker = new ArrayList<>();
	private List<Map<Character, Integer>> actionTracker = new ArrayList<>();
	private static final int DISTANCE_THRESHOLD = 16;
	private static final int NONEXISTENT_BASE_THRESHOLD = 2;

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
    	this.console = console;
    	setUpTrackers();
        String genome = randomString();
        int numBasesMutatedPerMutation = 0;
        for(int i = 0; i < 10; i++) {
            String mutatedGenome = console.Mutate(genome);
            this.numMutations = console.getNumberOfMutations();
            int numBasesMutated = 0;
        	for(int j = 0; j < genome.length(); j++)
        		if(genome.charAt(j) != mutatedGenome.charAt(j))
        			numBasesMutated++;
        	numBasesMutatedPerMutation = (int) Math.ceil(numBasesMutated * 1.0 / numMutations);
            implementTrackers(genome, mutatedGenome, numBasesMutatedPerMutation);        	
        }
        
    	System.out.println("Action tracker: ");
        for(int i = 0; i < actionTracker.size(); i++)
        	System.out.println("  " + (i + 1) + ". " + actionTracker.get(i));
    	System.out.println("Pattern tracker: ");
        for(int i = 0; i < patternTracker.size(); i++)
        	System.out.println("  " + (i + 1) + ". " + patternTracker.get(i));
        
        List<Mutagen> possibleMutagens = getPossibleMutagens(numBasesMutatedPerMutation);
        for(Mutagen mutagen : possibleMutagens) {
        	if(console.Guess(mutagen))
        		return mutagen;
        }
        
        if(possibleMutagens.size() > 0)
        	return possibleMutagens.get(0);

        Mutagen mutagen = new Mutagen();
        mutagen.add("a;c;c", "att");
        mutagen.add("g;c;c", "gtt");
        return mutagen;
    }

    private List<Mutagen> getPossibleMutagens(int numBasesMutatedPerMutation) {
    	List<Mutagen> mutagens = new ArrayList<>();
    	if(numBasesMutatedPerMutation == 1) {
    		Map<Integer, List<String>> interestingPatternsMap = new HashMap<>();
			for(int i = 0; i < patternTracker.size(); i++) {
				Map<Character, Integer> patternOccurrences = patternTracker.get(i);
				List<String> interestingPattern = new ArrayList<>();
				for(Character character : patternOccurrences.keySet()) {
					if(patternOccurrences.get(character) > NONEXISTENT_BASE_THRESHOLD)
						interestingPattern.add(character + "");
				}
				if(interestingPattern.size() != 4)
					interestingPatternsMap.put(i, interestingPattern);
			}
    		
    		Map<Character, Integer> actionOccurrences = actionTracker.get(0);
			List<Integer> possibleLocationsForAction = new ArrayList<>();
    		char letterMutation = getLetterMutation(actionOccurrences);    		
    		if(letterMutation != 'x') {
    			for(int i = 0; i < patternTracker.size(); i++) {
    				Map<Character, Integer> patternOccurrences = patternTracker.get(i);
    				boolean isValidLocationForAction = true;
    				for(Character character : patternOccurrences.keySet()) {
    					if((character != letterMutation && patternOccurrences.get(character) == 0) ||
    							(character == letterMutation && patternOccurrences.get(character) != 0)) {
    						isValidLocationForAction = false;
    						break;
    					}
    				}
    				if(isValidLocationForAction)
    					possibleLocationsForAction.add(i);
    			}
    			
    			/*
    			 * Determine pattern
    			 */
    			String pattern = "";
				int offsetForBaseMutationInAction = 0;
    			if(interestingPatternsMap.size() == 0) {
    				pattern += "acgt";
    			}
    			else {
    				/*
    				 * If we have acgt;acgt;gt;acgt;acgt;act@012c, then
    				 * "offsetForBaseMutationInAction" would be 3, since "c"
    				 * is in offset 3 in the action.
    				 */
    				List<Integer> interestingPatternLocations = new ArrayList<>();
    				for(Integer interestingPatternLocation : interestingPatternsMap.keySet())
    					interestingPatternLocations.add(interestingPatternLocation);
    				int currentLocation = interestingPatternLocations.get(0);
    				offsetForBaseMutationInAction = 9 - currentLocation;

    				for(int i = 0; i < interestingPatternLocations.size(); i++) {
    					int nextLocation = interestingPatternLocations.get(i);
    					while(currentLocation < nextLocation) {
    						pattern += "acgt;";
    						currentLocation++;
    					}
    					if(i == interestingPatternLocations.size() - 1)
    						pattern += String.join("", interestingPatternsMap.get(nextLocation));
    					else
    						pattern += String.join("", interestingPatternsMap.get(nextLocation)) + ";";
    					currentLocation++;
    				}
    			}
    			/*
    			 * Determine action
    			 */
    			String action = "";
    			for(int i = 0; i < offsetForBaseMutationInAction; i++) {
    				action += Integer.toString(i);
    			}
    			action += letterMutation;

    			System.out.println("Predicted pattern: " + pattern);
    			System.out.println("Predicted action: " + action);
    			System.out.println("Predicted mutated base: " + letterMutation);
    			System.out.println("Offset for base mutation in action: " + offsetForBaseMutationInAction);
    			System.out.println("Predicted rule: " + pattern + "@" + action);
    			
    			Mutagen mutagen = new Mutagen();
    			mutagen.add(pattern, action);
    			mutagens.add(mutagen);
    		}
    		
    		// Either the mutation is numerical, or the mutation could be either base or numerical
			for(int i = 0; i < patternTracker.size(); i++) {
				Map<Character, Integer> patternOccurrences = patternTracker.get(i);
				double distance = getDistance(actionOccurrences, patternOccurrences);
				if(distance <= DISTANCE_THRESHOLD)
					possibleLocationsForAction.add(i);
			}			
    	}
    	else {
    		
    	}
    	return mutagens;
    }
    
    private double getDistance(Map<Character, Integer> set1,
    						   Map<Character, Integer> set2) {
    	double distance = 0.0;
    	for(Character character : set1.keySet())
    		distance += Math.pow(set1.get(character) - set2.get(character), 2);
    	return Math.sqrt(distance);
    }
    
    private void setUpTrackers() {
    	patternTracker = new ArrayList<>();
    	actionTracker = new ArrayList<>();
		for(int i = 0; i < 19; i++) {
			Map<Character, Integer> occurrences = new HashMap<>();
			occurrences.put('a', 0);
			occurrences.put('c', 0);
			occurrences.put('g', 0);
			occurrences.put('t', 0);
			patternTracker.add(occurrences);			
		}
		for(int i = 0; i < 10; i++) {
			Map<Character, Integer> occurrences = new HashMap<>();
			occurrences.put('a', 0);
			occurrences.put('c', 0);
			occurrences.put('g', 0);
			occurrences.put('t', 0);
			actionTracker.add(occurrences);			
		}
    }
    
    private Character getLetterMutation(Map<Character, Integer> occurrences) {
		int numAs = occurrences.get('a');
		int numCs = occurrences.get('c');
		int numGs = occurrences.get('g');
		int numTs = occurrences.get('t');
		if(numAs != 0 && numCs == 0 && numGs == 0 && numTs == 0)
			return 'a';
		if(numAs == 0 && numCs != 0 && numGs == 0 && numTs == 0)
			return 'c';
		if(numAs == 0 && numCs == 0 && numGs != 0 && numTs == 0)
			return 'g';
		if(numAs == 0 && numCs == 0 && numGs == 0 && numTs != 0)
			return 't';
		return 'x';
    }
    
    private void implementTrackers(String genome, String mutatedGenome, int numBasesMutatedPerMutation) {
    	List<Window> windows = createWindows(genome, mutatedGenome);
//    	System.out.println("Number of windows: " + windows.size());
//    	for(int i = 0; i < windows.size(); i++) {
//    		System.out.println("Window " + (i + 1));
//    		Window window = windows.get(i);
//    		System.out.println("   (1) Original base: " + window.originalBase);
//    		System.out.println("   (2) Mutated base: " + window.mutatedBase);
//    		System.out.println("   (3) Base location: " + window.baseLocation);
//    		System.out.println("   (4) Window coordinates: (" + window.windowCoordinates.x + ", " + window.windowCoordinates.y + ")");
//    		System.out.println("   (5) Number of overlapping windows: " + window.overlappingWindows.size());
//    	}
    	    			
		if(numBasesMutatedPerMutation == 1) {
    		for(Window window : windows) {
    			int start = window.windowCoordinates.x;
    			int baseLocation = window.baseLocation;
    			baseLocation += start > baseLocation ? 1000 : 0;
    			for(int i = 0; i < patternTracker.size(); i++) {
    				Map<Character, Integer> occurrences = patternTracker.get(i);
    				Character genomeBase = genome.charAt((start + i) % genome.length());
    				Character mutatedGenomeBase = mutatedGenome.charAt((start + i) % mutatedGenome.length());
    				Character baseToIncrement;
    				if(start + i < baseLocation)
    					baseToIncrement = mutatedGenomeBase;
    				else {
    					if(start + i == baseLocation)
        		    		actionTracker.get(0).put(mutatedGenomeBase, actionTracker.get(0).get(mutatedGenomeBase) + 1);
    					baseToIncrement = genomeBase;
    				}
					occurrences.put(baseToIncrement, occurrences.get(baseToIncrement) + 1);
    			}
    		}
    	}    	
		else {
			
		}
    }
    
    private List<Window> createWindows(String genome, String mutatedGenome) {
    	List<Window> windows = new ArrayList<>();
    	for(int i = 0; i < genome.length(); i++) {
    		if(genome.charAt(i) !=  mutatedGenome.charAt(i)) {
    			Window window = new Window();
    			window.originalBase = genome.charAt(i);
    			window.mutatedBase = mutatedGenome.charAt(i);
    			window.baseLocation = i;
        		window.windowCoordinates.x = i < 9 ? genome.length() - (9 - i) : i - 9;
    			window.windowCoordinates.y = i > genome.length() - 10 ? 9 - (genome.length() - i) : i + 9;
    			windows.add(window);
    		}
    	}

    	/*
		 * Examples of overlapping windows:
		 * 
		 * (1, 19) and (4, 22)
		 * (974, 992) and (986, 4)
		 * (986, 4) and (992, 10)
		 * (992, 10) and (4, 22)
		 * 
		 * Examples of non-overlapping windows:
		 * 
		 * (2, 20) and (34, 52)
		 * (974, 992) and (995, 13)
		 * (995, 13) and (34, 52)
		 */    					
    	for(int i = 0; i < windows.size(); i++) {
    		for(int j = 0; j < windows.size(); j++) {
    			if(windows.get(i).equals(windows.get(j)))
    				continue;
    			int x_i = windows.get(i).windowCoordinates.x;
    			int y_i = windows.get(i).windowCoordinates.y;
    			int x_j = windows.get(j).windowCoordinates.x;
    			int y_j = windows.get(j).windowCoordinates.y;

    			if((x_i > x_j && x_i < y_j) || (y_i > x_j && y_i < y_j) ||
    					(x_j > x_i && x_j < y_i) || (y_j > x_i && y_j < y_i))
    				windows.get(i).overlappingWindows.add(windows.get(j));
    			else {
        			y_i += (x_i > y_i) ? 1000 : 0;
        			y_j += (x_j > y_j) ? 1000 : 0;    				
        			if((x_i > x_j && x_i < y_j) || (y_i > x_j && y_i < y_j) ||
        					(x_j > x_i && x_j < y_i) || (y_j > x_i && y_j < y_i))
        				windows.get(i).overlappingWindows.add(windows.get(j));
    			}	
    		}
    	}
    	return windows;
    }
	
    class Window {
    	public char originalBase;
    	public char mutatedBase;
    	public int baseLocation;
    	public Point windowCoordinates = new Point();
    	public List<Window> overlappingWindows = new ArrayList<>();
    }
}