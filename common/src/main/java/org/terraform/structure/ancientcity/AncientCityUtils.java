package org.terraform.structure.ancientcity;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class AncientCityUtils {
	
	public static final Material[] deepslateBricks = new Material[] {
			OneOneSevenBlockHandler.DEEPSLATE_BRICKS,
			OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS
	};

	public static final Material[] deepslateTiles = new Material[] {
			OneOneSevenBlockHandler.DEEPSLATE_TILES,
			OneOneSevenBlockHandler.CRACKED_DEEPSLATE_TILES
	};
	
    public static void placeSupportPillar(SimpleBlock w) {
    	Random dud = new Random();
    	//w.getUp().lsetType(Material.GRAY_WOOL);
    	w.downUntilSolid(dud, OneOneSevenBlockHandler.DEEPSLATE_BRICKS);
    	for(BlockFace face:BlockUtils.directBlockFaces)
        	w.getRelative(face).downUntilSolid(dud, OneOneSevenBlockHandler.DEEPSLATE_BRICKS);
    	
    	for(BlockFace face:BlockUtils.xzDiagonalPlaneBlockFaces)
    	{
        	int height = w.getRelative(face).downUntilSolid(dud, OneOneSevenBlockHandler.COBBLED_DEEPSLATE_WALL);
        	//w.getRelative(face).getUp().lsetType(Material.GRAY_WOOL);
        	w.getRelative(face).getDown(height-1).CorrectMultipleFacing(height);
    	}
    }
	
}
