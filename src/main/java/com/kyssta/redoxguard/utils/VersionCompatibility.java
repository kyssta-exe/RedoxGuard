package com.kyssta.redoxguard.utils;

import org.bukkit.Material;

/**
 * Utility class for handling Minecraft version compatibility across different server versions.
 * <p>
 * This class provides a centralized system for managing version-specific features and materials
 * that were introduced in different Minecraft versions. It ensures RedoxGuard functions correctly
 * across the supported version range (1.13-1.21.x) by:
 * <ul>
 *   <li>Detecting the availability of version-specific materials at startup</li>
 *   <li>Providing safe access methods that prevent NoSuchFieldError exceptions</li>
 *   <li>Enabling conditional check logic based on available game features</li>
 *   <li>Supporting graceful degradation for older server versions</li>
 * </ul>
 * <p>
 * The class uses static initialization to detect material availability once during plugin startup,
 * avoiding repeated reflection calls during runtime for optimal performance.
 * 
 * @author RedoxGuard Team
 * @version 1.0
 * @since 1.0.0
 */
public class VersionCompatibility {

    /** Whether RESPAWN_ANCHOR material exists (introduced in 1.16) */
    private static boolean hasRespawnAnchor;
    
    /** Whether CORNFLOWER material exists (introduced in 1.14) */
    private static boolean hasCornflower;
    
    /** Whether LILY_OF_THE_VALLEY material exists (introduced in 1.14) */
    private static boolean hasLilyOfTheValley;
    
    /** Whether WITHER_ROSE material exists (introduced in 1.14) */
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
     * Checks if the RESPAWN_ANCHOR material is available in the current Minecraft version.
     * <p>
     * The Respawn Anchor was introduced in Minecraft 1.16 (Nether Update) and is used
     * by the AutoAnchor check to detect anchor-based hacking. This method allows the
     * check to gracefully disable itself on older versions.
     * 
     * @return {@code true} if RESPAWN_ANCHOR material exists, {@code false} on versions prior to 1.16
     */
    public static boolean hasRespawnAnchor() {
        return hasRespawnAnchor;
    }
    
    /**
     * Checks if the CORNFLOWER material is available in the current Minecraft version.
     * <p>
     * The Cornflower was introduced in Minecraft 1.14 (Village & Pillage Update) as part
     * of the new flower varieties. This method enables version-specific material handling.
     * 
     * @return {@code true} if CORNFLOWER material exists, {@code false} on versions prior to 1.14
     */
    public static boolean hasCornflower() {
        return hasCornflower;
    }
    
    /**
     * Checks if the LILY_OF_THE_VALLEY material is available in the current Minecraft version.
     * <p>
     * The Lily of the Valley was introduced in Minecraft 1.14 (Village & Pillage Update)
     * as part of the new flower varieties. This method enables version-specific material handling.
     * 
     * @return {@code true} if LILY_OF_THE_VALLEY material exists, {@code false} on versions prior to 1.14
     */
    public static boolean hasLilyOfTheValley() {
        return hasLilyOfTheValley;
    }
    
    /**
     * Checks if the WITHER_ROSE material is available in the current Minecraft version.
     * <p>
     * The Wither Rose was introduced in Minecraft 1.14 (Village & Pillage Update) as a
     * unique flower that damages entities. This method enables version-specific material handling.
     * 
     * @return {@code true} if WITHER_ROSE material exists, {@code false} on versions prior to 1.14
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