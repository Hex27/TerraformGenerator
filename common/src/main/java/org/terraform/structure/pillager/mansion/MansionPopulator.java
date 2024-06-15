package org.terraform.structure.pillager.mansion;

import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.utils.GenUtils;

public class MansionPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(717281012, chunkX, chunkZ);
    }
    
    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 99572),
                (int) (TConfigOption.STRUCTURES_MANSION_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
    	//Enforce minimum distance
        if(Math.pow(chunkX*16,2) + Math.pow(chunkZ*16,2) < Math.pow(TConfigOption.STRUCTURES_MANSION_MINDISTANCE.getInt(),2))
            return false;

        //Mansions must spawn. Dark forests are rare enough. Ignore ground height.
    	if(biome == (BiomeBank.DARK_FOREST)) {
            return rollSpawnRatio(tw,chunkX,chunkZ);
        }
        return false;
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());


        //If it is below sea level, DON'T SPAWN IT.
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); //getCoordsFromMegaChunk(tw, mc);
        int y = GenUtils.getHighestGround(data, coords[0], coords[1]);
    	if(y < TerraformGenerator.seaLevel) y = TerraformGenerator.seaLevel;
    	

        MansionJigsawBuilder builder = new MansionJigsawBuilder(
        		TConfigOption.STRUCTURES_MANSION_SIZE.getInt(), 
        		TConfigOption.STRUCTURES_MANSION_SIZE.getInt(), 
        		data, coords[0], y, coords[1]
        );
        builder.generate(new Random());
        builder.build(new Random());
        
    }

    @Override
    public int getChunkBufferDistance() {
    	return TConfigOption.STRUCTURES_MANSION_CHUNK_EXCLUSION_ZONE.getInt();
    }

    @Override
    public boolean isEnabled() {
        return BiomeBank.isBiomeEnabled(BiomeBank.DARK_FOREST) 
        		&& TConfigOption.STRUCTURES_MANSION_ENABLED.getBoolean();
    }
}
