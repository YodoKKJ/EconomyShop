package com.brunobeduschi.economyshop.tag;

import com.brunobeduschi.economyshop.EconomyShopPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TagManager {

    private final EconomyShopPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final String defaultTagId;

    public TagManager(EconomyShopPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playertags.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        this.defaultTagId = plugin.getConfig().getString("default-tag", "membro").toLowerCase();
        loadTags();
    }

    private void loadTags() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tags");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection tagSection = section.getConfigurationSection(id);
            if (tagSection == null) {
                continue;
            }
            String display = tagSection.getString("display", "[" + id.toUpperCase() + "]");
            NamedTextColor color = parseColor(tagSection.getString("color", "gray"));
            tags.put(id.toLowerCase(), new Tag(id.toLowerCase(), display, color));
        }
    }

    private NamedTextColor parseColor(String name) {
        NamedTextColor color = NamedTextColor.NAMES.value(name.toLowerCase());
        return color != null ? color : NamedTextColor.GRAY;
    }

    public Tag getTag(Player player) {
        String id = data.getString(player.getUniqueId().toString(), defaultTagId).toLowerCase();
        return tags.getOrDefault(id, getDefaultTag());
    }

    public Tag getDefaultTag() {
        return tags.getOrDefault(defaultTagId, new Tag(defaultTagId, "[Membro]", NamedTextColor.GRAY));
    }

    public boolean hasTag(String id) {
        return tags.containsKey(id.toLowerCase());
    }

    public boolean setTag(Player player, String tagId) {
        if (!hasTag(tagId)) {
            return false;
        }
        data.set(player.getUniqueId().toString(), tagId.toLowerCase());
        save();
        return true;
    }

    public Collection<Tag> getTags() {
        return tags.values();
    }

    private void save() {
        try {
            plugin.getDataFolder().mkdirs();
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao salvar tags dos jogadores: " + e.getMessage());
        }
    }
}
