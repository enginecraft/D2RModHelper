package com.ransom.d2r.util;

import com.ransom.d2r.objects.ParsedErrors;
import com.ransom.d2r.objects.ReportType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ReportUtil {
    public static String generate(String outputDir, List<ParsedErrors> errors, ReportType reportType, String fileName) {
        try {
            Path outputPath = Paths.get(outputDir);
            Path outputReport;
            if (reportType.equals(ReportType.TEXT)) {
                outputReport = outputPath.resolve(fileName + ".txt");
                StringBuilder sb = getDescription();
                sb.append("\n\nResults:");
                errors.forEach(sb::append);
                WriteUtil.writeFile(outputReport, sb.toString());
            } else if (reportType.equals(ReportType.HTML)) {
                outputReport = outputPath.resolve(fileName + ".html");
                HtmlReportUtil.generate(errors, outputReport, getDescription().toString());
            } else {
                return "Error: Unknown report type";
            }

            return "Report written to: " + outputReport.toAbsolutePath();
        }
        catch (Exception e) {
            return "Unable to generate report: " + e.getMessage();
        }
    }

    private static StringBuilder getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("D2R Mod Error Report</h1>\n");
        sb.append("This report lists issues between the extracted D2R expansion and the mod. Click categories/files to expand.</p>\n");
        sb.append("\nReport category details:");
        sb.append("\n\tMissing Headers - Information on if any of the D2R extracted headers (columns) are missing in the mod file");
        sb.append("\n\tUnknown Headers - Information on if any of the mod headers (columns) do not exist in the D2R extracted file");
        sb.append("\n\tMismatched Headers - Information about order of the extracted headers vs the mod headers if they do not match");
        sb.append("\n\tMissing Entries - Information on whether there are any row entries in the D2R files that are not in the mod file, matching based on first column value");
        sb.append("\n\tMismatched Entries - Information on whether there are any row entries in the D2R files that have different values than in the mod file (Green - D2R row, Red - Mod row)</p>");
        return sb;
    }
}
