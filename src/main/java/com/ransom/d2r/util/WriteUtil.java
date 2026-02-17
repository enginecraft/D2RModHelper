package com.ransom.d2r.util;

import com.ransom.d2r.objects.FileInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriteUtil {
    public static void writeFile(Path outputPath, FileInfo fileInfo) throws IOException {
        Files.createDirectories(outputPath.getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(outputPath)) {
            if (fileInfo.headers != null && fileInfo.headers.length != 0) {
                bw.write(String.join("\t", fileInfo.headers));
                bw.newLine();
            }

            if (fileInfo.rows != null) {
                for (String[] row : fileInfo.rows) {
                    bw.write(String.join("\t", row));
                    bw.newLine();
                }
            }
        }
    }

    public static void writeFile(Path outputPath, String data) throws IOException {
        Files.createDirectories(outputPath.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write(data);
        }
    }
}
