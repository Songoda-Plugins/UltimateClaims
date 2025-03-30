package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimPlayerUnbanEvent;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandUnBan extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandUnBan(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "unban");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        Player player = (Player) sender;

        if (!this.plugin.getClaimManager().hasClaim(player)) {
            this.plugin.getLocale().getMessage("command.general.noclaim").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        Claim claim = this.plugin.getClaimManager().getClaim(player);

        OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

        if (toBan == null || !(toBan.hasPlayedBefore() || toBan.isOnline())) {
            this.plugin.getLocale().getMessage("command.general.noplayer").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (player.getUniqueId().equals(toBan.getUniqueId())) {
            this.plugin.getLocale().getMessage("command.unban.notself").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        ClaimPlayerUnbanEvent event = new ClaimPlayerUnbanEvent(claim, player, toBan);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        if (toBan.isOnline()) {
            this.plugin.getLocale().getMessage("command.unban.unbanned")
                    .processPlaceholder("claim", claim.getName())
                    .sendPrefixedMessage(toBan.getPlayer());
        }

        this.plugin.getLocale().getMessage("command.unban.unban")
                .processPlaceholder("name", toBan.getName())
                .processPlaceholder("claim", claim.getName())
                .sendPrefixedMessage(player);

        claim.unBanPlayer(toBan.getUniqueId());
        this.plugin.getDataHelper().deleteBan(claim, toBan.getUniqueId());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1 && sender instanceof Player) {
            // grab our ban list
            Claim claim = this.plugin.getClaimManager().getClaim((Player) sender);
            Set<UUID> bans;
            if (claim != null && !(bans = claim.getBannedPlayers()).isEmpty()) {
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p != sender && bans.stream().anyMatch(id -> p.getUniqueId().equals(id)))
                        .map(Player::getName).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.unban";
    }

    @Override
    public String getSyntax() {
        return "unban <member>";
    }

    @Override
    public String getDescription() {
        return "Unban a member from your claim.";
    }
}
