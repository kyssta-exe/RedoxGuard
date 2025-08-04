package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ReachCheck extends Check {

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
        
        PlayerData data = getPlayerData(player);
        
        // Calculate distance between player and target
        Location playerLoc = player.getEyeLocation();
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0); // Target center
        
        double distance = playerLoc.distance(targetLoc);
        
        // Get maximum allowed reach distance from config
        double maxReach = plugin.getConfigManager().getCheckConfig(getType())
                .getDoubleValue("reach.max-distance", 3.1);
        
        // Add a small buffer for lag compensation
        int ping = data.getPing();
        double pingCompensation = ping / 1000.0; // Add 0.001 blocks per ms of ping
        maxReach += pingCompensation;
        
        // Check if the player is reaching too far
        if (distance > maxReach) {
            flag(player, "reached too far (" + String.format("%.2f", distance) + 
                    " > " + String.format("%.2f", maxReach) + ")");
            
            debug(player.getName() + " reached too far: " + String.format("%.2f", distance) + 
                    " > " + String.format("%.2f", maxReach));
        }
    }
}