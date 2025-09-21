package com.kyssta.redoxguard.config;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.utils.LogUtil;
import com.kyssta.redoxguard.utils.VersionCompatibility;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigManager - Centralized configuration management system for RedoxGuard
 * 
 * <p>The ConfigManager handles all configuration-related operations for the RedoxGuard
 * anti-cheat system. It manages both the main plugin configuration and individual
 * check configurations, providing a unified interface for accessing settings.
 * 
 * <p>Key responsibilities include:
 * <ul>
 *   <li>Loading and managing the main config.yml file</li>
 *   <li>Creating and managing individual check configuration files</li>
 *   <li>Providing version-compatible default configurations</li>
 *   <li>Handling configuration validation and default value injection</li>
 *   <li>Offering convenient accessor methods for common settings</li>
 * </ul>
 * 
 * <p>The manager automatically creates default configurations for all check types
 * and ensures backward compatibility across different Minecraft versions.</p>
 * 
 * @author Kyssta
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigManager {

    /** Reference to the main RedoxGuard plugin instance */
    private final RedoxGuard plugin;
    
    /** The main plugin configuration loaded from config.yml */
    private FileConfiguration config;
    
    /** File reference to the main configuration file */
    private File configFile;
    
    /** Map storing all check-specific configurations by check type name */
    private final Map<String, CheckConfig> checkConfigs = new HashMap<>();
    
    /**
     * Constructs a new ConfigManager and initializes all configurations.
     * 
     * <p>This constructor automatically loads the main configuration and
     * all check-specific configurations during instantiation.</p>
     * 
     * @param plugin the RedoxGuard plugin instance
     */
    public ConfigManager(RedoxGuard plugin) {
        this.plugin = plugin;
        loadMainConfig();
        loadCheckConfigs();
    }
    
    /**
     * Loads and initializes the main plugin configuration file.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Creates the default config.yml if it doesn't exist</li>
     *   <li>Reloads the configuration from disk</li>
     *   <li>Establishes file references for future operations</li>
     *   <li>Injects any missing default values</li>
     * </ol>
     */
    private void loadMainConfig() {
        // Create default config.yml from plugin resources if missing
        plugin.saveDefaultConfig();
        
        // Reload configuration from disk to ensure latest values
        plugin.reloadConfig();
        config = plugin.getConfig();
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        // Ensure all required configuration keys have default values
        addDefaultValues();
    }
    
    /**
     * Injects default values for any missing configuration keys.
     * 
     * <p>This method ensures backward compatibility by adding new configuration
     * options with sensible defaults when they don't exist in existing config files.
     * The configuration is automatically saved if any defaults are added.</p>
     */
    private void addDefaultValues() {
        boolean needsSave = false;
        
        // General settings
        if (!config.contains("debug")) {
            config.set("debug", false);
            needsSave = true;
        }
        
        if (!config.contains("notify-staff")) {
            config.set("notify-staff", true);
            needsSave = true;
        }
        
        if (!config.contains("log-violations")) {
            config.set("log-violations", true);
            needsSave = true;
        }
        
        // Discord webhook settings
        if (!config.contains("discord-webhook.enabled")) {
            config.set("discord-webhook.enabled", false);
            needsSave = true;
        }
        
        if (!config.contains("discord-webhook.url")) {
            config.set("discord-webhook.url", "");
            needsSave = true;
        }
        
        // Save if needed
        if (needsSave) {
            saveConfig();
        }
    }
    
    /**
     * Loads all check-specific configuration files from the checks directory.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Creates the checks directory if it doesn't exist</li>
     *   <li>Generates default configuration files for all check categories</li>
     *   <li>Loads all existing .yml files from the checks directory</li>
     *   <li>Stores loaded configurations in the checkConfigs map for quick access</li>
     * </ol>
     * 
     * <p>Check categories include:
     * <ul>
     *   <li><b>movement.yml</b> - Speed, Fly detection settings</li>
     *   <li><b>combat.yml</b> - Combat-related check configurations</li>
     *   <li><b>player.yml</b> - Player behavior and interaction settings</li>
     * </ul>
     */
    private void loadCheckConfigs() {
        // Ensure checks directory exists for configuration storage
        File checksDir = new File(plugin.getDataFolder(), "checks");
        if (!checksDir.exists()) {
            checksDir.mkdirs();
        }
        
        // Generate default configurations for all check categories
        createDefaultCheckConfig("movement", checksDir);
        createDefaultCheckConfig("combat", checksDir);
        createDefaultCheckConfig("player", checksDir);
        
        // Load and register all check configuration files
        for (File file : checksDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String name = file.getName().replace(".yml", "");
                checkConfigs.put(name, new CheckConfig(name, YamlConfiguration.loadConfiguration(file), file));
                LogUtil.debug("Loaded check config: " + name);
            }
        }
    }
    
    /**
     * Creates a default configuration file for a specific check category.
     * 
     * <p>This method generates comprehensive default configurations with:
     * <ul>
     *   <li>Enabled/disabled states for each check</li>
     *   <li>Violation thresholds and punishment commands</li>
     *   <li>Check-specific parameters (distances, timings, angles)</li>
     *   <li>Version-specific compatibility handling</li>
     * </ul>
     * 
     * <p>The method only creates files that don't already exist, preserving
     * existing user configurations.</p>
     * 
     * @param name the check category name (movement, combat, player)
     * @param directory the directory where the configuration file should be created
     */
    private void createDefaultCheckConfig(String name, File directory) {
        File file = new File(directory, name + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                // Add default values based on check type
                switch (name) {
                    case "movement":
                        config.set("speed.enabled", true);
                        config.set("speed.max-violations", 5);
                        config.set("speed.punishment", "kick %player% [RedoxGuard] Unfair advantage - Movement (Speed)");
                        
                        config.set("fly.enabled", true);
                        config.set("fly.max-violations", 3);
                        config.set("fly.punishment", "kick %player% [RedoxGuard] Unfair advantage - Movement (Fly)");
                        break;
                        
                    case "combat":
                        config.set("reach.enabled", true);
                        config.set("reach.max-distance", 3.1);
                        config.set("reach.max-violations", 5);
                        config.set("reach.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (Reach)");
                        
                        config.set("killaura.enabled", true);
                        config.set("killaura.max-violations", 3);
                        config.set("killaura.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (KillAura)");
                        
                        config.set("hitbox.enabled", true);
                        config.set("hitbox.max-expansion", 0.1);
                        config.set("hitbox.max-violations", 5);
                        config.set("hitbox.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (Hitbox)");
                        
                        config.set("autocrystal.enabled", true);
                        config.set("autocrystal.min-place-time", 100);
                        config.set("autocrystal.min-break-time", 100);
                        config.set("autocrystal.min-switch-time", 150);
                        config.set("autocrystal.max-angle", 60.0);
                        config.set("autocrystal.max-violations", 5);
                        config.set("autocrystal.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (AutoCrystal)");
                        
                        // Only add AutoAnchor config if RESPAWN_ANCHOR exists in this version (added in 1.16)
                        if (VersionCompatibility.hasRespawnAnchor()) {
                            config.set("autoanchor.enabled", true);
                            config.set("autoanchor.min-place-time", 100);
                            config.set("autoanchor.min-charge-time", 100);
                            config.set("autoanchor.min-detonate-time", 100);
                            config.set("autoanchor.min-switch-time", 150);
                            config.set("autoanchor.max-violations", 5);
                            config.set("autoanchor.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (AutoAnchor)");
                        }
                        
                        config.set("crystalaura.enabled", true);
                        config.set("crystalaura.min-place-time", 100);
                        config.set("crystalaura.min-break-time", 100);
                        config.set("crystalaura.max-place-angle", 60.0);
                        config.set("crystalaura.max-break-angle", 60.0);
                        config.set("crystalaura.max-violations", 5);
                        config.set("crystalaura.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (CrystalAura)");
                        
                        config.set("triggerbot.enabled", true);
                        config.set("triggerbot.min-reaction-time", 100);
                        config.set("triggerbot.max-angle", 5.0);
                        config.set("triggerbot.max-violations", 3);
                        config.set("triggerbot.punishment", "kick %player% [RedoxGuard] Unfair advantage - Combat (TriggerBot)");
                        break;
                        
                    case "player":
                        config.set("inventory.enabled", true);
                        config.set("inventory.max-violations", 5);
                        config.set("inventory.punishment", "kick %player% [RedoxGuard] Unfair advantage - Player (Inventory)");
                        
                        config.set("fastbreak.enabled", true);
                        config.set("fastbreak.min-break-time", 150);
                        config.set("fastbreak.max-violations", 5);
                        config.set("fastbreak.punishment", "kick %player% [RedoxGuard] Unfair advantage - Player (FastBreak)");
                        
                        config.set("fastplace.enabled", true);
                        config.set("fastplace.min-place-time", 50);
                        config.set("fastplace.min-crystal-place-time", 100);
                        config.set("fastplace.max-violations", 5);
                        config.set("fastplace.punishment", "kick %player% [RedoxGuard] Unfair advantage - Player (FastPlace)");
                        
                        config.set("autototem.enabled", true);
                        config.set("autototem.min-reaction-time", 150);
                        config.set("autototem.min-pop-time", 100);
                        config.set("autototem.min-action-time", 100);
                        config.set("autototem.max-violations", 5);
                        config.set("autototem.punishment", "kick %player% [RedoxGuard] Unfair advantage - Player (AutoTotem)");
                        
                        config.set("simulation.enabled", true);
                        config.set("simulation.min-response-time", 100);
                        config.set("simulation.max-prediction-distance", 0.5);
                        config.set("simulation.max-violations", 5);
                        config.set("simulation.punishment", "kick %player% [RedoxGuard] Unfair advantage - Player (Simulation)");
                        break;
                }
                
                config.save(file);
            } catch (IOException e) {
                LogUtil.severe("Failed to create default check config: " + name);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Saves the main configuration file to disk.
     * 
     * <p>This method writes all current configuration values to the config.yml file.
     * If an I/O error occurs during the save operation, it will be logged as a severe error.</p>
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            LogUtil.severe("Failed to save config!");
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves a check-specific configuration by category name.
     * 
     * <p>Valid check category names include:
     * <ul>
     *   <li>"movement" - for Speed and Fly checks</li>
     *   <li>"combat" - for Reach, KillAura, AutoCrystal, etc.</li>
     *   <li>"player" - for Inventory, FastBreak, FastPlace, etc.</li>
     * </ul>
     * 
     * @param name the check category name
     * @return the CheckConfig instance for the specified category, or {@code null} if not found
     */
    public CheckConfig getCheckConfig(String name) {
        return checkConfigs.get(name);
    }
    
    /**
     * Retrieves the main plugin configuration.
     * 
     * @return the FileConfiguration instance containing all main plugin settings
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Checks if debug mode is currently enabled.
     * 
     * <p>When debug mode is enabled, RedoxGuard will output detailed logging
     * information to help with troubleshooting and development.</p>
     * 
     * @return {@code true} if debug mode is enabled, {@code false} otherwise
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug");
    }
    
    /**
     * Checks if staff notifications are enabled.
     * 
     * <p>When enabled, players with the 'redoxguard.notify' permission will
     * receive real-time violation alerts in chat.</p>
     * 
     * @return {@code true} if staff notifications are enabled, {@code false} otherwise
     */
    public boolean isNotifyStaffEnabled() {
        return config.getBoolean("notify-staff");
    }
    
    /**
     * Checks if violation logging is enabled.
     * 
     * <p>When enabled, all violations will be logged to the console and log files
     * for administrative review and analysis. This is essential for tracking
     * player behavior patterns and system performance.</p>
     * 
     * @return {@code true} if violation logging is enabled, {@code false} otherwise
     */
    public boolean isLogViolationsEnabled() {
        return config.getBoolean("log-violations");
    }
    
    /**
     * Checks if Discord webhook integration is enabled.
     * 
     * <p>When enabled, violation alerts will be sent to the configured Discord
     * channel via webhook, allowing for real-time monitoring outside of the game.</p>
     * 
     * @return {@code true} if Discord webhook is enabled, {@code false} otherwise
     */
    public boolean isWebhookEnabled() {
        return config.getBoolean("discord-webhook.enabled");
    }
    
    /**
     * Retrieves the configured Discord webhook URL.
     * 
     * <p>This URL is used to send violation notifications to a Discord channel.
     * The URL should be obtained from Discord's Server Settings → Integrations → Webhooks.</p>
     * 
     * @return the Discord webhook URL, or an empty string if not configured
     */
    public String getWebhookUrl() {
        return config.getString("discord-webhook.url", "");
    }
}