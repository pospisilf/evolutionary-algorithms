package ea.operator.termination;

import ea.api.TerminationCondition;
import ea.model.GenerationResult;

public class FitnessThreshold<T> implements TerminationCondition<T> {

    private final double threshold;

    public FitnessThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean shouldStop(GenerationResult<T> result) {
        return result.bestFitness() >= threshold;
    }
}
