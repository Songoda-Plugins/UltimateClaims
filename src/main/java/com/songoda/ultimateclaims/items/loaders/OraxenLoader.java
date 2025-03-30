package com.songoda.ultimateclaims.items.loaders;

import com.songoda.ultimateclaims.items.ItemLoader;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class OraxenLoader implements ItemLoader {
    @Override
    public String getName() {
        return "oraxen";
    }

    @Override
    public Function<ItemStack, Boolean> loadItem(String item) {
        return itemStack -> {
            ItemBuilder oraxenItemBuilder = OraxenItems.getItemById(item);
            if (oraxenItemBuilder == null) {
                return false;
            }
            ItemStack oraxenItem = oraxenItemBuilder.build();

            return oraxenItem.isSimilar(itemStack);
        };
    }

    @Override
    public ItemStack getItem(String key) {
        ItemBuilder oraxenItemBuilder = OraxenItems.getItemById(key);
        return oraxenItemBuilder == null ? null : oraxenItemBuilder.build();
    }
}
