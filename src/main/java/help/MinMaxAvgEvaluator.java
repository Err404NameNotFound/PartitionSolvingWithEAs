package help;

import static help.ArrayPrinter.printResult;

public class MinMaxAvgEvaluator {

    private long min;
    private long max;
    private long sum;
    private long count;
    private final boolean canBeNegative;
    private long absMin;
    private long absMax;
    private long absSum;

    public MinMaxAvgEvaluator(boolean canBeNegative) {
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        sum = 0;
        count = 0;
        this.canBeNegative = canBeNegative;
    }

    public void insert(long value) {
        ++count;
        sum += value;
        if (value > max) {
            max = value;
        }
        if (value < min) {
            min = value;
        }
        if (canBeNegative) {
            value = Math.abs(value);
            absSum += value;
            if (value > absMax) {
                absMax = value;
            }
            if (value < absMin) {
                absMin = value;
            }
        }
    }

    public void printEvaluation() {
        if (count == 0) {
            System.out.println("Nothing to evaluate, because count == 0");
        } else {
            System.out.printf("count: %d%n", count);
            System.out.printf("min  : %d%n", min);
            System.out.printf("max  : %d%n", max);
            System.out.printf("sum  : %d%n", sum);
            System.out.printf("avg  : %d%n", sum / count);
            if (canBeNegative) {
                System.out.println("Stats of absolute values:");
                System.out.printf("min  : %d%n", absMin);
                System.out.printf("max  : %d%n", absMax);
                System.out.printf("sum  : %d%n", absSum);
                System.out.printf("avg  : %d%n", absSum / count);
            }
        }
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public long getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }

    public static void printMultipleNonNegative(int digits, MinMaxAvgEvaluator... evaluators) {
        printCount("count: ", digits, evaluators);
        printMin("min  : ", digits, evaluators);
        printMax("max  : ", digits, evaluators);
        printAvg("avg  : ", digits, evaluators);
        printSum("sum  : ", digits, evaluators);
    }

    public static void printMultipleNonNegative(String min, String max, String avg, int digits, MinMaxAvgEvaluator... evaluators) {
        printAvg(avg, digits, evaluators);
        printMax(max, digits, evaluators);
        printMin(min, digits, evaluators);
    }

    private static void printSum(String sum, int digits, MinMaxAvgEvaluator... evaluators) {
        printResult(sum, (i) -> evaluators[i].count == 0 ? "-" : String.format("%,d", evaluators[i].sum), evaluators.length, digits);

    }

    private static void printCount(String count, int digits, MinMaxAvgEvaluator... evaluators) {
        printResult(count, (i) -> evaluators[i].count == 0 ? "-" : String.format("%,d", evaluators[i].count), evaluators.length, digits);
    }

    private static void printMin(String min, int digits, MinMaxAvgEvaluator... evaluators) {
        printResult(min, (i) -> evaluators[i].count == 0 ? "-" : String.format("%,d", evaluators[i].min), evaluators.length, digits);
    }

    private static void printMax(String max, int digits, MinMaxAvgEvaluator... evaluators) {
        printResult(max, (i) -> evaluators[i].count == 0 ? "-" : String.format("%,d", evaluators[i].max), evaluators.length, digits);
    }

    private static void printAvg(String avg, int digits, MinMaxAvgEvaluator... evaluators) {
        printResult(avg, (i) -> evaluators[i].count == 0 ? "-" : String.format("%,d", Math.round((float) evaluators[i].sum / evaluators[i].count)), evaluators.length, digits);
    }
}
