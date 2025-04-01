package com.songoda.ultimateclaims.listeners;

import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.third_party.com.cryptomorin.xseries.XSound;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.items.PowerCellItem;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryListeners implements Listener {
    private final UltimateClaims plugin;

    public InventoryListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        ClaimManager claimManager = this.plugin.getClaimManager();
        Player player = (Player) event.getPlayer();
        if (!(event.getInventory().getHolder() instanceof Chest)) {
            return;
        }

        Chest chest = (Chest) event.getInventory().getHolder();

        if (!claimManager.hasClaim(player) || chest.getLocation() == null) {
            return;
        }

        Chunk chunk = chest.getLocation().getChunk();

        if (!claimManager.hasClaim(chunk)) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())
                || claim.getPowerCell().hasLocation()) {
            return;
        }

        Map<Integer, PowerCellItem> recipe = this.plugin.getItemManager().getRecipe();

        boolean failed = false;
        for (int i = 0; i < 27; i++) {
            PowerCellItem item = recipe.get(i);
            if (item == null) {
                continue;
            }
            if (!item.isSimilar(event.getInventory().getItem(i))) {
                failed = true;
                break;
            }
        }

        if (failed) {
            return;
        }

        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            claim.getPowerCell().addItem(item);
        }
        event.getInventory().clear();
        Location location = chest.getLocation();
        claim.getPowerCell().setLocation(location.clone());

        if (Settings.SET_HOME_AUTOMATICALLY.getBoolean() && player.hasPermission("ultimateclaims.home.auto")) {
            Location powerCellLocation = claim.getPowerCell().getLocation();
            if (powerCellLocation != null) {
                Location homeLocation = powerCellLocation.add(0, 1, 0);
                claim.setHome(homeLocation);

                if (claim.getName() == null || claim.getName().isEmpty()) {
                    claim.setName(claim.getDefaultName());
                }
            }
        }

        this.plugin.getDataHelper().updateClaim(claim);

        if (Settings.POWERCELL_HOLOGRAMS.getBoolean()) {
            claim.getPowerCell().createHologram();
        }

        if (this.plugin.getDynmapManager() != null) {
            this.plugin.getDynmapManager().refresh();
        }

        float xx = (float) (0 + (Math.random() * 1));
        float yy = (float) (0 + (Math.random() * 2));
        float zz = (float) (0 + (Math.random() * 1));

        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.LAVA, location.add(.5, .5, .5), 25, xx, yy, zz);
        XSound.ENTITY_BLAZE_DEATH.play(player, 1, .4f);
        XSound.ENTITY_PLAYER_LEVELUP.play(player, 1, .1f);

        this.plugin.getLocale().getMessage("event.powercell.success").sendPrefixedMessage(player);
    }
}
