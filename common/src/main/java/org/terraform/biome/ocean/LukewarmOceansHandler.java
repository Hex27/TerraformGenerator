package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class LukewarmOceansHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.LUKEWARM_OCEAN;
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
        		Material.GRAVEL,
        		Material.GRAVEL,
                GenUtils.randMaterial(rand, Material.STONE, Material.GRAVEL, Material.STONE),
                GenUtils.randMaterial(rand, Material.STONE),
                GenUtils.randMaterial(rand, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, y, z) != getBiome()) continue;

                //Set ground near sea level to sand
                if(y >= TerraformGenerator.seaLevel - 2) {
                	data.setType(x, y, z, Material.SAND);
                }else if(y >= TerraformGenerator.seaLevel - 4) {
                	if(random.nextBoolean())
                    	data.setType(x, y, z, Material.SAND);
                }
                
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (GenUtils.chance(random, 10, 100)) { //SEA GRASS/KELP
                    CoralGenerator.generateKelpGrowth(data, x, y + 1, z);
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                boolean growCorals =
                        y <= TConfigOption.BIOME_LUKEWARM_OCEAN_CORAL_MAXHEIGHT.getInt()
                                && y >= TConfigOption.BIOME_LUKEWARM_OCEAN_CORAL_MINHEIGHT.getInt();
                if (data.getBiome(x, y, z) != getBiome()) continue;
                
                if (growCorals) {
                	//Spawn corals, along with a circular patch beneath them.
                    if (GenUtils.chance(random, 15, 100)) {
                        CoralGenerator.generateCoral(data, x, y + 1, z);
                        BlockUtils.replaceCircularPatch(random.nextInt(9999), 2, new SimpleBlock(data,x,y,z), Material.SAND);
                    }else if (GenUtils.chance(random, 1, 100)) {
                        TreeDB.spawnRandomGiantCoral(tw, data, x, y, z);
                        BlockUtils.replaceCircularPatch(random.nextInt(9999), 4, new SimpleBlock(data,x,y,z), Material.SAND);
                    }
                }
            }
        }
	}


}
