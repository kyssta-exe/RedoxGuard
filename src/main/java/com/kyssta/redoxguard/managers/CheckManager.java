package com.kyssta.redoxguard.managers;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.checks.combat.AutoAnchorCheck;
import com.kyssta.redoxguard.checks.combat.AutoCrystalCheck;
import com.kyssta.redoxguard.checks.combat.CrystalAuraCheck;
import com.kyssta.redoxguard.checks.combat.HitboxCheck;
import com.kyssta.redoxguard.checks.combat.KillAuraCheck;
import com.kyssta.redoxguard.checks.combat.ReachCheck;
import com.kyssta.redoxguard.checks.combat.TriggerBotCheck;
import com.kyssta.redoxguard.checks.movement.FlyCheck;
import com.kyssta.redoxguard.checks.movement.SpeedCheck;
import com.kyssta.redoxguard.checks.player.AutoTotemCheck;
import com.kyssta.redoxguard.checks.player.FastBreakCheck;
import com.kyssta.redoxguard.checks.player.FastPlaceCheck;
import com.kyssta.redoxguard.checks.player.InventoryCheck;
import com.kyssta.redoxguard.checks.player.SimulationCheck;
import com.kyssta.redoxguard.data.PlayerData;
import com.kyssta.redoxguard.utils.LogUtil;
import com.kyssta.redoxguard.utils.VersionCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * CheckManager - Central management system for all anti-cheat checks
 * 
 * <p>The CheckManager is responsible for registering, organizing, and managing all
 * anti-cheat checks within RedoxGuard. It provides a centralized system for:
 * <ul>
 *   <li>Registering movement, combat, and player behavior checks</li>
 *   <li>Version-specific check compatibility handling</li>
 *   <li>Check retrieval and filtering by type or name</li>
 *   <li>Violation processing and notification handling</li>
 * </ul>
 * 
 * <p>The manager automatically registers all available checks during initialization
 * and handles version compatibility to ensure checks only run on supported Minecraft versions.</p>
 * 
 * @author Kyssta
 * @since 1.0.0
 */
public class CheckManager {

    /** Reference to the main RedoxGuard plugin instance */
    private final RedoxGuard plugin;
    
    /** List of all registered anti-cheat checks */
    private final List<Check> checks = new ArrayList<>();
    
    /**
     * Constructs a new CheckManager and automatically registers all available checks.
     * 
     * @param plugin the RedoxGuard plugin instance
     */
    public CheckManager(RedoxGuard plugin) {
        this.plugin = plugin;
        registerChecks();
    }
    
    /**
     * Registers all available anti-cheat checks with version compatibility handling.
     * 
     * <p>This method initializes and registers all checks organized by category:
     * <ul>
     *   <li><b>Movement Checks:</b> Speed, Fly detection</li>
     *   <li><b>Combat Checks:</b> Reach, KillAura, Hitbox, AutoCrystal, CrystalAura, TriggerBot</li>
     *   <li><b>Player Checks:</b> Inventory, FastBreak, FastPlace, AutoTotem, Simulation</li>
     * </ul>
     * 
     * <p>Version-specific checks (like AutoAnchorCheck) are only registered if the
     * current Minecraft version supports the required features.</p>
     */
    private void registerChecks() {
        // Movement-based violation detection
        registerCheck(new SpeedCheck(plugin));
        registerCheck(new FlyCheck(plugin));
        
        // Combat-based violation detection
        registerCheck(new ReachCheck(plugin));
        registerCheck(new KillAuraCheck(plugin));
        registerCheck(new HitboxCheck(plugin));
        registerCheck(new AutoCrystalCheck(plugin));
        
        // Version-specific check: AutoAnchor (requires MC 1.16+ for RESPAWN_ANCHOR)
        if (VersionCompatibility.hasRespawnAnchor()) {
            registerCheck(new AutoAnchorCheck(plugin));
        } else {
            LogUtil.info("AutoAnchorCheck not registered as RESPAWN_ANCHOR is not available in this Minecraft version");
        }
        
        registerCheck(new CrystalAuraCheck(plugin));
        registerCheck(new TriggerBotCheck(plugin));
        
        // Player behavior and interaction checks
        registerCheck(new InventoryCheck(plugin));
        registerCheck(new FastBreakCheck(plugin));
        registerCheck(new FastPlaceCheck(plugin));
        registerCheck(new AutoTotemCheck(plugin));
        registerCheck(new SimulationCheck(plugin));
        
        LogUtil.info("Registered " + checks.size() + " checks");
    }
    
