package com.ransom.d2r.objects;

import java.util.Map;

public class RewriteRule {
    final String fileName;
    final Map<String,  ColumnRule> columnRules;
    final Map<String, RowRule> rowRules;

    public RewriteRule(String fileName, Map<String, ColumnRule> columnRules, Map<String, RowRule> rowRules) {
        this.fileName = fileName;
        this.columnRules = columnRules;
        this.rowRules = rowRules;
    }

    public static class RowRule {
        final String matchOn;
        final Integer colIndex;

        public RowRule(String matchOn, Integer colIndex) {
            this.matchOn = matchOn;
            this.colIndex = colIndex;
        }
    }

    public static class ColumnRule {
        final String columnName;
        final boolean toAdd;
        final boolean toDelete;
        final boolean toMigrate;
        final String toMigrateFile;
        final String replaceValueFrom;
        final String defaultValue;

        public ColumnRule(
                String columnName,
                boolean toAdd,
                String defaultValue
        ) {
            this.columnName = columnName;
            this.toAdd = toAdd;
            this.defaultValue = defaultValue;
            this.toDelete = false;
            this.toMigrate = false;
            this.toMigrateFile = null;
            this.replaceValueFrom = null;
        }

        public ColumnRule(
                String columnName,
                String replaceValueFrom
        ) {
            this.columnName = columnName;
            this.toAdd = false;
            this.defaultValue = null;
            this.toDelete = false;
            this.toMigrate = false;
            this.toMigrateFile = null;
            this.replaceValueFrom = replaceValueFrom;
        }

        public ColumnRule(
                String columnName,
                boolean toDelete,
                boolean toMigrate,
                String toMigrateFile
        ) {
            this.columnName = columnName;
            this.toAdd = false;
            this.defaultValue = null;
            this.toDelete = toDelete;
            this.toMigrate = toMigrate;
            this.toMigrateFile = toMigrateFile;
            this.replaceValueFrom = null;
        }
    }
}