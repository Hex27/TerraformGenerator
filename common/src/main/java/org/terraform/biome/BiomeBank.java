package org.terraform.biome;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.beach.*;
import org.terraform.biome.cavepopulators.AbstractCavePopulator;
import org.terraform.biome.cavepopulators.ForestedMountainsCavePopulator;
import org.terraform.biome.cavepopulators.FrozenCavePopulator;
import org.terraform.biome.cavepopulators.MossyCavePopulator;
import org.terraform.biome.flat.*;
import org.terraform.biome.mountainous.*;
import org.terraform.biome.ocean.*;
import org.terraform.biome.river.*;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.datastructs.ConcurrentLRUCache;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.Version;

import java.util.*;

public enum BiomeBank {
    // MOUNTAINOUS
    SNOWY_MOUNTAINS(
            new SnowyMountainsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_SNOWY_MOUNTAINS_WEIGHT,
            new FrozenCavePopulator()
    ),
    BIRCH_MOUNTAINS(new BirchMountainsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.COLD,
            TConfig.c.BIOME_BIRCH_MOUNTAINS_WEIGHT
    ),
    ROCKY_MOUNTAINS(new RockyMountainsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_ROCKY_MOUNTAINS_WEIGHT
    ),
/*    WINDSWEPT_HILLS(new WindsweptHillsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_ROCKY_MOUNTAINS_WEIGHT
    ),*/
    FORESTED_MOUNTAINS(new ForestedMountainsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_FORESTED_MOUNTAINS_WEIGHT,
            new ForestedMountainsCavePopulator()
    ),
    SHATTERED_SAVANNA(new ShatteredSavannaHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_SHATTERED_SAVANNA_WEIGHT
    ),
    PAINTED_HILLS(new PaintedHillsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_PAINTED_HILLS_WEIGHT
    ),
    BADLANDS_CANYON(new BadlandsCanyonHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.HOT_BARREN,
            TConfig.c.BIOME_BADLANDS_MOUNTAINS_WEIGHT
    ),
    // For now, disabled by default.
    DESERT_MOUNTAINS(
            new DesertHillsHandler(),
            BiomeType.MOUNTAINOUS,
            BiomeClimate.HOT_BARREN,
            TConfig.c.BIOME_DESERT_MOUNTAINS_WEIGHT
    ),

    // HIGH MOUNTAINOUS
    JAGGED_PEAKS(
            new JaggedPeaksHandler(),
            BiomeType.HIGH_MOUNTAINOUS,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_JAGGED_PEAKS_WEIGHT,
            new FrozenCavePopulator()
    ),
    COLD_JAGGED_PEAKS(new JaggedPeaksHandler(),
            BiomeType.HIGH_MOUNTAINOUS,
            BiomeClimate.COLD,
            TConfig.c.BIOME_JAGGED_PEAKS_WEIGHT,
            new FrozenCavePopulator()
    ),
    TRANSITION_JAGGED_PEAKS(new JaggedPeaksHandler(),
            BiomeType.HIGH_MOUNTAINOUS,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_JAGGED_PEAKS_WEIGHT,
            new FrozenCavePopulator()
    ),
    FORESTED_PEAKS(new ForestedMountainsHandler(),
            BiomeType.HIGH_MOUNTAINOUS,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_FORESTED_MOUNTAINS_WEIGHT,
            new ForestedMountainsCavePopulator()
    ),
    SHATTERED_SAVANNA_PEAK(new ShatteredSavannaHandler(),
            BiomeType.HIGH_MOUNTAINOUS,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_SHATTERED_SAVANNA_WEIGHT
    ),
    BADLANDS_CANYON_PEAK(new BadlandsCanyonHandler(),
            BiomeType.HIGH_MOUNTAINOUS,
            BiomeClimate.HOT_BARREN,
            TConfig.c.BIOME_BADLANDS_MOUNTAINS_WEIGHT
    ),

