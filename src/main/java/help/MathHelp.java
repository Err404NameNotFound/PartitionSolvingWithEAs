package help;

import java.util.Arrays;
import java.util.OptionalLong;
import java.util.Random;

public class MathHelp {

    private static final Random random = new Random();

    public static double log(double value, double base) {
        return java.lang.Math.log(value) / java.lang.Math.log(base);
    }

    public static long nlogn(long x) {
        return Math.round(x * Math.log(x));
    }

    public static long geometricK(long maxValue, double p) {
        long k = 0;
        while (random.nextDouble() >= p && k < maxValue) {
            ++k;
        }
        return k;
    }

    public static int binomialK(int n, double p) {
        double log_q = Math.log(1.0 - p);
        int x = 0;
        double sum = 0;
        while (true) {
            sum += Math.log(Math.random()) / (n - x);
            if (sum < log_q) {
                return x;
            }
            x++;
        }
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

    public static double randomDouble(double bottom, double top) {
        double f = Math.random() / Math.nextDown(1.0);
        return bottom * (1.0 - f) + top * f;
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

    public static int powerlawK(double bottom, double top, double n) {
        double y, x, a, b, e, f;
        y = Math.random();
        a = Math.pow(top, n + 1);
        b = Math.pow(bottom, n + 1);
        e = (1 / (n + 1));
        f = (a - b) * y + b;
        x = Math.pow(f, e);
        return (int) Math.round(x);
    }

    public interface NumberArray {
        long get(int index);
    }
}
