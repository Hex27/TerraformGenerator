package org.terraform.biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.terraform.biome.beach.*;
import org.terraform.biome.cave.*;
import org.terraform.biome.flat.*;
import org.terraform.biome.mountainous.*;
import org.terraform.biome.ocean.*;
import org.terraform.biome.river.*;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TWSimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public enum BiomeBank {
    //MOUNTAINOUS
    SNOWY_MOUNTAINS(new SnowyMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.SNOWY, TConfigOption.BIOME_SNOWY_MOUNTAINS_WEIGHT.getInt(), new FrozenCavePopulator()),
    BIRCH_MOUNTAINS(new BirchMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.COLD, TConfigOption.BIOME_BIRCH_MOUNTAINS_WEIGHT.getInt()),
    ROCKY_MOUNTAINS(new RockyMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.TRANSITION, TConfigOption.BIOME_ROCKY_MOUNTAINS_WEIGHT.getInt()),
    FORESTED_MOUNTAINS(new ForestedMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_FORESTED_MOUNTAINS_WEIGHT.getInt(), new ForestedMountainsCavePopulator()),
    SHATTERED_SAVANNA(new ShatteredSavannaHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_SHATTERED_SAVANNA_WEIGHT.getInt()),
    PAINTED_HILLS(new PaintedHillsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_PAINTED_HILLS_WEIGHT.getInt()),
    BADLANDS_CANYON(new BadlandsCanyonHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_BADLANDS_MOUNTAINS_WEIGHT.getInt()),
    //For now, disabled by default.
    DESERT_MOUNTAINS(new DesertHillsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_DESERT_MOUNTAINS_WEIGHT.getInt()),

    //HIGH MOUNTAINOUS
    JAGGED_PEAKS(new JaggedPeaksHandler(), BiomeType.HIGH_MOUNTAINOUS, BiomeClimate.SNOWY, TConfigOption.BIOME_JAGGED_PEAKS_WEIGHT.getInt(), new FrozenCavePopulator()),
    COLD_JAGGED_PEAKS(new JaggedPeaksHandler(), BiomeType.HIGH_MOUNTAINOUS, BiomeClimate.COLD, TConfigOption.BIOME_JAGGED_PEAKS_WEIGHT.getInt(), new FrozenCavePopulator()),
    TRANSITION_JAGGED_PEAKS(new JaggedPeaksHandler(), BiomeType.HIGH_MOUNTAINOUS, BiomeClimate.TRANSITION, TConfigOption.BIOME_JAGGED_PEAKS_WEIGHT.getInt(), new FrozenCavePopulator()),
    FORESTED_PEAKS(new ForestedMountainsHandler(), BiomeType.HIGH_MOUNTAINOUS, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_FORESTED_MOUNTAINS_WEIGHT.getInt(), new ForestedMountainsCavePopulator()),
    SHATTERED_SAVANNA_PEAK(new ShatteredSavannaHandler(), BiomeType.HIGH_MOUNTAINOUS, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_SHATTERED_SAVANNA_WEIGHT.getInt()),    
    BADLANDS_CANYON_PEAK(new BadlandsCanyonHandler(), BiomeType.HIGH_MOUNTAINOUS, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_BADLANDS_MOUNTAINS_WEIGHT.getInt()),    
    
    //OCEANIC
    OCEAN(new OceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.TRANSITION, TConfigOption.BIOME_OCEAN_WEIGHT.getInt()),
    BLACK_OCEAN(new BlackOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.TRANSITION, TConfigOption.BIOME_BLACK_OCEAN_WEIGHT.getInt()),
    COLD_OCEAN(new ColdOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.COLD, TConfigOption.BIOME_COLD_OCEAN_WEIGHT.getInt()),
    FROZEN_OCEAN(new FrozenOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.SNOWY, TConfigOption.BIOME_FROZEN_OCEAN_WEIGHT.getInt(), new FrozenCavePopulator()),
    WARM_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_WARM_OCEAN_WEIGHT.getInt()),
    HUMID_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_HUMID_OCEAN_WEIGHT.getInt()),
    DRY_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_DRY_OCEAN_WEIGHT.getInt()),
    CORAL_REEF_OCEAN(new CoralReefOceanHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_CORALREEF_OCEAN_WEIGHT.getInt()),
    
    //RIVERS (Don't include in selectBiome)
    //Rivers are handled specially and will not be allocated in selectBiome
    RIVER(new RiverHandler(), BiomeType.RIVER, BiomeClimate.TRANSITION), 
    BOG_RIVER(new BogRiverHandler(), BiomeType.RIVER, BiomeClimate.DRY_VEGETATION), 
    CHERRY_GROVE_RIVER(new CherryGroveRiverHandler(), BiomeType.RIVER, BiomeClimate.COLD), 
    SCARLET_FOREST_RIVER(new ScarletForestRiverHandler(), BiomeType.RIVER, BiomeClimate.COLD), 
    JUNGLE_RIVER(new JungleRiverHandler(), BiomeType.RIVER, BiomeClimate.HUMID_VEGETATION),
    FROZEN_RIVER(new FrozenRiverHandler(), BiomeType.RIVER, BiomeClimate.SNOWY, new FrozenCavePopulator()), //Special case, handle later
    DARK_FOREST_RIVER(new DarkForestRiverHandler(), BiomeType.RIVER, BiomeClimate.HUMID_VEGETATION, new FrozenCavePopulator()), //Special case, handle later
    DESERT_RIVER(new DesertRiverHandler(), BiomeType.RIVER, BiomeClimate.HOT_BARREN),
    BADLANDS_RIVER(new BadlandsRiverHandler(), BiomeType.RIVER, BiomeClimate.HOT_BARREN),
    
    //DEEP OCEANIC
    DEEP_OCEAN(new OceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.TRANSITION, TConfigOption.BIOME_DEEP_OCEAN_WEIGHT.getInt()),
    DEEP_COLD_OCEAN(new ColdOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.COLD, TConfigOption.BIOME_DEEP_COLD_OCEAN_WEIGHT.getInt()),
    DEEP_BLACK_OCEAN(new BlackOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.TRANSITION, TConfigOption.BIOME_DEEP_BLACK_OCEAN_WEIGHT.getInt()),
    DEEP_FROZEN_OCEAN(new FrozenOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.SNOWY, TConfigOption.BIOME_DEEP_FROZEN_OCEAN_WEIGHT.getInt(), new FrozenCavePopulator()),
    DEEP_WARM_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_DEEP_WARM_OCEAN_WEIGHT.getInt()),
    DEEP_HUMID_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_DEEP_HUMID_OCEAN_WEIGHT.getInt()),
    DEEP_DRY_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_DEEP_DRY_OCEAN_WEIGHT.getInt()),
    DEEP_LUKEWARM_OCEAN(new LukewarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_DEEP_LUKEWARM_OCEAN_WEIGHT.getInt()),
    MUSHROOM_ISLANDS(new MushroomIslandHandler(), BiomeType.DEEP_OCEANIC, BiomeClimate.TRANSITION, TConfigOption.BIOME_MUSHROOM_ISLAND_WEIGHT.getInt()),

    //FLAT
    PLAINS(new PlainsHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfigOption.BIOME_PLAINS_WEIGHT.getInt()),
    ELEVATED_PLAINS(new ElevatedPlainsHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfigOption.BIOME_PLAINS_WEIGHT.getInt()),
    GORGE(new GorgeHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfigOption.BIOME_GORGE_WEIGHT.getInt()),
    PETRIFIED_CLIFFS(new PetrifiedCliffsHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfigOption.BIOME_PETRIFIEDCLIFFS_WEIGHT.getInt()),
    ARCHED_CLIFFS(new ArchedCliffsHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, TConfigOption.BIOME_PETRIFIEDCLIFFS_WEIGHT.getInt()),
    SAVANNA(new SavannaHandler(), BiomeType.FLAT, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_SAVANNA_WEIGHT.getInt()),
    MUDDY_BOG(new MuddyBogHandler(), BiomeType.FLAT, BiomeClimate.DRY_VEGETATION, TConfigOption.BIOME_MUDDYBOG_WEIGHT.getInt()),
    FOREST(new ForestHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_FOREST_WEIGHT.getInt()),
    JUNGLE(new JungleHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_JUNGLE_WEIGHT.getInt()),
    BAMBOO_FOREST(new BambooForestHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_BAMBOO_FOREST_WEIGHT.getInt()),
    DESERT(new DesertHandler(), BiomeType.FLAT, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_DESERT_WEIGHT.getInt()),
    BADLANDS(new BadlandsHandler(), BiomeType.FLAT, BiomeClimate.HOT_BARREN, TConfigOption.BIOME_BADLANDS_WEIGHT.getInt()),
    ERODED_PLAINS(new ErodedPlainsHandler(), BiomeType.FLAT, BiomeClimate.COLD, TConfigOption.BIOME_ERODED_PLAINS_WEIGHT.getInt()),
    SCARLET_FOREST(new ScarletForestHandler(), BiomeType.FLAT, BiomeClimate.COLD, TConfigOption.BIOME_SCARLETFOREST_WEIGHT.getInt()),
    CHERRY_GROVE(new CherryGroveHandler(), BiomeType.FLAT, BiomeClimate.COLD, TConfigOption.BIOME_CHERRYGROVE_WEIGHT.getInt()),
    TAIGA(new TaigaHandler(), BiomeType.FLAT, BiomeClimate.COLD, TConfigOption.BIOME_TAIGA_WEIGHT.getInt()),
    SNOWY_TAIGA(new SnowyTaigaHandler(), BiomeType.FLAT, BiomeClimate.SNOWY, TConfigOption.BIOME_SNOWY_TAIGA_WEIGHT.getInt(), new FrozenCavePopulator()),
    SNOWY_WASTELAND(new SnowyWastelandHandler(), BiomeType.FLAT, BiomeClimate.SNOWY, TConfigOption.BIOME_SNOWY_WASTELAND_WEIGHT.getInt(), new FrozenCavePopulator()),
    ICE_SPIKES(new IceSpikesHandler(), BiomeType.FLAT, BiomeClimate.SNOWY, TConfigOption.BIOME_ICE_SPIKES_WEIGHT.getInt(), new FrozenCavePopulator()),
    DARK_FOREST(new DarkForestHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_DARK_FOREST_WEIGHT.getInt()),
    SWAMP(new SwampHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, TConfigOption.BIOME_SWAMP_WEIGHT.getInt()),

    //BEACHES (Don't include in selectBiome)
    SANDY_BEACH(new SandyBeachHandler(), BiomeType.BEACH, BiomeClimate.TRANSITION),
    BOG_BEACH(new BogBeachHandler(), BiomeType.BEACH, BiomeClimate.DRY_VEGETATION),
    DARK_FOREST_BEACH(new DarkForestBeachHandler(), BiomeType.BEACH, BiomeClimate.HUMID_VEGETATION),
    BADLANDS_BEACH(new BadlandsBeachHandler(), BiomeType.BEACH, BiomeClimate.HOT_BARREN),
    MUSHROOM_BEACH(new MushroomBeachHandler(), BiomeType.BEACH, BiomeClimate.TRANSITION),
    BLACK_OCEAN_BEACH(new BlackOceanBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    ROCKY_BEACH(new RockBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    ICY_BEACH(new IcyBeachHandler(), BiomeType.BEACH, BiomeClimate.SNOWY, new FrozenCavePopulator()),
    MUDFLATS(new MudflatsHandler(), BiomeType.BEACH, BiomeClimate.HUMID_VEGETATION), //Special case, handle later
    CHERRY_GROVE_BEACH(new CherryGroveBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    SCARLET_FOREST_BEACH(new ScarletForestBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
 ;
    public static final BiomeBank[] VALUES = values();
    public static boolean debugPrint = false;
    public static final ArrayList<BiomeBank> FLAT = new ArrayList<BiomeBank>() {{
    	for(BiomeBank b:VALUES) {
    		if(b.getType() == BiomeType.FLAT)
    			add(b);
    	}
    }};
    private final BiomeHandler handler;
    private final BiomeType type;
    private final AbstractCavePopulator cavePop;
    private final BiomeClimate climate;
    private int biomeWeight;

    private static final LoadingCache<BiomeSection, BiomeSection> BIOMESECTION_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(250).build(new BiomeSectionCacheLoader());

    //This is the most taxing calculation. Have a bigger cache.
    private static final LoadingCache<TWSimpleLocation, BiomeBank> HEIGHTINDEPENDENTBIOME_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(500).build(new HeightIndependentBiomeCacheLoader());
    
    BiomeBank(BiomeHandler handler, BiomeType type, BiomeClimate climate) {
        this.handler = handler;
        this.type = type;
        
        this.climate = climate;
        //Impossible to pick from selectBiome.
        this.biomeWeight = 0;
        
        this.cavePop = new MossyCavePopulator();
    }
    
    BiomeBank(BiomeHandler handler, BiomeType type, BiomeClimate climate, AbstractCavePopulator cavePop) {
        this.handler = handler;
        this.type = type;
        this.climate = climate;
        
        //Impossible to pick from selectBiome.
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

    BiomeBank(BiomeHandler handler, BiomeType type, BiomeClimate climate, int biomeWeight, AbstractCavePopulator cavePop) {
        this.handler = handler;
        this.type = type;
        this.climate = climate;
        this.cavePop = cavePop;
        this.biomeWeight = biomeWeight;
    }
    
    /**
     * Block X, block Z.
     * @param tw
     * @param x
     * @param z
     * @return
     */
    public static BiomeSection getBiomeSectionFromBlockCoords(TerraformWorld tw, int x, int z) {
    	BiomeSection sect = new BiomeSection(tw,x,z);
//		sect.doCalculations();
    	try {
    		sect = BIOMESECTION_CACHE.getUnchecked(sect);
    	}
    	catch(Throwable e) 
    	{
    		e.printStackTrace();
    		sect.doCalculations();
    	}
    	return sect;
    }

    /**
     * ChunkX, ChunkZ
     * @param tw
     * @param x
     * @param z
     * @return the biome section that this chunk belongs to.
     */
    public static BiomeSection getBiomeSectionFromChunk(TerraformWorld tw, int chunkX, int chunkZ) {
    	BiomeSection sect = new BiomeSection(tw,chunkX << 4, chunkZ << 4);
//		sect.doCalculations();
    	try {
    		sect = BIOMESECTION_CACHE.getUnchecked(sect);
    	}
    	catch(Throwable e) 
    	{
    		e.printStackTrace();
    		sect.doCalculations();
    	}
    	return sect;
    }
    
    public static BiomeSection getBiomeSectionFromSectionCoords(TerraformWorld tw, int x, int z, boolean useSectionCoords) {
    	BiomeSection sect = new BiomeSection(tw,x,z,useSectionCoords);
//		sect.doCalculations();
    	try {
    		sect = BIOMESECTION_CACHE.getUnchecked(sect);
    	}
    	catch(Throwable e) 
    	{
    		e.printStackTrace();
    		sect.doCalculations();
    	}
    	return sect;
    }

    /**
     * WARNING: NOBODY SHOULD BE CALLING THIS METHOD. 
     * THIS METHOD WILL RUN ALL CALCULATIONS.
     * <br><br>
     * Use terraformWorld.getCache(...).getBiomeBank(x,y,z) instead.
     * @param tw
     * @param rawX
     * @param height
     * @param rawZ
     * @return exact biome that will appear at these coordinates
     */
    public static BiomeBank calculateBiome(TerraformWorld tw, int rawX, int height, int rawZ) {
    	if(debugPrint) 
    		TerraformGeneratorPlugin.logger.info("calculateBiome called with args: " + tw.getName() + "," + rawX + "," + height + "," + rawZ);
    	BiomeBank bank = calculateHeightIndependentBiome(tw, rawX, rawZ);
    	
    	//locationBasedRandom  = tw.getHashedRand(rawX, 0, rawZ);
    	FastNoise beachNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_BEACH_HEIGHT, (world)->{
    		FastNoise n = new FastNoise((int) world.getSeed());
    		n.SetNoiseType(NoiseType.PerlinFractal);
    		n.SetFrequency(0.01f);
    		n.SetFractalOctaves(4);
    		
    		return n;
    	});
    	//If calculated height is less than sea level, but more than sea level after
    	//adding back river height, it means that the river height
    	//carved dry land into the sea level.
    	//That's a river.
        if(height < TerraformGenerator.seaLevel 
        		&& height + HeightMap.getRawRiverDepth(tw, rawX, rawZ) >= TerraformGenerator.seaLevel) {
        	bank = bank.getHandler().getRiverType();
        	if(debugPrint) 
        		TerraformGeneratorPlugin.logger.info("calculateBiome -> River Detected");
    	//If the height is at, or slightly higher than, sea level,
    	//it is a beach.
        }else if(height >= TerraformGenerator.seaLevel 
        		&& height <= TerraformGenerator.seaLevel + 4*2*Math.abs(beachNoise.GetNoise(rawX, rawZ))) {
        	bank = bank.getHandler().getBeachType();
        	if(debugPrint) 
        		TerraformGeneratorPlugin.logger.info("calculateBiome -> Beach calculated");
        }
        
        //Correct submerged biomes. They'll be rivers.
        //Exclude swamps from this check, as swamps are submerged.
        if(bank != BiomeBank.SWAMP
        		&& height < TerraformGenerator.seaLevel 
        		&& bank.getType().isDry()){
        	bank = bank.getHandler().getRiverType();
        	if(debugPrint) 
        		TerraformGeneratorPlugin.logger.info("calculateBiome -> Biome is submerged, defaulting to river");
        }
        
        //Oceanic biomes that are above water level 
        //should be handled as the closest, most dominant dry biome, or be a beach
        
        if(!bank.getType().isDry() && height >= TerraformGenerator.seaLevel) {
        	if(debugPrint) 
        		TerraformGeneratorPlugin.logger.info("calculateBiome -> Submerged biome above ground detected");
        	BiomeBank replacement = null;
        	
        	//If the ocean handler wants to force a beach default, it will be a beach default.
            if(!bank.getHandler().forceDefaultToBeach())
        	{
        		int highestDom = Integer.MIN_VALUE;
            	for(BiomeSection sect:BiomeSection.getSurroundingSections(tw, rawX, rawZ)) {
                	if(debugPrint) 
                		TerraformGeneratorPlugin.logger.info("calculateBiome -> -> Comparison Section: " + sect.toString());
            		if(sect.getBiomeBank().getType().isDry()) {
            			int compDist = (int) sect.getDominanceBasedOnRadius(rawX, rawZ);
                    	if(debugPrint) 
                    		TerraformGeneratorPlugin.logger.info("calculateBiome -> -> -> Dominance: " + compDist);
            			if(compDist > highestDom) {
            				replacement = sect.getBiomeBank();
            				highestDom = compDist;
            			}
            		}
            	}
        	}
        	
        	//Fallback to beach if surrounding biomes are not dry
        	if(replacement == null) {
        		bank = bank.getHandler().getBeachType();
        	}
        	else
            	bank = replacement;
        	
        	if(debugPrint) 
        		TerraformGeneratorPlugin.logger.info("calculateBiome -> -> Submerged biome defaulted to: " + replacement);
        	
        }
    	if(debugPrint) 
    		TerraformGeneratorPlugin.logger.info("calculateBiome -> Evaluated: " + bank);
        
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
     * @param height Ranges between 0-255
     * @return a biome type
     */
    public static BiomeBank calculateHeightIndependentBiome(TerraformWorld tw, int x, int z) {
    	TWSimpleLocation loc = new TWSimpleLocation(tw,x,0,z);
    	BiomeBank bank;
    	try {
    		bank = HEIGHTINDEPENDENTBIOME_CACHE.getUnchecked(loc);
    	}
    	catch(Throwable e) 
    	{
    		e.printStackTrace();
    		bank = BiomeBank.PLAINS;
    	}
    	return bank;
    }
    
	public static BiomeBank singleLand = null;
	public static BiomeBank singleOcean = null;
	public static BiomeBank singleDeepOcean = null;
	public static BiomeBank singleMountain = null;
	public static BiomeBank singleHighMountain = null;
	//static BiomeBank singleRiver;
	//static BiomeBank singleBeach;
    public static void initSinglesConfig() {
    	try
    	{ singleLand = BiomeBank.valueOf(TConfigOption.BIOME_SINGLE_TERRESTRIAL_TYPE.getString().toUpperCase()); }
    	catch(IllegalArgumentException e)
    		{singleLand = null;}
    	try
    	{ singleOcean = BiomeBank.valueOf(TConfigOption.BIOME_SINGLE_OCEAN_TYPE.getString().toUpperCase()); }
    	catch(IllegalArgumentException e)
    		{singleOcean = null;}
    	try
    	{ singleDeepOcean = BiomeBank.valueOf(TConfigOption.BIOME_SINGLE_DEEPOCEAN_TYPE.getString().toUpperCase()); }
    	catch(IllegalArgumentException e)
    		{singleDeepOcean = null;}
    	try
    	{ singleMountain = BiomeBank.valueOf(TConfigOption.BIOME_SINGLE_MOUNTAIN_TYPE.getString().toUpperCase()); }
    	catch(IllegalArgumentException e)
    		{singleMountain = null;}
    	try
    	{ singleHighMountain = BiomeBank.valueOf(TConfigOption.BIOME_SINGLE_HIGHMOUNTAIN_TYPE.getString().toUpperCase()); }
    	catch(IllegalArgumentException e)
    		{singleHighMountain = null;}
    }
    
    /**
     * Does not currently work for beach and river.
     * @param bank
     * @return
     */
    public static boolean isBiomeEnabled(BiomeBank bank) {
    	
    	if(bank.getBiomeWeight() <= 0)
    		return false;
    	
    	switch(bank.getType()) {
		case BEACH:
			return true; //L
		case DEEP_OCEANIC:
			return singleDeepOcean == null || singleDeepOcean == bank;
		case FLAT:
			return singleLand == null || singleLand == bank;
		case HIGH_MOUNTAINOUS:
			return singleHighMountain == null || singleHighMountain == bank;
		case MOUNTAINOUS:
			return singleMountain == null || singleMountain == bank;
		case OCEANIC:
			return singleOcean == null || singleOcean == bank;
		case RIVER:
			return true; //L    	
    	}
    	return true;
    }
    
    /**
     * Used to get a biomebank from temperature and moisture values.
     * @param section
     * @param temperature
     * @param moisture
     * @return
     */
    public static BiomeBank selectBiome(BiomeSection section, double temperature, double moisture) {
    	Random sectionRand = section.getSectionRandom();
    	
    	BiomeType targetType = null;
    	BiomeClimate climate = BiomeClimate.selectClimate(temperature, moisture);
    	
    	double oceanicNoise = section.getOceanLevel();
    	if(oceanicNoise < 0)
    	{
    		 oceanicNoise = Math.abs(oceanicNoise);
	    	if(oceanicNoise >= TConfigOption.BIOME_DEEP_OCEANIC_THRESHOLD.getFloat()){
	    		targetType = BiomeType.DEEP_OCEANIC;
	    	}else if(oceanicNoise >= TConfigOption.BIOME_OCEANIC_THRESHOLD.getFloat()){
	    		targetType = BiomeType.OCEANIC;
	    	}
    	}
    	else
    	{
    		//If it isn't an ocean, mountains may be plausible.
    		double mountainousNoise = section.getMountainLevel();
    		if(mountainousNoise > 0) {
    			if(mountainousNoise >= TConfigOption.BIOME_HIGH_MOUNTAINOUS_THRESHOLD.getFloat()){
    	    		targetType = BiomeType.HIGH_MOUNTAINOUS;
    	    	}else if(mountainousNoise >= TConfigOption.BIOME_MOUNTAINOUS_THRESHOLD.getFloat()){
    	    		targetType = BiomeType.MOUNTAINOUS;
    	    	}
    		}
    	}
    	
    	
    	//Force types if they're set.
    	if(targetType == BiomeType.OCEANIC && singleOcean != null) {
    		return singleOcean;
    	}else if(targetType == BiomeType.DEEP_OCEANIC && singleDeepOcean != null) {
    		return singleDeepOcean;
    	}else if(targetType == null && singleLand != null) {
    		return singleLand;
    	}else if(targetType == BiomeType.MOUNTAINOUS && singleMountain != null) {
    		return singleMountain;
    	}else if(targetType == BiomeType.HIGH_MOUNTAINOUS && singleHighMountain != null) {
    		return singleHighMountain;
    	}
    	
    	ArrayList<BiomeBank> contenders = new ArrayList<>();
    	for(BiomeBank biome:BiomeBank.values()) {
    		if(biome.biomeWeight <= 0) continue;
    		if(targetType != null) {
    			if(targetType != biome.getType())
    				continue;
    			
    			//Oceans and mountains only spawn with biome noise.
    		}else if(biome.getType() == BiomeType.DEEP_OCEANIC 
    				|| biome.getType() == BiomeType.OCEANIC 
    				|| biome.getType() == BiomeType.MOUNTAINOUS
    				|| biome.getType() == BiomeType.HIGH_MOUNTAINOUS) {
    			continue;
    		}
    		
    		if(biome.climate == climate) {
    			for(int i = 0; i < biome.biomeWeight; i++)
    				contenders.add(biome);
    		}
    	}
    	
    	
    	Collections.shuffle(contenders, sectionRand);
//    	if(!debugged && contenders.contains(BiomeBank.LUKEWARM_OCEAN)) {
//    		TerraformGeneratorPlugin.logger.info("Biomes in contenders:");
//    		for(BiomeBank b:contenders)
//    			TerraformGeneratorPlugin.logger.info(" - - - " + b);
//    		debugged = true;
//    	}
    	
    	if(contenders.size() == 0) {
    		switch(targetType) {
			case BEACH:
	    		TerraformGeneratorPlugin.logger.info("Defaulted for beach: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_BEACH.getString());
			case DEEP_OCEANIC:
	    		TerraformGeneratorPlugin.logger.info("Defaulted for deep oceanic: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_DEEPOCEANIC.getString());
			case FLAT:
	    		TerraformGeneratorPlugin.logger.info("Defaulted for flat: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_FLAT.getString());
			case MOUNTAINOUS:
	    		TerraformGeneratorPlugin.logger.info("Defaulted for mountainous: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_MOUNTAINOUS.getString());
			case OCEANIC:
	    		TerraformGeneratorPlugin.logger.info("Defaulted for ocean: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_OCEANIC.getString());
			case RIVER:
	    		TerraformGeneratorPlugin.logger.info("Defaulted for river: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_RIVER.getString());
			//default:
			//	return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_FLAT.getString());
			case HIGH_MOUNTAINOUS:
				TerraformGeneratorPlugin.logger.info("Defaulted for high mountainous: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
	    		return BiomeBank.valueOf(TConfigOption.BIOME_DEFAULT_HIGHMOUNTAINOUS.getString());
			}
    	}else
    		return contenders.get(0);
		return null;
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

}
