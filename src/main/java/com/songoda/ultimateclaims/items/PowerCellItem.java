package com.songoda.ultimateclaims.items;

import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class PowerCellItem {
    private final ItemStack displayItem;
    private final Function<ItemStack, Boolean> similar;
    private final int value;

    public PowerCellItem(ItemStack displayItem, Function<ItemStack, Boolean> similar, int value) {
        this.displayItem = displayItem;
        this.similar = similar;
        this.value = value;
    }

    public boolean isSimilar(ItemStack itemStack) {
        return this.similar.apply(itemStack);
    }

    public int getValue() {
        return this.value;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }
}
