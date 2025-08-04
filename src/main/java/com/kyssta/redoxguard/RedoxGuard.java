package com.kyssta.redoxguard;

import com.kyssta.redoxguard.commands.RedoxGuardCommand;
import com.kyssta.redoxguard.config.ConfigManager;
import com.kyssta.redoxguard.listeners.PlayerConnectionListener;
import com.kyssta.redoxguard.listeners.PlayerMovementListener;
import com.kyssta.redoxguard.managers.CheckManager;
import com.kyssta.redoxguard.managers.PlayerDataManager;
import com.kyssta.redoxguard.utils.LogUtil;
import com.kyssta.redoxguard.utils.VersionCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RedoxGuard extends JavaPlugin {
    
    private static RedoxGuard instance;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    
    @Override
    public void onEnable() {
        // Set instance
        instance = this;
        
        // Initialize version compatibility
        VersionCompatibility.init();
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.checkManager = new CheckManager(this);
        
        // Register commands
        getCommand("redoxguard").setExecutor(new RedoxGuardCommand(this));
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMovementListener(this), this);
        
        // Log startup
        LogUtil.info("RedoxGuard v" + getDescription().getVersion() + " has been enabled!");
        LogUtil.info("Developed by Kyssta");
    }
    
    @Override
    public void onDisable() {
        // Save data and clean up
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }
        
        // Log shutdown
        LogUtil.info("RedoxGuard has been disabled!");
    }
    
    /**
     * Get the plugin instance
     * @return The plugin instance
     */
    public static RedoxGuard getInstance() {
        return instance;
    }
    
    /**
     * Get the config manager
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Get the player data manager
     * @return The player data manager
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    /**
     * Get the check manager
     * @return The check manager
     */
    public CheckManager getCheckManager() {
        return checkManager;
    }
}