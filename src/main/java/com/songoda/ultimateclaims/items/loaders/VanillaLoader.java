package com.songoda.ultimateclaims.items.loaders;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.items.ItemLoader;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Function;

public class VanillaLoader implements ItemLoader {
    @Override
    public String getName() {
        return "vanilla";
    }

    @Override
    public Function<ItemStack, Boolean> loadItem(String item) {
        Optional<XMaterial> material = CompatibleMaterial.getMaterial(item);
        if (!material.isPresent()) {
            return null;
        }
        return itemStack -> itemStack != null && material.get().isSimilar(itemStack);
    }

    @Override
    public ItemStack getItem(String key) {
        return CompatibleMaterial.getMaterial(key).get().parseItem();
    }
}
