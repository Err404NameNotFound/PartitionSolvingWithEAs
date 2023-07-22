package help;

import java.time.Instant;

public class RNG {

    private static final MersenneTwisterFast RNG = new MersenneTwisterFast(Instant.now().toEpochMilli());

    public static int randomInt() {
        return RNG.nextInt();
    }

    public static int randomInt(int top) {
        return RNG.nextInt(top);
    }

    public static int randomInt(int bottom, int top) {
        return bottom + RNG.nextInt(top);
    }

    public static long randomLong(){
        return RNG.nextLong();
    }

    public static long randomLong(long top){
        return RNG.nextLong(top);
    }

    public static long randomLong(long bottom, long top){
        return bottom + RNG.nextLong(top);
    }

    public static double randomDouble(){
        return RNG.nextDouble();
    }
    public static double randomDouble(boolean includeZero, boolean includeOne){
        return RNG.nextDouble(includeZero, includeOne);
    }
    public static boolean randomBoolean(){
        return RNG.nextBoolean();
    }
    public static boolean randomBoolean(double probability){
        return RNG.nextBoolean(probability);
    }
}
