package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimDeleteReason;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a claim is deleted
 */
public class ClaimDeleteEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final ClaimDeleteReason deleteReason;
    private boolean cancel = false;

    public ClaimDeleteEvent(Claim claim, ClaimDeleteReason deleteReason) {
        super(claim);
        this.deleteReason = deleteReason;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public ClaimDeleteReason getDeleteReason() {
        return this.deleteReason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
