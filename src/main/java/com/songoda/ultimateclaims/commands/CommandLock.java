package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CommandLock extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandLock(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "lock");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (!this.plugin.getClaimManager().hasClaim(player)) {
            this.plugin.getLocale().getMessage("command.general.noclaim").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        Claim claim = this.plugin.getClaimManager().getClaim(player);

        if (!claim.isLocked()) {
            this.plugin.getLocale().getMessage("command.lock.locked")
                    .sendPrefixedMessage(player);
            for (ClaimMember member : claim.getMembers().stream().filter(m -> m.getRole() == ClaimRole.VISITOR)
                    .collect(Collectors.toList())) {
                member.eject(null);
            }
        } else {
            this.plugin.getLocale().getMessage("command.lock.unlocked")
                    .sendPrefixedMessage(player);
        }

        claim.setLocked(!claim.isLocked());

        this.plugin.getDataHelper().updateClaim(claim);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.lock";
    }

    @Override
    public String getSyntax() {
        return "lock";
    }

    @Override
    public String getDescription() {
        return "Lock or unlock your claim.";
    }
}
