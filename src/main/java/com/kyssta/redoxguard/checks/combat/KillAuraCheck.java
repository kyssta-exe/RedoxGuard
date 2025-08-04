package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KillAuraCheck extends Check {

    public KillAuraCheck(RedoxGuard plugin) {
        super(plugin, "KillAura", "combat");
    }
    
    /**
     * Check if a player is using kill aura (attacking too many entities or attacking without looking)
     * @param player The player
     * @param target The entity being attacked
     */
    public void checkKillAura(Player player, Entity target) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        
        // Update combat data
        data.updateCombat();
        
        // Check attack frequency
        checkAttackFrequency(player, data);
        
        // Check if player is looking at the target
        checkLookingAtTarget(player, target);
    }
    
    /**
     * Check if a player is attacking too frequently
     * @param player The player
     * @param data The player data
     */
    private void checkAttackFrequency(Player player, PlayerData data) {
        int attackCount = data.getAttackCount();
        
        // Most players can't legitimately attack more than 15 times per second
        if (attackCount > 15) {
            flag(player, "attacked too frequently (" + attackCount + " attacks/sec)");
            
            debug(player.getName() + " attacked too frequently: " + attackCount + " attacks/sec");
        }
    }
    
    /**
     * Check if a player is looking at the target they're attacking
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
        
        // Players should be looking at what they're attacking
        // Allow for some margin of error (30 degrees)
        if (angle > 30) {
            flag(player, "attacked without looking (angle: " + String.format("%.2f", angle) + "°)");
            
            debug(player.getName() + " attacked without looking: angle = " + 
                    String.format("%.2f", angle) + "°");
        }
    }
}