package com.ransom.d2r.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ParsedErrors {
    public final String file;
    public final boolean exists;
    public final List<String> extHeaders;
    public final List<String> missingHeaders = new ArrayList<>();
    public final List<String> unknownHeaders = new ArrayList<>();
    public final LinkedHashMap<String, String> mismatchedHeaders = new LinkedHashMap<>();
    public final List<String> missingEntries = new ArrayList<>();
    public final LinkedHashMap<String, String> mismatchedEntries = new LinkedHashMap<>();

    public ParsedErrors(String file, boolean exists, List<String> extHeaders) {
        this.file = file;
        this.exists = exists;
        this.extHeaders = extHeaders;
    }

    @Override
    public String toString() {
        if (!exists) return "\n\tFile: '" + file + "' no longer exists in the extracted folders!";
        StringBuilder eb = new StringBuilder();
        if (!missingHeaders.isEmpty()) {
            eb.append("\n\t\tMissing Headers: ");
            eb.append(String.join(", ", missingHeaders));
        }

        if (!unknownHeaders.isEmpty()) {
            eb.append("\n\t\tUnknown Headers: ");
            eb.append(String.join(", ", unknownHeaders));
        }

        if (!mismatchedHeaders.isEmpty()) {
            eb.append("\n\t\tMismatched Headers: ");
            mismatchedHeaders.forEach((k, v) -> {
                eb.append("\n\t\t\tExpected: ");
                eb.append(k);
                eb.append("\n\t\t\tFound: ");
                eb.append(v);
            });
        }

        if (!missingEntries.isEmpty()) {
            eb.append("\n\t\tMissing Entries: ");
            eb.append("\n\t\t\tHeaders: '");
            eb.append(String.join("\t", extHeaders));
            eb.append("'");
            missingEntries.forEach(v -> {
                eb.append("\n\t\t\tRow: '");
                eb.append(v);
                eb.append("'");
            });
        }

        if (!mismatchedEntries.isEmpty()) {
            eb.append("\n\t\tMismatched Entries: ");
            eb.append("\n\t\t\tHeaders: '");
            eb.append(String.join("\t", extHeaders));
            eb.append("'");
            mismatchedEntries.forEach((k, v) -> {
                eb.append("\n\t\t\tExtracted Row: '");
                eb.append(k);
                eb.append("'\n\t\t\tMod Row: '");
                eb.append(v);
                eb.append("'");
            });
        }

        if (!eb.isEmpty()) {
            return "\n\tFile: '" + file + "'" + eb;
        }
        return eb.toString();
    }
}
