package com.ransom.d2r.objects;

public class ReportInfo {
    public static final String TITLE = "D2R Mod Error Report";
    public static final String SUBTITLE = "This report lists issues between the extracted D2R expansion and the mod. Click categories/files to expand.";
    public static final String DESCRIPTION =
        "Report category details:" +
        "\n\tMissing Headers - Information on if any of the D2R extracted headers (columns) are missing in the mod file" +
        "\n\tUnknown Headers - Information on if any of the mod headers (columns) do not exist in the D2R extracted file" +
        "\n\tMismatched Headers - Information about order of the extracted headers vs the mod headers if they do not match" +
        "\n\tMissing Entries - Information on whether there are any row entries in the D2R files that are not in the mod file, matching based on first column value" +
        "\n\tMismatched Entries - Information on whether there are any row entries in the D2R files that have different values than in the mod file (Green - D2R row, Red - Mod row)";

    public static final String RESULTS = "Results:";
}
