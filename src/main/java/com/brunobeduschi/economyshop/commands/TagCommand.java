package com.brunobeduschi.economyshop.commands;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.tag.Tag;
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
import java.util.stream.Stream;

public class TagCommand implements CommandExecutor, TabCompleter {

    private final EconomyShopPlugin plugin;

    public TagCommand(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Uso: /tag <set|list>", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> showTags(sender);
            case "set" -> setTag(sender, args);
            default -> sender.sendMessage(Component.text("Uso: /tag <set|list>", NamedTextColor.RED));
        }
        return true;
    }

    private void showTags(CommandSender sender) {
        sender.sendMessage(Component.text("=== Tags disponíveis ===", NamedTextColor.GOLD));
        for (Tag tag : plugin.getTagManager().getTags()) {
            sender.sendMessage(Component.text(tag.id() + " → ", NamedTextColor.GRAY)
                    .append(Component.text(tag.display(), tag.color())));
        }
    }

    private void setTag(CommandSender sender, String[] args) {
        if (!sender.hasPermission("economyshop.tag.admin")) {
            sender.sendMessage(Component.text("Você não tem permissão para isso.", NamedTextColor.RED));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(Component.text("Uso: /tag set <jogador> <tag>", NamedTextColor.RED));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Jogador não encontrado ou offline.", NamedTextColor.RED));
            return;
        }
        boolean success = plugin.getTagManager().setTag(target, args[2]);
        if (success) {
            sender.sendMessage(Component.text("Tag de " + target.getName() + " definida para " + args[2] + ".", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Tag '" + args[2] + "' não existe. Use /tag list.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("set", "list")
                    .filter(option -> option.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return plugin.getTagManager().getTags().stream()
                    .map(Tag::id)
                    .filter(id -> id.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
