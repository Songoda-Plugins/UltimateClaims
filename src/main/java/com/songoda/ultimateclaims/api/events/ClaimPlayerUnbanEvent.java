package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is unbanned from a claim
 */
public class ClaimPlayerUnbanEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancel = false;
    private final Player executor;
    private final OfflinePlayer unbannedPlayer;

    public ClaimPlayerUnbanEvent(Claim claim, Player executor, OfflinePlayer unbannedPlayer) {
        super(claim);
        this.executor = executor;
        this.unbannedPlayer = unbannedPlayer;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public OfflinePlayer getUnbannedPlayer() {
        return this.unbannedPlayer;
    }

    public Player getExecutor() {
        return this.executor;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
