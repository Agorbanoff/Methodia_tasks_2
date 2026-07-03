package bg.academy.outdoor.excel;

import bg.academy.outdoor.output.SportResult;

import java.nio.file.Path;
import java.util.List;

public record ExcelActivityResult(
        String location,
        Path outputPath,
        int resultCount,
        List<SportResult> results
) {
}
