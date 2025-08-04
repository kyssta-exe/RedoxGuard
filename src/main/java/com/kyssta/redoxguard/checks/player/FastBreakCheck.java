package com.kyssta.redoxguard.checks.player;

import com.kyssta.redoxguard.RedoxGuard;
import com.kyssta.redoxguard.checks.Check;
import com.kyssta.redoxguard.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Checks if a player is breaking blocks too quickly
 */
public class FastBreakCheck extends Check {

    private final Map<UUID, Long> lastBreakTime = new HashMap<>();
    private final Map<UUID, Block> lastBrokenBlock = new HashMap<>();
    
    public FastBreakCheck(RedoxGuard plugin) {
        super(plugin, "FastBreak", "player");
    }
    
    /**
     * Check if a player is breaking blocks too quickly
     * @param player The player
     * @param block The block being broken
     */
    public void checkBlockBreak(Player player, Block block) {
        if (!isEnabled() || player.hasPermission("redoxguard.bypass")) {
            return;
        }
        
        // Skip check for creative mode players if configured
        if (player.getGameMode() == GameMode.CREATIVE && 
                plugin.getConfigManager().getConfig().getBoolean("exemptions.creative-mode", false)) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // If this is the first block broken, just record it
        if (!lastBreakTime.containsKey(uuid)) {
            lastBreakTime.put(uuid, currentTime);
            lastBrokenBlock.put(uuid, block);
            return;
        }
        
        long lastTime = lastBreakTime.get(uuid);
        Block lastBlock = lastBrokenBlock.get(uuid);
        
        // Update for next check
        lastBreakTime.put(uuid, currentTime);
        lastBrokenBlock.put(uuid, block);
        
        // Calculate minimum time needed to break this block
        long minBreakTime = calculateMinBreakTime(player, block);
        
        // Apply ping compensation
        PlayerData data = getPlayerData(player);
        int ping = data.getPing();
        long pingCompensation = Math.min(ping, 300); // Cap at 300ms
        
        // If the time between breaks is too short
        if (currentTime - lastTime < minBreakTime - pingCompensation) {
            // Don't flag if the player is breaking the same block (client-server desync)
            if (block.getLocation().equals(lastBlock.getLocation())) {
                return;
            }
            
            flag(player, "broke blocks too quickly (" + (currentTime - lastTime) + "ms, min: " + minBreakTime + "ms)");
            debug(player.getName() + " broke blocks too quickly: " + (currentTime - lastTime) + "ms, min: " + minBreakTime + "ms");
        }
    }
    
    /**
     * Calculate the minimum time needed to break a block
     * @param player The player
     * @param block The block
     * @return The minimum time in milliseconds
     */
    private long calculateMinBreakTime(Player player, Block block) {
        // Base minimum time (in ms) - can be configured
        long baseMinTime = plugin.getConfigManager().getCheckConfig("player")
                .getLong("fastbreak.base-min-time", 150);
        
        // If the block is instantly breakable, use a lower threshold
        if (isInstantBreak(block.getType())) {
            return baseMinTime;
        }
        
        // Check the player's tool and apply efficiency enchantment
        ItemStack tool = player.getInventory().getItemInMainHand();
                    int efficiencyLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
        
        // Calculate break time based on block hardness and tool
        float hardness = block.getType().getHardness();
        if (hardness <= 0) {
            return baseMinTime; // Instant break or unbreakable
        }
        
        // Very basic calculation - can be improved with more accurate Minecraft mechanics
        long breakTime = (long) (hardness * 1500); // Convert hardness to milliseconds
        
        // Reduce time based on efficiency level (simplified)
        if (efficiencyLevel > 0 && isCorrectToolForBlock(tool, block)) {
            breakTime = (long) (breakTime / (1 + (efficiencyLevel * 0.25)));
        }
        
        // Apply haste/mining fatigue effects (simplified)
        // TODO: Implement potion effect checks
        
        return Math.max(breakTime, baseMinTime);
    }
    
