package com.brunobeduschi.economyshop.commands;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.economy.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class EcoAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ACTIONS = List.of("give", "take", "set");

    private final EconomyShopPlugin plugin;

    public EcoAdminCommand(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economyshop.admin")) {
            sender.sendMessage(Component.text("Você não tem permissão para usar este comando.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 3 || !ACTIONS.contains(args[0].toLowerCase())) {
            sender.sendMessage(Component.text("Uso: /ecoadmin <give|take|set> <jogador> <quantia>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Jogador não encontrado ou offline.", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Quantia inválida.", NamedTextColor.RED));
            return true;
        }

        EconomyManager economy = plugin.getEconomyManager();
        switch (args[0].toLowerCase()) {
            case "give" -> economy.deposit(target.getUniqueId(), amount);
            case "take" -> economy.withdraw(target.getUniqueId(), amount);
            case "set" -> economy.setBalance(target.getUniqueId(), amount);
        }

        sender.sendMessage(Component.text("Saldo de %s atualizado.".formatted(target.getName()), NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return ACTIONS.stream()
                    .filter(action -> action.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