    // OCEANIC
    OCEAN(
            new OceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_OCEAN_WEIGHT
    ),
    BLACK_OCEAN(new BlackOceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_BLACK_OCEAN_WEIGHT
    ),
    COLD_OCEAN(new ColdOceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.COLD,
            TConfig.c.BIOME_COLD_OCEAN_WEIGHT
    ),
    FROZEN_OCEAN(new FrozenOceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_FROZEN_OCEAN_WEIGHT,
            new FrozenCavePopulator()
    ),
    WARM_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.HOT_BARREN,
            TConfig.c.BIOME_WARM_OCEAN_WEIGHT
    ),
    HUMID_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_HUMID_OCEAN_WEIGHT
    ),
    DRY_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_DRY_OCEAN_WEIGHT
    ),
    CORAL_REEF_OCEAN(new CoralReefOceanHandler(BiomeType.OCEANIC),
            BiomeType.OCEANIC,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_CORALREEF_OCEAN_WEIGHT
    ),

    // RIVERS (Don't include in selectBiome)
    // Rivers are handled specially and will not be allocated in selectBiome
    RIVER(new RiverHandler(), BiomeType.RIVER, BiomeClimate.TRANSITION),
    BOG_RIVER(new BogRiverHandler(), BiomeType.RIVER, BiomeClimate.DRY_VEGETATION),
    CHERRY_GROVE_RIVER(new CherryGroveRiverHandler(), BiomeType.RIVER, BiomeClimate.COLD),
    SCARLET_FOREST_RIVER(new ScarletForestRiverHandler(), BiomeType.RIVER, BiomeClimate.COLD),
    JUNGLE_RIVER(new JungleRiverHandler(), BiomeType.RIVER, BiomeClimate.HUMID_VEGETATION),
    FROZEN_RIVER(new FrozenRiverHandler(),
            BiomeType.RIVER,
            BiomeClimate.SNOWY,
            new FrozenCavePopulator()
    ), // Special case, handle later
    DARK_FOREST_RIVER(
            new DarkForestRiverHandler(),
            BiomeType.RIVER,
            BiomeClimate.HUMID_VEGETATION,
            new FrozenCavePopulator()
    ), // Special case, handle later
    DESERT_RIVER(new DesertRiverHandler(), BiomeType.RIVER, BiomeClimate.HOT_BARREN),
    BADLANDS_RIVER(new BadlandsRiverHandler(), BiomeType.RIVER, BiomeClimate.HOT_BARREN),

    // DEEP OCEANIC
    DEEP_OCEAN(
            new OceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_DEEP_OCEAN_WEIGHT
    ),
    DEEP_COLD_OCEAN(new ColdOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.COLD,
            TConfig.c.BIOME_DEEP_COLD_OCEAN_WEIGHT
    ),
    DEEP_BLACK_OCEAN(new BlackOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_DEEP_BLACK_OCEAN_WEIGHT
    ),
    DEEP_FROZEN_OCEAN(new FrozenOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_DEEP_FROZEN_OCEAN_WEIGHT,
            new FrozenCavePopulator()
    ),
    DEEP_WARM_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.HOT_BARREN,
            TConfig.c.BIOME_DEEP_WARM_OCEAN_WEIGHT
    ),
    DEEP_HUMID_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_DEEP_HUMID_OCEAN_WEIGHT
    ),
    DEEP_DRY_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_DEEP_DRY_OCEAN_WEIGHT
    ),
    DEEP_LUKEWARM_OCEAN(new LukewarmOceansHandler(BiomeType.DEEP_OCEANIC),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_DEEP_LUKEWARM_OCEAN_WEIGHT
    ),
    MUSHROOM_ISLANDS(new MushroomIslandHandler(),
            BiomeType.DEEP_OCEANIC,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_MUSHROOM_ISLAND_WEIGHT
    ),

    // FLAT
    PLAINS(new PlainsHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfig.c.BIOME_PLAINS_WEIGHT),
    MEADOW(new MeadowHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfig.c.BIOME_MEADOW_WEIGHT),
    ELEVATED_PLAINS(new ElevatedPlainsHandler(),
            BiomeType.FLAT,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_ELEVATED_PLAINS_WEIGHT
    ),
    GORGE(new GorgeHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfig.c.BIOME_GORGE_WEIGHT),
    PETRIFIED_CLIFFS(new PetrifiedCliffsHandler(),
            BiomeType.FLAT,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_PETRIFIEDCLIFFS_WEIGHT
    ),
    ARCHED_CLIFFS(new ArchedCliffsHandler(),
            BiomeType.FLAT,
            BiomeClimate.TRANSITION,
            TConfig.c.BIOME_ARCHED_CLIFFS_WEIGHT
    ),
    SAVANNA(new SavannaHandler(),
            BiomeType.FLAT,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_SAVANNA_WEIGHT
    ),
    MUDDY_BOG(new MuddyBogHandler(),
            BiomeType.FLAT,
            BiomeClimate.DRY_VEGETATION,
            TConfig.c.BIOME_MUDDYBOG_WEIGHT
    ),
    FOREST(new ForestHandler(),
            BiomeType.FLAT,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_FOREST_WEIGHT
    ),
    JUNGLE(new JungleHandler(),
            BiomeType.FLAT,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_JUNGLE_WEIGHT
    ),
    BAMBOO_FOREST(new BambooForestHandler(),
            BiomeType.FLAT,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_BAMBOO_FOREST_WEIGHT
    ),
    DESERT(new DesertHandler(), BiomeType.FLAT, BiomeClimate.HOT_BARREN, TConfig.c.BIOME_DESERT_WEIGHT),
    BADLANDS(new BadlandsHandler(),
            BiomeType.FLAT,
            BiomeClimate.HOT_BARREN,
            TConfig.c.BIOME_BADLANDS_WEIGHT
    ),
    ERODED_PLAINS(new ErodedPlainsHandler(),
            BiomeType.FLAT,
            BiomeClimate.COLD,
            TConfig.c.BIOME_ERODED_PLAINS_WEIGHT
    ),
    SCARLET_FOREST(new ScarletForestHandler(),
            BiomeType.FLAT,
            BiomeClimate.COLD,
            TConfig.c.BIOME_SCARLETFOREST_WEIGHT
    ),
    CHERRY_GROVE(new CherryGroveHandler(),
            BiomeType.FLAT,
            BiomeClimate.COLD,
            TConfig.c.BIOME_CHERRYGROVE_WEIGHT
    ),
    TAIGA(new TaigaHandler(), BiomeType.FLAT, BiomeClimate.COLD, TConfig.c.BIOME_TAIGA_WEIGHT),
    SNOWY_TAIGA(new SnowyTaigaHandler(),
            BiomeType.FLAT,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_SNOWY_TAIGA_WEIGHT,
            new FrozenCavePopulator()
    ),
    SNOWY_WASTELAND(new SnowyWastelandHandler(),
            BiomeType.FLAT,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_SNOWY_WASTELAND_WEIGHT,
            new FrozenCavePopulator()
    ),
    ICE_SPIKES(new IceSpikesHandler(),
            BiomeType.FLAT,
            BiomeClimate.SNOWY,
            TConfig.c.BIOME_ICE_SPIKES_WEIGHT,
            new FrozenCavePopulator()
    ),
    DARK_FOREST(new DarkForestHandler(),
            BiomeType.FLAT,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_DARK_FOREST_WEIGHT
    ),
    PALE_FOREST(new PaleForestHandler(),
            BiomeType.FLAT,
            BiomeClimate.HUMID_VEGETATION,
            (Version.isAtLeast(21.4)) ? TConfig.c.BIOME_PALE_FOREST_WEIGHT : 0
    ),
    SWAMP(new SwampHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, TConfig.c.BIOME_SWAMP_WEIGHT),
    MANGROVE(new MangroveHandler(),
            BiomeType.FLAT,
            BiomeClimate.HUMID_VEGETATION,
            TConfig.c.BIOME_MANGROVE_WEIGHT
    ),

    // BEACHES (Don't include in selectBiome)
    SANDY_BEACH(new SandyBeachHandler(), BiomeType.BEACH, BiomeClimate.TRANSITION),
    BOG_BEACH(new BogBeachHandler(), BiomeType.BEACH, BiomeClimate.DRY_VEGETATION),
    DARK_FOREST_BEACH(new DarkForestBeachHandler(), BiomeType.BEACH, BiomeClimate.HUMID_VEGETATION),
    BADLANDS_BEACH(new BadlandsBeachHandler(), BiomeType.BEACH, BiomeClimate.HOT_BARREN),
    MUSHROOM_BEACH(new MushroomBeachHandler(), BiomeType.BEACH, BiomeClimate.TRANSITION),
    BLACK_OCEAN_BEACH(new BlackOceanBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    ROCKY_BEACH(new RockBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    ICY_BEACH(new IcyBeachHandler(), BiomeType.BEACH, BiomeClimate.SNOWY, new FrozenCavePopulator()),
    MUDFLATS(new MudflatsHandler(), BiomeType.BEACH, BiomeClimate.HUMID_VEGETATION), // Special case, handle later
    CHERRY_GROVE_BEACH(new CherryGroveBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    SCARLET_FOREST_BEACH(new ScarletForestBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    ;
    private static final ConcurrentLRUCache<BiomeSection, BiomeSection> BIOMESECTION_CACHE = new ConcurrentLRUCache<>(
            "BIOMESECTION_CACHE",
            250,
            (key)->{key.doCalculations(); return key; }
    );
    // public static final BiomeBank[] VALUES = values();
    public static boolean debugPrint = false;
    public static @Nullable BiomeBank singleLand = null;
    public static @Nullable BiomeBank singleOcean = null;
    public static @Nullable BiomeBank singleDeepOcean = null;
    public static @Nullable BiomeBank singleMountain = null;
    public static @Nullable BiomeBank singleHighMountain = null;
    private final BiomeHandler handler;
    private final BiomeType type;
    private final AbstractCavePopulator cavePop;
    private final BiomeClimate climate;
    private final int biomeWeight;

    BiomeBank(BiomeHandler handler, BiomeType type, BiomeClimate climate) {
        this.handler = handler;
        this.type = type;

        this.climate = climate;
        // Impossible to pick from selectBiome.
        this.biomeWeight = 0;

        this.cavePop = new MossyCavePopulator();
    }

    BiomeBank(BiomeHandler handler, BiomeType type, BiomeClimate climate, AbstractCavePopulator cavePop) {
        this.handler = handler;
        this.type = type;
        this.climate = climate;

        // Impossible to pick from selectBiome.
        this.biomeWeight = 0;

        this.cavePop = cavePop;
    }

    BiomeBank(BiomeHandler handler, BiomeType type, BiomeClimate climate, int biomeWeight) {
        this.handler = handler;
        this.type = type;
        this.climate = climate;
        this.biomeWeight = biomeWeight;
        this.cavePop = new MossyCavePopulator();
    }

    BiomeBank(BiomeHandler handler,
              BiomeType type,
              BiomeClimate climate,
              int biomeWeight,
              AbstractCavePopulator cavePop)
    {
        this.handler = handler;
        this.type = type;
        this.climate = climate;
        this.cavePop = cavePop;
        this.biomeWeight = biomeWeight;
    }

    /**
     * @param x Block X
     * @param z Block Z
     */
    public static @NotNull BiomeSection getBiomeSectionFromBlockCoords(TerraformWorld tw, int x, int z) {
        BiomeSection sect = new BiomeSection(tw, x, z);
        //		sect.doCalculations();
        sect = BIOMESECTION_CACHE.get(sect);
        return sect;
    }

    /**
     * ChunkX, ChunkZ
     *
     * @return the biome section that this chunk belongs to.
     */
    public static @NotNull BiomeSection getBiomeSectionFromChunk(TerraformWorld tw, int chunkX, int chunkZ) {
        BiomeSection sect = new BiomeSection(tw, chunkX << 4, chunkZ << 4);
        sect = BIOMESECTION_CACHE.get(sect);

        return sect;
    }

    public static @NotNull BiomeSection getBiomeSectionFromSectionCoords(TerraformWorld tw,
                                                                         int x,
                                                                         int z,
                                                                         boolean useSectionCoords)
    {
        BiomeSection sect = new BiomeSection(tw, x, z, useSectionCoords);
        sect = BIOMESECTION_CACHE.get(sect);

        return sect;
    }

    /**
     * WARNING: NOBODY SHOULD BE CALLING THIS METHOD.
     * THIS METHOD WILL RUN ALL CALCULATIONS.
     * <br><br>
     * Use terraformWorld.getCache(...).getBiomeBank(x,y,z) instead.
     *
     * @return exact biome that will appear at these coordinates
     */
    public static @NotNull BiomeBank calculateBiome(@NotNull TerraformWorld tw, int rawX, int height, int rawZ) {
        if (debugPrint) {
            TerraformGeneratorPlugin.logger.info("calculateBiome called with args: "
                                                 + tw.getName()
                                                 + ","
                                                 + rawX
                                                 + ","
                                                 + height
                                                 + ","
                                                 + rawZ);
        }

        BiomeBank bank = calculateHeightIndependentBiome(tw, rawX, rawZ);

        // Bitshift rawX and rawZ. Biome storage is done every 4 blocks,
        // so there's no need to recalculate for every block.

        FastNoise beachNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_BEACH_HEIGHT, (world) -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.PerlinFractal);
            n.SetFrequency(0.01f);
            n.SetFractalOctaves(4);

            return n;
        });
        // If calculated height is less than sea level, but more than sea level after
        // adding back river height, it means that the river height
        // carved dry land into the sea level.
        // That's a river.
        if (height < TerraformGenerator.seaLevel
            && height + HeightMap.getRawRiverDepth(tw, rawX, rawZ) >= TerraformGenerator.seaLevel)
        {
            bank = bank.getHandler().getRiverType();
            if (debugPrint) {
                TerraformGeneratorPlugin.logger.info("calculateBiome -> River Detected");
            }
            // If the height is at, or slightly higher than, sea level,
            // it is a beach.
        }
        else if (height >= TerraformGenerator.seaLevel && height <= TerraformGenerator.seaLevel + 4 * 2 * Math.abs(
                beachNoise.GetNoise(rawX, rawZ)))
        {
            bank = bank.getHandler().getBeachType();
            if (debugPrint) {
                TerraformGeneratorPlugin.logger.info("calculateBiome -> Beach calculated");
            }
        }

        // Correct submerged biomes. They'll be rivers.
        // Exclude swamps from this check, as swamps are submerged.
        if (bank != BiomeBank.SWAMP
            && bank != BiomeBank.MANGROVE
            && height < TerraformGenerator.seaLevel
            && bank.isDry())
        {
            bank = bank.getHandler().getRiverType();
            if (debugPrint) {
                TerraformGeneratorPlugin.logger.info("calculateBiome -> Biome is submerged, defaulting to river");
            }
        }

        // Oceanic biomes that are above water level
        // should be handled as the closest, most dominant dry biome, or be a beach

        if (!bank.isDry() && height >= TerraformGenerator.seaLevel) {
            if (debugPrint) {
                TerraformGeneratorPlugin.logger.info("calculateBiome -> Submerged biome above ground detected");
            }
            BiomeBank replacement = null;

            // If the ocean handler wants to force a beach default, it will be a beach default.
            if (!bank.getHandler().forceDefaultToBeach()) {
                int highestDom = Integer.MIN_VALUE;
                for (BiomeSection sect : BiomeSection.getSurroundingSections(tw, rawX, rawZ)) {
                    if (debugPrint) {
                        TerraformGeneratorPlugin.logger.info("calculateBiome -> -> Comparison Section: "
                                                             + sect.toString());
                    }
                    if (sect.getBiomeBank().isDry()) {
                        int compDist = (int) sect.getDominanceBasedOnRadius(rawX, rawZ);
                        if (debugPrint) {
                            TerraformGeneratorPlugin.logger.info("calculateBiome -> -> -> Dominance: " + compDist);
                        }
                        if (compDist > highestDom) {
                            replacement = sect.getBiomeBank();
                            highestDom = compDist;
                        }
                    }
                }
            }

            // Fallback to beach if surrounding biomes are not dry
            bank = replacement == null ? bank.getHandler().getBeachType() : replacement;

            if (debugPrint) {
                TerraformGeneratorPlugin.logger.info("calculateBiome -> -> Submerged biome defaulted to: "
                                                     + replacement);
            }

        }
        if (debugPrint) {
            TerraformGeneratorPlugin.logger.info("calculateBiome -> Evaluated: " + bank);
        }

        return bank;
    }

    /**
     * NOBODY SHOULD BE CALLING THIS METHOD. THIS IS AN INTERNAL CALCULATION,
     * AND IT WILL NOT RETURN THE FINAL BIOME.
     * Use terraformWorld.getCache(...).getBiomeBank(x,y,z) instead.
     * <br><br>
     * Supply y with getHighestGround.
     * <br><br>
     * If for whatever reason, the biome must be calculated intead of
     * fetched from the cache, use calculateBiome(tw,x,y,z);
     *
     * @return a biome type
     */
    public static @NotNull BiomeBank calculateHeightIndependentBiome(TerraformWorld tw, int x, int z)
    {
        // This optimisation doesn't work here. Many aesthetic options rely on
        // the fact that this is block-accurate. Calculating once per 4x4 blocks
        // creates obvious ugly 4x4 artifacts
        // x = (x >> 2) << 2; z = (z >> 2) << 2;

        //There used to be a cache here, but it had an abysmal hitrate of near 0
        // when caching 32 chunks
        BiomeSection mostDominant = BiomeSection.getMostDominantSection(tw,x,z);
        return mostDominant.getBiomeBank();
    }

    public static void initSinglesConfig() {
        try {
            singleLand = BiomeBank.valueOf(TConfig.c.BIOME_SINGLE_TERRESTRIAL_TYPE
                                                                                .toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            singleLand = null;
        }
        try {
            singleOcean = BiomeBank.valueOf(TConfig.c.BIOME_SINGLE_OCEAN_TYPE
                                                                           .toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            singleOcean = null;
        }
        try {
            singleDeepOcean = BiomeBank.valueOf(TConfig.c.BIOME_SINGLE_DEEPOCEAN_TYPE
                                                                                   .toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            singleDeepOcean = null;
        }
        try {
            singleMountain = BiomeBank.valueOf(TConfig.c.BIOME_SINGLE_MOUNTAIN_TYPE
                                                                                 .toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            singleMountain = null;
        }
        try {
            singleHighMountain = BiomeBank.valueOf(TConfig.c.BIOME_SINGLE_HIGHMOUNTAIN_TYPE
                                                                                         .toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException e) {
            singleHighMountain = null;
        }
    }

    /**
     * Does not currently work for beach and river.
     */
    public static boolean isBiomeEnabled(@NotNull BiomeBank bank) {

        if (bank.getBiomeWeight() <= 0) {
            return false;
        }

        return switch (bank.getType()) {
            case BEACH, RIVER -> true; // L
            case DEEP_OCEANIC -> singleDeepOcean == null || singleDeepOcean == bank;
            case FLAT -> singleLand == null || singleLand == bank;
            case HIGH_MOUNTAINOUS -> singleHighMountain == null || singleHighMountain == bank;
            case MOUNTAINOUS -> singleMountain == null || singleMountain == bank;
            case OCEANIC -> singleOcean == null || singleOcean == bank;
            // L
        };
    }

    /**
     * Used to get a biomebank from temperature and moisture values.
     */
    public static @NotNull BiomeBank selectBiome(@NotNull BiomeSection section, double temperature, double moisture) {
        Random sectionRand = section.getSectionRandom();

        BiomeType targetType = BiomeType.FLAT;
        BiomeClimate climate = BiomeClimate.selectClimate(temperature, moisture);

        double oceanicNoise = section.getOceanLevel();
        if (oceanicNoise < 0 || TConfig.c.BIOME_OCEANIC_THRESHOLD < 0) {
            oceanicNoise = Math.abs(oceanicNoise);
            if (oceanicNoise >= TConfig.c.BIOME_DEEP_OCEANIC_THRESHOLD) {
                targetType = BiomeType.DEEP_OCEANIC;
            }
            else if (oceanicNoise >= TConfig.c.BIOME_OCEANIC_THRESHOLD) {
                targetType = BiomeType.OCEANIC;
            }
        }
        else {
            // If it isn't an ocean, mountains may be plausible.
            double mountainousNoise = section.getMountainLevel();
            if (mountainousNoise > 0) {
                if (mountainousNoise >= TConfig.c.BIOME_HIGH_MOUNTAINOUS_THRESHOLD) {
                    targetType = BiomeType.HIGH_MOUNTAINOUS;
                }
                else if (mountainousNoise >= TConfig.c.BIOME_MOUNTAINOUS_THRESHOLD) {
                    targetType = BiomeType.MOUNTAINOUS;
                }
            }
        }


        // Force types if they're set.
        switch(targetType){
            case FLAT -> { if(singleLand != null) return singleLand; }
            case OCEANIC -> { if(singleOcean != null) return singleOcean; }
            case DEEP_OCEANIC -> { if(singleDeepOcean != null) return singleDeepOcean; }
            case MOUNTAINOUS -> { if(singleMountain != null) return singleMountain; }
            case HIGH_MOUNTAINOUS -> { if(singleHighMountain != null) return singleHighMountain; }
        }

        ArrayList<BiomeBank> contenders = new ArrayList<>();
        for (BiomeBank biome : BiomeBank.values()) {
            //Excludes beaches and rivers
            if (biome.biomeWeight <= 0)
                continue;
            if (biome.getType() != targetType)
                continue;
            if (biome.climate == climate) {
                for (int i = 0; i < biome.biomeWeight; i++) {
                    contenders.add(biome);
                }
            }
        }

        Collections.shuffle(contenders, sectionRand);

        if (contenders.isEmpty()) {
            TerraformGeneratorPlugin.logger.info("Defaulted for: "
                                                 + temperature
                                                 + " : "
                                                 + moisture
                                                 + ","
                                                 + climate
                                                 + ":"
                                                 + targetType);
            return switch (targetType) {
                case BEACH -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_BEACH);
                case DEEP_OCEANIC -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_DEEPOCEANIC);
                case FLAT -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_FLAT);
                case MOUNTAINOUS -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_MOUNTAINOUS);
                case OCEANIC -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_OCEANIC);
                case RIVER -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_RIVER);
                case HIGH_MOUNTAINOUS -> BiomeBank.valueOf(TConfig.c.BIOME_DEFAULT_HIGHMOUNTAINOUS);
            };
        }
        else {
            return contenders.get(0);
        }
    }

    /**
     * @return the cavePop
     */
    public AbstractCavePopulator getCavePop() {
        return cavePop;
    }

    public BiomeType getType() {
        return type;
    }

    /**
     * @return the handler
     */
    public BiomeHandler getHandler() {
        return handler;
    }

    public BiomeClimate getClimate() {
        // TODO Auto-generated method stub
        return climate;
    }

    public int getBiomeWeight() {
        return biomeWeight;
    }

    public boolean isDry() {
        return getType().isDry();
    }
}
