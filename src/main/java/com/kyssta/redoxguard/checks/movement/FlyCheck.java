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

public class FlyCheck extends Check {

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
        
        // Skip if player is in water or climbing
        if (isInWater(player) || isClimbing(player)) {
            return;
        }
        
        // Check for illegal upward movement
        if (!data.isOnGround() && data.wasOnGround() && data.getLastDeltaY() > 0.42) {
            // Players can jump up to 0.42 blocks high normally
            // Check for jump boost potion effect
            double maxJumpHeight = 0.42;
                    if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
            int level = player.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier() + 1;
                maxJumpHeight += level * 0.1; // Each level adds 0.1 blocks to jump height
            }
            
            if (data.getLastDeltaY() > maxJumpHeight) {
                flag(player, "jumped too high (" + String.format("%.2f", data.getLastDeltaY()) + 
                        " > " + String.format("%.2f", maxJumpHeight) + ")");
                
                debug(player.getName() + " jumped too high: " + String.format("%.2f", data.getLastDeltaY()) + 
                        " > " + String.format("%.2f", maxJumpHeight));
            }
        }
        
        // Check for staying in air too long
        if (!data.isOnGround() && data.getAirTicks() > 20 && data.getLastDeltaY() >= 0) {
            // Players should start falling after being in the air for a while
            // This check allows for about 1 second of air time before flagging
            flag(player, "stayed in air too long (" + data.getAirTicks() + " ticks)");
            
            debug(player.getName() + " stayed in air too long: " + data.getAirTicks() + " ticks");
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