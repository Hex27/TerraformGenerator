package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.biome.flat.MuddyBogHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import java.util.Random;

public class BogBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.SWAMP;
    }

    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.MUDDY_BOG;
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
        return new Material[]{
        		Material.GRASS_BLOCK,
                GenUtils.randMaterial(rand, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        SimpleBlock block = new SimpleBlock(data,rawX,surfaceY,rawZ);
        if(!BlockUtils.isWet(block.getRelative(0,1,0))) {
            if(GenUtils.chance(random, 1, 85))
                block.getRelative(0,1,0).setType(Material.DEAD_BUSH);
            else if(GenUtils.chance(random, 1, 85))
                block.getRelative(0,1,0).setType(Material.BROWN_MUSHROOM);
            else if(GenUtils.chance(random, 1, 85))
                block.getRelative(0,1,0).setType(Material.GRASS);
            else if(GenUtils.chance(random, 1, 85))
                BlockUtils.setDoublePlant(data, rawX,surfaceY+1,rawZ, Material.TALL_GRASS);
            else { //Possible Sugarcane
                for(BlockFace face:BlockUtils.directBlockFaces) {
                    if(GenUtils.chance(random, 1, 75) && BlockUtils.isWet(block.getRelative(face))) {
                        new Wall(block.getRelative(0,1,0))
                            .LPillar(GenUtils.randInt(2, 5), random, Material.SUGAR_CANE);
                    }
                }
            }

        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
    	new MuddyBogHandler().populateLargeItems(tw, random, data);
	}
}
