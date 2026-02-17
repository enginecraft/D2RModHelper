package com.ransom.d2r.objects;

import java.util.List;

public class FileInfo {
    public final String[] headers;
    public final List<String[]> rows;

    public FileInfo(String[] headers, List<String[]> rows) {
        this.headers = headers;
        this.rows = rows;
    }
}