    /**
     * Registers a single check and adds it to the active checks list.
     * 
     * @param check the Check instance to register
     */
    private void registerCheck(Check check) {
        checks.add(check);
        LogUtil.debug("Registered check: " + check.getName());
    }
    
    /**
     * Retrieves all currently registered anti-cheat checks.
     * 
     * @return an unmodifiable view of all registered checks
     */
    public List<Check> getChecks() {
        return checks;
    }
    
    /**
     * Retrieves all checks that match the specified type category.
     * 
     * <p>Common check types include:
     * <ul>
     *   <li>"movement" - Speed, Fly checks</li>
     *   <li>"combat" - Reach, KillAura, AutoCrystal checks</li>
     *   <li>"player" - Inventory, FastBreak, FastPlace checks</li>
     * </ul>
     * 
     * @param type the check type to filter by (case-insensitive)
     * @return a new list containing all checks of the specified type
     */
    public List<Check> getChecksOfType(String type) {
        List<Check> result = new ArrayList<>();
        for (Check check : checks) {
            if (check.getType().equalsIgnoreCase(type)) {
                result.add(check);
            }
        }
        return result;
    }
    
    /**
     * Retrieves a specific check by its unique name.
     * 
     * @param name the name of the check to find (case-insensitive)
     * @return the Check instance if found, or {@code null} if no check with the given name exists
     */
    public Check getCheckByName(String name) {
        for (Check check : checks) {
            if (check.getName().equalsIgnoreCase(name)) {
                return check;
            }
        }
        return null;
    }
    
    /**
     * Processes a detected violation and handles the appropriate response.
     * 
     * <p>This method performs the following actions:
     * <ol>
     *   <li>Increments the player's violation count for the specific check</li>
     *   <li>Logs the violation with details for administrative review</li>
     *   <li>Evaluates if punishment is warranted based on violation thresholds</li>
     *   <li>Executes configured punishment commands if thresholds are exceeded</li>
     *   <li>Resets violation counts after punishment to prevent spam</li>
     * </ol>
     * 
     * <p><b>Permission Bypass:</b> Players with {@code redoxguard.bypass} permission
     * will not receive punishments, but violations are still logged for monitoring.</p>
     * 
     * @param player the Player who triggered the violation
     * @param check the Check that detected the violation
     * @param details additional context information about the violation (e.g., values, calculations)
     */
    public void handleViolation(Player player, Check check, String details) {
        PlayerData data = plugin.getPlayerDataManager().getOrCreatePlayerData(player);
        int vl = data.addViolation(check.getName());
        
        // Record violation for administrative review and debugging
        LogUtil.logViolation(player.getName(), check.getName(), details, vl);
        
        // Evaluate punishment threshold based on check configuration
        int maxVl = plugin.getConfigManager().getCheckConfig(check.getType())
                .getMaxViolations(check.getName().toLowerCase());
        
        if (vl >= maxVl && !player.hasPermission("redoxguard.bypass")) {
            // Retrieve and prepare punishment command with player substitution
            String command = plugin.getConfigManager().getCheckConfig(check.getType())
                    .getPunishmentCommand(check.getName().toLowerCase())
                    .replace("%player%", player.getName());
            
            // Execute punishment command on main thread for thread safety
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            
            // Reset violation count to prevent immediate re-punishment
            data.resetViolations(check.getName());
        }
    }
}