package com.songoda.ultimateclaims.items.loaders;

import com.songoda.ultimateclaims.items.ItemLoader;
import com.jojodmo.itembridge.ItemBridge;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class ItemBridgeLoader implements ItemLoader {
    @Override
    public String getName() {
        return "itembridge";
    }

    @Override
    public Function<ItemStack, Boolean> loadItem(String item) {
        return itemStack -> {
            ItemStack itemStackBridge = getItem(item);
            if (itemStackBridge == null) {
                return false;
            }
            return itemStack.isSimilar(itemStackBridge);
        };
    }

    @Override
    public ItemStack getItem(String key) {
        return ItemBridge.getItemStack(key);
    }
}
