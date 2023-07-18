package Evaluation;

import help.ArrayHelp;
import help.ArrayPrinter;
import help.MinMaxAvgEvaluator;
import help.Printer;
import help.ProgressPrinter;
import main.InputGenerator;
import main.Solution;
import main.Solver;

import java.util.Arrays;

import static help.ArrayHelp.cast;
import static help.ArrayHelp.fill;
import static help.ArrayHelp.generateIntArray;
import static help.ArrayPrinter.printArray;
import static help.Help.runCancellableTask;
import static help.Help.runCancellableTasks;
import static help.MathHelp.log;
import static help.MathHelp.max;
import static help.MathHelp.nlogn;
import static help.Printer.isPrintToConsole;
import static help.Printer.print;
import static help.Printer.printf;
import static help.Printer.println;
import static help.Printer.setPrintToConsole;
import static help.Printer.startFilePrinting;
import static help.Printer.stopWritingToFile;

public class Evaluation {

    private long[] avg, totalAverage, avgFailDif;
    private long[] failed, failDifSum;
    private long[] mut, mutSuccess, mutTried;
    private MinMaxAvgEvaluator[] evaluators;
    private InputGenerator generator;
    private Solver[] solvers;
    private int bestSolver;

    private Evaluation() {
    }

    public static Solver evaluate(int n, int type, int length) {
        return new Evaluation().solveMultiple(n, type, length, null);
    }

    public static Solver evaluate(int n, int type, int length, Solver[] solvers) {
        return new Evaluation().solveMultiple(n, type, length, solvers, null);
    }

    public static Solver evaluate(int n, int type, int length, Solver[] solvers, String postfix) {
        return new Evaluation().solveMultiple(n, type, length, solvers, postfix);
    }

    public static Solver evaluate(int n, InputGenerator generator, int length, Solver[] solvers, String postfix) {
        return new Evaluation().solveMultiple(n, generator, length, solvers, postfix);
    }

    public static void evaluate(int n, InputGenerator generator, int[] lengths, long[] stepLengths, Solver solver, String postfix) {
        new Evaluation().solveMultiple(n, lengths, stepLengths, generator, solver, postfix);
    }

    public static Solver evaluate(int n, int[] inputLengths, long[] stepSizes, InputGenerator generator, Solver[] solvers, String postfix) {
        Evaluation e = new Evaluation();
        int[] inputLengths2 = generateIntArray(inputLengths.length * solvers.length, (i) -> inputLengths[i / solvers.length]);
        long[] stepSizes2 = fill(inputLengths2.length, (i) -> stepSizes[i / solvers.length]);
        Solver[] solvers2 = new Solver[inputLengths2.length];
        fill(solvers2, (i) -> solvers[i % solvers.length]);
        generator.printDescription();
        System.out.println("Lengths:   " + Arrays.toString(inputLengths));
        System.out.println("Stepsizes: " + Arrays.toString(stepSizes));
        System.out.println("Solvers:   " + Arrays.toString(solvers));
        System.out.println("------");
//        int digits = 8;
//        printArray("Lengths:   ", cast(inputLengths2), digits);
//        printArray("Stepsizes: ", stepSizes2, digits);
//        printArray("Solvers:   ", (i) -> solvers2[i].shortName, solvers2.length, digits);
        System.out.println("Starting evaluation...");
        return e.solveMultiple(n, inputLengths2, stepSizes2, generator, solvers2, postfix, (a) -> e.printResult(a, inputLengths2, stepSizes2));
    }

