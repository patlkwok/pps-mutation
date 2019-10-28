package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Rule {

	private Pattern pattern;
	private Action action;

	public Rule(Pattern pattern, Action action) {
		this.pattern = pattern;
		this.action = action;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public Action getAction() {
		return action;
	}

}