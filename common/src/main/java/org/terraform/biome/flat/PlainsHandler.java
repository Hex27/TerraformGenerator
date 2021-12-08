package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.PLAINS;
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
        //Pumpkin Patch
        if (GenUtils.chance(1, 1000)) {
            for (int i = 0; i < GenUtils.randInt(5, 10); i++) {
                int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
                if (data.getBiome(loc[0], loc[2]) != getBiome()) continue;
                SimpleBlock target = new SimpleBlock(data, loc[0], GenUtils.getHighestGround(data, loc[0], loc[2])+1, loc[2]);
                if(!target.getType().isSolid())
                	target.setType(Material.PUMPKIN);
            }
        }

        //Melon Patch
        if (GenUtils.chance(1, 1000)) {
            for (int i = 0; i < GenUtils.randInt(5, 10); i++) {
                int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
                if (data.getBiome(loc[0], loc[2]) != getBiome()) continue;
                SimpleBlock target = new SimpleBlock(data, loc[0], GenUtils.getHighestGround(data, loc[0], loc[2])+1, loc[2]);
                if(!target.getType().isSolid())
                	target.setType(Material.MELON);
            }
        }
        
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                	
                    if (GenUtils.chance(random, 1, 10)) { //Grass
                        if (GenUtils.chance(random, 6, 10)) {
                            data.setType(x, y + 1, z, Material.GRASS);
                            if (random.nextBoolean()) {
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                            }
                        } else {
                            if (GenUtils.chance(random, 7, 10))
                                data.setType(x, y + 1, z, BlockUtils.pickFlower());
                            else
                                BlockUtils.setDoublePlant(data, x, y + 1, z, BlockUtils.pickTallFlower());
                        }
                    }
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//Small trees or grass poffs
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);
        
        for (SimpleLocation sLoc : trees) {
        	if(random.nextBoolean()) { //trees
        		int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(treeY);
                if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                        BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                    new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
                }
        	}else { //Poffs
                int poffY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(poffY);
                if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                        BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                	BlockUtils.replaceSphere(
                            random.nextInt(424444),
                            2, 2, 2,
                            new SimpleBlock(data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ()), false, Material.OAK_LEAVES);
                }
            }
            
        }
		
	}
}
