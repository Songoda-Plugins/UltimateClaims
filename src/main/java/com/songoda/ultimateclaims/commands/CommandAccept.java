package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimMemberAddEvent;
import com.songoda.ultimateclaims.invite.Invite;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandAccept extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandAccept(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "accept");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        Invite invite = this.plugin.getInviteTask().getInvite(player.getUniqueId());

        if (invite == null) {
            this.plugin.getLocale().getMessage("command.accept.none").sendPrefixedMessage(player);
        } else {
            if (Math.toIntExact(invite.getClaim().getMembers().stream()
                    .filter(member -> member.getRole() == ClaimRole.MEMBER).count()) >= Settings.MAX_MEMBERS.getInt()) {
                this.plugin.getLocale().getMessage("command.accept.maxed").sendPrefixedMessage(player);
                return ReturnType.FAILURE;
            }

            ClaimMemberAddEvent event = new ClaimMemberAddEvent(invite.getClaim(), player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return ReturnType.FAILURE;
            }

            ClaimMember newMember = invite.getClaim().getMember(player);
            if (newMember == null) {
                newMember = invite.getClaim().addMember(player, ClaimRole.MEMBER);
            } else if (newMember.getRole() == ClaimRole.VISITOR) {
                newMember.setRole(ClaimRole.MEMBER);
            }

            invite.accepted();

            this.plugin.getDataHelper().createMember(newMember);

            this.plugin.getLocale().getMessage("command.accept.success")
                    .processPlaceholder("claim", invite.getClaim().getName())
                    .sendPrefixedMessage(player);

            OfflinePlayer owner = Bukkit.getPlayer(invite.getInviter());

            if (owner != null && owner.isOnline()) {
                this.plugin.getLocale().getMessage("command.accept.accepted")
                        .processPlaceholder("name", player.getName())
                        .sendPrefixedMessage(owner.getPlayer());
            }
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.accept";
    }

    @Override
    public String getSyntax() {
        return "accept";
    }

    @Override
    public String getDescription() {
        return "Accept the latest claim invitation.";
    }
}
