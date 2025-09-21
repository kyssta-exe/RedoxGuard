package com.kyssta.redoxguard.utils;

import com.kyssta.redoxguard.RedoxGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Level;
import com.kyssta.redoxguard.utils.WebhookUtil;

/**
 * Utility class for centralized logging and notification management in RedoxGuard.
 * <p>
 * This class provides a unified interface for:
 * <ul>
 *   <li>Console logging with consistent formatting and prefixes</li>
 *   <li>Debug message handling with conditional output</li>
 *   <li>Staff notification system for real-time alerts</li>
 *   <li>Violation logging with multi-channel distribution</li>
 *   <li>Integration with webhook systems for external notifications</li>
 * </ul>
 * <p>
 * All logging methods are static and thread-safe, making them suitable for use
 * across the entire plugin without instantiation.
 * 
 * @author RedoxGuard Team
 * @version 1.0
 * @since 1.0.0
 */
public class LogUtil {

    /**
     * The standardized prefix used for all RedoxGuard log messages.
     * <p>
     * Formatted with dark red color for the plugin name and reset color
     * to ensure consistent appearance across all log outputs.
     */
    private static final String PREFIX = ChatColor.DARK_RED + "[RedoxGuard] " + ChatColor.RESET;
    
    /**
     * Logs an informational message to the server console.
     * <p>
     * This method is used for general information, status updates,
     * and non-critical notifications that should be visible in the console.
     * 
     * @param message The message to log, will be prefixed with the RedoxGuard identifier
     */
    public static void info(String message) {
        Bukkit.getLogger().info(PREFIX + message);
    }
    
    /**
     * Logs a warning message to the server console.
     * <p>
     * This method should be used for potentially problematic situations
     * that don't prevent the plugin from functioning but may require attention.
     * 
     * @param message The warning message to log, will be prefixed with the RedoxGuard identifier
     */
    public static void warning(String message) {
        Bukkit.getLogger().warning(PREFIX + message);
    }
    
    /**
     * Logs a severe error message to the server console.
     * <p>
     * This method should be used for critical errors, exceptions,
     * and situations that may impact plugin functionality or server stability.
     * 
     * @param message The error message to log, will be prefixed with the RedoxGuard identifier
     */
    public static void severe(String message) {
        Bukkit.getLogger().severe(PREFIX + message);
    }
    
    /**
     * Logs a debug message to the server console if debug mode is enabled.
     * <p>
     * Debug messages are only output when:
     * <ul>
     *   <li>The RedoxGuard instance is available</li>
     *   <li>The ConfigManager is initialized</li>
     *   <li>Debug mode is enabled in the configuration</li>
     * </ul>
     * <p>
     * Debug messages are formatted with a yellow [DEBUG] prefix for easy identification.
     * 
     * @param message The debug message to log, will be prefixed with debug identifier
     */
    public static void debug(String message) {
        if (RedoxGuard.getInstance() != null && RedoxGuard.getInstance().getConfigManager() != null && 
                RedoxGuard.getInstance().getConfigManager().isDebugEnabled()) {
            Bukkit.getLogger().log(Level.INFO, PREFIX + ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET + message);
        }
    }
    
    /**
     * Sends a notification message to all online staff members with appropriate permissions.
     * <p>
     * This method distributes messages to players who have the 'redoxguard.notify' permission,
     * allowing for real-time alerts about anti-cheat events. The notification is only sent when:
     * <ul>
     *   <li>The RedoxGuard instance is available</li>
     *   <li>The ConfigManager is initialized</li>
     *   <li>Staff notifications are enabled in the configuration</li>
     * </ul>
     * <p>
     * Messages are formatted with the standard RedoxGuard prefix for consistency.
     * 
     * @param message The notification message to send to staff members
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
     * Logs a comprehensive violation report across multiple channels.
     * <p>
     * This method handles the complete violation reporting workflow:
     * <ul>
     *   <li>Formats the violation message with player, check, and details</li>
     *   <li>Logs the violation to the server console</li>
     *   <li>Notifies online staff members with appropriate permissions</li>
     *   <li>Triggers webhook alerts for external monitoring systems</li>
     * </ul>
     * <p>
     * The violation is only processed when:
     * <ul>
     *   <li>The RedoxGuard instance is available</li>
     *   <li>The ConfigManager is initialized</li>
     *   <li>Violation logging is enabled in the configuration</li>
     * </ul>
     * <p>
     * The message is color-coded for better readability: red for player name and check,
     * gray for descriptive text, and dark gray for the violation level.
     * 
     * @param playerName The name of the player who triggered the violation
     * @param checkName The name of the anti-cheat check that was violated
     * @param details Additional contextual information about the violation
     * @param vl The current violation level for this player and check type
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