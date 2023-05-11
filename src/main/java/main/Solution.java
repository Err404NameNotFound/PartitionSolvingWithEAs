package main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static help.Help.intString;
import static help.Help.printFirstAndLastElements;
import static help.Help.swap;

public class Solution {
    private final int[] partition;
    private final long[] values;
    private BigInteger sum;
    private BigInteger optimalValue;
    private BigInteger dif;
    private List<Long> result;
    private BigInteger totalSum;
    private long tries;
    private long changes;
    private long bitsFlipped;
    private long bitsTried;
    private boolean sumIsEven;
    private int lastImprove;

    Solution(long[] values) {
        this.values = values;
        partition = new int[values.length];
        sum = BigInteger.ZERO;
        lastImprove = 0;
    }

    void setOptimalValue(BigInteger optimalValue) {
        this.optimalValue = optimalValue;
        dif = dif(sum);
    }

    void setValue(int index, int value) {
        partition[index] = value;
        if (value == 1) {
            sum = sum.add(BigInteger.valueOf(values[index]));
        } else {
            sum = sum.subtract(BigInteger.valueOf(values[index]));
        }
    }

    void setValueToOne(int index) {
        if (partition[index] != 1) {
            sum = sum.add(BigInteger.valueOf(values[index]));
            partition[index] = 1;
        }
    }

    BigInteger dif(BigInteger sol) {
        return sol.subtract(optimalValue).abs();
    }

    public void updateIfImprovement(int index) {
        ++tries;
        ++lastImprove;
        ++bitsTried;
        int nextPart = 1 - partition[index];
        BigInteger nextSum;
        if (nextPart == 1) {
            nextSum = sum.add(BigInteger.valueOf(values[index]));
        } else {
            nextSum = sum.subtract(BigInteger.valueOf(values[index]));
        }
        BigInteger nextDif = dif(nextSum);
        if (nextDif.compareTo(dif) < 0) {
            dif = nextDif;
            partition[index] = nextPart;
            sum = nextSum;
            ++changes;
            lastImprove = 0;
            ++bitsFlipped;
        }
    }

    public void updateIfImprovementMultiple(Set<Integer> indexes) {
        ++tries;
        if(indexes.size() > 0){
            //TODO Verzerrung des Durchschnittswert entfernen
            ++lastImprove;
            bitsTried += indexes.size();
            BigInteger nextSum = sum.add(BigInteger.ZERO);
            for (Integer i : indexes) {
                if (1 - partition[i] == 1) {
                    nextSum = nextSum.add(BigInteger.valueOf(values[i]));
                } else {
                    nextSum = nextSum.subtract(BigInteger.valueOf(values[i]));
                }
            }
            BigInteger nextDif = dif(nextSum);
            if (nextDif.compareTo(dif) < 0) {
                dif = nextDif;
                for (Integer i : indexes) {
                    partition[i] = 1 - partition[i];
                }
                sum = nextSum;
                ++changes;
                lastImprove = 0;
                bitsFlipped += indexes.size();
            }
        }

    }

    public void update(int index) {
        ++tries;
        ++changes;
        partition[index] = 1 - partition[index];
        if (partition[index] == 1) {
            sum = sum.add(BigInteger.valueOf(values[index]));
        } else {
            sum = sum.subtract(BigInteger.valueOf(values[index]));
        }
        dif = dif(sum);
    }

    boolean isNotOptimal(long maxSteps) {
        if (sum.equals(optimalValue)) {
            return false;
        } else if (!sumIsEven && sum.subtract(optimalValue).equals(BigInteger.ONE)) {
            dif = BigInteger.ZERO;
            return false;
        } else {
            return tries < maxSteps;
        }
    }

    boolean isNotOptimal() {
        if (sum.equals(optimalValue)) {
            return false;
        } else if (!sumIsEven && sum.subtract(optimalValue).equals(BigInteger.ONE)) {
            dif = BigInteger.ZERO;
            return false;
        }
        return true;
    }

    public void printResult() {
        System.out.println("*****************************************");
        System.out.printf("Array size: %s%n", intString(values.length));
        System.out.printf("total sum : %s%n", intString(totalSum));
        if (sumIsEven) {
            System.out.printf("optimum   : %s%n", intString(optimalValue));
        } else {
            System.out.printf("optimum 1 : %s%n", intString(optimalValue));
            System.out.printf("optimum 2 : %s%n", intString(totalSum.subtract(optimalValue)));
        }
        System.out.printf("solution  : %s%n", intString(sum));
        System.out.printf("Difference: %s%n", intString(dif));
        if (dif.equals(BigInteger.ZERO)) {
            System.out.println("Percentage: 100%");
        } else {
            System.out.printf("Percentage: %.15f%%%n", 100 * (1 - dif.doubleValue() / optimalValue.doubleValue()));
        }
        System.out.printf("Tries     : %s%n", intString(tries));
        System.out.printf("Changes   : %s%n", intString(changes));
    }

    void printResult(long maxSteps, long totalCount) {
        printResult();
        printTotalStats(maxSteps, totalCount);
    }

    public void printTotalStats(long maxSteps) {
        printTotalStats(maxSteps, tries);
    }

    public void printTotalStats(long maxSteps, long totalCount) {
        System.out.println("------ total stats ---------");
        System.out.printf("Limit count: %s%n", intString(maxSteps));
        System.out.printf("Total tries: %s%n", intString(totalCount));
        System.out.println("----------------------------");
    }

    @Override
    public String toString() {
        return String.format("Sum: %d, dif: %d, tries: %d, changes: %d", sum, dif, tries, changes);
    }

    public List<Long> result() {
        if (result == null) {
            result = new ArrayList<>();
            for (int i = 0; i < values.length; ++i) {
                if (partition[i] == 1) {
                    result.add(values[i]);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void setInputSum(BigInteger inputSum) {
        totalSum = inputSum;
        sumIsEven = inputSum.mod(BigInteger.TWO).equals(BigInteger.ZERO);
        setOptimalValue(inputSum.divide(BigInteger.TWO));
    }

    public boolean lastImproveLessThanXStepsAway(int steps) {
        return lastImprove <= steps;
    }

    public int get(int i) {
        return partition[i];
    }

    public BigInteger getDif() {
        return dif;
    }

    public void printDistribution() {
        printFirstAndLastElements(partition);
//        System.out.println(Arrays.toString(partition));
    }

    public void removeSwapping(int[] largestK) {
        for (int i = largestK.length - 1; i >= 0; --i) {
            swap(partition, i, largestK[i]);
        }
    }

    public long getTries() {
        return tries;
    }

    public long getFlippedBits() {
        return bitsFlipped;
    }

    public long getTriedFlips() {
        return bitsTried;
    }

    public long getChanges() {
        return changes;
    }
}
