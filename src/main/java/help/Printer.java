package help;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Printer {

    public static boolean printToConsole = true;
    public static boolean printToFile = false;
    public static String linebreak = "\n";

    private static BufferedWriter writer;

    public static void startFilePrinting(String file) {
        try {
            new File(file).createNewFile();
            writer = new BufferedWriter(new FileWriter(file));
            printToFile = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void stopWritingToFile() {
        if(printToFile){
            printToFile = false;
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private static void writeToFile(String s) {
        try {
            writer.append(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setPrintToConsole(boolean value) {
        printToConsole = value;
    }

    public static boolean isPrintToConsole(){
        return printToConsole;
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
