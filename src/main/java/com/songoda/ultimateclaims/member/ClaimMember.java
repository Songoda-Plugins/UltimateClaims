package com.songoda.ultimateclaims.member;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class ClaimMember {
    private int id;
    private final Claim claim;
    private final UUID uuid;
    private String lastName;
    private ClaimRole role;
    private boolean isPresent = false;
    private long playTime;
    private long memberSince = System.currentTimeMillis();

    public ClaimMember(Claim claim, UUID uuid, String name, ClaimRole role) {
        this.claim = claim;
        this.uuid = uuid;
        this.lastName = name;
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public Claim getClaim() {
        return this.claim;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public String getName() {
        return this.lastName;
    }

    public void setName(String name) {
        this.lastName = name;
    }

    public ClaimRole getRole() {
        return this.role;
    }

    public void setRole(ClaimRole role) {
        this.role = role;
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void setPresent(boolean present) {
        this.isPresent = present;
    }

    public long getPlayTime() {
        return this.playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public long getMemberSince() {
        return this.memberSince;
    }

    public void setMemberSince(long memberSince) {
        this.memberSince = memberSince;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.uuid);
    }

    public void eject(Location location) {
        OfflinePlayer player;
        if (!this.isPresent || !(player = getPlayer()).isOnline()) {
            return;
        }
        Location spawn = UltimateClaims.getInstance().getPluginSettings().getSpawnPoint();
        if (spawn == null && location == null) {
            return;
        }
        Bukkit.getScheduler().runTask(UltimateClaims.getInstance(), () -> player.getPlayer().teleport(location == null ? spawn : location));
        this.isPresent = false;
    }
}
