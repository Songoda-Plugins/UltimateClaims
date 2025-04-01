package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimPlayerBanEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandBan extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandBan(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "ban");
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
        ClaimMember target = claim.getMember(args[0]);
        OfflinePlayer toBan;

        if (target != null) {
            toBan = target.getPlayer();
        } else {
            // unknown player: double-check
            toBan = Bukkit.getOfflinePlayer(args[0]);

            if (toBan == null || !(toBan.hasPlayedBefore() || toBan.isOnline())) {
                this.plugin.getLocale().getMessage("command.general.noplayer").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }

            // all good!
            target = claim.getMember(toBan.getUniqueId());
        }

        if (player.getUniqueId().equals(toBan.getUniqueId())) {
            this.plugin.getLocale().getMessage("command.kick.notself").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        ClaimPlayerBanEvent event = new ClaimPlayerBanEvent(claim, player, toBan);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        if (toBan.isOnline()) {
            this.plugin.getLocale().getMessage("command.ban.banned")
                    .processPlaceholder("claim", claim.getName())
                    .sendPrefixedMessage(toBan.getPlayer());
        }

        this.plugin.getLocale().getMessage("command.ban.ban")
                .processPlaceholder("name", toBan.getName())
                .processPlaceholder("claim", claim.getName())
                .sendPrefixedMessage(player);

        if (target != null) {
            claim.removeMember(toBan);
            target.eject(null);
            if (target.getRole() == ClaimRole.MEMBER) {
                this.plugin.getDataHelper().deleteMember(target);
            }
        }

        claim.banPlayer(toBan.getUniqueId());
        this.plugin.getDataHelper().createBan(claim, toBan.getUniqueId());
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
        return "ultimateclaims.ban";
    }

    @Override
    public String getSyntax() {
        return "ban <member>";
    }

    @Override
    public String getDescription() {
        return "Ban a member from your claim.";
    }
}
