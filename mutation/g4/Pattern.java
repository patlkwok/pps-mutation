package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Pattern {

	private List<List<Base>> pattern = new ArrayList<>();
	private double credit = 0.0;
	private List<Mutation> mutations = new ArrayList<>();
	private final int maxRuleLength = 10;
	private final double threshhold = 0.25;

	public Pattern(String pattern) {
		String[] patternOptions = pattern.split(";");
		for (String patternOption : patternOptions) {
			List<Base> base = new ArrayList<>();
			for (char c : patternOption.toCharArray()) {
				base.add(Base.valueOf(""+Character.toUpperCase(c)));
			}
			this.pattern.add(base);
		}
	}

	public Pattern(List<List<Base>> pattern) {
		this.pattern = pattern;
	}

	public void addEvidence(Mutation mutation) {
		String before = mutation.getBefore();
		for (int i = 0; i < maxRuleLength; i++) {
			List<Base> patternBases = pattern.get(i);
			Base base = Base.valueOf(""+Character.toUpperCase(before.charAt(i)));
			if (!patternBases.contains(base)) {
				patternBases.add(base);
				Collections.sort(patternBases);
			}
		}
		mutations.add(mutation);
	}

	public int getEvidenceSize() {
		return mutations.size();
	}

	public boolean explains(String before) {
		if (before.length() != pattern.size()) return false;
		for (int i = 1; i < before.length(); i++) {
			if (!pattern.get(i).contains(Base.valueOf(""+Character.toUpperCase(before.charAt(i))))) return false;
		}
		return true;
	}

	public void recalibrate() {
		for (int i = 0; i < maxRuleLength; i++) {
			List<Base> patternBases = pattern.get(i);
			Map<Base, Integer> seenBases = new HashMap<>();
			seenBases.put(Base.A, 0);
			seenBases.put(Base.C, 0);
			seenBases.put(Base.G, 0);
			seenBases.put(Base.T, 0);
			for (int j = 0; j < mutations.size(); j++) {
				Mutation mutation = mutations.get(j);
				Base base = Base.valueOf("" + Character.toUpperCase(mutation.getBefore().charAt(i)));
				seenBases.put(base, seenBases.get(base)+1);
			}
			Set<Base> patternBasesToAdd = new HashSet<>();
			// for (Base base : seenBases.keySet()) {	
			// 	if (!patternBases.contains(base)) {
			// 		Integer count = seenBases.get(base);
			// 		double probability = ((double) count)/mutations.size();
			// 		if (Math.random() < probability) {
			// 			patternBasesToAdd.add(base);
			// 		}
			// 	}
			// }
			Set<Base> patternBasesToRemove = new HashSet<>();
			for (Base base : patternBases) {
				Integer count = seenBases.get(base);
				double frequency = ((double) count)/mutations.size();
				if (frequency < threshhold && Math.random() < threshhold-frequency) {
					patternBasesToRemove.add(base);
				}
			}
			patternBases.removeAll(patternBasesToRemove);
			patternBases.addAll(patternBasesToAdd);
			Collections.sort(patternBases);
		}
		Set<Mutation> mutationsToRemove = new HashSet<>();
		for (Mutation mutation : mutations) {
			if (!explains(mutation.getBefore()) && Math.random() < threshhold) {
				mutationsToRemove.add(mutation);
			}
		}
		mutations.removeAll(mutationsToRemove);
	}

	public void generalizeToEvidence() {
		for (Mutation mutation : mutations) {
			String before = mutation.getBefore();
			for (int i = 0; i < before.length(); i++) {
				List<Base> patternIndexBases = pattern.get(i);
				Base base = Base.valueOf(""+Character.toUpperCase(before.charAt(i)));
				if (!patternIndexBases.contains(base)) {
					patternIndexBases.add(base);
					Collections.sort(patternIndexBases);
				}
			}
		}
	}

	public void specifyToEvidence() {

	}

	// this is pretty close to Jaccard score!
	public double similarityScore(String before) {
		double score = 0.0;
		if (before.length() != pattern.size()) return 0.0;
		for (int i = 0; i < before.length(); i++) {
			List<Base> patternOptions = pattern.get(i);
			if (patternOptions.contains(Base.valueOf(""+Character.toUpperCase(before.charAt(i))))) {
				score += 1.0/patternOptions.size();
			} else {
				score -= 1.0/(4.0-patternOptions.size());
			}
		}
		return score;
	}

	@Override
	public String toString() {
		String result = "";
		char[] charArray;
		for (int i = 0; i < pattern.size(); i++) {
			charArray = new char[pattern.get(i).size()];
			for (int j = 0; j < pattern.get(i).size(); j++) {
				charArray[j] = new String(pattern.get(i).get(j).name()).charAt(0);
			}
			result += new String(charArray);
			if (i < pattern.size()-1) result += ";";
		}
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Pattern)) return false;
		Pattern pattern = (Pattern) object;
		return pattern.toString().equals(this.pattern.toString());
	}

	@Override
	public int hashCode() {
		String toString = this.toString();
		char[] charArray = toString.toCharArray();
		int result = 17;
		for (int i = 0; i < charArray.length; i++) {
			result += 31*Character.valueOf(charArray[i]).hashCode();
		}
		return result;
	}

}