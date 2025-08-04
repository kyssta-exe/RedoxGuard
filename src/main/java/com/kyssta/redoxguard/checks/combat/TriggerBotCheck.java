package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerBotCheck extends Check {

    private final Map<UUID, Integer> suspiciousHits = new HashMap<>();
    private final Map<UUID, Long> lastHitTime = new HashMap<>();

    public TriggerBotCheck(RedoxGuard plugin) {
        super(plugin, "TriggerBot", "combat");
    }
    
    /**
     * Check if a player is using trigger bot (attacking immediately when looking at an entity)
     * @param player The player
     * @param target The entity being attacked
     */
    public void checkTriggerBot(Player player, Entity target) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Skip if target is not a player (triggerbot is mainly for PvP)
        if (!(target instanceof Player)) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if player is looking at the target with suspicious precision
        if (isLookingAtTarget(player, target)) {
            // Check for suspiciously fast reaction time
            Long lastHit = lastHitTime.get(uuid);
            if (lastHit != null) {
                long timeSinceLastHit = currentTime - lastHit;
                
                // Only flag if reaction time is suspiciously fast (less than 50ms)
                // Normal human reaction time is 150-300ms, so 50ms is clearly cheating
                if (timeSinceLastHit < 50) {
                    int currentSuspicious = suspiciousHits.getOrDefault(uuid, 0);
                    suspiciousHits.put(uuid, currentSuspicious + 1);
                    
                    // Only flag after multiple suspicious hits
                    if (currentSuspicious >= 5) {
                        flag(player, "suspiciously fast reaction time (" + timeSinceLastHit + "ms)");
                        debug(player.getName() + " had suspiciously fast reaction time: " + timeSinceLastHit + "ms");
                    }
                }
            }
            
            lastHitTime.put(uuid, currentTime);
        } else {
            // Reset suspicious hits if player is not looking at target
            suspiciousHits.put(uuid, Math.max(0, suspiciousHits.getOrDefault(uuid, 0) - 1));
        }
    }
    
    /**
     * Check if a player is looking at the target they're attacking
     * @param player The player
     * @param target The entity being attacked
     * @return true if player is looking at target
     */
    private boolean isLookingAtTarget(Player player, Entity target) {
        // Calculate the angle between the player's look vector and the vector to the target
        Location playerLoc = player.getEyeLocation();
        Vector playerDir = playerLoc.getDirection();
        
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0); // Target center
        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();
        
        double angle = Math.toDegrees(Math.acos(playerDir.dot(toTarget)));
        
        // Much more generous angle - only flag if angle is suspiciously small (less than 2 degrees)
        // This means the player is looking almost directly at the target
        return angle < 2.0;
    }
}