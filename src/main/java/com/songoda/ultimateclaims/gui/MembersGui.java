package com.songoda.ultimateclaims.gui;

import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MembersGui extends CustomizableGui {
    private final UltimateClaims plugin;
    private final Claim claim;
    private ClaimRole displayedRole = ClaimRole.OWNER;
    private SortType sortType = SortType.DEFAULT;

    public MembersGui(UltimateClaims plugin, Claim claim) {
        super(plugin, "members");
        this.claim = claim;
        this.plugin = plugin;
        this.setRows(6);
        this.setTitle(plugin.getLocale().getMessage("interface.members.title").toText());

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        // edges will be type 3
        setDefaultItem(glass3);

        // decorate corners
        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 1, true, true, glass2);

        // exit buttons
        this.setButton("back", 0, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                        plugin.getLocale().getMessage("general.interface.back").toText(),
                        plugin.getLocale().getMessage("general.interface.exit").toText()),
                (event) -> this.guiManager.showGUI(event.player, claim.getPowerCell().getGui(event.player)));
        this.setButton("back", 8, this.getItem(0),
                (event) -> this.guiManager.showGUI(event.player, claim.getPowerCell().getGui(event.player)));

        // Member Stats (update on refresh)
        this.setItem("stats", 4, XMaterial.PAINTING.parseItem());

        // Filters
        this.setButton("type", 3, XMaterial.HOPPER.parseItem(), (event) -> toggleFilterType());
        this.setButton("sort", 5, XMaterial.HOPPER.parseItem(), (event) -> toggleSort());

        // Settings shortcuts
        this.setButton("visitor_settings", 5, 3, GuiUtils.createButtonItem(XMaterial.OAK_SIGN,
                        plugin.getLocale().getMessage("interface.members.visitorsettingstitle").toText(),
                        plugin.getLocale().getMessage("interface.members.visitorsettingslore").toText().split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this, ClaimRole.VISITOR)));

        this.setButton("member_settings", 5, 5, GuiUtils.createButtonItem(XMaterial.PAINTING,
                        plugin.getLocale().getMessage("interface.members.membersettingstitle").toText(),
                        plugin.getLocale().getMessage("interface.members.membersettingslore").toText().split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this, ClaimRole.MEMBER)));

        // enable page events
        setNextPage(5, 6, GuiUtils.createButtonItem(XMaterial.ARROW, plugin.getLocale().getMessage("general.interface.next").toText()));
        setPrevPage(5, 2, GuiUtils.createButtonItem(XMaterial.ARROW, plugin.getLocale().getMessage("general.interface.previous").toText()));
        setOnPage((event) -> showPage());
        showPage();
    }

    private void showPage() {
        // refresh stats
        this.setItem("stats", 4, GuiUtils.updateItem(this.getItem(4),
                this.plugin.getLocale().getMessage("interface.members.statstitle").toText(),
                this.plugin.getLocale().getMessage("interface.members.statslore")
                        .processPlaceholder("totalmembers", this.claim.getOwnerAndMembers().size())
                        .processPlaceholder("maxmembers", Settings.MAX_MEMBERS.getInt())
                        .processPlaceholder("members", this.claim.getMembers().size()).toText().split("\\|")));

        // Filters
        this.setItem("type", 3, GuiUtils.updateItem(this.getItem(3),
                this.plugin.getLocale().getMessage("interface.members.changetypetitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current",
                                this.displayedRole == ClaimRole.OWNER ? this.plugin.getLocale().getMessage("interface.role.all").toText() : this.plugin.getLocale().getMessage(this.displayedRole.getLocalePath()).toText())
                        .toText().split("\\|")));
        this.setItem("sort", 5, GuiUtils.updateItem(this.getItem(5),
                this.plugin.getLocale().getMessage("interface.members.changesorttitle").toText(),
                this.plugin.getLocale().getMessage("general.interface.current")
                        .processPlaceholder("current",
                                this.plugin.getLocale().getMessage(this.sortType.getLocalePath()).toText())
                        .toText().split("\\|")));

        // show members
        List<ClaimMember> toDisplay = new ArrayList<>(this.claim.getOwnerAndMembers());
        toDisplay = toDisplay.stream()
                .filter(m -> m.getRole() == this.displayedRole || this.displayedRole == ClaimRole.OWNER)
                .sorted(Comparator.comparingInt(claimMember -> claimMember.getRole().getIndex()))
                .collect(Collectors.toList());

        if (this.sortType == SortType.PLAYTIME) {
            toDisplay = toDisplay.stream().sorted(Comparator.comparingLong(ClaimMember::getPlayTime))
                    .collect(Collectors.toList());
        }
        if (this.sortType == SortType.MEMBER_SINCE) {
            toDisplay = toDisplay.stream().sorted(Comparator.comparingLong(ClaimMember::getPlayTime))
                    .collect(Collectors.toList());
        }

        Collections.reverse(toDisplay);
        this.pages = (int) Math.max(1, Math.ceil(toDisplay.size() / (7 * 4)));
        this.page = Math.max(this.page, this.pages);

        int currentMember = 21 * (this.page - 1);
        for (int row = 1; row < this.rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                if (toDisplay.size() - 1 < currentMember) {
                    this.clearActions(row, col);
                    this.setItem(row, col, AIR);
                    continue;
                }

                ClaimMember claimMember = toDisplay.get(currentMember);
                final UUID playerUUID = toDisplay.get(currentMember).getUniqueId();
                OfflinePlayer skullPlayer = Bukkit.getOfflinePlayer(playerUUID);

                this.setItem(row, col, GuiUtils.createButtonItem(ItemUtils.getPlayerSkull(skullPlayer),
                        ChatColor.AQUA + skullPlayer.getName(),
                        this.plugin.getLocale().getMessage("interface.members.skulllore")
                                .processPlaceholder("role",
                                        this.plugin.getLocale().getMessage(toDisplay.get(currentMember).getRole().getLocalePath()).toText())
                                .processPlaceholder("playtime", TimeUtils.makeReadable(claimMember.getPlayTime()))
                                .processPlaceholder("membersince",
                                        new SimpleDateFormat("dd/MM/yyyy").format(new Date(claimMember.getMemberSince())))
                                .toText().split("\\|")));

                currentMember++;
            }
        }
    }

    void toggleFilterType() {
        switch (this.displayedRole) {
            case OWNER:
                this.displayedRole = ClaimRole.VISITOR;
                break;
            case MEMBER:
                this.displayedRole = ClaimRole.OWNER;
                break;
            case VISITOR:
                this.displayedRole = ClaimRole.MEMBER;
                break;
        }
        showPage();
    }

    void toggleSort() {
        switch (this.sortType) {
            case DEFAULT:
                this.sortType = SortType.PLAYTIME;
                break;
            case PLAYTIME:
                this.sortType = SortType.MEMBER_SINCE;
                break;
            case MEMBER_SINCE:
                this.sortType = SortType.DEFAULT;
                break;
        }
        showPage();
    }

    public enum SortType {
        DEFAULT("interface.sortingmode.default"),
        PLAYTIME("interface.sortingmode.playtime"),
        MEMBER_SINCE("interface.sortingmode.membersince");

        private final String localePath;

        SortType(String localePath) {
            this.localePath = localePath;
        }

        public String getLocalePath() {
            return this.localePath;
        }
    }
}
