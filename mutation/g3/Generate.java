package mutation.g3;

import java.util.*;
import java.util.Map.Entry;
import mutation.sim.Console;
import mutation.sim.Mutagen;

/**
 *
 * @author group 3
 */
public class Player extends mutation.sim.Player {
	// Pick a random character from a string
	public static char selectAChar(String s) {
		Random random = new Random();
		int index = random.nextInt(s.length());
		return s.charAt(index);
	}

	// Generate a random string matching the pattern
	private String getMatchingPattern(String pattern, int minLength = 10) {
		String[] patternParts = pattern.split(";");
		String result = "";
		for (String p: patternParts) {
			result += selectAChar(p);
		}
		int currLength = result.length();
		for (int i = currLength; i < minLength; ++i) {
			result += selectAChar("acgt");
		}
		return result;
	}

	// Generate a random string that matches pattern1 but not pattern2 (assuming same length) 
	private String getDeltaString(String pattern1, String pattern2) {
		// TODO
		return "";
	}

	// Generate a random string of certain length
	private String getRandomString(int minLength = 10) {
		char[] pool = {'a', 'c', 'g', 't'};
		String result = "";
		for (int i = 0; i < minLength; ++i) {
			result += pool[Math.abs(random.nextInt() % 4)];
		}
		return result;
	}
	
	// Generate a random genome
	private String generateRandomGenome() {
		char[] pool = {'a', 'c', 'g', 't'};
		String result = "";
		for (int i = 0; i < 1000; ++i) {
			result += pool[Math.abs(random.nextInt() % 4)];
		}
		return result;
	}

	// Design experiment given two patterns
	private String generateTwoPatternExperiment(String pattern1, String pattern2) {
		String result = "";
		for (int i = 0; i < 25; i++) {
			result += generateMatchingPattern(pattern1, 10);
			result += generateRandomString(10);
			result += generateMatchingPattern(pattern2, 10);
			result += generateRandomString(10);
		}
		return result;
	}
	

	// Design experiment testing difference between pattern1 and pattern2
	private String generateDeltaExperiment(String pattern1, String pattern2) {
		String result = "";
		String delta1 = getDeltaString(pattern1, pattern2);
		String delta2 = getDeltaString(pattern2, pattern1);
		for (int i = 0; i < 25; i++) {
			result += generateMatchingPattern(delta1, 10);
			result += generateRandomString(10);
			result += generateMatchingPattern(delta2, 10);
			result += generateRandomString(10);
		}
		return result;
	}
	
	// Design experiments
	protected String designExperiment(int mode) {
		if (mode == 0) {
			return generateRandomGenome();
		}
		else if (mode == 1) {
			// pattern1 and pattern2 are patterns for two highest-ranked candidate rules
			return generateTwoPatternExperiment(pattern1, pattern2);
		}
		else if (mode == 2) {
			return generateDeltaExperiment(pattern1, pattern2);
		}
		else {
			// To be updated
			return generateRandomGenome();
		}
	}
}
