package com.songoda.ultimateclaims.dynmap;

import com.songoda.core.configuration.Config;
import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.region.ClaimCorners;
import com.songoda.ultimateclaims.claim.region.RegionCorners;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class DynmapManager {
    private final Random colorRandom = new Random(0);

    private final UltimateClaims plugin;
    private final DynmapAPI dynmapAPI;
    private MarkerSet markerSet; // null when not ready or disabled

    private boolean colorsEnabled;
    private Integer staticColor;
    private Config colorsFile;
    private boolean colorsFileDirty;

    private int taskID = -1;

    public DynmapManager(UltimateClaims plugin) {
        this.plugin = plugin;
        this.dynmapAPI = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");

        reload();
    }

    /**
     * Creates new {@link AreaMarker} for a {@link Claim} and deletes old ones
     */
    public void refresh() {
        if (this.markerSet == null) {
            return;
        }

        // Looks like the claim has been dissolved, as ClaimIds don't seem to be at least session unique, we need to
        // recreate all the markers as there is no way of guaranteeing for claim markers to be removed correctly (most of the time it fails)
        // Quit a weird behaviour... Maybe I'm just misunderstanding something?

        // Remove AreaMarkers of now unclaimed Chunks
        for (AreaMarker aMarker : this.markerSet.getAreaMarkers()) {
            aMarker.deleteMarker();
        }

        if (this.plugin.getClaimManager() != null) {
            for (Claim claim : this.plugin.getClaimManager().getRegisteredClaims()) {
                if (claim.getFirstClaimedChunk() != null && Bukkit.getWorld(claim.getFirstClaimedChunk().getWorld()) == null) {
                   continue;
                }

                if (claim.getCorners() != null) {
                    for (RegionCorners r : new HashSet<>(claim.getCorners())) {
                        for (ClaimCorners claimCorners : r.getClaimCorners()) {
                            if (this.markerSet.findAreaMarker(claim.getId() + ":" + claimCorners.chunkID) == null) {
                                AreaMarker marker = this.markerSet.createAreaMarker(claim.getId() + ":" + claimCorners.chunkID, "Claim #" + claim.getId(),
                                        false, claimCorners.getWorld().getName(), claimCorners.x, claimCorners.z, false);
                                if (this.colorsEnabled) {
                                    int color = determineColor(claim);
                                    marker.setFillStyle(marker.getFillOpacity(), color);
                                    marker.setLineStyle(marker.getLineWeight(), marker.getLineOpacity(), color);
                                }
                            }

                            refreshDescription(claim);
                        }
                    }
                }
            }

            if (this.colorsFileDirty) {
                this.colorsFile.save();
            }
        }
    }

    /**
     * Update the description of existing {@link AreaMarker} for a {@link Claim}
     */
    public void refreshDescription(Claim claim) {
        if (this.markerSet == null) {
            return;
        }

        String powerLeft;
        if (claim.getPowerCell().getTotalPower() > 1) {
            powerLeft = TimeUtils.makeReadable(claim.getPowerCell().getTotalPower() * 60 * 1000);
        } else {
            powerLeft = TimeUtils.makeReadable((claim.getPowerCell().getTotalPower() + Settings.MINIMUM_POWER.getInt()) * 60 * 1000);
        }

        String markerDesc = claim.getOwner() != null ?
                Settings.DYNMAP_BUBBLE.getString()
                        .replace("${Claim}", claim.getName())
                        .replace("${Owner}", claim.getOwner().getName())
                        .replace("${OwnerUUID}", claim.getOwner().getUniqueId().toString())
                        .replace("${MemberCount}", claim.getMembers().size() + "")
                        .replace("${PowerLeft}", powerLeft) :
                Settings.DYNMAP_BUBBLE_UNCLAIMED.getString()
                        .replace("${Claim}", claim.getName())
                        .replace("${MemberCount}", claim.getMembers().size() + "")
                        .replace("${PowerLeft}", powerLeft);

        for (AreaMarker aMarker : this.markerSet.getAreaMarkers()) {
            if (!aMarker.getMarkerID().startsWith(claim.getId() + ":")) {
                continue;
            }

            aMarker.setDescription(markerDesc);
        }
    }

    public void reload() {
        this.colorsEnabled = false;
        this.staticColor = null;
        this.colorsFile = null;
        this.colorsFileDirty = false;

        if (Settings.DYNMAP_COLORS.getObject() instanceof Number) {
            this.staticColor = ((Number) Settings.DYNMAP_COLORS.getObject()).intValue();
            this.colorsEnabled = true;
        } else {
            String colorSetting = Settings.DYNMAP_COLORS.getString();
            if ("file".equalsIgnoreCase(colorSetting)) {
                this.colorsFile = new Config(this.plugin, "dynmap-colors.yml");
                this.colorsFile.load();

                this.colorsEnabled = true;
            }

            if ("true".equalsIgnoreCase(colorSetting)) {
                this.colorsEnabled = true;
            }
        }

        this.markerSet = this.dynmapAPI.getMarkerAPI().getMarkerSet("UltimateClaims.chunks");

        if (this.markerSet != null) {
            this.markerSet.deleteMarkerSet();
            this.markerSet = null;
        }

        if (Settings.DYNMAP_ENABLED.getBoolean()) {
            this.markerSet = this.dynmapAPI.getMarkerAPI().createMarkerSet("UltimateClaims.chunks",
                    Settings.DYNMAP_LABEL.getString(), this.dynmapAPI.getMarkerAPI().getMarkerIcons(), false);

            int updateInterval = Settings.DYNMAP_UPDATE_INTERVAL.getInt();

            refresh();

            if (this.taskID == -1 && updateInterval > 0) {
                this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
                    if (this.plugin.getClaimManager() != null) {
                        for (Claim claim : this.plugin.getClaimManager().getRegisteredClaims()) {
                            if (Bukkit.getWorld(claim.getFirstClaimedChunk().getWorld()) != null && claim.getCorners() != null) {
                                refreshDescription(claim);
                            }
                        }
                    }
                }, 20L * updateInterval, 20L * updateInterval);
            } else if (updateInterval <= 0) {
                Bukkit.getScheduler().cancelTask(this.taskID);
            }
        } else {
            if (this.taskID != -1) {
                Bukkit.getScheduler().cancelTask(this.taskID);
                this.taskID = -1;
            }
        }
    }

    private int determineColor(Claim claim) {
        if (this.staticColor != null) {
            return this.staticColor;
        }

        Integer color = getColorFromConfig(claim.getOwner().getUniqueId());
        if (color != null) {
            return color;
        }

        this.colorRandom.setSeed(generateSeed(claim.getOwner().getUniqueId()));
        color = this.colorRandom.nextInt(1 << 24);

        if (this.colorsFile != null) {
            this.colorsFile.set("uuids." + claim.getOwner().getUniqueId().toString(), color);
            this.colorsFileDirty = true;
        }

        return color;
    }

    private Integer getColorFromConfig(UUID uuid) {
        if (this.colorsFile == null) {
            return null;
        }

        Object colorValue = this.colorsFile.get("uuids." + uuid.toString());
        if (colorValue == null) {
            return null;
        }

        return ((Number) colorValue).intValue();
    }

    private static long generateSeed(UUID uuid) {
        long hilo = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        return ((hilo >> 32)) ^ hilo;
    }
}
