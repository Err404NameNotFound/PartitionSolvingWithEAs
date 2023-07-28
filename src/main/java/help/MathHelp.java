package help;

import java.util.Arrays;
import java.util.OptionalLong;

public class MathHelp {

    public static double log(double value, double base) {
        return java.lang.Math.log(value) / java.lang.Math.log(base);
    }

    public static long nlogn(long x) {
        return Math.round(x * Math.log(x));
    }

    public static long nChooseK(int n, int k) {
        long sum = 1;
        for (int i = 1; i <= k; ++i) {
            sum = sum * (n - i + 1);
            sum /= i;
        }
        return sum;
    }

    public static double nChooseK_double(int n, int k) {
        double sum = 1;
        for (int i = 1; i <= k; ++i) {
            sum = sum * (n - i + 1);
            sum /= i;
        }
        return sum;
    }

    public static long max(long[] array) {
        OptionalLong result = Arrays.stream(array).max();
        if (result.isEmpty()) {
            throw new Error("List was empty and therefore had no max.");
        }
        return result.getAsLong();
    }

    public static long max(NumberArray array, int length) {
        long max = array.get(0);
        for (int i = 1; i < length; ++i) {
            long next = array.get(i);
            if (next > max) {
                max = next;
            }
        }
        return max;
    }

    public static long max(long[]... arrays) {
        long max = max(arrays[0]);
        for (int i = 1; i < arrays.length; ++i) {
            long current = max(arrays[i]);
            if (current > max) {
                max = current;
            }
        }
        return max;
    }

    public interface NumberArray {
        long get(int index);
    }
}
