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
    private final File playerTagsFile;
    private final YamlConfiguration playerTagsData;
    private final File customTagsFile;
    private final YamlConfiguration customTagsData;
    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final String defaultTagId;

    public TagManager(EconomyShopPlugin plugin) {
        this.plugin = plugin;
        this.playerTagsFile = new File(plugin.getDataFolder(), "playertags.yml");
        this.playerTagsData = YamlConfiguration.loadConfiguration(playerTagsFile);
        this.customTagsFile = new File(plugin.getDataFolder(), "tags.yml");
        this.customTagsData = YamlConfiguration.loadConfiguration(customTagsFile);
        this.defaultTagId = plugin.getConfig().getString("default-tag", "membro").toLowerCase();

        loadTagsFrom(plugin.getConfig().getConfigurationSection("tags"));
        loadTagsFrom(customTagsData.getConfigurationSection("tags"));
    }

    private void loadTagsFrom(ConfigurationSection section) {
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

    public NamedTextColor parseColorOrNull(String name) {
        return NamedTextColor.NAMES.value(name.toLowerCase());
    }

    public Tag getTag(Player player) {
        String id = playerTagsData.getString(player.getUniqueId().toString(), defaultTagId).toLowerCase();
        return tags.getOrDefault(id, getDefaultTag());
    }

    public Tag getDefaultTag() {
        return tags.getOrDefault(defaultTagId, new Tag(defaultTagId, "[Membro]", NamedTextColor.GRAY));
    }

    public boolean hasTag(String id) {
        return tags.containsKey(id.toLowerCase());
    }

    public void createTag(String id, String display, NamedTextColor color) {
        String key = id.toLowerCase();
        tags.put(key, new Tag(key, display, color));
        customTagsData.set("tags." + key + ".display", display);
        customTagsData.set("tags." + key + ".color", NamedTextColor.NAMES.key(color));
        try {
            plugin.getDataFolder().mkdirs();
            customTagsData.save(customTagsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao salvar tag customizada: " + e.getMessage());
        }
    }

    public boolean setTag(Player player, String tagId) {
        if (!hasTag(tagId)) {
            return false;
        }
        playerTagsData.set(player.getUniqueId().toString(), tagId.toLowerCase());
        savePlayerTags();
        return true;
    }

    public Collection<Tag> getTags() {
        return tags.values();
    }

    private void savePlayerTags() {
        try {
            plugin.getDataFolder().mkdirs();
            playerTagsData.save(playerTagsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao salvar tags dos jogadores: " + e.getMessage());
        }
    }
}