    /**
     * Check if a block type can be broken instantly
     * @param material The block material
     * @return True if the block can be broken instantly
     */
    private boolean isInstantBreak(Material material) {
        boolean isBasicFlower = material == Material.TORCH || 
               material == Material.REDSTONE_TORCH || 
               material == Material.REDSTONE_WALL_TORCH || 
               material == Material.WALL_TORCH || 
                               material == Material.GRASS_BLOCK || 
               material == Material.TALL_GRASS || 
               material == Material.SEAGRASS || 
               material == Material.TALL_SEAGRASS || 
               material == Material.FERN || 
               material == Material.LARGE_FERN || 
               material == Material.DEAD_BUSH || 
               material == Material.DANDELION || 
               material == Material.POPPY || 
               material == Material.BLUE_ORCHID || 
               material == Material.ALLIUM || 
               material == Material.AZURE_BLUET || 
               material == Material.RED_TULIP || 
               material == Material.ORANGE_TULIP || 
               material == Material.WHITE_TULIP || 
               material == Material.PINK_TULIP || 
               material == Material.OXEYE_DAISY || 
               material == Material.SUNFLOWER || 
               material == Material.LILAC || 
               material == Material.ROSE_BUSH || 
               material == Material.PEONY;
               
        // Check for 1.14+ flowers using version compatibility
        if (com.kyssta.redoxguard.utils.VersionCompatibility.hasCornflower() && material == Material.CORNFLOWER) {
            return true;
        }
        if (com.kyssta.redoxguard.utils.VersionCompatibility.hasLilyOfTheValley() && material == Material.LILY_OF_THE_VALLEY) {
            return true;
        }
        if (com.kyssta.redoxguard.utils.VersionCompatibility.hasWitherRose() && material == Material.WITHER_ROSE) {
            return true;
        }
        
        return isBasicFlower;
    }
    
    /**
     * Check if the tool is the correct one for the block
     * @param tool The tool
     * @param block The block
     * @return True if the tool is correct for the block
     */
    private boolean isCorrectToolForBlock(ItemStack tool, Block block) {
        // This is a simplified version - a complete implementation would check
        // all block types against their optimal tools
        Material blockType = block.getType();
        Material toolType = tool.getType();
        
        // Stone-like blocks
        if (blockType.name().contains("STONE") || 
            blockType.name().contains("COBBLESTONE") || 
            blockType.name().contains("BRICK") || 
            blockType == Material.NETHERRACK || 
            blockType == Material.BLACKSTONE) {
            return toolType.name().contains("PICKAXE");
        }
        
        // Dirt-like blocks
        if (blockType == Material.DIRT || 
            blockType == Material.GRASS_BLOCK || 
            blockType == Material.PODZOL || 
            blockType == Material.MYCELIUM || 
            blockType == Material.FARMLAND || 
            blockType == Material.SOUL_SAND || 
            blockType == Material.SOUL_SOIL || 
            blockType == Material.CLAY || 
            blockType == Material.GRAVEL) {
            return toolType.name().contains("SHOVEL");
        }
        
        // Wood-like blocks
        if (blockType.name().contains("WOOD") || 
            blockType.name().contains("LOG") || 
            blockType.name().contains("PLANKS") || 
            blockType.name().contains("FENCE") || 
            blockType == Material.BOOKSHELF || 
            blockType == Material.CHEST || 
            blockType == Material.TRAPPED_CHEST || 
            blockType == Material.BARREL || 
            blockType == Material.CRAFTING_TABLE) {
            return toolType.name().contains("AXE");
        }
        
        // Leaf-like blocks
        if (blockType.name().contains("LEAVES")) {
            return toolType.name().contains("SHEARS") || toolType.name().contains("HOE");
        }
        
        // Wool and web
        if (blockType.name().contains("WOOL") || blockType == Material.COBWEB) {
            return toolType.name().contains("SHEARS") || toolType.name().contains("SWORD");
        }
        
        return false;
    }
}