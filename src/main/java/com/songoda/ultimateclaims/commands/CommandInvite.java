package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.invite.Invite;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandInvite extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandInvite(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "invite");
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

        OfflinePlayer invited = Bukkit.getPlayer(args[0]);

        if (invited == null || !invited.isOnline()) {
            this.plugin.getLocale().getMessage("command.general.noplayer").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (player.getUniqueId().equals(invited.getUniqueId())) {
            this.plugin.getLocale().getMessage("command.invite.notself").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (claim.getMembers().stream()
                .filter(m -> m.getRole() == ClaimRole.MEMBER)
                .anyMatch(m -> m.getUniqueId().equals(invited.getUniqueId()))) {
            this.plugin.getLocale().getMessage("command.invite.already").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (this.plugin.getInviteTask().getInvite(player.getUniqueId()) != null) {
            this.plugin.getLocale().getMessage("command.invite.alreadyinvited").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        this.plugin.getInviteTask().addInvite(new Invite(player.getUniqueId(), invited.getUniqueId(), claim));

        this.plugin.getLocale().getMessage("command.invite.invite")
                .processPlaceholder("name", invited.getName())
                .sendPrefixedMessage(player);

        this.plugin.getLocale().getMessage("command.invite.invited")
                .processPlaceholder("claim", claim.getName())
                .sendPrefixedMessage(invited.getPlayer());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            return PlayerUtils.getVisiblePlayerNames(sender, args[0]);
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.invite";
    }

    @Override
    public String getSyntax() {
        return "invite <player>";
    }

    @Override
    public String getDescription() {
        return "Invite a player to join your claim.";
    }
}
