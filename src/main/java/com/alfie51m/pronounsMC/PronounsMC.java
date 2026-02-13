package com.alfie51m.pronounsMC;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PronounsMC extends JavaPlugin {

    private DatabaseManager database;
    private LangManager lang;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        lang = new LangManager(this);
        database = new DatabaseManager(this);

        try {
            database.connect();
            database.setup();
        } catch (Exception e) {
            getLogger().severe("Failed to connect to the database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PronounsCommand command = new PronounsCommand(this);
        getCommand("pronouns").setExecutor(command);
        getCommand("pronouns").setTabCompleter(command);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getScheduler().runTask(this, () -> new PronounsMC_Expansion(this).register());
        } else {
            getLogger().warning("PlaceholderAPI is not installed! Placeholders won't be available.");
        }
    }

    @Override
    public void onDisable() {
        database.close();
    }

    public DatabaseManager getDatabase() { return database; }
    public LangManager getLang() { return lang; }
    public FileConfiguration getPluginConfig() { return config; }

    public String getColoredPronoun(String rawKey) {
        String path = "availablePronouns." + rawKey;

        if (config.isString(path)) {
            return ColorUtil.color(config.getString(path));
        } else if (config.getBoolean("userSuppliedPronouns", false)) {
            String template = config.getString("defaultProunounTemplate", "&7(%s)&r");
            return ColorUtil.color(String.format(template, rawKey));
        } else {
            return ColorUtil.color("&7" + rawKey);
        }
    }


    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        lang.reload();
    }
}
