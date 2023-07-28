package main;

import help.ProgressPrinter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static help.ArrayHelp.findLargestK;
import static help.ArrayHelp.swap;
import static help.ArrayPrinter.printFirstAndLastElements;
import static help.MathHelp.nChooseK_double;
import static help.RNG.binomialK;
import static help.RNG.powerlawK;
import static help.RNG.randomBoolean;
import static help.RNG.randomDouble;
import static help.RNG.randomInt;

public class PartitionSolver {


    private static Solution solve(long[] values, long maxSteps, KGenerator generator) {
        Solution sol = createStartingPoint(values);
        return solve(values, maxSteps, generator, sol);
    }

    private static Solution solve(long[] values, long maxSteps, KGenerator generator, Solution sol) {
        while (sol.isNotOptimal(maxSteps)) {
            int k = generator.generateK(values.length);
            Set<Integer> indexes = randomKIndices(k, values);
            sol.updateIfImprovementMultiple(indexes);
        }
        return sol;
    }

    private static Solution solveWithProgress(long[] values, long maxSteps, KGenerator generator) {
        Solution sol = createStartingPoint(values);
        long steps = 0;
        ProgressPrinter progress = new ProgressPrinter(maxSteps);
        while (sol.isNotOptimal(maxSteps)) {
            int k = generator.generateK(values.length);
            Set<Integer> indexes = randomKIndices(k, values);
            sol.updateIfImprovementMultiple(indexes);
            progress.printProgressIfNecessary(steps);
            ++steps;
        }
        return sol;
    }

    public static Solution solveRLS(long[] values, long maxSteps) {
        return solveRLS(values, maxSteps, false);
    }

    public static Solution solveRLS(long[] values, long maxSteps, boolean withProgress) {
        KGenerator generator = a -> 1;
        if (withProgress) {
            return solveWithProgress(values, maxSteps, generator);
        } else {
            return solve(values, maxSteps, generator);
        }
    }

    public static Solution solveRLS(long[] values, long maxSteps, Solution sol) {
        return solve(values, maxSteps, a -> 1, sol);
    }

    public static Solution solveRLS_UniformRing(long[] values, long maxSteps, int neighbours, boolean withProgress) {
        KGenerator generator = (a) -> randomInt(1, neighbours + 1);
        if (withProgress) {
            return solveWithProgress(values, maxSteps, generator);
        } else {
            return solve(values, maxSteps, generator);
        }

    }

    public static Solution solveRLS_UniformNeighbour(long[] values, long maxSteps, int neighbours) {
        Solution sol = createStartingPoint(values);
        return solveRLS_UniformNeighbour(values, maxSteps, neighbours, sol);
    }

    public static Solution solveRLS_UniformNeighbour(long[] values, long maxSteps, int neighbours, boolean withProgress) {
        if (neighbours == 1) {
            return solveRLS(values, maxSteps, withProgress);
        }
        UniformNeighbourGenerator generator = new UniformNeighbourGenerator(neighbours, values.length);
        if (withProgress) {
            return solveWithProgress(values, maxSteps, generator);
        }
        return solve(values, maxSteps, generator);
    }

    public static Solution solveRLS_UniformNeighbour(long[] values, long maxSteps, int neighbours, Solution sol) {
        if (neighbours == 1) {
            return solveRLS(values, maxSteps, sol);
        }
        UniformNeighbourGenerator generator = new UniformNeighbourGenerator(neighbours, values.length);
        return solve(values, maxSteps, generator, sol);
    }

    public static Solution solveEA(long[] values, long maxSteps) {
        return solveEA(values, maxSteps, 1.0 / values.length);
    }

    public static Solution solveEA(long[] values, long maxSteps, double mutationRate) {
        if (mutationRate > values.length || mutationRate <= 0) {
            throw new IllegalArgumentException("Illegal mutation rate");
        }
        double maxRate = 0.5;
        if (mutationRate >= maxRate) {
            mutationRate = maxRate;
        }
        double finalMutationRate = mutationRate;
        return solve(values, maxSteps, (a) -> binomialK(a, finalMutationRate));
    }

    public static Solution solveEA(long[] values, long maxSteps, double mutationRate, Solution sol) {
        if (mutationRate > values.length || mutationRate <= 0) {
            throw new IllegalArgumentException("Illegal mutation rate");
        }
        return solve(values, maxSteps, (a) -> binomialK(a, mutationRate), sol);
    }

    public static Solution solveFmut(long[] values, long maxSteps, double p) {
        return solve(values, maxSteps, (a) -> randomDouble() <= p ? 1 : randomInt(1, a / 2));
    }

    public static Solution solvePmut(long[] values, long maxSteps, double n) {
        return solve(values, maxSteps, (a) -> powerlawK(1.0, a / 2.0, n));
    }

