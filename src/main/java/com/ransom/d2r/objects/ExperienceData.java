package com.ransom.d2r.objects;


import java.util.List;

public class ExperienceData extends FileInfo {
    public int levelColumnIndex;
    public String[] levelZeroRow;

    public ExperienceData(String[] headers, List<String[]> rows) {
        super(headers, rows);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t", headers));
        rows.forEach(row -> {
            sb.append("\n");
            sb.append(String.join("\t", row));
        });
        return sb.toString();
    }
}
