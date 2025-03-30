package com.songoda.ultimateclaims.listeners;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListeners implements Listener {
    private final UltimateClaims plugin;

    public LoginListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent event) {
        ClaimManager claimManager = this.plugin.getClaimManager();

        Chunk chunk = event.getPlayer().getLocation().getChunk();
        if (!claimManager.hasClaim(chunk)) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        ClaimMember member = claim.getMember(event.getPlayer());
        if (member == null) {
            claim.addMember(event.getPlayer(), ClaimRole.VISITOR);
            member = claim.getMember(event.getPlayer());
        }

        member.setPresent(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ClaimManager claimManager = this.plugin.getClaimManager();

        Chunk chunk = event.getPlayer().getLocation().getChunk();
        if (!claimManager.hasClaim(chunk)) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        ClaimMember member = claim.getMember(player);
        if (member != null) {
            if (member.getRole() == ClaimRole.VISITOR) {
                claim.removeMember(member);
            } else {
                member.setPresent(false);
            }
            this.plugin.getTrackerTask().toggleFlyOff(player);
        }
    }
}
