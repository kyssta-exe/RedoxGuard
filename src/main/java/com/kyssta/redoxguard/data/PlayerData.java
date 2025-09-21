package com.kyssta.redoxguard.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerData - Comprehensive data storage for individual player anti-cheat tracking
 * 
 * <p>This class maintains all necessary data for anti-cheat checks on a per-player basis.
 * It serves as a centralized repository for:
 * <ul>
 *   <li>Violation tracking and history for all check types</li>
 *   <li>Movement data including location history and ground state</li>
 *   <li>Combat statistics and attack pattern analysis</li>
 *   <li>Network latency information for lag compensation</li>
 * </ul>
 * 
 * <p>The data is automatically updated through various listeners and is used by
 * all anti-cheat checks to make informed decisions about player behavior.
 * This approach ensures consistent data access and reduces redundant calculations.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Automatic violation level management with timestamp tracking</li>
 *   <li>Real-time movement data updates for accurate detection</li>
 *   <li>Combat pattern analysis for advanced cheat detection</li>
 *   <li>Ping-based lag compensation support</li>
 * </ul>
 * 
 * @author Kyssta
 * @since 1.0.0
 */
public class PlayerData {

    /** The Bukkit player instance this data belongs to */
    private final Player player;
    
    /** Map storing violation levels for each check type */
    private final Map<String, Integer> violations = new HashMap<>();
    
    /** Map storing timestamps of last violations for each check type */
    private final Map<String, Long> lastViolationTime = new HashMap<>();
    
    // Movement tracking data
    /** The player's previous location for movement calculations */
    private Location lastLocation;
    
    /** The last location where the player was confirmed on solid ground */
    private Location lastGroundLocation;
    
    /** Timestamp of the last movement update */
    private long lastMovementTime;
    
    /** Current ground state - whether player is currently on solid ground */
    private boolean onGround;
    
    /** Previous ground state - whether player was on ground in last tick */
    private boolean wasOnGround;
    
    /** Last Y-axis movement delta for vertical movement analysis */
    private double lastDeltaY;
    
    /** Number of consecutive ticks the player has been airborne */
    private int airTicks;
    
    /** Number of consecutive ticks the player has been on ground */
    private int groundTicks;
    
    // Combat tracking data
    /** Timestamp of the player's last attack action */
    private long lastAttackTime;
    
    /** Number of attacks performed in the current time window */
    private int attackCount;
    
    /** Timestamp when the attack count was last reset */
    private long attackCountResetTime;
    
    // Network latency data
    /** Player's current ping/latency in milliseconds */
    private int ping;
    
    /** Timestamp of the last ping measurement update */
    private long lastPingUpdate;
    
    /**
     * Constructs a new PlayerData instance for the specified player.
     * 
     * <p>Initializes all tracking data with current player state and default values:
     * <ul>
     *   <li>Sets initial location data from player's current position</li>
     *   <li>Initializes ground state tracking from player's current state</li>
     *   <li>Resets all violation counters and timestamps</li>
     *   <li>Sets up combat and latency tracking with default values</li>
     * </ul>
     * 
     * @param player the Bukkit player to create data for
     */
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
     * Increments the violation level for a specific check and updates the timestamp.
     * 
     * <p>This method performs the following actions:
     * <ul>
     *   <li>Increments the current violation level by 1</li>
     *   <li>Updates the last violation timestamp to current time</li>
     *   <li>Returns the new violation level for immediate use</li>
     * </ul>
     * 
     * <p>Violation levels are used by checks to determine punishment thresholds
     * and track player behavior patterns over time.</p>
     * 
     * @param check the name of the check that detected the violation
     * @return the new violation level after incrementing
     */
    public int addViolation(String check) {
        int vl = getViolationLevel(check) + 1;
        violations.put(check, vl);
        lastViolationTime.put(check, System.currentTimeMillis());
        return vl;
    }
    
    /**
     * Retrieves the current violation level for a specific check.
     * 
     * @param check the name of the check to get violations for
     * @return the current violation level, or 0 if no violations exist
     */
    public int getViolationLevel(String check) {
        return violations.getOrDefault(check, 0);
    }
    
    /**
     * Resets the violation level for a specific check to zero.
     * 
     * <p>This is typically used when:
     * <ul>
     *   <li>A player's violations have decayed over time</li>
     *   <li>Administrative commands clear violation history</li>
     *   <li>Check-specific reset conditions are met</li>
     * </ul>
     * 
     * @param check the name of the check to reset violations for
     */
    public void resetViolations(String check) {
        violations.put(check, 0);
    }
    
    /**
     * Retrieves the timestamp of the last violation for a specific check.
     * 
     * <p>This timestamp is used for:
     * <ul>
     *   <li>Calculating time-based violation decay</li>
     *   <li>Determining punishment cooldowns</li>
     *   <li>Analyzing violation frequency patterns</li>
     * </ul>
     * 
     * @param check the name of the check to get the last violation time for
     * @return the timestamp of the last violation in milliseconds, or 0 if no violations exist
     */
    public long getLastViolationTime(String check) {
        return lastViolationTime.getOrDefault(check, 0L);
    }
    
