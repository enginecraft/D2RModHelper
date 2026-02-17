package com.ransom.d2r.util;

import com.ransom.d2r.objects.FileInfo;
import com.ransom.d2r.objects.PortalDefinition;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class LevelsUtil {
    public static void generate(
            String extractedDir,
            String outputDir,
            List<PortalDefinition> newPortals,
            double densityMultiplier
    ) throws IOException {
        Path input = Paths.get(extractedDir, "levels.txt");
        Path output = Paths.get(outputDir, "levels.txt");

        List<String[]> rows = ScannerUtil.scanFile(input);
        String[] headers = rows.getFirst();
        Map<String, Integer> colIndex = buildIndex(headers);

        List<String[]> newRows = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i).clone();
            multiplyDensity(row, colIndex, densityMultiplier);
            newRows.add(row);
        }

        for (PortalDefinition portal : newPortals) {
            String[] newRow = new String[headers.length];
            Arrays.fill(newRow, "0");

            newRow[colIndex.get("Name")] = portal.name;
            newRow[colIndex.get("MonLvlEx")] = String.valueOf(portal.normalLevel);
            newRow[colIndex.get("MonLvlEx(N)")] = String.valueOf(portal.nightmareLevel);
            newRow[colIndex.get("MonLvlEx(H)")] = String.valueOf(portal.hellLevel);

            int baseMonDen = portal.monDen >= 0 ? portal.monDen : 3;
            int baseNumMon = portal.numMonsters >= 0 ? portal.numMonsters : 10;

            setIfExists(newRow, colIndex, "MonDen", (int)Math.round(baseMonDen * densityMultiplier));
            setIfExists(newRow, colIndex, "MonDen(N)", (int)Math.round(baseMonDen * densityMultiplier));
            setIfExists(newRow, colIndex, "MonDen(H)", (int)Math.round(baseMonDen * densityMultiplier));
            setIfExists(newRow, colIndex, "NumMon", baseNumMon);
            setIfExists(newRow, colIndex, "NumMon(N)", baseNumMon);
            setIfExists(newRow, colIndex, "NumMon(H)", baseNumMon);

            newRows.add(newRow);
        }

        WriteUtil.writeFile(output, new FileInfo(headers, newRows));
    }

    private static void multiplyDensity(String[] row, Map<String, Integer> idx, double multiplier) {
        setIfExists(row, idx, "MonDen", (int)Math.round(parseInt(row, idx, "MonDen") * multiplier));
        setIfExists(row, idx, "MonDen(N)", (int)Math.round(parseInt(row, idx, "MonDen(N)") * multiplier));
        setIfExists(row, idx, "MonDen(H)", (int)Math.round(parseInt(row, idx, "MonDen(H)") * multiplier));
    }

    private static int parseInt(String[] row, Map<String, Integer> idx, String col) {
        if (idx.containsKey(col)) {
            try {
                return Integer.parseInt(row[idx.get(col)]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static void setIfExists(String[] row, Map<String, Integer> idx, String col, int val) {
        if (idx.containsKey(col)) {
            row[idx.get(col)] = String.valueOf(val);
        }
    }

    private static Map<String, Integer> buildIndex(String[] header) {
        Map<String,Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) map.put(header[i], i);
        return map;
    }
}