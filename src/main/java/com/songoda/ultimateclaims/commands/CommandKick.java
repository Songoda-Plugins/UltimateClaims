package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.utils.PlayerUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimPlayerKickEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandKick extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandKick(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "kick");
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
        OfflinePlayer toKick;

        if (target != null) {
            toKick = target.getPlayer();
        } else {
            // unknown player: double-check
            toKick = Bukkit.getOfflinePlayer(args[0]);

            if (toKick == null || !(toKick.hasPlayedBefore() || toKick.isOnline())) {
                this.plugin.getLocale().getMessage("command.general.noplayer").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            } else if (player.getUniqueId().equals(toKick.getUniqueId())) {
                this.plugin.getLocale().getMessage("command.kick.notself").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }

            // all good!
            target = claim.getMember(toKick.getUniqueId());
        }

        if (target == null || target.getRole() != ClaimRole.MEMBER) {
            this.plugin.getLocale().getMessage("command.general.notinclaim").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        ClaimPlayerKickEvent event = new ClaimPlayerKickEvent(claim, toKick);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        if (toKick.isOnline()) {
            this.plugin.getLocale().getMessage("command.kick.kicked")
                    .processPlaceholder("claim", claim.getName())
                    .sendPrefixedMessage(toKick.getPlayer());
        }

        this.plugin.getLocale().getMessage("command.kick.kick")
                .processPlaceholder("name", toKick.getName())
                .processPlaceholder("claim", claim.getName())
                .sendPrefixedMessage(player);

        // and YEET!
        target.setRole(ClaimRole.VISITOR);
        this.plugin.getDataHelper().deleteMember(target);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            // todo: list out members in this player's owned claim
            return PlayerUtils.getVisiblePlayerNames(sender, args[0]);
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.kick";
    }

    @Override
    public String getSyntax() {
        return "kick <member>";
    }

    @Override
    public String getDescription() {
        return "Kick a member from your claim.";
    }
}
