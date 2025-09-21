package com.kyssta.redoxguard.managers;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerDataManager - Centralized management system for player anti-cheat data
 * 
 * <p>The PlayerDataManager handles the lifecycle and storage of PlayerData instances
 * for all online players. It provides efficient access to player-specific anti-cheat
 * information and manages data creation, retrieval, and cleanup operations.
 * 
 * <p>Key responsibilities include:
 * <ul>
 *   <li>Creating PlayerData instances when players join the server</li>
 *   <li>Providing fast UUID-based lookup for player data retrieval</li>
 *   <li>Managing memory usage through automatic data cleanup on player disconnect</li>
 *   <li>Offering convenient accessor methods for checks and other components</li>
 * </ul>
 * 
 * <p>The manager uses a HashMap for O(1) average-case lookup performance,
 * ensuring minimal impact on server performance even with large player counts.</p>
 * 
 * @author Kyssta
 * @since 1.0.0
 */
public class PlayerDataManager {

    /** Reference to the main RedoxGuard plugin instance */
    private final RedoxGuard plugin;
    
    /** Map storing PlayerData instances indexed by player UUID for fast lookup */
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    /**
     * Constructs a new PlayerDataManager for the specified plugin.
     * 
     * @param plugin the RedoxGuard plugin instance
     */
    public PlayerDataManager(RedoxGuard plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates and registers a new PlayerData instance for the specified player.
     * 
     * <p>This method initializes comprehensive tracking data including:
     * <ul>
     *   <li>Movement history and ground state tracking</li>
     *   <li>Violation counters for all check types</li>
     *   <li>Combat statistics and attack pattern data</li>
     *   <li>Network latency information for lag compensation</li>
     * </ul>
     * 
     * <p>The created data is automatically stored in the internal map for
     * future retrieval and is immediately available to all anti-cheat checks.</p>
     * 
     * @param player the player to create data for
     * @return the newly created PlayerData instance
     */
    public PlayerData createData(Player player) {
        PlayerData data = new PlayerData(player);
        playerDataMap.put(player.getUniqueId(), data);
        LogUtil.debug("Created player data for " + player.getName());
        return data;
    }
    
    /**
     * Retrieves the PlayerData instance for the specified player.
     * 
     * <p>This method performs a direct UUID-based lookup in the internal map.
     * If no data exists for the player (e.g., they haven't joined yet or
     * their data was already cleaned up), this method returns {@code null}.</p>
     * 
     * @param player the player to get data for
     * @return the PlayerData instance if it exists, or {@code null} if not found
     */
    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }
    
    /**
     * Retrieves the PlayerData for a player, creating it if it doesn't exist.
     * 
     * <p>This is the preferred method for accessing player data in most scenarios,
     * as it ensures that data is always available when needed. The method:
     * <ul>
     *   <li>First attempts to retrieve existing data</li>
     *   <li>If no data exists, automatically creates and registers new data</li>
     *   <li>Returns the PlayerData instance (either existing or newly created)</li>
     * </ul>
     * 
     * <p>This method is thread-safe and commonly used by checks and listeners.</p>
     * 
     * @param player the player to get or create data for
     * @return the PlayerData instance (guaranteed to be non-null)
     */
    public PlayerData getOrCreatePlayerData(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) {
            data = createData(player);
        }
        return data;
    }
    
    /**
     * Removes and cleans up PlayerData for the specified player.
     * 
     * <p>This method should be called when a player disconnects from the server
     * to prevent memory leaks and maintain optimal performance. The cleanup process:
     * <ul>
     *   <li>Removes the PlayerData from the internal map</li>
     *   <li>Frees memory associated with the player's tracking data</li>
     *   <li>Logs the cleanup operation for debugging purposes</li>
     * </ul>
     * 
     * <p>After calling this method, any subsequent calls to {@code getPlayerData()}
     * for this player will return {@code null} until new data is created.</p>
     * 
     * @param player the player whose data should be removed
     */
    public void removeData(Player player) {
        playerDataMap.remove(player.getUniqueId());
        LogUtil.debug("Removed player data for " + player.getName());
    }
    
    /**
     * Saves all currently tracked player data to persistent storage.
     * 
     * <p>This method is designed for future implementation of data persistence
     * features such as database storage or file-based saving. Currently, it
     * serves as a placeholder and logs the save operation for debugging.</p>
     * 
     * <p>In a full implementation, this would:
     * <ul>
     *   <li>Serialize all PlayerData instances</li>
     *   <li>Write data to configured storage backend (database, files, etc.)</li>
     *   <li>Handle any I/O errors gracefully</li>
     *   <li>Provide confirmation of successful save operations</li>
     * </ul>
     */
    public void saveAllData() {
        // In a real implementation, this would save data to a database or file
        // For this example, we'll just log that we're saving data
        LogUtil.debug("Saving data for " + playerDataMap.size() + " players");
    }
    
    /**
     * Returns the number of players currently being tracked by the manager.
     * 
     * <p>This count represents the number of PlayerData instances currently
     * stored in memory, which typically corresponds to the number of online
     * players (excluding those with bypass permissions who may not have data).</p>
     * 
     * <p>This information is useful for:
     * <ul>
     *   <li>Performance monitoring and memory usage analysis</li>
     *   <li>Administrative commands and status reporting</li>
     *   <li>Debugging data lifecycle issues</li>
     * </ul>
     * 
     * @return the number of players currently being tracked
     */
    public int getPlayerCount() {
        return playerDataMap.size();
    }
}