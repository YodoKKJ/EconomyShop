package com.brunobeduschi.economyshop.gui;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class TagGuiManager {

    private final EconomyShopPlugin plugin;
    private final NamespacedKey tagIdKey;
    private final NamespacedKey colorKey;

    public TagGuiManager(EconomyShopPlugin plugin) {
        this.plugin = plugin;
        this.tagIdKey = new NamespacedKey(plugin, "tag_id");
        this.colorKey = new NamespacedKey(plugin, "color_key");
    }

    public NamespacedKey getTagIdKey() {
        return tagIdKey;
    }

    public NamespacedKey getColorKey() {
        return colorKey;
    }

    public Inventory buildListInventory() {
        TagListHolder holder = new TagListHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, Component.text("Tags - clique para editar"));
        holder.setInventory(inventory);

        int slot = 0;
        for (Tag tag : plugin.getTagManager().getTags()) {
            if (slot >= 54) {
                break;
            }
            ItemStack item = new ItemStack(ColorPalette.woolFor(tag.color()));
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(tag.display(), tag.color()).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("id: " + tag.id(), NamedTextColor.GRAY));
            lore.add(Component.text(tag.badgeChar() != null ? "Badge de imagem: sim" : "Badge de imagem: não (texto colorido)", NamedTextColor.GRAY));
            lore.add(Component.text("Clique para editar a cor", NamedTextColor.YELLOW));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(tagIdKey, PersistentDataType.STRING, tag.id());
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
        }
        return inventory;
    }

    public Inventory buildEditInventory(String tagId) {
        Tag tag = plugin.getTagManager().findTag(tagId);
        if (tag == null) {
            return null;
        }

        TagEditHolder holder = new TagEditHolder(tagId);
        Inventory inventory = Bukkit.createInventory(holder, 45, Component.text("Editar tag: " + tag.id()));
        holder.setInventory(inventory);

        ItemStack preview = new ItemStack(ColorPalette.woolFor(tag.color()));
        ItemMeta previewMeta = preview.getItemMeta();
        previewMeta.displayName(Component.text(tag.display(), tag.color()).decoration(TextDecoration.ITALIC, false));
        List<Component> previewLore = new ArrayList<>();
        previewLore.add(Component.text("Prévia da tag", NamedTextColor.GRAY));
        previewLore.add(Component.text("Cor atual: " + NamedTextColor.NAMES.key(tag.color()), NamedTextColor.GRAY));
        previewMeta.lore(previewLore);
        preview.setItemMeta(previewMeta);
        inventory.setItem(4, preview);

        List<NamedTextColor> colors = ColorPalette.ORDERED;
        for (int i = 0; i < colors.size(); i++) {
            int slotIndex = i < 8 ? 10 + i : 19 + (i - 8);
            NamedTextColor color = colors.get(i);
            ItemStack swatch = new ItemStack(ColorPalette.woolFor(color));
            ItemMeta swatchMeta = swatch.getItemMeta();
            swatchMeta.displayName(Component.text(NamedTextColor.NAMES.key(color), color).decoration(TextDecoration.ITALIC, false));
            swatchMeta.getPersistentDataContainer().set(colorKey, PersistentDataType.STRING, NamedTextColor.NAMES.key(color));
            swatch.setItemMeta(swatchMeta);
            inventory.setItem(slotIndex, swatch);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Voltar", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        inventory.setItem(40, back);

        return inventory;
    }
}
