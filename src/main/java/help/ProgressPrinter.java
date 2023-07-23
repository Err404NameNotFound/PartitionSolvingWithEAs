package help;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.Math.max;

public class ProgressPrinter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String clearString = "\b".repeat(47);
    private final static int BUFFER_SIZE = 60;
    private static final long UPDATE_EVERY_SECOND = 1000;
    private final long startTime;
    private final boolean byTime;
    private final long end;
    private final RingBufferFifo<Long> timeBuffer;
    private final RingBufferFifo<Long> iterationBuffer;
    private long nextUpdate;
    private long stepSize;

    private ProgressPrinter(long end, boolean byTime) {
        this.end = end;
        startTime = now();
        timeBuffer = new RingBufferFifo<>(BUFFER_SIZE, startTime);
        iterationBuffer = new RingBufferFifo<>(BUFFER_SIZE, 0L);
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

    private static long now() {
        return Instant.now().toEpochMilli();
    }

    private static String timeString(long epochSeconds) {
        return formatter.format(LocalDateTime.ofEpochSecond(epochSeconds / 1000, 0, ZoneOffset.UTC));
    }

    public static void clearProgressAndPrintElapsedTime(long start) {
        System.out.printf(clearString + "Elapsed time: %s%n",
                timeString(now() - start));
    }

    public void printProgressIfNecessary(long i) {
        if (byTime && Instant.now().toEpochMilli() >= nextUpdate || !byTime && i >= nextUpdate) {
            printProgress(i);
            nextUpdate += stepSize;
        }
    }

    private void printProgress(long i) {
        ++i;
        long now = now();
        long elapsedTime = now - startTime;
        long expectedTime = (now - timeBuffer.peek()) * (end - i) / (i - iterationBuffer.peek());
        timeBuffer.insert(now);
        iterationBuffer.insert(i);
        System.out.printf(clearString + "%5s%% | elapsed: %s | finished: %s",
                String.format("%.1f", (100.0 * i) / end), timeString(elapsedTime), timeString(expectedTime));
//        System.out.printf("now: %d, i: %d, bufSize: %d, bufVal: %d, exp1: %d, exp2: %d, exp3: %d%n", now, i, timeBuffer.getSize(), timeBuffer.peek(), expectedTime, elapsedTime * (end - i) / i, expectedTime*stepSize);
    }

    public void clearProgressAndPrintElapsedTime() {
        clearProgressAndPrintElapsedTime(startTime);
    }

    public void printElapsedTime() {
        System.out.printf("Elapsed time: %s%n", timeString(now() - startTime));
    }
}
