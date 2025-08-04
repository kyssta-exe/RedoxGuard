package com.kyssta.redoxguard.checks.movement;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedCheck extends Check {

    private final Map<UUID, Integer> violations = new HashMap<>();
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public SpeedCheck(RedoxGuard plugin) {
        super(plugin, "Speed", "movement");
    }
    
    /**
     * Check if a player is moving too fast
     * @param player The player
     * @param from The location the player moved from
     * @param to The location the player moved to
     */
    public void checkSpeed(Player player, Location from, Location to) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Skip if player is in vehicle, flying, or has special permissions
        if (player.isInsideVehicle() || player.getAllowFlight() || player.isFlying()) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Rate limiting - only check every 500ms
        Long lastCheck = lastCheckTime.get(uuid);
        if (lastCheck != null && currentTime - lastCheck < 500) {
            return;
        }
        lastCheckTime.put(uuid, currentTime);
        
        // Calculate horizontal distance moved
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distanceSquared = dx * dx + dz * dz;
        
        // Skip if no movement
        if (distanceSquared < 0.01) {
            return;
        }
        
        // Get maximum allowed speed with generous limits
        double maxSpeed = getMaxAllowedSpeed(player);
        double maxSpeedSquared = maxSpeed * maxSpeed;
        
        // Apply ping compensation (more generous)
        int ping = data.getPing();
        double pingCompensation = Math.min(ping / 200.0, 0.5); // Max 0.5 blocks compensation
        maxSpeedSquared += pingCompensation * pingCompensation;
        
        // Check if the player is moving too fast
        if (distanceSquared > maxSpeedSquared) {
            int currentViolations = violations.getOrDefault(uuid, 0);
            violations.put(uuid, currentViolations + 1);
            
            // Only flag after multiple violations
            if (currentViolations >= 3) {
                double actualSpeed = Math.sqrt(distanceSquared);
                flag(player, "moved too fast (" + String.format("%.2f", actualSpeed) + " > " + 
                        String.format("%.2f", maxSpeed) + ")");
                
                debug(player.getName() + " moved too fast: " + String.format("%.2f", actualSpeed) + 
                        " > " + String.format("%.2f", maxSpeed) + " (violations: " + currentViolations + ")");
            }
        } else {
            // Reset violations if player is moving normally
            violations.put(uuid, Math.max(0, violations.getOrDefault(uuid, 0) - 1));
        }
    }
    
    /**
     * Get the maximum allowed speed for a player
     * @param player The player
     * @return The maximum allowed speed
     */
    private double getMaxAllowedSpeed(Player player) {
        // Base speed - much more generous
        double maxSpeed = 0.35; // Increased from 0.2873
        
        // Adjust for sprinting
        if (player.isSprinting()) {
            maxSpeed = 0.45; // More generous sprint speed
        }
        
        // Adjust for speed potion effect
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int level = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            maxSpeed *= 1.0 + (level * 0.3); // Each level adds 30% speed (increased from 20%)
        }
        
        // Adjust for slowness potion effect
        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            int level = player.getPotionEffect(PotionEffectType.SLOW).getAmplifier() + 1;
            maxSpeed *= 1.0 - (level * 0.1); // Each level reduces speed by 10% (reduced from 15%)
        }
        
        // Add buffer for lag and server inconsistencies
        maxSpeed *= 1.2; // 20% buffer
        
        return maxSpeed;
    }
}