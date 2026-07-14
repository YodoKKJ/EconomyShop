package com.brunobeduschi.economyshop;

import com.brunobeduschi.economyshop.chatpack.ResourcePackServer;
import com.brunobeduschi.economyshop.commands.BalanceCommand;
import com.brunobeduschi.economyshop.commands.EcoAdminCommand;
import com.brunobeduschi.economyshop.commands.PayCommand;
import com.brunobeduschi.economyshop.commands.ShopCommand;
import com.brunobeduschi.economyshop.commands.TagCommand;
import com.brunobeduschi.economyshop.economy.EconomyManager;
import com.brunobeduschi.economyshop.gui.TagGuiManager;
import com.brunobeduschi.economyshop.listeners.ChatFormatListener;
import com.brunobeduschi.economyshop.listeners.PlayerDataListener;
import com.brunobeduschi.economyshop.listeners.ResourcePackJoinListener;
import com.brunobeduschi.economyshop.listeners.ShopListener;
import com.brunobeduschi.economyshop.listeners.TagGuiListener;
import com.brunobeduschi.economyshop.shop.ShopManager;
import com.brunobeduschi.economyshop.tag.TagManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class EconomyShopPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private ShopManager shopManager;
    private TagManager tagManager;
    private ResourcePackServer resourcePackServer;
    private TagGuiManager tagGuiManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Preenche no config.yml em disco quaisquer chaves novas que passaram a existir
        // no config.yml padrão do plugin desde a última vez que ele rodou nesse servidor,
        // sem sobrescrever nada que já esteja configurado.
        getConfig().options().copyDefaults(true);
        saveConfig();

        try {
            economyManager = new EconomyManager(this);
        } catch (SQLException e) {
            getLogger().severe("Não foi possível conectar ao banco de dados: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopManager = new ShopManager(this);
        tagManager = new TagManager(this);
        resourcePackServer = new ResourcePackServer(this);
        resourcePackServer.start();
        tagGuiManager = new TagGuiManager(this);

        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatFormatListener(this), this);
        getServer().getPluginManager().registerEvents(new ResourcePackJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new TagGuiListener(this), this);

        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ecoadmin").setExecutor(new EcoAdminCommand(this));
        getCommand("tag").setExecutor(new TagCommand(this));
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.close();
        }
        if (resourcePackServer != null) {
            resourcePackServer.stop();
        }
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public ResourcePackServer getResourcePackServer() {
        return resourcePackServer;
    }

    public TagGuiManager getTagGuiManager() {
        return tagGuiManager;
    }
}
