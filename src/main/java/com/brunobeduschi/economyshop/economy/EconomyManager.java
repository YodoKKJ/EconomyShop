package com.brunobeduschi.economyshop.economy;

import com.brunobeduschi.economyshop.EconomyShopPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {

    private final EconomyShopPlugin plugin;
    private final Connection connection;
    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();

    public EconomyManager(EconomyShopPlugin plugin) throws SQLException {
        this.plugin = plugin;
        File dbFile = new File(plugin.getDataFolder(), "economy.db");
        plugin.getDataFolder().mkdirs();
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS balances (uuid TEXT PRIMARY KEY, balance REAL NOT NULL)");
        }
    }

    public double getBalance(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> loadBalance(id));
    }

    public void loadPlayer(UUID uuid) {
        cache.put(uuid, loadBalance(uuid));
    }

    private double loadBalance(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM balances WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Falha ao carregar saldo de " + uuid + ": " + e.getMessage());
        }
        return plugin.getConfig().getDouble("starting-balance", 100.0);
    }

    public void savePlayer(UUID uuid) {
        Double balance = cache.get(uuid);
        if (balance == null) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO balances (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance")) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, balance);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Falha ao salvar saldo de " + uuid + ": " + e.getMessage());
        }
    }

    public void unload(UUID uuid) {
        savePlayer(uuid);
        cache.remove(uuid);
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public void deposit(UUID uuid, double amount) {
        cache.merge(uuid, amount, Double::sum);
    }

    public boolean withdraw(UUID uuid, double amount) {
        double balance = getBalance(uuid);
        if (balance < amount) {
            return false;
        }
        cache.put(uuid, balance - amount);
        return true;
    }

    public void setBalance(UUID uuid, double amount) {
        cache.put(uuid, Math.max(0, amount));
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            savePlayer(uuid);
        }
    }

    public void close() {
        saveAll();
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
