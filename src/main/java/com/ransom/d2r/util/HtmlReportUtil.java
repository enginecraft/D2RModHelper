package com.ransom.d2r.util;

import com.ransom.d2r.objects.ParsedErrors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class HtmlReportUtil {
    public static void generate(List<ParsedErrors> parsedErrors, Path outputHtml, String description) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"UTF-8\">\n");
        sb.append("<title>D2R Mod Diff Report</title>\n");

        // ================= CSS =================
        sb.append("<style>\n");
        sb.append("body { font-family: Consolas, monospace; background:#1e1e1e; color:#ddd; padding:20px; margin:0; }\n");
        sb.append("details { margin-bottom:10px; }\n");
        sb.append("details details { margin-left: 20px; }\n");
        sb.append("details details details { margin-left: 40px; }\n");
        sb.append("summary { cursor:pointer; font-weight:bold; color:#6cf; }\n");

        // Scrollable table
        sb.append(".table-container { max-height:300px; overflow:auto; border:1px solid #444; margin-top:5px; }\n");
        sb.append("table { border-collapse: collapse; width:max-content; min-width:100%; background:#222; }\n");
        sb.append("th, td { border:1px solid #444; padding:4px 8px; text-align:left; white-space:nowrap; }\n");
        sb.append("th { background:#333; color:#ffa; position:sticky; top:0; }\n");

        // Loading bar
        sb.append("#loading { position:fixed; top:0; left:0; width:100%; height:4px; background:#222; z-index:9999; }\n");
        sb.append("#loading-bar { width:0%; height:100%; background:#6cf; animation: loadingAnim 1.5s linear infinite; }\n");
        sb.append("@keyframes loadingAnim { 0%{width:0%} 50%{width:60%} 100%{width:100%} }\n");

        // Spinner
        sb.append("#spinner-container { text-align:center; margin-top:40px; }\n");
        sb.append(".spinner {\n");
        sb.append("  border: 6px solid #333;\n");
        sb.append("  border-top: 6px solid #6cf;\n");
        sb.append("  border-radius: 50%;\n");
        sb.append("  width: 50px;\n");
        sb.append("  height: 50px;\n");
        sb.append("  animation: spin 1s linear infinite;\n");
        sb.append("  margin: auto;\n");
        sb.append("}\n");
        sb.append("@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }\n");

        sb.append("#content { display:none; padding:20px; }\n");
        sb.append(".lazy { display:none; }\n");
        sb.append("</style>\n");

        // ================= JS =================
        sb.append("<script>\n");

        // Lazy loading
        sb.append("document.addEventListener('DOMContentLoaded', () => {\n");
        sb.append("  document.querySelectorAll('summary').forEach(s => {\n");
        sb.append("    s.addEventListener('click', () => {\n");
        sb.append("      const div = s.nextElementSibling;\n");
        sb.append("      if(div && div.classList.contains('lazy')) {\n");
        sb.append("         div.style.display='block';\n");
        sb.append("         div.classList.remove('lazy');\n");
        sb.append("      }\n");
        sb.append("    });\n");
        sb.append("  });\n");
        sb.append("});\n");

        // Hide loader when fully loaded
        sb.append("window.onload = function() {\n");
        sb.append("  document.getElementById('loading').style.display='none';\n");
        sb.append("  document.getElementById('spinner-container').style.display='none';\n");
        sb.append("  document.getElementById('content').style.display='block';\n");
        sb.append("};\n");

        sb.append("</script>\n");

        sb.append("</head>\n<body>\n");

        // ================= LOADING BAR =================
        sb.append("<div id='loading'><div id='loading-bar'></div></div>\n");

        if (description != null) {
            sb.append("<p>");
            sb.append(description.replace("\n", "<br>").replace("\t", "* "));
            sb.append("</p>\n");
        }

        sb.append("<div id='spinner-container'>\n");
        sb.append("  <div class='spinner'></div>\n");
        sb.append("  <div style='margin-top:10px; color:#aaa;'>Loading report...</div>\n");
        sb.append("</div>\n");

        // ================= CONTENT =================
        sb.append("<div id='content'>\n");
        String[] categories = {
                "Missing Headers",
                "Unknown Headers",
                "Mismatched Headers",
                "Missing Entries",
                "Mismatched Entries"
        };

        for (String category : categories) {

            long categoryCount = parsedErrors.stream().filter(e -> switch (category) {
                case "Missing Headers" -> !e.missingHeaders.isEmpty();
                case "Unknown Headers" -> !e.unknownHeaders.isEmpty();
                case "Mismatched Headers" -> !e.mismatchedHeaders.isEmpty();
                case "Missing Entries" -> !e.missingEntries.isEmpty();
                case "Mismatched Entries" -> !e.mismatchedEntries.isEmpty();
                default -> false;
            }).count();

            if (categoryCount == 0) continue;

            sb.append("<details>\n");
            sb.append("<summary>").append(category).append(" (").append(categoryCount).append(")</summary>\n");

            for (ParsedErrors e : parsedErrors) {

                List<?> items = switch (category) {
                    case "Missing Headers" -> e.missingHeaders;
                    case "Unknown Headers" -> e.unknownHeaders;
                    case "Mismatched Headers" -> e.mismatchedHeaders.entrySet().stream().toList();
                    case "Missing Entries" -> e.missingEntries;
                    case "Mismatched Entries" -> e.mismatchedEntries.entrySet().stream().toList();
                    default -> List.of();
                };

                if (items.isEmpty()) continue;

                sb.append("<details>\n");
                sb.append("<summary>").append(escape(e.file)).append(" (").append(items.size()).append(")</summary>\n");

                switch (category) {

                    case "Missing Headers":
                    case "Unknown Headers":
                        sb.append("<ul>\n");
                        for (Object h : items) {
                            sb.append("<li>").append(escape(h.toString())).append("</li>\n");
                        }
                        sb.append("</ul>\n");
                        break;

                    case "Mismatched Headers":
                        sb.append("<ul>\n");
                        for (Map.Entry<String, String> entry : e.mismatchedHeaders.entrySet()) {
                            sb.append("<li>Expected: ").append(escape(entry.getKey())).append(" | Found: ").append(escape(entry.getValue())).append("</li>\n");
                        }
                        sb.append("</ul>\n");
                        break;

                    case "Missing Entries":
                        sb.append("<div class='lazy'>\n");
                        sb.append(renderTable(e.extHeaders, e.missingEntries));
                        sb.append("</div>\n");
                        break;

                    case "Mismatched Entries":
                        sb.append("<div class='lazy'>\n");
                        sb.append(renderMismatchedTable(e.extHeaders, e.mismatchedEntries));
                        sb.append("</div>\n");
                        break;
                }

                sb.append("</details>\n");
            }

            sb.append("</details>\n");
        }

        sb.append("</div>\n"); // end content
        sb.append("</body>\n</html>");

        WriteUtil.writeFile(outputHtml, sb.toString());
    }

    private static String renderTable(List<String> headers, List<String> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='table-container'>\n");
        sb.append("<table>");
        sb.append("<thead><tr>");
        for (String h : headers) sb.append("<th>").append(escape(h)).append("</th>");
        sb.append("</tr></thead><tbody>");

        for (String row : rows) {
            sb.append("<tr>");
            for (String col : row.split("\t", -1)) {
                sb.append("<td>").append(escape(col)).append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table>\n</div>\n");
        return sb.toString();
    }

    private static String renderMismatchedTable(List<String> headers,
                                                java.util.LinkedHashMap<String, String> mismatchedRows) {

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='table-container'>\n");
        sb.append("<table>");
        sb.append("<thead><tr>");
        for (String h : headers) sb.append("<th>").append(escape(h)).append("</th>");
        sb.append("</tr></thead><tbody>");

        mismatchedRows.forEach((extRow, modRow) -> {

            String[] extCols = extRow.split("\t", -1);
            String[] modCols = modRow.split("\t", -1);

            // Extracted (green)
            sb.append("<tr style='background-color:#0a0;'>");
            for (int i = 0; i < extCols.length; i++) {
                String val = escape(extCols[i]);
                boolean diff = i < modCols.length &&
                        !extCols[i].equals(modCols[i]);
                if (diff)
                    sb.append("<td style='background-color:yellow; color:black;'>").append(val).append("</td>");
                else
                    sb.append("<td>").append(val).append("</td>");
            }
            sb.append("</tr>");

            // Mod (red)
            sb.append("<tr style='background-color:#a00;'>");
            for (int i = 0; i < modCols.length; i++) {
                String val = escape(modCols[i]);
                boolean diff = i < extCols.length &&
                        !modCols[i].equals(extCols[i]);
                if (diff)
                    sb.append("<td style='background-color:yellow; color:black;'>").append(val).append("</td>");
                else
                    sb.append("<td>").append(val).append("</td>");
            }
            sb.append("</tr>");
        });

        sb.append("</tbody></table>\n</div>\n");
        return sb.toString();
    }

    private static String escape(String s) {
        return s == null ? "" :
                s.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
    }
}
