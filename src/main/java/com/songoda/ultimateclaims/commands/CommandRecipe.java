package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.gui.RecipeDisplayGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandRecipe extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandRecipe(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "recipe");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.getGuiManager().showGUI((Player) sender, new RecipeDisplayGui());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }


    @Override
    public String getPermissionNode() {
        return "ultimateclaims.recipe";
    }

    @Override
    public String getSyntax() {
        return "recipe";
    }

    @Override
    public String getDescription() {
        return "View the recipe for a powercell.";
    }
}
