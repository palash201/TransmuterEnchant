package io.github.palash201.transmuterenchant;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import me.drawethree.ultraprisoncore.libs.worldguardwrapper.region.IWrappedRegion;
import me.drawethree.ultraprisoncore.libs.worldguardwrapper.selection.ICuboidSelection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Class name of your custom enchant. When creating custom enchants please make sure that your class extends 'UltraPrisonEnchantment'
 */
public class Transmuter extends UltraPrisonEnchantment {

    /**
     * Class attribute chance - Represent the chance of triggering the enchant
     */
    private double chance;
    private int minRadius;
    private int maxRadius;
    private int maxLevel;
    private Material transmuteTo;

    /**
     * Constructor of your custom enchant. You can load any parameters after calling super() constructors. Make sure to include unique id!
     * Please make sure that enchants.yml config in UltraPrisonCore (not in this plugin) has this enchant properties in config!
     */
    public Transmuter() {
        super(UltraPrisonEnchants.getInstance(), 24);
        this.reload();
    }

    private Material getBlockOrStone(String materialName) {
        Material m = Material.matchMaterial(materialName);
        if (m!=null && m.isBlock()) {
            return m;
        }
        return Material.STONE;
    }

    /**
     * Overridden method what should be done when player equips pickaxe with this enchant
     *
     * @param p       Player who equipped
     * @param pickAxe ItemStack of pickaxe that is held in hand
     * @param level   level of enchant
     */
    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    /**
     * Overridden method what should be done when player un-equips pickaxe with this enchant
     *
     * @param p       Player who equipped
     * @param pickAxe ItemStack of pickaxe that is held in hand
     * @param level   level of enchant
     */
    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }


    /**
     * Overridden method what should be done when player breaks a block in mine with this enchant
     *
     * @param e            BlockBreakEvent
     * @param enchantLevel level of enchant
     */
    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (this.chance * (double) enchantLevel >= ThreadLocalRandom.current().nextDouble(100.0D)) {
            Block brokenBlock = e.getBlock();
            IWrappedRegion mineRegion = RegionUtils.getMineRegionWithHighestPriority(brokenBlock.getLocation());
            if (mineRegion != null) {
                ICuboidSelection mineRegionSelection = (ICuboidSelection) mineRegion.getSelection();
                Location minLoc = mineRegionSelection.getMinimumPoint();
                Location maxLoc = mineRegionSelection.getMaximumPoint();
                int cubeRadius = calculateRadius(minRadius, maxRadius, enchantLevel, maxLevel);
                int centerX = brokenBlock.getX();
                int centerY = brokenBlock.getY();
                int centerZ = brokenBlock.getZ();
                int minX;
                int maxX;
                int minY;
                int maxY;
                int minZ;
                int maxZ;
                if (cubeRadius*2+1 >= maxLoc.getBlockX() - minLoc.getBlockX()) {
                    minX = minLoc.getBlockX();
                    maxX = maxLoc.getBlockX();
                } else {
                    if ((centerX - minLoc.getBlockX()) < cubeRadius) {
                        centerX += cubeRadius - (centerX-minLoc.getBlockX());
                    }
                    if ((maxLoc.getBlockX() - centerX) < cubeRadius) {
                        centerX -= cubeRadius - (maxLoc.getBlockX() - centerX);
                    }
                    minX = centerX-cubeRadius;
                    maxX = centerX+cubeRadius;
                }
                if (cubeRadius*2+1 >= maxLoc.getBlockY() - minLoc.getBlockY()) {
                    minY = minLoc.getBlockY();
                    maxY = maxLoc.getBlockY();
                } else {
                    if ((centerY - minLoc.getBlockY()) < cubeRadius) {
                        centerY += cubeRadius - (centerY-minLoc.getBlockY());
                    }
                    if ((maxLoc.getBlockY() - centerY) < cubeRadius) {
                        centerY -= cubeRadius - (maxLoc.getBlockY() - centerY);
                    }
                    minY = centerY-cubeRadius;
                    maxY = centerY+cubeRadius;
                }
                if (cubeRadius*2+1 >= maxLoc.getBlockZ() - minLoc.getBlockZ()) {
                    minZ = minLoc.getBlockZ();
                    maxZ = maxLoc.getBlockZ();
                } else {
                    if ((centerZ - minLoc.getBlockZ()) < cubeRadius) {
                        centerZ += cubeRadius - (centerZ-minLoc.getBlockZ());
                    }
                    if ((maxLoc.getBlockZ() - centerZ) < cubeRadius) {
                        centerZ -= cubeRadius - (maxLoc.getBlockZ() - centerZ);
                    }
                    minZ = centerZ-cubeRadius;
                    maxZ = centerZ+cubeRadius;
                }

                for (int posX = minX; posX <= maxX; posX++) {
                    for (int posY = minY; posY <= maxY; posY++) {
                        for (int posZ = minZ; posZ <= maxZ; posZ++) {
                            Block currentBlock = brokenBlock.getWorld().getBlockAt(posX, posY, posZ);
                            if (mineRegion.contains(currentBlock.getLocation())) { // foolproof check in case above code has a logical mistake
                                if (currentBlock.getType() != Material.AIR) { // only non-air blocks should be transmuted
                                    currentBlock.setType(transmuteTo);
                                }
                            } else { // error message
                                Bukkit.getLogger().log(Level.SEVERE, "TRANSMUTER ATTEMPTED TO EDIT BLOCK THAT WAS NOT IN MINE REGION! THIS IS NOT INTENDED BEHAVIOR!");
                            }
                        }
                    }
                }
            }
        }
    }

    private int calculateRadius(int minRadius, int maxRadius, int enchantLevel, int maxLevel) {
        double portionOfMax = enchantLevel / (double) maxLevel;
        int difference = maxRadius - minRadius;
        return (int) (minRadius + Math.floor(difference * portionOfMax));
    }


    @Override
    public void reload() {
        this.chance = this.plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.minRadius = this.plugin.getConfig().get().getInt("enchants." + id + ".Min-Radius");
        this.maxRadius = this.plugin.getConfig().get().getInt("enchants." + id + ".Max-Radius");
        this.maxLevel = this.plugin.getConfig().get().getInt("enchants." + id + ".Max");
        String materialName = this.plugin.getConfig().get().getString("enchants." + id + ".Transmute-To");
        this.transmuteTo = getBlockOrStone(materialName);
    }

    /**
     * Overridden method to get author who created this enchant.
     *
     * @return String - name of author
     */
    @Override
    public String getAuthor() {
        return "palash201 / vcvv";
    }
}