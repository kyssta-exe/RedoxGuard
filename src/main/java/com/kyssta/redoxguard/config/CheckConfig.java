package com.kyssta.redoxguard.config;

import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class CheckConfig {

    private final String name;
    private final FileConfiguration config;
    private final File file;
    
    public CheckConfig(String name, FileConfiguration config, File file) {
        this.name = name;
        this.config = config;
        this.file = file;
    }
    
    /**
     * Check if a specific check is enabled
     * @param check The name of the check
     * @return True if the check is enabled
     */
    public boolean isCheckEnabled(String check) {
        return config.getBoolean(check + ".enabled", true);
    }
    
    /**
     * Get the maximum violations for a check before punishment
     * @param check The name of the check
     * @return The maximum violations
     */
    public int getMaxViolations(String check) {
        return config.getInt(check + ".max-violations", 5);
    }
    
    /**
     * Get the punishment command for a check
     * @param check The name of the check
     * @return The punishment command
     */
    public String getPunishmentCommand(String check) {
        return config.getString(check + ".punishment", "kick %player% [RedoxGuard] Unfair advantage");
    }
    
    /**
     * Get a specific value from the check configuration
     * @param path The path to the value
     * @param defaultValue The default value if the path doesn't exist
     * @return The value at the path
     */
    public Object getValue(String path, Object defaultValue) {
        return config.get(path, defaultValue);
    }
    
    /**
     * Get a double value from the check configuration
     * @param path The path to the value
     * @param defaultValue The default value if the path doesn't exist
     * @return The double value at the path
     */
    public double getDoubleValue(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Save the check configuration
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
     * Get the name of this check configuration
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the configuration
     * @return The configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }
}