package ea.api;

import ea.model.GenerationResult;

public interface TerminationCondition<T> {
    boolean shouldStop(GenerationResult<T> result);
}
