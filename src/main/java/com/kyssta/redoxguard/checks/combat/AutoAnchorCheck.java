package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is using AutoAnchor/AnchorAura hacks
 */
public class AutoAnchorCheck extends Check {

    private final Map<UUID, Long> lastAnchorPlaceTime = new HashMap<>();
    private final Map<UUID, Long> lastAnchorChargeTime = new HashMap<>();
    private final Map<UUID, Long> lastAnchorDetonateTime = new HashMap<>();
    private final Map<UUID, Location> lastAnchorLocation = new HashMap<>();
    private final Map<UUID, Integer> suspiciousActions = new HashMap<>();
    
    public AutoAnchorCheck(RedoxGuard plugin) {
        super(plugin, "AutoAnchor", "combat");
    }
    
    /**
     * Check if a player is placing respawn anchors too quickly
     * @param player The player
     * @param block The block being placed
     */
    public void checkAnchorPlace(Player player, Block block) {
        // Skip if the check is disabled, player has bypass permission, or RESPAWN_ANCHOR doesn't exist in this version
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || 
            !com.kyssta.redoxguard.utils.VersionCompatibility.hasRespawnAnchor() || 
            block.getType() != Material.RESPAWN_ANCHOR) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Location location = block.getLocation();
        
        // If this is the first anchor placed, just record it
        if (!lastAnchorPlaceTime.containsKey(uuid)) {
            lastAnchorPlaceTime.put(uuid, currentTime);
            lastAnchorLocation.put(uuid, location);
            return;
        }
        
        long lastTime = lastAnchorPlaceTime.get(uuid);
        Location lastLocation = lastAnchorLocation.get(uuid);
        
        // Update for next check
        lastAnchorPlaceTime.put(uuid, currentTime);
        lastAnchorLocation.put(uuid, location);
        
