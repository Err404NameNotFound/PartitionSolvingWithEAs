package main;

import java.math.BigInteger;
import java.util.Set;

public interface Solution {
    boolean isNotOptimal();

    boolean isNotOptimal(long maxSteps);

    void updateIfImprovementMultiple(Set<Integer> indexes);

    int get(int index);

    void update(int index);

    void removeSwapping(int[] indexes);

    void printResult();

    void printDistribution();

    void updateIfImprovement(int index);

    boolean lastImproveLessThanXStepsAway(long steps);

    void setValueToOne(int index);

    BigInteger getDif();

    void printResult(long maxSteps, long totalCount);

    void printTotalStats(long maxSteps, long totalCount);

    int getLastBitFlippedCount();

    int getLastBitFlippedTried();

    long getTries();

    long getFlippedBits();

    long getChanges();

    long getTriedFlips();
    BigInteger getTotalSum();
}
