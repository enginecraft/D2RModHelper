package com.ransom.d2r.util;

import com.ransom.d2r.objects.SkillsData;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SkillsUtil {
    public static SkillsData generate(
            String extractedDir,
            String outputDir,
            int requiredLevelOverride,
            int maxLevelOverride
    ) throws IOException {
        Path extracted = Paths.get(extractedDir);
        Path output = Paths.get(outputDir);
        SkillsData data = loadSkillData(extracted, requiredLevelOverride, maxLevelOverride);
        WriteUtil.writeFile(output.resolve("skills.txt"), data);
        return data;
    }

    private static SkillsData loadSkillData(Path extractedDir, int requiredLevelOverride, int maxLevelOverride) throws IOException {
        Path expPath = extractedDir.resolve("skills.txt");
        List<String[]> all = ScannerUtil.scanFile(expPath);
        SkillsData data = new SkillsData(all.getFirst(), new ArrayList<>());

        for (int i = 0; i < data.headers.length; i++) {
            switch (data.headers[i]) {
                case "reqlevel":
                    data.reqLevelColumnIndex = i;
                    break;
                case "maxlvl":
                    data.maxLevelColumnIndex = i;
                    break;
            }
        }

        for (int i = 1; i < all.size(); i++) {
            String[] row = all.get(i);

            Object ref = row[data.reqLevelColumnIndex];
            if (ref != null && !ref.equals("") && requiredLevelOverride > 0) {
                row[data.reqLevelColumnIndex] = "" + requiredLevelOverride;
            }

            ref = row[data.maxLevelColumnIndex];
            if (ref != null && !ref.equals("") && maxLevelOverride > 0) {
                row[data.maxLevelColumnIndex] = "" + maxLevelOverride;
            }
            data.rows.add(row);
        }

        return data;
    }
}
