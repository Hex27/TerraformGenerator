package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Snowable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.OneOneEightBlockHandler;

import java.util.Random;

public class SnowyWastelandHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return OneOneEightBlockHandler.SNOWY_PLAINS;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.SNOW_BLOCK,
                Material.SNOW_BLOCK,
                GenUtils.randMaterial(rand, Material.SNOW_BLOCK, Material.SNOW_BLOCK, Material.DIRT, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        //Snowier Snow
        if(GenUtils.chance(random, 1, 500)) {
            BlockUtils.replaceCircularPatch(random.nextInt(9999), 3, new SimpleBlock(data,rawX,0,rawZ), OneOneSevenBlockHandler.POWDER_SNOW);
        }
        if (data.getType(rawX, surfaceY + 1, rawZ) == Material.AIR
                && data.getType(rawX, surfaceY, rawZ) != OneOneSevenBlockHandler.POWDER_SNOW
                && GenUtils.isGroundLike(data.getType(rawX, surfaceY, rawZ))) {
            data.setType(rawX, surfaceY + 1, rawZ, Material.SNOW);
            if (data.getBlockData(rawX, surfaceY, rawZ) instanceof Snowable snowable) {
                snowable.setSnowy(true);
                data.setBlockData(rawX, surfaceY, rawZ, snowable);
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        SimpleLocation[] items = GenUtils.randomObjectPositions(world, data.getChunkX(), data.getChunkZ(), 44);

        for(SimpleLocation sLoc:items) {
        	if (data.getBiome(sLoc.getX(),sLoc.getZ()) != getBiome()) continue;
        	sLoc.setY(GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ()));
            if (data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) != Material.SNOW_BLOCK) continue;

            BlockUtils.spawnPillar(random, data, sLoc.getX(),sLoc.getY(),sLoc.getZ(), Material.SPRUCE_LOG, 6, 8);

            if (GenUtils.chance(1, 3))
                new FractalTreeBuilder(FractalTypes.Tree.FROZEN_TREE_SMALL)
                        .setSnowyLeaves(true).build(world, data, sLoc.getX(), sLoc.getY()+1, sLoc.getZ());

            if (GenUtils.chance(1, 30))
                new FractalTreeBuilder(FractalTypes.Tree.FROZEN_TREE_BIG)
                        .build(world, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
        }
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ICY_BEACH;
	}
	@Override
	public BiomeBank getRiverType() {
		return BiomeBank.FROZEN_RIVER;
	}
}
