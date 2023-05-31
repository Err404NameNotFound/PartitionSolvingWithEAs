package help;

import main.Solver;

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

    public static void printResult(String before, long[] result, int digits) {
        print(before);
        printResult(result, digits);
    }

    public static void printResultWithDecimalPoint(String before, long[] result, int digits) {
        print(before);
        printResultWithDecimalPoint(result, digits);
    }

    public static void printResult(String before, Solver[] solvers, int digits) {
        print(before);
        printSolverDescription(solvers, digits);
    }

    public static void printResult(String before, long[] result, long[] count, int digits) {
        print(before);
        printResult(result, count, digits);
    }

    public static void printResult(long[] result, int digits) {
        printResult(i -> String.valueOf(result[i]), result.length, digits);
    }

    public static void printResultWithDecimalPoint(long[] result, int digits) {
        printResult(i -> String.format("%,d", result[i]), result.length, digits);
    }

    public static void printResult(long[] result, long[] count, int digits) {
        printResult(i -> String.format("%.3f", ((double) result[i]) / count[i]), result.length, digits);
    }

    public static void printSolverDescription(Solver[] solvers, int digits) {
        printResult(i -> solvers[i].description, solvers.length, digits);
    }

    public static void printResult(String before, String[] strings, int digits) {
        printResult(before, i -> strings[i], strings.length, digits);
    }

    public static void printResult(String before, Array array, int length, int digits) {
        print(before);
        printResult(array, length, digits);
    }

    public static void printResult(Array array, int length, int digits) {
        printf("%" + digits + "s", array.get(0));
        for (int i = 1; i < length; ++i) {
            printf(";%" + digits + "s", array.get(i));
        }
        println();
    }

    public interface Array {
        String get(int index);
    }
}
