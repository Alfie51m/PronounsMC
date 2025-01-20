package com.alfie51m.pronounsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;

public class PronounsPlugin extends JavaPlugin implements TabExecutor {

    private Connection connection;
    private FileConfiguration config;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        loadLangFile();

        try {
            connectToDatabase();
            setupDatabase();
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to the database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getCommand("pronouns") != null) {
            getCommand("pronouns").setExecutor(this);
            getCommand("pronouns").setTabCompleter(this);
        }
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().warning("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    private void loadLangFile() {
        String langFileName = config.getString("langFile", "en_US");
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        File langFile = new File(langFolder, langFileName + ".yml");

        if (!langFile.exists()) {
            saveResource("lang/" + langFileName + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private void connectToDatabase() throws SQLException {
        String dbType = config.getString("database.type", "mysql").toLowerCase();
        if (dbType.equals("mysql")) {
            // MySQL
            String host = config.getString("database.host", "localhost");
            int port = config.getInt("database.port", 3306);
            String dbName = config.getString("database.name", "minecraft");
            String user = config.getString("database.user", "root");
            String pass = config.getString("database.password", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
            connection = DriverManager.getConnection(url, user, pass);
            getLogger().info("Connected to MySQL database.");
        } else {
            // SQLite
            File dbFile = new File(getDataFolder(), "pronouns.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            getLogger().info("Connected to SQLite database at " + dbFile.getAbsolutePath());
        }
    }

    private void setupDatabase() throws SQLException {
        String dbType = config.getString("database.type", "mysql").toLowerCase();
        String createTableQuery;

        if (dbType.equals("mysql")) {
            createTableQuery =
                    "CREATE TABLE IF NOT EXISTS pronouns (" +
                            "  uuid VARCHAR(36) PRIMARY KEY," +
                            "  pronoun VARCHAR(100)" +
                            ");";
        } else {
            createTableQuery =
                    "CREATE TABLE IF NOT EXISTS pronouns (" +
                            "  uuid TEXT PRIMARY KEY," +
                            "  pronoun TEXT" +
                            ");";
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("pronouns")) return false;

        if (args.length == 0) {
            sender.sendMessage(color(getLang("messages.usageMain", "&cUsage: /pronouns <command>")));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "get": {
                if (args.length < 2) {
                    sender.sendMessage(color(getLang("messages.usageGet", "&cUsage: /pronouns get <username>")));
                    return true;
                }
                if (!sender.hasPermission("pronouns.get")) {
                    sender.sendMessage(color(getLang("messages.noPermission", "&cYou don't have permission.")));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color(getLang("messages.playerNotFound", "&cPlayer not found!")));
                    return true;
                }

                String storedKey = getPronouns(target.getUniqueId().toString());
                if (storedKey == null) {
                    String notSetMsg = color(getLang("messages.notSet", "&7Not set"));
                    String format = getLang("messages.playerPronounFormat", "{player}'s pronouns: {pronouns}");
                    format = format.replace("{player}", target.getName())
                            .replace("{pronouns}", notSetMsg);
                    sender.sendMessage(color(format));
                } else {
                    String colorized = getColoredPronoun(storedKey);
                    String format = getLang("messages.playerPronounFormat", "&a{player}'s pronouns: &r{pronouns}");
                    format = format.replace("{player}", target.getName())
                            .replace("{pronouns}", colorized);
                    sender.sendMessage(color(format));
                }
                return true;
            }

            case "list": {
                if (!config.isConfigurationSection("availablePronouns")) {
                    sender.sendMessage(color(getLang("messages.noPronounsConfigured", "&cNo pronouns configured.")));
                    return true;
                }
                sender.sendMessage(color(getLang("messages.availablePronounsHeader", "&aAvailable pronouns:")));

                Set<String> keys = config.getConfigurationSection("availablePronouns").getKeys(false);
                for (String key : keys) {
                    String colorized = getColoredPronoun(key);
                    sender.sendMessage(ChatColor.AQUA + "- " + key + ChatColor.GRAY + ": " + colorized);
                }
                return true;
            }

            case "reload": {
                if (!sender.hasPermission("pronouns.reload")) {
                    sender.sendMessage(color(getLang("messages.noPermission", "&cYou don't have permission.")));
                    return true;
                }

                reloadConfig();
                config = getConfig();

                loadLangFile();

                sender.sendMessage(color(getLang("messages.pluginReloaded", "&aPronounsPlugin config reloaded.")));
                return true;
            }

            default: {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(color(getLang("messages.onlyPlayers", "&cOnly players can set pronouns.")));
                    return true;
                }
                String chosenPronoun = subCommand;
                if (!config.contains("availablePronouns." + chosenPronoun)) {
                    sender.sendMessage(color(getLang("messages.invalidPronoun",
                            "&cInvalid pronoun. Use /pronouns list to see available options.")));
                    return true;
                }

                Player player = (Player) sender;
                setPronouns(player.getUniqueId().toString(), chosenPronoun);

                String colorized = getColoredPronoun(chosenPronoun);
                String msgTemplate = getLang("messages.pronounSet", "&aYour pronouns have been set to: &r{pronouns}");
                msgTemplate = msgTemplate.replace("{pronouns}", colorized);

                sender.sendMessage(color(msgTemplate));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("pronouns")) return null;

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("get");
            subCommands.add("list");
            if (sender.hasPermission("pronouns.reload")) {
                subCommands.add("reload");
            }
            if (config.isConfigurationSection("availablePronouns")) {
                subCommands.addAll(config.getConfigurationSection("availablePronouns").getKeys(false));
            }
            return subCommands;
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> playerNames.add(p.getName()));
            return playerNames;
        }
        return null;
    }

    public String getPronouns(String uuid) {
        String query = "SELECT pronoun FROM pronouns WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("pronoun");
            }
        } catch (SQLException e) {
            getLogger().warning("Failed to fetch pronouns: " + e.getMessage());
        }
        return null;
    }

    public void setPronouns(String uuid, String pronoun) {
        String dbType = config.getString("database.type", "mysql").toLowerCase();

        String query;
        if (dbType.equals("mysql")) {
            // MySQL uses ON DUPLICATE KEY
            query = "INSERT INTO pronouns (uuid, pronoun) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE pronoun = ?";
        } else {
            // SQLite uses INSERT OR REPLACE
            query = "INSERT OR REPLACE INTO pronouns (uuid, pronoun) VALUES (?, ?)";
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, pronoun);
            if (dbType.equals("mysql")) {
                stmt.setString(3, pronoun);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            getLogger().warning("Failed to set pronouns: " + e.getMessage());
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String getLang(String path, String def) {
        if (langConfig == null) {
            return def;
        }
        return langConfig.getString(path, def);
    }

    public String getColoredPronoun(String rawKey) {
        String path = "availablePronouns." + rawKey;
        if (!config.isString(path)) {
            return color(getLang("messages.notSet", "&7Not set"));
        }
        return color(config.getString(path, "&7Not set"));
    }
}
