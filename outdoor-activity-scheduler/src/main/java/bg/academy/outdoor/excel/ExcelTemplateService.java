package bg.academy.outdoor.excel;

import bg.academy.outdoor.config.OutdoorActivityProperties;
import bg.academy.outdoor.config.SportConfig;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ExcelTemplateService {
    private static final List<String> SPORT_COLUMNS = List.of(
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

    private final OutdoorActivityProperties properties;

    public ExcelTemplateService(OutdoorActivityProperties properties) {
        this.properties = properties;
    }

    public byte[] createTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CellStyle headerStyle = headerStyle(workbook);
            createSettingsSheet(workbook, headerStyle);
            createSportsSheet(workbook, headerStyle);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Excel template.", e);
        }
    }

    public boolean createTemplateIfMissing(Path inputPath) {
        if (Files.exists(inputPath)) {
            return false;
        }

        try {
            Path parent = inputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Workbook workbook = new XSSFWorkbook(); OutputStream outputStream = Files.newOutputStream(inputPath)) {
                CellStyle headerStyle = headerStyle(workbook);
                createSettingsSheet(workbook, headerStyle);
                createSportsSheet(workbook, headerStyle);
                workbook.write(outputStream);
            }

            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Excel input template: " + inputPath, e);
        }
    }

    private void createSettingsSheet(Workbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Settings");
        Row header = sheet.createRow(0);
        writeCell(header, 0, "field", headerStyle);
        writeCell(header, 1, "value", headerStyle);

        Row location = sheet.createRow(1);
        location.createCell(0).setCellValue("location");
        location.createCell(1).setCellValue("Sofia");

        Row daysAhead = sheet.createRow(2);
        daysAhead.createCell(0).setCellValue("daysAhead");
        daysAhead.createCell(1).setCellValue(properties.daysAhead());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createSportsSheet(Workbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Sports");
        Row header = sheet.createRow(0);
        for (int index = 0; index < SPORT_COLUMNS.size(); index++) {
            writeCell(header, index, SPORT_COLUMNS.get(index), headerStyle);
        }

        writeSport(sheet.createRow(1), new SportConfig("badminton", "Badminton", 10, 30, 5, 49, true, 2, true, 50));
        writeSport(sheet.createRow(2), new SportConfig("football", "Football", 5, 32, 30, 60, true, 2, true, null));

        for (int index = 0; index < SPORT_COLUMNS.size(); index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private void writeSport(Row row, SportConfig sport) {
        row.createCell(0).setCellValue(sport.name());
        row.createCell(1).setCellValue(sport.displayName());
        row.createCell(2).setCellValue(sport.minTempC());
        row.createCell(3).setCellValue(sport.maxTempC());
        row.createCell(4).setCellValue(sport.maxGustKph());
        row.createCell(5).setCellValue(sport.maxChanceOfRain());
        row.createCell(6).setCellValue(sport.requiresDaylight());
        row.createCell(7).setCellValue(sport.minConsecutiveHours());
        row.createCell(8).setCellValue(sport.preferWeekend());
        if (sport.preferCloudAbove() != null) {
            row.createCell(9).setCellValue(sport.preferCloudAbove());
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private void writeCell(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
