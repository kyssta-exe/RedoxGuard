package com.kyssta.redoxguard.commands;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedoxGuardCommand implements CommandExecutor, TabCompleter {

    private final RedoxGuard plugin;
    
    public RedoxGuardCommand(RedoxGuard plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("redoxguard.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // No arguments, show help
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        // Handle subcommands
        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(sender);
                break;
                
            case "reload":
                reloadConfig(sender);
                break;
                
            case "info":
                showInfo(sender);
                break;
                
            case "checks":
                listChecks(sender);
                break;
                
            case "toggle":
                toggleCheck(sender, args);
                break;
                
            case "violations":
                showViolations(sender, args);
                break;
                
            case "debug":
                toggleDebug(sender);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Type " + 
                        ChatColor.GRAY + "/redoxguard help" + ChatColor.RED + " for help.");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Check permission
        if (!sender.hasPermission("redoxguard.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // First argument - subcommands
            String[] subcommands = {"help", "reload", "info", "checks", "toggle", "violations", "debug"};
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            // Second argument - depends on first argument
            switch (args[0].toLowerCase()) {
                case "toggle":
                    // Complete with check names
                    for (Check check : plugin.getCheckManager().getChecks()) {
                        if (check.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(check.getName());
                        }
                    }
                    break;
                    
                case "violations":
                    // Complete with player names
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(player.getName());
                        }
                    }
                    break;
            }
        }
        
        return completions;
    }
    
    /**
     * Show help message
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "===== " + ChatColor.RED + "RedoxGuard Help" + 
                ChatColor.DARK_RED + " =====");
        sender.sendMessage(ChatColor.RED + "/redoxguard help" + ChatColor.GRAY + " - Show this help message");
        sender.sendMessage(ChatColor.RED + "/redoxguard reload" + ChatColor.GRAY + " - Reload the configuration");
        sender.sendMessage(ChatColor.RED + "/redoxguard info" + ChatColor.GRAY + " - Show plugin information");
        sender.sendMessage(ChatColor.RED + "/redoxguard checks" + ChatColor.GRAY + " - List all checks");
        sender.sendMessage(ChatColor.RED + "/redoxguard toggle <check>" + ChatColor.GRAY + 
                " - Toggle a check on/off");
        sender.sendMessage(ChatColor.RED + "/redoxguard violations [player]" + ChatColor.GRAY + 
                " - Show violations for a player");
        sender.sendMessage(ChatColor.RED + "/redoxguard debug" + ChatColor.GRAY + " - Toggle debug mode");
    }
    
    /**
     * Reload the configuration
     * @param sender The command sender
     */
    private void reloadConfig(CommandSender sender) {
        // Create a new config manager to reload all configs
        plugin.getConfigManager().saveConfig();
        plugin.reloadConfig();
        
        sender.sendMessage(ChatColor.GREEN + "RedoxGuard configuration reloaded!");
        LogUtil.info("Configuration reloaded by " + sender.getName());
    }
    
    /**
     * Show plugin information
     * @param sender The command sender
     */
    private void showInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "===== " + ChatColor.RED + "RedoxGuard Info" + 
                ChatColor.DARK_RED + " =====");
        sender.sendMessage(ChatColor.RED + "Version: " + ChatColor.GRAY + 
                plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.RED + "Author: " + ChatColor.GRAY + "Kyssta");
        sender.sendMessage(ChatColor.RED + "Players tracked: " + ChatColor.GRAY + 
                plugin.getPlayerDataManager().getPlayerCount());
        sender.sendMessage(ChatColor.RED + "Checks loaded: " + ChatColor.GRAY + 
                plugin.getCheckManager().getChecks().size());
        sender.sendMessage(ChatColor.RED + "Debug mode: " + ChatColor.GRAY + 
                (plugin.getConfigManager().isDebugEnabled() ? "Enabled" : "Disabled"));
    }
    
    /**
     * List all checks
     * @param sender The command sender
     */
    private void listChecks(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "===== " + ChatColor.RED + "RedoxGuard Checks" + 
                ChatColor.DARK_RED + " =====");
        
        // Group checks by type
        List<String> types = plugin.getCheckManager().getChecks().stream()
                .map(Check::getType)
                .distinct()
                .collect(Collectors.toList());
        
        for (String type : types) {
            sender.sendMessage(ChatColor.RED + type.substring(0, 1).toUpperCase() + 
                    type.substring(1) + " Checks:");
            
            for (Check check : plugin.getCheckManager().getChecksOfType(type)) {
                sender.sendMessage(ChatColor.GRAY + "- " + check.getName() + ": " + 
                        (check.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            }
        }
    }
    
    /**
     * Toggle a check on or off
     * @param sender The command sender
     * @param args The command arguments
     */
    private void toggleCheck(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /redoxguard toggle <check>");
            return;
        }
        
        String checkName = args[1];
        Check check = plugin.getCheckManager().getCheckByName(checkName);
        
        if (check == null) {
            sender.sendMessage(ChatColor.RED + "Check not found: " + checkName);
            return;
        }
        
        // Toggle the check
        check.setEnabled(!check.isEnabled());
        
        sender.sendMessage(ChatColor.GREEN + "Check " + check.getName() + " is now " + 
                (check.isEnabled() ? "enabled" : "disabled") + "!");
        LogUtil.info("Check " + check.getName() + " was " + 
                (check.isEnabled() ? "enabled" : "disabled") + " by " + sender.getName());
    }
    
    /**
     * Show violations for a player
     * @param sender The command sender
     * @param args The command arguments
     */
    private void showViolations(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Show violations for all online players
            sender.sendMessage(ChatColor.DARK_RED + "===== " + ChatColor.RED + "RedoxGuard Violations" + 
                    ChatColor.DARK_RED + " =====");
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                if (data == null) continue;
                
                // Count total violations
                int totalViolations = 0;
                for (Check check : plugin.getCheckManager().getChecks()) {
                    totalViolations += data.getViolationLevel(check.getName());
                }
                
                sender.sendMessage(ChatColor.RED + player.getName() + ": " + ChatColor.GRAY + 
                        totalViolations + " violations");
            }
        } else {
            // Show violations for a specific player
            String playerName = args[1];
            Player player = Bukkit.getPlayer(playerName);
            
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
                return;
            }
            
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
            if (data == null) {
                sender.sendMessage(ChatColor.RED + "No data found for player: " + playerName);
                return;
            }
            
            sender.sendMessage(ChatColor.DARK_RED + "===== " + ChatColor.RED + "Violations for " + 
                    player.getName() + ChatColor.DARK_RED + " =====");
            
            for (Check check : plugin.getCheckManager().getChecks()) {
                int vl = data.getViolationLevel(check.getName());
                sender.sendMessage(ChatColor.RED + check.getName() + ": " + ChatColor.GRAY + vl);
            }
        }
    }
    
    /**
     * Toggle debug mode
     * @param sender The command sender
     */
    private void toggleDebug(CommandSender sender) {
        boolean debug = !plugin.getConfigManager().isDebugEnabled();
        plugin.getConfigManager().getConfig().set("debug", debug);
        plugin.getConfigManager().saveConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Debug mode is now " + 
                (debug ? "enabled" : "disabled") + "!");
        LogUtil.info("Debug mode was " + (debug ? "enabled" : "disabled") + " by " + sender.getName());
    }
}