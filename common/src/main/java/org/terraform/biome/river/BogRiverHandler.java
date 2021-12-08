package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.biome.flat.MuddyBogHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import java.util.Random;

public class BogRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.SWAMP;
    }

    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.MUDDY_BOG;
    }


    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
    	
    	//Aggressively raise ground up from the river floor
    	for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
            	SimpleBlock block = new SimpleBlock(data, x,0,z).getGround();

            	FastNoise sinkin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_MUDDYBOG_HEIGHTMAP, world -> {
                    FastNoise n = new FastNoise((int) tw.getSeed());
                    n.SetNoiseType(NoiseType.SimplexFractal);
                    n.SetFractalOctaves(4);
                    n.SetFrequency(0.005f);
                    return n;
                });
            	
            	double noise = sinkin.GetNoise(x, z);
            	if(noise > -0.2) {
            		noise += 0.5;
            		if(noise > 1.05) noise = 1.05;
            		if(block.getY() < TerraformGenerator.seaLevel){
            			double maxHeight = (TerraformGenerator.seaLevel - block.getY()) + 2.0;
            			int height = (int) Math.round((maxHeight*noise));
                        if (data.getBiome(x, z) != getBiome())
                        	height *= 0.5;
            			
            			new Wall(block.getRelative(0,1,0)).LPillar(height, random, Material.DIRT);
            			block = block.getGround();
            			if(!BlockUtils.isWet(block.getRelative(0,1,0))) {
            				block.setType(Material.GRASS_BLOCK);
            			}
            		}
            	}
            }
    	}
        
        //Dry decorations
        new MuddyBogHandler().populateSmallItems(tw, random, data);
    	
    	//Water decorations
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                
                SimpleBlock block = new SimpleBlock(data,x,y,z);
                if(BlockUtils.isWet(block.getRelative(0,1,0)))
                {
                	if(GenUtils.chance(random, 1, 70))
                		block.getAtY(TerraformGenerator.seaLevel+1).setType(Material.LILY_PAD);
                	else if(GenUtils.chance(random, 1, 70))
                		CoralGenerator.generateKelpGrowth(data, x, y+1, z);
                	
                    // Generate clay
                    if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
                        BlockUtils.generateClayDeposit(x, y, z, data, random);
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
