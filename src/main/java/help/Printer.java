package help;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Printer {

    public static final String linebreak = "\n";
    public static final String PATH_TEXT_FILES = Paths.get("").toAbsolutePath() + "\\src\\main\\java\\textFiles\\";
    public static final String PATH_AUTO_GENERATED = Paths.get("").toAbsolutePath() + "\\src\\main\\java\\textFiles\\autoGeneratedResults\\";
    public static final String PATH_THESIS = Paths.get("").toAbsolutePath() + "\\Thesis\\";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM-HH_mm");
    private static boolean printToConsole = true;
    private static boolean printToFile = false;
    private static BufferedWriter writer;

    public static void startFilePrinting(String file) {
        startFilePrinting(file, false);
    }

    public static void startFilePrinting(String file, boolean delete) {
        try {
            File f = new File(file);
            boolean didNotExist = f.createNewFile();
            if (!didNotExist) {
                if (delete) {
                    try (PrintWriter writer = new PrintWriter(file)) {
                        writer.print("");
                    }
                } else {
                    System.out.printf("WARNING: file %s already existed%n", file);
                }
            }
            writer = new BufferedWriter(new FileWriter(file));
            printToFile = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void stopWritingToFile() {
        if (printToFile) {
            printToFile = false;
            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public static String getTodayAsString() {
        return formatter.format(LocalDateTime.now());
    }

    private static void writeToFile(String s) {
        try {
            writer.append(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pauseFileWriting() {
        printToFile = false;
    }

    public static void resumeFileWriting() {
        if (writer != null) {
            printToFile = true;
        }
    }

    public static boolean isPrintToConsole() {
        return printToConsole;
    }

    public static void setPrintToConsole(boolean value) {
        printToConsole = value;
    }

    public static void print(String s) {
        if (printToConsole) {
            System.out.print(s);
        }
        if (printToFile) {
            writeToFile(s);
        }
    }

    public static void print(int i) {
        print(String.valueOf(i));
    }

    public static void print(long l) {
        print(String.valueOf(l));
    }

    public static void println(String s) {
        if (printToConsole) {
            System.out.println(s);
        }
        if (printToFile) {
            writeToFile(s + linebreak);
        }
    }

    public static void println() {
        if (printToConsole) {
            System.out.println();
        }
        if (printToFile) {
            writeToFile(linebreak);
        }
    }

    public static void printf(String s, Object... args) {
        print(String.format(s, args));
    }

}
