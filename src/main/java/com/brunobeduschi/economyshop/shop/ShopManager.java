package com.brunobeduschi.economyshop.shop;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    private final EconomyShopPlugin plugin;
    private final List<ShopItem> items = new ArrayList<>();

    public ShopManager(EconomyShopPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        items.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop-items");
        if (section == null) {
            return;
        }
        int slot = 0;
        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }
            Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
            if (material == null) {
                plugin.getLogger().warning("Material inválido para o item de loja '" + key + "'");
                continue;
            }
            String displayName = itemSection.getString("name", key);
            double buyPrice = itemSection.getDouble("buy-price", 0);
            double sellPrice = itemSection.getDouble("sell-price", 0);
            items.add(new ShopItem(material, displayName, buyPrice, sellPrice, slot++));
        }
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public ShopItem getItemBySlot(int slot) {
        return items.stream().filter(item -> item.slot() == slot).findFirst().orElse(null);
    }

    public Inventory buildInventory() {
        ShopHolder holder = new ShopHolder();
        int rows = Math.max(1, (int) Math.ceil(items.size() / 9.0));
        Inventory inventory = Bukkit.createInventory(holder, rows * 9, Component.text("Loja"));
        holder.setInventory(inventory);

        for (ShopItem item : items) {
            ItemStack stack = new ItemStack(item.material());
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(Component.text(item.displayName(), NamedTextColor.YELLOW));
            List<Component> lore = new ArrayList<>();
            if (item.buyPrice() > 0) {
                lore.add(Component.text("Comprar: $ %.2f (clique esquerdo)".formatted(item.buyPrice()), NamedTextColor.GREEN));
            }
            if (item.sellPrice() > 0) {
                lore.add(Component.text("Vender: $ %.2f (clique direito)".formatted(item.sellPrice()), NamedTextColor.RED));
            }
            meta.lore(lore);
            stack.setItemMeta(meta);
            inventory.setItem(item.slot(), stack);
        }
        return inventory;
    }
}
