package com.songoda.ultimateclaims.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_MemberNames extends DataMigration {
    public _3_MemberNames() {
        super(3);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "member ADD COLUMN player_name VARCHAR(16) DEFAULT NULL");
        }
    }
}
