package com.craftaro.ultimateclaims.gui;

import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimateclaims.UltimateClaims;
import com.craftaro.ultimateclaims.claim.Claim;
import com.craftaro.ultimateclaims.claim.ClaimSetting;
import com.craftaro.ultimateclaims.member.ClaimRole;
import com.craftaro.ultimateclaims.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SettingsGui extends CustomizableGui {
    private final UltimateClaims plugin;
    private final Claim claim;
    private final boolean hostilemobspawning, firespread, pvp, mobgriefing, leafdecay, tnt, fly;

    public SettingsGui(UltimateClaims plugin, Claim claim, Player player) {
        super(plugin, "settings");
        this.claim = claim;
        this.plugin = plugin;
        this.setRows(3);
        this.setTitle(plugin.getLocale().getMessage("interface.settings.title").toText());

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        // edges will be type 3
        setDefaultItem(glass3);

        // decorate corners
        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 1, true, true, glass2);

        // exit buttons
        this.setButton("exit", 8, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                        plugin.getLocale().getMessage("general.interface.exit").toText()),
                (event) -> event.player.closeInventory());
        this.setButton("back", 0, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                        plugin.getLocale().getMessage("general.interface.back").toText()),
                (event) -> this.guiManager.showGUI(event.player, claim.getPowerCell().getGui(event.player)));

        // shortcuts for member settings
        this.setButton("visitors", this.rows - 1, 3, GuiUtils.createButtonItem(XMaterial.OAK_SIGN,
                        plugin.getLocale().getMessage("interface.members.visitorsettingstitle").toText(),
                        plugin.getLocale().getMessage("interface.members.visitorsettingslore").toText().split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this, ClaimRole.VISITOR)));

        this.setButton("visitors", this.rows - 1, 5, GuiUtils.createButtonItem(XMaterial.PAINTING,
                        plugin.getLocale().getMessage("interface.members.membersettingstitle").toText(),
                        plugin.getLocale().getMessage("interface.members.membersettingslore").toText().split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this, ClaimRole.MEMBER)));

        this.setItem(1, 4, AIR);
        if (this.hostilemobspawning = player.hasPermission("ultimateclaims.toggle.hostilemobspawning")) {
            this.setButton("hostilemobspawning", 1, 1, XMaterial.ZOMBIE_SPAWN_EGG.parseItem(), (event) -> toggle(ClaimSetting.HOSTILE_MOB_SPAWNING));
        }
        if (this.firespread = player.hasPermission("ultimateclaims.toggle.firespread")) {
            this.setButton("flintandsteal", 1, 2, XMaterial.FLINT_AND_STEEL.parseItem(), (event) -> toggle(ClaimSetting.FIRE_SPREAD));
        }
        if (this.pvp = player.hasPermission("ultimateclaims.toggle.pvp")) {
            this.setButton("pvp", 1, 3, XMaterial.DIAMOND_SWORD.parseItem(), (event) -> toggle(ClaimSetting.PVP));
        }
        if (this.mobgriefing = player.hasPermission("ultimateclaims.toggle.mobgriefing")) {
            this.setButton("mobgriefing", 1, 4, XMaterial.GUNPOWDER.parseItem(), (event) -> toggle(ClaimSetting.MOB_GRIEFING));
        }
        if (this.leafdecay = player.hasPermission("ultimateclaims.toggle.leafdecay")) {
            this.setButton("leafdecay", 1, 5, XMaterial.OAK_LEAVES.parseItem(), (event) -> toggle(ClaimSetting.LEAF_DECAY));
        }
        if (this.tnt = player.hasPermission("ultimateclaims.toggle.tnt")) {
            this.setButton("tnt", 1, 6, XMaterial.TNT.parseItem(), (event) -> toggle(ClaimSetting.TNT));
        }
        if (this.fly = player.hasPermission("ultimateclaims.toggle.fly")) {
            this.setButton("fly", 1, 7, XMaterial.ELYTRA.parseItem(), (event) -> toggle(ClaimSetting.FLY));
        }

        refreshDisplay();
    }

    private void refreshDisplay() {
        if (this.hostilemobspawning) {
            this.updateItem("hostilemobspawning", 1, 1,
                    this.plugin.getLocale().getMessage("interface.settings.hostilemobspawningtitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.HOSTILE_MOB_SPAWNING))
                            .toText().split("\\|"));
        }
        if (this.firespread) {
            this.updateItem("flintandsteal", 1, 2,
                    this.plugin.getLocale().getMessage("interface.settings.firespreadtitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.FIRE_SPREAD))
                            .toText().split("\\|"));
        }
        if (this.pvp) {
            this.updateItem("pvp", 1, 3,
                    this.plugin.getLocale().getMessage("interface.settings.pvptitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.PVP))
                            .toText().split("\\|"));
        }
        if (this.mobgriefing) {
            this.updateItem("mobgriefing", 1, 4,
                    this.plugin.getLocale().getMessage("interface.settings.mobgriefingtitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.MOB_GRIEFING))
                            .toText().split("\\|"));
        }
        if (this.leafdecay) {
            this.updateItem("leafdecay", 1, 5,
                    this.plugin.getLocale().getMessage("interface.settings.leafdecaytitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.LEAF_DECAY))
                            .toText().split("\\|"));
        }
        if (this.tnt) {
            this.updateItem("tnt", 1, 6,
                    this.plugin.getLocale().getMessage("interface.settings.tnttitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.TNT))
                            .toText().split("\\|"));
        }
        if (this.fly) {
            this.updateItem("tnt", 1, 7,
                    this.plugin.getLocale().getMessage("interface.settings.flytitle").toText(),
                    this.plugin.getLocale().getMessage("general.interface.current")
                            .processPlaceholder("current", this.claim.getClaimSettings().getStatus(ClaimSetting.FLY))
                            .toText().split("\\|"));
        }
    }

    private void toggle(ClaimSetting setting) {
        this.claim.getClaimSettings().setEnabled(setting, !this.claim.getClaimSettings().isEnabled(setting));
        this.plugin.getDataHelper().updateSettings(this.claim, this.claim.getClaimSettings());
        refreshDisplay();
    }
}
