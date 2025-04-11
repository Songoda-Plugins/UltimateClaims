package com.songoda.ultimateclaims.settings;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.configuration.ConfigSetting;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.ultimateclaims.UltimateClaims;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class Settings {
    static final Config config = UltimateClaims.getInstance().getCoreConfig();

    public static final ConfigSetting POWERCELL_RECIPE = new ConfigSetting(config, "Main.PowerCell Recipe",
            Collections.emptyList(),
            "The recipe players will need to place into a chest",
            "in order to create a powercell.");

    public static final ConfigSetting ITEM_VALUES = new ConfigSetting(config, "Main.PowerCell Item Values",
            Collections.emptyList(),
            "The value in minutes of each item put into the powercell.",
            "This is now configured in items.yml. Do not use this.");

    public static final ConfigSetting ECONOMY = new ConfigSetting(config, "Main.Economy",
            EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "You can choose from \"" + EconomyManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining("\", \"")) + "\".");

    public static final ConfigSetting HOLOGRAM = new ConfigSetting(config, "Main.Hologram",
            HologramManager.getHolograms() == null ? "HolographicDisplays" : HologramManager.getHolograms().getName(),
            "Which hologram plugin should be used?",
            "You can choose from \"" + HologramManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining(", ")) + "\".");

    public static final ConfigSetting ECONOMY_VALUE = new ConfigSetting(config, "Main.PowerCell Economy Value", 100,
            "How much money should constitute one minute?");

    public static final ConfigSetting MINIMUM_POWER = new ConfigSetting(config, "Main.Minimum PowerCell power", -30,
            "The minimum amount of power allowed before a claim",
            "auto dissolves.");

    public static final ConfigSetting SET_HOME_AUTOMATICALLY = new ConfigSetting(config, "Main.Set Home Automatically", true,
            "Should a home be set automatically when a claim is created?", "Player should have the permission 'ultimateclaims.home.auto'.");

    public static final ConfigSetting POWERCELL_HOLOGRAMS = new ConfigSetting(config, "Main.Powercell Holograms", true,
            "Should holograms be placed above powercells?");

    public static final ConfigSetting CLAIMS_BOSSBAR = new ConfigSetting(config, "Main.Claims Use Boss Bar", false,
            "Display a boss bar to players while they're in a claim?",
            "Default behavior is to show a title on entry/exit.");

    public static final ConfigSetting CHUNKS_MUST_TOUCH = new ConfigSetting(config, "Main.Chunks Must Touch", true,
            "Should chunks have to touch to be claimed?",
            "This prevents people from claiming little pieces all over the place.");

    public static final ConfigSetting INVITE_TIMEOUT = new ConfigSetting(config, "Main.Invite Timeout", 30,
            "The amount of time before an invite times out.");

    public static final ConfigSetting STARTING_POWER = new ConfigSetting(config, "Main.Starting Power", 10,
            "The starting amount of power in minutes a claim gets.",
            "This time should be used to create a powercell.");

    public static final ConfigSetting NAME_CHAR_LIMIT = new ConfigSetting(config, "Main.Name Character Limit", 25,
            "The maximum amount of characters allows in a claims name.");

    public static final ConfigSetting MAX_CHUNKS = new ConfigSetting(config, "Main.Max Chunks", 10,
            "The maximum amount of chunks a claim can have.");

    public static final ConfigSetting MAX_REGIONS = new ConfigSetting(config, "Main.Max Regions", 2,
            "The maximum amount of regions a claim can have.");

    public static final ConfigSetting MAX_MEMBERS = new ConfigSetting(config, "Main.Max Members", 10,
            "The maximum amount of members a claim can have.");

    public static final ConfigSetting DISABLED_WORLDS = new ConfigSetting(config, "Main.Disabled Worlds",
            Arrays.asList("disabled_world"), "The worlds that claims are disabled in.");

    public static final ConfigSetting COST_EQUATION = new ConfigSetting(config, "Main.Cost Equation",
            "DEFAULT", "The equation used for calculation the cost of a claim.",
            "NONE - The claim size does not affect the cost.",
            "DEFAULT - Multiplies the cost by the claim size.",
            "LINEAR [value] - Multiplies the cost by the claim size multiplied by the value.");

    public static final ConfigSetting PURGE_AUDIT_LOG_AFTER = new ConfigSetting(config, "Main.Purge Audit Log After", 15,
            "After how many days should the audit log purge data.");

    public static final ConfigSetting ENABLE_FUEL = new ConfigSetting(config, "Main.Powercells Require Fuel", true,
            "Should power cells not require fuel?");

    public static final ConfigSetting ENABLE_HOPPERS = new ConfigSetting(config, "Main.Enable Hoppers", true,
            "Should hoppers be able to put fuel into a power cell?",
            "Please note that this feature is experimental.");

    public static final ConfigSetting ENABLE_AUDIT_LOG = new ConfigSetting(config, "Main.Enable Audit Log", true,
            "Should we enable the audit log?",
            "Disable if you have issues with big databases.");

    public static final ConfigSetting ENABLE_CHUNK_ANIMATION = new ConfigSetting(config, "Main.Enable Chunk Animation", true,
            "Should we enable the chunk animation",
            "when chunks are claimed/unclaimed?");

    public static final ConfigSetting ALLOW_CUSTOM_SPAWN = new ConfigSetting(config, "Main.Allow Third Party Plugin and Command Spawn", true,
            "Should third party plugin and command spawn be allowed while hostile mob spawning is disabled in claim settings?," +
                    "This will allow you to spawn pets and other entities that are not vanilla mobs.");

    public static final ConfigSetting DEFAULT_CLAIM_HOSTILE_MOB_SPAWN = new ConfigSetting(config, "Default Settings.Claim.Hostile Mob Spawn", true,
            "Should hostile mob spawning be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_CLAIM_FIRE_SPREAD = new ConfigSetting(config, "Default Settings.Claim.Fire Spread", true,
            "Should fire spread be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_CLAIM_MOB_GRIEFING = new ConfigSetting(config, "Default Settings.Claim.Mob Griefing", true,
            "Should mob griefing be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_CLAIM_LEAF_DECAY = new ConfigSetting(config, "Default Settings.Claim.Leaf Decay", true,
            "Should leaf decay be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_CLAIM_PVP = new ConfigSetting(config, "Default Settings.Claim.Pvp", true,
            "Should PvP be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_CLAIM_TNT = new ConfigSetting(config, "Default Settings.Claim.Tnt", false,
            "Should TNT explosions be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_CLAIM_FLY = new ConfigSetting(config, "Default Settings.Claim.Fly", false,
            "Should fly be enabled by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_BREAK = new ConfigSetting(config, "Default Settings.Member.Break", true,
            "Should members be allowed to break blocks",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_INTERACT = new ConfigSetting(config, "Default Settings.Member.Interact", true,
            "Should members be allowed to interact with",
            "items by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_PLACE = new ConfigSetting(config, "Default Settings.Member.Place", true,
            "Should members be allowed to place blocks",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_MOB_KILL = new ConfigSetting(config, "Default Settings.Member.Mob Kill", true,
            "Should members be allowed to kill mobs",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_REDSTONE = new ConfigSetting(config, "Default Settings.Member.Redstone", true,
            "Should members be allowed to use redstone",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_DOORS = new ConfigSetting(config, "Default Settings.Member.Doors", true,
            "Should members be allowed to use doors",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_MEMBER_TRADE = new ConfigSetting(config, "Default Settings.Member.Trade", true,
            "Should members be allowed to trade",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_BREAK = new ConfigSetting(config, "Default Settings.Visitor.Break", false,
            "Should visitors be allowed to break blocks",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_INTERACT = new ConfigSetting(config, "Default Settings.Visitor.Interact", false,
            "Should visitors be allowed to interact with",
            "items by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_PLACE = new ConfigSetting(config, "Default Settings.Visitor.Place", false,
            "Should visitors be allowed to place blocks",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_MOB_KILL = new ConfigSetting(config, "Default Settings.Visitor.Mob Kill", false,
            "Should visitors be allowed to kill mobs",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_REDSTONE = new ConfigSetting(config, "Default Settings.Visitor.Redstone", false,
            "Should visitors be allowed to use redstone",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_DOORS = new ConfigSetting(config, "Default Settings.Visitor.Doors", false,
            "Should visitors be allowed to use doors",
            "by default in new claims?");

    public static final ConfigSetting DEFAULT_VISITOR_TRADE = new ConfigSetting(config, "Default Settings.Visitor.Trade", false,
            "Should visitors be allowed to trade",
            "by default in new claims?");

    public static final ConfigSetting GLASS_TYPE_1 = new ConfigSetting(config, "Interfaces.Glass Type 1", "GRAY_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_2 = new ConfigSetting(config, "Interfaces.Glass Type 2", "BLUE_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_3 = new ConfigSetting(config, "Interfaces.Glass Type 3", "LIGHT_BLUE_STAINED_GLASS_PANE");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    public static final ConfigSetting DYNMAP_ENABLED = new ConfigSetting(config, "Dynmap.Enabled", true, "Set to 'false' to disable highlighting claimed areas on Dynmap.");
    public static final ConfigSetting DYNMAP_COLORS = new ConfigSetting(config, "Dynmap.Colors", false,
            "The following options exist: false, true, <number>, file",
            "false: Dynmap's default color will be used",
            "true: A unique color will be generated for each player (based on their UUID)",
            "<number>: You can configure a specific color to be used (replace '<number>' with the numeric representation of the RGB color) e.g. 0xFF0000 for red",
            "file: A file containing the colors to use will be generated. If a UUID is not found in the file, a color will be generated (same as true)",
            "",
            "The 'file' is not recommended for servers with many players as it will generate a lot of unnecessary data");
    public static final ConfigSetting DYNMAP_LABEL = new ConfigSetting(config, "Dynmap.Label", "Claimed Chunks",
            "The label is shown to the user at the upper-right corner by default",
            "User can toggle a checkbox to disable this overlay");
    public static final ConfigSetting DYNMAP_BUBBLE = new ConfigSetting(config, "Dynmap.Bubble", "<b><u>${Claim}</u></b><br>\n<b>Owner</b>: ${Owner}<br>\n<b>Power left</b>: ${PowerLeft}",
            "The text shown when you click on an claim on the Dynmap. May contain HTML",
            "Supported placeholder: ${Claim}, ${Owner}, ${OwnerUUID}, ${MemberCount}, ${PowerLeft}");
    public static final ConfigSetting DYNMAP_BUBBLE_UNCLAIMED = new ConfigSetting(config, "Dynmap.BubbleUnClaimed", "<b><u>${Claim}</u></b><br>\n<i>Unclaimed</i><br>\n<b>Power left</b>: ${PowerLeft}",
            "The text shown when you click on an claim on the Dynmap. May contain HTML",
            "Supported placeholder: ${Claim}, ${Owner}, ${OwnerUUID}, ${MemberCount}, ${PowerLeft}");
    public static final ConfigSetting DYNMAP_UPDATE_INTERVAL = new ConfigSetting(config, "Dynmap.UpdateInterval", 60,
            "How often should existing Claims be updated on Dynmap");

    /**
     * In order to set dynamic economy comment correctly, this needs to be
     * called after EconomyManager load
     */
    public static void setupConfig() {
        config.load();
        config.setAutoremove(true).setAutosave(true);

        // convert glass pane settings
        int color;
        if ((color = GLASS_TYPE_1.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_1.getKey(), CompatibleMaterial.getGlassPaneForColor(color).name());
        }
        if ((color = GLASS_TYPE_2.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_2.getKey(), CompatibleMaterial.getGlassPaneForColor(color).name());
        }
        if ((color = GLASS_TYPE_3.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_3.getKey(), CompatibleMaterial.getGlassPaneForColor(color).name());
        }

        // convert economy settings
        if (config.getBoolean("Economy.Use Vault Economy") && EconomyManager.getManager().isEnabled("Vault")) {
            config.set("Main.Economy", "Vault");
        } else if (config.getBoolean("Economy.Use Reserve Economy") && EconomyManager.getManager().isEnabled("Reserve")) {
            config.set("Main.Economy", "Reserve");
        } else if (config.getBoolean("Economy.Use Player Points Economy") && EconomyManager.getManager().isEnabled("PlayerPoints")) {
            config.set("Main.Economy", "PlayerPoints");
        }

        config.saveChanges();
    }
}
