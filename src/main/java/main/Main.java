package main;

import help.ArrayPrinter;
import help.MinMaxAvgEvaluator;
import help.ProgressPrinter;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static help.ArrayHelp.fill;
import static help.ArrayPrinter.printFirstAndLastElements;
import static help.ArrayPrinter.printResult;
import static help.Help.runCancellableTask;
import static help.Help.runCancellableTasks;
import static help.MathHelp.nlogn;
import static help.MathHelp.powerlawK;
import static help.MathHelp.randomDouble;
import static help.Printer.printf;
import static help.Printer.println;
import static help.Printer.setPrintToConsole;
import static main.Evaluation.evaluate;
import static main.Evaluation.evaluateParallel;
import static main.InputGenerator.generateInput;

public class Main {

    //default params
    public static final int DEFAULT_LENGTH = 10 * 1000 * 1000;
    public static final long DEFAULT_TIME = Math.round(10 * nlogn(DEFAULT_LENGTH));
    public static final int DEFAULT_LOWEST_VALUE = 1;
    public static final int DEFAULT_BIGGEST_VALUE = 50 * 1000;
    public static final int DEFAULT_SUM_COUNT = 100;
    public static final int DEFAULT_N = 10000;
    public static final double DEFAULT_P_BINOMIAL = 0.1;
    public static final double DEFAULT_P_EXPONENTIAL = 0.001;
    public static final long DEFAULT_BINOMIAL_SHIFT = 100000000000000L;
    public static final double DEFAULT_PMUT_PARAM = -2.75;


    public static void main(String[] args) {
        int selection = 28;
        switch (selection) {
            case 0 -> runCancellableTask(() -> BinomialTesting.researchBinomialInput(1000));
            case 1 -> runCancellableTask(() -> BinomialTesting.estimateOptimalSolutionCount(1000 * 1000, 1000));
            case 2 -> evaluate(1000, 5, 100 * 1000);
            case 3 -> tryGeneratingWorstCaseInput();
            case 4 -> readAndSolveInput();
            case 5 -> printSolutionOfOneInput();
            case 6 -> BinomialTesting.testRandomBinomial(100000000, 1000);
            case 7 -> BinomialTesting.printBinomialDistribution(10000, 10000000, 0.0001);
            case 8 -> testRandomNextBoolean();
            case 9 -> evaluateMultiple(3, 2, 1000);
            case 10 ->
                    evaluate(100, 9, 40, new Solver[]{Solver.getRLSUniformRing(4), Solver.getRLSUniformNeighbour(4)});
            case 11 -> evaluate(1000, 5, 100 * 1000, Solver.getComparison(4, 3, 100, 0.5), "compare_all");
            case 12 -> evaluate(1000, 6, 10 * 1000, Solver.getComparison(), "TODO_DELETE");
            case 13 -> compareAllOnAllInstances(1000, 6);
            case 14 -> testRandomPowerLaw();
            case 15 -> compareAllOnAllInstances(100, Solver.getPmutComparison(), "X_pmut_compare");
            case 16 -> evaluate(1000, 0, 50 * 1000, Solver.getEAComparison(), "TODO_DELETE");
            case 17 -> evaluate(1000, 6, 10000, Solver.getComparison(2, 2, 3, -2.75), "Z_best_compare");
            case 18 -> evaluate(1000, 10, 10000, Solver.getComparison(2, 2, 3, -2.75), "powerLawDistTest");
            case 19 -> evaluateMultiple(1000, 1, 50 * 1000);
            case 20 -> evaluateParallel(1000, 7, 1000, Solver.getEAComparison(), 2);
//            case 20 -> evaluate(1000, 7,  1000,  Solver.getEAComparison());
            case 21 -> testParallelRun(6);
            case 22 -> testParallelRun2(6);
            case 23 -> BinomialTesting.testRandomBinomialPartition(14, 10000, 0.1, 1000);
            case 24 -> BinomialTesting.testMultipleRandomBinomialWithSolution(10, 10000, 10000);
            case 25 -> BinomialTesting.testMultipleRandomBinomialWithSolution();
            case 26 -> bruteForceInput(new long[]{1059, 965, 965, 991, 995, 1053, 1022, 1049, 985, 1038});
            case 27 -> bruteForceAll();
            case 28 -> checkLastBitFlippedCount();
        }
    }