    /**
     * Updates all movement-related tracking data with the player's new location.
     * 
     * <p>This method performs comprehensive movement analysis:
     * <ul>
     *   <li>Updates ground state tracking (current and previous)</li>
     *   <li>Calculates Y-axis movement delta for vertical motion analysis</li>
     *   <li>Updates location history for distance and speed calculations</li>
     *   <li>Manages air/ground tick counters for flight detection</li>
     *   <li>Records last ground location for teleport-back functionality</li>
     * </ul>
     * 
     * <p>This data is essential for movement-based checks like Speed and Fly detection.</p>
     * 
     * @param location the player's new location to update tracking data with
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
     * Updates combat-related tracking data when the player performs an attack.
     * 
     * <p>This method manages attack frequency analysis:
     * <ul>
     *   <li>Records the timestamp of the current attack</li>
     *   <li>Increments attack count within the current time window</li>
     *   <li>Automatically resets attack count after 1 second intervals</li>
     * </ul>
     * 
     * <p>This data is used by combat checks like KillAura and TriggerBot
     * to detect abnormal attack patterns and frequencies.</p>
     */
    public void updateCombat() {
        long now = System.currentTimeMillis();
        this.lastAttackTime = now;
        
        if (now > attackCountResetTime) {
            this.attackCount = 1;
            this.attackCountResetTime = now + 1000; // Reset attack counter after 1 second window
        } else {
            this.attackCount++;
        }
    }
    
    /**
     * Updates the player's network latency information for lag compensation.
     * 
     * <p>Ping data is crucial for:
     * <ul>
     *   <li>Adjusting check thresholds based on network conditions</li>
     *   <li>Providing fair detection for players with high latency</li>
     *   <li>Reducing false positives caused by network lag</li>
     * </ul>
     * 
     * @param ping the player's current ping/latency in milliseconds
     */
    public void updatePing(int ping) {
        this.ping = ping;
        this.lastPingUpdate = System.currentTimeMillis();
    }
    
    /**
     * Returns the Bukkit player instance this data belongs to.
     * 
     * @return the associated Player object
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Returns the player's previous location for movement calculations.
     * 
     * @return the last recorded location
     */
    public Location getLastLocation() {
        return lastLocation;
    }
    
    /**
     * Returns the last location where the player was confirmed on solid ground.
     * 
     * <p>This is used for teleport-back functionality and ground state validation.</p>
     * 
     * @return the last confirmed ground location
     */
    public Location getLastGroundLocation() {
        return lastGroundLocation;
    }
    
    /**
     * Returns the timestamp of the last movement update.
     * 
     * @return the last movement time in milliseconds
     */
    public long getLastMovementTime() {
        return lastMovementTime;
    }
    
    /**
     * Checks if the player is currently on solid ground.
     * 
     * @return {@code true} if the player is on ground, {@code false} otherwise
     */
    public boolean isOnGround() {
        return onGround;
    }
    
    /**
     * Checks if the player was on solid ground in the previous tick.
     * 
     * <p>This is essential for detecting illegal jump/flight patterns.</p>
     * 
     * @return {@code true} if the player was on ground in the previous tick, {@code false} otherwise
     */
    public boolean wasOnGround() {
        return wasOnGround;
    }
    
    /**
     * Returns the last vertical movement delta for flight detection.
     * 
     * <p>Positive values indicate upward movement, negative values indicate downward movement.</p>
     * 
     * @return the Y-axis movement delta in blocks
     */
    public double getLastDeltaY() {
        return lastDeltaY;
    }
    
    /**
     * Returns the number of consecutive ticks the player has been airborne.
     * 
     * <p>This counter is used by flight detection checks to identify
     * players who remain in the air for suspiciously long periods.</p>
     * 
     * @return the number of consecutive air ticks
     */
    public int getAirTicks() {
        return airTicks;
    }
    
    /**
     * Returns the number of consecutive ticks the player has been on solid ground.
     * 
     * <p>This counter helps validate ground state and detect rapid
     * ground-to-air transitions that may indicate cheating.</p>
     * 
     * @return the number of consecutive ground ticks
     */
    public int getGroundTicks() {
        return groundTicks;
    }
    
    /**
     * Returns the timestamp of the player's most recent attack action.
     * 
     * <p>This is used for combat pattern analysis and cooldown calculations.</p>
     * 
     * @return the timestamp of the last attack in milliseconds
     */
    public long getLastAttackTime() {
        return lastAttackTime;
    }
    
    /**
     * Returns the number of attacks performed within the current time window.
     * 
     * <p>The attack count automatically resets every second and is used
     * to detect abnormally high attack frequencies that may indicate automation.</p>
     * 
     * @return the current attack count within the time window
     */
    public int getAttackCount() {
        return attackCount;
    }
    
    /**
     * Returns the player's current network latency (ping) in milliseconds.
     * 
     * <p>This value is used throughout the anti-cheat system for lag compensation,
     * ensuring fair detection regardless of network conditions.</p>
     * 
     * @return the player's ping in milliseconds
     */
    public int getPing() {
        return ping;
    }
}