package com.songoda.ultimateclaims.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _5_TntSetting extends DataMigration {
    public _5_TntSetting() {
        super(5);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "settings ADD COLUMN tnt TINYINT NOT NULL DEFAULT 0");
        }
    }
}
