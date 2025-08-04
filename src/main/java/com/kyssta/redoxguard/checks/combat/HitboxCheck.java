package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

/**
 * Checks if a player is hitting entities outside their hitbox
 */
public class HitboxCheck extends Check {

    public HitboxCheck(RedoxGuard plugin) {
        super(plugin, "Hitbox", "combat");
    }
    
    /**
     * Check if a player is hitting entities outside their hitbox
     * @param player The player
     * @param target The entity being attacked
     */
    public void checkHitbox(Player player, Entity target) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Get the target's bounding box
        BoundingBox targetBox = target.getBoundingBox();
        
        // Expand the box slightly to account for lag and minor discrepancies
        // Default Minecraft hitboxes have a small margin of error
        double expansionAmount = plugin.getConfigManager().getCheckConfig("combat")
                .getDouble("hitbox.expansion-amount", 0.3);
        
        BoundingBox expandedBox = targetBox.expand(expansionAmount, expansionAmount, expansionAmount);
        
        // Get the player's look vector
        double maxDistance = plugin.getConfigManager().getCheckConfig("combat")
                .getDouble("hitbox.max-distance", 4.5);
        
        // Ray trace from the player's eyes
        boolean hit = player.getEyeLocation().getDirection().normalize()
                .multiply(maxDistance)
                .toLocation(player.getWorld(), player.getEyeLocation().getX(), 
                        player.getEyeLocation().getY(), player.getEyeLocation().getZ())
                .toVector()
                .isInBox(expandedBox);
        
        if (!hit) {
            // Apply ping compensation
            int ping = getPlayerData(player).getPing();
            double pingCompensation = Math.min(ping / 100.0, 0.5); // Max 0.5 blocks of compensation
            
            // Expand box further based on ping
            BoundingBox pingCompensatedBox = expandedBox.expand(
                    pingCompensation, pingCompensation, pingCompensation);
            
            hit = player.getEyeLocation().getDirection().normalize()
                    .multiply(maxDistance)
                    .toLocation(player.getWorld(), player.getEyeLocation().getX(), 
                            player.getEyeLocation().getY(), player.getEyeLocation().getZ())
                    .toVector()
                    .isInBox(pingCompensatedBox);
            
            if (!hit) {
                flag(player, "hit outside hitbox (distance too far)");
                debug(player.getName() + " hit outside hitbox of " + target.getType().name());
            }
        }
    }
}