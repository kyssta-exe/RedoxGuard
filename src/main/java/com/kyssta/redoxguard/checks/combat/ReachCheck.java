package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReachCheck extends Check {

    private final Map<UUID, Integer> reachViolations = new HashMap<>();

    public ReachCheck(RedoxGuard plugin) {
        super(plugin, "Reach", "combat");
    }
    
    /**
     * Check if a player is attacking an entity from too far away
     * @param player The player
     * @param target The entity being attacked
     */
    public void checkReach(Player player, Entity target) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Skip if target is not a player (reach hacks are mainly for PvP)
        if (!(target instanceof Player)) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        UUID uuid = player.getUniqueId();
        
        // Calculate distance between player and target
        Location playerLoc = player.getEyeLocation();
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0); // Target center
        
        double distance = playerLoc.distance(targetLoc);
        
        // Much more generous reach limit - 4.5 blocks instead of 3.1
        double maxReach = 4.5;
        
        // Add generous ping compensation
        int ping = data.getPing();
        double pingCompensation = Math.min(ping / 100.0, 1.0); // Max 1 block compensation
        maxReach += pingCompensation;
        
        // Check if the player is reaching too far
        if (distance > maxReach) {
            int currentViolations = reachViolations.getOrDefault(uuid, 0);
            reachViolations.put(uuid, currentViolations + 1);
            
            // Only flag after multiple violations
            if (currentViolations >= 3) {
                flag(player, "reached too far (" + String.format("%.2f", distance) + 
                        " > " + String.format("%.2f", maxReach) + ")");
                
                debug(player.getName() + " reached too far: " + String.format("%.2f", distance) + 
                        " > " + String.format("%.2f", maxReach) + " (violations: " + currentViolations + ")");
            }
        } else {
            // Reset violations if player is not reaching too far
            reachViolations.put(uuid, Math.max(0, reachViolations.getOrDefault(uuid, 0) - 1));
        }
    }
}