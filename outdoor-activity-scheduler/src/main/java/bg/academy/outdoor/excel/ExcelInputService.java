package bg.academy.outdoor.excel;

import bg.academy.outdoor.config.OutdoorActivityProperties;
import bg.academy.outdoor.config.SportConfig;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExcelInputService {
    private static final String SETTINGS_SHEET = "Settings";
    private static final String SPORTS_SHEET = "Sports";

    private final OutdoorActivityProperties properties;
    private final DataFormatter dataFormatter = new DataFormatter(Locale.ROOT);

    public ExcelInputService(OutdoorActivityProperties properties) {
        this.properties = properties;
    }

    public ExcelActivityRequest read(Path inputPath) {
        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException("Excel input file does not exist: " + inputPath);
        }

        try (Workbook workbook = WorkbookFactory.create(inputPath.toFile())) {
            Map<String, String> settings = readSettings(requiredSheet(workbook, SETTINGS_SHEET));
            List<SportConfig> sports = readSports(requiredSheet(workbook, SPORTS_SHEET));

            if (sports.isEmpty()) {
                throw new IllegalArgumentException("Excel input must define at least one sport in sheet " + SPORTS_SHEET);
            }

            return new ExcelActivityRequest(
                    requiredSetting(settings, "location"),
                    intValue(settings, "daysAhead", properties.daysAhead(), "Settings.daysAhead"),
                    sports
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel input file: " + inputPath, e);
        }
    }

    private Sheet requiredSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Excel input file must contain a sheet named " + sheetName);
        }

        return sheet;
    }

    private Map<String, String> readSettings(Sheet sheet) {
        Map<String, String> values = new LinkedHashMap<>();
        for (Row row : sheet) {
            String field = text(row, 0);
            String value = text(row, 1);

            if (field.isBlank() || field.equalsIgnoreCase("field")) {
                continue;
            }

            values.put(normalize(field), value);
        }

        return values;
    }

    private List<SportConfig> readSports(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("Sports sheet must contain a header row.");
        }

        Map<String, Integer> columns = readHeaderColumns(header);
        List<SportConfig> sports = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (isBlankRow(row)) {
                continue;
            }

            sports.add(readSport(row, columns));
        }

        return sports;
    }

    private Map<String, Integer> readHeaderColumns(Row header) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int cellIndex = 0; cellIndex < header.getLastCellNum(); cellIndex++) {
            String name = text(header, cellIndex);
            if (!name.isBlank()) {
                columns.put(normalize(name), cellIndex);
            }
        }

        List<String> requiredColumns = List.of(
                "name",
                "displayName",
                "minTempC",
                "maxTempC",
                "maxGustKph",
                "maxChanceOfRain",
                "requiresDaylight",
                "minConsecutiveHours",
                "preferWeekend",
                "preferCloudAbove"
        );

        for (String requiredColumn : requiredColumns) {
            if (!columns.containsKey(normalize(requiredColumn))) {
                throw new IllegalArgumentException("Sports sheet is missing required column: " + requiredColumn);
            }
        }

        return columns;
    }

    private SportConfig readSport(Row row, Map<String, Integer> columns) {
        int excelRowNumber = row.getRowNum() + 1;

        return new SportConfig(
                requiredText(row, columns, "name", excelRowNumber),
                requiredText(row, columns, "displayName", excelRowNumber),
                requiredDouble(row, columns, "minTempC", excelRowNumber),
                requiredDouble(row, columns, "maxTempC", excelRowNumber),
                requiredDouble(row, columns, "maxGustKph", excelRowNumber),
                requiredInt(row, columns, "maxChanceOfRain", excelRowNumber),
                requiredBoolean(row, columns, "requiresDaylight", excelRowNumber),
                requiredInt(row, columns, "minConsecutiveHours", excelRowNumber),
                requiredBoolean(row, columns, "preferWeekend", excelRowNumber),
                nullableInt(row, columns, "preferCloudAbove", excelRowNumber)
        );
    }

    private boolean isBlankRow(Row row) {
        if (row == null) {
            return true;
        }

        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
            if (!text(row, cellIndex).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String requiredSetting(Map<String, String> values, String field) {
        String value = values.get(normalize(field));
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Settings." + field + " must not be empty.");
        }

        return value;
    }

    private double requiredDouble(Row row, Map<String, Integer> columns, String field, int excelRowNumber) {
        String value = requiredText(row, columns, field, excelRowNumber);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw validationError(field, excelRowNumber, "must be a number", e);
        }
    }

    private int requiredInt(Row row, Map<String, Integer> columns, String field, int excelRowNumber) {
        String value = requiredText(row, columns, field, excelRowNumber);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw validationError(field, excelRowNumber, "must be a whole number", e);
        }
    }

    private boolean requiredBoolean(Row row, Map<String, Integer> columns, String field, int excelRowNumber) {
        String value = requiredText(row, columns, field, excelRowNumber);
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1")) {
            return true;
        }

        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equals("0")) {
            return false;
        }

        throw validationError(field, excelRowNumber, "must be true or false");
    }

    private Integer nullableInt(Row row, Map<String, Integer> columns, String field, int excelRowNumber) {
        String value = text(row, column(columns, field));
        if (value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw validationError(field, excelRowNumber, "must be blank or a whole number", e);
        }
    }

    private String requiredText(Row row, Map<String, Integer> columns, String field, int excelRowNumber) {
        String value = text(row, column(columns, field));
        if (value.isBlank()) {
            throw validationError(field, excelRowNumber, "must not be empty");
        }

        return value;
    }

    private int intValue(Map<String, String> values, String field, int defaultValue, String label) {
        String value = values.get(normalize(field));
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number.", e);
        }
    }

    private int column(Map<String, Integer> columns, String field) {
        return columns.get(normalize(field));
    }

    private String text(Row row, int cellIndex) {
        if (row == null || cellIndex < 0) {
            return "";
        }

        return dataFormatter.formatCellValue(row.getCell(cellIndex)).trim();
    }

    private IllegalArgumentException validationError(String field, int excelRowNumber, String message) {
        return new IllegalArgumentException("Sports row " + excelRowNumber + " field " + field + " " + message + ".");
    }

    private IllegalArgumentException validationError(
            String field,
            int excelRowNumber,
            String message,
            Exception cause
    ) {
        return new IllegalArgumentException(
                "Sports row " + excelRowNumber + " field " + field + " " + message + ".",
                cause
        );
    }

    private String normalize(String field) {
        return field.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
