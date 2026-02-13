package com.alfie51m.pronounsMC;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PronounsCommand implements TabExecutor {

    private final PronounsMC plugin;

    public PronounsCommand(PronounsMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.usageMain", "&cUsage: /pronouns <command>")));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "reset":
                if (args.length >= 2) {
                    if (!sender.hasPermission("pronouns.admin")) {
                        sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.noPermission", "&cNo permission.")));
                        return true;
                    }

                    String targetName = args[1];
                    Player onlineTarget = Bukkit.getPlayerExact(targetName);
                    UUID targetUUID;

                    if (onlineTarget != null) {
                        targetUUID = onlineTarget.getUniqueId();
                    } else {
                        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
                        if (offlineTarget == null || !offlineTarget.hasPlayedBefore()) {
                            sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.playerNotFound", "&cPlayer not found!")));
                            return true;
                        }
                        targetUUID = offlineTarget.getUniqueId();
                    }

                    plugin.getDatabase().resetPronouns(targetUUID.toString());
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.pronounResetTarget", "&aReset pronouns for {player}." ).replace("{player}", targetName)));
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.onlyPlayers", "&cOnly players can reset pronouns.")));
                    return true;
                }

                plugin.getDatabase().resetPronouns(player.getUniqueId().toString());
                sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.pronounReset", "&aYour pronouns have been reset.")));
                return true;


            case "reload":
                if (!sender.hasPermission("pronouns.reload")) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.noPermission", "&cNo permission.")));
                    return true;
                }
                plugin.reloadPluginConfig();
                sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.pluginReloaded", "&aPronounsMC config reloaded.")));
                return true;

            case "get":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.usageGet", "&cUsage: /pronouns get <username>")));
                    return true;
                }
                if (!sender.hasPermission("pronouns.get")) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.noPermission", "&cNo permission.")));
                    return true;
                }

                String targetName = args[1];
                Player onlineTarget = Bukkit.getPlayerExact(targetName);
                UUID targetUUID;

                if (onlineTarget != null) {
                    targetUUID = onlineTarget.getUniqueId();
                } else {
                    OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
                    if (offlineTarget == null || !offlineTarget.hasPlayedBefore()) {
                        sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.playerNotFound", "&cPlayer not found!")));
                        return true;
                    }
                    targetUUID = offlineTarget.getUniqueId();
                }

                String storedKey = plugin.getDatabase().getPronouns(targetUUID.toString());
                if (storedKey == null) {
                    String msgTemplate = plugin.getLang().get("messages.playerPronounNone", "&a{player}'s pronouns: &bNot set");
                    msgTemplate = msgTemplate.replace("{player}", targetName);
                    sender.sendMessage(ColorUtil.color(msgTemplate));
                } else {
                    String colorized = plugin.getColoredPronoun(storedKey);
                    String msgTemplate = plugin.getLang().get("messages.playerPronounFormat", "&a{player}'s pronouns: &r{pronouns}");
                    msgTemplate = msgTemplate.replace("{player}", targetName);
                    msgTemplate = msgTemplate.replace("{pronouns}", colorized);
                    sender.sendMessage(ColorUtil.color(msgTemplate));
                }
                return true;

            case "list":
                if (!plugin.getPluginConfig().isConfigurationSection("availablePronouns")) {
                    sender.sendMessage(ColorUtil.color("&cNo pronouns configured."));
                    return true;
                }

                sender.sendMessage(ColorUtil.color(
                        plugin.getLang().get("messages.availablePronounsHeader", "&aAvailable pronouns:")
                ));

                for (String key : plugin.getPluginConfig().getConfigurationSection("availablePronouns").getKeys(false)) {
                    String colorized = plugin.getColoredPronoun(key);
                    sender.sendMessage(ColorUtil.color("&b- " + key + "&7: " + colorized));
                }

                if (plugin.getPluginConfig().getBoolean("userSuppliedPronouns", false)) {
                    sender.sendMessage(ColorUtil.color(
                            plugin.getLang().get("messages.availablePronounsFooter", "&9You can also set your own pronouns using /pronouns set <pronoun>")
                    ));
                }

                return true;

            case "set":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.onlyPlayers", "&cOnly players can set pronouns.")));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.usageSet", "&cUsage: /pronouns set <pronoun>")));
                    return true;
                }

                String chosenPronoun = args[1].toLowerCase();
                if (!(plugin.getPluginConfig().getBoolean("userSuppliedPronouns", false)
                        || plugin.getPluginConfig().contains("availablePronouns." + chosenPronoun))) {
                    sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.invalidPronoun", "&cInvalid pronoun. Use /pronouns list to see available options.")));
                    return true;
                }

                plugin.getDatabase().setPronouns(player.getUniqueId().toString(), chosenPronoun);
                String colorized = plugin.getColoredPronoun(chosenPronoun);
                String msgTemplate = plugin.getLang().get("messages.pronounSet", "&aYour pronouns have been set to: &r{pronouns}");
                msgTemplate = msgTemplate.replace("{pronouns}", colorized);
                sender.sendMessage(ColorUtil.color(msgTemplate));
                return true;
        }

        sender.sendMessage(ColorUtil.color(plugin.getLang().get("messages.usageMain", "&cUsage: /pronouns <command>")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("list");
            subCommands.add("reset");
            subCommands.add("set");
            if (sender.hasPermission("pronouns.get")) subCommands.add("get");
            if (sender.hasPermission("pronouns.reload")) subCommands.add("reload");
            return subCommands;
        }

        if (args.length == 2) {

            if (args[0].equalsIgnoreCase("get")) {
                List<String> completions = new ArrayList<>();
                String partial = args[1].toLowerCase();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(partial))
                        completions.add(onlinePlayer.getName());
                }
                return completions;
            }

            if (args[0].equalsIgnoreCase("set")) {
                List<String> completions = new ArrayList<>();
                String partial = args[1].toLowerCase();
                if (plugin.getPluginConfig().isConfigurationSection("availablePronouns")) {
                    for (String key : plugin.getPluginConfig().getConfigurationSection("availablePronouns").getKeys(false)) {
                        if (key.toLowerCase().startsWith(partial)) completions.add(key);
                    }
                }
                return completions;
            }
        }

        return null;
    }
}
