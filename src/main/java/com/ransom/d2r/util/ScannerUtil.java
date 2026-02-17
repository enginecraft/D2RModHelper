package com.ransom.d2r.util;

import com.ransom.d2r.objects.ParsedErrors;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScannerUtil {
    public static List<String[]> scanFile(Path path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split("\t", -1));
            }
        }
        return rows;
    }

    public static List<String> scanTreeForTextFiles(String rootDir) throws IOException {
        Path rootPath = Paths.get(rootDir);

        try (Stream<Path> stream = Files.walk(rootPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .map(path -> rootPath
                            .relativize(path)
                            .toString()
                            .replace("\\", "/"))  // normalize for D2R
                    .collect(Collectors.toList());
        }
    }

    public static List<ParsedErrors> scanForComparisons(
            String refDir,
            String compDir
    ) throws IOException {
        List<String> txtFiles = scanTreeForTextFiles(
                compDir
        );

        List<ParsedErrors> parsedErrors = new ArrayList<>();
        Path modPath = Paths.get(compDir);
        Path extractedPath = Paths.get(refDir);
        for (String txtFile : txtFiles) {
            Path modTarget = modPath.resolve(txtFile);
            Path extTarget = extractedPath.resolve(txtFile);
            if (!Files.exists(extTarget)) {
                parsedErrors.add(new ParsedErrors(txtFile, false, null));
                continue;
            }
            List<String[]> modded = scanFile(modTarget);
            List<String[]> extracted = scanFile(extTarget);
            List<String> modHeaders = Arrays.asList(modded.getFirst());
            List<String> extHeaders = Arrays.asList(extracted.getFirst());

            ParsedErrors parsedFile = new ParsedErrors(txtFile, true, extHeaders);

            boolean badHeaders = false;
            for (String extHeader : extHeaders) {
                if (!modHeaders.contains(extHeader)) {
                    parsedFile.missingHeaders.add(extHeader);
                    badHeaders = true;
                }
            }

            for (String modHeader : modHeaders) {
                if (!extHeaders.contains(modHeader)) {
                    parsedFile.unknownHeaders.add(modHeader);
                    badHeaders = true;
                }
            }

            if (!badHeaders) {
                for (int i = 0; i < extHeaders.size(); i++) {
                    String extHeader = extHeaders.get(i);
                    String modHeader = modHeaders.get(i);
                    if (!extHeader.equals(modHeader)) {
                        parsedFile.mismatchedHeaders.put(extHeader, modHeader);
                    }
                }

                for (int i = 1; i < extracted.size(); i++) {
                    String[] extRow = extracted.get(i);
                    String key = extRow[0];
                    boolean found = false;
                    for (int ii = 1; ii < modded.size(); ii++) {
                        String[] modRow = modded.get(ii);
                        if (key.equals(modRow[0])) {
                            found = true;
                            for (int iii = 1; iii < modRow.length; iii++) {
                                String extVal = extRow[iii];
                                String modVal = modRow[iii];
                                if (!extVal.equals(modVal)) {
                                    parsedFile.mismatchedEntries.put(
                                            String.join("\t", extRow),
                                            String.join("\t", modRow)
                                    );
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    if (!found) {
                        parsedFile.missingEntries.add(String.join("\t", extRow));
                    }
                }
            }

            parsedErrors.add(parsedFile);
        }

        return parsedErrors;
    }
}
