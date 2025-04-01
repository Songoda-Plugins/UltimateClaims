package com.songoda.ultimateclaims.gui;

import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.inventory.ItemStack;

public class SettingsMemberGui extends CustomizableGui {
    private final UltimateClaims plugin;
    private final Claim claim;
    private final ClaimRole role;

    public SettingsMemberGui(UltimateClaims plugin, Claim claim, Gui returnGui, ClaimRole type) {
        super(plugin, "membersettings");
        this.claim = claim;
        this.role = type;
        this.plugin = plugin;
        this.setRows(3);
        this.setTitle(plugin.getLocale().getMessage("interface.permsettings.title")
                .processPlaceholder("role", TextUtils.formatText(this.role.toString().toLowerCase(), true)).toText());

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        // edges will be type 3
        setDefaultItem(glass3);

        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 1, true, true, glass2);

        // exit buttons
        this.setButton("back", 0, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                        plugin.getLocale().getMessage("general.interface.back").toText(),
                        plugin.getLocale().getMessage("general.interface.exit").toText()),
                (event) -> event.player.closeInventory());
        this.setButton("back", 8, this.getItem(0), (event) -> this.guiManager.showGUI(event.player, returnGui));

        // settings
        this.setButton("break", 1, 1, XMaterial.IRON_PICKAXE.parseItem(), (event) -> toggle(ClaimPerm.BREAK));
        this.setButton("place", 1, 2, XMaterial.STONE.parseItem(), (event) -> toggle(ClaimPerm.PLACE));
        this.setButton("interact", 1, 3, XMaterial.LEVER.parseItem(), (event) -> toggle(ClaimPerm.INTERACT));
        this.setButton("trading", 1, 4, XMaterial.EMERALD.parseItem(), (event) -> toggle(ClaimPerm.TRADING));
        this.setButton("doors", 1, 5, XMaterial.OAK_DOOR.parseItem(), (event) -> toggle(ClaimPerm.DOORS));
        this.setButton("kills", 1, 6, XMaterial.DIAMOND_SWORD.parseItem(), (event) -> toggle(ClaimPerm.MOB_KILLING));
        this.setButton("redstone", 1, 7, XMaterial.REDSTONE.parseItem(), (event) -> toggle(ClaimPerm.REDSTONE));
        refreshDisplay();
    }

    private void refreshDisplay() {
        this.updateItem("break", 1, 1,
                this.plugin.getLocale().getMessage("interface.permsettings.breaktitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.BREAK) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.BREAK))
                        .toText().split("\\|"));
        this.updateItem("place", 1, 2,
                this.plugin.getLocale().getMessage("interface.permsettings.placetitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.PLACE) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.PLACE))
                        .toText().split("\\|"));
        this.updateItem("interact", 1, 3,
                this.plugin.getLocale().getMessage("interface.permsettings.interacttitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.INTERACT) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.INTERACT))
                        .toText().split("\\|"));

        this.updateItem("trading", 1, 4,
                this.plugin.getLocale().getMessage("interface.permsettings.tradingtitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.TRADING) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.TRADING))
                        .toText().split("\\|"));

        this.updateItem("doors", 1, 5,
                this.plugin.getLocale().getMessage("interface.permsettings.doorstitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.DOORS) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.DOORS))
                        .toText().split("\\|"));
        this.updateItem("kills", 1, 6,
                this.plugin.getLocale().getMessage("interface.permsettings.mobkilltitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.MOB_KILLING) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.MOB_KILLING))
                        .toText().split("\\|"));
        this.updateItem("redstone", 1, 7,
                this.plugin.getLocale().getMessage("interface.permsettings.redstonetitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current", this.role == ClaimRole.MEMBER
                                ? this.claim.getMemberPermissions().getStatus(ClaimPerm.REDSTONE) : this.claim.getVisitorPermissions().getStatus(ClaimPerm.REDSTONE))
                        .toText().split("\\|"));

    }

    private void toggle(ClaimPerm perm) {
        if (this.role == ClaimRole.MEMBER) {
            this.claim.getMemberPermissions().setAllowed(perm, !this.claim.getMemberPermissions().hasPermission(perm));
            this.plugin.getDataHelper().updatePermissions(this.claim, this.claim.getMemberPermissions(), ClaimRole.MEMBER);
        } else {
            this.claim.getVisitorPermissions().setAllowed(perm, !this.claim.getVisitorPermissions().hasPermission(perm));
            this.plugin.getDataHelper().updatePermissions(this.claim, this.claim.getVisitorPermissions(), ClaimRole.VISITOR);
        }
        refreshDisplay();
    }
}
