package help;

import main.Solver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import static help.Printer.print;
import static help.Printer.printf;
import static help.Printer.println;

public class Help {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    static Random random = new Random();

    public static String intString(int val) {
        return NumberFormat.getNumberInstance().format(val);
    }

    public static String intString(long val) {
        return NumberFormat.getNumberInstance().format(val);
    }

    public static String intString(BigInteger val) {
        return NumberFormat.getNumberInstance().format(val);
    }

    public static int[] findLargestK(long[] arr, int k) {
        Queue<Tuple> queue = new PriorityQueue<>(Tuple::compareTo);

        for (int i = 0; i < arr.length; ++i) {
            if (queue.size() == k && queue.peek().value < arr[i]) {
                queue.remove();
            }
            if (queue.size() < k) {
                queue.add(new Tuple(i, arr[i]));
            }
        }
        int[] indexes = new int[k];
        for (int i = k - 1; i >= 0; --i) {
            indexes[i] = queue.remove().index;
        }
        return indexes;
    }

    public static void swap(long[] values, int i, int j) {
        long help = values[i];
        values[i] = values[j];
        values[j] = help;
    }

    public static void swap(int[] values, int i, int j) {
        int help = values[i];
        values[i] = values[j];
        values[j] = help;
    }

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

    public static void printProgress(long current, long end, long startSeconds) {
        ++current;
        long expectedTime = (Instant.now().getEpochSecond() - startSeconds) * (end - current) / current;
        printf("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%3s%% | finished: %s",
                (100 * current) / end, formatter.format(LocalDateTime.ofEpochSecond(expectedTime, 0, ZoneOffset.UTC)));
    }

    public static void clearProgressBar(){
        System.out.println("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
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
        printResult(solvers, digits);
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

    public static void printResult(Solver[] solvers, int digits) {
        printResult(i -> solvers[i].description, solvers.length, digits);
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

    public static void runCancellableTask(Runnable task) {
        runCancellableTask(new Thread(task));
    }

    public static void runCancellableTask(Thread t) {
        t.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!br.ready() && t.isAlive()) {
                Thread.sleep(200);
            }
            if (t.isAlive()) {
                br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (t.isAlive()) {
                t.interrupt();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public interface Array {
        String get(int index);
    }

    private static class Tuple implements Comparable<Tuple> {
        public int index;
        public long value;

        public Tuple(int index, long value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Tuple o) {
            return (int) (value - o.value);
        }
    }
}
