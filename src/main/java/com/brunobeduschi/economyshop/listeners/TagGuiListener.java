package com.brunobeduschi.economyshop.listeners;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.chatpack.ResourcePackServer;
import com.brunobeduschi.economyshop.gui.TagEditHolder;
import com.brunobeduschi.economyshop.gui.TagListHolder;
import com.brunobeduschi.economyshop.tag.Tag;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class TagGuiListener implements Listener {

    private final EconomyShopPlugin plugin;

    public TagGuiListener(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof TagListHolder) {
            event.setCancelled(true);
            handleListClick(event);
        } else if (event.getInventory().getHolder() instanceof TagEditHolder holder) {
            event.setCancelled(true);
            handleEditClick(event, holder);
        }
    }

    private void handleListClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        ItemMeta meta = clicked.getItemMeta();
        String tagId = meta.getPersistentDataContainer().get(plugin.getTagGuiManager().getTagIdKey(), PersistentDataType.STRING);
        if (tagId == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        player.openInventory(plugin.getTagGuiManager().buildEditInventory(tagId));
    }

    private void handleEditClick(InventoryClickEvent event, TagEditHolder holder) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        if (clicked.getType() == Material.ARROW) {
            player.openInventory(plugin.getTagGuiManager().buildListInventory());
            return;
        }

        String colorName = clicked.getItemMeta().getPersistentDataContainer()
                .get(plugin.getTagGuiManager().getColorKey(), PersistentDataType.STRING);
        if (colorName == null) {
            return;
        }
        NamedTextColor newColor = NamedTextColor.NAMES.value(colorName);
        if (newColor == null) {
            return;
        }

        applyColor(player, holder.getTagId(), newColor);
    }

    private void applyColor(Player player, String tagId, NamedTextColor newColor) {
        Tag tag = plugin.getTagManager().findTag(tagId);
        if (tag == null) {
            return;
        }

        plugin.getTagManager().updateTagColor(tagId, newColor);

        if (tag.badgeChar() != null) {
            try {
                String label = tag.display().replaceAll("[\\[\\]]", "").toUpperCase();
                Color awtColor = new Color(newColor.red(), newColor.green(), newColor.blue());
                plugin.getResourcePackServer().updateBadgeColor(tagId, label, awtColor);
                pushResourcePackToOnlinePlayers();
                player.sendMessage(Component.text("Badge atualizado! Reenviando o resource pack pros jogadores online...", NamedTextColor.GREEN));
            } catch (IOException | NoSuchAlgorithmException e) {
                plugin.getLogger().severe("Falha ao regenerar badge: " + e.getMessage());
                player.sendMessage(Component.text("Cor salva, mas houve um erro ao regenerar a imagem do badge.", NamedTextColor.RED));
            }
        } else {
            player.sendMessage(Component.text("Cor da tag atualizada!", NamedTextColor.GREEN));
        }

        player.openInventory(plugin.getTagGuiManager().buildEditInventory(tagId));
    }

    private void pushResourcePackToOnlinePlayers() {
        ResourcePackServer server = plugin.getResourcePackServer();
        for (Player online : Bukkit.getOnlinePlayers()) {
            ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(
                    UUID.randomUUID(), URI.create(server.getPublicUrl()), server.getHash());
            ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                    .packs(info)
                    .required(false)
                    .prompt(Component.text("As tags de chat foram atualizadas! Baixe o novo resource pack.", NamedTextColor.AQUA))
                    .build();
            online.sendResourcePacks(request);
        }
    }
}
