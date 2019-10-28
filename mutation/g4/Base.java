package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public enum Base {

	A('a'), C('c'), G('g'), T('t');
	private char c;

	private Base(char c) {
		this.c = c;
	}

}