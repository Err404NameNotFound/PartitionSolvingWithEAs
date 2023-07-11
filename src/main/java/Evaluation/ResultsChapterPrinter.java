package Evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static help.Printer.PATH_THESIS;
import static help.Printer.print;
import static help.Printer.println;
import static help.Printer.startFilePrinting;
import static help.Printer.stopWritingToFile;

public class ResultsChapterPrinter {

    private static final String[] FOLDERS = new String[]{
            "binomial", "geometric", "uniform", "onemax",
            "twoThirds", "mixed", "overlapped", "mixedAndOverlapped"
    };
    private static final String[] SECTIONS = new String[]{
            "\\section{Binomial distributed values}",
            "\\section{Geometric distributed values}",
            "\\section{Uniform distributed inputs}",
            "\\section{OneMax Equivalent for PARTITION}",
            "\\section{Carsten Witts worst case input}",
            "\\section{Multiple distributions overlapped}",
            "\\section{Multiple distributions mixed}",
            "\\section{Multiple distributions mixed \\& overlapped}",
    };
    private static final String[] SUBSECTIONS = new String[]{
            "\\subsection{RLS Comparison}",
            "\\subsection{(1+1) EA Comparison}",
            "\\subsection{pmut Comparison}",
            "\\subsection{Comparison of the best variants}"
    };

    private static final String[] RESULTS_FILE_NAMES = new String[]{
            "rls_compare.txt", "ea_compare.txt", "pmut_compare.txt", "best.txt"
    };


    private static final String[] ADDITIONAL_TEX_FILES = new String[]{
            "beforeAll", "beforeRLS", "afterRLS", "beforeEA", "afterEA",
            "beforePmut", "afterPmut", "beforeBest", "afterBest"
    };

    private static void printTable(String path) {
        String text = convertFileWithPath(path);
        String content = "\\begin{tabular}[h]{" +
                "c".repeat(getTableLengthForCSVFile(text)) +
                "}\n" +
                text +
                "\\end{tabular}";
        println(content);
    }

    public static void printCompleteEvaluation() {
        startFilePrinting(PATH_THESIS+"chapters/expRes/tables.tex", true);
        createFilesForEvaluation();
        for (int i = 0; i < SECTIONS.length; ++i) {
            println(SECTIONS[i]);
            int remainingTextIndex = 0;
            includeTexFile(i,0);
            for (int e = 0; e < SUBSECTIONS.length; ++e) {
                println(SUBSECTIONS[e]);
                includeTexFile(i, ++remainingTextIndex);
                printTable(PATH_THESIS + "data\\" + FOLDERS[i] + "\\" + RESULTS_FILE_NAMES[e]);
                includeTexFile(i, ++remainingTextIndex);
            }
        }
        print("test hier steht etwas");
        stopWritingToFile();
    }

    private static void includeTexFile(int folder, int file){
        println("\\input{chapters/expRes/" + FOLDERS[folder] + "/" + ADDITIONAL_TEX_FILES[file] + "}");
    }

    private static String convertTxtFileToLatexText(String text) {
        text = text.substring(text.indexOf("algo type"), text.indexOf("---------\nRLS     -> standard RLS"));
        text = text.replace("---------\n", "\\hline");
        text = text.replace(';', '&');
        text = text.replace("\n", "\\\\\n");
        text = text.replace("\\hline", "\\hline\n");
        return text;
    }

    public static String convertFileWithPath(String path) {
        String text;
        try {
            text = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return convertTxtFileToLatexText(text);
    }

    public static int getTableLengthForCSVFile(String file) {
        int count = 1;
        int index = 0;
        while (index < file.length()) {
            if (file.charAt(index) == '\n') {
                return count;
            } else if (file.charAt(index) == '&') {
                ++count;
            }
            ++index;
        }
        return count;
    }

    public static void createFilesForEvaluation() {
        for (String folder : FOLDERS) {
            for (String file : ADDITIONAL_TEX_FILES) {
                String path = PATH_THESIS + "\\chapters\\expRes\\" + folder + "\\" + file + ".tex";
                try {
                    File f = new File(path);
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }
        }
    }
}