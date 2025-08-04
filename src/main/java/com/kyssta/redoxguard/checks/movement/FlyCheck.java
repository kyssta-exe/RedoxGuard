package com.kyssta.redoxguard.checks.movement;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyCheck extends Check {

    private final Map<UUID, Integer> airViolations = new HashMap<>();
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public FlyCheck(RedoxGuard plugin) {
        super(plugin, "Fly", "movement");
    }
    
    /**
     * Check if a player is flying illegally
     * @param player The player
     * @param to The location the player moved to
     */
    public void checkFlight(Player player, Location to) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Skip if player is allowed to fly
        if (player.getAllowFlight() || player.getGameMode() == GameMode.CREATIVE || 
                player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Rate limiting - only check every 1 second
        Long lastCheck = lastCheckTime.get(uuid);
        if (lastCheck != null && currentTime - lastCheck < 1000) {
            return;
        }
        lastCheckTime.put(uuid, currentTime);
        
        // Skip if player is in water, climbing, or in vehicle
        if (isInWater(player) || isClimbing(player) || player.isInsideVehicle()) {
            return;
        }
        
        // Check for illegal upward movement (much more generous)
        if (!data.isOnGround() && data.wasOnGround() && data.getLastDeltaY() > 0.6) {
            // Increased from 0.42 to 0.6 to account for lag and server inconsistencies
            // Check for jump boost potion effect
            double maxJumpHeight = 0.6;
            if (player.hasPotionEffect(PotionEffectType.JUMP)) {
                int level = player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1;
                maxJumpHeight += level * 0.15; // Each level adds 0.15 blocks (increased from 0.1)
            }
            
            // Add buffer for lag compensation
            maxJumpHeight += 0.2; // 0.2 block buffer
            
            if (data.getLastDeltaY() > maxJumpHeight) {
                int currentViolations = airViolations.getOrDefault(uuid, 0);
                airViolations.put(uuid, currentViolations + 1);
                
                // Only flag after multiple violations
                if (currentViolations >= 3) {
                    flag(player, "jumped too high (" + String.format("%.2f", data.getLastDeltaY()) + 
                            " > " + String.format("%.2f", maxJumpHeight) + ")");
                    
                    debug(player.getName() + " jumped too high: " + String.format("%.2f", data.getLastDeltaY()) + 
                            " > " + String.format("%.2f", maxJumpHeight) + " (violations: " + currentViolations + ")");
                }
            }
        }
        
        // Check for staying in air too long (much more generous)
        if (!data.isOnGround() && data.getAirTicks() > 60 && data.getLastDeltaY() >= 0) {
            // Increased from 20 to 60 ticks (3 seconds instead of 1 second)
            // This allows for legitimate air time from jumping, elytra, etc.
            int currentViolations = airViolations.getOrDefault(uuid, 0);
            airViolations.put(uuid, currentViolations + 1);
            
            // Only flag after multiple violations
            if (currentViolations >= 5) {
                flag(player, "stayed in air too long (" + data.getAirTicks() + " ticks)");
                debug(player.getName() + " stayed in air too long: " + data.getAirTicks() + " ticks (violations: " + currentViolations + ")");
            }
        } else {
            // Reset violations if player is on ground
            airViolations.put(uuid, Math.max(0, airViolations.getOrDefault(uuid, 0) - 1));
        }
    }
    
    /**
     * Check if a player is in water
     * @param player The player
     * @return True if the player is in water
     */
    private boolean isInWater(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.WATER;
    }
    
    /**
     * Check if a player is climbing (ladder or vine)
     * @param player The player
     * @return True if the player is climbing
     */
    private boolean isClimbing(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.LADDER || block.getType() == Material.VINE;
    }
}