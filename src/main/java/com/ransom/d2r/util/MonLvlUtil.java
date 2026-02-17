package com.ransom.d2r.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MonLvlUtil {

    public static final int MAX_LEVEL = 127;
    public static final int VANILLA_PRESERVE_UNTIL = 85;

    private static final double NORMAL_MULT = 1.0;
    private static final double NM_MULT     = 1.8;
    private static final double HELL_MULT   = 2.7;

    private static final double HP_K = 2.2;
    private static final double HP_ALPHA = 0.018;

    private static final double DM_K = 2.6;
    private static final double DM_ALPHA = 0.016;

    private static final double XP_K = 2.0;
    private static final double XP_ALPHA = 0.020;

    public static void buildMonLvlFile(String extractedDir, String outputDir) throws IOException {

        Path input = Paths.get(extractedDir, "monlvl.txt");
        Path output = Paths.get(outputDir, "monlvl.txt");

        List<String[]> rows = ReaderUtil.readTabFile(input);
        String[] header = rows.getFirst();

        Map<String, Integer> colIndex = buildIndex(header);

        List<String[]> newRows = new ArrayList<>();
        newRows.add(header);

        // Preserve vanilla 1–85 exactly
        for (int i = 1; i < rows.size(); i++) {
            int level = Integer.parseInt(rows.get(i)[0]);
            if (level <= VANILLA_PRESERVE_UNTIL) {
                newRows.add(rows.get(i));
            }
        }

        String[] refRow = rows.get(VANILLA_PRESERVE_UNTIL);
        double refHP = Double.parseDouble(refRow[colIndex.get("HP")]);
        double refDM = Double.parseDouble(refRow[colIndex.get("DM")]);
        double refXP = Double.parseDouble(refRow[colIndex.get("XP")]);

        for (int level = VANILLA_PRESERVE_UNTIL + 1; level <= MAX_LEVEL; level++) {

            String[] newRow = new String[header.length];
            newRow[0] = String.valueOf(level);

            double hp = tapered(level, refHP, HP_K, HP_ALPHA);
            double dm = tapered(level, refDM, DM_K, DM_ALPHA);
            double xp = tapered(level, refXP, XP_K, XP_ALPHA);

            fillTriplet(newRow, colIndex, "HP", hp);
            fillTriplet(newRow, colIndex, "DM", dm);
            fillTriplet(newRow, colIndex, "XP", xp);

            // Keep AR & AC modest linear scaling
            double ar = level * 12;
            double ac = level * 10;

            fillTriplet(newRow, colIndex, "TH", ar);
            fillTriplet(newRow, colIndex, "AC", ac);

            // Per-level bonuses (gentle)
            fillTriplet(newRow, colIndex, "L-HP", hp * 0.03);
            fillTriplet(newRow, colIndex, "L-DM", dm * 0.025);
            fillTriplet(newRow, colIndex, "L-XP", xp * 0.02);
            fillTriplet(newRow, colIndex, "L-TH", ar * 0.02);
            fillTriplet(newRow, colIndex, "L-AC", ac * 0.02);

            newRows.add(newRow);
        }

        write(output, newRows);
    }

    private static double tapered(int level, double ref, double k, double alpha) {
        double f = level / (1.0 + alpha * level);
        return ref * Math.exp(k * f / 100.0);
    }

    private static void fillTriplet(String[] row, Map<String,Integer> idx, String prefix, double baseVal) {

        set(row, idx, prefix, baseVal * NORMAL_MULT);
        set(row, idx, prefix + "(N)", baseVal * NM_MULT);
        set(row, idx, prefix + "(H)", baseVal * HELL_MULT);
    }

    private static void set(String[] row, Map<String,Integer> idx, String col, double val) {
        if (idx.containsKey(col)) {
            row[idx.get(col)] = String.valueOf((int)Math.round(val));
        }
    }

    private static Map<String,Integer> buildIndex(String[] header) {
        Map<String,Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], i);
        }
        return map;
    }

    private static void write(Path output, List<String[]> rows) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(output)) {
            for (String[] row : rows) {
                bw.write(String.join("\t", row));
                bw.newLine();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        buildMonLvlFile(
                "C:\\Users\\spaul\\git\\D2RModHelper\\extracted-data\\data\\global\\excel",
                "."
        );
    }
}

