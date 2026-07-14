package com.brunobeduschi.economyshop.gui;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class ColorPalette {

    public static final List<NamedTextColor> ORDERED = List.of(
            NamedTextColor.BLACK, NamedTextColor.DARK_GRAY, NamedTextColor.GRAY, NamedTextColor.WHITE,
            NamedTextColor.DARK_RED, NamedTextColor.RED, NamedTextColor.GOLD, NamedTextColor.YELLOW,
            NamedTextColor.DARK_GREEN, NamedTextColor.GREEN, NamedTextColor.DARK_AQUA, NamedTextColor.AQUA,
            NamedTextColor.DARK_BLUE, NamedTextColor.BLUE, NamedTextColor.DARK_PURPLE, NamedTextColor.LIGHT_PURPLE
    );

    private static final Map<NamedTextColor, Material> WOOL_BY_COLOR = Map.ofEntries(
            Map.entry(NamedTextColor.BLACK, Material.BLACK_WOOL),
            Map.entry(NamedTextColor.DARK_GRAY, Material.GRAY_WOOL),
            Map.entry(NamedTextColor.GRAY, Material.LIGHT_GRAY_WOOL),
            Map.entry(NamedTextColor.WHITE, Material.WHITE_WOOL),
            Map.entry(NamedTextColor.DARK_RED, Material.BROWN_WOOL),
            Map.entry(NamedTextColor.RED, Material.RED_WOOL),
            Map.entry(NamedTextColor.GOLD, Material.ORANGE_WOOL),
            Map.entry(NamedTextColor.YELLOW, Material.YELLOW_WOOL),
            Map.entry(NamedTextColor.DARK_GREEN, Material.GREEN_WOOL),
            Map.entry(NamedTextColor.GREEN, Material.LIME_WOOL),
            Map.entry(NamedTextColor.DARK_AQUA, Material.PINK_WOOL),
            Map.entry(NamedTextColor.AQUA, Material.CYAN_WOOL),
            Map.entry(NamedTextColor.DARK_BLUE, Material.BLUE_WOOL),
            Map.entry(NamedTextColor.BLUE, Material.LIGHT_BLUE_WOOL),
            Map.entry(NamedTextColor.DARK_PURPLE, Material.PURPLE_WOOL),
            Map.entry(NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_WOOL)
    );

    public static Material woolFor(NamedTextColor color) {
        return WOOL_BY_COLOR.getOrDefault(color, Material.WHITE_WOOL);
    }

    private ColorPalette() {
    }
}
