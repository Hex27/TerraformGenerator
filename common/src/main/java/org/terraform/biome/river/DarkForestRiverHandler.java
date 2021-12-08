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
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

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
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if(y >= TerraformGenerator.seaLevel) //Don't apply to dry land
                	continue;
                if (data.getBiome(x, z) != getBiome()) continue;

                //Set ground near sea level to coarse dirt
                if(y >= TerraformGenerator.seaLevel - 2) {
                	data.setType(x, y, z, Material.COARSE_DIRT);
                }else if(y >= TerraformGenerator.seaLevel - 4) {
                	if(random.nextBoolean())
                    	data.setType(x, y, z, Material.COARSE_DIRT);
                }
                
                //Don't generate kelp on non-stonelike.
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;

                // SEA GRASS/KELP
                if (GenUtils.chance(random, 1, 10)) {
                    data.setType(x, y + 1, z, Material.SEAGRASS);
                    if (random.nextBoolean() && y < TerraformGenerator.seaLevel - 2)
                    	generateKelp(x, y + 1, z, data, random);
                }

                // Generate clay
                if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
                    BlockUtils.generateClayDeposit(x, y, z, data, random);
                }
                
                if(GenUtils.chance(random, 1, 1000)) {
                	BlockUtils.replaceCircularPatch(random.nextInt(9999), 2.0f, new SimpleBlock(data,x,y,z), Material.MAGMA_BLOCK);
                }
            }
        }
    }

    private void generateKelp(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        for (int ny = y; ny < TerraformGenerator.seaLevel - GenUtils.randInt(5, 15); ny++) {
        	if(data.getType(x, ny, z) != Material.WATER)
        		break;
            data.setType(x, ny, z, Material.KELP_PLANT);
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
	                    new FractalTreeBuilder(FractalTypes.Tree.SWAMP_BOTTOM)
	                            .skipGradientCheck().build(tw, data, sLoc.getX(), treeY - 3, sLoc.getZ());
	                    new FractalTreeBuilder(FractalTypes.Tree.SWAMP_TOP)
	                    		.skipGradientCheck().build(tw, data, sLoc.getX(), treeY - 2, sLoc.getZ());
	                }
	            }
		 }
        
        for (SimpleLocation sLoc : roots) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int rootY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(rootY);
                if(!BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())))
                		continue;
                
                int minHeight = 3;
                if (sLoc.getY() < TerraformGenerator.seaLevel) {
                    minHeight = TerraformGenerator.seaLevel - sLoc.getY();
                }

                BlockUtils.spawnPillar(random, data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ(), Material.OAK_LOG, minHeight, minHeight + 3);
                
            }
        }
	}


}
