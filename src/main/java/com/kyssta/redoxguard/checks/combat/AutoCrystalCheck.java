package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is using AutoCrystal hacks
 */
public class AutoCrystalCheck extends Check {

    private final Map<UUID, Long> lastCrystalPlaceTime = new HashMap<>();
    private final Map<UUID, Long> lastCrystalBreakTime = new HashMap<>();
    private final Map<UUID, Location> lastCrystalPlaceLocation = new HashMap<>();
    private final Map<UUID, Integer> suspiciousActions = new HashMap<>();
    
    public AutoCrystalCheck(RedoxGuard plugin) {
        super(plugin, "AutoCrystal", "combat");
    }
    
    /**
     * Check if a player is placing end crystals too quickly
     * @param player The player
     * @param location The location where the crystal was placed
     */
    public void checkCrystalPlace(Player player, Location location) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first crystal placed, just record it
        if (!lastCrystalPlaceTime.containsKey(uuid)) {
            lastCrystalPlaceTime.put(uuid, currentTime);
            lastCrystalPlaceLocation.put(uuid, location);
            return;
        }
        
        long lastTime = lastCrystalPlaceTime.get(uuid);
        Location lastLocation = lastCrystalPlaceLocation.get(uuid);
        
        // Update for next check
        lastCrystalPlaceTime.put(uuid, currentTime);
        lastCrystalPlaceLocation.put(uuid, location);
        
        // Get minimum time between crystal placements from config
        long minPlaceTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autocrystal.min-place-time", 100);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between placements is too short
        if (currentTime - lastTime < minPlaceTime - pingCompensation) {
            // Check if the crystals are placed far apart (indicates auto-targeting)
            if (lastLocation != null && location.getWorld().equals(lastLocation.getWorld())) {
                double distance = location.distance(lastLocation);
                double maxDistance = plugin.getConfigManager().getCheckConfig("combat")
                        .getDouble("autocrystal.max-distance", 10.0);
                
                if (distance > maxDistance) {
                    incrementSuspiciousActions(player, "placed crystals too quickly at distant locations");
                    return;
                }
            }
            
            incrementSuspiciousActions(player, "placed crystals too quickly (" + (currentTime - lastTime) + "ms)");
        }
    }
    
    /**
     * Check if a player is breaking end crystals too quickly
     * @param player The player
     * @param entity The crystal entity
     */
    public void checkCrystalBreak(Player player, Entity entity) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || entity.getType() != EntityType.END_CRYSTAL) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first crystal broken, just record it
        if (!lastCrystalBreakTime.containsKey(uuid)) {
            lastCrystalBreakTime.put(uuid, currentTime);
            return;
        }
        
        long lastTime = lastCrystalBreakTime.get(uuid);
        
        // Update for next check
        lastCrystalBreakTime.put(uuid, currentTime);
        
        // Get minimum time between crystal breaks from config
        long minBreakTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autocrystal.min-break-time", 100);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between breaks is too short
        if (currentTime - lastTime < minBreakTime - pingCompensation) {
            incrementSuspiciousActions(player, "broke crystals too quickly (" + (currentTime - lastTime) + "ms)");
        }
    }
    
    /**
     * Check if a player is switching between crystal and sword too quickly
     * @param player The player
     * @param newItem The new item in hand
     */
    public void checkItemSwitch(Player player, ItemStack newItem) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        if (newItem == null) {
            return;
        }
        
        Material type = newItem.getType();
        
        // Only check for crystal/sword switches
        if (type != Material.END_CRYSTAL && type != Material.DIAMOND_SWORD && 
            type != Material.NETHERITE_SWORD && type != Material.IRON_SWORD) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if player recently placed or broke a crystal
        long lastPlace = lastCrystalPlaceTime.getOrDefault(uuid, 0L);
        long lastBreak = lastCrystalBreakTime.getOrDefault(uuid, 0L);
        
        long lastAction = Math.max(lastPlace, lastBreak);
        long timeSinceAction = currentTime - lastAction;
        
        // Get minimum time for item switch from config
        long minSwitchTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autocrystal.min-switch-time", 150);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the switch was too fast after crystal action
        if (lastAction > 0 && timeSinceAction < minSwitchTime - pingCompensation) {
            incrementSuspiciousActions(player, "switched items too quickly after crystal action (" + timeSinceAction + "ms)");
        }
    }
    
    /**
     * Check if a player is looking at crystals when breaking them
     * @param player The player
     * @param entity The crystal entity
     */
    public void checkCrystalAngle(Player player, Entity entity) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || entity.getType() != EntityType.END_CRYSTAL) {
            return;
        }
        
        // Calculate angle between player's look vector and vector to crystal
        Location playerLoc = player.getEyeLocation();
        Location crystalLoc = entity.getLocation().add(0, 0.5, 0); // Adjust to crystal center
        
        // Vector from player to crystal
        double dx = crystalLoc.getX() - playerLoc.getX();
        double dy = crystalLoc.getY() - playerLoc.getY();
        double dz = crystalLoc.getZ() - playerLoc.getZ();
        
        // Normalize the vector
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        
        // Get player's look vector
        double lookX = playerLoc.getDirection().getX();
        double lookY = playerLoc.getDirection().getY();
        double lookZ = playerLoc.getDirection().getZ();
        
        // Calculate dot product
        double dotProduct = dx * lookX + dy * lookY + dz * lookZ;
        
        // Convert to angle in degrees
        double angle = Math.toDegrees(Math.acos(dotProduct));
        
        // Get maximum angle from config
        double maxAngle = plugin.getConfigManager().getCheckConfig("combat")
                .getDouble("autocrystal.max-angle", 45.0);
        
        // If the angle is too large
        if (angle > maxAngle) {
            incrementSuspiciousActions(player, "broke crystal without looking at it (angle: " + angle + "°)");
        }
    }
    
    /**
     * Increment the suspicious actions counter and flag if threshold is reached
     * @param player The player
     * @param reason The reason for the suspicious action
     */
    private void incrementSuspiciousActions(Player player, String reason) {
        UUID uuid = player.getUniqueId();
        int count = suspiciousActions.getOrDefault(uuid, 0) + 1;
        suspiciousActions.put(uuid, count);
        
        // Get threshold from config
        int threshold = plugin.getConfigManager().getCheckConfig("combat")
                .getInt("autocrystal.threshold", 3);
        
        debug(player.getName() + " suspicious AutoCrystal action: " + reason + " (" + count + "/" + threshold + ")");
        
        if (count >= threshold) {
            flag(player, "possible AutoCrystal: " + reason);
            suspiciousActions.put(uuid, 0); // Reset counter after flagging
        }
    }
}