package com.songoda.ultimateclaims.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSetSpawn extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandSetSpawn(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "setspawn");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;
        this.plugin.getPluginSettings().setSpawnPoint(player.getLocation());
        this.plugin.getDataHelper().createOrUpdatePluginSettings(this.plugin.getPluginSettings());
        this.plugin.getLocale().newMessage("&aSpawn point set!").sendPrefixedMessage(player);
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
        return "setspawn";
    }

    @Override
    public String getDescription() {
        return "Set the spawn point for varies use such as ejecting and banning.";
    }
}
