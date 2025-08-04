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

public class ConfigManager {

    private final RedoxGuard plugin;
    private FileConfiguration config;
    private File configFile;
    
    private final Map<String, CheckConfig> checkConfigs = new HashMap<>();
    
    public ConfigManager(RedoxGuard plugin) {
        this.plugin = plugin;
        loadMainConfig();
        loadCheckConfigs();
    }
    
    /**
     * Load the main configuration file
     */
    private void loadMainConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        
        // Load config
        plugin.reloadConfig();
        config = plugin.getConfig();
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        // Add default values if they don't exist
        addDefaultValues();
    }
    
    /**
     * Add default values to the config if they don't exist
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
     * Load all check configurations
     */
    private void loadCheckConfigs() {
        // Create checks directory if it doesn't exist
        File checksDir = new File(plugin.getDataFolder(), "checks");
        if (!checksDir.exists()) {
            checksDir.mkdirs();
        }
        
        // Create default check configs
        createDefaultCheckConfig("movement", checksDir);
        createDefaultCheckConfig("combat", checksDir);
        createDefaultCheckConfig("player", checksDir);
        
        // Load all check configs
        for (File file : checksDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String name = file.getName().replace(".yml", "");
                checkConfigs.put(name, new CheckConfig(name, YamlConfiguration.loadConfiguration(file), file));
                LogUtil.debug("Loaded check config: " + name);
            }
        }
    }
    
    /**
     * Create a default check configuration file
     * @param name The name of the check
     * @param directory The directory to create the file in
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
     * Save the main configuration file
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
     * Get a check configuration
     * @param name The name of the check
     * @return The check configuration
     */
    public CheckConfig getCheckConfig(String name) {
        return checkConfigs.get(name);
    }
    
    /**
     * Get the main configuration
     * @return The main configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Check if debug mode is enabled
     * @return True if debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug");
    }
    
    /**
     * Check if staff notifications are enabled
     * @return True if staff notifications are enabled
     */
    public boolean isNotifyStaffEnabled() {
        return config.getBoolean("notify-staff");
    }
    
    /**
     * Check if violation logging is enabled
     * @return True if violation logging is enabled
     */
    public boolean isLogViolationsEnabled() {
        return config.getBoolean("log-violations");
    }
    
    /**
     * Check if Discord webhook is enabled
     * @return True if Discord webhook is enabled
     */
    public boolean isWebhookEnabled() {
        return config.getBoolean("discord-webhook.enabled");
    }
    
    /**
     * Get the Discord webhook URL
     * @return The Discord webhook URL
     */
    public String getWebhookUrl() {
        return config.getString("discord-webhook.url", "");
    }
}