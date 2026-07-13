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

public class PayCommand implements CommandExecutor, TabCompleter {

    private final EconomyShopPlugin plugin;

    public PayCommand(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar este comando.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Uso: /pay <jogador> <quantia>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Jogador não encontrado ou offline.", NamedTextColor.RED));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Você não pode pagar a si mesmo.", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Quantia inválida.", NamedTextColor.RED));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(Component.text("A quantia deve ser maior que zero.", NamedTextColor.RED));
            return true;
        }

        EconomyManager economy = plugin.getEconomyManager();
        if (!economy.withdraw(player.getUniqueId(), amount)) {
            player.sendMessage(Component.text("Saldo insuficiente.", NamedTextColor.RED));
            return true;
        }
        economy.deposit(target.getUniqueId(), amount);

        player.sendMessage(Component.text("Você pagou $ %.2f para %s.".formatted(amount, target.getName()), NamedTextColor.GREEN));
        target.sendMessage(Component.text("Você recebeu $ %.2f de %s.".formatted(amount, player.getName()), NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
