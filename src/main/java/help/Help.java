package help;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static help.Printer.print;
import static help.Printer.printf;
import static help.Printer.println;

public class Help {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void printProgress(long current, long end, long startSeconds) {
        ++current;
        long expectedTime = (Instant.now().getEpochSecond() - startSeconds) * (end - current) / current;
        printf("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%3s%% | finished: %s",
                (100 * current) / end, formatter.format(LocalDateTime.ofEpochSecond(expectedTime, 0, ZoneOffset.UTC)));
    }

    public static void clearProgressBar(){
        System.out.println("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
    }

    public static void runCancellableTask(Runnable task) {
        runCancellableTask(new Thread(task));
    }

    public static void runCancellableTask(Thread t) {
        t.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!br.ready() && t.isAlive()) {
                Thread.sleep(200);
            }
            if (t.isAlive()) {
                br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (t.isAlive()) {
                t.interrupt();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public interface Array {
        String get(int index);
    }

}
