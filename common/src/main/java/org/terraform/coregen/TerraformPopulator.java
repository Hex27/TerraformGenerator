package org.terraform.coregen;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.cave.MasterCavePopulatorDistributor;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.populators.AmethystGeodePopulator;
import org.terraform.populators.OrePopulator;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.StructureBufferDistanceHandler;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.OrePopulatorFallbackSettings;
import org.terraform.utils.version.Version;

import java.util.EnumSet;
import java.util.Random;

public class TerraformPopulator {
	
    private static final OrePopulator[] ORE_POPS = {
            // Ores
            null,//deepslate
            null,//tuff
            null,//Space for copper
            new OrePopulator(Material.COAL_ORE,
             TConfigOption.ORE_COAL_CHANCE.getInt(),
             TConfigOption.ORE_COAL_VEINSIZE.getInt(),
             TConfigOption.ORE_COAL_MAXVEINNUMBER.getInt(),
			 TConfigOption.ORE_COAL_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_COAL_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_COAL_MAXSPAWNHEIGHT.getInt(),
             false),
            
            new OrePopulator(Material.IRON_ORE,
             TConfigOption.ORE_IRON_CHANCE.getInt(),
             TConfigOption.ORE_IRON_VEINSIZE.getInt(),
             TConfigOption.ORE_IRON_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_IRON_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_IRON_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_IRON_MAXSPAWNHEIGHT.getInt(),
             false),
            
            new OrePopulator(Material.GOLD_ORE,
             TConfigOption.ORE_GOLD_CHANCE.getInt(),
             TConfigOption.ORE_GOLD_VEINSIZE.getInt(),
             TConfigOption.ORE_GOLD_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_GOLD_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_GOLD_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_GOLD_MAXSPAWNHEIGHT.getInt(),
             false),
            
            new OrePopulator(Material.DIAMOND_ORE,
             TConfigOption.ORE_DIAMOND_CHANCE.getInt(),
             TConfigOption.ORE_DIAMOND_VEINSIZE.getInt(),
             TConfigOption.ORE_DIAMOND_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_DIAMOND_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_DIAMOND_COMMONSPAWNHEIGHT.getInt(),
			 TConfigOption.ORE_DIAMOND_MAXSPAWNHEIGHT.getInt(),
             false),
            
            
            //Emeralds only spawn in mountainous biomes (except deserts)
            new OrePopulator(Material.EMERALD_ORE,
             TConfigOption.ORE_EMERALD_CHANCE.getInt(),
             TConfigOption.ORE_EMERALD_VEINSIZE.getInt(),
             TConfigOption.ORE_EMERALD_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_EMERALD_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_EMERALD_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_EMERALD_MAXSPAWNHEIGHT.getInt(),
             false,
             BiomeBank.BIRCH_MOUNTAINS,
             BiomeBank.ROCKY_MOUNTAINS,
             BiomeBank.SNOWY_MOUNTAINS),
            
            
            new OrePopulator(Material.LAPIS_ORE,
             TConfigOption.ORE_LAPIS_CHANCE.getInt(),
             TConfigOption.ORE_LAPIS_VEINSIZE.getInt(),
             TConfigOption.ORE_LAPIS_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_LAPIS_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_LAPIS_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_LAPIS_MAXSPAWNHEIGHT.getInt(),
             false),
            
            new OrePopulator(Material.REDSTONE_ORE,
             TConfigOption.ORE_REDSTONE_CHANCE.getInt(),
             TConfigOption.ORE_REDSTONE_VEINSIZE.getInt(),
             TConfigOption.ORE_REDSTONE_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_REDSTONE_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_REDSTONE_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_REDSTONE_MAXSPAWNHEIGHT.getInt(),
             false),
            
            
            //Non-ores
            new OrePopulator(Material.GRAVEL,
             TConfigOption.ORE_GRAVEL_CHANCE.getInt(),
             TConfigOption.ORE_GRAVEL_VEINSIZE.getInt(),
             TConfigOption.ORE_GRAVEL_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_GRAVEL_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_GRAVEL_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_GRAVEL_MAXSPAWNHEIGHT.getInt(),
             true),
            
            new OrePopulator(Material.ANDESITE,
             TConfigOption.ORE_ANDESITE_CHANCE.getInt(),
             TConfigOption.ORE_ANDESITE_VEINSIZE.getInt(),
             TConfigOption.ORE_ANDESITE_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_ANDESITE_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_ANDESITE_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_ANDESITE_MAXSPAWNHEIGHT.getInt(),
             true),
            
            new OrePopulator(Material.DIORITE,
             TConfigOption.ORE_DIORITE_CHANCE.getInt(),
             TConfigOption.ORE_DIORITE_VEINSIZE.getInt(),
             TConfigOption.ORE_DIORITE_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_DIORITE_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_DIORITE_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_DIORITE_MAXSPAWNHEIGHT.getInt(),
             true),
            
            new OrePopulator(Material.GRANITE,
             TConfigOption.ORE_GRANITE_CHANCE.getInt(),
             TConfigOption.ORE_GRANITE_VEINSIZE.getInt(),
             TConfigOption.ORE_GRANITE_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_GRANITE_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_GRANITE_COMMONSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_GRANITE_MAXSPAWNHEIGHT.getInt(),
             true)
    }; 

