package com.kyssta.redoxguard;

import com.kyssta.redoxguard.config.ConfigManager;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.managers.CheckManager;
import com.kyssta.redoxguard.managers.PlayerDataManager;
import com.kyssta.redoxguard.listeners.PlayerConnectionListener;
import com.kyssta.redoxguard.listeners.PlayerMovementListener;
import com.kyssta.redoxguard.utils.LogUtil;
import com.kyssta.redoxguard.utils.VersionCompatibility;
import com.kyssta.redoxguard.utils.WebhookUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RedoxGuard - Advanced Anti-Cheat Solution for Minecraft Servers
 * 
 * <p>RedoxGuard is a comprehensive anti-cheat plugin designed to protect Minecraft servers
 * from various forms of cheating and exploitation. It provides advanced detection algorithms
 * for movement, combat, and player behavior violations while maintaining optimal performance
 * and minimizing false positives through intelligent latency compensation.</p>
 * 
 * <p>Key Features:</p>
 * <ul>
 *   <li>Advanced movement detection (Speed, Fly)</li>
 *   <li>Comprehensive combat checks (Reach, KillAura, Hitbox, etc.)</li>
 *   <li>Player behavior monitoring (Inventory, FastBreak, FastPlace, etc.)</li>
 *   <li>Automation detection (AutoCrystal, CrystalAura, TriggerBot, etc.)</li>
 *   <li>Smart latency compensation</li>
 *   <li>Discord webhook integration</li>
 *   <li>Multi-version compatibility (MC 1.13-1.21.x)</li>
 * </ul>
 * 
 * @author Kyssta
 * @version 1.0.0
 * @since 1.0.0
 */
public class RedoxGuard extends JavaPlugin {

    /** Singleton instance of the RedoxGuard plugin */
    private static RedoxGuard instance;
    
    /** Configuration manager for handling plugin settings */
    private ConfigManager configManager;
    
    /** Player data manager for tracking player information and violations */
    private PlayerDataManager playerDataManager;
    
    /** Check manager for registering and managing all anti-cheat checks */
    private CheckManager checkManager;
    
    /** Scheduled executor service for asynchronous task processing */
    private ScheduledExecutorService asyncExecutor;
    
    /** Webhook utility for Discord integration and notifications */
    private WebhookUtil webhookUtil;
    
    /** Plugin enabled state flag */
    private boolean enabled = false;

    /**
     * Called when the plugin is enabled.
     * Initializes all managers, registers listeners and commands,
     * and starts asynchronous monitoring tasks.
     */
    @Override
    public void onEnable() {
        instance = this;
        
        LogUtil.info("Starting RedoxGuard v2.0 - Advanced Anti-Cheat System");
        
        // Initialize async executor with 4 threads for optimal performance
        asyncExecutor = Executors.newScheduledThreadPool(4);
        
        // Initialize version compatibility layer
        VersionCompatibility.init();
        
        // Initialize all core managers
        initializeManagers();
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        // Start async tasks
        startAsyncTasks();
        
        enabled = true;
        LogUtil.info("RedoxGuard v2.0 has been enabled successfully!");
    }

    /**
     * Called when the plugin is disabled.
     * Performs cleanup operations including shutting down the async executor,
     * saving player data, and updating the plugin state.
     */
    @Override
    public void onDisable() {
        LogUtil.info("Disabling RedoxGuard v2.0...");
        
        // Gracefully shutdown async executor
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
            }
        }
        
        // Save all player data before shutdown
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }
        
        enabled = false;
        LogUtil.info("RedoxGuard v2.0 has been disabled.");
    }

    /**
     * Initializes all core managers required for the plugin to function.
     * This includes configuration, player data, check management, and webhook utilities.
     */
    private void initializeManagers() {
        // Initialize configuration manager
        configManager = new ConfigManager(this);
        
        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this);
        
        // Initialize check manager with all anti-cheat checks
        checkManager = new CheckManager(this);
        
        // Initialize webhook utility for Discord integration
        webhookUtil = new WebhookUtil();
    }

    /**
     * Registers all event listeners required for the anti-cheat system.
     * This includes player connection and movement listeners.
     */
    private void registerListeners() {
        // Register player connection listener for join/quit events
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        
        // Register player movement listener for movement-based checks
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(this), this);
    }

    /**
     * Registers all plugin commands with their respective executors.
     */
    private void registerCommands() {
        getCommand("redoxguard").setExecutor(new com.kyssta.redoxguard.commands.RedoxGuardCommand(this));
    }

    /**
     * Starts all asynchronous background tasks for the plugin.
     * This includes player data cleanup and performance monitoring tasks.
     */
    private void startAsyncTasks() {
        // Player data cleanup task - runs every 5 minutes
        asyncExecutor.scheduleAtFixedRate(() -> {
            if (enabled && playerDataManager != null) {
                // Cleanup inactive players to prevent memory leaks
                LogUtil.debug("Running player data cleanup...");
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        // Performance monitoring task - runs every 30 seconds
        asyncExecutor.scheduleAtFixedRate(() -> {
            if (enabled) {
                monitorPerformance();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Monitors plugin performance by tracking player count and memory usage.
     * Logs debug information about current resource consumption.
     */
    private void monitorPerformance() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        LogUtil.debug("Performance: " + playerCount + " players, " + 
                (memoryUsage / 1024 / 1024) + "MB memory usage");
    }

    // ========== GETTER METHODS ==========
    
    /**
     * Gets the singleton instance of the RedoxGuard plugin.
     * 
     * @return the RedoxGuard plugin instance
     */
    public static RedoxGuard getInstance() {
        return instance;
    }

    /**
     * Gets the configuration manager for accessing plugin settings.
     * 
     * @return the ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the player data manager for accessing player information and violations.
     * 
     * @return the PlayerDataManager instance
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * Gets the check manager for accessing registered anti-cheat checks.
     * 
     * @return the CheckManager instance
     */
    public CheckManager getCheckManager() {
        return checkManager;
    }

    /**
     * Gets the webhook utility for Discord integration.
     * 
     * @return the WebhookUtil instance
     */
    public WebhookUtil getWebhookUtil() {
        return webhookUtil;
    }

    /**
     * Gets the scheduled executor service for asynchronous task processing.
     * 
     * @return the ScheduledExecutorService instance
     */
    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    /**
     * Checks if the plugin is currently enabled and operational.
     * 
     * @return true if the plugin is enabled, false otherwise
     */
    public boolean isPluginEnabled() {
        return enabled;
    }
}