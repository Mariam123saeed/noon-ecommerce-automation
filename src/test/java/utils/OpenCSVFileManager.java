package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class OpenCSVFileManager {

    private static final Logger log = LogManager.getLogger(OpenCSVFileManager.class);

    private final String csvFilePath;
    private final List<String> columns = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();

    public OpenCSVFileManager(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        log.info("Reading CSV file: {}", csvFilePath);

        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            List<String[]> all = reader.readAll();
            if (all.isEmpty()) {
                log.error("CSV file is empty: {}", csvFilePath);
                throw new IllegalStateException("CSV file is empty: " + csvFilePath);
            }
            columns.addAll(Arrays.asList(all.get(0)));
            rows.addAll(all.subList(1, all.size()));
            log.info("CSV loaded: {} columns, {} rows from [{}]", columns.size(), rows.size(), csvFilePath);
        } catch (IOException | CsvException e) {
            log.error("Could not read CSV file [{}]", csvFilePath, e);
            throw new IllegalStateException("Could not read CSV file: " + csvFilePath, e);
        }
    }

    public List<String[]> getRows() {
        return new ArrayList<>(rows);
    }
    public List<String> getColumns() {
        return new ArrayList<>(columns);
    }


    public Map<String, List<String>> getColumnsWithData() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
            map.put(columns.get(colIdx), getSpecificColumnData(colIdx));
        }
        return map;
    }


    public String getFirstColumn() {
        if (columns.isEmpty()) {
            log.warn("No columns found in [{}]", csvFilePath);
            return null;
        }
        return columns.get(0);
    }


    public String getLastColumn() {
        if (columns.isEmpty()) {
            log.warn("No columns found in [{}]", csvFilePath);
            return null;
        }
        return columns.get(columns.size() - 1);
    }


    public String getSpecificColumnName(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns.size()) {
            log.warn("Column index {} is out of range (0-{})", columnIndex, columns.size() - 1);
            return null;
        }
        return columns.get(columnIndex);
    }


    public List<String> getSpecificColumnData(String columnName) {
        int idx = columns.indexOf(columnName);
        if (idx == -1) {
            log.warn("Column not found: {}", columnName);
            return Collections.emptyList();
        }
        return getSpecificColumnData(idx);
    }


    public List<String> getSpecificColumnData(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns.size()) {
            log.warn("Column index {} is out of range", columnIndex);
            return Collections.emptyList();
        }
        List<String> data = new ArrayList<>();
        for (String[] row : rows) {
            if (columnIndex < row.length) {
                data.add(row[columnIndex]);
            }
        }
        log.debug("Column '{}' has {} values", columns.get(columnIndex), data.size());
        return data;
    }


    public String getCellData(int rowNum, int columnIndex) {
        if (rowNum < 0 || rowNum >= rows.size()) {
            log.warn("Row {} is out of range (0-{})", rowNum, rows.size() - 1);
            return null;
        }
        String[] row = rows.get(rowNum);
        if (columnIndex < 0 || columnIndex >= row.length) {
            log.warn("Column index {} is out of range for row {}", columnIndex, rowNum);
            return null;
        }
        return row[columnIndex];
    }


    public String getCellData(int rowNum, String columnName) {
        int idx = columns.indexOf(columnName);
        if (idx == -1) {
            log.warn("Column not found: {}", columnName);
            return null;
        }
        return getCellData(rowNum, idx);
    }

    public int getCellCount(String columnName) {
        return getSpecificColumnData(columnName).size();
    }

    public int getCellCount(int columnIndex) {
        return getSpecificColumnData(columnIndex).size();
    }

    public String formatAll(List<String> columns, List<List<String>> ans) {
        int colWidth = "Column".length();
        int wordsWidth = "Word(s)".length();
        List<String> wordsText = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            List<String> subAns = ans.get(i);
            List<String> words = new ArrayList<>(subAns.subList(0, subAns.size() - 1));
            Collections.sort(words);
            String text = String.join(", ", words);
            wordsText.add(text);
            colWidth = Math.max(colWidth, columns.get(i).length());
            wordsWidth = Math.max(wordsWidth, text.length());
        }

        String line = "+-" + "-".repeat(colWidth) + "-+-------+-" + "-".repeat(wordsWidth) + "-+";
        String row = "| %-" + colWidth + "s | %5s | %-" + wordsWidth + "s |%n";

        StringBuilder sb = new StringBuilder("\n Most frequent word(s) per column\n");
        sb.append(line).append("\n");
        sb.append(String.format(row, "Column", "Count", "Word(s)"));
        sb.append(line).append("\n");
        for (int i = 0; i < columns.size(); i++) {
            List<String> subAns = ans.get(i);
            sb.append(String.format(row, columns.get(i), subAns.get(subAns.size() - 1), wordsText.get(i)));
        }
        sb.append(line);
        return sb.toString();
    }


    public List<String> findMaxFrequentWordsInCloumn(List<String> values) {
        Map<String, Integer> counts = new HashMap<>();
        int max = 0;
        for (String line : values) {
            for (String word : line.toLowerCase().trim().split("\\s+")) {
                if (word.isEmpty()) {
                    continue;
                }
                max = Math.max(max, counts.merge(word, 1, Integer::sum));
            }
        }

        List<String> subAns = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == max) {
                subAns.add(entry.getKey());
            }
        }
        subAns.add(String.valueOf(max));
        log.debug("Max frequency {} for {} candidate word(s)", max, subAns.size() - 1);
        return subAns;
    }

    public List<List<String>> maxFrequentWordLogic() {
        List<List<String>> ans = new ArrayList<>();
        List<String> colNames = getColumns();
        for (String colName : colNames) {
            ans.add(findMaxFrequentWordsInCloumn(getSpecificColumnData(colName)));
        }
        log.info(formatAll(colNames, ans));
        return ans;
    }
}