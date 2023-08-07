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
            "binomial",
            "geometric",
            "uniform",
            "powerlaw",
            "onemax",
            "twoThirds",
//            "mixed",
//            "overlapped",
            "mixedAndOverlapped"
    };

    private static final String[] CAPTIONS = new String[]{
            "Number of runs where the algorithms did not find a perfect partition",
            "Average runtime of runs returning a perfect partition",
            "Average runtime of all runs"
    };

    private static final String[] RESULTS_FILE_NAMES = new String[]{
            "rls_compare.txt", "ea_compare.txt", "pmut_compare.txt", "best.txt"
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

    private static String fixAlgoParams(String text) {
        text = text.replace("/n", "$/n$");
        text = text.replace("-1.", "1.");
        text = text.replace("-2.", "2.");
        text = text.replace("-3.", "3.");
        return text;
    }

    private static String convertTxtFileToLatexText(String text) {
        text = text.replace("\r\n", "\n");
        text = text.substring(text.indexOf("algo type"), text.indexOf("---------\nRLS     ->"));
        text = text.replaceFirst("fails(;\\s*\\d?\\d\\.?\\d*)+\\n", "");
        text = text.replaceFirst("avg fail dif(;\\s*\\-1)+\\n", "");
        int totalAvgIndex = text.indexOf("total avg count;") + 16;
        int indexAvg = text.indexOf("\n", totalAvgIndex);
        String totalAvg = text.substring(totalAvgIndex, indexAvg).trim();
        indexAvg += 16;
        String avg = text.substring(indexAvg, text.indexOf("\n", indexAvg)).trim();
        if (totalAvg.equals(avg)) {
            text = text.replaceFirst("total avg count;\\s*" + totalAvg + "\\n", "");
        }
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
        text = text.replace("RLS-R", "\\RLSR[s]");
        text = text.replace("RLS-N", "\\RLSN[b]");
        text = text.replace("r=", "s=");
        text = text.replace("n=", "b=");
        text = fixAlgoParams(text);
        text = text.replace("EA&", "EA-SM&");
//        text = text.replace("EA-SM", "(1+1) EA");
//        text = text.replace("EA-SM", "EA");
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
        boolean withCaption = false;
        String text = readFileFromPath(path);
        String tableStart = "\\begin{tabular}[h]{" + "c".repeat(getTableLengthForCSVFile(text, ';')) + "}\n";
        String tableEnd = "\\\\\n\\end{tabular}\n";
        if (withCaption) {
            tableStart = "\\begin{table}[h]\n\\caption{%s}\n" + tableStart;
            tableEnd = tableEnd + "\\end{table}\n";
        }
        text = text.trim();
        text = text.replace(";", "&");
        String firstLine = text.substring(0, text.indexOf('\n'));
        text = text.replace("\n", "\\\\\n");
        text = text.replace("\\\\\n\\\\\n", tableEnd + "\n" + tableStart);
        text = text.replace(firstLine + "\\\\", "input size" + firstLine + "\\\\\\hline");
        text = text.replaceFirst("input size", "fails in 1000 runs");
        text = text.replaceFirst("input size", "avg");
        text = text.replaceFirst("input size", "total avg");
        text = text.replace("RLS-R (2)", "\\RLSR[2]");
        text = text.replace("RLS-R (3)", "\\RLSR[3]");
        text = text.replace("RLS-R (4)", "\\RLSR[4]");
        text = text.replace("RLS-N (2)", "\\RLSR[2]");
        text = text.replace("RLS-N (3)", "\\RLSN[3]");
        text = text.replace("RLS-N (4)", "\\RLSN[4]");
        text = fixAlgoParams(text);
        text = tableStart + text + tableEnd;
        if (withCaption) {
            text = String.format(text, CAPTIONS[0], CAPTIONS[1], CAPTIONS[2]);
        }
        return text;
    }

    public static void printAllTables() {
        printSingleNTablesFromTo(PATH_THESIS + "\\data", RESULTS_FILE_NAMES, PATH_THESIS + "\\tables");
        printMultipleNTablesFromTo(PATH_THESIS + "\\data", PATH_THESIS + "\\tables");
        updatePowerlaw2_75EATable2();
    }

    private static void updatePowerlaw2_75EATable2() {
        String resultsFileName = "ea_compare2_75.txt";
        startFilePrinting(PATH_THESIS + "tables\\powerlaw\\" + resultsFileName.replace(".txt,", ".tex"), true);
        printTable(PATH_THESIS + "data\\powerlaw\\" + resultsFileName, false);
        stopWritingToFile();
    }

    public static void printSingleNTablesFromTo(String pathRead, String[] fileNames, String pathWrite) {
        for (String folder : FOLDERS) {
            String folderRead = pathRead + "\\" + folder + "\\";
            String folderWrite = pathWrite + "\\" + folder + "\\";
            for (int i = 0; i < RESULTS_FILE_NAMES.length; i++) {
                String resultsFileName = RESULTS_FILE_NAMES[i];
                startFilePrinting(folderWrite + resultsFileName.replace(".txt", ".tex"), true);
                printTable(folderRead + fileNames[i], resultsFileName.contains("pmut"));
                stopWritingToFile();
            }
        }
    }

    public static void printMultipleNTablesFromTo(String pathRead, String pathWrite) {
        String[] tableNamesDetail = new String[]{"multipleN_fails.tex", "multipleN_avg.tex", "multipleN_totalAvg.tex"};
        for (String folder : FOLDERS) {
            String folderRead = pathRead + "\\" + folder + "\\";
            String folderWrite = pathWrite + "\\" + folder + "\\";
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

    public static void renameFilesToFitLatex(String basePath, String[] filesNames) {
        if (filesNames.length != RESULTS_FILE_NAMES.length) {
            throw new IllegalArgumentException();
        }
        for (String folder : FOLDERS) {
            String path = basePath + folder + "\\";
            for (int i = 0; i < RESULTS_FILE_NAMES.length; i++) {
                try {
                    File oldF = new File(path + filesNames[i]);
                    File newF = new File(path + RESULTS_FILE_NAMES[i]);
                    oldF.renameTo(newF);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
