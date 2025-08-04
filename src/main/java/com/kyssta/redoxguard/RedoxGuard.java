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

public class RedoxGuard extends JavaPlugin {

    private static RedoxGuard instance;
    
    // Managers
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    
    // Async processing
    private ScheduledExecutorService asyncExecutor;
    
    // Webhook
    private WebhookUtil webhookUtil;
    
    // Plugin state
    private boolean enabled = false;

    @Override
    public void onEnable() {
        instance = this;
        
        LogUtil.info("Starting RedoxGuard v2.0 - Advanced Anti-Cheat System");
        
        // Initialize async executor
        asyncExecutor = Executors.newScheduledThreadPool(4);
        
        // Initialize version compatibility
        VersionCompatibility.init();
        
        // Initialize managers
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

    @Override
    public void onDisable() {
        LogUtil.info("Disabling RedoxGuard v2.0...");
        
        // Shutdown async executor
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
        
        // Save player data
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }
        
        enabled = false;
        LogUtil.info("RedoxGuard v2.0 has been disabled.");
    }

    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        // Config manager
        configManager = new ConfigManager(this);
        
        // Player data manager
        playerDataManager = new PlayerDataManager(this);
        
        // Check manager
        checkManager = new CheckManager(this);
        
        // Webhook util
        webhookUtil = new WebhookUtil();
    }

    /**
     * Register all listeners
     */
    private void registerListeners() {
        // Player connection listener
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        
        // Player movement listener (async)
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(this), this);
    }

    /**
     * Register commands
     */
    private void registerCommands() {
        getCommand("redoxguard").setExecutor(new com.kyssta.redoxguard.commands.RedoxGuardCommand(this));
    }

    /**
     * Start async tasks
     */
    private void startAsyncTasks() {
        // Player data cleanup task (every 5 minutes)
        asyncExecutor.scheduleAtFixedRate(() -> {
            if (enabled && playerDataManager != null) {
                // Cleanup inactive players (placeholder for now)
                LogUtil.debug("Running player data cleanup...");
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        // Performance monitoring task (every 30 seconds)
        asyncExecutor.scheduleAtFixedRate(() -> {
            if (enabled) {
                monitorPerformance();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Monitor plugin performance
     */
    private void monitorPerformance() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        LogUtil.debug("Performance: " + playerCount + " players, " + 
                (memoryUsage / 1024 / 1024) + "MB memory usage");
    }

    // Getters
    public static RedoxGuard getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public WebhookUtil getWebhookUtil() {
        return webhookUtil;
    }

    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public boolean isPluginEnabled() {
        return enabled;
    }
}