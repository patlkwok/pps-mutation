package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Rule {

	private Pattern pattern;
	private Action action;

	public Rule(String pattern, String action) {
		this.pattern = new Pattern(pattern);
		this.action = new Action(action);
	}

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

	public boolean explains(Mutation mutation) {
		return pattern.explains(mutation.getBefore()) && action.explains(mutation.getAfter());
	}

	@Override
	public String toString() {
		return "Pattern: " + pattern.toString() + "\nAction: " + action.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Rule)) return false;
		Rule rule = (Rule) object;
		return this.pattern.toString().equals(rule.pattern.toString()) && this.action.toString().equals(rule.action.toString());
	}

	@Override 
	public int hashCode() {
		int result = 19;
		result = 43*result + this.pattern.hashCode();
		result = 43*result + this.action.hashCode();
		return result;
	}

}