    public static Solution solveWithFirstK(long[] values, long maxSteps, int k) {
        int[] largestK = findLargestK(values, k);
        // swap values so that largest k are in front
        for (int i = 0; i < k; ++i) {
            swap(values, i, largestK[i]);
        }
        List<Solution> solutions = createStartingPoints(values, (int) Math.pow(2, k - 1));

        // adjust permutation of highest k elements
        for (int i = 0; i < Math.pow(2, k - 1); ++i) {
            Solution sol = solutions.get(i);
            for (int j = 0; j < k; ++j) {
                int bit = (i >> j) & 1;
                if (sol.get(j) != bit) {
                    sol.update(j);
                }
            }
        }
        Solution ret = solveWithProgress(solutions, maxSteps, values, k, values.length, false);

        // restore original sorting of the array
        for (int i = k - 1; i >= 0; --i) {
            swap(values, i, largestK[i]);
        }
        ret.removeSwapping(largestK);

        // print and return result
        ret.printResult();
        return ret;
    }

    public static Solution solvePopulation(long[] values, long maxSteps, int populationSize) {
        List<Solution> solutions = createStartingPoints(values, populationSize);
        return solvePopulation(solutions, maxSteps, values);
    }

    private static Solution solvePopulation(List<Solution> solutions, long maxSteps, long[] values) {
        return solveWithProgress(solutions, maxSteps, values, 0, values.length, true);
    }

    private static Solution solveWithProgress(List<Solution> solutions, long maxSteps, long[] values,
                                              int start, int end, boolean print) {
        long count = 0;
        boolean notDone = true;
        System.out.print("Input: ");
        printFirstAndLastElements(values);
        for (Solution s : solutions) {
            System.out.print("Initial distribution: ");
            s.printDistribution();
        }
        System.out.print("Progress:  0 %                    ");
        ProgressPrinter progress = new ProgressPrinter(maxSteps);
        do {
            for (Solution sol : solutions) {
                do {
                    sol.updateIfImprovement(randomInt(start, end));
                    ++count;
                    notDone = notDone && sol.isNotOptimal() && count < maxSteps;
                } while (sol.lastImproveLessThanXStepsAway(100) && notDone);
            }
            progress.printProgressIfNecessary(count);
        }
        while (notDone);
        System.out.println();
        Optional<Solution> solution = solutions.stream().min(Comparator.comparing(Solution::getDif));
        if (solution.isEmpty()) {
            throw new Error();
        }
        if (print) {
            solution.get().printResult(maxSteps, count);
        } else {
            solution.get().printTotalStats(maxSteps, count);
        }
        return solution.get();
    }

    private static Solution createStartingPoint(long[] values) {
        BigInteger inputSum = BigInteger.ZERO;
        SolutionBigInt sol = new SolutionBigInt(values);
        for (int i = 0; i < values.length; ++i) {
            inputSum = inputSum.add(BigInteger.valueOf(values[i]));
            if (randomBoolean()) {
                sol.setValueToOne(i);
            }
        }
        sol.setInputSum(inputSum);
        if (inputSum.bitLength() <= 64) {
            return new SolutionLong(sol, values);
        }
        return sol;
    }

    private static List<Solution> createStartingPoints(long[] values, int popCount) {
        System.out.println("Generating initial population");
        BigInteger inputSum = BigInteger.ZERO;
        List<SolutionBigInt> solutions = new ArrayList<>(popCount);
        for (int i = 0; i < popCount; ++i) {
            solutions.add(new SolutionBigInt(values));
        }
        for (int i = 0; i < values.length; ++i) {
            inputSum = inputSum.add(BigInteger.valueOf(values[i]));
            for (Solution sol : solutions) {
                if (randomBoolean()) {
                    sol.setValueToOne(i);
                }
            }
        }
        List<Solution> ret = new ArrayList<>();
        for (SolutionBigInt sol : solutions) {
            sol.setInputSum(inputSum);
            if (inputSum.bitLength() <= 64) {
                ret.add(new SolutionLong(sol, values));
            } else {
                ret.add(sol);
            }
        }
        return ret;
    }

    private static Set<Integer> randomKIndices(int k, long[] values) {
        return randomKIndices(k, 0, values.length);
    }

    private static Set<Integer> randomKIndices(int k, int start, int end) {
        Set<Integer> set = new HashSet<>(k);
        while (set.size() < k) {
            set.add(randomInt(start, end));
        }
        return set;
    }

    private interface KGenerator {
        int generateK(int max);
    }

    private static class UniformNeighbourGenerator implements KGenerator {

        final double[] stepSizes;
        private final int neighbours;

        private UniformNeighbourGenerator(int k, int length) {
            neighbours = k;
            stepSizes = new double[neighbours];
            stepSizes[0] = length;
            for (int i = 1; i < stepSizes.length; ++i) {
                stepSizes[i] = nChooseK_double(length, i + 1) + stepSizes[i - 1];
            }
            for (int i = 0; i < stepSizes.length; ++i) {
                stepSizes[i] = stepSizes[i] / stepSizes[stepSizes.length - 1];
            }
        }

        @Override
        public int generateK(int max) {
            if (neighbours == 1) {
                return 1;
            } else {
                double r = randomDouble();
                int k = 0;
                while (k < stepSizes.length && stepSizes[k] < r) {
                    ++k;
                }
                return k + 1;
            }
        }
    }
}