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
import org.terraform.main.config.TConfig;
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
            new OrePopulator(Material.DEEPSLATE,
                    TConfig.c.ORE_DEEPSLATE_CHANCE,
                    TConfig.c.ORE_DEEPSLATE_VEINSIZE,
                    TConfig.c.ORE_DEEPSLATE_MAXVEINNUMBER,
                    TConfig.c.ORE_DEEPSLATE_MINSPAWNHEIGHT,
                    TConfig.c.ORE_DEEPSLATE_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_DEEPSLATE_MAXSPAWNHEIGHT,
                    true
            ),// deepslate
            new OrePopulator(Material.TUFF,
                    TConfig.c.ORE_TUFF_CHANCE,
                    TConfig.c.ORE_TUFF_VEINSIZE,
                    TConfig.c.ORE_TUFF_MAXVEINNUMBER,
                    TConfig.c.ORE_TUFF_MINSPAWNHEIGHT,
                    TConfig.c.ORE_TUFF_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_TUFF_MAXSPAWNHEIGHT,
                    true
            ),// tuff
            new OrePopulator(Material.COPPER_ORE,
                    TConfig.c.ORE_COPPER_CHANCE,
                    TConfig.c.ORE_COPPER_VEINSIZE,
                    TConfig.c.ORE_COPPER_MAXVEINNUMBER,
                    TConfig.c.ORE_COPPER_MINSPAWNHEIGHT,
                    TConfig.c.ORE_COPPER_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_COPPER_MAXSPAWNHEIGHT,
                    false
            ),// Space for copper
            new OrePopulator(Material.COAL_ORE,
                    TConfig.c.ORE_COAL_CHANCE,
                    TConfig.c.ORE_COAL_VEINSIZE,
                    TConfig.c.ORE_COAL_MAXVEINNUMBER,
                    TConfig.c.ORE_COAL_MINSPAWNHEIGHT,
                    TConfig.c.ORE_COAL_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_COAL_MAXSPAWNHEIGHT,
                    false
            ),

            new OrePopulator(Material.IRON_ORE,
                    TConfig.c.ORE_IRON_CHANCE,
                    TConfig.c.ORE_IRON_VEINSIZE,
                    TConfig.c.ORE_IRON_MAXVEINNUMBER,
                    TConfig.c.ORE_IRON_MINSPAWNHEIGHT,
                    TConfig.c.ORE_IRON_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_IRON_MAXSPAWNHEIGHT,
                    false
            ),

            new OrePopulator(Material.GOLD_ORE,
                    TConfig.c.ORE_GOLD_CHANCE,
                    TConfig.c.ORE_GOLD_VEINSIZE,
                    TConfig.c.ORE_GOLD_MAXVEINNUMBER,
                    TConfig.c.ORE_GOLD_MINSPAWNHEIGHT,
                    TConfig.c.ORE_GOLD_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_GOLD_MAXSPAWNHEIGHT,
                    false
            ),

            // BADLANDS SPAWNRATE
            new OrePopulator(Material.GOLD_ORE,
                    TConfig.c.ORE_BADLANDSGOLD_CHANCE,
                    TConfig.c.ORE_BADLANDSGOLD_VEINSIZE,
                    TConfig.c.ORE_BADLANDSGOLD_MAXVEINNUMBER,
                    TConfig.c.ORE_BADLANDSGOLD_MINSPAWNHEIGHT,
                    TConfig.c.ORE_BADLANDSGOLD_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_BADLANDSGOLD_MAXSPAWNHEIGHT,
                    false,
                    BiomeBank.BADLANDS,
                    BiomeBank.BADLANDS_CANYON,
                    BiomeBank.BADLANDS_CANYON_PEAK,
                    BiomeBank.BADLANDS_BEACH,
                    BiomeBank.BADLANDS_RIVER
            ),

            new OrePopulator(Material.DIAMOND_ORE,
                    TConfig.c.ORE_DIAMOND_CHANCE,
                    TConfig.c.ORE_DIAMOND_VEINSIZE,
                    TConfig.c.ORE_DIAMOND_MAXVEINNUMBER,
                    TConfig.c.ORE_DIAMOND_MINSPAWNHEIGHT,
                    TConfig.c.ORE_DIAMOND_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_DIAMOND_MAXSPAWNHEIGHT,
                    false
            ),


            // Emeralds only spawn in mountainous biomes (except deserts)
            new OrePopulator(Material.EMERALD_ORE,
                    TConfig.c.ORE_EMERALD_CHANCE,
                    TConfig.c.ORE_EMERALD_VEINSIZE,
                    TConfig.c.ORE_EMERALD_MAXVEINNUMBER,
                    TConfig.c.ORE_EMERALD_MINSPAWNHEIGHT,
                    TConfig.c.ORE_EMERALD_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_EMERALD_MAXSPAWNHEIGHT,
                    false,
                    BiomeBank.BIRCH_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.SNOWY_MOUNTAINS,
                    BiomeBank.FORESTED_MOUNTAINS,
                    BiomeBank.COLD_JAGGED_PEAKS,
                    BiomeBank.JAGGED_PEAKS,
                    BiomeBank.FORESTED_PEAKS
            ),


            new OrePopulator(Material.LAPIS_ORE,
                    TConfig.c.ORE_LAPIS_CHANCE,
                    TConfig.c.ORE_LAPIS_VEINSIZE,
                    TConfig.c.ORE_LAPIS_MAXVEINNUMBER,
                    TConfig.c.ORE_LAPIS_MINSPAWNHEIGHT,
                    TConfig.c.ORE_LAPIS_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_LAPIS_MAXSPAWNHEIGHT,
                    false
            ),

            new OrePopulator(Material.REDSTONE_ORE,
                    TConfig.c.ORE_REDSTONE_CHANCE,
                    TConfig.c.ORE_REDSTONE_VEINSIZE,
                    TConfig.c.ORE_REDSTONE_MAXVEINNUMBER,
                    TConfig.c.ORE_REDSTONE_MINSPAWNHEIGHT,
                    TConfig.c.ORE_REDSTONE_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_REDSTONE_MAXSPAWNHEIGHT,
                    false
            ),


            // Non-ores
            new OrePopulator(Material.GRAVEL,
                    TConfig.c.ORE_GRAVEL_CHANCE,
                    TConfig.c.ORE_GRAVEL_VEINSIZE,
                    TConfig.c.ORE_GRAVEL_MAXVEINNUMBER,
                    TConfig.c.ORE_GRAVEL_MINSPAWNHEIGHT,
                    TConfig.c.ORE_GRAVEL_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_GRAVEL_MAXSPAWNHEIGHT,
                    true
            ),

            new OrePopulator(Material.ANDESITE,
                    TConfig.c.ORE_ANDESITE_CHANCE,
                    TConfig.c.ORE_ANDESITE_VEINSIZE,
                    TConfig.c.ORE_ANDESITE_MAXVEINNUMBER,
                    TConfig.c.ORE_ANDESITE_MINSPAWNHEIGHT,
                    TConfig.c.ORE_ANDESITE_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_ANDESITE_MAXSPAWNHEIGHT,
                    true
            ),

            new OrePopulator(Material.DIORITE,
                    TConfig.c.ORE_DIORITE_CHANCE,
                    TConfig.c.ORE_DIORITE_VEINSIZE,
                    TConfig.c.ORE_DIORITE_MAXVEINNUMBER,
                    TConfig.c.ORE_DIORITE_MINSPAWNHEIGHT,
                    TConfig.c.ORE_DIORITE_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_DIORITE_MAXSPAWNHEIGHT,
                    true
            ),

            new OrePopulator(Material.GRANITE,
                    TConfig.c.ORE_GRANITE_CHANCE,
                    TConfig.c.ORE_GRANITE_VEINSIZE,
                    TConfig.c.ORE_GRANITE_MAXVEINNUMBER,
                    TConfig.c.ORE_GRANITE_MINSPAWNHEIGHT,
                    TConfig.c.ORE_GRANITE_COMMONSPAWNHEIGHT,
                    TConfig.c.ORE_GRANITE_MAXSPAWNHEIGHT,
                    true
            )
    };

    private final AmethystGeodePopulator amethystGeodePopulator = new AmethystGeodePopulator(
            TConfig.c.ORE_AMETHYST_GEODE_SIZE,
            TConfig.c.ORE_AMETHYST_CHANCE,
            TConfig.c.ORE_AMETHYST_MIN_DEPTH,
            TConfig.c.ORE_AMETHYST_MIN_DEPTH_BELOW_SURFACE
    );
    private final MasterCavePopulatorDistributor caveDistributor = new MasterCavePopulatorDistributor();

    @Override
    public void populate(@NotNull org.bukkit.generator.WorldInfo worldInfo,
                         @NotNull java.util.Random random,
                         int chunkX,
                         int chunkZ,
                         @NotNull org.bukkit.generator.LimitedRegion limitedRegion)
    {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(), worldInfo.getSeed());
        PopulatorDataAbstract data = new PopulatorDataSpigotAPI(limitedRegion, tw, chunkX, chunkZ);
        this.populate(tw, data);
    }

    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        Random random = tw.getHashedRand(571162, data.getChunkX(), data.getChunkZ());

        // ores
        for (OrePopulator ore : ORE_POPS) {
            ore.populate(tw, random, data);
        }
        
        // Get all biomes in a chunk
        EnumSet<BiomeBank> banks = EnumSet.noneOf(BiomeBank.class);

        boolean[] canDecorate = StructureBufferDistanceHandler.canDecorateChunk(tw, data.getChunkX(), data.getChunkZ());

        // Amethysts
        if(canDecorate[1])
            amethystGeodePopulator.populate(tw, random, data);

        // Small Populators run per block.
        for (int rawX = data.getChunkX() * 16; rawX <= data.getChunkX() * 16 + 16; rawX++) {
            for (int rawZ = data.getChunkZ() * 16; rawZ <= data.getChunkZ() * 16 + 16; rawZ++) {
                int surfaceY = GenUtils.getTransformedHeight(data.getTerraformWorld(), rawX, rawZ);
                BiomeBank bank = tw.getBiomeBank(rawX, surfaceY, rawZ);
                banks.add(bank);

                // Don't populate wet stuff in places that aren't wet
                if (!bank.isDry() && data.getType(rawX, surfaceY + 1, rawZ) != Material.WATER) {
                    continue;
                }
                bank.getHandler().populateSmallItems(tw, random, rawX, surfaceY, rawZ, data);
            }
        }

        // Only decorate disruptive features if the structures allow for them
        if (canDecorate[0]) {
            for (BiomeBank bank : banks) {
                bank.getHandler().populateLargeItems(tw, random, data);
            }
        }


        // Cave populators
        // They will recalculate biomes per block.
        caveDistributor.populate(tw, random, data, canDecorate[1]);

        // Multi-megachunk structures
        for (MultiMegaChunkStructurePopulator spop : StructureRegistry.smallStructureRegistry) {
            if (TConfig.areStructuresEnabled() && spop.canSpawn(tw, data.getChunkX(), data.getChunkZ())) {
                TerraformGeneratorPlugin.logger.info("Generating "
                                                     + spop.getClass().getName()
                                                     + " at chunk: "
                                                     + data.getChunkX()
                                                     + ","
                                                     + data.getChunkZ());

                // No async events
                // Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(data.getChunkX()*16+8, data.getChunkZ()*16+8, spop.getClass().getName()));

                spop.populate(tw, data);
            }
        }
    }
}
