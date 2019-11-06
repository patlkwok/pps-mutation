package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Mutation {

	private String before;
	private String after;

	public Mutation(String before, String after) {
		this.before = before;
		this.after = after;
	}

	public String getBefore() {
		return before;
	} 

	public String getAfter() {
		return after;
	}

	@Override
	public String toString() {
		return "Before: " + before + "\n After: " + after.toString() + "\n";
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Mutation)) return false;
		Mutation objectMutation = (Mutation) object;
		return this.before.equals(objectMutation.getBefore()) && this.after.equals(objectMutation.getAfter());
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31*result + this.before.hashCode();
		result = 31*result + this.after.hashCode();
		return result;
	}

}