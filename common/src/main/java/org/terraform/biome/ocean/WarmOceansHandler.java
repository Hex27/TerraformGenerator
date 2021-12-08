package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class WarmOceansHandler extends AbstractOceanHandler {

    public WarmOceansHandler(BiomeType oceanType) {
		super(oceanType);
		// TODO Auto-generated constructor stub
	}

	@Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
    	if(this.oceanType == BiomeType.DEEP_OCEANIC)
    		return Biome.WARM_OCEAN;
        return Biome.WARM_OCEAN;
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
        //boolean growCorals = random.nextBoolean();

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                
                //Set ground near sea level to sand
                if(y >= TerraformGenerator.seaLevel - 2) {
                	data.setType(x, y, z, Material.SAND);
                }else if(y >= TerraformGenerator.seaLevel - 4) {
                	if(random.nextBoolean())
                    	data.setType(x, y, z, Material.SAND);
                }
                
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (GenUtils.chance(random, 10, 100)) { //SEA GRASS/KELP
                    data.setType(x, y + 1, z, Material.SEAGRASS);
                    if (random.nextBoolean() && y < TerraformGenerator.seaLevel - 3)
                        BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_SEAGRASS);
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//Spawn rocks
		SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 25, 0.4f);
        
        for (SimpleLocation sLoc : rocks) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int rockY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(rockY);
                if(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) != Material.GRAVEL)
                		continue;
                
                BlockUtils.replaceSphere(
                		random.nextInt(9987),
                		(float) GenUtils.randDouble(random, 3, 7), 
                		(float) GenUtils.randDouble(random, 2, 4), 
                		(float) GenUtils.randDouble(random, 3, 7), 
                		new SimpleBlock(data,sLoc), 
                		true, 
                		GenUtils.randMaterial(
                				Material.STONE,
                				Material.GRANITE,
                				Material.ANDESITE,
                				Material.DIORITE
                		));
            }
        }
	}

}
