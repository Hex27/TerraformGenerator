package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ColdOceansHandler extends AbstractOceanHandler {

    public ColdOceansHandler(BiomeType oceanType) {
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
    		return Biome.DEEP_COLD_OCEAN;
        return Biome.COLD_OCEAN;
    }

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
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        //Set ground near sea level to gravel
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;
        if (GenUtils.chance(random, 1, 150)) { //SEA GRASS/KELP
            CoralGenerator.generateKelpGrowth(data, rawX, surfaceY + 1, rawZ);
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

	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ROCKY_BEACH;
	}

}
