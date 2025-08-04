package com.kyssta.redoxguard.listeners;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerConnectionListener implements Listener {

    private final RedoxGuard plugin;
    
    public PlayerConnectionListener(RedoxGuard plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Create player data
        PlayerData data = plugin.getPlayerDataManager().createData(player);
        
        // Start ping update task
        startPingUpdateTask(player);
        
        // Log join
        LogUtil.debug(player.getName() + " joined, created player data");
        
        // Check if player has bypass permission
        if (player.hasPermission("redoxguard.bypass")) {
            LogUtil.debug(player.getName() + " has bypass permission");
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove player data
        plugin.getPlayerDataManager().removeData(player);
        
        // Log quit
        LogUtil.debug(player.getName() + " quit, removed player data");
    }
    
    /**
     * Start a task to update the player's ping
     * @param player The player
     */
    private void startPingUpdateTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Get player data
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                if (data == null) {
                    cancel();
                    return;
                }
                
                // Update ping
                try {
                    // Get ping using reflection (works on most versions)
                    int ping = player.getPing();
                    data.updatePing(ping);
                } catch (Exception e) {
                    // Fallback to a default ping if reflection fails
                    data.updatePing(100);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Update every second
    }
}