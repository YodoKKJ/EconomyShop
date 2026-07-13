package com.brunobeduschi.economyshop.commands;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final EconomyShopPlugin plugin;

    public ShopCommand(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar este comando.", NamedTextColor.RED));
            return true;
        }
        player.openInventory(plugin.getShopManager().buildInventory());
        return true;
    }
}
