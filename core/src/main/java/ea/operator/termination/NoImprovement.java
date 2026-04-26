package ea.operator.termination;

import ea.api.TerminationCondition;
import ea.model.GenerationResult;

public class NoImprovement<T> implements TerminationCondition<T> {

    private final int patience;
    private double bestSeen = Double.NEGATIVE_INFINITY;
    private int count = 0;

    public NoImprovement(int patience) {
        this.patience = patience;
    }

    @Override
    public boolean shouldStop(GenerationResult<T> result) {
        if (result.bestFitness() > bestSeen) {
            bestSeen = result.bestFitness();
            count = 0;
        } else {
            count++;
        }
        return count >= patience;
    }
}
