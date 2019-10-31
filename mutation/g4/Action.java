package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Action {

	private List<Reference> action = new ArrayList<>();

	public Action(String action) {
		for (char c : action.toCharArray()) {
			this.action.add(Reference.valueOf(""+Character.toUpperCase(c)));
		}
	}

	public Action(List<Reference> action) {
		this.action = action;
	}

	public boolean explains(String after) {
		return true;
	}

	@Override
	public String toString() {
		char[] charArray = new char[action.size()];
		for (int i = 0; i < action.size(); i++) {
			charArray[i] = new String(""+action.get(i)).charAt(0);
		}
		return new String(charArray);
	}
}
