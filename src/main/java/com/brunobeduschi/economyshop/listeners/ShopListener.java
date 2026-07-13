package com.brunobeduschi.economyshop.listeners;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.economy.EconomyManager;
import com.brunobeduschi.economyshop.shop.ShopHolder;
import com.brunobeduschi.economyshop.shop.ShopItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final EconomyShopPlugin plugin;

    public ShopListener(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopHolder)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof ShopHolder)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ShopItem item = plugin.getShopManager().getItemBySlot(event.getSlot());
        if (item == null) {
            return;
        }

        EconomyManager economy = plugin.getEconomyManager();

        if (event.isLeftClick()) {
            if (item.buyPrice() <= 0) {
                player.sendMessage(Component.text("Este item não pode ser comprado.", NamedTextColor.RED));
                return;
            }
            if (!economy.withdraw(player.getUniqueId(), item.buyPrice())) {
                player.sendMessage(Component.text("Saldo insuficiente.", NamedTextColor.RED));
                return;
            }
            player.getInventory().addItem(new ItemStack(item.material()));
            player.sendMessage(Component.text("Você comprou %s por $ %.2f.".formatted(item.displayName(), item.buyPrice()), NamedTextColor.GREEN));
        } else if (event.isRightClick()) {
            if (item.sellPrice() <= 0) {
                player.sendMessage(Component.text("Este item não pode ser vendido.", NamedTextColor.RED));
                return;
            }
            if (!player.getInventory().containsAtLeast(new ItemStack(item.material()), 1)) {
                player.sendMessage(Component.text("Você não tem esse item para vender.", NamedTextColor.RED));
                return;
            }
            player.getInventory().removeItem(new ItemStack(item.material()));
            economy.deposit(player.getUniqueId(), item.sellPrice());
            player.sendMessage(Component.text("Você vendeu %s por $ %.2f.".formatted(item.displayName(), item.sellPrice()), NamedTextColor.GREEN));
        }
    }
}
