package com.songoda.ultimateclaims.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Audit;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class AuditGui extends CustomizableGui {
    private final UltimateClaims plugin;
    private final Claim claim;
    private final Player player;

    public AuditGui(UltimateClaims plugin, Claim claim, Player player) {
        super(plugin, "audits");
        this.plugin = plugin;
        this.claim = claim;
        this.player = player;
        this.setRows(6);
        this.setTitle(plugin.getLocale().getMessage("interface.audits.title").toText());
        this.showPage();
    }

    private void showPage() {
        reset();
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        mirrorFill("mirrorfill_1", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_2", 1, 1, true, true, glass3);
        mirrorFill("mirrorfill_3", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_4", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_5", 0, 1, true, true, glass2);

        this.plugin.getAuditManager().getAudits(this.claim, audits -> {
            this.pages = (int) Math.max(1, Math.ceil(audits.size() / ((double) 28)));
            setNextPage(5, 7, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.interface.next").toText()));
            setPrevPage(5, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.interface.previous").toText()));
            setOnPage(event -> showPage());
            setButton("exit", 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, this.plugin.getLocale().getMessage("general.interface.exit").toText()), (event) -> this.player.closeInventory());
            List<Audit> entries = audits.stream().skip((this.page - 1) * 28).limit(28L).collect(Collectors.toList());
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                int num = 11;
                for (Audit entry : entries) {
                    if (num == 16 || num == 36) {
                        num += 2;
                    }

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getWho());
                    ItemStack itemStack = GuiUtils.createButtonItem(XMaterial.PLAYER_HEAD, this.plugin.getLocale().getMessage("interface.audits.entryname")
                                    .processPlaceholder("who", offlinePlayer.getName())
                                    .processPlaceholder("when", TimeUtils.makeReadable(System.currentTimeMillis() - entry.getWhen()))
                                    .toText(),
                            this.plugin.getLocale().getMessage("interface.audits.entrylore")
                                    .processPlaceholder("who", offlinePlayer.getName())
                                    .processPlaceholder("when", TimeUtils.makeReadable(System.currentTimeMillis() - entry.getWhen()))
                                    .toText().split("\\|"));

                    SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                        meta.setOwningPlayer(offlinePlayer);
                    } else {
                        meta.setOwner(offlinePlayer.getName());
                    }

                    itemStack.setItemMeta(meta);
                    setItem(num, itemStack);
                    num++;
                }
                update();
            });
        });
    }
}
