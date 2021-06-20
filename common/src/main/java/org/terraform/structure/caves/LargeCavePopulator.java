package org.terraform.structure.caves;

import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class LargeCavePopulator extends SingleMegaChunkStructurePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        if (!TConfigOption.STRUCTURES_LARGECAVE_ENABLED.getBoolean())
            return;
        
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());

        int[] spawnCoords = mc.getCenterBlockCoords();

        int x = spawnCoords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = spawnCoords[1];//data.getChunkZ()*16 + random.nextInt(16);
        Random rand = tw.getHashedRand(x, z, 999323);

        int highest = HeightMap.getBlockHeight(tw, x, z);//GenUtils.getHighestGround(data, x, z);
        int rY = (highest - 20) / 2; //5 block padding bottom, 15 padding top.

        switch(rand.nextInt(3)) {
        case 0:
    		new GenericLargeCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
    		break;
        case 1:
    		new MushroomCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
    		break;
    	default:
    		new LargeLushCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
    		break;
        }
    }

    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12345),
                (int) (TConfigOption.STRUCTURES_LARGECAVE_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
		if(biome.getType() == BiomeType.DEEP_OCEANIC)
			return false;
        return rollSpawnRatio(tw,chunkX,chunkZ);
    }

    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(123912, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_LARGECAVE_ENABLED.getBoolean();
    }

    @Override
    public int getChunkBufferDistance() {
    	return 0;
    }
}
