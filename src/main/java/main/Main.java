package main;

import help.ProgressPrinter;

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static help.ArrayPrinter.printFirstAndLastElements;
import static help.ArrayPrinter.printResult;
import static help.Help.runCancellableTask;
import static help.MathHelp.binomialK;
import static help.MathHelp.powerlawK;
import static help.MathHelp.randomDouble;
import static help.MathHelp.xlnx;
import static help.Printer.setPrintToConsole;
import static help.ProgressPrinter.printProgress;
import static main.Evaluation.evaluate;
import static main.InputGenerator.generateInput;

public class Main {

    //default params
    public static final int DEFAULT_LENGTH = 10 * 1000 * 1000;
    public static final long DEFAULT_TIME = Math.round(10 * xlnx(DEFAULT_LENGTH));
    public static final int DEFAULT_LOWEST_VALUE = 1;
    public static final int DEFAULT_BIGGEST_VALUE = 50 * 1000;
    public static final int DEFAULT_SUM_COUNT = 100;
    public static final int DEFAULT_N = 10000;
    public static final double DEFAULT_P_BINOMIAL = 0.1;
    public static final double DEFAULT_P_EXPONENTIAL = 0.001;
    public static final long DEFAULT_BINOMIAL_SHIFT = 100000000000000L;
    public static final double DEFAULT_PMUT_PARAM = -2.75;


    public static void main(String[] args) {
        int selection = 13;
        switch (selection) {
            case 0 -> runCancellableTask(() -> researchBinomialInput(1000));
            case 1 -> runCancellableTask(() -> estimateOptimalSolutionCount(1000 * 1000, 1000));
            case 2 -> evaluate(1000, 5, 100 * 1000);
            case 3 -> tryGeneratingWorstCaseInput();
            case 4 -> readAndSolveInput();
            case 5 -> printSolutionOfOneInput();
            case 6 -> testRandomBinomial(100000000, 1000);
            case 7 -> printBinomialDistribution();
            case 8 -> testRandomNextBoolean();
            case 9 -> evaluateMultiple(3, 2, 1000);
            case 10 ->
                    evaluate(100, 9, 40, new Solver[]{Solver.getRLSUniformRing(4), Solver.getRLSUniformNeighbour(4)});
            case 11 -> evaluate(1000, 5, 100 * 1000, Solver.getComparison(4, 3, 100, 0.5), "compare_all");
            case 12 -> evaluate(1000, 6, 10 * 1000, Solver.getComparison(), "TODO_DELETE");
            case 13 -> compareAllOnAllInstances(1000, 6);
            case 14 -> testRandomPowerLaw();
            case 15 -> compareAllOnAllInstances(100, Solver.getPmutComparison(), "X_pmut_compare");
            case 16 -> evaluate(1000, 6, 10000, Solver.getEAComparison(), "TODO_DELETE");
            case 17 -> evaluate(1000, 6, 10000, Solver.getComparison(2, 2, 3, -2.75), "Z_best_compare");
            case 18 -> evaluate(1000, 10, 10000, Solver.getComparison(2, 2, 3, -2.75), "powerLawDistTest");
            case 19 -> evaluateMultiple(1000, 7, 10 * 1000);
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
                1 * 1000,
                1 * 1000,
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

    private static void compareOnSpecificType() {
//        evaluate(n);
    }

    private static void printSolutionOfOneInput() {
        int length = 200000;
        long[] temp = generateInput(0, length);
        Solution sol = PartitionSolver.solvePmut(temp, 100 * xlnx(length), -2.0);
//        Solution sol = PartitionSolver.solveRLS(temp, 100 * xlnx(length));
        sol.printResult();
    }

    public static void researchBinomialInput(int T) {
        int expected = (int) Math.round(DEFAULT_P_BINOMIAL * DEFAULT_N);
        long sum = 0;
        long sumAbs = 0;
        int intervallLength = 200;
        long[] counts = new long[12000 / intervallLength];
        int t;
        for (t = 0; t < T && !Thread.interrupted(); ++t) {
            long[] input = generateInput(7, 10 * 1000);
            for (int i = 0; i < input.length; ++i) {
                input[i] -= expected;
            }
            long temp = Arrays.stream(input).sum();
            System.out.printf("%d;%d%n", t, temp);
            int abs = (int) Math.abs(temp);
            sum += temp;
            sumAbs += abs;
            ++counts[abs / intervallLength];
        }
        System.out.println("*******************");
        System.out.println("avg: " + (sum / t));
        System.out.println("avg: " + (sumAbs / t));
        long[] countsM = new long[counts.length];
        for (int i = 0; i < countsM.length; ++i) {
            countsM[i] = i * intervallLength;
        }
        printResult(countsM, 5);
        printResult(counts, 5);
    }

    public static void printBinomialDistribution() {
        long[] input = generateInput(7, 10 * 1000);
        long min = input[0];
        long max = input[0];
        //finding borders
        for (int i = 1; i < input.length; ++i) {
            if (input[i] > max) {
                max = input[i];
            } else if (input[i] < min) {
                min = input[i];
            }
        }

        //counting
        int expected = (int) Math.round(DEFAULT_P_BINOMIAL * DEFAULT_N);
        int range = (int) Math.max(expected - min, max - expected);
        long[] amount = new long[2 * range + 1];
        int offset = expected - range;
        for (long l : input) {
            amount[(int) l - offset]++;
        }
        long[] values = new long[amount.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = offset + i;
        }
        printResult(values, 4);
        printResult(amount, 4);
        for (int i = 0; i < values.length; ++i) {
            values[i] -= expected;
        }
        printResult(values, 4);
    }

    public static void estimateOptimalSolutionCount(int m, int amountOfArrays) {
        long[] input = generateInput(7, 10000);
        int counter = 0;
        int stepSize = m / amountOfArrays;
        int next = stepSize;
        System.out.print("  0%");
        long startTime = Instant.now().getEpochSecond();
        int i;
        for (i = 0; i < m && !Thread.interrupted(); ++i) {
            if (!PartitionSolver.solveRLS(input, 0).isNotOptimal()) {
                ++counter;
            }
            if (i == next) {
                printProgress(i, m, startTime);
                next += stepSize;
                input = generateInput(7, 10000);
            }
        }
        printProgress(i, m, startTime);
        System.out.printf("%n%d out of %,d solutions were optimal", counter, i);
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

    private static void testRandomBinomial(long m, int n) {
        testRandomBinomial(m, n, 1.0 / n);
    }

    private static void testRandomBinomial(long m, int n, double p) {
        long sum = 0;
        for (int i = 0; i < m; ++i) {
            sum += binomialK(n, p);
        }
        System.out.printf("Expected: %.5f%n", m * (n * p));
        System.out.printf("actual  : %d%n", sum);
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

}