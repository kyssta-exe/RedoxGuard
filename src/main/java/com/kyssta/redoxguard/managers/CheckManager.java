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

public class CheckManager {

    private final RedoxGuard plugin;
    private final List<Check> checks = new ArrayList<>();
    
    public CheckManager(RedoxGuard plugin) {
        this.plugin = plugin;
        registerChecks();
    }
    
    /**
     * Register all checks
     */
    private void registerChecks() {
        // Movement checks
        registerCheck(new SpeedCheck(plugin));
        registerCheck(new FlyCheck(plugin));
        
        // Combat checks
        registerCheck(new ReachCheck(plugin));
        registerCheck(new KillAuraCheck(plugin));
        registerCheck(new HitboxCheck(plugin));
        registerCheck(new AutoCrystalCheck(plugin));
        
        // Only register AutoAnchorCheck if RESPAWN_ANCHOR exists in this version (added in 1.16)
        if (VersionCompatibility.hasRespawnAnchor()) {
            registerCheck(new AutoAnchorCheck(plugin));
        } else {
            LogUtil.info("AutoAnchorCheck not registered as RESPAWN_ANCHOR is not available in this Minecraft version");
        }
        
        registerCheck(new CrystalAuraCheck(plugin));
        registerCheck(new TriggerBotCheck(plugin));
        
        // Player checks
        registerCheck(new InventoryCheck(plugin));
        registerCheck(new FastBreakCheck(plugin));
        registerCheck(new FastPlaceCheck(plugin));
        registerCheck(new AutoTotemCheck(plugin));
        registerCheck(new SimulationCheck(plugin));
        
        LogUtil.info("Registered " + checks.size() + " checks");
    }
    
    /**
     * Register a check
     * @param check The check to register
     */
    private void registerCheck(Check check) {
        checks.add(check);
        LogUtil.debug("Registered check: " + check.getName());
    }
    
    /**
     * Get all registered checks
     * @return The list of checks
     */
    public List<Check> getChecks() {
        return checks;
    }
    
    /**
     * Get checks of a specific type
     * @param type The type of check
     * @return The list of checks of the specified type
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
     * Get a check by name
     * @param name The name of the check
     * @return The check, or null if it doesn't exist
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
     * Handle a violation
     * @param player The player
     * @param check The check that was violated
     * @param details Additional details about the violation
     */
    public void handleViolation(Player player, Check check, String details) {
        PlayerData data = plugin.getPlayerDataManager().getOrCreatePlayerData(player);
        int vl = data.addViolation(check.getName());
        
        // Log the violation
        LogUtil.logViolation(player.getName(), check.getName(), details, vl);
        
        // Check if punishment is needed
        int maxVl = plugin.getConfigManager().getCheckConfig(check.getType())
                .getMaxViolations(check.getName().toLowerCase());
        
        if (vl >= maxVl && !player.hasPermission("redoxguard.bypass")) {
            // Get punishment command
            String command = plugin.getConfigManager().getCheckConfig(check.getType())
                    .getPunishmentCommand(check.getName().toLowerCase())
                    .replace("%player%", player.getName());
            
            // Execute punishment
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            
            // Reset violations after punishment
            data.resetViolations(check.getName());
        }
    }
}