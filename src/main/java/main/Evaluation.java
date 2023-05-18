package main;

import help.ArrayPrinter;
import help.ProgressPrinter;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static help.ArrayPrinter.printFirstAndLastElements;
import static help.Help.runCancellableTask;
import static help.Help.runCancellableTasks;
import static help.MathHelp.log;
import static help.MathHelp.max;
import static help.MathHelp.xlnx;
import static help.Printer.isPrintToConsole;
import static help.Printer.print;
import static help.Printer.printf;
import static help.Printer.println;
import static help.Printer.setPrintToConsole;
import static help.Printer.startFilePrinting;
import static help.Printer.stopWritingToFile;

public class Evaluation {


    private static final String path = Paths.get("").toAbsolutePath() + "\\src\\main\\java\\textFiles\\autoGeneratedResults\\";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM-HH_mm");

    private long[] stepSum, stepMin, stepMax;
    private long[] avg, totalAverage;
    private long[] failed, failDif;
    private long[] mut, mutSuccess, mutTried;
    private InputGenerator generator;
    private Solver[] solvers;
    private int bestSolver;

    private Evaluation() {
    }

    private void initialize(Solver[] solvers) {
        this.solvers = solvers;
        int length = solvers.length;
        stepSum = new long[length];
        stepMin = new long[length];
        stepMax = new long[length];
        avg = new long[length];
        totalAverage = new long[length];
        failed = new long[length];
        failDif = new long[length];
        mut = new long[length];
        mutSuccess = new long[length];
        mutTried = new long[length];
        Arrays.fill(stepMin, Long.MAX_VALUE);
    }

    private static Evaluation merge(Evaluation... evals) {
        for (int i = 1; i < evals.length; ++i) {
            if (evals[i].totalAverage.length != evals[0].totalAverage.length) {
                throw new IllegalArgumentException("Evaluators are not compatible due to different lengths");
            }
        }
        Evaluation result = new Evaluation();
        result.initialize(evals[0].solvers);
        result.generator = evals[0].generator;
        for (Evaluation e : evals) {
            for (int i = 0; i < result.totalAverage.length; ++i) {
                result.totalAverage[i] += e.totalAverage[i];
                result.avg[i] += e.avg[i];
                result.mut[i] += e.mut[i];
                result.mutSuccess[i] += e.mutSuccess[i];
                result.mutTried[i] += e.mutTried[i];
                result.stepSum[i] += e.stepSum[i];
                result.stepMax[i] = Math.max(result.stepMax[i], e.stepMax[i]);
                result.stepMin[i] = Math.min(result.stepMin[i], e.stepMin[i]);
                result.failDif[i] += e.failDif[i];
                result.failed[i] += e.failed[i];
            }
        }
        result.findBestSolver();
        return result;
    }

    public static void evaluateParallel(int n, int type, int length, Solver[] solvers, int runCount) {
        long steps = 100 * xlnx(length);
        Evaluation[] evaluators = new Evaluation[runCount];
        for (int i = 0; i < evaluators.length; ++i) {
            evaluators[i] = new Evaluation();
            evaluators[i].initialize(solvers);
            evaluators[i].generator = InputGenerator.create(type);
        }
        setPrintToConsole(false);
        int newN = n / runCount;

        int[] runLengths = new int[evaluators.length];
        Thread[] treads = new Thread[evaluators.length];
        String folder = path + evaluators[0].generator.folder;
        String startTime = formatter.format(LocalDateTime.now());
        treads[0] = new Thread(() -> runLengths[0] = evaluators[0].calculate(newN, length, steps, true));
        for (int i = 1; i < treads.length; ++i) {
            int finalI = i;
            treads[i] = new Thread(() -> runLengths[finalI] = evaluators[finalI].calculate(newN, length, steps, false));
        }
        runCancellableTasks(treads);
        setPrintToConsole(true);
        Evaluation eval = merge(evaluators);
        System.out.println();
        startFilePrinting(folder + startTime + "-sum_parallel" + runCount + ".txt");
        eval.printResult(Arrays.stream(runLengths).sum(), length, steps);
        stopWritingToFile();
        System.out.println("---------------Evaluation complete-------------");
    }

    public static Solver evaluate(int n, int type, int length) {
        return new Evaluation().solveMultiple(n, type, length, null);
    }

