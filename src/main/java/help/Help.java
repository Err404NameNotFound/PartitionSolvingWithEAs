package help;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Help {

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
//                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void runCancellableTasks(Thread[] threads) {
        for (Thread t : threads) {
            t.start();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!br.ready() && threadAlive(threads)) {
                Thread.sleep(200);
            }
            if (threadAlive(threads)) {
                br.readLine();
            }
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (threadAlive(threads)) {
                for (Thread t : threads) {
                    t.interrupt();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static boolean threadAlive(Thread[] threads) {
        return Arrays.stream(threads).anyMatch(Thread::isAlive);
    }
}
