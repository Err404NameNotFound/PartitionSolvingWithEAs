package main;

import java.math.BigInteger;
import java.util.Set;

public class SolutionBigInt extends BaseSolution {
    private BigInteger sum;
    private BigInteger optimalValue;
    private BigInteger dif;
    private BigInteger totalSum;

    SolutionBigInt(long[] values) {
        super(values);
        sum = BigInteger.ZERO;
    }

    void setValue(int index, int value) {
        partition[index] = value;
        if (value == 1) {
            sum = sum.add(BigInteger.valueOf(values[index]));
        } else {
            sum = sum.subtract(BigInteger.valueOf(values[index]));
        }
    }

    public void setValueToOne(int index) {
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
            sum = sum.add(BigInteger.valueOf(values[index]));
        } else {
            sum = sum.subtract(BigInteger.valueOf(values[index]));
        }
        dif = dif(sum);
    }

    public boolean isNotOptimal(long maxSteps) {
        if (sum.equals(optimalValue)) {
            return false;
        } else if (!sumIsEven && sum.subtract(optimalValue).equals(BigInteger.ONE)) {
            dif = BigInteger.ZERO;
            return false;
        } else {
            return tries < maxSteps;
        }
    }

    public boolean isOptimal() {
        return !isNotOptimal();
    }

    public boolean isNotOptimal() {
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
        System.out.printf("Array size: %,d%n", values.length);
        System.out.printf("total sum : %,d%n", totalSum);
        if (sumIsEven) {
            System.out.printf("optimum   : %,d%n", optimalValue);
        } else {
            System.out.printf("optimum 1 : %,d%n", optimalValue);
            System.out.printf("optimum 2 : %,d%n", totalSum.subtract(optimalValue));
        }
        System.out.printf("solution  : %,d%n", sum);
        System.out.printf("Difference: %,d%n", dif);
        if (dif.equals(BigInteger.ZERO)) {
            System.out.println("Percentage: 100%");
        } else {
            System.out.printf("Percentage: %.15f%%%n", 100 * (1 - dif.doubleValue() / optimalValue.doubleValue()));
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

    public void setInputSum(BigInteger inputSum) {
        totalSum = inputSum;
        sumIsEven = inputSum.mod(BigInteger.TWO).equals(BigInteger.ZERO);
        setOptimalValue(inputSum.divide(BigInteger.TWO));
    }

    public BigInteger getDif() {
        return dif;
    }

    public BigInteger getTotalSum() {
        return totalSum;
    }

    public BigInteger getSum() {
        return sum;
    }

    public BigInteger getOptimalValue() {
        return optimalValue;
    }

    void setOptimalValue(BigInteger optimalValue) {
        this.optimalValue = optimalValue;
        dif = dif(sum);
    }
}
