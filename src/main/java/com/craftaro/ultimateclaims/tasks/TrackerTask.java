package com.craftaro.ultimateclaims.tasks;

import com.craftaro.ultimateclaims.UltimateClaims;
import com.craftaro.ultimateclaims.claim.Claim;
import com.craftaro.ultimateclaims.claim.ClaimSetting;
import com.craftaro.ultimateclaims.member.ClaimMember;
import com.craftaro.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackerTask extends BukkitRunnable implements Listener {
    private static TrackerTask instance;
    private static UltimateClaims plugin;

    private final Map<UUID, TrackedPlayer> trackedPlayers = new HashMap<>();

    public TrackerTask(UltimateClaims plug) {
        plugin = plug;
    }

    public static TrackerTask startTask(UltimateClaims plug) {
        plugin = plug;
        if (instance == null) {
            instance = new TrackerTask(plugin);
            instance.runTaskTimerAsynchronously(plugin, 10, 20);
        }

        return instance;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            TrackedPlayer trackedPlayer = this.trackedPlayers.computeIfAbsent(player.getUniqueId(), t -> new TrackedPlayer(player.getUniqueId()));
            Claim claim = plugin.getClaimManager().getClaim(player.getLocation().getChunk());
            if (claim == null) {
                //Disable fly if player is not in a claim
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    toggleFlyOff(player);
                }
                continue;
            }

            if (!player.hasPermission("ultimateclaims.admin.invisible")) {
                ClaimMember member = claim.getMember(player);
                if (member == null) {
                    claim.addMember(player, ClaimRole.VISITOR);
                    member = claim.getMember(player);
                }
                member.setPresent(true);

                if (claim.isBanned(player.getUniqueId()) && !player.hasPermission("ultimateclaims.bypass.ban")
                        || claim.isLocked() && claim.getMember(player).getRole() == ClaimRole.VISITOR && !player.hasPermission("ultimateclaims.bypass.lock")) {
                    toggleFlyOff(player);
                    member.eject(trackedPlayer.getLastBeforeClaim());
                }
            }

            if ((claim.getClaimSettings().isEnabled(ClaimSetting.FLY)
                    && player.hasPermission("ultimateclaims.fly")
                    || player.hasPermission("ultimateclaims.bypass.fly"))
                    && !player.getAllowFlight()
                    && player.getGameMode() != GameMode.CREATIVE) {
                trackedPlayer.setWasFlyActivated(true);

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(true));
            }
        }

        for (Claim claim : plugin.getClaimManager().getRegisteredClaims()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ClaimMember member = claim.getMember(player);

                if (member == null || !member.isPresent()) {
                    continue;
                }

                Claim in = plugin.getClaimManager().getClaim(player.getLocation().getChunk());

                if (in == claim) {
                    continue;
                }
                member.setPresent(false);
                toggleFlyOff(player);
            }
        }
    }

    public void addLastBefore(Player player, Location location) {
        TrackedPlayer trackedPlayer = this.trackedPlayers.get(player.getUniqueId());

        if (trackedPlayer == null) {
            return;
        }

        trackedPlayer.setLastBeforeClaim(location);
    }

    public void toggleFlyOff(Player player) {
        TrackedPlayer trackedPlayer = this.trackedPlayers.get(player.getUniqueId());

        if (trackedPlayer == null) {
            return;
        }

        if (!trackedPlayer.wasFlyActivated()) {
            return;
        }

        //Disable flight if player is not using essentials fly
        if (!trackedPlayer.isEssentialsFly()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setFallDistance(0F);
            });
        }

        //Disable fly tracking regardless of Essentials fly status
        trackedPlayer.setWasFlyActivated(false);
    }

    public TrackedPlayer getTrackedPlayer(UUID uniqueId) {
        return this.trackedPlayers.computeIfAbsent(uniqueId, TrackedPlayer::new);
    }

    public static class TrackedPlayer {
        private final UUID uuid;
        private Location lastBeforeClaim;
        private boolean wasFlyActivated;
        private boolean essentialsFly;

        public TrackedPlayer(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getUniqueId() {
            return this.uuid;
        }

        public Location getLastBeforeClaim() {
            return this.lastBeforeClaim;
        }

        public void setLastBeforeClaim(Location lastBeforeClaim) {
            this.lastBeforeClaim = lastBeforeClaim;
        }

        public boolean wasFlyActivated() {
            return this.wasFlyActivated;
        }

        public void setWasFlyActivated(boolean wasFlyActivated) {
            this.wasFlyActivated = wasFlyActivated;
        }

        public boolean isEssentialsFly() {
            return this.essentialsFly;
        }

        public void setEssentialsFly(boolean essentialsFly) {
            this.essentialsFly = essentialsFly;
        }
    }
}
