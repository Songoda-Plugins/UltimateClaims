package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is banned from a claim
 */
public class ClaimPlayerBanEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancel = false;
    private final Player executor;
    private final OfflinePlayer bannedPlayer;

    public ClaimPlayerBanEvent(Claim claim, Player executor, OfflinePlayer bannedPlayer) {
        super(claim);
        this.executor = executor;
        this.bannedPlayer = bannedPlayer;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public OfflinePlayer getBannedPlayer() {
        return this.bannedPlayer;
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
