package mutation.g3;

import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author group3
 */
public class ChangeSummary {

    private HashSet<Mutation> mutation;
    private ChangeBasedDistribution distribution;
    private int count;

    public ChangeSummary(HashSet<Mutation> mutation) {
        this(mutation, null);
    }

    public ChangeSummary(HashSet<Mutation> mutation, ChangeBasedDistribution distribution) {
        this.mutation = mutation;
        this.distribution = distribution;
        this.count = 1;
    }

    public HashSet<Mutation> getMutation() {
        return mutation;
    }

    public void setMutation(HashSet<Mutation> mutation) {
        this.mutation = mutation;
    }

    public ChangeBasedDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(ChangeBasedDistribution distribution) {
        this.distribution = distribution;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    public void increaseCount() {
        count++;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.mutation);
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
        final ChangeSummary other = (ChangeSummary) obj;
        return Objects.equals(this.mutation, other.mutation);
    }

    @Override
    public String toString() {
        return mutation + "," + count;
    }
}
