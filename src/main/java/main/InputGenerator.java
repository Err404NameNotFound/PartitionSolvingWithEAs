package main;

import java.util.Arrays;
import java.util.Scanner;

import static help.ArrayHelp.fill;
import static help.ArrayPrinter.printFirstAndLastElements;
import static help.MathHelp.binomialK;
import static help.MathHelp.geometricK;
import static help.MathHelp.powerlawK;
import static help.Printer.printf;
import static help.Printer.println;
import static help.RNG.randomInt;
import static main.Main.DEFAULT_BIGGEST_VALUE;
import static main.Main.DEFAULT_BINOMIAL_SHIFT;
import static main.Main.DEFAULT_LOWEST_VALUE;
import static main.Main.DEFAULT_N;
import static main.Main.DEFAULT_PMUT_PARAM;
import static main.Main.DEFAULT_P_BINOMIAL;
import static main.Main.DEFAULT_P_GEOMETRIC;
import static main.Main.DEFAULT_SUM_COUNT;

public class InputGenerator {
    public static final int READ_FROM_CONSOLE = -1;
    public static final int UNIFORM_INTEGER = 0;
    public static final int UNIFORM_INTERVALL = 1;
    public static final int ONEMAX_ONE = 2;
    public static final int ONEMAX_UNIFORM = 3;
    public static final int TWO_THIRDS = 4;
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
    public final long bottom;
    public final long top;
    public final int sumCount;
    public final long n;
    public final double p;
    public final long expectedValue;
    public final double pMutParam;
    private final Generator generator;
    private final boolean outputConstant;
    private long[] output;

    private InputGenerator(int type, Generator generator) {
        this(type, generator, DEFAULT_LOWEST_VALUE, DEFAULT_BIGGEST_VALUE,
                type == TWO_THIRDS ? 2 : (type == 2 || type == 3 ? 1 : DEFAULT_SUM_COUNT),
                DEFAULT_N,
                type == BINOMIAL_DISTRIBUTED ? DEFAULT_P_BINOMIAL : DEFAULT_P_GEOMETRIC,
                DEFAULT_PMUT_PARAM);
    }


    private InputGenerator(int type, Generator generator, long bottom, long top, int sumCount,
                           long n, double p, double pMutParam) {
        this.type = type;
        this.generator = generator;
        this.bottom = bottom;
        this.top = top;
        this.sumCount = sumCount;
        this.n = n;
        this.p = p;
        this.expectedValue = switch (type) {
            case UNIFORM_INTEGER -> Integer.MAX_VALUE / 2;
            case UNIFORM_INTERVALL -> (top - bottom) / 2;
            case BINOMIAL_DISTRIBUTED -> Math.round(n * p);
            case GEOMETRIC_DISTRIBUTED -> Math.round(1.0 / p);
            case BINOMIAL_DISTRIBUTED_SHIFT -> Math.round(n * p) + DEFAULT_BINOMIAL_SHIFT;
            default -> 0;
        };
        this.pMutParam = pMutParam;
        folder = getFolder(type);
        description = getInputTypeDescription(type);
        outputConstant = type == ONEMAX_ONE
                || type == TWO_THIRDS || type == ALL_ONE_EXCEPT_LAST_X_ELEMENTS;
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
            case UNIFORM_INTEGER -> completeIntRange(length);
            case UNIFORM_INTERVALL -> uniformRandomRange(length, low, high);
            case ONEMAX_ONE -> oneMaxEquivalent(length);
            case ONEMAX_UNIFORM -> oneMaxEquivalentUniformRange(length, low, high);
            case TWO_THIRDS -> lastTwoSumRestOne(length);
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
            case UNIFORM_INTEGER -> "uniform random values from complete int range";
            case UNIFORM_INTERVALL -> "uniform random values from given range";
            case ONEMAX_ONE -> "OneMax equivalent for partition";
            case ONEMAX_UNIFORM ->
                    "uniform random values from given range except last field as sum of all other fields";
            case TWO_THIRDS -> "Carsten Witts worst case input (1/3, 1/3, 1/(3(n-2)), ...)";
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
        return fill(length, (i) -> Math.abs(randomInt()));
    }

    public static long[] uniformRandomRange(int length, int low, int high) {
        return fill(length, (i) -> Math.abs(randomInt(low, high)));
    }

    public static long[] oneMaxEquivalent(int length) {
        long[] ret = new long[length];
        Arrays.fill(ret, 1);
        ret[ret.length - 1] = length - 1;
        return ret;
    }

