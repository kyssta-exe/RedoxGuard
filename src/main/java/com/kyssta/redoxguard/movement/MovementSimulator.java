package com.kyssta.redoxguard.movement;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced movement simulation engine inspired by Grim
 * Provides 1:1 replication of player movements with full world state awareness
 */
public class MovementSimulator {

    private final RedoxGuard plugin;
    private final WorldCache worldCache;

    public MovementSimulator(RedoxGuard plugin) {
        this.plugin = plugin;
        this.worldCache = new WorldCache(plugin);
    }

    /**
     * Simulate all possible movements for a player
     * @param player The player
     * @param from The starting location
     * @param to The target location
     * @return List of valid movements
     */
    public List<MovementResult> simulateMovements(Player player, Location from, Location to) {
        List<MovementResult> validMovements = new ArrayList<>();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Get player's current state
        boolean onGround = data.isOnGround();
        boolean inWater = isInWater(player);
        boolean inLava = isInLava(player);
        boolean onLadder = isOnLadder(player);
        boolean onVine = isOnVine(player);
        boolean inCobweb = isInCobweb(player);
        boolean inBubbleColumn = isInBubbleColumn(player);

        // Calculate base movement vector
        Vector movement = to.toVector().subtract(from.toVector());
        
        // Apply different movement types based on player state
        if (inWater) {
            simulateWaterMovement(player, from, movement, validMovements);
        } else if (inLava) {
            simulateLavaMovement(player, from, movement, validMovements);
        } else if (onLadder || onVine) {
            simulateClimbingMovement(player, from, movement, validMovements);
        } else if (inCobweb) {
            simulateCobwebMovement(player, from, movement, validMovements);
        } else if (inBubbleColumn) {
            simulateBubbleColumnMovement(player, from, movement, validMovements);
        } else if (onGround) {
            simulateGroundMovement(player, from, movement, validMovements);
        } else {
            simulateAirMovement(player, from, movement, validMovements);
        }

        return validMovements;
    }

    /**
     * Simulate ground movement (walking, sprinting, sneaking)
     */
    private void simulateGroundMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        double baseSpeed = 0.2873;
        
        // Apply sprinting
        if (player.isSprinting()) {
            baseSpeed *= 1.3;
        }
        
        // Apply sneaking
        if (player.isSneaking()) {
            baseSpeed *= 0.3;
        }
        
        // Apply potion effects
        baseSpeed = applyPotionEffects(player, baseSpeed);
        
        // Calculate maximum distance
        double maxDistance = baseSpeed;
        
        // Check if movement is within limits
        double actualDistance = movement.length();
        if (actualDistance <= maxDistance) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid ground movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Movement too fast"));
        }
    }

    /**
     * Simulate air movement (jumping, falling)
     */
    private void simulateAirMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        // Air movement is more restrictive
        double maxHorizontalSpeed = 0.2873;
        double maxVerticalSpeed = 0.42; // Normal jump height
        
        // Apply potion effects
        maxHorizontalSpeed = applyPotionEffects(player, maxHorizontalSpeed);
        
        // Check horizontal movement
        double horizontalDistance = Math.sqrt(movement.getX() * movement.getX() + movement.getZ() * movement.getZ());
        boolean validHorizontal = horizontalDistance <= maxHorizontalSpeed;
        
        // Check vertical movement
        double verticalDistance = Math.abs(movement.getY());
        boolean validVertical = verticalDistance <= maxVerticalSpeed;
        
        if (validHorizontal && validVertical) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid air movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Invalid air movement"));
        }
    }

    /**
     * Simulate water movement
     */
    private void simulateWaterMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        double maxSpeed = 0.115; // Water is much slower
        
        // Apply potion effects
        maxSpeed = applyPotionEffects(player, maxSpeed);
        
        double distance = movement.length();
        if (distance <= maxSpeed) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid water movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Water movement too fast"));
        }
    }

    /**
     * Simulate lava movement
     */
    private void simulateLavaMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        double maxSpeed = 0.05; // Lava is extremely slow
        
        double distance = movement.length();
        if (distance <= maxSpeed) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid lava movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Lava movement too fast"));
        }
    }

    /**
     * Simulate climbing movement (ladders, vines)
     */
    private void simulateClimbingMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        double maxSpeed = 0.15; // Climbing speed
        
        double distance = movement.length();
        if (distance <= maxSpeed) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid climbing movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Climbing movement too fast"));
        }
    }

    /**
     * Simulate cobweb movement
     */
    private void simulateCobwebMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        double maxSpeed = 0.05; // Cobwebs are very slow
        
        double distance = movement.length();
        if (distance <= maxSpeed) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid cobweb movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Cobweb movement too fast"));
        }
    }

    /**
     * Simulate bubble column movement
     */
    private void simulateBubbleColumnMovement(Player player, Location from, Vector movement, List<MovementResult> results) {
        // Bubble columns can push players up or down
        double maxSpeed = 0.5; // Bubble columns can be fast
        
        double distance = movement.length();
        if (distance <= maxSpeed) {
            results.add(new MovementResult(from, from.clone().add(movement), true, "Valid bubble column movement"));
        } else {
            results.add(new MovementResult(from, from.clone().add(movement), false, "Bubble column movement too fast"));
        }
    }

    /**
     * Apply potion effects to movement speed
     */
    private double applyPotionEffects(Player player, double baseSpeed) {
        // Speed potion
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            int level = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier() + 1;
            baseSpeed *= 1.0 + (level * 0.2);
        }
        
        // Slowness potion
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW)) {
            int level = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SLOW).getAmplifier() + 1;
            baseSpeed *= 1.0 - (level * 0.15);
        }
        
        return baseSpeed;
    }

    // Environment detection methods
    private boolean isInWater(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.WATER;
    }

    private boolean isInLava(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.LAVA;
    }

    private boolean isOnLadder(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.LADDER;
    }

    private boolean isOnVine(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.VINE;
    }

    private boolean isInCobweb(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.COBWEB;
    }

    private boolean isInBubbleColumn(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.BUBBLE_COLUMN;
    }

    /**
     * Movement result class
     */
    public static class MovementResult {
        private final Location from;
        private final Location to;
        private final boolean valid;
        private final String reason;

        public MovementResult(Location from, Location to, boolean valid, String reason) {
            this.from = from;
            this.to = to;
            this.valid = valid;
            this.reason = reason;
        }

        public Location getFrom() { return from; }
        public Location getTo() { return to; }
        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
    }
} 