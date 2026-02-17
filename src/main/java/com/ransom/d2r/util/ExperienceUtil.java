package com.ransom.d2r.util;

import com.ransom.d2r.objects.ExperienceData;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExperienceUtil {
    public static final int MAX_LEVEL_LIMIT = 127;
    public static final BigInteger MAX_XP_PER_LEVEL =  new BigInteger("4000000000");
    public static final BigInteger MIN_VAL = new BigInteger("1");
    public static final int MAX_EXP_RATIO = 1024;

    private static List<BigInteger> getProgression(int maxCount, BigInteger maxVal, BigInteger minVal, int offset, double difficulty, boolean reverse) {
        if (difficulty > 1) difficulty = 1;
        if (difficulty <= 0) difficulty = 0.0001;
        if (maxCount < 1) maxCount = 1;
        else if (maxCount > MAX_LEVEL_LIMIT) maxCount = MAX_LEVEL_LIMIT;
        if (maxVal.compareTo(MIN_VAL) < 0) maxVal = MIN_VAL;
        if (minVal.compareTo(MIN_VAL) < 0) minVal = MIN_VAL;
        if (minVal.compareTo(maxVal) > 0) minVal = maxVal;
        if (offset < 1) offset = 1;
        if (offset > MAX_LEVEL_LIMIT) offset = MAX_LEVEL_LIMIT;

        List<BigInteger> progression = new ArrayList<>();

        BigDecimal initialAmount = new BigDecimal(reverse ? maxVal : minVal)
                .multiply(BigDecimal.valueOf(difficulty));

        BigDecimal finalAmount = new BigDecimal(reverse ? minVal : maxVal)
                .multiply(BigDecimal.valueOf(difficulty));

        MathContext mc = new MathContext(20, RoundingMode.HALF_UP);

        double ratio = Math.pow(
                finalAmount.divide(initialAmount, mc).doubleValue(),
                1.0 / (maxCount - offset)
        );

        for (int level = 1; level <= maxCount; level++) {
            if (level < offset) {
                progression.add(initialAmount.toBigInteger());
                continue;
            }

            double req = initialAmount.doubleValue() * Math.pow(ratio, level - offset);
            BigDecimal reqValue = BigDecimal.valueOf(req)
                    .setScale(0, RoundingMode.HALF_UP);
            progression.add(reqValue.toBigInteger());
        }

        return progression;
    }

    public static ExperienceData generate(
            String extractedDir,
            String outputDir,
            int maxLevel,
            BigInteger maxXpPerLevel,
            BigInteger minXpPerLevel,
            int maxExpRatio,
            int minExpRatio,
            int expRatioPenaltyOffset,
            double difficulty
    ) {
        try {
            Path extracted = Paths.get(extractedDir);
            Path output = Paths.get(outputDir);

            if (maxXpPerLevel.compareTo(MAX_XP_PER_LEVEL) > 0) maxXpPerLevel = MAX_XP_PER_LEVEL;
            if (minXpPerLevel.compareTo(MAX_XP_PER_LEVEL) > 0) minXpPerLevel = MAX_XP_PER_LEVEL;
            if (maxExpRatio > MAX_EXP_RATIO) maxExpRatio = MAX_EXP_RATIO;
            if (minExpRatio > MAX_EXP_RATIO) minExpRatio = MAX_EXP_RATIO;

            List<BigInteger> lvlProgression = getProgression(maxLevel, maxXpPerLevel, minXpPerLevel, 1, difficulty, false);
            List<BigInteger> expRatioProgression = getProgression(maxLevel, new BigInteger("" + maxExpRatio), new BigInteger("" + minExpRatio), expRatioPenaltyOffset, 1, true);
            ExperienceData expData = loadExperienceData(extracted, CharStatsUtil.loadClassNames(extracted), lvlProgression, expRatioProgression);

            WriteUtil.writeFile(output.resolve("experience.txt"), expData);
            return expData;
        } catch (IOException e) {
            throw new RuntimeException("Failed building experience.txt", e);
        }
    }

    private static ExperienceData loadExperienceData(Path extractedDir, List<String> classes, List<BigInteger> lvlProgressionOverride, List<BigInteger> expRatioProgressionOverride) throws IOException {
        Path expPath = extractedDir.resolve("experience.txt");
        List<String[]> rows = ScannerUtil.scanFile(expPath);

        int numOfClasses = classes.size();
        ExperienceData data = new ExperienceData(rows.getFirst(), new ArrayList<>());

        for (int i = 0; i < data.headers.length; i++) {
            if (data.headers[i].equals("level")) {
                data.levelColumnIndex = i;
            }
        }

        boolean maxLvlFound = false;

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (lvlProgressionOverride != null && expRatioProgressionOverride != null) {
                if (row[0].equals("MaxLvl")) {
                    int maxLevel = lvlProgressionOverride.size();
                    for (int ii = 1; ii < numOfClasses; ii++) {
                        row[ii] = maxLevel + "";
                    }
                    maxLvlFound = true;
                }
                else if ("0".equals(row[data.levelColumnIndex])) {
                    data.levelZeroRow = row;
                    data.rows.add(row);
                    break;
                }
            }
            data.rows.add(row);
        }

        if (!maxLvlFound) {
            throw new IllegalStateException("No 'MaxLvl' column found in experience.txt");
        }

        if (lvlProgressionOverride != null && expRatioProgressionOverride != null) {
            if (data.levelZeroRow == null) throw new IllegalArgumentException("Unable to level zero row in experience.txt");

            for (int level = 1; level <= lvlProgressionOverride.size(); level++) {
                String[] newRow = data.levelZeroRow.clone();
                newRow[data.levelColumnIndex] = String.valueOf(level);
                newRow[newRow.length - 1] = expRatioProgressionOverride.get(level - 1).toString();

                for (int col = 0; col < data.headers.length; col++) {
                    String columnName = data.headers[col];
                    if (classes.contains(columnName)) {
                        newRow[col] = lvlProgressionOverride.get(level - 1).toString();
                    }
                }

                data.rows.add(newRow);
            }
        }

        return data;
    }
}
