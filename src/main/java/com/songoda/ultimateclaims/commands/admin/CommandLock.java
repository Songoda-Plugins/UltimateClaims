package com.songoda.ultimateclaims.commands.admin;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CommandLock extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandLock(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "admin lock");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();
        Claim claim = this.plugin.getClaimManager().getClaim(chunk);

        if (claim == null) {
            this.plugin.getLocale().getMessage("command.general.notclaimed").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!claim.isLocked()) {
            this.plugin.getLocale().getMessage("command.lock.lockedother")
                    .sendPrefixedMessage(player);
            for (ClaimMember member : claim.getMembers().stream().filter(m -> m.getRole() == ClaimRole.VISITOR)
                    .collect(Collectors.toList())) {
                member.eject(null);
            }
        } else {
            this.plugin.getLocale().getMessage("command.lock.unlockedother")
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
        return "ultimateclaims.admin.lock";
    }

    @Override
    public String getSyntax() {
        return "admin lock";
    }

    @Override
    public String getDescription() {
        return "Lock or unlock the claim you are standing in.";
    }
}