    public static Solver evaluate(int n, int type, int length, Solver[] solvers) {
        return new Evaluation().solveMultiple(n, type, length, solvers, null);
    }

    public static Solver evaluate(int n, int type, int length, String postfix) {
        return new Evaluation().solveMultiple(n, type, length, postfix);
    }

    public static Solver evaluate(int n, int type, int length, Solver[] solvers, String postfix) {
        return new Evaluation().solveMultiple(n, type, length, solvers, postfix);
    }

    private Solver solveMultiple(int n, int type, int length, String postfix) {
        return solveMultiple(n, type, length, Solver.getComparison(), postfix);
    }

    private Solver solveMultiple(int n, int type, int length, Solver[] solvers, String postfix) {
        long steps = 100 * xlnx(length);
        initialize(solvers);
        generator = InputGenerator.create(type);
        runCancellableTask(() ->
        {
            String folder = path + generator.folder;
            String startTime = formatter.format(LocalDateTime.now());
            String append = postfix == null || postfix.equals("") ? "" : "-" + postfix;
//            startFilePrinting(folder + startTime + "-res" + append + ".csv");
            setPrintToConsole(false);
            int temp = calculate(n, length, steps, !isPrintToConsole());
            setPrintToConsole(true);
            stopWritingToFile();
            println("***************************");
            startFilePrinting(folder + startTime + "-sum" + append + ".txt");
            printResult(temp, length, steps);
            stopWritingToFile();
            bestSolver = findBestSolver();
            append = append.equals("") ? "" : append.substring(1);
            System.out.printf("---------------Evaluation %s complete-------------%n", append);
        });
        return solvers[bestSolver];
    }

    private int findBestSolver() {
        int best = 0;
        for (int i = 1; i < solvers.length; ++i) {
            if (compareSolver(i, best) < 0) {
                best = i;
            }
        }
        return best;
    }

    private int calculate(int n, int length, long maxSteps, boolean printProgress) {
        long[] input;
        ProgressPrinter progress = new ProgressPrinter(n);
        for (int t = 0; t < n; ++t) {
            if (Thread.interrupted()) {
                return t;
            }
            print(t);
            input = generator.generate(length);
            for (int i = 0; i < stepSum.length; ++i) {
                Solution sol = solvers[i].solve(input, maxSteps);
                if (sol.isNotOptimal()) {
                    ++failed[i];
                    failDif[i] += sol.getDif().longValue();
                    print(";fail");
                } else {
                    long tries = sol.getTries();
                    mut[i] += sol.getFlippedBits();
                    mutSuccess[i] += sol.getChanges();
                    mutTried[i] += sol.getTriedFlips();
                    stepSum[i] += tries;
                    if (tries > stepMax[i]) {
                        stepMax[i] = sol.getTries();
                    }
                    if (sol.getTries() < stepMin[i]) {
                        stepMin[i] = sol.getTries();
                    }
                    print(";" + tries);
                }
            }
            println();
            if (printProgress) {
                progress.printProgressIfNecessary(t);
            }
        }
        if (printProgress) {
            progress.clearProgressAndPrintElapsedTime();
        }
        return n;
    }

    private void printResult(int n, int length, long maxSteps) {
        String separation = "---------";
        if (generator.type > 1 && generator.type < 7) {
            printFirstAndLastElements(generator.generate(length), 10);
        }
        printf("input type:      %s (%d)%n", generator.description, generator.type);
        printf("array length:    %,d%n", length);
        printf("number of runs:  %,d%n", n);
        printf("Limit per run:   %,d%n", maxSteps);
        double ratio = 100 * log(max(generator.generate(length)), 2.0) / length;
        printf("ratio 100 * m/n: %.5f -> %s%n", ratio, ratio > 1.0 ? "hard" : "easy");
        if (generator.hasBounds()) {
            printf("lowest value:    %,d%n", generator.bottom);
            printf("highest value:   %,d%n", generator.top);
        }
        if (generator.hasFieldWithSum()) {
            printf("Fields with sum: %,d%n", generator.sumCount);
        }
        if (generator.type == 7 || generator.type == 8) {
            println(separation);
            printf("n value:         %,d%n", generator.n);
            printf("p:               %.6f%n", generator.p);
            printf("expected value:  %,d%n", generator.expectedValue);
        }
        printTable(separation, maxSteps, n);
        printExplanation(separation);
    }