    public static long[] oneMaxEquivalentUniformRange(int length, int low, int high) {
        long[] ret = fill(length, (a) -> Math.abs(randomInt(low, high)), 0, length - 1);
        ret[ret.length - 1] = Arrays.stream(ret).sum();
        return ret;
    }

    public static long[] lastTwoSumRestOne(int length) {
        long[] ret = new long[length];
        Arrays.fill(ret, 1);
        double oneThird = 1.0 / 3.0;
        double epsilon = 0.3;
        long sum = Math.round((oneThird - epsilon / 4.0) * (length - 2) / (oneThird + epsilon / 2.0));
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
        long[] ret = fill(length, (i) -> Math.abs(randomInt(low, high)), 0, length - sumCount);
        long sum = Arrays.stream(ret).sum();
        sum = sum / (sumCount + 1) - epsilonDif(length, sumCount);
        Arrays.fill(ret, ret.length - sumCount, ret.length, sum);
        return ret;
    }

    public static long[] binomialDistributed(int length, int n, double p) {
        return fill(length, (i) -> binomialK(n, p));
    }

    public static long[] geometricDistributed(int length, double p, long highestVal) {
        return fill(length, (i) -> geometricK(highestVal, p));
    }

    public static long[] binomialDistributedShifted(int length, int n, double p, long shift) {
        return fill(length, (i) -> shift + binomialK(n, p));
    }

    public static long[] powerlawDistributed(int length, double bottom, double top, double n) {
        return fill(length, (i) -> powerlawK(bottom, top, n));
    }

    private static long overlappedValue(double bottom, double top, double pBin, int nBin, double pGeom, double nPmut) {
        return randomInt((int) bottom, (int) top) + binomialK(nBin, pBin) + geometricK((int) top, pGeom)
                + powerlawK(bottom, top, nPmut);
    }

    private static long mixedOrOverlappedValue(int index, double bottom, double top, int nBin, double pBin, double pGeom, double nPmut) {
        return switch (index) {
            case 0 -> randomInt((int) bottom, (int) top);
            case 1 -> binomialK(nBin, pBin);
            case 2 -> geometricK((int) top, pGeom);
            case 3 -> powerlawK(bottom, top, nPmut);
            default -> overlappedValue(bottom, top, pBin, nBin, pGeom, nPmut);
        };
    }

    public static long[] mixed(int length, double bottom, double top, int nBin, double pBin, double pGeom, double nPmut) {
        long[] ret = new long[length];
        for (int i = 0; i < length; ++i) {
            int rgn = randomInt(0, 4);
            ret[i] = mixedOrOverlappedValue(rgn, bottom, top, nBin, pBin, pGeom, nPmut);
        }
        return ret;
    }

    public static long[] overlapped(int length, double bottom, double top, int pBin, double nBin, double pGeom, double nPmut) {
        long[] ret = new long[length];
        for (int i = 0; i < length; ++i) {
            ret[i] = overlappedValue(bottom, top, nBin, pBin, pGeom, nPmut);
        }
        return ret;
    }

    public static long[] mixedAndOverlapped(int length, double bottom, double top, int nBin, double pBin, double pGeom, double nPmut) {
        long[] ret = new long[length];
        for (int i = 0; i < length; ++i) {
            int rgn = randomInt(0, 8);
            ret[i] = mixedOrOverlappedValue(rgn, bottom, top, nBin, pBin, pGeom, nPmut);
        }
        return ret;
    }

    public static InputGenerator create(int type) {
        return new InputGenerator(type, (a) -> generateInput(type, a));
    }

    public static InputGenerator createBinomial(int n, double p) {
        return new InputGenerator(BINOMIAL_DISTRIBUTED, (a) -> binomialDistributed(a, n, p), 0,
                n, 0, n, p, DEFAULT_PMUT_PARAM);
    }

    public static InputGenerator createGeometric(double p, long maxValue) {
        return new InputGenerator(GEOMETRIC_DISTRIBUTED, (a) -> geometricDistributed(a, p, maxValue), 0,
                maxValue, 0, maxValue, p, DEFAULT_PMUT_PARAM);
    }

    public static InputGenerator createUniform(int min, int max) {
        return new InputGenerator(UNIFORM_INTERVALL, (a) -> uniformRandomRange(a, min, max), min,
                max, 0, DEFAULT_N, 1.0 / (max - min), DEFAULT_PMUT_PARAM);
    }

