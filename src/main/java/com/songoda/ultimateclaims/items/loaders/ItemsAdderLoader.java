package com.songoda.ultimateclaims.items.loaders;

import com.songoda.ultimateclaims.items.ItemLoader;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class ItemsAdderLoader implements ItemLoader {
    @Override
    public String getName() {
        return "itemsadder";
    }

    @Override
    public Function<ItemStack, Boolean> loadItem(String item) {
        return itemStack -> {
            CustomStack customStack = CustomStack.getInstance(item);
            if (customStack == null) {
                return false;
            }
            return itemStack.isSimilar(customStack.getItemStack());
        };
    }

    @Override
    public ItemStack getItem(String key) {
        CustomStack customStack = CustomStack.getInstance(key);
        if (customStack == null) {
            return null;
        }
        return customStack.getItemStack();
    }
}
