package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
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

public class ScarletForestHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.FOREST;
    }
    
    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.SCARLET_FOREST;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (world.getBiomeBank(x,z) != BiomeBank.SCARLET_FOREST) continue;
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                	
                    if (GenUtils.chance(random, 1, 10)) { //Grass
                        if (GenUtils.chance(random, 6, 10)) {
                            data.setType(x, y + 1, z, Material.GRASS);
                            if (random.nextBoolean()) {
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                            }
                        } else {
                            if (GenUtils.chance(random, 7, 10))
                                data.setType(x, y + 1, z, Material.POPPY);
                            else
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.ROSE_BUSH);
                        }
                    }
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);
        
        for (SimpleLocation sLoc : trees) {
        	
    		int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            
            if(tw.getBiomeBank(sLoc.getX(),sLoc.getZ()) == BiomeBank.SCARLET_FOREST &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())))
            {
                if(TConfigOption.TREES_SCARLET_BIG_ENABLED.getBoolean())
                    new FractalTreeBuilder(FractalTypes.Tree.SCARLET_BIG).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
                else
                    new FractalTreeBuilder(FractalTypes.Tree.SCARLET_SMALL).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());

                TaigaHandler.replacePodzol(
                            tw.getHashedRand(sLoc.getX(),sLoc.getY(),sLoc.getZ()).nextInt(9999),
                            7f,
                            new SimpleBlock(data,sLoc.getX(),sLoc.getY(),sLoc.getZ()));
            }
        }
        
        SimpleLocation[] smalltrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7);
        
        for (SimpleLocation sLoc : smalltrees) {
        	
    		int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())))
            {
        		new FractalTreeBuilder(FractalTypes.Tree.SCARLET_SMALL).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
        	}
        }
	}
	
    public BiomeBank getBeachType() {
    	return BiomeBank.SCARLET_FOREST_BEACH;
    }
    
    public BiomeBank getRiverType() {
    	return BiomeBank.SCARLET_FOREST_RIVER;
    }

}