    private static void evaluateMultiple(int n, int type, int length) {
        Solver[] solvers = new Solver[3];
        solvers[0] = evaluate(n, type, length, Solver.getRLSComparison(), "rls_compare");
        solvers[1] = evaluate(n, type, length, Solver.getEAComparison(), "ea_compare");
        solvers[2] = evaluate(n, type, length, Solver.getPmutComparison(), "pmut_compare");
        Solver best = evaluate(n, type, length, solvers, "T_comparingBest");
        System.out.printf("+++++++++ %d: %s -> %s +++++++++%n", type, Arrays.toString(solvers), best);
    }

    private static void compareAllOnAllInstances(int n, int start) {
        int[] inputLengths = new int[]{
                2000,           // Integer
                20 * 1000,      // range
                1000 * 1000,    // one Max
                0,              // one Max
                100 * 1000,     // two thirds
                100 * 1000,     // multi sum one
                10 * 1000,      // multi sum range
                10 * 1000,      // binomial
                10 * 1000,      // exponential
                10 * 1000,      // binomial shifted
                20 * 1000,      // power law distributed
        };
        setPrintToConsole(false);
        for (int i = start; i < inputLengths.length; ++i) {
            evaluateMultiple(n, i, inputLengths[i]);
        }
        setPrintToConsole(true);
    }

    private static void compareAllOnAllInstances(int n, Solver[] solvers, String postfix) {
        int[] inputLengths = new int[]{
                2000,
                50 * 1000,
                1000,
                1000,
                1000 * 1000,
                1000 * 1000,
                500 * 1000,
                10 * 1000,
                10 * 1000
        };
        for (int i = 5; i < inputLengths.length; ++i) {
            evaluate(n, i, inputLengths[i], solvers, postfix);
            System.out.println(i + " done");
        }
    }

    private static void printSolutionOfOneInput() {
        int length = 200000;
        long[] temp = generateInput(0, length);
        Solution sol = PartitionSolver.solvePmut(temp, 100 * nlogn(length), -2.0);
//        Solution sol = PartitionSolver.solveRLS(temp, 100 * xlnx(length));
        sol.printResult();
    }

