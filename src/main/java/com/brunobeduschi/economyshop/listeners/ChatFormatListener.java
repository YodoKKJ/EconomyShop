package com.brunobeduschi.economyshop.listeners;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.tag.Tag;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatFormatListener implements Listener {

    private final EconomyShopPlugin plugin;

    public ChatFormatListener(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Tag tag = plugin.getTagManager().getTag(player);
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component badge = Component.text(tag.display() + " ", tag.color());
            Component name = Component.text(source.getName(), NamedTextColor.WHITE)
                    .hoverEvent(HoverEvent.showText(Component.text("Saldo: $ %,.2f".formatted(balance), NamedTextColor.GREEN)))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + source.getName() + " "));
            return badge.append(name)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(message);
        });
    }
}
