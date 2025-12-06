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

        System.out.println("\nUruchamianie ewolucji (10 uruchomień)...");

        Phenotype<EnumGene<Integer>, Double> globalBest = null;
        List<List<Double>> allRunsHistory = new ArrayList<>();
        int NUM_RUNS = 10;

        for (int i = 0; i < NUM_RUNS; i++) {
            List<Double> currentRunHistory = new ArrayList<>();
            final Phenotype<EnumGene<Integer>, Double> result = engine.stream()
                    .limit(MAX_GENERATIONS)
                    .peek(r -> currentRunHistory.add(r.bestFitness()))
                    .collect(EvolutionResult.toBestPhenotype());

            allRunsHistory.add(currentRunHistory);

            if (globalBest == null || result.fitness() < globalBest.fitness()) {
                globalBest = result;
            }
            System.out.printf("Uruchomienie %d: Najlepszy dystans: %.4f%n", i + 1, result.fitness());
        }

        System.out.println("\nEwolucja zakończona.");
        if (globalBest != null) {
            System.out.println("\nNajlepsze znalezione rozwiązanie (globalnie):");
            System.out.printf("Dystans: %.4f%n", globalBest.fitness());
            System.out.println("Trasa (kolejność miast):");

            System.out.println(globalBest.genotype().chromosome().stream()
                    .map(EnumGene::allele)
                    .map(Object::toString)
                    .collect(Collectors.joining(" -> ")));

            saveResults(cities, globalBest.genotype().chromosome().stream()
                    .map(EnumGene::allele)
                    .collect(ISeq.toISeq()), globalBest.fitness(), allRunsHistory);
        } else {
            System.out.println("Nie znaleziono rozwiązania.");
        }
    }

    private void saveResults(List<City> cities, ISeq<Integer> route, double distance, List<List<Double>> history) {
        java.io.File file = new java.io.File("target/tsp_result.json");
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"distance\": ").append(String.format(java.util.Locale.US, "%.4f", distance))
                        .append(",\n");

                json.append("  \"cities\": [\n");
                for (int i = 0; i < cities.size(); i++) {
                    City c = cities.get(i);
                    json.append(String.format(java.util.Locale.US, "    {\"x\": %.4f, \"y\": %.4f}", c.x(), c.y()));
                    if (i < cities.size() - 1)
                        json.append(",");
                    json.append("\n");
                }
                json.append("  ],\n");

                json.append("  \"route\": [");
                for (int i = 0; i < route.size(); i++) {
                    json.append(route.get(i));
                    if (i < route.size() - 1)
                        json.append(", ");
                }
                json.append("],\n");

                json.append("  \"history\": [\n");
                for (int i = 0; i < history.size(); i++) {
                    json.append("    [");
                    List<Double> run = history.get(i);
                    for (int j = 0; j < run.size(); j++) {
                        json.append(String.format(java.util.Locale.US, "%.4f", run.get(j)));
                        if (j < run.size() - 1)
                            json.append(", ");
                    }
                    json.append("]");
                    if (i < history.size() - 1)
                        json.append(",");
                    json.append("\n");
                }
                json.append("  ]\n");

                json.append("}");

                writer.write(json.toString());
                System.out.println("\nWyniki zapisano do pliku " + file.getPath());
            }
        } catch (java.io.IOException e) {
            System.err.println("Błąd podczas zapisywania wyników: " + e.getMessage());
        }
    }
}
