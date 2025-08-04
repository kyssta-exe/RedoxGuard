package com.kyssta.redoxguard.checks.player;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is using simulation hacks (predicting server responses)
 */
public class SimulationCheck extends Check {

    private final Map<UUID, Long> lastActionTime = new HashMap<>();
    private final Map<UUID, Integer> suspiciousActions = new HashMap<>();
    private final Map<UUID, Location> predictedLocations = new HashMap<>();
    
    public SimulationCheck(RedoxGuard plugin) {
        super(plugin, "Simulation", "player");
    }
    
    /**
     * Check if a player is responding to events too quickly (indicating simulation/prediction)
     * @param player The player
     * @param eventType The type of event being checked
     */
    public void checkResponseTime(Player player, String eventType) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first action, just record it
        if (!lastActionTime.containsKey(uuid)) {
            lastActionTime.put(uuid, currentTime);
            return;
        }
        
        long lastTime = lastActionTime.get(uuid);
        
        // Update for next check
        lastActionTime.put(uuid, currentTime);
        
        // Get minimum response time from config based on event type
        long minResponseTime;
        switch (eventType) {
            case "knockback":
                minResponseTime = plugin.getConfigManager().getCheckConfig("player")
                        .getLong("simulation.min-knockback-response-time", 100);
                break;
            case "velocity":
                minResponseTime = plugin.getConfigManager().getCheckConfig("player")
                        .getLong("simulation.min-velocity-response-time", 80);
                break;
            case "teleport":
                minResponseTime = plugin.getConfigManager().getCheckConfig("player")
                        .getLong("simulation.min-teleport-response-time", 150);
                break;
            default:
                minResponseTime = plugin.getConfigManager().getCheckConfig("player")
                        .getLong("simulation.min-response-time", 100);
        }
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping / 2, 100); // Cap at 100ms, use half of ping
        
        // If the response was too quick
        if (currentTime - lastTime < minResponseTime - pingCompensation) {
            incrementSuspiciousActions(player, "responded to " + eventType + " too quickly (" + (currentTime - lastTime) + "ms)");
        }
    }
    
    /**
     * Check if a player's movement indicates simulation/prediction
     * @param player The player
     * @param location The player's current location
     * @param isPredictionCheck Whether this is a check after a server-side event that should disrupt predictions
     */
    public void checkMovementPrediction(Player player, Location location, boolean isPredictionCheck) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        
        if (isPredictionCheck) {
            // This is a check after a server-side event that should disrupt client predictions
            // (like teleport, knockback, or velocity change)
            
            if (predictedLocations.containsKey(uuid)) {
                Location predicted = predictedLocations.get(uuid);
                
                // If the player's actual location is too close to the predicted one
                // (indicating they might be simulating server responses)
                if (predicted.getWorld().equals(location.getWorld())) {
                    double distance = predicted.distance(location);
                    double maxDistance = plugin.getConfigManager().getCheckConfig("player")
                            .getDouble("simulation.max-prediction-distance", 0.5);
                    
                    if (distance < maxDistance) {
                        incrementSuspiciousActions(player, "movement matched prediction too closely after server event");
                    }
                }
                
                // Clear the prediction after checking
                predictedLocations.remove(uuid);
            }
        } else {
            // Store the current location for future prediction checks
            predictedLocations.put(uuid, location.clone());
        }
    }
    
    /**
     * Check if a player is responding to damage too quickly
     * @param player The player
     */
    public void checkDamageResponse(Player player) {
        checkResponseTime(player, "damage");
    }
    
    /**
     * Check if a player is responding to knockback too quickly
     * @param player The player
     */
    public void checkKnockbackResponse(Player player) {
        checkResponseTime(player, "knockback");
    }
    
    /**
     * Check if a player is responding to velocity changes too quickly
     * @param player The player
     */
    public void checkVelocityResponse(Player player) {
        checkResponseTime(player, "velocity");
    }
    
    /**
     * Check if a player is responding to teleportation too quickly
     * @param player The player
     */
    public void checkTeleportResponse(Player player) {
        checkResponseTime(player, "teleport");
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
        int threshold = plugin.getConfigManager().getCheckConfig("player")
                .getInt("simulation.threshold", 5);
        
        debug(player.getName() + " suspicious Simulation action: " + reason + " (" + count + "/" + threshold + ")");
        
        if (count >= threshold) {
            flag(player, "possible Simulation hack: " + reason);
            suspiciousActions.put(uuid, 0); // Reset counter after flagging
        }
    }
}