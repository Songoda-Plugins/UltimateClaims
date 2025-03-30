package com.songoda.ultimateclaims.settings;

import org.bukkit.Location;

public class PluginSettings {
    private Location spawnPoint = null;

    public Location getSpawnPoint() {
        return this.spawnPoint == null ? null : this.spawnPoint.clone();
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
}
