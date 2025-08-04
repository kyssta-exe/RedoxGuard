package com.kyssta.redoxguard.listeners;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.combat.KillAuraCheck;
import com.kyssta.redoxguard.checks.combat.ReachCheck;
import com.kyssta.redoxguard.checks.combat.TriggerBotCheck;
import com.kyssta.redoxguard.checks.movement.FlyCheck;
import com.kyssta.redoxguard.checks.movement.SpeedCheck;
import com.kyssta.redoxguard.checks.player.InventoryCheck;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener implements Listener {

    private final RedoxGuard plugin;
    
    public PlayerMovementListener(RedoxGuard plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Skip if player has bypass permission
        if (player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Get player data
        PlayerData data = plugin.getPlayerDataManager().getOrCreatePlayerData(player);
        
        // Update movement data
        data.updateMovement(event.getTo());
        
        // Run movement checks
        SpeedCheck speedCheck = (SpeedCheck) plugin.getCheckManager().getCheckByName("Speed");
        if (speedCheck != null && speedCheck.isEnabled()) {
            speedCheck.checkSpeed(player, event.getFrom(), event.getTo());
        }
        
        FlyCheck flyCheck = (FlyCheck) plugin.getCheckManager().getCheckByName("Fly");
        if (flyCheck != null && flyCheck.isEnabled()) {
            flyCheck.checkFlight(player, event.getTo());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        Entity target = event.getEntity();
        
        // Skip if player has bypass permission
        if (player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Run combat checks
        ReachCheck reachCheck = (ReachCheck) plugin.getCheckManager().getCheckByName("Reach");
        if (reachCheck != null && reachCheck.isEnabled()) {
            reachCheck.checkReach(player, target);
        }
        
        KillAuraCheck killAuraCheck = (KillAuraCheck) plugin.getCheckManager().getCheckByName("KillAura");
        if (killAuraCheck != null && killAuraCheck.isEnabled()) {
            killAuraCheck.checkKillAura(player, target);
        }
        
        TriggerBotCheck triggerBotCheck = (TriggerBotCheck) plugin.getCheckManager().getCheckByName("TriggerBot");
        if (triggerBotCheck != null && triggerBotCheck.isEnabled()) {
            triggerBotCheck.checkTriggerBot(player, target);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicker is a player
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Skip if player has bypass permission
        if (player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Run inventory checks
        InventoryCheck inventoryCheck = (InventoryCheck) plugin.getCheckManager().getCheckByName("Inventory");
        if (inventoryCheck != null && inventoryCheck.isEnabled()) {
            inventoryCheck.checkInventoryClick(player, event);
            inventoryCheck.checkInventorySpeed(player, System.currentTimeMillis());
        }
    }
}