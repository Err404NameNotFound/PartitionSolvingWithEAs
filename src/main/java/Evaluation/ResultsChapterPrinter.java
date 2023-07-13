package Evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static help.Printer.PATH_THESIS;
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

    private static void printTable(String path, boolean longTable) {
        String text = convertFileWithPath(path);
        String content = "\\begin{tabular}[h]{" +
                "c".repeat(getTableLengthForCSVFile(text)) +
                "}\n" +
                text +
                "\\end{tabular}";
        String before = "\\makebox[\\linewidth]{\n";
        if (longTable) {
            before += "\\scriptsize\n";
        }
        before = before + "\\begin{tabular}{lp{3cm}p{6cm}p{6cm}}\n";
        content = before + content + "\n\\end{tabular}\n}";
        println(content);
    }

    public static void printCompleteEvaluation() {
        startFilePrinting(PATH_THESIS + "chapters/expRes/tables.tex", true);
        createFilesForEvaluation();
        for (int i = 0; i < SECTIONS.length; ++i) {
            println(SECTIONS[i]);
            int remainingTextIndex = 0;
            includeTexFile(i, 0);
            for (int e = 0; e < SUBSECTIONS.length; ++e) {
                println(SUBSECTIONS[e]);
                includeTexFile(i, ++remainingTextIndex);
                println();
                printTable(PATH_THESIS + "data\\" + FOLDERS[i] + "\\" + RESULTS_FILE_NAMES[e], RESULTS_FILE_NAMES[e].contains("pmut"));
                println();
                includeTexFile(i, ++remainingTextIndex);
            }
        }
        stopWritingToFile();
    }

    private static void includeTexFile(int folder, int file) {
        println("\\input{chapters/expRes/" + FOLDERS[folder] + "/" + ADDITIONAL_TEX_FILES[file] + "}");
    }

    private static String convertTxtFileToLatexText(String text) {
        text = text.substring(text.indexOf("algo type"), text.indexOf("---------\nRLS     -> standard RLS"));
        text = text.replace("---------\n", "\\hline");
        text = text.replace("-1;", " -;");
        String tempReplacement = "eofwpfjweofj";
        text = text.replace(",", tempReplacement);
        text = text.replace(".", ",");
        text = text.replace(tempReplacement, ".");
        text = text.replace("-1\n", " -\n");
        text = text.replace(';', '&');
        text = text.replace("\n", "\\\\\n");
        text = text.replace("\\hline", "\\hline\n");
        return text;
    }

    public static String readFileFromPath(String path) {
        try {
            return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertFileWithPath(String path) {
        return convertTxtFileToLatexText(readFileFromPath(path));
    }

    public static int getTableLengthForCSVFile(String file) {
        return getTableLengthForCSVFile(file, '&');
    }
    public static int getTableLengthForCSVFile(String file, char delimiter) {
        int count = 1;
        int index = 0;
        while (index < file.length()) {
            if (file.charAt(index) == '\n') {
                return count;
            } else if (file.charAt(index) == delimiter) {
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

    public static void printComparisonBestTable(String path) {
        String text = readFileFromPath(path);
        String tableStart = "\\begin{tabular}[h]{" + "c".repeat(getTableLengthForCSVFile(text, ';')) + "}\n";
        String tableEnd = "\\\\\n\\end{tabular}\n";
        text = text.trim();
        text = text.replace(";", "&");
        text = text.replace("\n", "\\\\\n");
        text = text.replace("\\\\\n\\\\\n", tableEnd + "\n" + tableStart);
        text = text.replace("&20&50&100&500&1000&5000&10000\\\\", "input size&20&50&100&500&1000&5000&10000\\\\\\hline");
        String content = tableStart + text + tableEnd;
        println(content);
    }
}
