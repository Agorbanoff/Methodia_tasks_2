package bg.academy.outdoor.excel;

import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ExcelOutputService {
    private static final String RESULTS_SHEET = "Results";
    private static final String PLANNED_ACTIONS_SHEET = "PlannedActions";

    public int write(List<SportResult> results, Path outputPath) {
        return write(results, outputPath, false);
    }

    public int write(List<SportResult> results, Path outputPath, boolean includePlannedActions) {
        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream outputStream = Files.newOutputStream(outputPath)) {
                Sheet sheet = workbook.createSheet(RESULTS_SHEET);
                writeHeader(workbook, sheet);
                int rowIndex = writeRows(sheet, results);
                for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
                    sheet.autoSizeColumn(columnIndex);
                }

                if (includePlannedActions) {
                    writePlannedActions(workbook, results);
                }

                workbook.write(outputStream);
                return rowIndex - 1;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel output file: " + outputPath, e);
        }
    }

    private void writeHeader(Workbook workbook, Sheet sheet) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        Row header = sheet.createRow(0);
        String[] columns = {"sport", "sportName", "date", "interval", "preferred"};
        for (int index = 0; index < columns.length; index++) {
            Cell cell = header.createCell(index);
            cell.setCellValue(columns[index]);
            cell.setCellStyle(style);
        }
    }

    private int writeRows(Sheet sheet, List<SportResult> results) {
        int rowIndex = 1;
        for (SportResult sportResult : results) {
            for (DayResult day : sportResult.days()) {
                for (String interval : day.hours()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(sportResult.sport());
                    row.createCell(1).setCellValue(sportResult.sportName());
                    row.createCell(2).setCellValue(day.date());
                    row.createCell(3).setCellValue(interval);
                    row.createCell(4).setCellValue(day.preferred());
                }
            }
        }

        return rowIndex;
    }

    private void writePlannedActions(Workbook workbook, List<SportResult> results) {
        Sheet sheet = workbook.createSheet(PLANNED_ACTIONS_SHEET);
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        Row header = sheet.createRow(0);
        String[] columns = {"action", "sport", "sportName", "date", "interval", "preferred"};
        for (int index = 0; index < columns.length; index++) {
            Cell cell = header.createCell(index);
            cell.setCellValue(columns[index]);
            cell.setCellStyle(style);
        }

        int rowIndex = 1;
        for (SportResult sportResult : results) {
            for (DayResult day : sportResult.days()) {
                for (String interval : day.hours()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue("notification candidate");
                    row.createCell(1).setCellValue(sportResult.sport());
                    row.createCell(2).setCellValue(sportResult.sportName());
                    row.createCell(3).setCellValue(day.date());
                    row.createCell(4).setCellValue(interval);
                    row.createCell(5).setCellValue(day.preferred());
                }
            }
        }

        for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }
    }
}
