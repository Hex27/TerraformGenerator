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
import org.terraform.data.Wall;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.OneTwentyBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Random;

public class CherryGroveHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.PLAINS;
    }
    
    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.CHERRY_GROVE;
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
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {

            if (GenUtils.chance(random, 2, 10)) { //Grass
                if (GenUtils.chance(random, 8, 10)) {
                    //Pink petals. No longer generate tall grass.
                    if (Version.isAtLeast(20) && GenUtils.chance(random, 6, 10)) {
                        data.setBlockData(rawX,surfaceY+1,rawZ, OneTwentyBlockHandler.getPinkPetalData(GenUtils.randInt(1,4)));
                    }else
                        data.setType(rawX, surfaceY + 1, rawZ, Material.GRASS);
                } else {
                    if (GenUtils.chance(random, 7, 10))
                        data.setType(rawX, surfaceY + 1, rawZ, Material.ALLIUM);
                    else
                        BlockUtils.setDoublePlant(data, rawX, surfaceY + 1, rawZ, Material.PEONY);
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//Small trees or grass poffs
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);
        
        for (SimpleLocation sLoc : trees) {
        	
    		int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            
            if(tw.getBiomeBank(sLoc.getX(),sLoc.getZ()) == BiomeBank.CHERRY_GROVE &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())))
                switch(random.nextInt(20)) //0 to 19 inclusive
                {
                    case 19, 18, 17, 16, 15 -> //Rock (5/20)
                            new SphereBuilder(random, new SimpleBlock(data, sLoc), Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE, Material.MOSSY_COBBLESTONE)
                                    .setRadius(GenUtils.randInt(random, 3, 5))
                                    .setRY(GenUtils.randInt(random, 6, 10))
                                    .build();
                    default -> { //Tree (15/20)
                        if(random.nextBoolean())  //small trees
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                        else
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_THICK).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                        //No spore blossoms on 1.20 as the new cherry trees already drop petals
                        if(Version.isAtLeast(17) && !Version.isAtLeast(20)) {
                            for(int rX = sLoc.getX() - 6; rX <= sLoc.getX() + 6; rX++) {
                                for(int rZ = sLoc.getZ() - 6; rZ <= sLoc.getZ() + 6; rZ++) {
                                    Wall ceil = new Wall(new SimpleBlock(data, rX, sLoc.getY(), rZ)).findCeiling(15);
                                    if(ceil != null && GenUtils.chance(random, 1, 30)) {
                                        if(ceil.getType() == Material.DARK_OAK_LEAVES) {
                                            ceil.getRelative(0, -1, 0).setType(OneOneSevenBlockHandler.SPORE_BLOSSOM);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
	}
	
    public BiomeBank getBeachType() {
    	return BiomeBank.CHERRY_GROVE_BEACH;
    }
    
    public BiomeBank getRiverType() {
    	return BiomeBank.CHERRY_GROVE_RIVER;
    }

}
