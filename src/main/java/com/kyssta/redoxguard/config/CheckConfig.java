package com.kyssta.redoxguard.config;

import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Configuration wrapper for individual anti-cheat check categories.
 * <p>
 * This class provides a specialized interface for managing check-specific configurations
 * within RedoxGuard. Each CheckConfig instance represents a category of checks
 * (movement, combat, or player) and provides:
 * <ul>
 *   <li>Check enable/disable state management</li>
 *   <li>Violation threshold and punishment configuration</li>
 *   <li>Type-safe configuration value retrieval</li>
 *   <li>Automatic file persistence and error handling</li>
 * </ul>
 * <p>
 * CheckConfig instances are typically managed by the ConfigManager and provide
 * a clean abstraction over Bukkit's FileConfiguration for check-specific settings.
 * 
 * @author RedoxGuard Team
 * @version 1.0
 * @since 1.0.0
 */
public class CheckConfig {

    /** The name identifier for this check configuration category */
    private final String name;
    
    /** The underlying Bukkit configuration instance */
    private final FileConfiguration config;
    
    /** The file where this configuration is persisted */
    private final File file;
    
    /**
     * Creates a new CheckConfig wrapper for the specified configuration.
     * <p>
     * This constructor initializes a new configuration wrapper that provides
     * type-safe access to check-specific settings and automatic persistence.
     * 
     * @param name the category name for this configuration (e.g., "movement", "combat", "player")
     * @param config the underlying FileConfiguration instance
     * @param file the file where this configuration should be persisted
     */
    public CheckConfig(String name, FileConfiguration config, File file) {
        this.name = name;
        this.config = config;
        this.file = file;
    }
    
    /**
     * Checks if a specific anti-cheat check is enabled in this configuration.
     * <p>
     * This method looks for the "enabled" property under the check's configuration
     * section. If the property is not found, it defaults to {@code true} to ensure
     * checks are enabled by default.
     * 
     * @param check the name of the check to query (e.g., "Speed", "Reach", "KillAura")
     * @return {@code true} if the check is enabled, {@code false} otherwise
     */
    public boolean isCheckEnabled(String check) {
        return config.getBoolean(check + ".enabled", true);
    }
    
    /**
     * Retrieves the maximum violation threshold for a check before punishment is triggered.
     * <p>
     * This method looks for the "max-violations" property under the check's configuration
     * section. If not found, it defaults to 5 violations as a reasonable threshold.
     * 
     * @param check the name of the check to query
     * @return the maximum number of violations allowed before punishment (default: 5)
     */
    public int getMaxViolations(String check) {
        return config.getInt(check + ".max-violations", 5);
    }
    
    /**
     * Retrieves the punishment command to execute when a check's violation threshold is exceeded.
     * <p>
     * The punishment command supports placeholder substitution:
     * <ul>
     *   <li>%player% - replaced with the violating player's name</li>
     *   <li>%check% - replaced with the check name</li>
     * </ul>
     * <p>
     * If no punishment is configured, defaults to a kick command with a generic message.
     * 
     * @param check the name of the check to query
     * @return the punishment command string with placeholders (default: kick with unfair advantage message)
     */
    public String getPunishmentCommand(String check) {
        return config.getString(check + ".punishment", "kick %player% [RedoxGuard] Unfair advantage");
    }
    
    /**
     * Retrieves a generic configuration value from the specified path.
     * <p>
     * This method provides access to any configuration value using dot notation
     * for nested properties (e.g., "Speed.threshold" or "Reach.max-distance").
     * 
     * @param path the configuration path using dot notation
     * @param defaultValue the value to return if the path doesn't exist
     * @return the configuration value at the specified path, or the default value
     */
    public Object getValue(String path, Object defaultValue) {
        return config.get(path, defaultValue);
    }
    
    /**
     * Retrieves a double precision floating-point value from the configuration.
     * <p>
     * This method is commonly used for threshold values, multipliers, and other
     * numeric settings that require decimal precision in check configurations.
     * 
     * @param path the configuration path using dot notation
     * @param defaultValue the default double value if the path doesn't exist
     * @return the double value at the specified path, or the default value
     */
    public double getDoubleValue(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Retrieves a double precision floating-point value from the configuration.
     * <p>
     * This method is an alias for {@link #getDoubleValue(String, double)} and provides
     * the same functionality with a shorter method name for convenience.
     * 
     * @param path the configuration path using dot notation
     * @param defaultValue the default double value if the path doesn't exist
     * @return the double value at the specified path, or the default value
     */
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Retrieves a long integer value from the configuration.
     * <p>
     * This method is useful for timestamp values, large numeric thresholds,
     * and other settings that require 64-bit integer precision.
     * 
     * @param path the configuration path using dot notation
     * @param defaultValue the default long value if the path doesn't exist
     * @return the long value at the specified path, or the default value
     */
    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    
    /**
     * Retrieves an integer value from the configuration.
     * <p>
     * This method is commonly used for violation counts, tick thresholds,
     * and other whole number settings in check configurations.
     * 
     * @param path the configuration path using dot notation
     * @param defaultValue the default integer value if the path doesn't exist
     * @return the integer value at the specified path, or the default value
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    /**
     * Persists the current configuration state to the associated file.
     * <p>
     * This method writes all configuration changes to disk, ensuring that
     * modifications are preserved across server restarts. If an I/O error
     * occurs during saving, it is logged as a severe error.
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            LogUtil.severe("Failed to save check config: " + name);
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the category name identifier for this configuration.
     * <p>
     * The name typically corresponds to a check category such as "movement",
     * "combat", or "player", which groups related anti-cheat checks together.
     * 
     * @return the configuration category name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the underlying Bukkit FileConfiguration instance.
     * <p>
     * This method provides direct access to the raw configuration for advanced
     * operations that may not be covered by the wrapper methods.
     * 
     * @return the FileConfiguration instance backing this CheckConfig
     */
    public FileConfiguration getConfig() {
        return config;
    }
}