package com.brunobeduschi.economyshop.listeners;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import com.brunobeduschi.economyshop.chatpack.ResourcePackServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.util.UUID;

public class ResourcePackJoinListener implements Listener {

    private final EconomyShopPlugin plugin;

    public ResourcePackJoinListener(EconomyShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ResourcePackServer server = plugin.getResourcePackServer();
        if (server == null || !server.isRunning()) {
            return;
        }

        Player player = event.getPlayer();
        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(
                UUID.randomUUID(), URI.create(server.getPublicUrl()), server.getHash());
        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .required(false)
                .prompt(Component.text("Baixe o resource pack para ver as tags de chat com badges coloridos!", NamedTextColor.AQUA))
                .build();
        player.sendResourcePacks(request);
    }
}
