package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TriggerBotCheck extends Check {

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
        
        PlayerData data = getPlayerData(player);
        
        // Update combat data
        data.updateCombat();
        
        // Check reaction time
        checkReactionTime(player, data);
        
        // Check if player is looking at the target
        checkLookingAtTarget(player, target);
    }
    
    /**
     * Check if a player's reaction time is suspiciously fast
     * @param player The player
     * @param data The player data
     */
    private void checkReactionTime(Player player, PlayerData data) {
        int attackCount = data.getAttackCount();
        long lastAttackTime = data.getLastAttackTime();
        long currentTime = System.currentTimeMillis();
        
        // Get minimum reaction time from config
        long minReactionTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("triggerbot.min-reaction-time", 100);
        
        // Apply ping compensation
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 50); // Cap at 50ms, use half of ping
        
        // Check if the player's reaction time is suspiciously fast
        if (attackCount > 1 && (currentTime - lastAttackTime) < minReactionTime - pingCompensation) {
            flag(player, "suspiciously fast reaction time (" + (currentTime - lastAttackTime) + "ms)");
            
            debug(player.getName() + " had suspiciously fast reaction time: " + 
                    (currentTime - lastAttackTime) + "ms (min: " + (minReactionTime - pingCompensation) + "ms)");
        }
    }
    
    /**
     * Check if a player is looking at the target they're attacking with suspicious precision
     * @param player The player
     * @param target The entity being attacked
     */
    private void checkLookingAtTarget(Player player, Entity target) {
        // Calculate the angle between the player's look vector and the vector to the target
        Location playerLoc = player.getEyeLocation();
        Vector playerDir = playerLoc.getDirection();
        
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0); // Target center
        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();
        
        double angle = Math.toDegrees(Math.acos(playerDir.dot(toTarget)));
        
        // Get maximum angle from config
        double maxAngle = plugin.getConfigManager().getCheckConfig("combat")
                .getDouble("triggerbot.max-angle", 5.0);
        
        // TriggerBot users typically have very precise aim
        // If the angle is suspiciously small, flag it
        if (angle < maxAngle) {
            flag(player, "suspiciously precise aim (angle: " + String.format("%.2f", angle) + "°)");
            
            debug(player.getName() + " had suspiciously precise aim: angle = " + 
                    String.format("%.2f", angle) + "° (max: " + maxAngle + "°)");
        }
    }
}