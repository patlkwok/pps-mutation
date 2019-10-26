package mutation.g5;

import java.awt.Point;
import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {

	private Random random;
	private Console console;
	private int numMutations;

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
    	
        String genome = randomString();
        String mutatedGenome = console.Mutate(genome);
        this.numMutations = console.getNumberOfMutations();
        Mutagen mutagen = getMutagen(genome, mutatedGenome);
        console.Guess(mutagen);
        return mutagen;
    }
    
    private Mutagen getMutagen(String genome, String mutatedGenome) {
    	List<Window> windows = createWindows(genome, mutatedGenome);
    	System.out.println("Number of windows: " + windows.size());
    	for(int i = 0; i < windows.size(); i++) {
    		System.out.println("Window " + (i + 1));
    		Window window = windows.get(i);
    		System.out.println("   (1) Original base: " + window.originalBase);
    		System.out.println("   (2) Mutated base: " + window.mutatedBase);
    		System.out.println("   (3) Base location: " + window.baseLocation);
    		System.out.println("   (4) Window coordinates: (" + window.windowCoordinates.x + ", " + window.windowCoordinates.y + ")");
    		System.out.println("   (5) Number of overlapping windows: " + window.overlappingWindows.size());
    	}
    	
    	int numBasesMutated = 0;
    	for(int i = 0; i < genome.length(); i++)
    		if(genome.charAt(i) != mutatedGenome.charAt(i))
    			numBasesMutated++;
    	int ceilingNumBasesMutatedPerMutation = (int) Math.ceil(numBasesMutated * 1.0 / numMutations);
    	int floorNumBasesMutatedPerMutation = (int) Math.floor(numBasesMutated * 1.0 / numMutations);
    	
		List<Map<Character, Integer>> patternTracker = new ArrayList<>();
		Map<Character, Integer> actionTracker = new HashMap<>();
		
		for(int i = 0; i < 20 - ceilingNumBasesMutatedPerMutation; i++) {
			Map<Character, Integer> occurrences = new HashMap<>();
			occurrences.put('a', 0);
			occurrences.put('c', 0);
			occurrences.put('g', 0);
			occurrences.put('t', 0);
			patternTracker.add(occurrences);			
		}
		for(int i = 0; i < ceilingNumBasesMutatedPerMutation; i++) {
			actionTracker.put('a', 0);
			actionTracker.put('c', 0);
			actionTracker.put('g', 0);
			actionTracker.put('t', 0);
		}
		
		if(ceilingNumBasesMutatedPerMutation == 1) {
			Mutagen mutagen = new Mutagen();
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
        		    		actionTracker.put(mutatedGenomeBase, actionTracker.get(mutatedGenomeBase) + 1);
    					baseToIncrement = genomeBase;
    				}
					occurrences.put(baseToIncrement, occurrences.get(baseToIncrement) + 1);
    			}
    		}
        	System.out.println("Action tracker: " + actionTracker);
        	System.out.println("Pattern tracker: " + patternTracker);
        	return mutagen;
    	}    	
		else if(ceilingNumBasesMutatedPerMutation == floorNumBasesMutatedPerMutation)
    		return getExactMutagen(genome, mutatedGenome, ceilingNumBasesMutatedPerMutation, windows);
    	else
    		return getInexactMutagen(genome, mutatedGenome, ceilingNumBasesMutatedPerMutation, windows);
    }
    
	private Mutagen getExactMutagen(String genome, String mutatedGenome, int numBasesMutatedPerMutation, List<Window> windows) {
		Mutagen mutagen = new Mutagen();
		return mutagen;
	}

	private Mutagen getInexactMutagen(String genome, String mutatedGenome, int numBasesMutatedPerMutation, List<Window> windows) {
		Mutagen mutagen = new Mutagen();
		return mutagen;
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