package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class OneTwentyBlockHandler {
	public static final Material PITCHER_PLANT = !Version.isAtLeast(20) ?
            Material.TALL_GRASS : Material.getMaterial("PITCHER_PLANT");
	public static final Material CHERRY_LOG = !Version.isAtLeast(20) ?
			Material.DARK_OAK_LOG : Material.getMaterial("CHERRY_LOG");
    public static final Material CHERRY_WOOD = !Version.isAtLeast(20) ?
            Material.DARK_OAK_WOOD : Material.getMaterial("CHERRY_WOOD");
    public static final Material CHERRY_LEAVES = !Version.isAtLeast(20) ?
            Material.DARK_OAK_LEAVES : Material.getMaterial("CHERRY_LEAVES");
    public static final Material CHERRY_SAPLING = !Version.isAtLeast(20) ?
            Material.DARK_OAK_SAPLING : Material.getMaterial("CHERRY_SAPLING");
    public static final Material PINK_PETALS = !Version.isAtLeast(20) ?
            Material.AIR : Material.getMaterial("PINK_PETALS");
    public static final Material SUSPICIOUS_SAND = !Version.isAtLeast(20) ?
            Material.SAND : Material.getMaterial("SUSPICIOUS_SAND");
    public static final Material SUSPICIOUS_GRAVEL = !Version.isAtLeast(20) ?
            Material.GRAVEL : Material.getMaterial("SUSPICIOUS_GRAVEL");

    public static final EntityType CAMEL = getCamel();

    public static BlockData getPinkPetalData(int count)
    {
        return Bukkit.createBlockData("pink_petals[flower_amount=" + count + ",facing=" + BlockUtils.getDirectBlockFace(new Random()).toString().toLowerCase() +  "]");
    }

    private static EntityType getCamel(){
        try{
            return EntityType.valueOf("CAMEL");
        }catch(Exception e){ return null; }
    }
}