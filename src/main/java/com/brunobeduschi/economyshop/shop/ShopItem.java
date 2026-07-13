package com.brunobeduschi.economyshop.shop;

import org.bukkit.Material;

public record ShopItem(Material material, String displayName, double buyPrice, double sellPrice, int slot) {
}
