package com.songoda.ultimateclaims.database.migrations;

import com.songoda.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {
    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = " AUTO_INCREMENT";

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "plugin_settings (" +
                    "spawn_world TEXT, " +
                    "spawn_x DOUBLE, " +
                    "spawn_y DOUBLE, " +
                    "spawn_z DOUBLE, " +
                    "spawn_pitch FLOAT, " +
                    "spawn_yaw FLOAT" +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "claim (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "name TEXT NOT NULL, " +
                    "home_world TEXT, " +
                    "home_x DOUBLE, " +
                    "home_y DOUBLE, " +
                    "home_z DOUBLE, " +
                    "home_pitch DOUBLE, " +
                    "home_yaw DOUBLE, " +
                    "powercell_world TEXT, " +
                    "powercell_x INTEGER, " +
                    "powercell_y INTEGER, " +
                    "powercell_z INTEGER, " +
                    "powercell_inventory TEXT, " +
                    "power INTEGER NOT NULL, " +
                    "eco_bal DOUBLE NOT NULL, " +
                    "locked TINYINT NOT NULL" +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "member (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "claim_id INTEGER NOT NULL, " +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "role TINYINT NOT NULL, " +
                    "play_time BIGINT NOT NULL, " +
                    "member_since BIGINT NOT NULL" +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "ban (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "claim_id INTEGER NOT NULL, " +
                    "player_uuid VARCHAR(36) NOT NULL" +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "chunk (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "claim_id INTEGER NOT NULL, " +
                    "world TEXT, " +
                    "x INTEGER, " +
                    "z INTEGER" +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "settings (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "claim_id INTEGER NOT NULL, " +
                    "hostile_mob_spawning TINYINT NOT NULL, " +
                    "fire_spread TINYINT NOT NULL, " +
                    "mob_griefing TINYINT NOT NULL, " +
                    "leaf_decay TINYINT NOT NULL, " +
                    "pvp TINYINT NOT NULL" +
                    ")");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "permissions (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "claim_id INTEGER NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "interact TINYINT NOT NULL, " +
                    "break TINYINT NOT NULL, " +
                    "place TINYINT NOT NULL, " +
                    "mob_kill TINYINT NOT NULL" +
                    ")");
        }
    }
}
