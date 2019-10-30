package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Pattern {

	private List<List<Base>> pattern = new ArrayList<>();

	public Pattern(String pattern) {
		for (char c : pattern.toCharArray()) {
			List<Base> base = new ArrayList<>();
			base.add(Base.valueOf(""+Character.toUpperCase(c)));
			this.pattern.add(base);
		}
	}

	public Pattern(List<List<Base>> pattern) {
		this.pattern = pattern;
	}

	public boolean explains(String before) {
		if (before.length() != pattern.size()) return false;
		for (int i = 1; i < before.length(); i++) {
			if (!pattern.get(i).contains(Base.valueOf(""+Character.toUpperCase(before.charAt(i))))) return false;
		}
		return true;
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

	// @Override
	// public boolean equals(Object object) {
	// 	if (object == null) return false;
	// 	if (!(object instanceof Pattern)) return false;
	// 	Pattern pattern = (Pattern) object;
	// 	return pattern.toString().equals(this.pattern.toString());
	// }
	// // 	if (object == null) return false;
	// // 	if (!(object instanceof Pattern)) return false;
	// // 	Pattern pattern = (Pattern) object;
	// // 	if (this.pattern.size() != pattern.pattern.size()) return false;
	// // 	for (int i = 0; i < this.pattern.size(); i++) {
	// // 		Set<Base> bases = new HashSet<>();
	// // 		bases.addAll(this.pattern.get(i));
	// // 		for (Base base : pattern.pattern.get(i)) {
	// // 			bases.remove(base);
	// // 		}
	// // 		if (bases.size() != 0) return false;
	// // 	}
	// // 	return true;
	// // }

	// @Override
	// public int hashcode() {
	// 	return this.toString().hashcode();
	// }

}