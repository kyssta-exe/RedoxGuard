package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is using CrystalAura hacks
 */
public class CrystalAuraCheck extends Check {

    private final Map<UUID, Long> lastCrystalPlaceTime = new HashMap<>();
    private final Map<UUID, Long> lastCrystalBreakTime = new HashMap<>();
    private final Map<UUID, Location> lastCrystalPlaceLocation = new HashMap<>();
    private final Map<UUID, Integer> suspiciousActions = new HashMap<>();
    private final Map<UUID, Integer> crystalSequenceCount = new HashMap<>();
    
    public CrystalAuraCheck(RedoxGuard plugin) {
        super(plugin, "CrystalAura", "combat");
    }
    
    /**
     * Check if a player is placing end crystals in a suspicious pattern
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
                .getLong("crystalaura.min-place-time", 150);
        
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
                        .getDouble("crystalaura.max-distance", 10.0);
                
                if (distance > maxDistance) {
                    incrementSuspiciousActions(player, "placed crystals too quickly at distant locations");
                    return;
                }
            }
            
            incrementSuspiciousActions(player, "placed crystals too quickly (" + (currentTime - lastTime) + "ms)");
        }
        
        // Check if player is looking at the placement location
        checkPlacementAngle(player, location);
    }
    
    /**
     * Check if a player is breaking end crystals in a suspicious pattern
     * @param player The player
     * @param entity The crystal entity
     */
    public void checkCrystalBreak(Player player, Entity entity) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || entity.getType() != EntityType.END_CRYSTAL) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if player recently placed a crystal
        if (lastCrystalPlaceTime.containsKey(uuid)) {
            long lastPlaceTime = lastCrystalPlaceTime.get(uuid);
            long timeSincePlacement = currentTime - lastPlaceTime;
            
            // Get minimum time between place and break from config
            long minPlaceBreakTime = plugin.getConfigManager().getCheckConfig("combat")
                    .getLong("crystalaura.min-place-break-time", 50);
            
            // Apply ping compensation
            PlayerData data = getPlayerData(player);
            int ping = data.getPing();
            long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
            
            // If the break was too fast after placement
            if (timeSincePlacement < minPlaceBreakTime - pingCompensation) {
                // Increment the sequence counter
                int sequenceCount = crystalSequenceCount.getOrDefault(uuid, 0) + 1;
                crystalSequenceCount.put(uuid, sequenceCount);
                
                // Get sequence threshold from config
                int sequenceThreshold = plugin.getConfigManager().getCheckConfig("combat")
                        .getInt("crystalaura.sequence-threshold", 3);
                
                if (sequenceCount >= sequenceThreshold) {
                    incrementSuspiciousActions(player, "performed " + sequenceCount + " rapid place-break sequences");
                    crystalSequenceCount.put(uuid, 0); // Reset after flagging
                }
            } else {
                // Reset sequence counter if time between place and break is normal
                crystalSequenceCount.put(uuid, 0);
            }
        }
        
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
                .getLong("crystalaura.min-break-time", 150);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between breaks is too short
        if (currentTime - lastTime < minBreakTime - pingCompensation) {
            incrementSuspiciousActions(player, "broke crystals too quickly (" + (currentTime - lastTime) + "ms)");
        }
        
        // Check if player is looking at the crystal
        checkBreakAngle(player, entity);
    }
    
    /**
     * Check if a player is looking at the crystal placement location
     * @param player The player
     * @param location The location where the crystal was placed
     */
    private void checkPlacementAngle(Player player, Location location) {
        // Calculate angle between player's look vector and vector to placement location
        Location playerLoc = player.getEyeLocation();
        
        // Vector from player to placement location
        Vector toPlacement = location.toVector().subtract(playerLoc.toVector()).normalize();
        
        // Get player's look vector
        Vector lookDir = playerLoc.getDirection().normalize();
        
        // Calculate dot product
        double dotProduct = toPlacement.dot(lookDir);
        
        // Convert to angle in degrees
        double angle = Math.toDegrees(Math.acos(dotProduct));
        
        // Get maximum angle from config
        double maxAngle = plugin.getConfigManager().getCheckConfig("combat")
                .getDouble("crystalaura.max-place-angle", 30.0);
        
        // If the angle is too large
        if (angle > maxAngle) {
            incrementSuspiciousActions(player, "placed crystal without looking at location (angle: " + angle + "°)");
        }
    }
    
    /**
     * Check if a player is looking at the crystal when breaking it
     * @param player The player
     * @param entity The crystal entity
     */
    private void checkBreakAngle(Player player, Entity entity) {
        // Calculate angle between player's look vector and vector to crystal
        Location playerLoc = player.getEyeLocation();
        Location crystalLoc = entity.getLocation().add(0, 0.5, 0); // Adjust to crystal center
        
        // Vector from player to crystal
        Vector toCrystal = crystalLoc.toVector().subtract(playerLoc.toVector()).normalize();
        
        // Get player's look vector
        Vector lookDir = playerLoc.getDirection().normalize();
        
        // Calculate dot product
        double dotProduct = toCrystal.dot(lookDir);
        
        // Convert to angle in degrees
        double angle = Math.toDegrees(Math.acos(dotProduct));
        
        // Get maximum angle from config
        double maxAngle = plugin.getConfigManager().getCheckConfig("combat")
                .getDouble("crystalaura.max-break-angle", 45.0);
        
        // If the angle is too large
        if (angle > maxAngle) {
            incrementSuspiciousActions(player, "broke crystal without looking at it (angle: " + angle + "°)");
        }
    }
    
    /**
     * Check if a player is placing crystals on obsidian that was just placed
     * @param player The player
     * @param block The obsidian block
     */
    public void checkObsidianCrystalSequence(Player player, Block block) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || block.getType() != Material.OBSIDIAN) {
            return;
        }
        
        // This method would be called when a player places obsidian
        // We'll store the location and time to check if a crystal is placed on it too quickly
        
        // Implementation would track obsidian placements and then check if crystals are placed
        // on the same block coordinates too quickly afterward
        
        // For simplicity, we're not implementing the full logic here, but this would be part
        // of a complete CrystalAura detection system
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
                .getInt("crystalaura.threshold", 5);
        
        debug(player.getName() + " suspicious CrystalAura action: " + reason + " (" + count + "/" + threshold + ")");
        
        if (count >= threshold) {
            flag(player, "possible CrystalAura: " + reason);
            suspiciousActions.put(uuid, 0); // Reset counter after flagging
        }
    }
}