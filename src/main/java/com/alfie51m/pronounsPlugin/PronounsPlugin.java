package com.alfie51m.pronounsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PronounsPlugin extends JavaPlugin implements TabExecutor {

    private Connection connection;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

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
                getLogger().warning("Failed to close database connection.");
            }
        }
    }

    private void connectToDatabase() throws SQLException {
        String url = "jdbc:mysql://" + config.getString("database.host") + ":" + config.getInt("database.port")
                + "/" + config.getString("database.name");
        String user = config.getString("database.user");
        String password = config.getString("database.password");
        connection = DriverManager.getConnection(url, user, password);
        getLogger().info("Connected to the database.");
    }

    private void setupDatabase() throws SQLException {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS pronouns (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "pronoun VARCHAR(100)" +  // store raw key
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("pronouns")) return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /pronouns <command>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "get":
                // /pronouns get <username>
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /pronouns get <username>");
                    return true;
                }
                if (!sender.hasPermission("pronouns.get")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                String storedKey = getPronouns(target.getUniqueId().toString()); // e.g. "he/him"
                if (storedKey == null) {
                    sender.sendMessage(ChatColor.GREEN + target.getName() + "'s pronouns: "
                            + ChatColor.AQUA + "Not set");
                } else {
                    // Convert raw key -> color from config
                    String colorized = getColoredPronoun(storedKey);
                    sender.sendMessage(ChatColor.GREEN + target.getName() + "'s pronouns: "
                            + ChatColor.RESET + colorized);
                }
                break;

            case "list":
                // /pronouns list
                Set<String> availablePronouns = config.getConfigurationSection("availablePronouns").getKeys(false);
                sender.sendMessage(ChatColor.GREEN + "Available pronouns:");
                for (String key : availablePronouns) {
                    String colorized = getColoredPronoun(key);
                    // show both raw key and color version
                    sender.sendMessage(ChatColor.AQUA + "- " + key + ChatColor.GRAY + ": "
                            + ChatColor.RESET + colorized);
                }
                break;

            default:
                // /pronouns <rawKey> -> attempt to set pronouns
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can set pronouns.");
                    return true;
                }

                // e.g., if user types /pronouns he/him
                String chosenPronoun = subCommand;  // "he/him"
                if (!config.contains("availablePronouns." + chosenPronoun)) {
                    sender.sendMessage(ChatColor.RED
                            + "Invalid pronoun. Use /pronouns list to see available options.");
                    return true;
                }

                Player player = (Player) sender;
                setPronouns(player.getUniqueId().toString(), chosenPronoun);

                // Show them the colorized version
                String colorized = getColoredPronoun(chosenPronoun);
                sender.sendMessage(ChatColor.GREEN + "Your pronouns have been set to: " + ChatColor.RESET + colorized);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("pronouns")) return null;

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("get");
            subCommands.add("list");
            subCommands.addAll(config.getConfigurationSection("availablePronouns").getKeys(false));
            return subCommands;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> playerNames.add(p.getName()));
            return playerNames;
        }
        return null;
    }

    /**
     * Returns the raw pronoun key from the DB (e.g. "he/him").
     * @param uuid Player's UUID as String
     */
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

    /**
     * Stores the raw pronoun key (e.g. "he/him") in the DB.
     */
    public void setPronouns(String uuid, String pronoun) {
        String query = "INSERT INTO pronouns (uuid, pronoun) VALUES (?, ?) ON DUPLICATE KEY UPDATE pronoun = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, pronoun);
            stmt.setString(3, pronoun);
            stmt.executeUpdate();
        } catch (SQLException e) {
            getLogger().warning("Failed to set pronouns: " + e.getMessage());
        }
    }


    public String getColoredPronoun(String rawKey) {
        String path = "availablePronouns." + rawKey;
        if (!config.isString(path)) {
            return ChatColor.RED + "Not set";
        }
        String raw = config.getString(path, "&cNot set");
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