    public static InputGenerator createOneMaxOne() {
        return new InputGenerator(ONEMAX_ONE, InputGenerator::oneMaxEquivalent, 1,
                1, 1, DEFAULT_N, DEFAULT_P_BINOMIAL, DEFAULT_PMUT_PARAM);
    }

    public static InputGenerator createMixed() {
        return create(MIXED);
    }

    public static InputGenerator createMixed(double lowest, double highest, int nBin, double pBin, double pGeom, double nPmut) {
        return new InputGenerator(MIXED, (a) -> mixed(a, lowest, highest, nBin, pBin, pGeom, nPmut), (long) lowest,
                (long) highest, 0, nBin, pBin, nPmut);
    }

    public static InputGenerator createOverlapped(double lowest, double highest, int nBin, double pBin, double pGeom, double nPmut) {
        return new InputGenerator(OVERLAPPED, (a) -> overlapped(a, lowest, highest, nBin, pBin, pGeom, nPmut), (long) lowest,
                (long) highest, 0, nBin, pBin, nPmut);
    }

    public static InputGenerator createMixedAndOverlapped(double lowest, double highest, int nBin, double pBin, double pGeom, double nPmut) {
        return new InputGenerator(MIXED_AND_OVERLAPPED, (a) -> mixedAndOverlapped(a, lowest, highest, nBin, pBin, pGeom, nPmut), (long) lowest,
                (long) highest, 0, nBin, pBin, nPmut);
    }

    public static InputGenerator createPowerlaw(double n, double lowest, double highest) {
        return new InputGenerator(POWERLAW_DISTRIBUTED, (a) -> powerlawDistributed(a, lowest, highest, n), (long) lowest,
                (long) highest, 0, DEFAULT_N, DEFAULT_P_BINOMIAL, n);
    }

    public long[] generate(int length) {
        if (outputConstant) {
            if (output == null || output.length != length) {
                output = generator.generate(length);
            }
            return output;
        }
        return generator.generate(length);
    }

    private String getFolder(int type) {
        return switch (type) {
            case UNIFORM_INTEGER, UNIFORM_INTERVALL -> "UniformIntervall";
            case ONEMAX_ONE, ONEMAX_UNIFORM -> "OneMax";
            case TWO_THIRDS -> "TwoThirds";
            case ALL_ONE_EXCEPT_LAST_X_ELEMENTS -> "MultipleSumsAtEnd_One";
            case ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS -> "MultipleSumsAtEnd_Range";
            case BINOMIAL_DISTRIBUTED -> "Binomial";
            case GEOMETRIC_DISTRIBUTED -> "Geometric";
            case POWERLAW_DISTRIBUTED -> "powerLawDistributed";
            case MIXED -> "Mixed";
            case OVERLAPPED -> "Overlapped";
            case MIXED_AND_OVERLAPPED -> "MixedAndOverlapped";
            default -> "other";
        } + "\\";
    }

    public boolean hasBounds() {
        return type == UNIFORM_INTERVALL || type == ONEMAX_UNIFORM || type == ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS
                || type == POWERLAW_DISTRIBUTED || type == MIXED || type == MIXED_AND_OVERLAPPED;
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

    public static String getName(int type) {
        return switch (type) {
            case UNIFORM_INTEGER -> "uniform integer";
            case UNIFORM_INTERVALL -> "uniform intervall";
            case ONEMAX_ONE -> "OneMax (all one)";
            case ONEMAX_UNIFORM -> "OneMax (uniform)";
            case TWO_THIRDS -> "Worst case input by carsten witt";
            case BINOMIAL_DISTRIBUTED -> "binomial";
            case GEOMETRIC_DISTRIBUTED -> "geometric";
            case POWERLAW_DISTRIBUTED -> "powerlaw";
            case MIXED -> "mixed";
            case OVERLAPPED -> "overlapped";
            case MIXED_AND_OVERLAPPED -> "mixed and overlapped";
            case ALL_ONE_EXCEPT_LAST_X_ELEMENTS -> "multiple sumes (rest one)";
            case ALL_IN_RANGE_EXCEPT_LAST_X_ELEMENTS -> "multiple sums (rest uniform)";
            case BINOMIAL_DISTRIBUTED_SHIFT -> "shifted binomial";
            default -> "unknown type";
        };
    }

    public boolean hasFieldWithSum() {
        return type >= 2 && type <= 6;
    }

    private interface Generator {
        long[] generate(int length);
    }
}
