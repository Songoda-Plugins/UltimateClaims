package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandReload(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "reload");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.reloadConfig();
        this.plugin.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.admin";
    }

    @Override
    public String getSyntax() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}
