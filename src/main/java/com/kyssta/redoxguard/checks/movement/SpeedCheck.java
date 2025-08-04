package com.kyssta.redoxguard.checks.movement;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SpeedCheck extends Check {

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
        
        PlayerData data = getPlayerData(player);
        
        // Calculate horizontal distance moved
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distanceSquared = dx * dx + dz * dz;
        
        // Get maximum allowed speed
        double maxSpeed = getMaxAllowedSpeed(player);
        double maxSpeedSquared = maxSpeed * maxSpeed;
        
        // Check if the player is moving too fast
        if (distanceSquared > maxSpeedSquared) {
            // Calculate actual speed
            double actualSpeed = Math.sqrt(distanceSquared);
            
            // Flag the player
            flag(player, "moved too fast (" + String.format("%.2f", actualSpeed) + " > " + 
                    String.format("%.2f", maxSpeed) + ")");
            
            debug(player.getName() + " moved too fast: " + String.format("%.2f", actualSpeed) + 
                    " > " + String.format("%.2f", maxSpeed));
        }
    }
    
    /**
     * Get the maximum allowed speed for a player
     * @param player The player
     * @return The maximum allowed speed
     */
    private double getMaxAllowedSpeed(Player player) {
        // Base speed
        double maxSpeed = 0.2873; // Normal walking speed
        
        // Adjust for sprinting
        if (player.isSprinting()) {
            maxSpeed *= 1.3; // Sprinting is about 30% faster
        }
        
        // Adjust for speed potion effect
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int level = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            maxSpeed *= 1.0 + (level * 0.2); // Each level adds 20% speed
        }
        
        // Adjust for slowness potion effect
        if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            int level = player.getPotionEffect(PotionEffectType.SLOW).getAmplifier() + 1;
            maxSpeed *= 1.0 - (level * 0.15); // Each level reduces speed by 15%
        }
        
        // Add a small buffer for lag compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        double pingCompensation = 1.0 + (ping / 1000.0); // Add 0.1% per ms of ping
        maxSpeed *= pingCompensation;
        
        return maxSpeed;
    }
}