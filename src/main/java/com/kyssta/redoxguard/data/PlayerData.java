package com.kyssta.redoxguard.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    private final Player player;
    private final Map<String, Integer> violations = new HashMap<>();
    private final Map<String, Long> lastViolationTime = new HashMap<>();
    
    // Movement data
    private Location lastLocation;
    private Location lastGroundLocation;
    private long lastMovementTime;
    private boolean onGround;
    private boolean wasOnGround;
    private double lastDeltaY;
    private int airTicks;
    private int groundTicks;
    
    // Combat data
    private long lastAttackTime;
    private int attackCount;
    private long attackCountResetTime;
    
    // Latency data
    private int ping;
    private long lastPingUpdate;
    
    public PlayerData(Player player) {
        this.player = player;
        this.lastLocation = player.getLocation();
        this.lastGroundLocation = player.getLocation();
        this.lastMovementTime = System.currentTimeMillis();
        this.onGround = player.isOnGround();
        this.wasOnGround = player.isOnGround();
        this.lastDeltaY = 0;
        this.airTicks = 0;
        this.groundTicks = 0;
        this.lastAttackTime = 0;
        this.attackCount = 0;
        this.attackCountResetTime = 0;
        this.ping = 0;
        this.lastPingUpdate = 0;
    }
    
    /**
     * Add a violation for a specific check
     * @param check The name of the check
     * @return The new violation level
     */
    public int addViolation(String check) {
        int vl = getViolationLevel(check) + 1;
        violations.put(check, vl);
        lastViolationTime.put(check, System.currentTimeMillis());
        return vl;
    }
    
    /**
     * Get the violation level for a specific check
     * @param check The name of the check
     * @return The violation level
     */
    public int getViolationLevel(String check) {
        return violations.getOrDefault(check, 0);
    }
    
    /**
     * Reset the violation level for a specific check
     * @param check The name of the check
     */
    public void resetViolations(String check) {
        violations.put(check, 0);
    }
    
    /**
     * Get the time of the last violation for a specific check
     * @param check The name of the check
     * @return The time of the last violation, or 0 if there are no violations
     */
    public long getLastViolationTime(String check) {
        return lastViolationTime.getOrDefault(check, 0L);
    }
    
    /**
     * Update movement data
     * @param location The new location
     */
    public void updateMovement(Location location) {
        this.wasOnGround = this.onGround;
        this.onGround = player.isOnGround();
        this.lastDeltaY = location.getY() - lastLocation.getY();
        this.lastLocation = location;
        this.lastMovementTime = System.currentTimeMillis();
        
        if (onGround) {
            this.groundTicks++;
            this.airTicks = 0;
            this.lastGroundLocation = location;
        } else {
            this.airTicks++;
            this.groundTicks = 0;
        }
    }
    
    /**
     * Update combat data
     */
    public void updateCombat() {
        long now = System.currentTimeMillis();
        this.lastAttackTime = now;
        
        if (now > attackCountResetTime) {
            this.attackCount = 1;
            this.attackCountResetTime = now + 1000; // Reset after 1 second
        } else {
            this.attackCount++;
        }
    }
    
    /**
     * Update ping data
     * @param ping The new ping
     */
    public void updatePing(int ping) {
        this.ping = ping;
        this.lastPingUpdate = System.currentTimeMillis();
    }
    
    /**
     * Get the player
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get the last location
     * @return The last location
     */
    public Location getLastLocation() {
        return lastLocation;
    }
    
    /**
     * Get the last ground location
     * @return The last ground location
     */
    public Location getLastGroundLocation() {
        return lastGroundLocation;
    }
    
    /**
     * Get the last movement time
     * @return The last movement time
     */
    public long getLastMovementTime() {
        return lastMovementTime;
    }
    
    /**
     * Check if the player is on the ground
     * @return True if the player is on the ground
     */
    public boolean isOnGround() {
        return onGround;
    }
    
    /**
     * Check if the player was on the ground in the previous tick
     * @return True if the player was on the ground
     */
    public boolean wasOnGround() {
        return wasOnGround;
    }
    
    /**
     * Get the last Y-axis movement
     * @return The last Y-axis movement
     */
    public double getLastDeltaY() {
        return lastDeltaY;
    }
    
    /**
     * Get the number of ticks the player has been in the air
     * @return The number of air ticks
     */
    public int getAirTicks() {
        return airTicks;
    }
    
    /**
     * Get the number of ticks the player has been on the ground
     * @return The number of ground ticks
     */
    public int getGroundTicks() {
        return groundTicks;
    }
    
    /**
     * Get the time of the last attack
     * @return The time of the last attack
     */
    public long getLastAttackTime() {
        return lastAttackTime;
    }
    
    /**
     * Get the number of attacks in the current second
     * @return The attack count
     */
    public int getAttackCount() {
        return attackCount;
    }
    
    /**
     * Get the player's ping
     * @return The ping
     */
    public int getPing() {
        return ping;
    }
}