        // Get minimum time between anchor placements from config
        long minPlaceTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autoanchor.min-place-time", 200);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between placements is too short
        if (currentTime - lastTime < minPlaceTime - pingCompensation) {
            // Check if the anchors are placed far apart (indicates auto-targeting)
            if (lastLocation != null && location.getWorld().equals(lastLocation.getWorld())) {
                double distance = location.distance(lastLocation);
                double maxDistance = plugin.getConfigManager().getCheckConfig("combat")
                        .getDouble("autoanchor.max-distance", 10.0);
                
                if (distance > maxDistance) {
                    incrementSuspiciousActions(player, "placed anchors too quickly at distant locations");
                    return;
                }
            }
            
            incrementSuspiciousActions(player, "placed anchors too quickly (" + (currentTime - lastTime) + "ms)");
        }
    }
    
    /**
     * Check if a player is charging respawn anchors too quickly
     * @param player The player
     * @param block The anchor block
     */
    public void checkAnchorCharge(Player player, Block block) {
        // Skip if the check is disabled, player has bypass permission, or RESPAWN_ANCHOR doesn't exist in this version
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || 
            !com.kyssta.redoxguard.utils.VersionCompatibility.hasRespawnAnchor() || 
            block.getType() != Material.RESPAWN_ANCHOR) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first anchor charged, just record it
        if (!lastAnchorChargeTime.containsKey(uuid)) {
            lastAnchorChargeTime.put(uuid, currentTime);
            return;
        }
        
        long lastTime = lastAnchorChargeTime.get(uuid);
        
        // Update for next check
        lastAnchorChargeTime.put(uuid, currentTime);
        
        // Get minimum time between anchor charges from config
        long minChargeTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autoanchor.min-charge-time", 150);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between charges is too short
        if (currentTime - lastTime < minChargeTime - pingCompensation) {
            incrementSuspiciousActions(player, "charged anchors too quickly (" + (currentTime - lastTime) + "ms)");
        }
        
        // Check time since placement (place-charge sequence)
        if (lastAnchorPlaceTime.containsKey(uuid)) {
            long lastPlaceTime = lastAnchorPlaceTime.get(uuid);
            long timeSincePlacement = currentTime - lastPlaceTime;
            
            long minPlaceChargeTime = plugin.getConfigManager().getCheckConfig("combat")
                    .getLong("autoanchor.min-place-charge-time", 100);
            
            if (timeSincePlacement < minPlaceChargeTime - pingCompensation) {
                incrementSuspiciousActions(player, "charged anchor too quickly after placement (" + timeSincePlacement + "ms)");
            }
        }
    }
    
    /**
     * Check if a player is detonating respawn anchors too quickly
     * @param player The player
     * @param block The anchor block
     */
    public void checkAnchorDetonate(Player player, Block block) {
        // Skip if the check is disabled, player has bypass permission, or RESPAWN_ANCHOR doesn't exist in this version
        if (!isEnabled() || player.hasPermission("redoxguard.bypass") || 
            !com.kyssta.redoxguard.utils.VersionCompatibility.hasRespawnAnchor() || 
            block.getType() != Material.RESPAWN_ANCHOR) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first anchor detonated, just record it
        if (!lastAnchorDetonateTime.containsKey(uuid)) {
            lastAnchorDetonateTime.put(uuid, currentTime);
            return;
        }
        
        long lastTime = lastAnchorDetonateTime.get(uuid);
        
        // Update for next check
        lastAnchorDetonateTime.put(uuid, currentTime);
        
        // Get minimum time between anchor detonations from config
        long minDetonateTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autoanchor.min-detonate-time", 300);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the time between detonations is too short
        if (currentTime - lastTime < minDetonateTime - pingCompensation) {
            incrementSuspiciousActions(player, "detonated anchors too quickly (" + (currentTime - lastTime) + "ms)");
        }
        
        // Check time since charge (charge-detonate sequence)
        if (lastAnchorChargeTime.containsKey(uuid)) {
            long lastChargeTime = lastAnchorChargeTime.get(uuid);
            long timeSinceCharge = currentTime - lastChargeTime;
            
            long minChargeDetonateTime = plugin.getConfigManager().getCheckConfig("combat")
                    .getLong("autoanchor.min-charge-detonate-time", 150);
            
            if (timeSinceCharge < minChargeDetonateTime - pingCompensation) {
                incrementSuspiciousActions(player, "detonated anchor too quickly after charging (" + timeSinceCharge + "ms)");
            }
        }
    }
    
    /**
     * Check if a player is switching between anchor-related items too quickly
     * @param player The player
     * @param newItem The new item in hand
     */
    public void checkItemSwitch(Player player, ItemStack newItem) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        if (newItem == null) {
            return;
        }
        
        Material type = newItem.getType();
        
        // Only check for anchor/glowstone/weapon switches
        boolean isAnchorRelated = (com.kyssta.redoxguard.utils.VersionCompatibility.hasRespawnAnchor() && type == Material.RESPAWN_ANCHOR) || 
                                  type == Material.GLOWSTONE || 
                                  type == Material.DIAMOND_SWORD || 
                                  type == Material.NETHERITE_SWORD || 
                                  type == Material.IRON_SWORD;
        
        if (!isAnchorRelated) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if player recently performed an anchor action
        long lastPlace = lastAnchorPlaceTime.getOrDefault(uuid, 0L);
        long lastCharge = lastAnchorChargeTime.getOrDefault(uuid, 0L);
        long lastDetonate = lastAnchorDetonateTime.getOrDefault(uuid, 0L);
        
        long lastAction = Math.max(Math.max(lastPlace, lastCharge), lastDetonate);
        long timeSinceAction = currentTime - lastAction;
        
        // Get minimum time for item switch from config
        long minSwitchTime = plugin.getConfigManager().getCheckConfig("combat")
                .getLong("autoanchor.min-switch-time", 150);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the switch was too fast after anchor action
        if (lastAction > 0 && timeSinceAction < minSwitchTime - pingCompensation) {
            incrementSuspiciousActions(player, "switched items too quickly after anchor action (" + timeSinceAction + "ms)");
        }
    }
    
    /**
     * Increment the suspicious actions counter and flag if threshold is reached
     * @param player The player
     * @param reason The reason for the suspicious action
     */
    private void incrementSuspiciousActions(Player player, String reason) {
        UUID uuid = player.getUniqueId();
        int count = suspiciousActions.getOrDefault(uuid, 0) + 1;
        suspiciousActions.put(uuid, count);
        
        // Get threshold from config
        int threshold = plugin.getConfigManager().getCheckConfig("combat")
                .getInt("autoanchor.threshold", 3);
        
        debug(player.getName() + " suspicious AutoAnchor action: " + reason + " (" + count + "/" + threshold + ")");
        
        if (count >= threshold) {
            flag(player, "possible AutoAnchor: " + reason);
            suspiciousActions.put(uuid, 0); // Reset counter after flagging
        }
    }
}