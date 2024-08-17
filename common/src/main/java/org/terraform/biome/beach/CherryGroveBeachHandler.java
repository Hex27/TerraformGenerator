package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CherryGroveBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BEACH;
    }

    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.CHERRY_GROVE;
    }
    
    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {

        boolean hasSugarcane = GenUtils.chance(random, 1, 100);

        Material base = data.getType(rawX,surfaceY,rawZ);

        //Remove submerged grass
        if(base == Material.GRASS_BLOCK && data.getType(rawX, surfaceY+1, rawZ) == Material.WATER)
            data.setType(rawX, surfaceY, rawZ,Material.DIRT);

        if (base != Material.SAND && base != Material.GRASS_BLOCK) return;

        surfaceY++;

        //Spawn sugarcane
        if (hasSugarcane) {
            boolean hasWater = false;
            if (data.getType(rawX + 1, surfaceY - 1, rawZ) == Material.WATER)
                hasWater = true;
            if (data.getType(rawX - 1, surfaceY - 1, rawZ) == Material.WATER)
                hasWater = true;
            if (data.getType(rawX, surfaceY - 1, rawZ + 1) == Material.WATER)
                hasWater = true;
            if (data.getType(rawX, surfaceY - 1, rawZ - 1) == Material.WATER)
                hasWater = true;

            if (hasWater) BlockUtils.spawnPillar(random, data, rawX, surfaceY, rawZ, Material.SUGAR_CANE, 3, 7);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        
//		SimpleLocation[] coconutTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);
//
//		// Big trees and giant mushrooms
//        for (SimpleLocation sLoc : coconutTrees) {
//            int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
//            sLoc.setY(treeY);
//            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() &&
//                    (BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))
//                    || data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) == Material.SAND)) {
//                TreeDB.spawnCoconutTree(tw, data, sLoc.getX(), sLoc.getY()+1 ,sLoc.getZ());
//            }
//        }
		
	}
}
