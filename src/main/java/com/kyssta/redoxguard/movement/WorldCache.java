package com.kyssta.redoxguard.movement;

import com.kyssta.redoxguard.RedoxGuard;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World cache system inspired by Grim
 * Maintains per-player world replicas for accurate movement prediction
 */
public class WorldCache {

    private final RedoxGuard plugin;
    private final Map<UUID, PlayerWorldCache> playerCaches = new ConcurrentHashMap<>();

    public WorldCache(RedoxGuard plugin) {
        this.plugin = plugin;
    }

    /**
     * Get or create world cache for a player
     */
    public PlayerWorldCache getPlayerCache(Player player) {
        return playerCaches.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerWorldCache(player));
    }

    /**
     * Update block in player's world cache
     */
    public void updateBlock(Player player, Location location, Material material) {
        PlayerWorldCache cache = getPlayerCache(player);
        cache.updateBlock(location, material);
    }

    /**
     * Get block from player's world cache
     */
    public Material getBlock(Player player, Location location) {
        PlayerWorldCache cache = getPlayerCache(player);
        return cache.getBlock(location);
    }

    /**
     * Remove player cache when they disconnect
     */
    public void removePlayerCache(Player player) {
        playerCaches.remove(player.getUniqueId());
    }

    /**
     * Per-player world cache
     */
    public static class PlayerWorldCache {
        private final Player player;
        private final Map<Long, Material> blockCache = new HashMap<>();
        private final Map<Long, Long> blockTimestamps = new HashMap<>();

        public PlayerWorldCache(Player player) {
            this.player = player;
        }

        /**
         * Update a block in the cache
         */
        public void updateBlock(Location location, Material material) {
            long key = getBlockKey(location);
            blockCache.put(key, material);
            blockTimestamps.put(key, System.currentTimeMillis());
        }

        /**
         * Get a block from the cache or world
         */
        public Material getBlock(Location location) {
            long key = getBlockKey(location);
            Material cached = blockCache.get(key);
            
            if (cached != null) {
                return cached;
            }
            
            // Fallback to actual world
            Block block = location.getBlock();
            Material material = block.getType();
            
            // Cache the result
            blockCache.put(key, material);
            blockTimestamps.put(key, System.currentTimeMillis());
            
            return material;
        }

        /**
         * Check if a location is solid
         */
        public boolean isSolid(Location location) {
            Material material = getBlock(location);
            return material.isSolid();
        }

        /**
         * Check if a location is liquid
         */
        public boolean isLiquid(Location location) {
            Material material = getBlock(location);
            return material == Material.WATER || material == Material.LAVA;
        }

        /**
         * Check if a location is climbable
         */
        public boolean isClimbable(Location location) {
            Material material = getBlock(location);
            return material == Material.LADDER || material == Material.VINE;
        }

        /**
         * Check if a location is cobweb
         */
        public boolean isCobweb(Location location) {
            Material material = getBlock(location);
            return material == Material.COBWEB;
        }

        /**
         * Check if a location is bubble column
         */
        public boolean isBubbleColumn(Location location) {
            Material material = getBlock(location);
            return material == Material.BUBBLE_COLUMN;
        }

        /**
         * Get block key for caching
         */
        private long getBlockKey(Location location) {
            return ((long) location.getBlockX() << 32) | 
                   ((long) location.getBlockY() << 16) | 
                   location.getBlockZ();
        }

        /**
         * Clean up old cache entries
         */
        public void cleanup() {
            long currentTime = System.currentTimeMillis();
            long maxAge = 300000; // 5 minutes
            
            blockTimestamps.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > maxAge);
            
            // Remove corresponding block cache entries
            blockCache.keySet().removeIf(key -> 
                !blockTimestamps.containsKey(key));
        }
    }
} 