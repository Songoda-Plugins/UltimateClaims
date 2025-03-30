package com.songoda.ultimateclaims.claim.region;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaimedChunk {
    private ClaimedRegion claimedRegion;
    private final String world;
    private final int x;
    private final int z;

    public ClaimedChunk(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public ClaimedChunk(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public Chunk getChunk() {
        World world = Bukkit.getWorld(this.world);
        if (world == null) {
            return null;
        }
        return world.getChunkAt(this.x, this.z);
    }

    public String getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public boolean isAttached(ClaimedChunk chunk) {
        if (!this.world.equalsIgnoreCase(chunk.getWorld())) {
            return false;
        } else if (chunk.getX() == this.x - 1 && this.z == chunk.getZ()) {
            return true;
        } else if (chunk.getX() == this.x + 1 && this.z == chunk.getZ()) {
            return true;
        } else if (chunk.getX() == this.x && this.z == chunk.getZ() - 1) {
            return true;
        } else {
            return chunk.getX() == this.x && this.z == chunk.getZ() + 1;
        }
    }

    public List<ClaimedChunk> getAttachedChunks() {
        List<ClaimedChunk> chunks = new ArrayList<>();

        for (ClaimedChunk chunk : this.claimedRegion.getChunks()) {
            if (isAttached(chunk)) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public void mergeRegions(Claim claim) {
        for (ClaimedChunk chunk : claim.getClaimedChunks()) {
            ClaimedRegion region = chunk.getRegion();
            if (isAttached(chunk) && region != this.claimedRegion) {
                claim.removeClaimedRegion(region);
                UltimateClaims.getInstance().getDataHelper().deleteClaimedRegion(region);
                this.claimedRegion.addChunks(region.getChunks());
                UltimateClaims.getInstance().getDataHelper().updateClaimedChunks(region.getChunks());
            }
        }
    }

    public Block getCenter() {
        return this.getChunk().getBlock(8, 0, 8);
    }

    public ClaimedRegion getAttachedRegion(Claim claim) {
        ClaimedChunk claimedChunk = claim.getClaimedChunks().stream().filter(c -> c.isAttached(this)).findFirst().orElse(null);
        return claimedChunk == null ? null : claimedChunk.getRegion();
    }


    public ClaimedRegion getRegion() {
        return this.claimedRegion;
    }

    public void setRegion(ClaimedRegion claimedRegion) {
        this.claimedRegion = claimedRegion;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ClaimedChunk) {
            ClaimedChunk other = (ClaimedChunk) o;
            return this.world.equals(other.world) && this.x == other.x && this.z == other.z;
        } else if (o instanceof Chunk) {
            Chunk other = (Chunk) o;
            return this.world.equals(other.getWorld().getName()) && this.x == other.getX() && this.z == other.getZ();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.world, this.x, this.z);
    }
}