    private AmethystGeodePopulator amethystGeodePopulator;
    public TerraformPopulator(TerraformWorld tw) {
    	if(Version.isAtLeast(17)) {
    		amethystGeodePopulator = new AmethystGeodePopulator(
    				TConfigOption.ORE_AMETHYST_GEODE_SIZE.getInt(),
    				TConfigOption.ORE_AMETHYST_CHANCE.getDouble(),
    				TConfigOption.ORE_AMETHYST_MIN_DEPTH.getInt(),
    				TConfigOption.ORE_AMETHYST_MIN_DEPTH_BELOW_SURFACE.getInt());
    		
    		ORE_POPS[0] = new OrePopulator(OneOneSevenBlockHandler.COPPER_ORE, TConfigOption.ORE_COPPER_CHANCE.getInt(), TConfigOption.ORE_COPPER_VEINSIZE.getInt(),
                    TConfigOption.ORE_COPPER_MAXVEINNUMBER.getInt(), TConfigOption.ORE_COPPER_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_COPPER_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_COPPER_MAXSPAWNHEIGHT.getInt(),
                    false);
    		ORE_POPS[1] = new OrePopulator(OneOneSevenBlockHandler.DEEPSLATE, TConfigOption.ORE_DEEPSLATE_CHANCE.getInt(), TConfigOption.ORE_DEEPSLATE_VEINSIZE.getInt(),
                    TConfigOption.ORE_DEEPSLATE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_DEEPSLATE_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_DEEPSLATE_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_DEEPSLATE_MAXSPAWNHEIGHT.getInt(),
                    true);
    		ORE_POPS[2] = new OrePopulator(OneOneSevenBlockHandler.TUFF, TConfigOption.ORE_TUFF_CHANCE.getInt(), TConfigOption.ORE_TUFF_VEINSIZE.getInt(),
                    TConfigOption.ORE_TUFF_MAXVEINNUMBER.getInt(), TConfigOption.ORE_TUFF_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_TUFF_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_TUFF_MAXSPAWNHEIGHT.getInt(),
                    true);
    	}
    	
    	if(!Version.isAtLeast(18)) //Below 1.18
    	{
    		boolean repairOreSettings = false;
    		for(OrePopulator orePop:ORE_POPS) { 
    			if(orePop != null)
	    			if(orePop.getMinRange() <= 0) { //Not configured for min height
	    				TerraformGeneratorPlugin.logger.stdout("&c" + orePop.getType().toString() + " was configured to use Y <= 0! Reverting ore configuration to hardcoded 1.16 values.");
	    				repairOreSettings = true;
	    			}
    		}
    		if(repairOreSettings)
				OrePopulatorFallbackSettings.repairOreSettings(ORE_POPS);
    	}
    }
    
    private MasterCavePopulatorDistributor caveDistributor = new MasterCavePopulatorDistributor();

    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
    	random = tw.getHashedRand(571162, data.getChunkX(), data.getChunkZ());
        //ores
        for (OrePopulator ore : ORE_POPS) {
        	if(ore == null)
        		continue;
            ore.populate(tw, random, data);
        }
        
        //Amethysts
        if(amethystGeodePopulator != null)
        	amethystGeodePopulator.populate(tw, random, data);

        // Get all biomes in a chunk
        EnumSet<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, data.getChunkX(), data.getChunkZ());

        boolean canDecorate = StructureBufferDistanceHandler.canDecorateChunk(tw, data.getChunkX(), data.getChunkZ());
        for (BiomeBank bank : banks) {
            // Biome specific populators
            bank.getHandler().populateSmallItems(tw, random, data);
            
            //Only decorate disruptive features if the structures allow for them
            if(canDecorate)
            	bank.getHandler().populateLargeItems(tw, random, data);
        }
        
		// Cave populators
        //They will recalculate biomes per block.
		caveDistributor.populate(tw, random, data);

		//Multi-megachunk structures
        for (StructurePopulator spop : StructureRegistry.smallStructureRegistry) {
            if (((MultiMegaChunkStructurePopulator)spop).canSpawn(tw, data.getChunkX(), data.getChunkZ())) {
                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                
                //No async events
                //Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(data.getChunkX()*16+8, data.getChunkZ()*16+8, spop.getClass().getName()));
                
                spop.populate(tw, data);
            }
        }
    }
}
