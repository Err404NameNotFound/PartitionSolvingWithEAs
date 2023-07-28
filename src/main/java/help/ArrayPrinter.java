package help;

import main.Solver;

import static help.MathHelp.max;
import static help.Printer.print;
import static help.Printer.printf;
import static help.Printer.println;

public class ArrayPrinter {
    public static void printFirstAndLastElements(int[] arr) {
        printFirstAndLastElements(arr, 4);
    }

    public static void printFirstAndLastElements(long[] arr) {
        printFirstAndLastElements(arr, 4);
    }

    public static void printFirstAndLastElements(int[] arr, int count) {
        print("Input: [");
        for (int i = 0; i < count; ++i) {
            printf("%d, ", arr[i]);
        }
        print("...");
        for (int i = arr.length - count; i < arr.length; ++i) {
            printf(", %d", arr[i]);
        }
        println("]");
    }

    public static void printFirstAndLastElements(long[] arr, int count) {
        print("Input: [");
        for (int i = 0; i < count; ++i) {
            printf("%d, ", arr[i]);
        }
        print("...");
        for (int i = arr.length - count; i < arr.length; ++i) {
            printf(", %d", arr[i]);
        }
        println("]");
    }

    public static void printArray(long[] input) {
        printArray(i -> String.valueOf(input[i]), input.length);
    }

    public static void printArray(int[] input) {
        printArray(i -> String.valueOf(input[i]), input.length);
    }

    public static void printArray(long[] input, int[] sorting) {
        printArray(i -> String.valueOf(input[sorting[i]]), sorting.length);
    }

    public static void printArray(double[] input, int[] sorting) {
        printArray(i -> String.format("%.3f", input[sorting[i]]), sorting.length);
    }

    public static void printArray(String before, long[] result, int digits) {
        print(before);
        printArray(result, digits);
    }

    public static void printArrayWithDecimalPoint(String before, long[] result, int digits) {
        print(before);
        printArrayWithDecimalPoint(result, digits);
    }

    public static void printArray(String before, Solver[] solvers, int digits) {
        print(before);
        printSolverDescription(solvers, digits);
    }

    public static void printArray(String before, long[] result, long[] count, int digits) {
        print(before);
        printArray(result, count, digits);
    }

    public static void printArray(long[] result, int digits) {
        printArray(i -> String.valueOf(result[i]), result.length, digits);
    }

    public static void printArrayWithDecimalPoint(long[] result, int digits) {
        printArray(i -> String.format("%,d", result[i]), result.length, digits);
    }

    public static void printArray(long[] result, long[] count, int digits) {
        printArray(i -> String.format("%.3f", ((double) result[i]) / count[i]), result.length, digits);
    }

    public static void printSolverDescription(Solver[] solvers, int digits) {
        printArray(i -> solvers[i].description, solvers.length, digits);
    }

    public static void printArray(String before, String[] strings, int digits) {
        printArray(before, i -> strings[i], strings.length, digits);
    }

    public static void printArray(String before, Array array, int length, int digits) {
        print(before);
        printArray(array, length, digits);
    }

    public static void printArray(Array array, int length, int digits) {
        printf("%" + digits + "s", array.get(0));
        for (int i = 1; i < length; ++i) {
            printf(";%" + digits + "s", array.get(i));
        }
        println();
    }

    public static void printArray(Array array, int length) {
        printf("%s", array.get(0));
        for (int i = 1; i < length; ++i) {
            printf(";%s", array.get(i));
        }
        println();
    }

    public static long getNeededDigits(long val) {
        if (val == 0) {
            return 1;
        } else if (val < 0) {
            return getNeededDigits(-val);
        }
        val = (long) Math.ceil(Math.log10((double) val)); // without decimal point
        return val + val / 3; // + decimal points
    }

    public static long getNeededDigitsSpaced(long val) {
        return 1 + getNeededDigits(val); // + 1 for spacing
    }

    public static long getNeededDigits(long[]... values) {
        long max = max(values);
        return getNeededDigits(max);
    }

    public static long getNeededDigits(long min, long[]... values) {
        long max = max(values);
        return getNeededDigits(Math.max(max, min));
    }

    public interface Array {
        String get(int index);
    }
}
