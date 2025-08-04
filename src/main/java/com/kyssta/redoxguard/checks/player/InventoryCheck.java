package com.kyssta.redoxguard.checks.player;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryCheck extends Check {

    public InventoryCheck(RedoxGuard plugin) {
        super(plugin, "Inventory", "player");
    }
    
    /**
     * Check if a player is using inventory hacks (clicking too fast or moving while in inventory)
     * @param player The player
     * @param event The inventory click event
     */
    public void checkInventoryClick(Player player, InventoryClickEvent event) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        
        // Check if player is moving while in inventory
        if (player.getVelocity().lengthSquared() > 0.01 && 
                event.getInventory().getType() != InventoryType.CRAFTING) {
            // Players shouldn't be able to move while in most inventories
            // Exclude crafting inventory (player inventory) as that's always open
            flag(player, "moved while in inventory");
            
            debug(player.getName() + " moved while in inventory");
        }
    }
    
    /**
     * Check if a player is using inventory hacks (moving items too quickly)
     * @param player The player
     * @param clickTime The time of the click in milliseconds
     */
    public void checkInventorySpeed(Player player, long clickTime) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        PlayerData data = getPlayerData(player);
        
        // Get the last attack time to compare with click time
        long lastAttack = data.getLastAttackTime();
        
        // If the player clicked in inventory very soon after attacking, flag them
        if (lastAttack > 0 && clickTime - lastAttack < 100) {
            // Players shouldn't be able to attack and click in inventory within 100ms
            flag(player, "clicked inventory too soon after attacking (" + 
                    (clickTime - lastAttack) + "ms)");
            
            debug(player.getName() + " clicked inventory too soon after attacking: " + 
                    (clickTime - lastAttack) + "ms");
        }
    }
}