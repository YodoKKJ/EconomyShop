package com.brunobeduschi.economyshop.commands;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyShopPlugin plugin;

    public BalanceCommand(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Apenas jogadores podem usar este comando sem argumentos.", NamedTextColor.RED));
                return true;
            }
            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            player.sendMessage(Component.text("Seu saldo: ", NamedTextColor.GOLD)
                    .append(Component.text(format(balance), NamedTextColor.GREEN)));
            return true;
        }

        if (!sender.hasPermission("economyshop.balance.others")) {
            sender.sendMessage(Component.text("Você não tem permissão para ver o saldo de outros jogadores.", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Jogador não encontrado ou offline.", NamedTextColor.RED));
            return true;
        }

        double balance = plugin.getEconomyManager().getBalance(target.getUniqueId());
        sender.sendMessage(Component.text("Saldo de " + target.getName() + ": ", NamedTextColor.GOLD)
                .append(Component.text(format(balance), NamedTextColor.GREEN)));
        return true;
    }

    private String format(double value) {
        return String.format("$ %,.2f", value);
    }
}
