package mutation.g3;

import java.util.Objects;

/**
 *
 * @author group3
 */
public class Mutation {

    private final String original;
    private final String mutated;

    public Mutation(String original, String mutated) {
        this.original = original;
        this.mutated = mutated;
    }

    public String getOriginal() {
        return original;
    }

    public String getMutated() {
        return mutated;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.original);
        hash = 11 * hash + Objects.hashCode(this.mutated);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mutation other = (Mutation) obj;
        if (!Objects.equals(this.original, other.original)) {
            return false;
        }
        if (!Objects.equals(this.mutated, other.mutated)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return original + "->" + mutated;
    }

}
