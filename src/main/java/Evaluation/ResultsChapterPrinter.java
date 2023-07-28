package Evaluation;

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
            "binomial", "geometric", "uniform", "powerlaw", "onemax",
            "twoThirds", "mixed", "overlapped", "mixedAndOverlapped"
    };

    private static final String[] SECTIONS = new String[]{
            "\\section{Binomial distributed values}",
            "\\section{Geometric distributed values}",
            "\\section{Uniform distributed inputs}",
            "\\section{powerlaw distributed inputs}",
            "\\section{OneMax Equivalent for PARTITION}",
            "\\section{Carsten Witts worst case input}",
            "\\section{Multiple distributions mixed}",
            "\\section{Multiple distributions overlapped}",
            "\\section{Multiple distributions mixed \\& overlapped}",
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

    private static String convertTxtFileToLatexText(String text) {
        text = text.replace("\r\n", "\n");
        text = text.substring(text.indexOf("algo type"), text.indexOf("---------\nRLS     ->"));
        text = text.replaceFirst("fails(;\\s*\\d\\.?\\d*)+\\n", "");
        text = text.replaceFirst("avg fail dif(;\\s*\\-1)+\\n", "");
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
        text = text.replace("RLS-R", "\\RLSR");
        text = text.replace("RLS-N", "\\RLSN");
        text = text.replace("r=", "s=");
        text = text.replace("n=", "b=");
        text = text.replace("EA&", "EA-SM&");
        text = text.replace("EA-SM", "(1+1) EA");
        text = text.replace("/n", "$/n$");
        text = text.replace("-1.", " 1.");
        text = text.replace("-2.", " 2.");
        text = text.replace("-3.", " 3.");
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

    private static String readAndConvertComparisonTable(String path) {
        String text = readFileFromPath(path);
        String tableStart = "\\begin{tabular}[h]{" + "c".repeat(getTableLengthForCSVFile(text, ';')) + "}\n";
        String tableEnd = "\\\\\n\\end{tabular}\n";
        text = text.trim();
        text = text.replace(";", "&");
        String firstLine = text.substring(0, text.indexOf('\n'));
        text = text.replace("\n", "\\\\\n");
        text = text.replace("\\\\\n\\\\\n", tableEnd + "\n" + tableStart);
        text = text.replace(firstLine + "\\\\", "input size" + firstLine + "\\\\\\hline");
        text = text.replaceFirst("input size", "fails");
        text = text.replaceFirst("input size", "avg");
        text = text.replaceFirst("input size", "total avg");
        return tableStart + text + tableEnd;
    }

    public static void printAllTables() {
        String pathWrite = PATH_THESIS + "\\tables";
        String pathRead = PATH_THESIS + "\\data";
        String[] tableNamesDetail = new String[]{"multipleN_fails.tex", "multipleN_avg.tex", "multipleN_totalAvg.tex"};
        for (int i = 0; i < SECTIONS.length; ++i) {
            String folderRead = pathRead + "\\" + FOLDERS[i] + "\\";
            String folderWrite = pathWrite + "\\" + FOLDERS[i] + "\\";
            for (String resultsFileName : RESULTS_FILE_NAMES) {
                startFilePrinting(folderWrite + resultsFileName.replace(".txt", ".tex"), true);
                printTable(folderRead + resultsFileName, resultsFileName.contains("pmut"));
                stopWritingToFile();
            }
            String table;
            try {
                table = readAndConvertComparisonTable(folderRead + "multipleN.csv");
            } catch (Exception e) {
                table = null;
            }
            if (table != null) {
                String[] tables = table.split("\n\n");
                for (int e = 0; e < tableNamesDetail.length; ++e) {
                    startFilePrinting(folderWrite + tableNamesDetail[e], true);
                    println(tables[e]);
                    stopWritingToFile();
                }
            }
        }
    }
}
