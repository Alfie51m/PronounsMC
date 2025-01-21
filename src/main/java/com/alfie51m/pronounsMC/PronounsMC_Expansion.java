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
        return "pronouns";
    }

    @Override
    public String getAuthor() {
        return "Alfie51m";
    }

    @Override
    public String getVersion() { return "1.1.0"; }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return (Bukkit.getPluginManager().getPlugin("PronounsMC") != null);
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        PronounsMC plugin = (PronounsMC) Bukkit.getPluginManager().getPlugin("PronounsMC");
        if (plugin == null) {
            return "";
        }

        if (identifier.isEmpty()) {
            String rawKey = plugin.getPronouns(player.getUniqueId().toString());
            if (rawKey == null) {
                return "";
            }
            return plugin.getColoredPronoun(rawKey);
        }

        return null;
    }
}
