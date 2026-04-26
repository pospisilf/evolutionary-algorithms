package ea.operator.selection;

import ea.api.SelectionStrategy;
import ea.model.Individual;
import ea.model.Population;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TournamentSelection<T> implements SelectionStrategy<T> {

    private final int tournamentSize;
    private final Random random;

    public TournamentSelection(int tournamentSize) {
        this(tournamentSize, new Random());
    }

    public TournamentSelection(int tournamentSize, Random random) {
        this.tournamentSize = tournamentSize;
        this.random = random;
    }

    @Override
    public List<Individual<T>> select(Population<T> population, int count) {
        List<Individual<T>> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(runTournament(population.individuals()));
        }
        return result;
    }

    private Individual<T> runTournament(List<Individual<T>> individuals) {
        Individual<T> best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual<T> candidate = individuals.get(random.nextInt(individuals.size()));
            if (best == null || candidate.fitness() > best.fitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
