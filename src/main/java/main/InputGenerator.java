package main;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static help.ArrayHelp.fill;
import static help.ArrayPrinter.printFirstAndLastElements;
import static help.MathHelp.binomialK;
import static help.MathHelp.geometricK;
import static help.MathHelp.powerlawK;
import static help.Printer.printf;
import static help.Printer.println;
import static main.Main.DEFAULT_BIGGEST_VALUE;
import static main.Main.DEFAULT_BINOMIAL_SHIFT;
import static main.Main.DEFAULT_LOWEST_VALUE;
import static main.Main.DEFAULT_N;
import static main.Main.DEFAULT_PMUT_PARAM;
import static main.Main.DEFAULT_P_BINOMIAL;
import static main.Main.DEFAULT_P_GEOMETRIC;
import static main.Main.DEFAULT_SUM_COUNT;

public class InputGenerator {

    public static final Random RANDOM = new Random();
    public static final int READ_FROM_CONSOLE = -1;
    public static final int COMPLETE_INT_RANGE = 0;
    public static final int PARTIAL_INT_RANGE = 1;
    public static final int ALL_SAME_AND_LAST_SUM = 2;
    public static final int LAST_SUM_WITH_RANGE = 3;
    public static final int LAST_TWO_SUM_REST_ONE = 4;
    public static final int ALL_ONE_EXCEPT_LAST_X_ELEMENTS = 5;
    public static final int ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS = 6;
    public static final int BINOMIAL_DISTRIBUTED = 7;
    public static final int GEOMETRIC_DISTRIBUTED = 8;
    public static final int BINOMIAL_DISTRIBUTED_SHIFT = 9;
    public static final int POWERLAW_DISTRIBUTED = 10;
    public static final int MIXED = 11;
    public static final int OVERLAPPED = 12;
    public static final int MIXED_AND_OVERLAPPED = 13;
    public final int type;
    public final String folder;
    public final String description;
    public final int bottom;
    public final int top;
    public final int sumCount;
    public final int n;
    public final double p;
    public final long expectedValue;
    public final double pMutParam;
    private final Generator generator;
    private final boolean outputConstant;
    private long[] output;
    private InputGenerator(int type, Generator generator) {
        this(type, generator, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE,
                type == LAST_TWO_SUM_REST_ONE ? 2 : (type == 2 || type == 3 ? 1 : DEFAULT_SUM_COUNT),
                DEFAULT_N,
                type == BINOMIAL_DISTRIBUTED ? DEFAULT_P_BINOMIAL : DEFAULT_P_GEOMETRIC,
                Math.round(type == 7 ? DEFAULT_P_BINOMIAL * DEFAULT_N : 1.0 / DEFAULT_P_GEOMETRIC),
                DEFAULT_PMUT_PARAM);
    }


    private InputGenerator(int type, Generator generator, int bottom, int top, int sumCount, int n, double p,
                           long expectedValue, double pMutParam) {
        this.type = type;
        this.generator = generator;
        this.bottom = bottom;
        this.top = top;
        this.sumCount = sumCount;
        this.n = n;
        this.p = p;
        this.expectedValue = expectedValue;
        this.pMutParam = pMutParam;
        folder = getFolder(type);
        description = getInputTypeDescription(type);
        outputConstant = type == ALL_SAME_AND_LAST_SUM
                || type == LAST_TWO_SUM_REST_ONE || type == ALL_ONE_EXCEPT_LAST_X_ELEMENTS;
    }

