package mutation.g4;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public enum Reference {

	ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), A(11), C(12), G(13), T(14);
	private int i;

	private Reference(int i) {
		this.i = i;
	}

	public int getI() {
		return i;
	}

}