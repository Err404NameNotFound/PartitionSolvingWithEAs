package help;

import java.util.PriorityQueue;
import java.util.Queue;

public class ArrayHelp {

    public static long[] cast(int[] input) {
        return fill(input.length, (i) -> input[i]);
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

    public static int[] generateIntArray(int length, NumberGenerator generator) {
        int[] ret = new int[length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = (int) generator.generate(i);
        }
        return ret;
    }

    public static void fill(long[] values, NumberGenerator generator) {
        fill(values, generator, 0, values.length);
    }

    public static void fill(long[] values, NumberGenerator generator, int start, int end) {
        for (int i = start; i < end; ++i) {
            values[i] = generator.generate(i);
        }
    }

    public static <T> void fill(T[] values, ObjectGenerator<T> generator) {
        fill(values, generator, 0, values.length);
    }

    public static <T> void fill(T[] values, ObjectGenerator<T> generator, int start, int end) {
        for (int i = start; i < end; ++i) {
            values[i] = generator.generate(i);
        }
    }

    public static long[] fill(int length, NumberGenerator generator) {
        return fill(length, generator, 0, length);
    }

    public static long[] fill(int length, NumberGenerator generator, int start, int end) {
        long[] ret = new long[length];
        fill(ret, generator, start, end);
        return ret;
    }


    public interface NumberGenerator {
        long generate(int index);
    }

    public interface ObjectGenerator<T> {
        T generate(int index);
    }

    private record Tuple(int index, long value) implements Comparable<Tuple> {

        @Override
        public int compareTo(Tuple o) {
            return (int) (value - o.value);
        }
    }
}
