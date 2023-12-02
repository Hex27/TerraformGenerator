package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import java.util.Random;

public class MuddyBogHandler extends BiomeHandler {

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
    
    //Beach type. This will be used instead if the height is too close to sea level.
    public BiomeBank getBeachType() {
    	return BiomeBank.BOG_BEACH;
    }
    
    //River type. This will be used instead if the heightmap got carved into a river.
    public BiomeBank getRiverType() {
    	return BiomeBank.BOG_RIVER;
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
                if (world.getBiomeBank(x,z) != BiomeBank.MUDDY_BOG) continue;

                SimpleBlock block = new SimpleBlock(data,x,y,z);
                if(block.getRelative(0,1,0).getType() == Material.AIR &&
                		block.getType() == Material.GRASS_BLOCK) {
                	if(GenUtils.chance(random, 1, 85))
                		block.getRelative(0,1,0).setType(Material.DEAD_BUSH);
                	else if(GenUtils.chance(random, 1, 85))
                		block.getRelative(0,1,0).setType(Material.BROWN_MUSHROOM);
                	else if(GenUtils.chance(random, 1, 85))
                		block.getRelative(0,1,0).setType(Material.GRASS);
                	else if(GenUtils.chance(random, 1, 85))
                		BlockUtils.setDoublePlant(data, x, y+1, z, Material.TALL_GRASS);
                	else if(GenUtils.chance(random, 1, 300)) 
                	{//Dripstone Cluster
                		BlockUtils.replaceCircularPatch(random.nextInt(9999), 2.5f, block, OneOneSevenBlockHandler.DRIPSTONE_BLOCK);
                		if(GenUtils.chance(random, 1, 7))
                			OneOneSevenBlockHandler.upLPointedDripstone(GenUtils.randInt(random, 2, 4), block.getRelative(0,1,0));
                		for(BlockFace face:BlockUtils.xzPlaneBlockFaces)
                			if(GenUtils.chance(random, 1, 7))
                    			OneOneSevenBlockHandler.upLPointedDripstone(GenUtils.randInt(random, 2, 4), block.getRelative(face).getGround().getRelative(0,1,0));
                	}
                		
                }

                //Mud
                //Weirdly, STRANGELY, mud is not brown, it is grey
                //It looks absolutely hideous with the brown colour palatte.
                //Guess there is no mud in the muddy bog. Wee.
//                if(Version.isAtLeast(1.19))
//	                if (GenUtils.chance(random, 3, 1000)) {
//	                	BlockUtils.replaceCircularPatch(random.nextInt(9999), 3f, new SimpleBlock(data,x,y,z), OneOneNineBlockHandler.MUD);
//	                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		 //Small brown mushrooms on dry areas
        SimpleLocation[] shrooms = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);

        for (SimpleLocation sLoc : shrooms) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
            		!BlockUtils.isWet(new SimpleBlock(data,sLoc.getX(),sLoc.getY()+1,sLoc.getZ()))&&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
            	if(data.getType(sLoc.getX(),sLoc.getY()+1,sLoc.getZ()) == Material.AIR)
	            	if(random.nextBoolean())
	            		new MushroomBuilder(FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM)
	            		.build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
	            	else
	            		new MushroomBuilder(FractalTypes.Mushroom.TINY_BROWN_MUSHROOM)
	            		.build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
            }
        }
	}
	
    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
        
    	double height = super.calculateHeight(tw, x, z) - 5;
    	
    	FastNoise sinkin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_MUDDYBOG_HEIGHTMAP, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(4);
            n.SetFrequency(0.08f);
            return n;
        });
    	
    	if(sinkin.GetNoise(x, z) < -0.15) {
    		if(height > TerraformGenerator.seaLevel)
    			height -= (height - TerraformGenerator.seaLevel) + 2;
    	}
    	
        return height;
    }
}
