package com.alfie51m.pronounsMC;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PronounsMC_Expansion extends PlaceholderExpansion {

    private final PronounsMC plugin;

    public PronounsMC_Expansion(PronounsMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "Pronouns";
    }

    @Override
    public String getAuthor() { return "Alfie51m"; }

    @Override
    public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin("PronounsMC") != null;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        PronounsMC pluginInstance = (PronounsMC) Bukkit.getPluginManager().getPlugin("PronounsMC");
        if (pluginInstance == null) {
            return "";
        }

        if (identifier.isEmpty()) {
            String rawKey = pluginInstance.getDatabase().getPronouns(player.getUniqueId().toString());
            if (rawKey == null) {
                return "";
            }
            return pluginInstance.getColoredPronoun(rawKey);
        }

        return null;
    }
}
