package com.songoda.ultimateclaims.claim;

import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.utils.TimeUtils;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.gui.PowerCellGui;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.UUID;

public class PowerCell {
    // This is the unique identifier for this power cell.
    // It is reset on every plugin load.
    // Used for holograms.
    private final UUID uniqueId = UUID.randomUUID();

    protected final Claim claim;
    protected final UltimateClaims plugin = UltimateClaims.getInstance();

    protected Location location = null;

    protected List<ItemStack> items = new ArrayList<>();

    protected int currentPower = Settings.STARTING_POWER.getInt();

    protected double economyBalance = 0;
    protected PowerCellGui opened = null;

    public PowerCell(Claim claim) {
        this.claim = claim;
    }

    public int tick() {
        boolean loaded = false;
        if (this.location != null && this.location.getWorld() != null) {
            int x = this.location.getBlockX() >> 4;
            int z = this.location.getBlockZ() >> 4;

            loaded = this.location.getWorld().isChunkLoaded(x, z);
        }

        if (this.currentPower <= 0 && this.location != null) {
            updateItemsFromGui();

            ListIterator<ItemStack> iterator = this.items.listIterator();
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                double itemValue = this.plugin.getItemManager().getItemValue(itemStack);

                if (itemValue < 1) { // Remove items based on number of claimed chunks
                    int itemsToRemove = (int) Math.ceil(1 / itemValue);
                    itemStack.setAmount(itemStack.getAmount() - itemsToRemove);
                    this.currentPower += itemValue * itemsToRemove;
                } else { // Remove only one item
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    this.currentPower += itemValue;
                }

                if (itemStack.getAmount() <= 1) {
                    iterator.remove();
                }

                if (loaded && Settings.POWERCELL_HOLOGRAMS.getBoolean()) {
                    updateHologram();
                }
                return this.currentPower;
            }

            double economyValue = getEconomyValue();
            if (this.economyBalance >= economyValue) {
                this.economyBalance -= economyValue;
                this.currentPower += 1;
                if (loaded && Settings.POWERCELL_HOLOGRAMS.getBoolean()) {
                    updateHologram();
                }
                return this.currentPower;
            }
        }
        if (loaded && Settings.POWERCELL_HOLOGRAMS.getBoolean()) {
            updateHologram();
        }
        stackItems();
        return this.currentPower--;
    }

    public void rejectUnusable() {
        if (this.location == null) {
            return;
        }
        // list of items in the inventory that are worthless and removed from our inventory
        List<ItemStack> rejects = new ArrayList<>();
        for (int i = this.items.size() - 1; i >= 0; i--) {
            final ItemStack item = this.items.get(i);
            if (item != null && this.plugin.getItemManager().getItems().stream().noneMatch(powerCellItem -> powerCellItem.isSimilar(item))) {
                rejects.add(this.items.remove(i));
            }
        }

        if (!rejects.isEmpty()) {
            // YEET
            updateGuiInventory();
            rejects.stream().filter(item -> item.getType() != XMaterial.AIR.parseMaterial())
                    .forEach(item -> this.location.getWorld().dropItemNaturally(this.location, item));
        }
    }

    public void updateGuiInventory() {
        if (this.opened != null) {
            this.opened.updateGuiInventory(this.items);
        }
    }

    public void updateItemsFromGui() {
        updateItemsFromGui(false);
    }

    public void updateItemsFromGui(boolean force) {
        if (!isInventoryOpen()
                && !force) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (int i = 10; i < 44; i++) {
            if (i == 17
                    || i == 18
                    || i == 26
                    || i == 27
                    || i == 35
                    || i == 36) {
                continue;
            }
            ItemStack item = this.opened.getItem(i);
            if (item != null && item.getType() != XMaterial.AIR.parseMaterial()) {
                items.add(item);
            }
        }
        setItems(items);
    }

    public boolean isInventoryOpen() {
        return this.opened != null
                && this.opened.getInventory() != null
                && !this.opened.getInventory().getViewers().isEmpty();
    }

    public void createHologram() {
        if (this.location == null) {
            return;
        }

        if (!HologramManager.isHologramLoaded(getHologramId())) {
            HologramManager.createHologram(getHologramId(), this.location, getTimeRemaining());
        }
    }

    public void updateHologram() {
        if (this.location == null) {
            return;
        }

        if (HologramManager.isHologramLoaded(getHologramId())) {
            HologramManager.updateHologram(getHologramId(), getTimeRemaining());
        }
    }

    public String getTimeRemaining() {
        if (getTotalPower() > 1) {
            return this.plugin.getLocale().getMessage("general.claim.powercell")
                    .processPlaceholder("time", TimeUtils.makeReadable(getTotalPower() * 60 * 1000))
                    .toText();
        } else {
            return this.plugin.getLocale().getMessage("general.claim.powercell.low")
                    .processPlaceholder("time", TimeUtils.makeReadable((getTotalPower() + Settings.MINIMUM_POWER.getInt()) * 60 * 1000))
                    .toText();
        }
    }

    public void removeHologram() {
        if (HologramManager.isHologramLoaded(getHologramId())) {
            HologramManager.removeHologram(getHologramId());
        }
    }

    public int getCurrentPower() {
        return this.currentPower;
    }

    public void setCurrentPower(int currentPower) {
        this.currentPower = currentPower;

        if (this.plugin.getDynmapManager() != null) {
            this.plugin.getDynmapManager().refreshDescription(this.claim);
        }
    }

    public void setEconomyBalance(double economyBalance) {
        this.economyBalance = economyBalance;
    }

    public long getTotalPower() {
        return getItemPower() + (long) getEconomyPower() + this.currentPower;
    }

    public long getItemPower() {
        updateItemsFromGui();
        double total = 0;
        for (ItemStack itemStack : this.items) {
            double itemValue = itemStack.getAmount() * this.plugin.getItemManager().getItemValue(itemStack);

            switch (getCostEquation()) {
                case DEFAULT:
                    total += itemValue / this.claim.getClaimSize();
                    break;
                case LINEAR:
                    total += itemValue / (this.claim.getClaimSize() * getLinearValue());
                    break;
                default:
                    total += itemValue;
            }
        }
        return (int) total;
    }

    // Must not be ran if this inventory is open.
    public void stackItems() {
        List<Integer> removed = new ArrayList<>();
        List<ItemStack> newItems = new ArrayList<>();
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack item = this.items.get(i);
            XMaterial material = XMaterial.matchXMaterial(item);

            if (removed.contains(i)) {
                continue;
            }

            ItemStack newItem = item.clone();
            newItems.add(newItem);
            removed.add(i);

            if (item.getAmount() >= item.getMaxStackSize()) {
                continue;
            }

            for (int j = 0; j < this.items.size(); j++) {
                ItemStack second = this.items.get(j);

                if (newItem.getAmount() > newItem.getMaxStackSize()) {
                    break;
                }

                if (item.getAmount() >= second.getMaxStackSize()
                        || removed.contains(j)
                        || XMaterial.matchXMaterial(second) != material
                        || !second.isSimilar(item)) {
                    continue;
                }

                if (item.getAmount() + second.getAmount() > item.getMaxStackSize()) {
                    second.setAmount(newItem.getAmount() + second.getAmount() - newItem.getMaxStackSize());
                    newItem.setAmount(newItem.getMaxStackSize());
                } else {
                    removed.add(j);
                    newItem.setAmount(newItem.getAmount() + second.getAmount());
                }
            }
        }
        this.items = newItems;
    }

    public double getEconomyBalance() {
        return this.economyBalance;
    }

    public double getEconomyPower() {
        return this.economyBalance / getEconomyValue();
    }

    public double getEconomyValue() {
        double value = Settings.ECONOMY_VALUE.getDouble();

        switch (getCostEquation()) {
            case DEFAULT:
                return value * this.claim.getClaimSize();
            case LINEAR:
                return value * (this.claim.getClaimSize() * getLinearValue());
            default:
                return value;
        }
    }

    private CostEquation getCostEquation() {
        if (Settings.COST_EQUATION.getString().startsWith("LINEAR")) {
            return CostEquation.LINEAR;
        } else {
            return CostEquation.valueOf(Settings.COST_EQUATION.getString());
        }
    }

    private double getLinearValue() {
        if (getCostEquation() != CostEquation.LINEAR) {
            return 1.0d;
        }
        String[] equationSplit = Settings.COST_EQUATION.getString().split(" ");
        return Double.parseDouble(equationSplit[1]);
    }

    public List<ItemStack> getItems() {
        return new ArrayList<>(this.items);
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public boolean addItem(ItemStack item) {
        if (this.items.size() >= 28) {
            return false;
        }
        this.items.add(item);
        return true;
    }

    public void addEconomy(double amount) {
        this.economyBalance += amount;
    }

    public void removeEconomy(double amount) {
        this.economyBalance -= amount;
    }

    public void clearItems() {
        this.items.clear();
    }

    public Location getLocation() {
        return this.location == null ? null : this.location.clone();
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public PowerCellGui getGui(Player player) {
        if (this.opened != null && this.opened.isOpen()) {
            this.opened.close();
            updateItemsFromGui(true);
            stackItems();
        }

        return this.opened = new PowerCellGui(UltimateClaims.getInstance(), this.claim, player);
    }

    public void destroy() {
        if (this.location != null && this.location.getWorld() != null) {
            getItems().stream().filter(Objects::nonNull)
                    .forEach(item -> this.location.getWorld().dropItemNaturally(this.location, item));
            removeHologram();

            OfflinePlayer owner = this.claim.getOwner().getPlayer();
            EconomyManager.deposit(owner, this.economyBalance);
            if (owner.isOnline()) {
                this.plugin.getLocale().getMessage("event.powercell.destroyed")
                        .processPlaceholder("balance", this.economyBalance).sendPrefixedMessage(owner.getPlayer());
            }
        }
        this.economyBalance = 0;
        this.items.clear();
        if (this.opened != null) {
            this.opened.exit();
        }
        this.opened = null;
        this.clearItems();
        this.location = null;
    }

    public String getHologramId() {
        return "UltimateClaims-" + this.uniqueId;
    }
}
