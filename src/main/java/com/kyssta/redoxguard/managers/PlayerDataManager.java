package com.kyssta.redoxguard.managers;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final RedoxGuard plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    public PlayerDataManager(RedoxGuard plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Create player data for a player
     * @param player The player
     * @return The player data
     */
    public PlayerData createData(Player player) {
        PlayerData data = new PlayerData(player);
        playerDataMap.put(player.getUniqueId(), data);
        LogUtil.debug("Created player data for " + player.getName());
        return data;
    }
    
    /**
     * Get player data for a player
     * @param player The player
     * @return The player data, or null if it doesn't exist
     */
    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }
    
    /**
     * Get player data for a player, creating it if it doesn't exist
     * @param player The player
     * @return The player data
     */
    public PlayerData getOrCreatePlayerData(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) {
            data = createData(player);
        }
        return data;
    }
    
    /**
     * Remove player data for a player
     * @param player The player
     */
    public void removeData(Player player) {
        playerDataMap.remove(player.getUniqueId());
        LogUtil.debug("Removed player data for " + player.getName());
    }
    
    /**
     * Save all player data
     */
    public void saveAllData() {
        // In a real implementation, this would save data to a database or file
        // For this example, we'll just log that we're saving data
        LogUtil.debug("Saving data for " + playerDataMap.size() + " players");
    }
    
    /**
     * Get the number of players being tracked
     * @return The number of players
     */
    public int getPlayerCount() {
        return playerDataMap.size();
    }
}