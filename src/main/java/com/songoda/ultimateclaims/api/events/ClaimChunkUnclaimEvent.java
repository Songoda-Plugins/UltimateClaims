package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Chunk;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a chunk is unclaimed by a claim
 */
public class ClaimChunkUnclaimEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Chunk chunk;
    private boolean cancel = false;

    public ClaimChunkUnclaimEvent(Claim claim, Chunk chunk) {
        super(claim);
        this.chunk = chunk;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
