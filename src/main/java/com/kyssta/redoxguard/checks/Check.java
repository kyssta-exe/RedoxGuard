package com.kyssta.redoxguard.checks;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.entity.Player;

/**
 * Check - Abstract base class for all anti-cheat detection checks
 * 
 * <p>This abstract class provides the foundation for all anti-cheat checks in RedoxGuard.
 * It defines the common structure and functionality that all checks must implement:
 * <ul>
 *   <li>Check identification (name and type categorization)</li>
 *   <li>Configuration-based enable/disable functionality</li>
 *   <li>Violation flagging and reporting system</li>
 *   <li>Player data access and debugging utilities</li>
 * </ul>
 * 
 * <p>Check categories include:
 * <ul>
 *   <li><b>movement</b> - Speed, Fly, and other movement-based checks</li>
 *   <li><b>combat</b> - Reach, KillAura, AutoCrystal, and combat-related checks</li>
 *   <li><b>player</b> - Inventory, FastBreak, FastPlace, and player behavior checks</li>
 * </ul>
 * 
 * <p>All checks automatically respect configuration settings and permission-based bypasses.
 * Checks are enabled by default but can be disabled through configuration files.</p>
 * 
 * @author Kyssta
 * @since 1.0.0
 */
public abstract class Check {

    /** Reference to the main RedoxGuard plugin instance */
    protected final RedoxGuard plugin;
    
    /** The unique name identifier for this check */
    private final String name;
    
    /** The category type this check belongs to (movement, combat, player) */
    private final String type;
    
    /** Whether this check is currently enabled and active */
    private boolean enabled;
    
    /**
     * Constructs a new Check with the specified parameters and loads configuration settings.
     * 
     * <p>The check will automatically determine its enabled state from the configuration
     * file corresponding to its type. If no configuration exists, the check defaults to enabled.</p>
     * 
     * @param plugin the RedoxGuard plugin instance
     * @param name the unique name identifier for this check
     * @param type the category type (movement, combat, player)
     */
    public Check(RedoxGuard plugin, String name, String type) {
        this.plugin = plugin;
        this.name = name;
        this.type = type;
        this.enabled = true;
        
        // Load enabled state from configuration file
        if (plugin.getConfigManager().getCheckConfig(type) != null) {
            this.enabled = plugin.getConfigManager().getCheckConfig(type).isCheckEnabled(name.toLowerCase());
        }
    }
    
    /**
     * Flags a player for violating this check and initiates violation handling.
     * 
     * <p>This method performs the following actions:
     * <ul>
     *   <li>Verifies the check is enabled and player doesn't have bypass permissions</li>
     *   <li>Delegates to CheckManager for violation processing and logging</li>
     *   <li>Triggers configured punishments and staff notifications</li>
     * </ul>
     * 
     * <p>Players with the "redoxguard.bypass" permission will not be flagged.</p>
     * 
     * @param player the player who violated this check
     * @param details additional context and details about the specific violation
     */
    protected void flag(Player player, String details) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        plugin.getCheckManager().handleViolation(player, this, details);
    }
    
    /**
     * Retrieves or creates PlayerData for the specified player.
     * 
     * <p>PlayerData contains essential information for check processing:
     * <ul>
     *   <li>Movement history and ground state tracking</li>
     *   <li>Network latency (ping) for lag compensation</li>
     *   <li>Violation counts and timing information</li>
     * </ul>
     * 
     * @param player the player to get data for
     * @return the PlayerData instance for the specified player
     */
    protected PlayerData getPlayerData(Player player) {
        return plugin.getPlayerDataManager().getOrCreatePlayerData(player);
    }
    
    /**
     * Outputs a debug message with this check's name prefix.
     * 
     * <p>Debug messages are only displayed when debug mode is enabled in the
     * main configuration. This helps with troubleshooting and development.</p>
     * 
     * @param message the debug message to output
     */
    protected void debug(String message) {
        LogUtil.debug("[" + getName() + "] " + message);
    }
    
    /**
     * Returns the unique name identifier of this check.
     * 
     * @return the check's name (e.g., "Speed", "Reach", "KillAura")
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the category type this check belongs to.
     * 
     * @return the check type ("movement", "combat", or "player")
     */
    public String getType() {
        return type;
    }
    
    /**
     * Checks whether this check is currently enabled and active.
     * 
     * @return {@code true} if the check is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets the enabled state of this check.
     * 
     * <p>This allows runtime toggling of checks through commands or administrative interfaces.
     * Disabled checks will not process violations or flag players.</p>
     * 
     * @param enabled {@code true} to enable the check, {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}