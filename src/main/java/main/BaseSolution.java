package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static help.ArrayHelp.swap;
import static help.ArrayPrinter.printFirstAndLastElements;

public abstract class BaseSolution implements Solution {
    protected final int[] partition;
    protected final long[] values;
    protected List<Long> result;
    protected long tries;
    protected long changes;
    protected long bitsFlipped;
    protected long bitsTried;
    protected boolean sumIsEven;
    protected int lastImprove;
    protected int lastBitFlippedCount;
    protected int lastBitFlippedTried;

    BaseSolution(long[] values) {
        this.values = values;
        this.partition = new int[values.length];
    }

    public boolean isOptimal() {
        return !isNotOptimal();
    }

    public void printResult(long maxSteps, long totalCount) {
        printResult();
        printTotalStats(maxSteps, totalCount);
    }

    public void printTotalStats(long maxSteps) {
        printTotalStats(maxSteps, tries);
    }

    public void printTotalStats(long maxSteps, long totalCount) {
        System.out.println("------ total stats ---------");
        System.out.printf("Limit count: %,d%n", maxSteps);
        System.out.printf("Total tries: %,d%n", totalCount);
        System.out.println("----------------------------");
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

    public boolean lastImproveLessThanXStepsAway(long steps) {
        return lastImprove <= steps;
    }

    public int get(int i) {
        return partition[i];
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

    public int[] getPartition() {
        return partition;
    }

    public int getLastBitFlippedCount() {
        return lastBitFlippedCount;
    }

    public int getLastBitFlippedTried() {
        return lastBitFlippedTried;
    }

    public int getLastImprove() {
        return lastImprove;
    }
}
