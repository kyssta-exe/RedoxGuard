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

public class KillAuraCheck extends Check {

    private final Map<UUID, Integer> attackViolations = new HashMap<>();
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();

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
        
        // Skip if target is not a player (killaura is mainly for PvP)
        if (!(target instanceof Player)) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check attack frequency (much more generous)
        Long lastAttack = lastAttackTime.get(uuid);
        if (lastAttack != null) {
            long timeSinceLastAttack = currentTime - lastAttack;
            
            // Only flag if attacks are suspiciously fast (less than 50ms between attacks)
            // Normal human clicking is 100-200ms, so 50ms is clearly cheating
            if (timeSinceLastAttack < 50) {
                int currentViolations = attackViolations.getOrDefault(uuid, 0);
                attackViolations.put(uuid, currentViolations + 1);
                
                // Only flag after multiple violations
                if (currentViolations >= 5) {
                    flag(player, "attacked too frequently (" + timeSinceLastAttack + "ms between attacks)");
                    debug(player.getName() + " attacked too frequently: " + timeSinceLastAttack + "ms between attacks (violations: " + currentViolations + ")");
                }
            } else {
                // Reset violations if attacks are normal
                attackViolations.put(uuid, Math.max(0, attackViolations.getOrDefault(uuid, 0) - 1));
            }
        }
        
        lastAttackTime.put(uuid, currentTime);
        
        // Check if player is looking at the target (much more generous)
        if (!isLookingAtTarget(player, target)) {
            int currentViolations = attackViolations.getOrDefault(uuid, 0);
            attackViolations.put(uuid, currentViolations + 1);
            
            // Only flag after multiple violations
            if (currentViolations >= 3) {
                flag(player, "attacked without looking at target");
                debug(player.getName() + " attacked without looking at target (violations: " + currentViolations + ")");
            }
        } else {
            // Reset violations if player is looking at target
            attackViolations.put(uuid, Math.max(0, attackViolations.getOrDefault(uuid, 0) - 1));
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
        
        // Much more generous angle - allow up to 60 degrees (increased from 30)
        // This accounts for legitimate combat scenarios where players might not be looking directly at target
        return angle <= 60;
    }
}