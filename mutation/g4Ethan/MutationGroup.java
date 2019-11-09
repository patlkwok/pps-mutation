package mutation.g4Ethan;

import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;
import mutation.sim.Console;
import mutation.sim.Mutagen;
import javafx.util.Pair;

public class MutationGroup {

	private static final int MAX_RULE_LENGTH = 10;
	private static final String ANY = "acgt";
	private List<Map<Base, Integer>> listMapBefore = new ArrayList<>();
	private List<Map<Base, Integer>> listMapAfter = new ArrayList<>();
	private List<Map<Pair<Base, Base>, Integer>> listMapDelta = new ArrayList<>();
	private Set<Mutation> mutations = new HashSet<>();

	public MutationGroup() {
		initializeMap();
	}

	public int getNumMutations() {
		return mutations.size();
	}

	private void initializeMap() {
		HashMap<Base, Integer> baseCountMap = new HashMap<>();
		HashMap<Pair<Base, Base>, Integer> basePairCountMap = new HashMap<>();
		for (Base b1 : Base.values()) {
			baseCountMap.put(b1, 0);
			for (Base b2 : Base.values()) {
				basePairCountMap.put(new Pair<Base, Base>(b1, b2), 0);
			}
		}
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			listMapBefore.add(new HashMap<>(baseCountMap));
			listMapAfter.add(new HashMap<>(baseCountMap));
			listMapDelta.add(new HashMap<>(basePairCountMap));
		}
	}

	public Set<Mutation> getMutations() {
		return mutations;
	}

	public void addAll(Set<Mutation> mutations) {
		for (Mutation mutation : mutations) {
			add(mutation);
		}
	}

	public void add(Mutation mutation) {
		mutations.add(mutation);
		String before = mutation.getBefore();
		String after = mutation.getAfter();
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			Base baseBefore = getBase(before.charAt(i));
			Base baseAfter = getBase(after.charAt(i));
			Map<Base, Integer> mapBefore = listMapBefore.get(i);
			Map<Base, Integer> mapAfter = listMapAfter.get(i);
			Pair<Base, Base> baseDelta = new Pair<>(baseBefore, baseAfter);
			Map<Pair<Base, Base>, Integer> mapDelta = listMapDelta.get(i);
			mapBefore.put(baseBefore, mapBefore.get(baseBefore)+1);
			mapAfter.put(baseAfter, mapAfter.get(baseAfter)+1);
			mapDelta.put(baseDelta, mapDelta.get(baseDelta)+1);
		}
	}

	public String getPattern() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			Map<Base, Integer> mapBefore = listMapBefore.get(i);
			List<Base> bases = new ArrayList<>();
			for (Base base : mapBefore.keySet()) {
				double frequency = ((double) mapBefore.get(base))/mutations.size();
				if (frequency > 0.05) {
					bases.add(base);
				}
			}
			Collections.sort(bases);
			for (Base base : bases) {
				result.append(base.getChar());
			}
			if (i != MAX_RULE_LENGTH - 1) {
				result.append(";");
			}
		}
		return result.toString();
	}

	public String getAction() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			Map<Pair<Base, Base>, Integer> mapDelta = listMapDelta.get(i);
			int countStayedTheSame = 0;
			for (Base base : Base.values()) {
				countStayedTheSame += mapDelta.get(new Pair<Base, Base>(base, base));
			}
			Base bestAction = null;
			int bestCount = countStayedTheSame;
			for (Base after : Base.values()) {
				int count = 0;
				for (Base before : Base.values()) {
					if (before != after) {
						count += mapDelta.get(new Pair<Base, Base>(before, after));
					}
				}
				if (count > bestCount) {
					bestAction = after;
					bestCount = count;
				}
			}
			if (bestAction == null) {
				result.append(""+i);
			} else {
				result.append(bestAction.getChar());
			}
		}
		return result.toString();
	}

	public Pair<String, String> getRule() {
		StringBuilder resultPattern = new StringBuilder();
		StringBuilder resultAction = new StringBuilder();
		String pattern = getPattern();
		String action = getAction();
		String[] patternArray = pattern.split(";");
		int start = 0;
		while (ANY.equals(patternArray[start]) && (action.charAt(start) == ((char) (start+'0')))) {
			start++;
		}
		int actionEnd = MAX_RULE_LENGTH-1;
		int patternEnd = MAX_RULE_LENGTH-1;
		while (ANY.equals(patternArray[patternEnd]) && (action.charAt(actionEnd) == ((char) (actionEnd+'0')))) {
			actionEnd--;
			patternEnd--;
		}
		System.out.println("start: " + start);
		System.out.println("patternEnd: " + patternEnd);
		while (ANY.equals(patternArray[patternEnd]) && patternEnd > start) {
			patternEnd--;
		}
		if (patternEnd < start) {
			throw new RuntimeException("Something is wrong generating the rule");
		} else {
			for (int i = start; i <= patternEnd; i++) {
				resultPattern.append(patternArray[i]);
				if (i != patternEnd) resultPattern.append(";");
			}
			for (int i = start; i <= actionEnd; i++) {
				if (Character.isDigit(action.charAt(i))) {
					int value = Character.getNumericValue(action.charAt(i));
					resultAction.append(Character.forDigit(value-start, 10));
				} else {
					resultAction.append(action.charAt(i));
				}
			}
			return new Pair<String, String>(resultPattern.toString(), resultAction.toString());
		}
	}

	public double similarity(Mutation mutation) {
		String before = mutation.getBefore();
		String after = mutation.getAfter();
		double similarityBefore = similarityBefore(before);
		double similarityAfter = similarityAfter(after);
		double similarityDelta = 4 * similarityDelta(before, after);
		return similarityBefore + similarityAfter + similarityDelta;
	}

	private double similarityBefore(String before) {
		double similarity = 0.0;
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			Base base = getBase(before.charAt(i));
			Integer count = listMapBefore.get(i).get(base);
			similarity += ((double) count)/mutations.size();
		}
		return similarity;
	}

	private double similarityAfter(String after) {
		double similarity = 0.0;
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			Base base = getBase(after.charAt(i));
			Integer count = listMapAfter.get(i).get(base);
			similarity += ((double) count)/mutations.size();
		}
		return similarity;
	}

	private double similarityDelta(String before, String after) {
		double similarity = 0.0;
		for (int i = 0; i < MAX_RULE_LENGTH; i++) {
			Base baseBefore = getBase(before.charAt(i));
			Base baseAfter = getBase(after.charAt(i));
			if (baseBefore != baseAfter) {
				Integer count = listMapDelta.get(i).get(new Pair<Base, Base>(baseBefore, baseAfter));
				similarity += ((double) count)/mutations.size();
			}
		}
		return similarity;
	}

	private Base getBase(char c) {
		return Base.valueOf("" + Character.toUpperCase(c));
	}

}