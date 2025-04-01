package com.songoda.ultimateclaims.claim;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.third_party.com.cryptomorin.xseries.XSound;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimDeleteEvent;
import com.songoda.ultimateclaims.api.events.ClaimTransferOwnershipEvent;
import com.songoda.ultimateclaims.claim.region.ClaimCorners;
import com.songoda.ultimateclaims.claim.region.ClaimedChunk;
import com.songoda.ultimateclaims.claim.region.ClaimedRegion;
import com.songoda.ultimateclaims.claim.region.RegionCorners;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimPermissions;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Claim {
    private static boolean reportedClaimAnimationsNotSupportedOnCurrentServerVersion = false;

    private int id;
    private String name = null;
    private ClaimMember owner;
    private final Set<ClaimMember> members = new HashSet<>();

    private final Set<ClaimedRegion> claimedRegions = new HashSet<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();

    private Location home = null;
    private boolean locked = false;

    private final ClaimSettings claimSettings = new ClaimSettings()
            .setEnabled(ClaimSetting.HOSTILE_MOB_SPAWNING, Settings.DEFAULT_CLAIM_HOSTILE_MOB_SPAWN.getBoolean())
            .setEnabled(ClaimSetting.FIRE_SPREAD, Settings.DEFAULT_CLAIM_FIRE_SPREAD.getBoolean())
            .setEnabled(ClaimSetting.MOB_GRIEFING, Settings.DEFAULT_CLAIM_MOB_GRIEFING.getBoolean())
            .setEnabled(ClaimSetting.LEAF_DECAY, Settings.DEFAULT_CLAIM_LEAF_DECAY.getBoolean())
            .setEnabled(ClaimSetting.PVP, Settings.DEFAULT_CLAIM_PVP.getBoolean())
            .setEnabled(ClaimSetting.TNT, Settings.DEFAULT_CLAIM_TNT.getBoolean())
            .setEnabled(ClaimSetting.FLY, Settings.DEFAULT_CLAIM_FLY.getBoolean());

    private ClaimPermissions memberPermissions = new ClaimPermissions()
            .setAllowed(ClaimPerm.BREAK, Settings.DEFAULT_MEMBER_BREAK.getBoolean())
            .setAllowed(ClaimPerm.INTERACT, Settings.DEFAULT_MEMBER_INTERACT.getBoolean())
            .setAllowed(ClaimPerm.PLACE, Settings.DEFAULT_MEMBER_PLACE.getBoolean())
            .setAllowed(ClaimPerm.MOB_KILLING, Settings.DEFAULT_MEMBER_MOB_KILL.getBoolean())
            .setAllowed(ClaimPerm.REDSTONE, Settings.DEFAULT_MEMBER_REDSTONE.getBoolean())
            .setAllowed(ClaimPerm.DOORS, Settings.DEFAULT_MEMBER_DOORS.getBoolean())
            .setAllowed(ClaimPerm.TRADING, Settings.DEFAULT_MEMBER_TRADE.getBoolean());

    private ClaimPermissions visitorPermissions = new ClaimPermissions()
            .setAllowed(ClaimPerm.BREAK, Settings.DEFAULT_VISITOR_BREAK.getBoolean())
            .setAllowed(ClaimPerm.INTERACT, Settings.DEFAULT_VISITOR_INTERACT.getBoolean())
            .setAllowed(ClaimPerm.PLACE, Settings.DEFAULT_VISITOR_PLACE.getBoolean())
            .setAllowed(ClaimPerm.MOB_KILLING, Settings.DEFAULT_VISITOR_MOB_KILL.getBoolean())
            .setAllowed(ClaimPerm.REDSTONE, Settings.DEFAULT_VISITOR_REDSTONE.getBoolean())
            .setAllowed(ClaimPerm.DOORS, Settings.DEFAULT_VISITOR_DOORS.getBoolean())
            .setAllowed(ClaimPerm.TRADING, Settings.DEFAULT_VISITOR_TRADE.getBoolean());

    private PowerCell powerCell = new PowerCell(this);

    private BossBar bossBarVisitor = null;
    private BossBar bossBarMember = null;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public ClaimedChunk getFirstClaimedChunk() {
        if (!this.claimedRegions.isEmpty()) {
            return this.claimedRegions.iterator().next().getFirstClaimedChunk();
        } else {
            return null;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        if (this.bossBarMember != null) {
            this.bossBarMember.setTitle(name);
        }
        if (this.bossBarVisitor != null) {
            this.bossBarVisitor.setTitle(name);
        }
    }

    public String getDefaultName() {
        return UltimateClaims.getInstance().getLocale()
                .getMessage("general.claim.defaultname")
                .processPlaceholder("name", this.owner.getName())
                .toText();
    }

    public BossBar getVisitorBossBar() {
        if (this.bossBarVisitor == null) {
            this.bossBarVisitor = Bukkit.getServer().createBossBar(this.name, BarColor.YELLOW, BarStyle.SOLID);
        }
        return this.bossBarVisitor;
    }

    public BossBar getMemberBossBar() {
        if (this.bossBarMember == null) {
            this.bossBarMember = Bukkit.getServer().createBossBar(this.name, BarColor.GREEN, BarStyle.SOLID);
        }
        return this.bossBarMember;
    }

    public ClaimMember getOwner() {
        return this.owner;
    }

    public ClaimMember setOwner(UUID owner) {
        return this.owner = new ClaimMember(this, owner, null, ClaimRole.OWNER);
    }

    public ClaimMember setOwner(OfflinePlayer owner) {
        return this.owner = new ClaimMember(this, owner.getUniqueId(), owner.getName(), ClaimRole.OWNER);
    }

    public boolean transferOwnership(OfflinePlayer newOwner) {
        if (newOwner.getUniqueId() == this.owner.getUniqueId()) {
            return false;
        }

        ClaimTransferOwnershipEvent event = new ClaimTransferOwnershipEvent(this, this.owner.getPlayer(), newOwner);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        boolean wasNameChanged = this.name.equals(getDefaultName());

        removeMember(newOwner.getUniqueId());
        this.owner.setRole(ClaimRole.MEMBER);
        addMember(this.owner);
        setOwner(newOwner);
        if (!wasNameChanged) {
            setName(getDefaultName());
        }
        return true;
    }

    public Set<ClaimMember> getMembers() {
        return this.members;
    }

    public Set<ClaimMember> getOwnerAndMembers() {
        Set<ClaimMember> members = new HashSet<>(this.members);
        members.add(this.owner);
        return members;
    }

    public ClaimMember addMember(ClaimMember member) {
        // Removing the player if they already are a member (i.e. they are a visitor) to avoid conflict
        this.removeMember(member.getUniqueId());
        this.members.add(member);
        return member;
    }

    public ClaimMember addMember(OfflinePlayer player, ClaimRole role) {
        ClaimMember newMember = new ClaimMember(this, player.getUniqueId(), player.getName(), role);
        return addMember(newMember);
    }

    public ClaimMember getMember(UUID uuid) {
        if (this.owner.getUniqueId().equals(uuid)) {
            return this.owner;
        }
        return this.members.stream()
                .filter(member -> member.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Search for a member by username
     *
     * @param name name to search
     * @return Member instance matching this username, if any
     */
    public ClaimMember getMember(String name) {
        if (name == null) {
            return null;
        }
        if (name.equals(this.owner.getName())) {
            return this.owner;
        }
        return this.members.stream()
                .filter(member -> name.equals(member.getName()))
                .findFirst()
                .orElse(null);
    }

    public ClaimMember getMember(OfflinePlayer player) {
        return getMember(player.getUniqueId());
    }

    public void removeMember(UUID uuid) {
        for (ClaimMember member : this.members) {
            if (member.getUniqueId().equals(uuid)) {
                this.members.remove(member);
                break;
            }
        }
    }

    public void removeMember(ClaimMember member) {
        this.members.remove(member);
    }

    public void removeMember(OfflinePlayer player) {
        this.removeMember(player.getUniqueId());
    }

    public boolean playerHasPerms(Player player, ClaimPerm claimPerm) {
        ClaimMember member = getMember(player);
        if (player.hasPermission("ultimateclaims.bypass.perms")
                || player.getUniqueId().equals(this.owner.getUniqueId())) {
            return true;
        }
        if (member == null) {
            return false;
        }
        return member.getRole() == ClaimRole.VISITOR && getVisitorPermissions().hasPermission(claimPerm)
                || member.getRole() == ClaimRole.MEMBER && getMemberPermissions().hasPermission(claimPerm);
    }

    public boolean isOwnerOrMember(OfflinePlayer player) {
        if (player.getUniqueId().equals(this.owner.getUniqueId())) {
            return true;
        }
        ClaimMember member = getMember(player);
        if (member == null) {
            return false;
        }
        return member.getRole() == ClaimRole.MEMBER;
    }

    public boolean containsChunk(Chunk chunk) {
        final String world = chunk.getWorld().getName();
        return this.claimedRegions.stream().anyMatch(r -> r.containsChunk(world, chunk.getX(), chunk.getZ()));
    }

    public boolean containsChunk(String world, int chunkX, int chunkZ) {
        return this.claimedRegions.stream().anyMatch(r -> r.containsChunk(world, chunkX, chunkZ));
    }

    public ClaimedRegion getPotentialRegion(Chunk chunk) {
        ClaimedChunk newChunk = new ClaimedChunk(chunk);
        return newChunk.getAttachedRegion(this);
    }

    public boolean addClaimedChunk(Chunk chunk, Player player) {
        animateChunk(chunk, player, XMaterial.EMERALD_BLOCK.parseMaterial());
        return addClaimedChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public boolean isNewRegion(Chunk chunk) {
        return isNewRegion(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public boolean isNewRegion(String world, int x, int z) {
        ClaimedChunk newChunk = new ClaimedChunk(world, x, z);
        return newChunk.getAttachedRegion(this) == null;
    }

    public boolean addClaimedChunk(String world, int x, int z) {
        ClaimedChunk newChunk = new ClaimedChunk(world, x, z);
        ClaimedRegion region = newChunk.getAttachedRegion(this);
        if (region == null) {
            this.claimedRegions.add(new ClaimedRegion(newChunk, this));
            return true;
        } else {
            region.addChunk(newChunk);
            newChunk.mergeRegions(this);
        }
        return false;
    }

    public void addClaimedRegion(ClaimedRegion region) {
        this.claimedRegions.add(region);
    }

    public void removeClaimedRegion(ClaimedRegion region) {
        this.claimedRegions.remove(region);
    }

    public ClaimedChunk removeClaimedChunk(Chunk chunk, Player player) {
        animateChunk(chunk, player, XMaterial.REDSTONE_BLOCK.parseMaterial());
        ClaimedChunk newChunk = getClaimedChunk(chunk);
        for (ClaimedRegion region : new ArrayList<>(this.claimedRegions)) {
            List<ClaimedRegion> claimedRegions = region.removeChunk(newChunk);
            if (!claimedRegions.isEmpty()) {
                this.claimedRegions.remove(region);
                this.claimedRegions.addAll(claimedRegions);
            }
            if (region.getChunks().isEmpty()) {
                this.claimedRegions.remove(region);
                UltimateClaims.getInstance().getDataHelper().deleteClaimedRegion(region);
            }
        }
        return newChunk;
    }

    public Set<ClaimedRegion> getClaimedRegions() {
        return Collections.unmodifiableSet(this.claimedRegions);
    }

    public ClaimedRegion getClaimedRegion(Chunk chunk) {
        for (ClaimedChunk claimedChunk : getClaimedChunks()) {
            if (claimedChunk.equals(chunk)) {
                return claimedChunk.getRegion();
            }
        }
        return null;
    }

    public List<ClaimedChunk> getClaimedChunks() {
        List<ClaimedChunk> chunks = new ArrayList<>();
        for (ClaimedRegion claimedRegion : this.claimedRegions) {
            chunks.addAll(claimedRegion.getChunks());
        }
        return chunks;
    }

    public ClaimedChunk getClaimedChunk(Chunk chunk) {
        for (ClaimedChunk claimedChunk : getClaimedChunks()) {
            if (claimedChunk.equals(chunk)) {
                return claimedChunk;
            }
        }
        return null;
    }

    public int getClaimSize() {
        return this.claimedRegions.stream().map(r -> r.getChunks().size()).mapToInt(Integer::intValue).sum();
    }

    public int getMaxClaimSize(Player player) {
        return PlayerUtils.getNumberFromPermission(player, "ultimateclaims.maxclaims", Settings.MAX_CHUNKS.getInt());
    }

    public void animateChunk(Chunk chunk, Player player, Material material) {
        if (Settings.ENABLE_CHUNK_ANIMATION.getBoolean()) {
            if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                if (!reportedClaimAnimationsNotSupportedOnCurrentServerVersion) {
                    reportedClaimAnimationsNotSupportedOnCurrentServerVersion = true;
                    UltimateClaims.getInstance().getLogger().warning("Chunk (claim) animations are not supported on this server version. Please update to 1.13 or higher.");
                }

            } else {
                int bx = chunk.getX() << 4;
                int bz = chunk.getZ() << 4;
                Location playerLocation = player.getLocation();

                for(int xx = bx; xx < bx + 16; ++xx) {
                    for(int zz = bz; zz < bz + 16; ++zz) {
                        for(int yy = playerLocation.getBlockY() - 5; yy < playerLocation.getBlockY() + 5; ++yy) {
                            Block block = playerLocation.getWorld().getBlockAt(xx, yy, zz);
                            if (block.getType().isOccluding() && !block.getType().isInteractable()) {
                                Location blockLocation = block.getLocation();
                                UltimateClaims.getInstance().getTaskScheduler().addTask(() -> {
                                    player.sendBlockChange(blockLocation, material.createBlockData());
                                    UltimateClaims.getInstance().getTaskScheduler().addTask(() -> {
                                        player.sendBlockChange(blockLocation, block.getBlockData());
                                    }, (long)(ThreadLocalRandom.current().nextInt(10, 50) + 1) * 20L);
                                    player.playSound(blockLocation, XSound.BLOCK_METAL_STEP.parseSound(), 1.0F, 0.2F);
                                }, (long)(ThreadLocalRandom.current().nextInt(10, 50) + 1) * 20L);
                            }
                        }
                    }
                }

            }
        }
    }

    public List<RegionCorners> getCorners() {
        if (this.claimedRegions.size() <= 0) {
            return null;
        }

        List<RegionCorners> result = new ArrayList<>();

        for (ClaimedRegion region : this.claimedRegions) {
            RegionCorners regionCorners = new RegionCorners();
            for (ClaimedChunk cChunk : region.getChunks()) {
                double[] xArr = new double[2],
                        zArr = new double[2];

                int cX = cChunk.getX() * 16,
                        cZ = cChunk.getZ() * 16;

                xArr[0] = cX;
                zArr[0] = cZ;

                xArr[1] = cX + 16;
                zArr[1] = cZ + 16;

                regionCorners.addCorners(new ClaimCorners(cChunk.getChunk(), xArr, zArr));
            }
            result.add(regionCorners);
        }

        return result;
    }

    public boolean hasPowerCell() {
        return this.powerCell.location != null;
    }

    public PowerCell getPowerCell() {
        return this.powerCell;
    }

    public void setPowerCell(PowerCell powerCell) {
        this.powerCell = powerCell;
    }

    public ClaimPermissions getMemberPermissions() {
        return this.memberPermissions;
    }

    public void setMemberPermissions(ClaimPermissions memberPermissions) {
        this.memberPermissions = memberPermissions;
    }

    public ClaimPermissions getVisitorPermissions() {
        return this.visitorPermissions;
    }

    public void setVisitorPermissions(ClaimPermissions visitorPermissions) {
        this.visitorPermissions = visitorPermissions;
    }

    public void banPlayer(UUID uuid) {
        this.bannedPlayers.add(uuid);
    }

    public void unBanPlayer(UUID uuid) {
        this.bannedPlayers.remove(uuid);
    }

    public boolean isBanned(UUID uuid) {
        return this.bannedPlayers.contains(uuid);
    }

    public void destroy(ClaimDeleteReason reason) {
        ClaimDeleteEvent event = new ClaimDeleteEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        this.claimedRegions.clear();

        if (UltimateClaims.getInstance().getDynmapManager() != null) {
            UltimateClaims.getInstance().getDynmapManager().refresh();
        }

        this.powerCell.destroy();
        UltimateClaims.getInstance().getDataHelper().deleteClaim(this);
        UltimateClaims.getInstance().getClaimManager().removeClaim(this);

        // we've just unclaimed the chunk we're in, so we've "moved" out of the claim
        if (this.bossBarMember != null) {
            this.bossBarMember.removeAll();
        }
        if (this.bossBarVisitor != null) {
            this.bossBarVisitor.removeAll();
        }
        getOwnerAndMembers().forEach(m -> m.setPresent(false));
        this.members.clear();
    }

    public Set<UUID> getBannedPlayers() {
        return this.bannedPlayers;
    }

    public void setOwner(ClaimMember owner) {
        this.owner = owner;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Location getHome() {
        return this.home;
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public ClaimSettings getClaimSettings() {
        return this.claimSettings;
    }

    public String getPowercellTimeRemaining() {
        if (hasPowerCell()) {
            return this.powerCell.getTimeRemaining();
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.id == ((Claim) obj).id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.id;
        return hash;
    }
}
