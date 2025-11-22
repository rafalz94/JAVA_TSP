# Traveling Salesperson Problem (TSP) Solver

This is a Spring Boot application that solves the **Traveling Salesperson Problem (TSP)** using a **Genetic Algorithm** provided by the [Jenetics](https://jenetics.io/) library.

## Overview

The application generates a set of random cities and evolves a population of routes to find the shortest possible path that visits every city exactly once and returns to the start.

- **Algorithm**: Genetic Algorithm (GA)
- **Optimization Goal**: Minimize total Euclidean distance
- **Population Size**: 500
- **Generations**: 500

## Tech Stack

- **Java**: 21
- **Spring Boot**: 3.5.7
- **Jenetics**: 8.0.0 (Evolutionary Algorithm Library)

## Key Features

- **Minimization**: Correctly configured to find the shortest route.
- **Reproducibility**: Logs the random seed used for city generation, allowing for reproducible runs.
- **Visualization**: Prints the evolution progress and the final best route to the console.

## How to Run

### Prerequisites
- Java 21 or higher installed.

### Build and Run
You can run the application directly using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or build the JAR and run it:

```bash
./mvnw package -DskipTests
java -jar target/TSP-0.0.1-SNAPSHOT.jar
```

## Example Output

```text
Generowanie 20 losowych miast...
Random Seed: 1763850708497
...
Uruchamianie ewolucji...
Generacja: 50, Najlepszy dystans: 553.3097
...
Generacja: 500, Najlepszy dystans: 496.4761

Ewolucja zakończona.
Najlepsze znalezione rozwiązanie:
Dystans: 493.5507
Trasa (kolejność miast):
2 -> 13 -> 15 -> 14 -> 6 -> 10 -> 11 -> 1 -> 3 -> 17 -> 12 -> 9 -> 16 -> 19 -> 0 -> 7 -> 4 -> 18 -> 5 -> 8
```
