package com.inventory.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.identity.IdentityColumnSupport;

/**
 * Minimal SQLite Dialect adapted for current Hibernate version.
 * Kept intentionally small to avoid incompatible API calls.
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
        // Intentionally left minimal: avoid direct registerColumnType calls
        // which vary across Hibernate versions.
    }

    public boolean supportsIdentityColumns() {
        return true;
    }

    public String getIdentityColumnString() {
        return "integer"; // SQLite autoincrement uses INTEGER
    }

    public String getIdentitySelectString() {
        return "select last_insert_rowid()";
    }

    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }

    public IdentityColumnSupport getIdentityColumnSupport() {
        return new IdentityColumnSupportImpl();
    }

    public boolean supportsLimit() {
        return true;
    }

    public String getLimitString(String query, boolean hasOffset) {
        return query + (hasOffset ? " LIMIT ? OFFSET ?" : " LIMIT ?");
    }
}
