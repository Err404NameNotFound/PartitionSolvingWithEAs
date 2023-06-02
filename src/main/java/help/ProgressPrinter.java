package help;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.Math.max;

public class ProgressPrinter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String clearString = "\b".repeat(47);

    private final long startTime;
    private final boolean byTime;
    private long nextUpdate;
    private long stepSize;
    private final long end;
    private final RingBufferFifo<Long> buffer;
    private final static int BUFFER_SIZE = 100;
    private static final long UPDATE_EVERY_SECOND = 1000;

    private ProgressPrinter(long end, boolean byTime) {
        this.end = end;
        startTime = now();
        buffer = new RingBufferFifo<>(BUFFER_SIZE, startTime);
        this.byTime = byTime;
    }

    public ProgressPrinter(long end, long nextUpdate, boolean byTime) {
        this(end, byTime);
        if (byTime) {
            stepSize = nextUpdate;
            this.nextUpdate = nextUpdate + now();
        } else {
            this.stepSize = max(nextUpdate, 1);
            this.nextUpdate = this.stepSize - 1;
        }
    }

    public ProgressPrinter(long end) {
        this(end, UPDATE_EVERY_SECOND, true);
    }

    public ProgressPrinter(long end, long stepSize) {
        this(end, stepSize, false);
    }

    public void printProgressIfNecessary(long i) {
        if (byTime && Instant.now().toEpochMilli() >= nextUpdate || !byTime && i >= nextUpdate) {
            printProgress(i);
            nextUpdate += stepSize;
        }
    }

    private static long now() {
        return Instant.now().toEpochMilli();
    }

    private void printProgress(long i) {
        ++i;
        long now = now();
        long elapsedTime = now - startTime;
        long expectedTime = (now - buffer.peek()) * (end - i) / buffer.getSize() / stepSize;
        buffer.insert(now);
        System.out.printf(clearString + "%5s%% | elapsed: %s | finished: %s",
                (100.0 * i) / end, timeString(elapsedTime), timeString(expectedTime));
//        System.out.printf("n: %d, i: %d, bs: %d, bv: %d, e1: %d, e2: %d%n", now, i, buffer.getSize(), buffer.peek(), expectedTime, elapsedTime * (end - i) / i);
    }

    public void clearProgressAndPrintElapsedTime() {
        clearProgressAndPrintElapsedTime(startTime);
    }

    private static String timeString(long epochSeconds) {
        return formatter.format(LocalDateTime.ofEpochSecond(epochSeconds / 1000, 0, ZoneOffset.UTC));
    }

    public static void clearProgressAndPrintElapsedTime(long start) {
        System.out.printf(clearString + "Elapsed time: %s%n",
                timeString(now() - start));
    }
}
