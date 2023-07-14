package main;

import Evaluation.BinomialTesting;
import Evaluation.ResultsChapterPrinter;
import help.ArrayPrinter;
import help.MinMaxAvgEvaluator;
import help.Printer;
import help.ProgressPrinter;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static Evaluation.BinomialTesting.printBinomialDistribution;
import static Evaluation.Evaluation.evaluate;
import static Evaluation.Evaluation.evaluateParallel;
import static help.ArrayHelp.fill;
import static help.ArrayPrinter.getNeededDigits;
import static help.ArrayPrinter.getNeededDigitsSpaced;
import static help.ArrayPrinter.printFirstAndLastElements;
import static help.Help.runCancellableTask;
import static help.Help.runCancellableTasks;
import static help.MathHelp.max;
import static help.MathHelp.nlogn;
import static help.MathHelp.powerlawK;
import static help.MathHelp.randomDouble;
import static help.Printer.PATH_AUTO_GENERATED;
import static help.Printer.pauseFileWriting;
import static help.Printer.printf;
import static help.Printer.println;
import static help.Printer.resumeFileWriting;
import static help.Printer.setPrintToConsole;
import static help.Printer.startFilePrinting;
import static help.Printer.stopWritingToFile;
import static main.InputGenerator.ALL_SAME_AND_LAST_SUM;
import static main.InputGenerator.BINOMIAL_DISTRIBUTED;
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
    public static final double DEFAULT_P_GEOMETRIC = 0.001;
    public static final long DEFAULT_BINOMIAL_SHIFT = 100000000000000L;
    public static final double DEFAULT_PMUT_PARAM = -2.75;


    public static void main(String[] args) {
        int selection;
        try {
            selection = Integer.parseInt(args[0]);
            if (selection == -10) {
                for (int i = 1; i < args.length; ++i) {
                    try {
                        selection = Integer.parseInt(args[i]);
                        mainSelection(selection);
                    } catch (NumberFormatException e) {
                        System.out.println("Number " + args[i] + " could not be parsed as an integer.");
                    } catch (Exception e) {
                        System.out.println("Executing " + selection + " did not work.");
                    }
                }
                return;
            }
        } catch (Exception e) {
            //cmd parameter was not present -> use default value
            selection = 40;
        }
        mainSelection(selection);
    }

    private static void mainSelection(int selection) {
        switch (selection) {
            case 0 -> runCancellableTask(() -> BinomialTesting.researchBinomialInput(1000));
            case 1 -> runCancellableTask(() -> BinomialTesting.estimateOptimalSolutionCount(1000 * 1000, 1000));
            case 2 -> evaluate(1000, 5, 100 * 1000);
            case 3 -> tryGeneratingWorstCaseInput();
            case 4 -> readAndSolveInput();
            case 5 -> printSolutionOfOneInput();
            case 6 -> BinomialTesting.testRandomBinomial(100000000, 1000);
            case 7 -> printBinomialDistribution(10000000, 0.0001, 10000);
            case 8 -> testRandomNextBoolean();
            case 9 -> evaluateMultiple(3, InputGenerator.ALL_SAME_AND_LAST_SUM, 1000, "onemaxOne");
            case 10 ->
                    evaluate(100, 9, 40, new Solver[]{Solver.getRLSUniformRing(4), Solver.getRLSUniformNeighbour(4)});
            case 11 -> evaluate(1000, 5, 100 * 1000, Solver.getComparison(4, 3, 100, 0.5), "compare_all");
            case 12 -> evaluate(1000, 6, 10 * 1000, Solver.getComparison(), "DELETE_TEMP_RESULT");
            case 13 -> compareAllOnAllInstances(1000, 6);
            case 14 -> testRandomPowerLaw();
            case 15 -> compareAllOnAllInstances(100, Solver.getPmutComparison(), "X_pmut_compare");
            case 16 -> evaluate(10, ALL_SAME_AND_LAST_SUM, 10 * 1000, Solver.getRLSComparison(), "DELETE_TEMP_RESULT");
            case 17 -> evaluate(10, 6, 10000, Solver.getComparison(2, 2, 3, -2.75), "Z_best_compare");
            case 18 -> evaluate(1000, 10, 10000, Solver.getComparison(2, 2, 3, -2.75), "powerLawDistTest");
            case 19 -> evaluateMultiple(1000, InputGenerator.PARTIAL_INT_RANGE, 10 * 1000, "uniform");
            case 20 -> evaluateParallel(1000, 7, 1000, Solver.getEAComparison(), 2);
            case 21 -> evaluate(1000, 7, 1000, Solver.getEAComparison());
            case 22 -> testParallelRun(6);
            case 23 -> testParallelRun2(6);
            case 24 -> BinomialTesting.testRandomBinomialPartition(14, 10000, 0.1, 1000);
            case 25 -> BinomialTesting.testMultipleRandomBinomialWithSolution(10, 10000, 10000);
            case 26 -> BinomialTesting.testMultipleRandomBinomialWithSolution();
            case 27 -> bruteForceInput(new long[]{1059, 965, 965, 991, 995, 1053, 1022, 1049, 985, 1038});
            case 28 -> bruteForceAll();
            case 29 -> checkLastBitFlippedCount();
            case 30 -> evaluate(1000, InputGenerator.createBinomial(10, 0.3), 10000, Solver.getRLSComparison(), null);
            case 31 -> bruteForceAll(InputGenerator.createBinomial(1000000, 0.1), 1000, 20);
            case 32 -> printDistribution(InputGenerator.createUniform(1, 101), 10000);
            case 33 -> runCancellableTask(Main::bruteForceMultiple);
            case 34 ->
                    System.out.println(Arrays.toString(InputGenerator.generateInput(InputGenerator.GEOMETRIC_DISTRIBUTED, 20)));
            case 35 ->
                    evaluateSameSolver(1000, new int[]{10, 100, 1000, 10000, 100000}, InputGenerator.createBinomial(1000, 0.1));
            case 36 -> BinomialTesting.testBinomialSolutionCount(1000, 20, 10000, 0.1);
            case 37 -> BinomialTesting.testBinomialSolutionCount(10000, new int[]{10, 12, 14, 16, 18, 20}, 10000, 0.5);
            case 38 -> runCancellableTask(() -> BinomialTesting.testBinomialSolutionCount(10000));
            case 39 -> fineEvaluation(InputGenerator.createUniform(DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE));
            case 40 -> ResultsChapterPrinter.printCompleteEvaluation();
            case 41 ->
                    bruteForceAll(InputGenerator.createUniform(DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE), 1000, 20);
            case 42 ->
                    ResultsChapterPrinter.printComparisonBestTable(PATH_AUTO_GENERATED + "\\UniformIntervall\\13_07-12_29.csv");
            default -> System.out.printf("Invalid input: \"%d\"%n", selection);
        }
    }

    public static void printDistribution(InputGenerator generator, int length) {
        long[] input = generator.generate(length);
        long[] minMax = calculateMinMax(input);
        printDistribution(input, generator, minMax[0], minMax[1]);
    }

    public static void printDistribution(long[] input, InputGenerator generator, long min, long max) {
        long[] amount = new long[(int) (min + max + 1)];
        for (long l : input) {
            amount[(int) (l - min)]++;
        }
        long[] values = new long[amount.length];
        fill(values, (i) -> min + i);
        int digits = (int) getNeededDigits(amount, values);
        generator.printDescription();
        ArrayPrinter.printArray("value;  ", values, digits);
        ArrayPrinter.printArray("count;  ", amount, digits);
        for (int i = 0; i < values.length; ++i) {
            values[i] -= generator.expectedValue;
        }
        ArrayPrinter.printArray("offset; ", values, digits);
    }

    private static void fineEvaluation(InputGenerator generator) {
        int[] lengths = new int[]{20, 50, 100, 500, 1000, 5000, 10000};
        long[] stepSizes = fill(5, (i) -> Math.max(100000, 10 * lengths[i] * nlogn(lengths[i])));
        Solver[] solvers = new Solver[]{
                Solver.getRLSUniformNeighbour(2),
                Solver.getRLSUniformRing(3),
                Solver.getRLSUniformRing(4),
                Solver.getEA(2),
                Solver.getEA(3),
                Solver.getEA(4),
                Solver.getPmut(-2.5),
        };
        evaluate(1000, lengths, stepSizes, generator, solvers, null);
    }

    private static void evaluateSameSolver(int n, int[] lengths, InputGenerator generator) {
        evaluate(n, generator, lengths, fill(lengths.length, (i) -> 100 * nlogn(lengths[i])), Solver.getEA(3), "EA_3_DiffLengths");
    }

    private static void evaluateMultiple(int n, int type, int length, String postfixBest) {
        Solver[] solvers = new Solver[3];
        solvers[0] = evaluate(n, type, length, Solver.getRLSComparison(), "rls_compare");
        solvers[1] = evaluate(n, type, length, Solver.getEAComparison(), "ea_compare");
        solvers[2] = evaluate(n, type, length, Solver.getPmutComparison(), "pmut_compare");
        Solver best = evaluate(n, type, length, solvers, postfixBest);
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
                10 * 1000,      // geometric
                10 * 1000,      // binomial shifted
                20 * 1000,      // power law distributed
        };
        setPrintToConsole(false);
        for (int i = start; i < inputLengths.length; ++i) {
            evaluateMultiple(n, i, inputLengths[i], "");
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
        int length = 10000;
        long[] temp = generateInput(BINOMIAL_DISTRIBUTED, length);
        Solution sol = PartitionSolver.solveRLS_UniformNeighbour(temp, 100 * nlogn(length), 2);
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
            Solution sol = PartitionSolver.solveEA(temp, DEFAULT_TIME);
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
            progress.printProgressIfNecessary(t);
        }
        System.out.println();
        int digits = 6;
        ArrayPrinter.printArray("n  ; ", (i) -> String.format("%.2f", ns[i]), ns.length, digits);
        ArrayPrinter.printArray("avg; ", avg, digits);
        ArrayPrinter.printArray("max; ", max, digits);
        ArrayPrinter.printArray("min; ", min, digits);
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
        long[] unnecessaryMemoryWaste = new long[50000000]; // increases the cost of context switching
        Random r = new Random(); // produce cache misses for more accurate comparison to real scenario
        for (long i = start; i < end; ++i) {
            current += i;
            unnecessaryMemoryWaste[Math.abs(r.nextInt(unnecessaryMemoryWaste.length))] = i;
        }
        // hopefully compiler  does not remove the array due to this statement without any effets
        System.out.printf("%d\b", unnecessaryMemoryWaste[Math.abs(r.nextInt(unnecessaryMemoryWaste.length))] % 2);
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

    private static long bruteForceInput(long[] input) {
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
        return dif;
    }

    private static void bruteForceAll() {
        long[][] unsolvableInputs = new long[][]{
                new long[]{983, 997, 997, 943, 1017, 977, 1013, 944, 999, 1017, 1036, 977, 977, 999},
                new long[]{966, 1012, 1036, 1010, 1049, 1062, 995, 1054, 927, 1059, 958, 958, 979, 989}
        };
        bruteForceAll(unsolvableInputs);
    }

    private static void bruteForceAll(InputGenerator generator, int n, int length) {
        System.out.println("Generating inputs...");
        long[][] unsolvableInputs = new long[n][];
        ProgressPrinter p = new ProgressPrinter(unsolvableInputs.length);
        for (int i = 0; i < n; ++i) {
            unsolvableInputs[i] = generator.generate(length);
            p.printProgressIfNecessary(i);
        }
        p.clearProgressAndPrintElapsedTime();
        generator.printDescription();
        bruteForceAll(unsolvableInputs);
    }

    private static void bruteForceMultiple() {
//        int[] ns = new int[]{10, 100, 1000, 10 * 1000, 100 * 1000};
//        double[] ps = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
//        long[] inputLengths = fill(19, (i) -> i + 2);
        int[] ns = new int[]{10, 20, 30, 40, 50, 100, 200, 500, 1000, 2000, 5000, 10 * 1000, 50 * 1000};
        double[] ps = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        long[] inputLengths = fill(10, (i) -> 2L * (i + 1));
        int count = 1000;
        startFilePrinting(Printer.PATH_AUTO_GENERATED + "other\\"
                + Printer.getTodayAsString() + "binomialInputs_" + ".csv");
        println("id;length;n;p;count;minDif;maxDif;avgDif;solvable;solvablePercent");
        int idOffset = 1000;
        int id = idOffset;
        ProgressPrinter printer = new ProgressPrinter((long) ns.length * ps.length * inputLengths.length, 1);
        for (double p : ps) {
            for (int n : ns) {
                for (long length : inputLengths) {
                    bruteForceInputWithStats((int) length, (int) (n / p), p, count, id);
                    printer.printProgressIfNecessary(id - idOffset);
                    ++id;
                    if (Thread.interrupted()) {
                        stopWritingToFile();
                        return;
                    }
                }
            }
        }
        stopWritingToFile();
    }

    private static void bruteForceInputWithStats(int length, int n, double p, int count, int id) {
        MinMaxAvgEvaluator evaluator = new MinMaxAvgEvaluator(false);
//        ProgressPrinter printer = new ProgressPrinter(count);
        int solvealbe = 0;
        setPrintToConsole(false);
        pauseFileWriting();
        for (int i = 0; i < count; ++i) {
            long current = bruteForceInput(InputGenerator.binomialDistributed(length, n, p));
            evaluator.insert(current);
            if (current == 0) {
                ++solvealbe;
            }
//            printer.printProgressIfNecessary(i);
        }
//        setPrintToConsole(true);
        resumeFileWriting();
//        printer.clearProgressAndPrintElapsedTime();
        printf("%d;%d;%d;%.2f;%d;%d;%d;%d;%d;%.3f%n", id, length, n, p, count, evaluator.getMin(),
                evaluator.getMax(), evaluator.getAvg(), solvealbe, (double) solvealbe / count);
    }

    private static void bruteForceAll(long[][] unsolvableInputs) {
        setPrintToConsole(false);
        MinMaxAvgEvaluator evaluator = new MinMaxAvgEvaluator(false);
        System.out.println("Starting brute force of all inputs...");
        ProgressPrinter p = new ProgressPrinter(unsolvableInputs.length);
        int solvealbe = 0;
        for (int i = 0; i < unsolvableInputs.length; ++i) {
            long current = bruteForceInput(unsolvableInputs[i]);
            evaluator.insert(current);
            if (current == 0) {
                ++solvealbe;
            }
            p.printProgressIfNecessary(i);
        }
        p.clearProgressAndPrintElapsedTime();
        setPrintToConsole(true);
        evaluator.printEvaluation();
        System.out.println("solvable: " + solvealbe);
    }

    private static void checkLastBitFlippedCount() {
//        long[] inputLengths = fill(3, (i) -> Math.round(Math.pow(10, i + 3)));
//        long[] inputLengths = new long[]{1000, 10 * 1000, 100 * 1000, 1000 * 1000};
        long[] inputLengths = new long[]{10000, 100000};
        long[] maxSteps = fill(inputLengths.length, (i) -> 10 * nlogn(inputLengths[i]));
        Solver[] solvers = new Solver[]{Solver.getRLS(), Solver.getRLSUniformRing(2), Solver.getEA(), Solver.getEA(2)};
        for (int input = 0; input < inputLengths.length; ++input) {
            int length = (int) inputLengths[input];
            long[] inputArray = InputGenerator.oneMaxEquivalentUniformRange(length, 1, 10000);
            long steps = maxSteps[input];
            inputArray[length - 2] = inputArray[length - 1] - 1;
            inputArray[length - 1] = 2 * inputArray[length - 2];
            runCancellableTask(() -> checkLastBitFlippedCount(inputArray, steps, solvers, 10));
        }
    }

    private static void checkLastBitFlippedCount(long[] input, long maxSteps, Solver[] solvers, int runs) {
        println(String.valueOf(input.length));
        MinMaxAvgEvaluator[] evaluators = new MinMaxAvgEvaluator[2 * solvers.length];
        fill(evaluators, (i) -> new MinMaxAvgEvaluator(false));
        ProgressPrinter p = new ProgressPrinter((long) solvers.length * runs, solvers.length);
        long pCounter = 0;
        for (int run = 0; run < runs; ++run) {
            if (Thread.interrupted()) {
                break;
            }
            for (int i = 0; i < solvers.length; ++i) {
                Solution sol = solvers[i].solve(input, maxSteps);
                evaluators[i].insert(sol.getLastBitFlippedCount());
                evaluators[i + solvers.length].insert(sol.getLastBitFlippedTried());
//                    printf("run %2d, %5s -> %2d, %3d, %b%n", run, solvers[i].description,
//                            sol.getLastBitFlippedCount(), sol.getLastBitFlippedTried(), sol.isOptimal());
                p.printProgressIfNecessary(pCounter);
                ++pCounter;
            }
        }
        p.clearProgressAndPrintElapsedTime();
        print(solvers, evaluators);
    }

    private static void print(Solver[] solvers, MinMaxAvgEvaluator[] evaluators) {
        int digits = (int) getNeededDigitsSpaced(max((i) -> evaluators[i].getSum(), evaluators.length));
        digits = Math.max(7, digits);
        String[] evalSolverDesc = new String[evaluators.length];
        String[] evalSolverParam = new String[evaluators.length];
        String[] evalType = new String[evaluators.length];
        for (int i = 0; i < solvers.length; ++i) {
            evalSolverDesc[i] = solvers[i].description;
            evalSolverDesc[i + solvers.length] = solvers[i].description;
            evalType[i] = "flips";
            evalType[i + solvers.length] = "tries";
            evalSolverParam[i] = solvers[i].parameter;
            evalSolverParam[i + solvers.length] = solvers[i].parameter;
        }
        ArrayPrinter.printArray("algo:  ", evalSolverDesc, digits);
        ArrayPrinter.printArray("algo:  ", evalSolverParam, digits);
        ArrayPrinter.printArray("type:  ", evalType, digits);
        MinMaxAvgEvaluator.printMultipleNonNegative(digits, evaluators);
    }

    public static long[] calculateMinMax(long[] input) {
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
        return new long[]{min, max};
    }
}