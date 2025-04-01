package com.songoda.ultimateclaims.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _8_ClaimedRegions extends DataMigration {
    public _8_ClaimedRegions() {
        super(8);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "claimed_regions (" +
                    "claim_id INTEGER NOT NULL, " +
                    "id VARCHAR(36) NOT NULL " +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "chunk ADD COLUMN region_id VARCHAR (36)");
        }
    }
}
