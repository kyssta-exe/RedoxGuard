package com.kyssta.redoxguard.checks.combat;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced triggerbot detection inspired by TotemGuard
 * Detects sophisticated triggerbot cheats with multiple detection methods
 */
public class AdvancedTriggerBotCheck extends Check {

    private final Map<UUID, TriggerBotData> playerData = new HashMap<>();

    public AdvancedTriggerBotCheck(RedoxGuard plugin) {
        super(plugin, "AdvancedTriggerBot", "combat");
    }

    /**
     * Check for triggerbot usage
     * @param player The player
     * @param target The entity being attacked
     */
    public void checkTriggerBot(Player player, Entity target) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }

        // Skip if target is not a player (triggerbot is mainly for PvP)
        if (!(target instanceof Player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        TriggerBotData data = playerData.computeIfAbsent(uuid, k -> new TriggerBotData());

        // Update data
        data.updateAttack();

        // Multiple detection methods
        checkReactionTime(player, data);
        checkAimPrecision(player, target, data);
        checkAttackPattern(player, data);
        checkTargetTracking(player, target, data);
    }

    /**
     * Check reaction time (most reliable triggerbot indicator)
     */
    private void checkReactionTime(Player player, TriggerBotData data) {
        long reactionTime = data.getLastReactionTime();
        
        // Triggerbot typically has reaction times under 50ms
        // Normal human reaction time is 150-300ms
        if (reactionTime < 50) {
            data.incrementSuspiciousReactions();
            
            if (data.getSuspiciousReactions() >= 3) {
                flag(player, "suspiciously fast reaction time (" + reactionTime + "ms)");
                debug(player.getName() + " had suspicious reaction time: " + reactionTime + "ms");
            }
        } else {
            data.resetSuspiciousReactions();
        }
    }

    /**
     * Check aim precision (triggerbot users have perfect aim)
     */
    private void checkAimPrecision(Player player, Entity target, TriggerBotData data) {
        Location playerLoc = player.getEyeLocation();
        Vector playerDir = playerLoc.getDirection();
        
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0);
        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();
        
        double angle = Math.toDegrees(Math.acos(playerDir.dot(toTarget)));
        
        // Triggerbot users typically have angles under 1 degree
        if (angle < 1.0) {
            data.incrementPerfectAims();
            
            if (data.getPerfectAims() >= 5) {
                flag(player, "suspiciously perfect aim (angle: " + String.format("%.2f", angle) + "°)");
                debug(player.getName() + " had perfect aim: " + String.format("%.2f", angle) + "°");
            }
        } else {
            data.resetPerfectAims();
        }
    }

    /**
     * Check attack pattern (triggerbot has consistent timing)
     */
    private void checkAttackPattern(Player player, TriggerBotData data) {
        long[] recentAttacks = data.getRecentAttacks();
        
        if (recentAttacks.length >= 3) {
            // Check for suspiciously consistent timing
            long variance = calculateVariance(recentAttacks);
            
            // Triggerbot has very low variance (consistent timing)
            if (variance < 1000) { // Less than 1ms variance
                data.incrementConsistentPatterns();
                
                if (data.getConsistentPatterns() >= 3) {
                    flag(player, "suspiciously consistent attack pattern (variance: " + variance + "ms)");
                    debug(player.getName() + " had consistent attack pattern: " + variance + "ms variance");
                }
            } else {
                data.resetConsistentPatterns();
            }
        }
    }

    /**
     * Check target tracking (triggerbot tracks targets perfectly)
     */
    private void checkTargetTracking(Player player, Entity target, TriggerBotData data) {
        Location targetLoc = target.getLocation();
        Location playerLoc = player.getLocation();
        
        double distance = playerLoc.distance(targetLoc);
        
        // Track how often player attacks at optimal distance
        if (distance >= 2.5 && distance <= 3.5) { // Optimal PvP distance
            data.incrementOptimalDistanceAttacks();
            
            if (data.getOptimalDistanceAttacks() >= 10) {
                double ratio = (double) data.getOptimalDistanceAttacks() / data.getTotalAttacks();
                
                // If more than 80% of attacks are at optimal distance, suspicious
                if (ratio > 0.8) {
                    flag(player, "suspicious target tracking (optimal distance ratio: " + String.format("%.2f", ratio) + ")");
                    debug(player.getName() + " had suspicious target tracking: " + String.format("%.2f", ratio) + " ratio");
                }
            }
        }
    }

    /**
     * Calculate variance of attack timings
     */
    private long calculateVariance(long[] attacks) {
        if (attacks.length < 2) return 0;
        
        long sum = 0;
        for (long attack : attacks) {
            sum += attack;
        }
        long mean = sum / attacks.length;
        
        long variance = 0;
        for (long attack : attacks) {
            variance += (attack - mean) * (attack - mean);
        }
        
        return variance / attacks.length;
    }

    /**
     * TriggerBot data for each player
     */
    private static class TriggerBotData {
        private long lastAttackTime;
        private long lastReactionTime;
        private int suspiciousReactions;
        private int perfectAims;
        private int consistentPatterns;
        private int optimalDistanceAttacks;
        private int totalAttacks;
        private final long[] recentAttacks = new long[10];
        private int attackIndex = 0;

        public void updateAttack() {
            long currentTime = System.currentTimeMillis();
            
            if (lastAttackTime > 0) {
                lastReactionTime = currentTime - lastAttackTime;
            }
            
            lastAttackTime = currentTime;
            totalAttacks++;
            
            // Update recent attacks array
            recentAttacks[attackIndex] = lastReactionTime;
            attackIndex = (attackIndex + 1) % recentAttacks.length;
        }

        public void incrementSuspiciousReactions() { suspiciousReactions++; }
        public void resetSuspiciousReactions() { suspiciousReactions = 0; }
        public int getSuspiciousReactions() { return suspiciousReactions; }

        public void incrementPerfectAims() { perfectAims++; }
        public void resetPerfectAims() { perfectAims = 0; }
        public int getPerfectAims() { return perfectAims; }

        public void incrementConsistentPatterns() { consistentPatterns++; }
        public void resetConsistentPatterns() { consistentPatterns = 0; }
        public int getConsistentPatterns() { return consistentPatterns; }

        public void incrementOptimalDistanceAttacks() { optimalDistanceAttacks++; }
        public int getOptimalDistanceAttacks() { return optimalDistanceAttacks; }

        public long getLastReactionTime() { return lastReactionTime; }
        public long[] getRecentAttacks() { return recentAttacks; }
        public int getTotalAttacks() { return totalAttacks; }
    }
} 