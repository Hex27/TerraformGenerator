package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneNineBlockHandler;

import java.util.Random;

public class DarkForestRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.RIVER;
    }
    
    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.DIRT, Material.PODZOL,
        		Material.STONE,
        		Material.STONE};
    }


    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {

        if(surfaceY >= TerraformGenerator.seaLevel) //Don't apply to dry land
            return;

        //Set ground near sea level to coarse dirt
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.COARSE_DIRT);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.COARSE_DIRT);
        }

        //Don't generate kelp on non-stonelike.
        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;

        // SEA GRASS/KELP
        RiverHandler.riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        // Generate clay
        if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }

        if(GenUtils.chance(random, 1, 1000)) {
            BlockUtils.replaceCircularPatch(random.nextInt(9999), 2.0f, new SimpleBlock(data,rawX,surfaceY,rawZ), Material.MAGMA_BLOCK);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		SimpleLocation[] roots = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7, 0.6f);
        SimpleLocation[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 24);
        for (SimpleLocation sLoc : bigTrees) {
	            int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
	            sLoc.setY(treeY);
	            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
	                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
	                treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
	                
	                if(treeY < TerraformGenerator.seaLevel) {
	                	 //Don't do gradient checks for swamp trees, the mud is uneven.
	                	//just make sure it's submerged
                        TreeDB.spawnBreathingRoots(tw, new SimpleBlock(data,sLoc), OneOneNineBlockHandler.MANGROVE_ROOTS);
                        FractalTypes.Tree.SWAMP_TOP.build(tw, new SimpleBlock(data,sLoc), (t)->t.setCheckGradient(false));
	                }
	            }
		 }
	}


}
