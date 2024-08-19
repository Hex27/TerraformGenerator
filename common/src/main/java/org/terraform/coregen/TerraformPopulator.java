package org.terraform.coregen;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.cavepopulators.MasterCavePopulatorDistributor;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.populators.AmethystGeodePopulator;
import org.terraform.populators.OrePopulator;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.StructureBufferDistanceHandler;
import org.terraform.structure.StructureRegistry;
import org.terraform.utils.GenUtils;

import java.util.EnumSet;
import java.util.Random;

public class TerraformPopulator extends BlockPopulator {
	
    private static final OrePopulator[] ORE_POPS = {
            // Ores
            new OrePopulator(Material.DEEPSLATE, TConfigOption.ORE_DEEPSLATE_CHANCE.getInt(), TConfigOption.ORE_DEEPSLATE_VEINSIZE.getInt(),
                    TConfigOption.ORE_DEEPSLATE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_DEEPSLATE_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_DEEPSLATE_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_DEEPSLATE_MAXSPAWNHEIGHT.getInt(),
                    true),// deepslate
            new OrePopulator(Material.TUFF, TConfigOption.ORE_TUFF_CHANCE.getInt(), TConfigOption.ORE_TUFF_VEINSIZE.getInt(),
                    TConfigOption.ORE_TUFF_MAXVEINNUMBER.getInt(), TConfigOption.ORE_TUFF_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_TUFF_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_TUFF_MAXSPAWNHEIGHT.getInt(),
                    true),// tuff
            new OrePopulator(Material.COPPER_ORE, TConfigOption.ORE_COPPER_CHANCE.getInt(), TConfigOption.ORE_COPPER_VEINSIZE.getInt(),
                    TConfigOption.ORE_COPPER_MAXVEINNUMBER.getInt(), TConfigOption.ORE_COPPER_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_COPPER_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_COPPER_MAXSPAWNHEIGHT.getInt(),
                    false),// Space for copper
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

            // BADLANDS SPAWNRATE
            new OrePopulator(Material.GOLD_ORE,
                    TConfigOption.ORE_BADLANDSGOLD_CHANCE.getInt(),
                    TConfigOption.ORE_BADLANDSGOLD_VEINSIZE.getInt(),
                    TConfigOption.ORE_BADLANDSGOLD_MAXVEINNUMBER.getInt(),
                    TConfigOption.ORE_BADLANDSGOLD_MINSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_BADLANDSGOLD_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_BADLANDSGOLD_MAXSPAWNHEIGHT.getInt(),
                    false,
                    BiomeBank.BADLANDS,
                    BiomeBank.BADLANDS_CANYON,
                    BiomeBank.BADLANDS_CANYON_PEAK,
                    BiomeBank.BADLANDS_BEACH,
                    BiomeBank.BADLANDS_RIVER),
            
            new OrePopulator(Material.DIAMOND_ORE,
             TConfigOption.ORE_DIAMOND_CHANCE.getInt(),
             TConfigOption.ORE_DIAMOND_VEINSIZE.getInt(),
             TConfigOption.ORE_DIAMOND_MAXVEINNUMBER.getInt(),
             TConfigOption.ORE_DIAMOND_MINSPAWNHEIGHT.getInt(),
             TConfigOption.ORE_DIAMOND_COMMONSPAWNHEIGHT.getInt(),
			 TConfigOption.ORE_DIAMOND_MAXSPAWNHEIGHT.getInt(),
             false),
            
            
            // Emeralds only spawn in mountainous biomes (except deserts)
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
             BiomeBank.SNOWY_MOUNTAINS,
             BiomeBank.FORESTED_MOUNTAINS,
             BiomeBank.COLD_JAGGED_PEAKS,
             BiomeBank.JAGGED_PEAKS,
             BiomeBank.FORESTED_PEAKS),
            
            
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
            
            
            // Non-ores
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

    private final AmethystGeodePopulator amethystGeodePopulator = new AmethystGeodePopulator(
            TConfigOption.ORE_AMETHYST_GEODE_SIZE.getInt(),
            TConfigOption.ORE_AMETHYST_CHANCE.getDouble(),
            TConfigOption.ORE_AMETHYST_MIN_DEPTH.getInt(),
            TConfigOption.ORE_AMETHYST_MIN_DEPTH_BELOW_SURFACE.getInt());
    public TerraformPopulator(TerraformWorld tw) {
    }
    
    private final MasterCavePopulatorDistributor caveDistributor = new MasterCavePopulatorDistributor();

    @Override
    public void populate(@NotNull org.bukkit.generator.WorldInfo worldInfo, @NotNull java.util.Random random,
                         int chunkX, int chunkZ,
                         @NotNull org.bukkit.generator.LimitedRegion limitedRegion)
    {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        PopulatorDataAbstract data = new PopulatorDataSpigotAPI(limitedRegion, tw, chunkX, chunkZ);
        this.populate(tw, data);
    }

    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        Random random = tw.getHashedRand(571162, data.getChunkX(), data.getChunkZ());
        // ores
        for (OrePopulator ore : ORE_POPS) {
            ore.populate(tw, random, data);
        }
        
        // Amethysts
        amethystGeodePopulator.populate(tw, random, data);

        // Get all biomes in a chunk
        EnumSet<BiomeBank> banks = EnumSet.noneOf(BiomeBank.class);

        boolean canDecorate = StructureBufferDistanceHandler.canDecorateChunk(tw, data.getChunkX(), data.getChunkZ());

        // Small Populators run per block.
        for(int rawX = data.getChunkX()*16; rawX <= data.getChunkX()*16+16; rawX++)
            for(int rawZ = data.getChunkZ()*16; rawZ <= data.getChunkZ()*16+16; rawZ++)
            {
                int surfaceY = GenUtils.getTransformedHeight(data.getTerraformWorld(), rawX, rawZ);
                BiomeBank bank = tw.getBiomeBank(rawX,surfaceY,rawZ);
                banks.add(bank);

                // Don't populate wet stuff in places that aren't wet
                if(!bank.isDry() && data.getType(rawX,surfaceY+1,rawZ) != Material.WATER)
                    continue;
                bank.getHandler().populateSmallItems(tw, random, rawX, surfaceY, rawZ, data);
            }

        // Only decorate disruptive features if the structures allow for them
        if(canDecorate)
            for (BiomeBank bank : banks)
                bank.getHandler().populateLargeItems(tw, random, data);

        
		// Cave populators
        // They will recalculate biomes per block.
		caveDistributor.populate(tw, random, data);

		// Multi-megachunk structures
        for (MultiMegaChunkStructurePopulator spop : StructureRegistry.smallStructureRegistry) {
            if (TConfigOption.areStructuresEnabled() && spop.canSpawn(tw, data.getChunkX(), data.getChunkZ())) {
                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                
                // No async events
                // Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(data.getChunkX()*16+8, data.getChunkZ()*16+8, spop.getClass().getName()));
                
                spop.populate(tw, data);
            }
        }
    }
}