    private int tableLength(long[] avg, long[] totalAvg) {
        long highestMutRate = 1 + 3 + getNeededDigits(getHighestMutationRate());
        long max = max(max(avg), max(totalAvg));
        max = Math.max(max, max(stepMax));
        max = Math.max(max, max(stepMin));
        max = Math.max(max, max(failed));
        max = Math.max(max, max(failDif));
        max = Math.max(getNeededDigits(max), highestMutRate);
        return Math.max(1 + (int) max, 6);
    }

    private long getHighestMutationRate() {
        long max = -1;
        for (int i = 0; i < mutTried.length; ++i) {
            long temp = Math.max(stepSum[i] == 0 ? -1 : mutTried[i] / stepSum[i], mutSuccess[i] == 0 ? -1 : mut[i] / mutSuccess[i]);
            if (temp > max) {
                max = temp;
            }
        }
        return max;
    }

    private long getNeededDigits(long val) {
        val = (long) Math.log10((double) val); // without decimal point
        return val + 1 + val / 3; // first digit + decimal points
    }

    private void printTable(String separation, long maxSteps, int n) {
        for (int i = 0; i < stepMin.length; ++i) {
            if (stepMin[i] == Long.MAX_VALUE) {
                stepMin[i] = -1;
            }
        }
        for (int i = 0; i < stepSum.length; ++i) {
            totalAverage[i] = (stepSum[i] + failed[i] * maxSteps) / n;
        }
        for (int i = 0; i < stepSum.length; ++i) {
            avg[i] = failed[i] == n ? -1 : stepSum[i] / (n - failed[i]);
        }
        for (int i = 0; i < stepSum.length; ++i) {
            failDif[i] = failed[i] == 0 ? -1 : failDif[i] / failed[i];
        }
        long[] divisors = new long[failed.length];
        Arrays.fill(divisors, n);
        int digits = tableLength(avg, totalAverage);
        println(separation);
        Integer[] indexes = new Integer[totalAverage.length];
        for (int i = 0; i < indexes.length; ++i) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, this::compareSolver);

        ArrayPrinter.printResult("algo type:       ", (i) -> solvers[indexes[i]].description, solvers.length, digits);
        ArrayPrinter.printResult("algo param:      ", (i) -> solvers[indexes[i]].parameter, solvers.length, digits);
        printRow("avg mut/change:  ", mut, mutSuccess, indexes, digits);
        printRow("avg mut/step:    ", mutTried, stepSum, indexes, digits);
        println(separation);
        printRow("total avg count: ", totalAverage, indexes, digits);
        printRow("avg eval count:  ", avg, indexes, digits);
        printRow("max eval count:  ", stepMax, indexes, digits);
        printRow("min eval count:  ", stepMin, indexes, digits);
        println(separation);
        printRow("fails:           ", failed, indexes, digits);
        printRow("fail ratio:      ", failed, divisors, indexes, digits);
        printRow("avg fail dif:    ", failDif, indexes, digits);
    }

    private int compareSolver(int a, int b) {
        int res = Long.compare(totalAverage[a], totalAverage[b]);
        return res == 0 ? Long.compare(failDif[a], failDif[b]) : res;
    }

    private void printRow(String title, long[] values, Integer[] sorting, int digits) {
        ArrayPrinter.printResult(title, (i) -> String.format("%,d", values[sorting[i]]), sorting.length, digits);
    }

    private void printRow(String title, long[] values, long[] divisors, Integer[] sorting, int digits) {
        ArrayPrinter.printResult(title, i -> String.format("%.3f", ((double) values[sorting[i]]) / divisors[sorting[i]]),
                sorting.length, digits);
    }

    private void printExplanation(String separation) {
        println(separation);
        println("RLS     -> standard RLS: uniform random neighbour with Hamming Distance == 1");
        println("RLS-N   -> modified RLS: uniform random neighbour with Hamming Distance <= n");
        println("RLS-R   -> modified RLS: uniform random neighbour with Hamming Distance == x, where x is chosen uniform random from {1,...,r}");
        println("EA      -> standard EA : each bit is flipped with prob 1/n");
        println("EA-SM   -> modified EA : each bit is flipped with prob c/n");
        println("fmut    -> 1 bit flip with prob p and uniform random amount with 1-p");
        println("pmut    -> flips k random bits where k is chosen from powerlaw distribution");
    }
}
