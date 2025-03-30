package com.songoda.ultimateclaims.items.loaders;

import com.songoda.ultimateclaims.items.ItemLoader;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class SlimefunLoader implements ItemLoader {
    @Override
    public String getName() {
        return "slimefun";
    }

    @Override
    public Function<ItemStack, Boolean> loadItem(String item) {
        return itemStack -> {
            SlimefunItem slimefunItem = SlimefunItem.getById(item);
            if (slimefunItem == null) {
                return false;
            }

            return slimefunItem.isItem(itemStack);
        };
    }

    @Override
    public ItemStack getItem(String key) {
        SlimefunItem slimefunItem = SlimefunItem.getById(key);
        if (slimefunItem == null) {
            return null;
        }
        return slimefunItem.getItem();
    }
}
