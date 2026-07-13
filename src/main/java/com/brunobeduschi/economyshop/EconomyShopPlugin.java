package com.brunobeduschi.economyshop;

import com.brunobeduschi.economyshop.commands.BalanceCommand;
import com.brunobeduschi.economyshop.commands.EcoAdminCommand;
import com.brunobeduschi.economyshop.commands.PayCommand;
import com.brunobeduschi.economyshop.commands.ShopCommand;
import com.brunobeduschi.economyshop.economy.EconomyManager;
import com.brunobeduschi.economyshop.listeners.PlayerDataListener;
import com.brunobeduschi.economyshop.listeners.ShopListener;
import com.brunobeduschi.economyshop.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class EconomyShopPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            economyManager = new EconomyManager(this);
        } catch (SQLException e) {
            getLogger().severe("Não foi possível conectar ao banco de dados: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopManager = new ShopManager(this);

        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ecoadmin").setExecutor(new EcoAdminCommand(this));
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.close();
        }
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}
