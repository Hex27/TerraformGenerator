package org.terraform.coregen.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.coregen.populatordata.PopulatorDataRecursiveICA;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.event.TerraformStructureSpawnEvent;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.structure.stronghold.StrongholdPopulator;
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

        //Spawn large structures
        MegaChunk mc = new MegaChunk(chunk.getX(), chunk.getZ());
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
        
        //Special Case
        if(new StrongholdPopulator().canSpawn(tw, data.getChunkX(), data.getChunkZ(), biome)) {
        	TerraformGeneratorPlugin.logger.info("Generating Stronghold at chunk: " + data.getChunkX() + "," + data.getChunkZ());
        	new StrongholdPopulator().populate(tw, data);
        }
        
        //Only check singlemegachunkstructures if this chunk is a central chunk.
        int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
        //TerraformGeneratorPlugin.logger.info("[v] MC(" + mc.getX() + "," + mc.getZ() + ") - " + data.getChunkX() + "," + data.getChunkZ() + " - Center: " + chunkCoords[0] + "," + chunkCoords[1]);
        if(chunkCoords[0] == data.getChunkX() 
        		&& chunkCoords[1] == data.getChunkZ()) {
        	int[] blockCoords = mc.getCenterBiomeSectionBlockCoords();
            
        	//TerraformGeneratorPlugin.logger.info("[!] MC(" + mc.getX() + "," + mc.getZ() + ") - " + data.getChunkX() + "," + data.getChunkZ() + " - Center: " + chunkCoords[0] + "," + chunkCoords[1]);
            for (StructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
	            if (spop == null) continue;
	            if (!spop.isEnabled()) continue;
	            if (spop instanceof StrongholdPopulator) continue;
	            //TerraformGeneratorPlugin.logger.info("[v]       MC(" + mc.getX() + "," + mc.getZ() + ") - Checking " + spop.getClass().getName());
	            if (((SingleMegaChunkStructurePopulator)spop).canSpawn(tw, data.getChunkX(), data.getChunkZ(), biome)) {
	                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
	                Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(blockCoords[0], blockCoords[1], spop.getClass().getName()));
	                spop.populate(tw, data);
	                break;
	            }
	        }
        }

        //Spawn small structures
        for (StructurePopulator spop : StructureRegistry.smallStructureRegistry) {
            if (((MultiMegaChunkStructurePopulator)spop).canSpawn(tw, data.getChunkX(), data.getChunkZ())) {
                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(data.getChunkX()*16+8, data.getChunkZ()*16+8, spop.getClass().getName()));
                spop.populate(tw, data);
            }
        }
    }
}
