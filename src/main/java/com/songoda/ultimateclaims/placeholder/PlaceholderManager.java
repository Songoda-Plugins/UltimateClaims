package com.songoda.ultimateclaims.placeholder;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlaceholderManager extends PlaceholderExpansion {
    private final UltimateClaims plugin;

    public PlaceholderManager(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        List<Claim> claims = this.plugin.getClaimManager().getClaims(player);

        switch (params) {
            case "claims":
                return claims.isEmpty() ? this.plugin.getLocale().getMessage("general.word.none").toText() : claims.stream().map(Claim::getName).collect(Collectors.joining(", "));
            case "remainingpower":
            case "totalpower": // Legacy
                return claims.isEmpty() ? "0" : claims.stream().map(Claim::getPowercellTimeRemaining).collect(Collectors.joining(", "));
            case "totalchunks": {
                if (!player.isOnline()) return "0/0";
                return claims.isEmpty() ? "0/0" : claims.stream().mapToInt(Claim::getClaimSize).sum() + "/" + claims.stream().mapToInt(c -> c.getMaxClaimSize(player.getPlayer())).sum();
            }
            case "owner":
                return claims.isEmpty() ? this.plugin.getLocale().getMessage("general.word.none").toText() : claims.stream().map(c -> c.getOwner().getName()).collect(Collectors.joining(", "));
            case "members": {
                List<ClaimMember> members = new ArrayList<>();
                for (Claim claim : claims)
                    members.addAll(claim.getOwnerAndMembers());
                return claims.isEmpty() ? this.plugin.getLocale().getMessage("general.word.none").toText() : members.stream().map(ClaimMember::getName).collect(Collectors.joining(", "));
            }
            case "bans": {
                List<String> bans = new ArrayList<>();
                for (Claim claim : claims)
                    for (UUID uuid : claim.getBannedPlayers()) {
                        String banned = Bukkit.getOfflinePlayer(uuid).getName();
                        if (banned != null)
                            bans.add(banned);
                    }
                return claims.isEmpty() ? this.plugin.getLocale().getMessage("general.word.none").toText() : String.join(", ", bans);
            }
            default:
                return null;
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ultimateclaims";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Songoda";
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }
}
