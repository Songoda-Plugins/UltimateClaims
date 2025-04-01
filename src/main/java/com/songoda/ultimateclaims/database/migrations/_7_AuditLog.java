package com.songoda.ultimateclaims.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _7_AuditLog extends DataMigration {
    public _7_AuditLog() {
        super(7);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "audit_log (" +
                    "claim_id TEXT NOT NULL, " +
                    "who VARCHAR(36) NOT NULL, " +
                    "time BIGINT NOT NULL" +
                    ")");
        }
    }
}
