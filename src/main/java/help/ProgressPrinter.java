package help;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.Math.max;

public class ProgressPrinter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final long startTime;
    private boolean byTime;
    private long nextUpdate;
    private long stepSize;
    private final long end;

    public ProgressPrinter(long end) {
        this.end = end;
        startTime = Instant.now().getEpochSecond();
        setUpdateMillis(2000);
    }

    public ProgressPrinter(long end, long stepSize) {
        this.end = end;
        this.byTime = false;
        this.stepSize = max(stepSize, 1);
        nextUpdate = this.stepSize;
        startTime = Instant.now().getEpochSecond();
    }

    public void setUpdateMillis(long millis) {
        byTime = true;
        stepSize = millis;
        nextUpdate = millis + Instant.now().toEpochMilli();
    }

    public void printProgressIfNecessary(long i) {
        if (byTime && Instant.now().toEpochMilli() >= nextUpdate || !byTime && i>= nextUpdate) {
            printProgress(i);
            nextUpdate += stepSize;
        }
    }

    public void printProgress(long i) {
        printProgress(i, end, startTime);
    }

    public void clearProgressAndPrintElapsedTime(){
        clearProgressAndPrintElapsedTime(startTime);
    }

    public static void printProgress(long current, long end, long startSeconds) {
        ++current;
        long expectedTime = (Instant.now().getEpochSecond() - startSeconds) * (end - current) / current;
        System.out.printf("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%3s%% | finished: %s",
                (100 * current) / end, formatter.format(LocalDateTime.ofEpochSecond(expectedTime, 0, ZoneOffset.UTC)));
    }

    public static void clearProgressAndPrintElapsedTime(long start) {
        System.out.printf("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\bElapsed time: %s%n",
                formatter.format(LocalDateTime.ofEpochSecond(Instant.now().getEpochSecond() - start, 0, ZoneOffset.UTC)));
    }
}
