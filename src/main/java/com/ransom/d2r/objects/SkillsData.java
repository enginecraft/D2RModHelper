package com.ransom.d2r.objects;


import java.util.List;

public class SkillsData extends FileInfo {
    public int reqLevelColumnIndex;
    public int maxLevelColumnIndex;

    public SkillsData(String[] headers, List<String[]> rows) {
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