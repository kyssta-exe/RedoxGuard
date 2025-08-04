package com.kyssta.redoxguard.utils;

import org.bukkit.Material;

/**
 * Utility class to handle version compatibility issues
 * Ensures the plugin works across Minecraft versions 1.13-1.21.x
 */
public class VersionCompatibility {

    // Store whether certain materials exist in this Minecraft version
    private static boolean hasRespawnAnchor;
    private static boolean hasCornflower;
    private static boolean hasLilyOfTheValley;
    private static boolean hasWitherRose;
    
    /**
     * Initialize the compatibility checks
     * Should be called when the plugin is enabled
     */
    public static void init() {
        // Check for materials added in newer versions
        try {
            Material.valueOf("RESPAWN_ANCHOR");
            hasRespawnAnchor = true;
        } catch (IllegalArgumentException e) {
            hasRespawnAnchor = false;
            LogUtil.info("RESPAWN_ANCHOR material not found in this Minecraft version. AutoAnchor checks will be disabled.");
        }
        
        try {
            Material.valueOf("CORNFLOWER");
            hasCornflower = true;
        } catch (IllegalArgumentException e) {
            hasCornflower = false;
        }
        
        try {
            Material.valueOf("LILY_OF_THE_VALLEY");
            hasLilyOfTheValley = true;
        } catch (IllegalArgumentException e) {
            hasLilyOfTheValley = false;
        }
        
        try {
            Material.valueOf("WITHER_ROSE");
            hasWitherRose = true;
        } catch (IllegalArgumentException e) {
            hasWitherRose = false;
        }
    }
    
    /**
     * Check if the RESPAWN_ANCHOR material exists in this version
     * @return true if the material exists
     */
    public static boolean hasRespawnAnchor() {
        return hasRespawnAnchor;
    }
    
    /**
     * Check if the CORNFLOWER material exists in this version
     * @return true if the material exists
     */
    public static boolean hasCornflower() {
        return hasCornflower;
    }
    
    /**
     * Check if the LILY_OF_THE_VALLEY material exists in this version
     * @return true if the material exists
     */
    public static boolean hasLilyOfTheValley() {
        return hasLilyOfTheValley;
    }
    
    /**
     * Check if the WITHER_ROSE material exists in this version
     * @return true if the material exists
     */
    public static boolean hasWitherRose() {
        return hasWitherRose;
    }
    
    /**
     * Safely get a material by name, returning null if it doesn't exist
     * @param name The material name
     * @return The material or null if not found
     */
    public static Material getMaterial(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}