    public static long[] generateInput(int selection, int length) {
        if (selection == READ_FROM_CONSOLE) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Insert Type: ");
            int type = scanner.nextInt();
            System.out.print("Insert length: ");
            length = scanner.nextInt();
            System.out.print("Insert lowest value : ");
            int start = scanner.nextInt();
            System.out.print("Insert highest value: ");
            int end = scanner.nextInt();
            System.out.print("Insert amount of fields with sum as value: ");
            int sumCount = scanner.nextInt();
            return createInput(type, length, start, end, sumCount);
        } else {
            return createInput(selection, length, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE, DEFAULT_SUM_COUNT);
        }
    }

    public static long[] createInput(int type, int length, int low, int high, int sumCount) {
        return switch (type) {
            case COMPLETE_INT_RANGE -> completeIntRange(length);
            case PARTIAL_INT_RANGE -> uniformRandomRange(length, low, high);
            case ALL_SAME_AND_LAST_SUM -> oneMaxEquivalent(length);
            case LAST_SUM_WITH_RANGE -> oneMaxEquivalentUniformRange(length, low, high);
            case LAST_TWO_SUM_REST_ONE -> lastTwoSumRestOne(length);
            case ALL_ONE_EXCEPT_LAST_X_ELEMENTS -> lastXSumRestOne(length, sumCount);
            case ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS -> lastXSumRestUniformRange(length, low, high, sumCount);
            case BINOMIAL_DISTRIBUTED -> binomialDistributed(length, DEFAULT_N, DEFAULT_P_BINOMIAL);
            case GEOMETRIC_DISTRIBUTED -> geometricDistributed(length, DEFAULT_P_GEOMETRIC, DEFAULT_N);
            case BINOMIAL_DISTRIBUTED_SHIFT ->
                    binomialDistributedShifted(length, DEFAULT_N, DEFAULT_P_BINOMIAL, DEFAULT_BINOMIAL_SHIFT);
            case POWERLAW_DISTRIBUTED ->
                    powerlawDistributed(length, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE, DEFAULT_PMUT_PARAM);
            case MIXED -> mixed(length, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE, DEFAULT_N, DEFAULT_P_BINOMIAL,
                    DEFAULT_P_GEOMETRIC, DEFAULT_PMUT_PARAM);
            case OVERLAPPED ->
                    overlapped(length, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE, DEFAULT_N, DEFAULT_P_BINOMIAL,
                            DEFAULT_P_GEOMETRIC, DEFAULT_PMUT_PARAM);
            case MIXED_AND_OVERLAPPED ->
                    mixedAndOverlapped(length, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE, DEFAULT_N, DEFAULT_P_BINOMIAL,
                            DEFAULT_P_GEOMETRIC, DEFAULT_PMUT_PARAM);
            default -> null;
        };
    }

    private static long epsilonDif(int length, int sumCount) {
        return Math.round(((length - sumCount) * 0.3) / (sumCount + 2));
    }

    public static String getInputTypeDescription(int type) {
        return switch (type) {
            case COMPLETE_INT_RANGE -> "uniform random values from complete int range";
            case PARTIAL_INT_RANGE -> "uniform random values from given range";
            case ALL_SAME_AND_LAST_SUM -> "OneMax equivalent for partition";
            case LAST_SUM_WITH_RANGE ->
                    "uniform random values from given range except last field as sum of all other fields";
            case LAST_TWO_SUM_REST_ONE -> "Carsten Witts worst case input (1/3, 1/3, 1/(3(n-2)), ...)";
            case ALL_ONE_EXCEPT_LAST_X_ELEMENTS ->
                    "all values are one except the last x fields as sum of all other fields";
            case ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS ->
                    "uniform random values from given range except last x fields as sum of all other fields";
            case BINOMIAL_DISTRIBUTED -> "binomial distributed";
            case GEOMETRIC_DISTRIBUTED -> "geometric distributed";
            case BINOMIAL_DISTRIBUTED_SHIFT -> "binomial distributed with shift";
            case POWERLAW_DISTRIBUTED -> "powerlaw distributed";
            case MIXED -> "mixed";
            case OVERLAPPED -> "overlapped";
            case MIXED_AND_OVERLAPPED -> "mixed and overlapped";
            default -> "invalid input";
        };
    }

    public static long[] completeIntRange(int length) {
        return fill(length, (i) -> Math.abs(RANDOM.nextInt()));
    }

    public static long[] uniformRandomRange(int length, int low, int high) {
        return fill(length, (i) -> Math.abs(RANDOM.nextInt(low, high)));
    }

    public static long[] oneMaxEquivalent(int length) {
        long[] ret = new long[length];
        Arrays.fill(ret, 1);
        ret[ret.length - 1] = length - 1;
        return ret;
    }

    public static long[] oneMaxEquivalentUniformRange(int length, int low, int high) {
        long[] ret = fill(length, (a) -> Math.abs(RANDOM.nextInt(low, high)), 0, length - 1);
        ret[ret.length - 1] = Arrays.stream(ret).sum();
        return ret;
    }

    public static long[] lastTwoSumRestOne(int length) {
        long[] ret = new long[length];
        Arrays.fill(ret, 1);
        long sum = (length - 2) - epsilonDif(length, 2);
        ret[ret.length - 2] = sum;
        ret[ret.length - 1] = sum;
        return ret;
    }

    public static long[] lastXSumRestOne(int length, int sumCount) {
        long[] ret = new long[length];
        Arrays.fill(ret, 0, ret.length - sumCount, 1);
        long sum = (length - sumCount) / (sumCount + 1) - epsilonDif(length, sumCount);
        Arrays.fill(ret, ret.length - sumCount, ret.length, sum);
        return ret;
    }

    public static long[] lastXSumRestUniformRange(int length, int low, int high, int sumCount) {
        long[] ret = fill(length, (i) -> Math.abs(RANDOM.nextInt(low, high)), 0, length - sumCount);
        long sum = Arrays.stream(ret).sum();
        sum = sum / (sumCount + 1) - epsilonDif(length, sumCount);
        Arrays.fill(ret, ret.length - sumCount, ret.length, sum);
        return ret;
    }

    public static long[] binomialDistributed(int length, int n, double p) {
        return fill(length, (i) -> binomialK(n, p));
    }

    public static long[] geometricDistributed(int length, double p, int highestVal) {
        return fill(length, (i) -> geometricK(highestVal, p));
    }

    public static long[] binomialDistributedShifted(int length, int n, double p, long shift) {
        return fill(length, (i) -> shift + binomialK(n, p));
    }

    public static long[] powerlawDistributed(int length, double bottom, double top, double n) {
        return fill(length, (i) -> powerlawK(bottom, top, n));
    }

    private static long overlappedValue(double bottom, double top, double pBin, int nBin, double pExp, double nPmut) {
        return RANDOM.nextInt((int) bottom, (int) top) + binomialK(nBin, pBin) + geometricK((int) top, pExp)
                + powerlawK(bottom, top, nPmut);
    }

    private static long mixedOrOverlappedValue(int index, double bottom, double top, int nBin, double pBin, double pExp, double nPmut) {
        return switch (index) {
            case 0 -> RANDOM.nextInt((int) bottom, (int) top);
            case 1 -> binomialK(nBin, pBin);
            case 2 -> geometricK((int) top, pExp);
            case 3 -> powerlawK(bottom, top, nPmut);
            default -> overlappedValue(bottom, top, pBin, nBin, pExp, nPmut);
        };
    }

    public static long[] mixed(int length, double bottom, double top, int nBin, double pBin, double pExp, double nPmut) {
        long[] ret = new long[length];
        for (int i = 0; i < length; ++i) {
            int rgn = RANDOM.nextInt(0, 4);
            ret[i] = mixedOrOverlappedValue(rgn, bottom, top, nBin, pBin, pExp, nPmut);
        }
        return ret;
    }

    public static long[] overlapped(int length, double bottom, double top, int pBin, double nBin, double pExp, double nPmut) {
        long[] ret = new long[length];
        for (int i = 0; i < length; ++i) {
            ret[i] = overlappedValue(bottom, top, nBin, pBin, pExp, nBin);
        }
        return ret;
    }

    public static long[] mixedAndOverlapped(int length, double bottom, double top, int nBin, double pBin, double pExp, double nPmut) {
        long[] ret = new long[length];
        for (int i = 0; i < length; ++i) {
            int rgn = RANDOM.nextInt(0, 8);
            ret[i] = mixedOrOverlappedValue(rgn, bottom, top, nBin, pBin, pExp, nPmut);
        }
        return ret;
    }

    public static InputGenerator create(int type) {
        return new InputGenerator(type, (a) -> generateInput(type, a));
    }

    public static InputGenerator createBinomial(int n, double p) {
        long expectedValue = Math.round(n * p);
        return new InputGenerator(BINOMIAL_DISTRIBUTED, (a) -> binomialDistributed(a, n, p), 0,
                (int) expectedValue, 0, n, p, expectedValue, DEFAULT_PMUT_PARAM);
    }

    public long[] generate(int length) {
        if (outputConstant) {
            if (output == null) {
                output = generator.generate(length);
            }
            return output;
        }
        return generator.generate(length);
    }

    private String getFolder(int type) {
        return switch (type) {
            case COMPLETE_INT_RANGE, PARTIAL_INT_RANGE -> "UniformIntervall";
            case ALL_SAME_AND_LAST_SUM, LAST_SUM_WITH_RANGE -> "OneMax";
            case LAST_TWO_SUM_REST_ONE -> "TwoThirds";
            case ALL_ONE_EXCEPT_LAST_X_ELEMENTS -> "MultipleSumsAtEnd_One";
            case ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS -> "MultipleSumsAtEnd_Range";
            case BINOMIAL_DISTRIBUTED -> "Binomial";
            case GEOMETRIC_DISTRIBUTED -> "Geometric";
            case MIXED -> "Mixed";
            case OVERLAPPED -> "Overlapped";
            case MIXED_AND_OVERLAPPED -> "MixedAndOverlapped";
            default -> "other";
        } + "\\";
    }

    public boolean hasBounds() {
        return type == PARTIAL_INT_RANGE || type == LAST_SUM_WITH_RANGE
                || type == ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS || type == POWERLAW_DISTRIBUTED;
    }

    public void printDescription() {
        printDescription(null);
    }

    public void printDescription(String separation) {
        printf("input type;      %s (%d)%n", description, type);
        if (hasBounds()) {
            printf("lowest value;    %,d%n", bottom);
            printf("highest value;   %,d%n", top);
        }
        if (hasFieldWithSum()) {
            printf("Fields with sum; %,d%n", sumCount);
        }
        if (type == 7 || type == 8) {
            if (separation != null) {
                println(separation);
            }
            printf("n value;         %,d%n", n);
            printf("p;               %.6f%n", p);
            printf("expected value;  %,d%n", expectedValue);
        }
        if (outputConstant && output != null) {
            printFirstAndLastElements(output, 10);
        }
    }

    public boolean hasFieldWithSum() {
        return type >= 2 && type <= 6;
    }

    private interface Generator {
        long[] generate(int length);
    }
}
