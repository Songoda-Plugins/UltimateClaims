package com.songoda.ultimateclaims.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_NewPermissions extends DataMigration {
    public _2_NewPermissions() {
        super(2);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "permissions ADD COLUMN redstone TINYINT NOT NULL DEFAULT 0");
            statement.execute("ALTER TABLE " + tablePrefix + "permissions ADD COLUMN doors TINYINT NOT NULL DEFAULT 0");
        }
    }
}
