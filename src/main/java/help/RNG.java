package help;

public class RNG {

    private static final MersenneTwisterFast RNG = new MersenneTwisterFast();

    public static int randomInt() {
        return RNG.nextInt();
    }

    public static int randomInt(int top) {
        return RNG.nextInt(top);
    }

    public static int randomInt(int bottom, int top) {
        return bottom + RNG.nextInt(top - bottom);
    }

    public static long randomLong() {
        return RNG.nextLong();
    }

    public static long randomLong(long top) {
        return RNG.nextLong(top);
    }

    public static long randomLong(long bottom, long top) {
        return bottom + RNG.nextLong(top - bottom);
    }

    public static double randomDouble() {
        return RNG.nextDouble();
    }

    public static double randomDouble(boolean includeZero, boolean includeOne) {
        return RNG.nextDouble(includeZero, includeOne);
    }

    public static boolean randomBoolean() {
        return RNG.nextBoolean();
    }

    public static boolean randomBoolean(double probability) {
        return RNG.nextBoolean(probability);
    }

    public static long geometricK(double p) {
        long k = 0;
        while (randomDouble() >= p && k < Long.MAX_VALUE) {
            ++k;
        }
        return k;
    }

    public static int binomialK(int n, double p) {
        double log_q = Math.log(1.0 - p);
        int x = 0;
        double sum = 0;
        while (true) {
            sum += Math.log(Math.random()) / (n - x);
            if (sum < log_q || x >= n) {
                return x;
            }
            x++;
        }
    }

    public static int powerlawK(double bottom, double top, double n) {
        double y, x, a, b, e, f;
        y = Math.random();
        a = Math.pow(top, n + 1);
        b = Math.pow(bottom, n + 1);
        e = (1 / (n + 1));
        f = (a - b) * y + b;
        x = Math.pow(f, e);
        return (int) Math.round(x);
    }

    public static double randomDouble(double bottom, double top) {
        double f = Math.random() / Math.nextDown(1.0);
        return bottom * (1.0 - f) + top * f;
    }
}
