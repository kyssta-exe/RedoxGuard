package com.kyssta.redoxguard.utils;

import com.kyssta.redoxguard.RedoxGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Level;
import com.kyssta.redoxguard.utils.WebhookUtil;

public class LogUtil {

    private static final String PREFIX = ChatColor.DARK_RED + "[RedoxGuard] " + ChatColor.RESET;
    
    /**
     * Log an info message
     * @param message The message to log
     */
    public static void info(String message) {
        Bukkit.getLogger().info(PREFIX + message);
    }
    
    /**
     * Log a warning message
     * @param message The message to log
     */
    public static void warning(String message) {
        Bukkit.getLogger().warning(PREFIX + message);
    }
    
    /**
     * Log a severe message
     * @param message The message to log
     */
    public static void severe(String message) {
        Bukkit.getLogger().severe(PREFIX + message);
    }
    
    /**
     * Log a debug message (only if debug mode is enabled)
     * @param message The message to log
     */
    public static void debug(String message) {
        if (RedoxGuard.getInstance() != null && RedoxGuard.getInstance().getConfigManager() != null && 
                RedoxGuard.getInstance().getConfigManager().isDebugEnabled()) {
            Bukkit.getLogger().log(Level.INFO, PREFIX + ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET + message);
        }
    }
    
    /**
     * Send a message to all staff members with the redoxguard.notify permission
     * @param message The message to send
     */
    public static void notifyStaff(String message) {
        if (RedoxGuard.getInstance() != null && RedoxGuard.getInstance().getConfigManager() != null && 
                RedoxGuard.getInstance().getConfigManager().isNotifyStaffEnabled()) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("redoxguard.notify"))
                    .forEach(player -> player.sendMessage(PREFIX + message));
        }
    }
    
    /**
     * Log a violation to the console and notify staff
     * @param playerName The name of the player
     * @param checkName The name of the check
     * @param details Additional details about the violation
     * @param vl The violation level
     */
    public static void logViolation(String playerName, String checkName, String details, int vl) {
        if (RedoxGuard.getInstance() != null && RedoxGuard.getInstance().getConfigManager() != null && 
                RedoxGuard.getInstance().getConfigManager().isLogViolationsEnabled()) {
            String message = ChatColor.RED + playerName + ChatColor.GRAY + " failed " + 
                    ChatColor.RED + checkName + ChatColor.GRAY + " " + details + 
                    ChatColor.DARK_GRAY + " (VL: " + vl + ")";
            
            info(message);
            notifyStaff(message);
            
            // Send webhook alert
            WebhookUtil.sendWebhookAlert(playerName, checkName, details, vl);
        }
    }
}