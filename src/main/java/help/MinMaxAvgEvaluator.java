package help;

public class MinMaxAvgEvaluator {

    private long min;
    private long max;
    private long sum;
    private long count;
    private boolean canBeNegative;
    private long absMin;
    private long absMax;
    private long absSum;

    public MinMaxAvgEvaluator(boolean canBeNegative) {
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        sum = 0;
        count = 0;
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
            System.out.printf("min  : %d%n", min);
            System.out.printf("max  : %d%n", max);
            System.out.printf("sum  : %d%n", sum);
            System.out.printf("avg  : %d%n", sum / count);
            System.out.printf("count: %d%n", count);
            if (canBeNegative) {
                System.out.println("Stats of absolute values:");
                System.out.printf("min  : %d%n", absMin);
                System.out.printf("max  : %d%n", absMax);
                System.out.printf("sum  : %d%n", absSum);
                System.out.printf("avg  : %d%n", absSum / count);
            }
        }

    }
}