    private static void readAndSolveInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Zahl eingeben: ");
        int selection = scanner.nextInt();
        long[] temp = generateInput(selection, DEFAULT_LENGTH);
        if (temp != null) {
            System.out.println("Solving for the given input");
            long start = Instant.now().toEpochMilli();
//            main.Solution sol = main.PartitionSolver.solve(temp);
//            main.Solution sol = main.PartitionSolver.solveHeavy(temp, 2.0);
            Solution sol = PartitionSolver.solvePopulation(temp, DEFAULT_TIME, 1);
//            main.Solution sol = main.PartitionSolver.solveWithFirstK(temp, 3);
            System.out.printf("elapsed time: %dms%n", Instant.now().toEpochMilli() - start);
            printFirstAndLastElements(temp);
            System.out.print("main.Solution: ");
            sol.printDistribution();
        } else {
            System.err.println("Invalid input.");
        }
    }

    private static void testRandomNextBoolean() {
        System.out.println("  true   equal");
        long sumT = 0;
        long sumE = 0;
        long sumLE = 0;
        for (int j = 0; j < 100; ++j) {
            Random random = new Random();
            boolean last = random.nextBoolean();
            boolean current;
            int t = 0;
            int e = 0;

            for (int i = 2; i < DEFAULT_LENGTH; ++i) {
                current = random.nextBoolean();
                if (current) {
                    ++t;
                }
                if (current == last) {
                    ++e;
                }
                last = current;
            }
            current = random.nextBoolean();
            System.out.printf("%d %d %b %b -> %b %n", t, e, last, current, last == current);
            sumT += t;
            sumE += e;
            sumLE += last == current ? 1 : 0;
        }
        System.out.println("--------------");
        System.out.printf("%d %d %d%n", sumT, sumE, sumLE);
    }

    private static void testRandomPowerLaw() {
        double[] ns = new double[]{
                -1.01, -1.1, -1.2, -1.3, -1.4, -1.5, -1.6, -1.7, -1.8, -1.9,
                -2.0, -2.1, -2.2, -2.3, -2.4, -2.5, -2.6, -2.7, -2.8, -2.9,
                -3.0, -3.1, -3.2, -3.3, -3.4, -3.5, -3.6, -3.7, -3.8, -3.9, -4.0
        };
        long[] avg = new long[ns.length];
        long[] max = new long[ns.length];
        long[] min = new long[ns.length];
        Arrays.fill(min, Integer.MAX_VALUE);
        ProgressPrinter progress = new ProgressPrinter(ns.length, 1L);
        for (int t = 0; t < ns.length; ++t) {
            int val;
            int n = 100000000;
            for (int i = 0; i < n; ++i) {
                val = powerlawK(1.0, 10000.0, ns[t]);
                if (val > max[t]) {
                    max[t] = val;
                } else if (val < min[t]) {
                    min[t] = val;
                }
                avg[t] += val;
//            System.out.println(val);
            }
            avg[t] /= n;
            progress.printProgress(t);
        }
        System.out.println();
        int digits = 6;
        printResult("n  : ", (i) -> String.format("%.2f", ns[i]), ns.length, digits);
        printResult("avg: ", avg, digits);
        printResult("max: ", max, digits);
        printResult("min: ", min, digits);
    }

    private static void tryGeneratingWorstCaseInput() {
        double[] w = new double[6];
        double overallWorst = 0.0;
        for (int j = 0; j < 5000000; ++j) {
            fillRandomWeights(w, 0.05);
            double worst = generateBestDistribution(w);
            if (worst > overallWorst) {
                overallWorst = worst;
                System.out.printf("%.5f", w[0]);
                for (int i = 1; i < w.length; ++i) {
                    System.out.printf(" | %.5f", w[i]);
                }
                System.out.printf(" -> %.8f | %.8f%n", worst, 2 * worst);
            }
        }
        System.out.printf("%.10f", overallWorst);
    }

    private static double generateBestDistribution(double[] w) {
        double worst = 1.0;
        for (int i = 0; i < Math.pow(2, w.length - 1); ++i) {
            double current = (i & 1) * w[1];
            for (int j = 2; j < w.length; ++j) {
                current += ((i >> (j - 1)) & 1) * w[j];
            }
            worst = Math.min(worst, current > 0.5 ? current : 1.0 - current);
        }
        return worst;
    }

    private static void fillRandomWeights(double[] w, double minLastWeight) {
        w[0] = randomDouble(1.0 / w.length, 0.5);
        double remaining = 1.0 - w[0];
        for (int i = 1; i < w.length - 1; ++i) {
            w[i] = randomDouble(remaining / (w.length - i), Math.min(remaining - minLastWeight, w[i - 1]));
            remaining -= w[i];
        }
        w[w.length - 1] = remaining;
    }

    private static void testParallelRun(int numberOfThreads) {
        long start = 0;
        long end = (3L * Integer.MAX_VALUE / numberOfThreads) * numberOfThreads;
        long stepSize = end / numberOfThreads;
        Thread[] threads = new Thread[numberOfThreads];
        long[] results = new long[threads.length];
        for (int i = 0; i < threads.length; ++i) {
            long s = i * stepSize;
            int finalI = i;
            threads[i] = new Thread(() -> results[finalI] = unnecessaryComputation(s, s + stepSize));
        }
        ProgressPrinter p = new ProgressPrinter(end);
        System.out.println(unnecessaryComputation(start, end));
        p.clearProgressAndPrintElapsedTime();
        p = new ProgressPrinter(end);
        runCancellableTasks(threads);
        System.out.println(Arrays.stream(results).sum());
        p.clearProgressAndPrintElapsedTime();
    }

    private static long unnecessaryComputation(long start, long end) {
        long current = 0;
//        long[] unnecessaryMemoryWaste = new long[50000000]; // increases the cost of context switching
//        Random r = new Random(); // produce cache misses for more accurate comparison to real scenario
        for (long i = start; i < end; ++i) {
            current += i;
//            unnecessaryMemoryWaste[Math.abs(r.nextInt(unnecessaryMemoryWaste.length))] = i;
        }
        // hopefully compiler  does not remove the array due to this statement without any effets
//        System.out.printf("%d\b", unnecessaryMemoryWaste[Math.abs(r.nextInt(unnecessaryMemoryWaste.length))] % 2);
        return current;
    }

    private static void testParallelRun2(int numberOfThreads) {
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(Main::timeWasteCalculation);
        }
        ProgressPrinter p = new ProgressPrinter(1);
        for (int i = 0; i < numberOfThreads; ++i) {
            timeWasteCalculation();
            System.out.println(i);
        }
        p.clearProgressAndPrintElapsedTime();
        p = new ProgressPrinter(1);
        runCancellableTasks(threads);
        p.clearProgressAndPrintElapsedTime();
    }

    private static void timeWasteCalculation() {
        InputGenerator generator = InputGenerator.create(10);
        int length = 10 * 1000 * 1000;
        long steps = 10 * nlogn(length);
        PartitionSolver.solveEA(generator.generate(length), steps);
        PartitionSolver.solveEA(generator.generate(length), steps);
        PartitionSolver.solveEA(generator.generate(length), steps);
        PartitionSolver.solveEA(generator.generate(length), steps);
        PartitionSolver.solveEA(generator.generate(length), steps);
    }

    private static boolean bruteForceInput(long[] input) {
        String format = "%0" + input.length + "d: -> dif = %d%n";
        long sum = Arrays.stream(input).sum();
        long best = sum;
        long optimal = sum / 2 + sum % 2;
        long dif = sum - optimal;
        long end = Math.round(Math.pow(2, input.length - 1));
        for (int i = 0; i < end; ++i) {
            long current = (i & 1) * input[1];
            for (int j = 2; j < input.length; ++j) {
                current += ((i >> (j - 1)) & 1) * input[j];
            }
            current = Math.max(current, sum - current);
            if (current < best) {
                best = current;
                dif = best - optimal;
                printf(format, new BigInteger(Integer.toBinaryString(i)), dif);
                if (dif == 0) {
                    break;
                }
            }
        }
        printf("sum:     %d%n", sum);
        printf("optimal: %d%n", optimal);
        printf("best:    %d%n", best);
        printf("dif:     %d%n", dif);
        printf("optimal:     %b%n", dif == 0);
        return dif == 0;
    }

    private static void bruteForceAll() {
        long[][] unsolvableInputs = new long[][]{
                new long[]{983, 997, 997, 943, 1017, 977, 1013, 944, 999, 1017, 1036, 977, 977, 999},
                new long[]{966, 1012, 1036, 1010, 1049, 1062, 995, 1054, 927, 1059, 958, 958, 979, 989}
        };
        setPrintToConsole(false);
        for (long[] input : unsolvableInputs) {
            System.out.println(bruteForceInput(input));
        }
        setPrintToConsole(true);
    }

    private static void checkLastBitFlippedCount() {
        long[] inputLengths = fill(3, (i) -> Math.round(Math.pow(10, i + 3)));
//        long[] inputLengths = new long[]{1000, 10 * 1000, 100 * 1000, 1000 * 1000};
        long[] maxSteps = fill(inputLengths.length, (i) -> 10 * nlogn(inputLengths[i]));
        Solver[] solvers = new Solver[]{Solver.getRLS(), Solver.getRLSUniformRing(2), Solver.getEA(), Solver.getEA(2)};
        MinMaxAvgEvaluator[] evaluators = new MinMaxAvgEvaluator[2 * solvers.length];
        for (int i = 0; i < evaluators.length; ++i) {
            evaluators[i] = new MinMaxAvgEvaluator(false);
        }
        int countPerArray = 100;
        long[] inputArray;
        for (int input = 0; input < inputLengths.length; ++input) {
            inputArray = InputGenerator.oneMaxEquivalentUniformRange((int) inputLengths[input], 1, 10000);
            println(String.valueOf(inputLengths[input]));
            for (int i = 0; i < solvers.length; ++i) {
                for (int run = 0; run < countPerArray; ++run) {
                    Solution sol = solvers[i].solve(inputArray, maxSteps[input]);
                    evaluators[i].insert(sol.getLastBitFlippedCount());
                    evaluators[i + solvers.length].insert(sol.getLastBitFlippedTried());
                    printf("run %2d, %5s -> %2d, %3d, %b%n", run, solvers[i].description,
                            sol.getLastBitFlippedCount(), sol.getLastBitFlippedTried(), sol.isOptimal());
                }
            }
        }
        int digits = 7;
        String[] evalDescriptions1 = new String[evaluators.length];
        String[] evalDescriptions2 = new String[evaluators.length];
        for (int i = 0; i < solvers.length; ++i) {
            evalDescriptions1[i] = solvers[i].description;
            evalDescriptions2[i] = "flips";
            evalDescriptions1[i + solvers.length] = solvers[i].description;
            evalDescriptions2[i + solvers.length] = "tries";
        }
        ArrayPrinter.printResult("algo:  ", evalDescriptions1, digits);
        ArrayPrinter.printResult("type:  ", evalDescriptions2, digits);
        MinMaxAvgEvaluator.printMultipleNonNegative(digits, evaluators);
    }
}