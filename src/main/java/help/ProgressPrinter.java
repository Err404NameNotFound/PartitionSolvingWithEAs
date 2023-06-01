package help;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.Math.max;

public class ProgressPrinter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String clearString = "\b".repeat(45);

    private final long startTime;
    private boolean byTime;
    private long nextUpdate;
    private long stepSize;
    private final long end;
    private final RingBufferFifo<Long> buffer;
    private final static int BUFFER_SIZE = 5;

    public ProgressPrinter(long end) {
        this.end = end;
        startTime = Instant.now().getEpochSecond();
        setUpdateMillis(2000);
        buffer = new RingBufferFifo<>(BUFFER_SIZE, startTime);
    }

    public ProgressPrinter(long end, long stepSize) {
        this.end = end;
        this.byTime = false;
        this.stepSize = max(stepSize, 1);
        nextUpdate = this.stepSize - 1;
        startTime = Instant.now().getEpochSecond();
        buffer = new RingBufferFifo<>(BUFFER_SIZE, startTime);
        System.out.println(startTime + ", " + end);
    }

    public void setUpdateMillis(long millis) {
        byTime = true;
        stepSize = millis;
        nextUpdate = millis + Instant.now().toEpochMilli();
    }

    public void printProgressIfNecessary(long i) {
        if (byTime && Instant.now().toEpochMilli() >= nextUpdate || !byTime && i >= nextUpdate) {
            printProgress(i);
            nextUpdate += stepSize;
        }
    }

    private void printProgress(long i) {
        ++i;
        long now = Instant.now().getEpochSecond();
        long elapsedTime = now - startTime;
        long expectedTime = (now - buffer.peek()) * (end - i) / buffer.getSize() / stepSize;
        buffer.insert(now);
        System.out.printf(clearString + "%3s%% | elapsed: %s | finished: %s",
                (100 * i) / end, timeString(elapsedTime), timeString(expectedTime));
    }

    public void clearProgressAndPrintElapsedTime() {
        clearProgressAndPrintElapsedTime(startTime);
    }

    private static String timeString(long epochSeconds) {
        return formatter.format(LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC));
    }

    public static void clearProgressAndPrintElapsedTime(long start) {
        System.out.printf(clearString + "Elapsed time: %s%n",
                timeString(Instant.now().getEpochSecond() - start));
    }
}
