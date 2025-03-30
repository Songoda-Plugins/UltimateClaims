package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a claim's ownership is transferred
 */
public class ClaimTransferOwnershipEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancel = false;
    private final OfflinePlayer oldOwner;
    private final OfflinePlayer newOwner;

    public ClaimTransferOwnershipEvent(Claim claim, OfflinePlayer oldOwner, OfflinePlayer newOwner) {
        super(claim);
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public OfflinePlayer getNewOwner() {
        return this.newOwner;
    }

    public OfflinePlayer getOldOwner() {
        return this.oldOwner;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
