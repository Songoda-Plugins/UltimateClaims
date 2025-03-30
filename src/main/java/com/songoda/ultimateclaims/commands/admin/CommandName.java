package com.songoda.ultimateclaims.commands.admin;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandName extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandName(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "admin name");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();
        Claim claim = this.plugin.getClaimManager().getClaim(chunk);

        if (claim == null) {
            this.plugin.getLocale().getMessage("command.general.notclaimed").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        final String name = String.join(" ", args);

        claim.setName(name);

        this.plugin.getDataHelper().updateClaim(claim);

        this.plugin.getLocale().getMessage("command.name.set")
                .processPlaceholder("name", name)
                .sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.admin.name";
    }

    @Override
    public String getSyntax() {
        return "admin name <name>";
    }

    @Override
    public String getDescription() {
        return "Set the display name for the claim you are standing in.";
    }
}
