package com.kyssta.redoxguard.checks.player;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is using AutoTotem hacks
 */
public class AutoTotemCheck extends Check {

    private final Map<UUID, Long> lastTotemEquipTime = new HashMap<>();
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private final Map<UUID, Integer> suspiciousTotemEquips = new HashMap<>();
    
    public AutoTotemCheck(RedoxGuard plugin) {
        super(plugin, "AutoTotem", "player");
    }
    
    /**
     * Check if a player equipped a totem too quickly after taking damage
     * @param player The player
     */
    public void checkTotemEquip(Player player) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Record the totem equip time
        lastTotemEquipTime.put(uuid, currentTime);
        
        // Check if player recently took damage
        if (lastDamageTime.containsKey(uuid)) {
            long lastDamage = lastDamageTime.get(uuid);
            long timeSinceDamage = currentTime - lastDamage;
            
            // Get minimum time threshold from config
            long minReactionTime = plugin.getConfigManager().getCheckConfig("player")
                    .getLong("autototem.min-reaction-time", 150);
            
            // Apply ping compensation
            PlayerData data = getPlayerData(player);
            int ping = data.getPing();
            long pingCompensation = Math.min(ping, 200); // Cap at 200ms
            
            // If the player equipped a totem too quickly after taking damage
            if (timeSinceDamage < minReactionTime - pingCompensation) {
                // Increment suspicious count
                int suspiciousCount = suspiciousTotemEquips.getOrDefault(uuid, 0) + 1;
                suspiciousTotemEquips.put(uuid, suspiciousCount);
                
                // Get threshold from config
                int threshold = plugin.getConfigManager().getCheckConfig("player")
                        .getInt("autototem.threshold", 3);
                
                if (suspiciousCount >= threshold) {
                    flag(player, "equipped totem too quickly after damage (" + timeSinceDamage + "ms)");
                    debug(player.getName() + " equipped totem too quickly after damage: " + timeSinceDamage + "ms");
                    suspiciousTotemEquips.put(uuid, 0); // Reset counter after flagging
                }
            }
        }
    }
    
    /**
     * Record when a player takes damage
     * @param player The player
     */
    public void recordDamage(Player player) {
        UUID uuid = player.getUniqueId();
        lastDamageTime.put(uuid, System.currentTimeMillis());
    }
    
    /**
     * Check for instant totem equip after totem pop
     * @param player The player
     */
    public void checkTotemPop(Player player) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if player has a totem in offhand immediately after pop
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.TOTEM_OF_UNDYING) {
            // Record the time and check if it was too fast
            long minPopTime = plugin.getConfigManager().getCheckConfig("player")
                    .getLong("autototem.min-pop-time", 50);
            
            // Apply ping compensation
            PlayerData data = getPlayerData(player);
            int ping = data.getPing();
            long pingCompensation = Math.min(ping, 100); // Cap at 100ms
            
            // If we have a previous equip time, check the difference
            if (lastTotemEquipTime.containsKey(uuid)) {
                long lastEquip = lastTotemEquipTime.get(uuid);
                long timeSinceEquip = currentTime - lastEquip;
                
                if (timeSinceEquip < minPopTime - pingCompensation) {
                    flag(player, "equipped new totem too quickly after pop (" + timeSinceEquip + "ms)");
                    debug(player.getName() + " equipped new totem too quickly after pop: " + timeSinceEquip + "ms");
                }
            }
            
            // Update the equip time
            lastTotemEquipTime.put(uuid, currentTime);
        }
    }
    
    /**
     * Check for suspicious inventory actions related to totems
     * @param player The player
     * @param slotClicked The inventory slot that was clicked
     * @param cursorItem The item on the cursor
     * @param clickedItem The item in the clicked slot
     */
    public void checkInventoryTotemAction(Player player, int slotClicked, ItemStack cursorItem, ItemStack clickedItem) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Check if this is a totem-related action
        boolean isTotemAction = (cursorItem != null && cursorItem.getType() == Material.TOTEM_OF_UNDYING) || 
                               (clickedItem != null && clickedItem.getType() == Material.TOTEM_OF_UNDYING);
        
        if (isTotemAction) {
            // Check if player is in combat
            PlayerData data = getPlayerData(player);
            long lastAttackTime = data.getLastAttackTime();
            long timeSinceAttack = currentTime - lastAttackTime;
            
            // Get minimum time threshold from config
            long minActionTime = plugin.getConfigManager().getCheckConfig("player")
                    .getLong("autototem.min-inventory-action-time", 500);
            
            // If the player performed a totem action too quickly after combat
            if (timeSinceAttack < minActionTime) {
                // Increment suspicious count
                int suspiciousCount = suspiciousTotemEquips.getOrDefault(uuid, 0) + 1;
                suspiciousTotemEquips.put(uuid, suspiciousCount);
                
                // Get threshold from config
                int threshold = plugin.getConfigManager().getCheckConfig("player")
                        .getInt("autototem.threshold", 3);
                
                if (suspiciousCount >= threshold) {
                    flag(player, "suspicious totem inventory action during combat");
                    debug(player.getName() + " performed suspicious totem inventory action during combat");
                    suspiciousTotemEquips.put(uuid, 0); // Reset counter after flagging
                }
            }
        }
    }
}