package main;

import help.MinMaxAvgEvaluator;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

import static help.ArrayHelp.fill;
import static help.ArrayPrinter.printResult;
import static help.MathHelp.binomialK;
import static help.Printer.printf;
import static help.Printer.println;
import static help.Printer.setPrintToConsole;
import static help.Printer.startFilePrinting;
import static help.Printer.stopWritingToFile;
import static help.ProgressPrinter.printProgress;
import static main.InputGenerator.generateInput;

public class BinomialTesting {
    private static final String path = Paths.get("").toAbsolutePath() + "\\src\\main\\java\\textFiles\\autoGeneratedResults\\other\\binomialResults\\";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM-HH_mm");

    public static void testMultipleRandomBinomialWithSolution() {
        double[] p = new double[]{0.1, 0.5, 0.1, 0.5, 0.25};
        int count = p.length;
        long[] ms = new long[]{100, 100, 50, 50, 50};
        long[] ns = fill(count, (i) -> 10000);
        long[] ks = fill(count, (i) -> 100);
        testMultipleRandomBinomialWithSolution(ms, ns, p, ks);
    }

    public static void testMultipleRandomBinomialWithSolution(long m, long n, long k) {
        double[] p = new double[]{0.1, 0.25, 0.5, 0.75, 0.9};
        int count = p.length;
        long[] ms = fill(count, (i) -> m);
        long[] ns = fill(count, (i) -> n);
        long[] ks = fill(count, (i) -> k);
        testMultipleRandomBinomialWithSolution(ms, ns, p, ks);
    }

    private static void testMultipleRandomBinomialWithSolution(long[] m, long[] n, double[] p, long[] k) {
        startFilePrinting(path + formatter.format(LocalDateTime.now()) + "_difFromExpectation");
        for (int i = 0; i < m.length; ++i) {
            testRandomBinomialPartition(m[i], n[i], p[i], k[i]);
            println("------------------------------------------------------------------------");
        }
        stopWritingToFile();
    }

    public static void researchBinomialInput(int T) {
        int expected = (int) Math.round(Main.DEFAULT_P_BINOMIAL * Main.DEFAULT_N);
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

    public static void printBinomialDistribution(int length, int n, double p) {
        long[] input = InputGenerator.binomialDistributed(length, n, p);
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
        int expected = (int) Math.round(p * n);
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

    public static void testRandomBinomial(long m, int n) {
        System.out.println(testRandomBinomial(m, n, 1.0 / n));
    }

    public static long testRandomBinomial(long m, int n, double p) {
        long sum = 0;
        for (int i = 0; i < m; ++i) {
            sum += binomialK(n, p);
        }
        return Math.abs(Math.round(m * (n * p)) - sum);
    }

    public static void testRandomBinomialPartition(long m, long n, double p, long k) {
        printf("testRandomBinomialPartition(%,d, %,d, %.3f, %,d)%n", m, n, p, k);
        MinMaxAvgEvaluator evaluator = new MinMaxAvgEvaluator(false);
        MinMaxAvgEvaluator evalDif = new MinMaxAvgEvaluator(false);
        MinMaxAvgEvaluator evalSteps = new MinMaxAvgEvaluator(false);
        long expected = Math.round(m * n * p);
        printf("Expected value: %,d%n", expected);
        setPrintToConsole(false);
        long[] input;
        int countRLSN_fail = 0;
        int countDouble_fail = 0;
        long maxSteps = 100 * 1000;
        long[] failed_BitFlipAmountNecessary = new long[10];
        int changedBitMax = failed_BitFlipAmountNecessary.length - 1;
        int unsolvableCount = 0;
        for (int i = 0; i < k; ++i) {
            input = InputGenerator.binomialDistributed((int) m, (int) n, p);
            Solution sol = PartitionSolver.solveRLS_UniformNeighbour(input, maxSteps, 2);
//            Solution sol = PartitionSolver.solveEA(input, 100000, 3.0 / m);
            evaluator.insert(Math.abs(sol.getTotalSum().longValue() - expected));
            long dif = sol.getDif().longValue();
            if (dif != 0) {
                evalDif.insert(sol.getDif().longValue());
                if (PartitionSolver.solveEA(input, maxSteps, 3.0 / m).isNotOptimal()) {
                    ++countDouble_fail;
                } else {
                    ++countRLSN_fail;
                }
                if (dif == 1) {
                    /*
                    if (PartitionSolver.solveRLS_UniformNeighbour(input, 10 * maxSteps, 2, sol).isNotOptimal()) {
                        System.out.println("Seems unsolvable");
                        ++unsolvableCount;
                        printSeeminglyNotPossibleInput(input, sol, Math.round(n * p));
                    } else {
                        System.out.println("algo just did not have enough time");
                    }
                    /*/
                    long flippedBits = sol.getFlippedBits();
//                    long changes = sol.getChanges();
                    if (PartitionSolver.solveEA(input, 100 * maxSteps, 3.0 / m, sol).isNotOptimal()) {
//                        System.out.println("Seems unsolvable");
                        ++unsolvableCount;
                    } else {
                        flippedBits = sol.getFlippedBits() - flippedBits;
//                        System.out.printf("algo needed to flip %d bits in %d steps%n", flippedBits, sol.getChanges() - changes);
                        ++failed_BitFlipAmountNecessary[(int) Math.min(flippedBits, changedBitMax)];
                    }
                    //*/
                }
            } else {
                evalSteps.insert(sol.getTries());
            }
        }
        String[] temp = new String[]{"dif from expected val", "dif from opt (fail)", "steps count (success)"};
        int digits = Arrays.stream(temp).max(Comparator.comparingInt(String::length)).get().length();
        printResult("desc : ", (i) -> temp[i], temp.length, digits);
        MinMaxAvgEvaluator.printMultipleNonNegative(digits, evaluator, evalDif, evalSteps);
        if (countRLSN_fail > 0) {
            printf("RLS-N(2) did not find a solution but EA-SM(3/n): %d%n", countRLSN_fail);
            if (Arrays.stream(failed_BitFlipAmountNecessary).sum() > 0) {
                digits = 4;
                printResult("Bits that needed to be flipped when dif was 1: ", (i) -> i < changedBitMax ? String.valueOf(i) : changedBitMax + "+", failed_BitFlipAmountNecessary.length, digits);
                printResult("                                        Count: ", failed_BitFlipAmountNecessary, digits);
            }
        }
        if (countDouble_fail > 0) {
            printf("both did not find a solution:                    %d%n", countRLSN_fail);
        }
        if (unsolvableCount > 0) {
            printf("unsolvable inputs: %,d", unsolvableCount);
        }
        setPrintToConsole(true);
    }

    private static void printSeeminglyNotPossibleInput(long[] input, Solution sol, long expected) {
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
        int range = (int) Math.max(expected - min, max - expected);
        long[] amount0 = new long[2 * range + 1];
        long[] amount1 = new long[2 * range + 1];
        int offset = (int) expected - range;
        int[] partition = sol.getPartition();
        for (int i = 0; i < input.length; ++i) {
            if (partition[i] == 0) {
                amount0[(int) input[i] - offset]++;
            } else {
                amount1[(int) input[i] - offset]++;
            }
        }
        long[] values = new long[amount0.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = offset + i;
        }
        printResult(values, 4);
        printResult(amount0, 4);
        printResult(amount1, 4);
        for (int i = 0; i < values.length; ++i) {
            values[i] -= expected;
        }
        printResult(values, 4);
    }
}
