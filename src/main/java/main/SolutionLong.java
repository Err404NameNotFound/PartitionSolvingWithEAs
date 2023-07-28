package main;

import java.math.BigInteger;
import java.util.Set;

public class SolutionLong extends BaseSolution {
    private long sum;
    private long optimalValue;
    private long dif;
    private long totalSum;

    SolutionLong(long[] values) {
        super(values);
        sum = 0;
        lastImprove = 0;
    }

    SolutionLong(SolutionBigInt solution, long[] values) {
        super(values);
        int[] temp = solution.getPartition();
        System.arraycopy(temp, 0, partition, 0, partition.length);
        totalSum = solution.getTotalSum().longValue();
        sum = solution.getSum().longValue();
        optimalValue = solution.getOptimalValue().longValue();
        dif = solution.getDif().longValue();
        result = solution.result();
        tries = solution.getTries();
        changes = solution.getChanges();
        bitsFlipped = solution.getFlippedBits();
        bitsTried = solution.getTriedFlips();
        sumIsEven = totalSum % 2 == 0;
        lastImprove = solution.getLastImprove();
        lastBitFlippedCount = solution.getLastBitFlippedCount();
        lastBitFlippedTried = solution.getLastBitFlippedTried();
    }

    void setOptimalValue(long optimalValue) {
        this.optimalValue = optimalValue;
        dif = dif(sum);
    }

    void setValue(int index, int value) {
        partition[index] = value;
        if (value == 1) {
            sum = sum + values[index];
        } else {
            sum = sum - values[index];
        }
    }

    public void setValueToOne(int index) {
        if (partition[index] != 1) {
            sum += values[index];
            partition[index] = 1;
        }
    }

    long dif(long sol) {
        return Math.abs(sol - optimalValue);
    }

    public void updateIfImprovement(int index) {
        ++tries;
        ++lastImprove;
        ++bitsTried;
        int nextPart = 1 - partition[index];
        long nextSum = sum + nextPart == 1 ? values[index] : -values[index];
        long nextDif = dif(nextSum);
        if (nextDif < dif) {
            dif = nextDif;
            partition[index] = nextPart;
            sum = nextSum;
            ++changes;
            lastImprove = 0;
            ++bitsFlipped;
            if (index == partition.length - 1) {
                ++lastBitFlippedCount;
            }
        }
        if (index == partition.length - 1) {
            ++lastBitFlippedTried;
        }
    }

    public void updateIfImprovementMultiple(Set<Integer> indexes) {
        ++tries;
        if (indexes.size() > 0) {
            //TODO Verzerrung des Durchschnittswert entfernen
            ++lastImprove;
            bitsTried += indexes.size();
            long nextSum = sum;
            for (Integer i : indexes) {
                nextSum = nextSum + 1 - partition[i] == 1 ? values[i] : -values[i];
            }
            long nextDif = dif(nextSum);
            if (nextDif < dif) {
                dif = nextDif;
                for (Integer i : indexes) {
                    partition[i] = 1 - partition[i];
                }
                sum = nextSum;
                ++changes;
                lastImprove = 0;
                bitsFlipped += indexes.size();
                if (indexes.contains(partition.length - 1)) {
                    ++lastBitFlippedCount;
                }
            }
        }
        if (indexes.contains(partition.length - 1)) {
            ++lastBitFlippedTried;
        }
    }

    public void update(int index) {
        ++tries;
        ++changes;
        partition[index] = 1 - partition[index];
        if (partition[index] == 1) {
            sum = sum + values[index];
        } else {
            sum = sum - values[index];
        }
        dif = dif(sum);
    }

    public boolean isNotOptimal(long maxSteps) {
        if (sum == optimalValue) {
            return false;
        } else if (!sumIsEven && Math.abs(sum - optimalValue) == 1) {
            dif = 0;
            return false;
        } else {
            return tries < maxSteps;
        }
    }

    public boolean isOptimal() {
        return !isNotOptimal();
    }

    public boolean isNotOptimal() {
        if (sum == optimalValue) {
            return false;
        } else if (Math.abs(sum - optimalValue) == 1) {
            dif = 0;
            return false;
        }
        return true;
    }

    public void printResult() {
        System.out.println("*****************************************");
        System.out.printf("Array size: %,d%n", values.length);
        System.out.printf("total sum : %,d%n", totalSum);
        if (sumIsEven) {
            System.out.printf("optimum   : %,d%n", optimalValue);
        } else {
            System.out.printf("optimum 1 : %,d%n", optimalValue);
            System.out.printf("optimum 2 : %,d%n", totalSum - optimalValue);
        }
        System.out.printf("solution  : %,d%n", sum);
        System.out.printf("Difference: %,d%n", dif);
        if (dif == 0) {
            System.out.println("Percentage: 100%");
        } else {
            System.out.printf("Percentage: %.15f%%%n", 100 * (1.0 - (double) dif / optimalValue));
        }
        System.out.printf("Tries     : %,d%n", tries);
        System.out.printf("Changes   : %,d%n", changes);
    }

    public void printResult(long maxSteps, long totalCount) {
        printResult();
        printTotalStats(maxSteps, totalCount);
    }

    @Override
    public String toString() {
        return String.format("Sum: %d, dif: %d, tries: %d, changes: %d", sum, dif, tries, changes);
    }

    public void setInputSum(long inputSum) {
        totalSum = inputSum;
        sumIsEven = inputSum % 2 == 0;
        setOptimalValue(inputSum / 2);
    }

    public BigInteger getDif() {
        return BigInteger.valueOf(dif);
    }

    public BigInteger getTotalSum() {
        return BigInteger.valueOf(totalSum);
    }
}
