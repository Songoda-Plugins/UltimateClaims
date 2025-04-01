package com.songoda.ultimateclaims.items;

import com.songoda.core.configuration.Config;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.items.loaders.ItemBridgeLoader;
import com.songoda.ultimateclaims.items.loaders.ItemsAdderLoader;
import com.songoda.ultimateclaims.items.loaders.OraxenLoader;
import com.songoda.ultimateclaims.items.loaders.SlimefunLoader;
import com.songoda.ultimateclaims.items.loaders.VanillaLoader;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemManager {
    private final UltimateClaims plugin;
    private final List<ItemLoader> itemLoaders;
    private final List<PowerCellItem> items;
    private final Map<Integer, PowerCellItem> recipe;
    private final Config itemConfig;

    public ItemManager(UltimateClaims plugin) {
        this.plugin = plugin;
        this.itemLoaders = new ArrayList<>();
        this.items = new ArrayList<>();
        this.recipe = new HashMap<>();
        this.itemConfig = new Config(plugin, "items.yml");

        loadLoaders();
        loadItems();
    }

    private void loadLoaders() {
        if (Bukkit.getPluginManager().getPlugin("Slimefun") != null) {
            this.itemLoaders.add(new SlimefunLoader());
        }

        if (Bukkit.getPluginManager().getPlugin("ItemBridge") != null) {
            this.itemLoaders.add(new ItemBridgeLoader());
        }

        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            this.itemLoaders.add(new ItemsAdderLoader());
        }

        if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
            this.itemLoaders.add(new OraxenLoader());
        }

        this.itemLoaders.add(new VanillaLoader());
    }

    public void loadItems() {
        this.itemConfig.load();
        this.items.clear();

        if (!this.itemConfig.isConfigurationSection("items")) {
            this.itemConfig.setHeader("This is where you configure the power cell items.",
                    "Supported item types: vanilla, slimefun, itembridge, itemsadder",
                    "Note: Vanilla items should be placed at the bottom to prevent conflicts.");

            List<String> oldItems = Settings.ITEM_VALUES.getStringList();
            if (!oldItems.isEmpty()) {
                convertOldItems(oldItems);
            } else {
                this.itemConfig.addDefault("items.0.type", "vanilla");
                this.itemConfig.addDefault("items.0.item", "DIAMOND");
                this.itemConfig.addDefault("items.0.value", 120);

                this.itemConfig.addDefault("items.1.type", "vanilla");
                this.itemConfig.addDefault("items.1.item", "IRON_INGOT");
                this.itemConfig.addDefault("items.1.value", 30);
            }
        }

        if (!this.itemConfig.isConfigurationSection("recipe")) {
            List<String> oldRecipe = Settings.POWERCELL_RECIPE.getStringList();
            if (!oldRecipe.isEmpty()) {
                convertOldRecipe(oldRecipe);
            } else {
                // We use this because putting in the items manually would be a lot of lines.
                convertOldRecipe(Arrays.asList("3:IRON_INGOT", "4:DIAMOND", "5:IRON_INGOT",
                        "12:DIAMOND", "13:IRON_INGOT", "14:DIAMOND",
                        "21:IRON_INGOT", "22:DIAMOND", "23:IRON_INGOT"));
            }
        }


        this.itemConfig.options().copyDefaults(true);
        this.itemConfig.save();

        for (String key : this.itemConfig.getConfigurationSection("items").getKeys(false)) {
            String prefix = "items." + key;

            String type = this.itemConfig.getString(prefix + ".type");
            String item = this.itemConfig.getString(prefix + ".item");
            int value = this.itemConfig.getInt(prefix + ".value");

            this.itemLoaders.stream().filter(loader -> loader.getName().equalsIgnoreCase(type)).findAny().ifPresent(loader -> {
                if (loader.getItem(item) != null) {
                    this.items.add(new PowerCellItem(loader.getItem(item), loader.loadItem(item), value));
                } else {
                    this.plugin.getLogger().warning("Failed to load item: " + item + " with type: " + type);
                }
            });
        }

        for (String key : this.itemConfig.getConfigurationSection("recipe").getKeys(false)) {
            String prefix = "recipe." + key;

            String type = this.itemConfig.getString(prefix + ".type");
            String item = this.itemConfig.getString(prefix + ".item");
            int slot = this.itemConfig.getInt(prefix + ".slot");

            this.itemLoaders.stream().filter(loader -> loader.getName().equalsIgnoreCase(type)).findAny().ifPresent(loader -> {
                this.recipe.put(slot, new PowerCellItem(loader.getItem(item), loader.loadItem(item), 0));
            });
        }
    }

    private void convertOldItems(List<String> oldItems) {
        int currentItem = 0;
        for (String oldItem : oldItems) {
            String[] split = oldItem.split(":");
            if (split.length == 2) {
                this.itemConfig.set("items." + currentItem + ".type", "vanilla");
                this.itemConfig.set("items." + currentItem + ".item", split[0]);
                this.itemConfig.set("items." + currentItem + ".value", Integer.parseInt(split[1]));
                currentItem++;
            }
        }
    }

    private void convertOldRecipe(List<String> recipe) {
        int currentItem = 0;
        for (String oldItem : recipe) {
            String[] split = oldItem.split(":");
            if (split.length == 2) {
                this.itemConfig.set("recipe." + currentItem + ".type", "vanilla");
                this.itemConfig.set("recipe." + currentItem + ".item", split[1]);
                this.itemConfig.set("recipe." + currentItem + ".slot", Integer.parseInt(split[0]));
                currentItem++;
            }
        }
    }

    public List<PowerCellItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public Map<Integer, PowerCellItem> getRecipe() {
        return Collections.unmodifiableMap(this.recipe);
    }

    public int getItemValue(ItemStack itemStack) {
        Optional<PowerCellItem> optional = this.items.stream().filter(powerCellItem -> powerCellItem.isSimilar(itemStack)).findAny();
        return optional.map(PowerCellItem::getValue).orElse(0);
    }
}
