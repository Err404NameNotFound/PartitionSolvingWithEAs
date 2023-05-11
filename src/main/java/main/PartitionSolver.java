package main;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static help.MathHelp.binomialK;
import static help.ArrayHelp.findLargestK;
import static help.MathHelp.nChooseK_double;
import static help.Help.printFirstAndLastElements;
import static help.Help.printProgress;
import static help.ArrayHelp.swap;
import static help.MathHelp.powerlawK;

public class PartitionSolver {

    private static Random RNG = new Random();

    private static Solution solve(long[] values, long maxSteps, KGenerator generator){
        Random random = new Random();
        Solution sol = createStartingPoint(values, random);
        while (sol.isNotOptimal(maxSteps)) {
            int k = generator.generateK(values.length);
            Set<Integer> indexes = randomKIndices(k, random, values);
            sol.updateIfImprovementMultiple(indexes);
        }
        return sol;
    }
    private static Solution solveWithProgress(long[] values, long maxSteps, KGenerator generator){
        Random random = new Random();
        Solution sol = createStartingPoint(values, random);
        long stepSize = maxSteps/1000;
        long next = stepSize;
        long steps = 0;
        long startTime = Instant.now().getEpochSecond();
        while (sol.isNotOptimal(maxSteps)) {
            int k = generator.generateK(values.length);
            Set<Integer> indexes = randomKIndices(k, random, values);
            sol.updateIfImprovementMultiple(indexes);
            if(steps >= next){
                printProgress(steps, maxSteps, startTime);
                next += stepSize;
            }
            ++steps;
        }
        return sol;
    }

    public static Solution solveRLS(long[] values, long maxSteps) {
        return solve(values, maxSteps, (a) -> 1);
    }

    public static Solution solveRLS_UniformRing(long[] values, long maxSteps, int neighbours) {
        return solve(values, maxSteps, (a) -> RNG.nextInt(1, neighbours+1));
    }

    public static Solution solveRLS_UniformNeighbour(long[] values, long maxSteps, int neighbours) {
        if (neighbours == 1) {
            return solveRLS(values, maxSteps);
        }
        Random random = new Random();
        Solution sol = createStartingPoint(values, random);
        double[] stepSizes = new double[neighbours];
        stepSizes[0] = values.length;
        for (int i = 1; i < stepSizes.length; ++i) {
            stepSizes[i] = nChooseK_double(values.length, i + 1) + stepSizes[i - 1];
        }
        for (int i = 0; i < stepSizes.length; ++i) {
            stepSizes[i] = stepSizes[i] / stepSizes[stepSizes.length - 1];
        }
        while (sol.isNotOptimal(maxSteps)) {
            double r = random.nextDouble();
            int k = 0;
            while (k < stepSizes.length && stepSizes[k] < r) {
                ++k;
            }
            sol.updateIfImprovementMultiple(randomKIndices(k + 1, random, values));
        }
        return sol;
    }

    public static Solution solveEA(long[] values, long maxSteps) {
        return solveEA(values, maxSteps, 1.0 / values.length);
    }

    public static Solution solveEA(long[] values, long maxSteps, double mutationRate) {
        if (mutationRate > values.length || mutationRate <= 0) {
            throw new IllegalArgumentException("Illegal mutation rate");
        }
        return solve(values, maxSteps, (a) -> binomialK(a, mutationRate));
    }

    public static Solution solveFmut(long[] values, long maxSteps, double p) {
        return solve(values, maxSteps, (a) -> RNG.nextDouble()<= p ? 1 : RNG.nextInt(1, a / 2));
    }

    public static Solution solvePmut(long[] values, long maxSteps, double n) {
        return solve(values, maxSteps, (a) -> powerlawK(1.0, a/2.0, n));
    }

    public static Solution solveWithFirstK(long[] values, long maxSteps, int k) {
        Random random = new Random();
        int[] largestK = findLargestK(values, k);
        // swap values so that largest k are in front
        for (int i = 0; i < k; ++i) {
            swap(values, i, largestK[i]);
        }
        List<Solution> solutions = createStartingPoints(values, random, (int) Math.pow(2, k - 1));

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
        Solution ret = solveWithProgress(solutions, random, maxSteps, values, k, values.length, false);

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
        Random random = new Random();
        List<Solution> solutions = createStartingPoints(values, random, populationSize);
        return solvePopulation(solutions, random, maxSteps, values);
    }

    private static Solution solvePopulation(List<Solution> solutions, Random random, long maxSteps, long[] values) {
        return solveWithProgress(solutions, random, maxSteps, values, 0, values.length, true);
    }

    private static Solution solveWithProgress(List<Solution> solutions, Random random, long maxSteps, long[] values,
                                              int start, int end, boolean print) {
        long count = 0;
        boolean notDone = true;
        long increase = maxSteps / 100;
        long next = maxSteps / 100;
        System.out.print("Input: ");
        printFirstAndLastElements(values);
        for (Solution s : solutions) {
            System.out.print("Initial distribution: ");
            s.printDistribution();
        }
        System.out.print("Progress:  0 %                    ");
        long startTime = Instant.now().getEpochSecond();
        do {
            for (Solution sol : solutions) {
                do {
                    sol.updateIfImprovement(random.nextInt(start, end));
                    ++count;
                    notDone = notDone && sol.isNotOptimal() && count < maxSteps;
                } while (sol.lastImproveLessThanXStepsAway(100) && notDone);
            }
            if (count > next) {
                printProgress(count, maxSteps, startTime);
                next += increase;
            }
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

    private static Solution createStartingPoint(long[] values, Random random) {
        BigInteger inputSum = BigInteger.ZERO;
        Solution sol = new Solution(values);
        for (int i = 0; i < values.length; ++i) {
            inputSum = inputSum.add(BigInteger.valueOf(values[i]));
            if (random.nextBoolean()) {
                sol.setValueToOne(i);
            }
        }
        sol.setInputSum(inputSum);
        return sol;
    }

    private static List<Solution> createStartingPoints(long[] values, Random random, int popCount) {
        System.out.println("Generating initial population");
        BigInteger inputSum = BigInteger.ZERO;
        List<Solution> solutions = new ArrayList<>(popCount);
        for (int i = 0; i < popCount; ++i) {
            solutions.add(new Solution(values));
        }
        for (int i = 0; i < values.length; ++i) {
            inputSum = inputSum.add(BigInteger.valueOf(values[i]));
            for (Solution sol : solutions) {
                if (random.nextBoolean()) {
                    sol.setValueToOne(i);
                }
            }
        }
        for (Solution sol : solutions) {
            sol.setInputSum(inputSum);
        }
        return solutions;
    }

    private static Set<Integer> randomKIndices(int k, Random random, long[] values) {
        return randomKIndices(k, random, 0, values.length);
    }

    private static Set<Integer> randomKIndices(int k, Random random, int start, int end) {
        Set<Integer> set = new HashSet<>(k);
        while (set.size() < k) {
            set.add(random.nextInt(start, end));
        }
        return set;
    }

    private interface KGenerator{
        int generateK(int max);
    }
}