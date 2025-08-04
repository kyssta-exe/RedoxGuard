package com.kyssta.redoxguard.checks;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.entity.Player;

public abstract class Check {

    protected final RedoxGuard plugin;
    private final String name;
    private final String type;
    private boolean enabled;
    
    public Check(RedoxGuard plugin, String name, String type) {
        this.plugin = plugin;
        this.name = name;
        this.type = type;
        this.enabled = true;
        
        // Check if this check is enabled in the config
        if (plugin.getConfigManager().getCheckConfig(type) != null) {
            this.enabled = plugin.getConfigManager().getCheckConfig(type).isCheckEnabled(name.toLowerCase());
        }
    }
    
    /**
     * Flag a player for violating this check
     * @param player The player
     * @param details Additional details about the violation
     */
    protected void flag(Player player, String details) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        plugin.getCheckManager().handleViolation(player, this, details);
    }
    
    /**
     * Get the player data for a player
     * @param player The player
     * @return The player data
     */
    protected PlayerData getPlayerData(Player player) {
        return plugin.getPlayerDataManager().getOrCreatePlayerData(player);
    }
    
    /**
     * Debug a message for this check
     * @param message The message to debug
     */
    protected void debug(String message) {
        LogUtil.debug("[" + getName() + "] " + message);
    }
    
    /**
     * Get the name of this check
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the type of this check
     * @return The type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Check if this check is enabled
     * @return True if the check is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set whether this check is enabled
     * @param enabled True to enable the check, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}