package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.small.WitchHutPopulator;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneNineBlockHandler;

import java.util.Random;

public class MangroveHandler extends BiomeHandler {

    @Override
    public BiomeBank getRiverType(){ return BiomeBank.MANGROVE; }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return OneOneNineBlockHandler.MANGROVE_SWAMP;
    }
    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.GRASS_BLOCK, Material.PODZOL, Material.PODZOL),
                GenUtils.randMaterial(rand, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        int seaLevel = TerraformGenerator.seaLevel;

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (y < seaLevel) {
                    
                    FastNoise mudNoise = NoiseCacheHandler.getNoise(
                    		tw, 
                    		NoiseCacheEntry.BIOME_SWAMP_MUDNOISE, 
                    		world -> {
                                FastNoise n = new FastNoise((int) (world.getSeed() * 4));
                                n.SetNoiseType(NoiseType.SimplexFractal);
                                n.SetFrequency(0.05f);
                                n.SetFractalOctaves(4);
                            
                    	        return n;
                    		});
                    
                    double noise = mudNoise.GetNoise(x,z);
                	
                	if (noise < 0) noise = 0;
                    int att = (int) Math.round(noise * 10);
                    if (att + y > seaLevel)
                        att = seaLevel - y;
                    for (int i = 1; i <= att; i++) {
                        if (i < att)
                            data.setType(x, y + i, z, getSurfaceCrust(random)[1]);
                        else
                            data.setType(x, y + i, z, getSurfaceCrust(random)[0]);
                    }
                    y += att;

                    if (data.getType(x,TerraformGenerator.seaLevel,z) == Material.WATER) {
                        if (GenUtils.chance(random, 1, 30))
                            data.setType(x, TerraformGenerator.seaLevel + 1, z, Material.LILY_PAD);
                    }
                }

                if (BlockUtils.isWet(new SimpleBlock(data,x,y+1,z))
                        && GenUtils.chance(random, 10, 100) && y < TerraformGenerator.seaLevel - 3) { //SEA GRASS/KELP
                    CoralGenerator.generateKelpGrowth(data, x, y + 1, z);

                }
                if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
                    BlockUtils.generateClayDeposit(x, y, z, data, random);
                }
                if(GenUtils.chance(random, 5, 1000)) {
                	BlockUtils.replaceCircularPatch(
                			random.nextInt(9999), 
                			3.5f, 
                			new SimpleBlock(data,x,y,z), OneOneNineBlockHandler.MUD);
                }
            }
        }

    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        int treeX, treeY, treeZ;
        if (GenUtils.chance(random, 8, 10)) {
            treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;

            if (data.getBiome(treeX, treeZ) == getBiome()) {
                treeY = GenUtils.getHighestGround(data, treeX, treeZ);
                
                if(treeY > TerraformGenerator.seaLevel-6) {
                	 //Don't do gradient checks for swamp trees, the mud is uneven.
                	//just make sure it's submerged
                    TreeDB.spawnBreathingRoots(tw, new SimpleBlock(data,treeX,treeY,treeZ), OneOneNineBlockHandler.MANGROVE_ROOTS);
                    FractalTypes.Tree.SWAMP_TOP.build(tw, new SimpleBlock(data,treeX,treeY,treeZ), (t)->t.setCheckGradient(false));
                }
            }
        }
	}

	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.MUDFLATS;
	}
	
	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	
        double height = HeightMap.CORE.getHeight(tw, x, z) - 10;

        //If the height is too low, force it back to 3.
        //30/11/2023: what the fuck is this guard clause for
        if (height <= 0) height = 3;
        
        return height;
    }

}
