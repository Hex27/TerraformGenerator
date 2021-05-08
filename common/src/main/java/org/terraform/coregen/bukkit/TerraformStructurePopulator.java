package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.PopulatorDataRecursiveICA;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class TerraformStructurePopulator extends BlockPopulator {

    private final TerraformWorld tw;

    public TerraformStructurePopulator(TerraformWorld tw) {
        this.tw = tw;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        //Don't attempt generation pre-injection.
        if (!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) return;
        PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);

        //Use IChunkAccess to place blocks instead. Known to cause lighting problems.
        if (TConfigOption.DEVSTUFF_EXPERIMENTAL_STRUCTURE_PLACEMENT.getBoolean())
            data = new PopulatorDataRecursiveICA(chunk);

        //PopulatorDataAbstract data = TerraformGeneratorPlugin.injector.getICAData(chunk);
        ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, data.getChunkX(), data.getChunkZ());


        //Spawn large structures
        MegaChunk mc = new MegaChunk(chunk.getX(), chunk.getZ());
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
        
        //Special Case
        if(new StrongholdPopulator().canSpawn(tw, data.getChunkX(), data.getChunkZ(), biome)) {
        	TerraformGeneratorPlugin.logger.info("Generating Stronghold at chunk: " + data.getChunkX() + "," + data.getChunkZ());
        	new StrongholdPopulator().populate(tw, data);
        }
        
        //Only check singlemegachunkstructures if this chunk is a central chunk.
        int[] coords = mc.getCenterBlockCoords();
        if(coords[0] >> 4 == data.getChunkX() 
        		&& coords[1] >> 4 == data.getChunkZ())
	        for (StructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
	            if (spop == null) continue;
	            if (!spop.isEnabled()) continue;
	            if (spop instanceof StrongholdPopulator) continue;
	            if (((SingleMegaChunkStructurePopulator)spop).canSpawn(tw, data.getChunkX(), data.getChunkZ(), biome)) {
	                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
	                spop.populate(tw, data);
	            }
	        }

        //Spawn small structures
        for (StructurePopulator spop : StructureRegistry.smallStructureRegistry) {
            if (((MultiMegaChunkStructurePopulator)spop).canSpawn(tw, data.getChunkX(), data.getChunkZ(), banks)) {
                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                spop.populate(tw, data);
            }
        }
    }
}
