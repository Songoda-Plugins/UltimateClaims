package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandHome extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandHome(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "home");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        Player player = (Player) sender;

        StringBuilder claimBuilder = new StringBuilder();
        for (String line : args) {
            claimBuilder.append(line).append(" ");
        }
        String claimStr = claimBuilder.toString().trim();

        boolean bypass = sender.hasPermission("ultimateclaims.bypass.home");
        Optional<Claim> oClaim = this.plugin.getClaimManager().getRegisteredClaims().stream()
                .filter(c -> c.getName().equalsIgnoreCase(claimStr)
                        && (bypass || c.isOwnerOrMember(player))).findFirst();

        if (!oClaim.isPresent()) {
            this.plugin.getLocale().getMessage("command.general.notapartclaim").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        Claim claim = oClaim.get();

        if (claim.getHome() == null) {
            this.plugin.getLocale().getMessage("command.home.none").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        player.teleport(claim.getHome());

        this.plugin.getLocale().getMessage("command.home.success").sendPrefixedMessage(player);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = ((Player) sender);
        if (args.length == 1) {
            boolean bypass = sender.hasPermission("ultimateclaims.bypass.home");
            List<String> claims = new ArrayList<>();
            for (Claim claim : this.plugin.getClaimManager().getRegisteredClaims()) {
                if (!claim.isOwnerOrMember(player) && !bypass) {
                    continue;
                }
                claims.add(claim.getName());
            }
            return claims;
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.home";
    }

    @Override
    public String getSyntax() {
        return "home <claim>";
    }

    @Override
    public String getDescription() {
        return "Go to a claims home.";
    }
}
