package ea.operator.termination;

import ea.api.TerminationCondition;
import ea.model.GenerationResult;

public class MaxGenerations<T> implements TerminationCondition<T> {

    private final int maxGenerations;

    public MaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    @Override
    public boolean shouldStop(GenerationResult<T> result) {
        return result.generation() >= maxGenerations;
    }
}
