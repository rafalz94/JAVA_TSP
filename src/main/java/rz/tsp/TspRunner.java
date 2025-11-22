package rz.tsp;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.Optimize;
import io.jenetics.util.ISeq;

@Component
public class TspRunner implements CommandLineRunner {

    private static final int NUM_CITIES = 20;
    private static final int POP_SIZE = 500;
    private static final int MAX_GENERATIONS = 500;
    private static final double CROSSOVER_PROB = 0.7;
    private static final double MUTATION_PROB = 0.2;
    private static final int TOURNAMENT_SIZE = 3;

    private record City(double x, double y) {
        double distance(City other) {
            final double dx = this.x - other.x;
            final double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    private final List<City> cities = new ArrayList<>();

    private double calculateDistance(final ISeq<Integer> route) {
        double distance = 0.0;
        for (int i = 0; i < route.size(); ++i) {
            final City start = cities.get(route.get(i));
            final City end = cities.get(route.get((i + 1) % route.size()));
            distance += start.distance(end);
        }
        return distance;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Generowanie " + NUM_CITIES + " losowych miast...");

        final long seed = System.currentTimeMillis();
        System.out.println("Random Seed: " + seed);
        final RandomGenerator random = new java.util.Random(seed);

        for (int i = 0; i < NUM_CITIES; ++i) {
            cities.add(new City(
                    random.nextDouble(100.0),
                    random.nextDouble(100.0)));
        }
        cities.forEach(System.out::println);

        final Codec<ISeq<Integer>, EnumGene<Integer>> CODEC = Codec.of(
                Genotype.of(PermutationChromosome.ofInteger(NUM_CITIES)),
                genotype -> genotype.chromosome().stream()
                        .map(EnumGene::allele)
                        .collect(ISeq.toISeq()));

        final Engine<EnumGene<Integer>, Double> engine = Engine
                .builder(this::calculateDistance, CODEC)
                .populationSize(POP_SIZE)
                .optimize(Optimize.MINIMUM)
                .selector(new TournamentSelector<>(TOURNAMENT_SIZE))
                .alterers(
                        new PartiallyMatchedCrossover<>(CROSSOVER_PROB),
                        new SwapMutator<>(MUTATION_PROB))
                .build();

        final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

        System.out.println("\nUruchamianie ewolucji...");

        final Phenotype<EnumGene<Integer>, Double> bestResult = engine.stream()
                .limit(MAX_GENERATIONS)
                .peek(statistics)
                .peek(r -> {
                    if (r.generation() % 50 == 0) {
                        System.out.printf(
                                "Generacja: %d, Najlepszy dystans: %.4f%n",
                                r.generation(),
                                r.bestFitness());
                    }
                })
                .collect(EvolutionResult.toBestPhenotype());

        System.out.println("\nEwolucja zakończona.");
        System.out.println("Statystyki:");
        System.out.println(statistics);
        System.out.println("\nNajlepsze znalezione rozwiązanie:");
        System.out.printf("Dystans: %.4f%n", bestResult.fitness());
        System.out.println("Trasa (kolejność miast):");

        System.out.println(bestResult.genotype().chromosome().stream()
                .map(EnumGene::allele)
                .map(Object::toString)
                .collect(Collectors.joining(" -> ")));
    }
}
