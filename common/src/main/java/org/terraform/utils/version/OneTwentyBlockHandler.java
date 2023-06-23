package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class OneTwentyBlockHandler {
	
	public static final Material CHERRY_LOG = Material.getMaterial("CHERRY_LOG") == null ?
			Material.DARK_OAK_LOG : Material.getMaterial("CHERRY_LOG");
    public static final Material CHERRY_WOOD = Material.getMaterial("CHERRY_WOOD") == null ?
            Material.DARK_OAK_WOOD : Material.getMaterial("CHERRY_WOOD");
    public static final Material CHERRY_LEAVES = Material.getMaterial("CHERRY_LEAVES") == null ?
            Material.DARK_OAK_LEAVES : Material.getMaterial("CHERRY_LEAVES");
    public static final Material PINK_PETALS = Material.getMaterial("PINK_PETALS") == null ?
            Material.AIR : Material.getMaterial("PINK_PETALS");
    public static final Material SUSPICIOUS_SAND = Material.getMaterial("SUSPICIOUS_SAND") == null ?
            Material.SAND : Material.getMaterial("SUSPICIOUS_SAND");
    public static final Material SUSPICIOUS_GRAVEL = Material.getMaterial("SUSPICIOUS_GRAVEL") == null ?
            Material.GRAVEL : Material.getMaterial("SUSPICIOUS_GRAVEL");

    public static BlockData getPinkPetalData(int count)
    {
        return Bukkit.createBlockData("pink_petals[flower_amount=" + count + ",facing=" + BlockUtils.getDirectBlockFace(new Random()).toString().toLowerCase() +  "]");
    }
}