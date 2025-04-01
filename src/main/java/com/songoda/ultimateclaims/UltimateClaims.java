package com.songoda.ultimateclaims;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.configuration.Config;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.hooks.WorldGuardHook;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.claim.AuditManager;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.commands.CommandAccept;
import com.songoda.ultimateclaims.commands.CommandAddMember;
import com.songoda.ultimateclaims.commands.CommandBan;
import com.songoda.ultimateclaims.commands.CommandClaim;
import com.songoda.ultimateclaims.commands.CommandDissolve;
import com.songoda.ultimateclaims.commands.CommandHome;
import com.songoda.ultimateclaims.commands.CommandInvite;
import com.songoda.ultimateclaims.commands.CommandKick;
import com.songoda.ultimateclaims.commands.CommandLeave;
import com.songoda.ultimateclaims.commands.CommandLock;
import com.songoda.ultimateclaims.commands.CommandName;
import com.songoda.ultimateclaims.commands.CommandRecipe;
import com.songoda.ultimateclaims.commands.CommandReload;
import com.songoda.ultimateclaims.commands.CommandSetHome;
import com.songoda.ultimateclaims.commands.CommandSetSpawn;
import com.songoda.ultimateclaims.commands.CommandSettings;
import com.songoda.ultimateclaims.commands.CommandShow;
import com.songoda.ultimateclaims.commands.CommandUnBan;
import com.songoda.ultimateclaims.commands.CommandUnClaim;
import com.songoda.ultimateclaims.commands.admin.CommandRemoveClaim;
import com.songoda.ultimateclaims.commands.admin.CommandTransferOwnership;
import com.songoda.ultimateclaims.database.DataHelper;
import com.songoda.ultimateclaims.database.migrations._1_InitialMigration;
import com.songoda.ultimateclaims.database.migrations._2_NewPermissions;
import com.songoda.ultimateclaims.database.migrations._3_MemberNames;
import com.songoda.ultimateclaims.database.migrations._4_TradingPermission;
import com.songoda.ultimateclaims.database.migrations._5_TntSetting;
import com.songoda.ultimateclaims.database.migrations._6_FlySetting;
import com.songoda.ultimateclaims.database.migrations._7_AuditLog;
import com.songoda.ultimateclaims.database.migrations._8_ClaimedRegions;
import com.songoda.ultimateclaims.dynmap.DynmapManager;
import com.songoda.ultimateclaims.items.ItemManager;
import com.songoda.ultimateclaims.listeners.BlockListeners;
import com.songoda.ultimateclaims.listeners.EntityListeners;
import com.songoda.ultimateclaims.listeners.EssentialsListener;
import com.songoda.ultimateclaims.listeners.InteractListeners;
import com.songoda.ultimateclaims.listeners.InventoryListeners;
import com.songoda.ultimateclaims.listeners.LoginListeners;
import com.songoda.ultimateclaims.placeholder.PlaceholderManager;
import com.songoda.ultimateclaims.settings.PluginSettings;
import com.songoda.ultimateclaims.settings.Settings;
import com.songoda.ultimateclaims.tasks.AnimateTask;
import com.songoda.ultimateclaims.tasks.InviteTask;
import com.songoda.ultimateclaims.tasks.PowerCellTask;
import com.songoda.ultimateclaims.tasks.TrackerTask;
import com.songoda.ultimateclaims.tasks.VisualizeTask;
import com.songoda.ultimateclaims.tasks.VisualizeTaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UltimateClaims extends SongodaPlugin {
    private PluginSettings pluginSettings;

    private DataHelper dataHelper;

    private final GuiManager guiManager = new GuiManager(this);
    private CommandManager commandManager;
    private ClaimManager claimManager;
    private DynmapManager dynmapManager;
    private ItemManager itemManager;
    private AuditManager auditManager;
    private InviteTask inviteTask;
    private TrackerTask trackerTask;
    private PlaceholderManager placeholderManager;
    private VisualizeTaskScheduler taskScheduler;

    /**
     * @deprecated Use {@link org.bukkit.plugin.java.JavaPlugin#getPlugin(Class)} instead.
     */
    @Deprecated
    public static UltimateClaims getInstance() {
        return getPlugin(UltimateClaims.class);
    }

    @Override
    public void onPluginLoad() {
        WorldGuardHook.addHook("allow-claims", true);
    }

    @Override
    public void onPluginEnable() {
        // Register in Songoda Core
        SongodaCore.registerPlugin(this, 65, XMaterial.CHEST);
        this.taskScheduler = new VisualizeTaskScheduler(this);

        // Load Economy & Hologram hooks
        EconomyManager.load();
        HologramManager.load(this);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Set Economy & Hologram preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY.getString());
        HologramManager.getManager().setPreferredHook(Settings.HOLOGRAM.getString());

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Setup managers
        this.itemManager = new ItemManager(this);
        this.auditManager = new AuditManager(this);

        // Listeners
        this.guiManager.init();
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new LoginListeners(this), this);

        // Load Commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("c")
                .addSubCommands(
                        new CommandSettings(this),
                        new CommandReload(this),
                        new CommandClaim(this),
                        new CommandUnClaim(this),
                        new CommandShow(this),
                        new CommandInvite(this),
                        new CommandAccept(this),
                        new CommandAddMember(this),
                        new CommandKick(this),
                        new CommandDissolve(this),
                        new CommandLeave(this),
                        new CommandLock(this),
                        new CommandHome(this),
                        new CommandSetHome(this),
                        new CommandBan(this),
                        new CommandUnBan(this),
                        new CommandRecipe(this),
                        new CommandSetSpawn(this),
                        new CommandName(this),

                        new CommandRemoveClaim(this),
                        new CommandTransferOwnership(this),
                        new com.songoda.ultimateclaims.commands.admin.CommandName(this),
                        new com.songoda.ultimateclaims.commands.admin.CommandLock(this)
                );

        // Tasks
        this.inviteTask = InviteTask.startTask(this);
        AnimateTask.startTask(this);
        if (Settings.ENABLE_FUEL.getBoolean()) {
            PowerCellTask.startTask(this);
        }
        this.trackerTask = TrackerTask.startTask(this);
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            pluginManager.registerEvents(new EssentialsListener(), this);
        }
        VisualizeTask.startTask(this);

        // Register Placeholders
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            this.placeholderManager = new PlaceholderManager(this);
            this.placeholderManager.register();
        }

        // Start our databases
        this.claimManager = new ClaimManager();
    }

    @Override
    public void onPluginDisable() {
        // save all claims data
        this.guiManager.closeAll();
        this.dataHelper.bulkUpdateClaims(this.claimManager.getRegisteredClaims());
        this.dataManager.shutdown();
        if (this.placeholderManager != null) {
            this.placeholderManager.unregister();
        }

        // cleanup holograms
        HologramManager.removeAllHolograms();

        // cleanup boss bars
        if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
            this.claimManager.getRegisteredClaims().forEach(x -> {
                x.getVisitorBossBar().removeAll();
                x.getMemberBossBar().removeAll();
            });
        }
    }

    @Override
    public void onDataLoad() {
        // Database stuff, go!
        initDatabase(Arrays.asList(
                new _1_InitialMigration(),
                new _2_NewPermissions(),
                new _3_MemberNames(),
                new _4_TradingPermission(),
                new _5_TntSetting(),
                new _6_FlySetting(),
                new _7_AuditLog(),
                new _8_ClaimedRegions())
        );
        this.dataHelper = new DataHelper(getDataManager(), this);

        this.dataHelper.getPluginSettings((pluginSettings) -> this.pluginSettings = pluginSettings);
        final boolean useHolo = Settings.POWERCELL_HOLOGRAMS.getBoolean() && HologramManager.getManager().isEnabled();

        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            this.dynmapManager = new DynmapManager(this);
        }

        this.dataHelper.getClaims((claims) -> {
            this.claimManager.addClaims(claims);
            if (useHolo) {
                this.claimManager.getRegisteredClaims().stream().filter(Claim::hasPowerCell).forEach(x -> x.getPowerCell().createHologram());
            }

            if (this.dynmapManager != null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, this.dynmapManager::refresh);
            }

            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> this.dataHelper.purgeAuditLog(), 1000, 15 * 60 * 1000);
        });
    }

    @Override
    public List<Config> getExtraConfig() {
        return Collections.emptyList();
    }

    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
        this.itemManager.loadItems();

        if (this.dynmapManager != null) {
            this.dynmapManager.reload();
        }
    }

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ClaimManager getClaimManager() {
        return this.claimManager;
    }

    public DynmapManager getDynmapManager() {
        return this.dynmapManager;
    }

    public InviteTask getInviteTask() {
        return this.inviteTask;
    }

    public TrackerTask getTrackerTask() {
        return this.trackerTask;
    }

    public PluginSettings getPluginSettings() {
        return this.pluginSettings;
    }

    public ItemManager getItemManager() {
        return this.itemManager;
    }

    public AuditManager getAuditManager() {
        return this.auditManager;
    }

    public DataHelper getDataHelper() {
        return this.dataHelper;
    }

    public VisualizeTaskScheduler getTaskScheduler() {
        return taskScheduler;
    }
}
