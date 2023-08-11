package main;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SolutionLong extends BaseSolution {
    private final long totalSum;
    private long sum;
    private long optimalValue;
    private long fitness;

    SolutionLong(long[] values){
        super(values);
        sum = 0;
        totalSum = Arrays.stream(values).sum();
        setOptimalValue();
        updateFitness();
    }

    SolutionLong(SolutionBigInt solution, long[] values) {
        super(values);
        int[] temp = solution.getPartition();
        System.arraycopy(temp, 0, partition, 0, partition.length);
        totalSum = solution.getTotalSum().longValue();
        sum = solution.getSum().longValue();
        setOptimalValue();
        updateFitness();
        result = solution.result();
        tries = solution.getTries();
        changes = solution.getChanges();
        bitsFlipped = solution.getFlippedBits();
        bitsTried = solution.getTriedFlips();
        lastImprove = solution.getLastImprove();
        lastBitFlippedCount = solution.getLastBitFlippedCount();
        lastBitFlippedTried = solution.getLastBitFlippedTried();
    }

    private void setOptimalValue(){
        optimalValue = totalSum / 2;
        sumIsEven = totalSum % 2 == 0;
        if (!sumIsEven) {
            optimalValue += 1;
        }
    }

    private void updateFitness() {
        fitness = Math.max(sum, totalSum - sum);
    }

    private long getFitness(long sum) {
        return Math.max(sum, totalSum - sum);
    }

    public void setValueToOne(int index) {
        if (partition[index] != 1) {
            sum += values[index];
            partition[index] = 1;
            updateFitness();
        }
    }

    private void acceptImprovement(long nextFitness, Set<Integer> indexes, long nextSum, boolean lastBitFlipped) {
        fitness = nextFitness;
        for (Integer i : indexes) {
            partition[i] = 1 - partition[i];
        }
        sum = nextSum;
        ++changes;
        lastImprove = 0;
        bitsFlipped += indexes.size();
        if (lastBitFlipped) {
            ++lastBitFlippedCount;
        }
    }

    private void updateFlippedBits(long bitsTriedToFlip, boolean lastBitFlipped) {
        ++tries;
        ++lastImprove;
        bitsTried += bitsTriedToFlip;
        if (lastBitFlipped) {
            ++lastBitFlippedTried;
        }
    }

    public void updateIfImprovement(int index) {
        boolean lastBitFlipped = index == partition.length - 1;
        updateFlippedBits(1, lastBitFlipped);
        long nextSum = sum + (partition[index] == 0 ? values[index] : -values[index]);
        long nextFitness = getFitness(nextSum);
        if (nextFitness < fitness) {
            Set<Integer> indexes = new HashSet<>(1);
            indexes.add(index);
            acceptImprovement(nextFitness, indexes, nextSum, lastBitFlipped);
        }
    }

    public void updateIfImprovementMultiple(Set<Integer> indexes) {
        boolean lastBitFlipped = indexes.contains(partition.length - 1);
        updateFlippedBits(indexes.size(), lastBitFlipped);
        if (indexes.size() > 0) {
            long nextSum = sum;
            for (Integer i : indexes) {
                nextSum = nextSum + (partition[i] == 0 ? values[i] : -values[i]);
            }
            long nextFitness = getFitness(nextSum);
            if (nextFitness < fitness) {
                acceptImprovement(nextFitness, indexes, nextSum, lastBitFlipped);
            }
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
        updateFitness();
    }

    public boolean isNotOptimal(long maxSteps) {
        return isNotOptimal() && tries < maxSteps;
    }

    public boolean isOptimal() {
        return !isNotOptimal();
    }

    public boolean isNotOptimal() {
        return fitness != optimalValue;
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
        System.out.printf("Difference: %,d%n", getDif());
        if (isOptimal()) {
            System.out.println("Percentage: 100%");
        } else {
            System.out.printf("Percentage: %.15f%%%n", 100 * (1.0 - (fitness - optimalValue) / optimalValue));
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
        return String.format("Sum: %d, dif: %d, tries: %d, changes: %d", sum, getDif(), tries, changes);
    }

    public BigInteger getDif() {
        return BigInteger.valueOf(fitness - optimalValue);
    }

    public BigInteger getTotalSum() {
        return BigInteger.valueOf(totalSum);
    }
}
