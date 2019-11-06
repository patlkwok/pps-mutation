package mutation.g3;

/**
 *
 * @author group3
 */
public class ExperimentResult {

    final Mutation mutation;
    final int expectedMutations;
    final int appliedMutations;

    public ExperimentResult(Mutation mutation, int expectedMutations, int appliedMutations) {
        this.mutation = mutation;
        this.expectedMutations = expectedMutations;
        this.appliedMutations = appliedMutations;
    }
}