    private static Evaluation merge(Evaluation... evaluations) {
        for (int i = 1; i < evaluations.length; ++i) {
            if (evaluations[i].totalAverage.length != evaluations[0].totalAverage.length) {
                throw new IllegalArgumentException("Evaluators are not compatible due to different lengths");
            }
        }
        Evaluation result = new Evaluation();
        result.initialize(evaluations[0].solvers);
        result.generator = evaluations[0].generator;
        for (Evaluation e : evaluations) {
            for (int i = 0; i < result.totalAverage.length; ++i) {
                result.totalAverage[i] += e.totalAverage[i];
                result.avg[i] += e.avg[i];
                result.mut[i] += e.mut[i];
                result.mutSuccess[i] += e.mutSuccess[i];
                result.mutTried[i] += e.mutTried[i];
                result.failDifSum[i] += e.failDifSum[i];
                result.failed[i] += e.failed[i];
                result.evaluators[i].merge(e.evaluators[i]);
                //TODO merge MinMaxEvaluators
            }
        }
        System.out.println("MinMaxEvaluatorsMerging is missing");
        result.findBestSolver();
        return result;
    }

    public static void evaluateParallel(int n, int type, int length, Solver[] solvers, int runCount) {
        long steps = 100 * nlogn(length);
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
        String folder = Printer.PATH_AUTO_GENERATED + evaluators[0].generator.folder;
        String startTime = Printer.getTodayAsString();
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

    private void initialize(Solver[] solvers) {
        this.solvers = solvers;
        int length = solvers.length;
        avg = new long[length];
        totalAverage = new long[length];
        failed = new long[length];
        failDifSum = new long[length];
        avgFailDif = new long[length];
        mut = new long[length];
        mutSuccess = new long[length];
        mutTried = new long[length];
        evaluators = new MinMaxAvgEvaluator[length];
        fill(evaluators, (i) -> new MinMaxAvgEvaluator(false));
    }

    private Solver solveMultiple(int n, int type, int length, String postfix) {
        return solveMultiple(n, type, length, Solver.getComparison(), postfix);
    }

    private Solver solveMultiple(int n, int type, int length, Solver[] solvers, String postfix) {
        return solveMultiple(n, InputGenerator.create(type), length, solvers, postfix);
    }

    private Solver solveMultiple(int n, InputGenerator generator, int length, Solver[] solvers, String postfix) {
        long steps = 10 * nlogn(length);
//        long steps = 50 * 1000;
        long[] maxStepsArray = fill(solvers.length, (i) -> steps);
        int[] lengthArray = new int[solvers.length];
        Arrays.fill(lengthArray, length);
        return solveMultiple(n, lengthArray, maxStepsArray, generator, solvers, postfix, (a) -> printResult(a, length, steps));
    }

    private void solveMultiple(int n, int[] inputLengths, long[] stepSizes, InputGenerator generator, Solver solver, String postfix) {
        Solver[] solvers = new Solver[inputLengths.length];
        fill(solvers, (i) -> solver);
        solveMultiple(n, inputLengths, stepSizes, generator, solvers, postfix, (a) -> printResult(a, inputLengths, stepSizes));
    }

    private Solver solveMultiple(int n, int[] inputLengths, long[] stepSizes, InputGenerator generator, Solver[] solvers, String postfix, IResultPrinter resultPrinter) {
        initialize(solvers);
        this.generator = generator;
        runCancellableTask(() ->
        {
            String folder = Printer.PATH_AUTO_GENERATED + generator.folder;
            String startTime = Printer.getTodayAsString();
            String append = postfix == null || postfix.equals("") ? "" : "-" + postfix;
//            startFilePrinting(folder + startTime + "-res" + append + ".csv");
            setPrintToConsole(false);
            int temp = calculate(n, inputLengths, stepSizes, !isPrintToConsole());
            setPrintToConsole(true);
            stopWritingToFile();
            println("***************************");
            startFilePrinting(folder + startTime + append + ".txt");
            resultPrinter.printResult(temp);
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

    private void evaluateSolution(int i, long[] input, long maxSteps) {
        Solution sol = solvers[i].solve(input, maxSteps);
        if (sol.isNotOptimal()) {
            ++failed[i];
            failDifSum[i] += sol.getDif().longValue();
            print(";fail");
        } else {
            long tries = sol.getTries();
            evaluators[i].insert(tries);
            print(";" + tries);
        }
        mut[i] += sol.getFlippedBits();
        mutSuccess[i] += sol.getChanges();
        mutTried[i] += sol.getTriedFlips();
    }

    private int calculate(int n, int length, long maxSteps, boolean printProgress) {
        long[] maxStepsArray = fill(solvers.length, (i) -> maxSteps);
        int[] lengthArray = new int[solvers.length];
        Arrays.fill(lengthArray, length);
        return calculate(n, lengthArray, maxStepsArray, printProgress);
    }

    private int calculate(int n, int[] lengths, long[] maxSteps, boolean printProgress) {
        long[] input;
        ProgressPrinter progress = new ProgressPrinter(n);
        int t;
        for (t = 0; t < n; ++t) {
            if (Thread.interrupted()) {
                break;
            }
            print(t);
            input = generator.generate(lengths[0]);
            for (int i = 0; i < solvers.length; ++i) {
                if (input.length != lengths[i]) {
                    input = generator.generate(lengths[i]);
                }
                evaluateSolution(i, input, maxSteps[i]);
            }
            println();
            if (printProgress) {
                progress.printProgressIfNecessary(t);
            }
        }
        if (printProgress) {
            progress.clearProgressAndPrintElapsedTime();
        }
        for (int i = 0; i < evaluators.length; ++i) {
            totalAverage[i] = (evaluators[i].getSum() + failed[i] * maxSteps[i]) / n;
            avg[i] = failed[i] == n ? -1 : evaluators[i].getSum() / (n - failed[i]);
            avgFailDif[i] = failed[i] == 0 ? -1 : failDifSum[i] / failed[i];
        }
        return t;
    }

    private void printResult(int n, int length, long maxSteps) {
        String separation = "---------";
        printf("number of runs;  %,d%n", n);
        printf("Limit per run;   %,d%n", maxSteps);
        printf("array length;    %,d%n", length);
        double ratio = 100 * log(max(generator.generate(length)), 2.0) / length;
        printf("ratio 100 * m/n; %.5f -> %s%n", ratio, ratio > 1.0 ? "hard" : "easy");
        generator.printDescription(separation);
        printTable(separation, maxSteps, n);
        printExplanation(separation);
    }

    private void printResult(int n, int[] inputLengths, long[] stepSizes) {
        String separation = "---------";
        printf("number of runs;  %,d%n", n);
        generator.printDescription(separation);
        printTable(separation, inputLengths, stepSizes, n);
        printExplanation(separation);
        String replacement = "_latex.csv";
        String file = Printer.getCurrenFileName().replace(".txt", replacement);
        stopWritingToFile();
        startFilePrinting(file);
        printCsvForMultipleN(inputLengths);
        stopWritingToFile();
        startFilePrinting(file.replace(replacement, "_unsorted.txt"));
        printTable(separation, inputLengths, stepSizes, n, false);
    }

    private void printCsvForMultipleN(int[] inputLengths) {
        int solverCount = 1;
        while (solverCount < solvers.length && solvers[solverCount] != solvers[0]) {
            ++solverCount;
        }
        int finalSolverCount = solverCount;
        int[] lengths = generateIntArray(solvers.length / solverCount, (i) -> inputLengths[i * finalSolverCount]);
        int[][] indexes = new int[solverCount][];
        for (int i = 0; i < solverCount; ++i) {
            long finalI = i;
            indexes[i] = ArrayHelp.generateIntArray(lengths.length, (a) -> finalI + (long) finalSolverCount * a);
        }
        printPartialCsvForMultipleN(solverCount, lengths, failed, indexes);
        printPartialCsvForMultipleN(solverCount, lengths, avg, indexes);
        printPartialCsvForMultipleN(solverCount, lengths, totalAverage, indexes);
    }

    private void printPartialCsvForMultipleN(int solverCount, int[] lengths, long[] values, int[][] indexes) {
        print(";");
        printArray(lengths);
        for (int i = 0; i < solverCount; ++i) {
            print(solvers[i].fullName + ";");
            printArray(values, indexes[i]);
        }
        println();
    }

    private int columnLength(long[] stepSum, long[] stepMax, long[] stepMin) {
        long highestMutRate = 1 + 3 + ArrayPrinter.getNeededDigits(getHighestMutationRate(stepSum));
        long max = ArrayPrinter.getNeededDigits(avg, totalAverage, stepMax, stepMin, failed, avgFailDif);
        max = Math.max(max, highestMutRate);
        return Math.max(1 + (int) max, 6);
    }

    private long getHighestMutationRate(long[] stepSum) {
        long max = -1;
        for (int i = 0; i < mutTried.length; ++i) {
            long temp = Math.max(stepSum[i] == 0 ? -1 : mutTried[i] / stepSum[i], mutSuccess[i] == 0 ? -1 : mut[i] / mutSuccess[i]);
            if (temp > max) {
                max = temp;
            }
        }
        return max;
    }

    private void printTable(String separation, long maxSteps, int n) {
        printTable(separation, null, fill(solvers.length, (i) -> maxSteps), n);
    }

    private void printTable(String separation, int[] lengths, long[] stepSizes, int n) {
        printTable(separation, lengths, stepSizes, n, true);
    }

    private void printTable(String separation, int[] lengths, long[] stepSizes, int n, boolean sortTable) {
        long[] stepMin = fill(evaluators.length, (i) -> evaluators[i].getMin() == Long.MAX_VALUE ? -1 : evaluators[i].getMin());
        long[] stepMax = fill(evaluators.length, (i) -> evaluators[i].getMax() == Long.MIN_VALUE ? -1 : evaluators[i].getMax());
        long[] stepSum = fill(evaluators.length, (i) -> evaluators[i].getSum());
        long[] totalSum = fill(evaluators.length, (i) -> evaluators[i].getSum() + failed[i] * stepSizes[i]);
        long[] divisors = new long[failed.length];
        Arrays.fill(divisors, n);
        int digits = columnLength(stepSum, stepMax, stepMin);
        println(separation);
        Integer[] indexes = new Integer[totalAverage.length];
        fill(indexes, (i) -> i);
        if (sortTable) {
            Arrays.sort(indexes, this::compareSolver);
        }

        if (lengths != null) {
            long[] castedArray = fill(lengths.length, (i) -> lengths[i]);
            digits = (int) Math.max(ArrayPrinter.getNeededDigits(max(stepSizes, castedArray)), digits);
            printRow("Limit per run;   ", stepSizes, indexes, digits);
            printRow("array length;    ", castedArray, indexes, digits);
            println(separation);
        }
        printArray("algo type;       ", (i) -> solvers[indexes[i]].description, solvers.length, digits);
        printArray("algo param;      ", (i) -> solvers[indexes[i]].parameter, solvers.length, digits);
        printRow("avg mut/change;  ", mut, mutSuccess, indexes, digits);
        printRow("avg mut/step;    ", mutTried, totalSum, indexes, digits);
        println(separation);
        printRow("total avg count; ", totalAverage, indexes, digits);
        printRow("avg eval count;  ", avg, indexes, digits);
        printRow("max eval count;  ", stepMax, indexes, digits);
        printRow("min eval count;  ", stepMin, indexes, digits);
        println(separation);
        printRow("fails;           ", failed, indexes, digits);
        printRow("fail ratio;      ", failed, divisors, indexes, digits);
        printRow("avg fail dif;    ", avgFailDif, indexes, digits);
    }

    private int compareSolver(int a, int b) {
        int res = Long.compare(totalAverage[a], totalAverage[b]);
        return res == 0 ? Long.compare(failDifSum[a], failDifSum[b]) : res;
    }

    private void printRow(String title, long[] values, Integer[] sorting, int digits) {
        printArray(title, (i) -> String.format("%,d", values[sorting[i]]), sorting.length, digits);
    }

    private void printRow(String title, long[] values, long[] divisors, Integer[] sorting, int digits) {
        printArray(title, i -> String.format("%.3f", ((double) values[sorting[i]]) / divisors[sorting[i]]),
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

    private interface IResultPrinter {
        void printResult(int n);
    }
}
