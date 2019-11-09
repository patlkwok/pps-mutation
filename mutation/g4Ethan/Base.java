package mutation.g4Ethan;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public enum Base {

	A('a'), C('c'), G('g'), T('t');
	private char c;

	private Base(char c) {
		this.c = c;
	}

	public char getChar() {
		return c;
	}

}