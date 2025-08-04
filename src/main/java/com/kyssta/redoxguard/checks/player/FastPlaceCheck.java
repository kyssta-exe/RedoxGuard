package com.kyssta.redoxguard.checks.player;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is placing blocks too quickly
 */
public class FastPlaceCheck extends Check {

    private final Map<UUID, Long> lastPlaceTime = new HashMap<>();
    private final Map<UUID, Material> lastPlacedType = new HashMap<>();
    
    public FastPlaceCheck(RedoxGuard plugin) {
        super(plugin, "FastPlace", "player");
    }
    
    /**
     * Check if a player is placing blocks too quickly
     * @param player The player
     * @param block The block being placed
     */
    public void checkBlockPlace(Player player, Block block) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Skip check for creative mode players if configured
        if (player.getGameMode() == GameMode.CREATIVE && 
                plugin.getConfigManager().getConfig().getBoolean("exemptions.creative-mode", false)) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Material blockType = block.getType();
        
        // If this is the first block placed, just record it
        if (!lastPlaceTime.containsKey(uuid)) {
            lastPlaceTime.put(uuid, currentTime);
            lastPlacedType.put(uuid, blockType);
            return;
        }
        
        long lastTime = lastPlaceTime.get(uuid);
        Material lastType = lastPlacedType.get(uuid);
        
        // Update for next check
        lastPlaceTime.put(uuid, currentTime);
        lastPlacedType.put(uuid, blockType);
        
        // Get minimum time between block placements from config
        long minPlaceTime = plugin.getConfigManager().getCheckConfig("player")
                .getLong("fastplace.min-place-time", 50);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // Special case for ender eyes (end portal frames)
        if (blockType == Material.END_PORTAL_FRAME || lastType == Material.END_PORTAL_FRAME) {
            minPlaceTime = plugin.getConfigManager().getCheckConfig("player")
                    .getLong("fastplace.min-special-time", 100);
        }
        
        // Special case for crystals
        if (blockType == Material.END_CRYSTAL || lastType == Material.END_CRYSTAL) {
            minPlaceTime = plugin.getConfigManager().getCheckConfig("player")
                    .getLong("fastplace.min-crystal-time", 100);
        }
        
        // If the time between placements is too short
        if (currentTime - lastTime < minPlaceTime - pingCompensation) {
            flag(player, "placed blocks too quickly (" + (currentTime - lastTime) + "ms, min: " + minPlaceTime + "ms)");
            debug(player.getName() + " placed blocks too quickly: " + (currentTime - lastTime) + "ms, min: " + minPlaceTime + "ms");
        }
    }
    
    /**
     * Check if a player is placing crystals too quickly
     * @param player The player
     */
    public void checkCrystalPlace(Player player) {
        // This is a specialized version of the block place check for end crystals
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first crystal placed, just record it
        if (!lastPlaceTime.containsKey(uuid)) {
            lastPlaceTime.put(uuid, currentTime);
            lastPlacedType.put(uuid, Material.END_CRYSTAL);
            return;
        }
        
        long lastTime = lastPlaceTime.get(uuid);
        
        // Update for next check
        lastPlaceTime.put(uuid, currentTime);
        lastPlacedType.put(uuid, Material.END_CRYSTAL);
        
        // Get minimum time between crystal placements from config
        long minCrystalTime = plugin.getConfigManager().getCheckConfig("player")
                .getLong("fastplace.min-crystal-time", 100);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between placements is too short
        if (currentTime - lastTime < minCrystalTime - pingCompensation) {
            flag(player, "placed crystals too quickly (" + (currentTime - lastTime) + "ms, min: " + minCrystalTime + "ms)");
            debug(player.getName() + " placed crystals too quickly: " + (currentTime - lastTime) + "ms, min: " + minCrystalTime + "ms");
        }
    }
}