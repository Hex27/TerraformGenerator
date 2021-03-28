package org.terraform.biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import org.terraform.biome.beach.BadlandsBeachHandler;
import org.terraform.biome.beach.IcyBeachHandler;
import org.terraform.biome.beach.MudflatsHandler;
import org.terraform.biome.beach.RockBeachHandler;
import org.terraform.biome.beach.SandyBeachHandler;
import org.terraform.biome.cave.AbstractCavePopulator;
import org.terraform.biome.cave.FrozenCavePopulator;
import org.terraform.biome.cave.MossyCavePopulator;
import org.terraform.biome.flat.BadlandsHandler;
import org.terraform.biome.flat.BambooForestHandler;
import org.terraform.biome.flat.DarkForestHandler;
import org.terraform.biome.flat.DesertHandler;
import org.terraform.biome.flat.ErodedPlainsHandler;
import org.terraform.biome.flat.ForestHandler;
import org.terraform.biome.flat.IceSpikesHandler;
import org.terraform.biome.flat.JungleHandler;
import org.terraform.biome.flat.PlainsHandler;
import org.terraform.biome.flat.SavannaHandler;
import org.terraform.biome.flat.SnowyTaigaHandler;
import org.terraform.biome.flat.SnowyWastelandHandler;
import org.terraform.biome.flat.TaigaHandler;
import org.terraform.biome.mountainous.BadlandsMountainHandler;
import org.terraform.biome.mountainous.BirchMountainsHandler;
import org.terraform.biome.mountainous.DesertMountainHandler;
import org.terraform.biome.mountainous.RockyMountainsHandler;
import org.terraform.biome.mountainous.SnowyMountainsHandler;
import org.terraform.biome.ocean.BlackOceansHandler;
import org.terraform.biome.ocean.ColdOceansHandler;
import org.terraform.biome.ocean.FrozenOceansHandler;
import org.terraform.biome.ocean.LukewarmOceansHandler;
import org.terraform.biome.ocean.OceansHandler;
import org.terraform.biome.ocean.SwampHandler;
import org.terraform.biome.ocean.WarmOceansHandler;
import org.terraform.biome.river.FrozenRiverHandler;
import org.terraform.biome.river.JungleRiverHandler;
import org.terraform.biome.river.RiverHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TWSimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public enum BiomeBank {
    //MOUNTAINOUS
    ROCKY_MOUNTAINS(new RockyMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.TRANSITION ,1),
    BADLANDS_MOUNTAINS(new BadlandsMountainHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.HOT_BARREN, 1),
    SNOWY_MOUNTAINS(new SnowyMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.SNOWY, 1, new FrozenCavePopulator()),
    BIRCH_MOUNTAINS(new BirchMountainsHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.COLD, 1),
    DESERT_MOUNTAINS(new DesertMountainHandler(), BiomeType.MOUNTAINOUS, BiomeClimate.HOT_BARREN, 1),

    //OCEANIC
    OCEAN(new OceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.TRANSITION, 5),
    BLACK_OCEAN(new BlackOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.TRANSITION, 1),
    COLD_OCEAN(new ColdOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.COLD, 5),
    FROZEN_OCEAN(new FrozenOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.SNOWY, 5, new FrozenCavePopulator()),
    WARM_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.HOT_BARREN, 5),
    HUMID_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.HUMID_VEGETATION, 5),
    DRY_OCEAN(new WarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.DRY_VEGETATION, 5),
    LUKEWARM_OCEAN(new LukewarmOceansHandler(BiomeType.OCEANIC), BiomeType.OCEANIC, BiomeClimate.WARM_VEGETATION, 5),
    
    //RIVERS (Don't include in selectBiome)
    //Rivers are handled specially and will not be allocated in selectBiome
    RIVER(new RiverHandler(), BiomeType.RIVER, BiomeClimate.TRANSITION), 
    JUNGLE_RIVER(new JungleRiverHandler(), BiomeType.RIVER, BiomeClimate.HUMID_VEGETATION),
    FROZEN_RIVER(new FrozenRiverHandler(), BiomeType.RIVER, BiomeClimate.SNOWY, new FrozenCavePopulator()), //Special case, handle later

    //DEEP OCEANIC
    DEEP_OCEAN(new OceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.TRANSITION, 5),
    DEEP_COLD_OCEAN(new ColdOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.COLD, 5),
    DEEP_BLACK_OCEAN(new BlackOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.TRANSITION, 1),
    DEEP_FROZEN_OCEAN(new FrozenOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.SNOWY, 5, new FrozenCavePopulator()),
    DEEP_WARM_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.HOT_BARREN, 5),
    DEEP_HUMID_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.HUMID_VEGETATION, 5),
    DEEP_DRY_OCEAN(new WarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.DRY_VEGETATION, 5),
    DEEP_LUKEWARM_OCEAN(new LukewarmOceansHandler(BiomeType.DEEP_OCEANIC), BiomeType.DEEP_OCEANIC, BiomeClimate.WARM_VEGETATION, 5),

    //FLAT
    PLAINS(new PlainsHandler(), BiomeType.FLAT, BiomeClimate.TRANSITION, 3),
    ERODED_PLAINS(new ErodedPlainsHandler(), BiomeType.FLAT, BiomeClimate.COLD, 3),
    SAVANNA(new SavannaHandler(), BiomeType.FLAT, BiomeClimate.DRY_VEGETATION, 3),
    FOREST(new ForestHandler(), BiomeType.FLAT, BiomeClimate.WARM_VEGETATION, 3),
    DESERT(new DesertHandler(), BiomeType.FLAT, BiomeClimate.HOT_BARREN, 3),
    JUNGLE(new JungleHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, 3),
    BAMBOO_FOREST(new BambooForestHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, 1),
    BADLANDS(new BadlandsHandler(), BiomeType.FLAT, BiomeClimate.HOT_BARREN, 1),
    TAIGA(new TaigaHandler(), BiomeType.FLAT, BiomeClimate.COLD, 3),
    SNOWY_TAIGA(new SnowyTaigaHandler(), BiomeType.FLAT, BiomeClimate.SNOWY, 3, new FrozenCavePopulator()),
    SNOWY_WASTELAND(new SnowyWastelandHandler(), BiomeType.FLAT, BiomeClimate.SNOWY, 3, new FrozenCavePopulator()),
    ICE_SPIKES(new IceSpikesHandler(), BiomeType.FLAT, BiomeClimate.SNOWY, 3, new FrozenCavePopulator()),
    DARK_FOREST(new DarkForestHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, 3),
    SWAMP(new SwampHandler(), BiomeType.FLAT, BiomeClimate.HUMID_VEGETATION, 3),

    //BEACHES (Don't include in selectBiome)
    SANDY_BEACH(new SandyBeachHandler(), BiomeType.BEACH, BiomeClimate.TRANSITION),
    BADLANDS_BEACH(new BadlandsBeachHandler(), BiomeType.BEACH, BiomeClimate.HOT_BARREN),
    ROCKY_BEACH(new RockBeachHandler(), BiomeType.BEACH, BiomeClimate.COLD),
    ICY_BEACH(new IcyBeachHandler(), BiomeType.BEACH, BiomeClimate.SNOWY, new FrozenCavePopulator()),
    MUDFLATS(new MudflatsHandler(), BiomeType.BEACH, BiomeClimate.HUMID_VEGETATION), //Special case, handle later
    ;
    public static final BiomeBank[] VALUES = values();
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
    		.maximumSize(1000).build(new HeightIndependentBiomeCacheLoader());
    
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
    	try {
    		sect = BIOMESECTION_CACHE.get(sect);
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
    	try {
    		sect = BIOMESECTION_CACHE.get(sect);
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
    	try {
    		sect = BIOMESECTION_CACHE.get(sect);
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
    	BiomeBank bank = calculateHeightIndependentBiome(tw, rawX, rawZ);
    	Random locationBasedRandom  = new Random(Objects.hash(tw.getSeed(),rawX,rawZ));
    	
    	//If calculated height is less than sea level, but more than sea level after
    	//adding back river height, it means that the river height
    	//carved dry land into the sea level.
    	//That's a river.
        if(height < TerraformGenerator.seaLevel 
        		&& height + HeightMap.getRawRiverDepth(tw, rawX, rawZ) >= TerraformGenerator.seaLevel) {
        	bank = bank.getHandler().getRiverType();
        
    	//If the height is at, or slightly higher than, sea level,
    	//it is a beach.
        }else if(height >= TerraformGenerator.seaLevel 
        		&& height <= TerraformGenerator.seaLevel + locationBasedRandom.nextInt(5)) {
        	bank = bank.getHandler().getBeachType();
        }
        
        //Correct submerged biomes. They'll be rivers.
        //Exclude swamps from this check, as swamps are submerged.
        if(bank != BiomeBank.SWAMP
        		&& height < TerraformGenerator.seaLevel 
        		&& (bank.getType() == BiomeType.FLAT || bank.getType() == BiomeType.MOUNTAINOUS)) {
        	bank = bank.getHandler().getRiverType();
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
     * @param height Ranges between 0-255
     * @return a biome type
     */
    public static BiomeBank calculateHeightIndependentBiome(TerraformWorld tw, int x, int z) {
    	TWSimpleLocation loc = new TWSimpleLocation(tw,x,0,z);
    	BiomeBank bank;
    	try {
    		bank = HEIGHTINDEPENDENTBIOME_CACHE.get(loc);
    	}
    	catch(Throwable e) 
    	{
    		e.printStackTrace();
    		bank = BiomeBank.PLAINS;
    	}
    	return bank;
    }
    
    //private static boolean debugged = false;
    
    /**
     * Used to get a biomebank from temperature and moisture values.
     * @param section
     * @param temperature
     * @param moisture
     * @return
     */
    public static BiomeBank selectBiome(BiomeSection section, double temperature, double moisture) {
    	Random sectionRand = section.getSectionRandom();
    	
    	BiomeBank defaultBiome = BiomeBank.PLAINS;
    	BiomeType targetType = null;
    	BiomeClimate climate = BiomeClimate.selectClimate(temperature, moisture);
    	
    	double oceanNoise = section.getTw().getOceanOctave().GetNoise(section.getX(),section.getZ());
    	oceanNoise = oceanNoise*50.0;
    	if(oceanNoise < 0) oceanNoise = 0;
    	
    	if(oceanNoise >= 22f){//TConfigOption.HEIGHT_MAP_DEEP_OCEANIC_THRESHOLD.getFloat()) {
    		targetType = BiomeType.DEEP_OCEANIC;
    	}else if(oceanNoise >= 20f){//TConfigOption.HEIGHT_MAP_OCEANIC_THRESHOLD.getFloat()) {
    		targetType = BiomeType.OCEANIC;
    	}
    	
    	ArrayList<BiomeBank> contenders = new ArrayList<>();
    	for(BiomeBank biome:BiomeBank.values()) {
    		if(biome.biomeWeight <= 0) continue;
    		if(targetType != null) {
    			if(targetType != biome.getType())
    				continue;
    			
    			//Oceans only spawn with biome noise.
    		}else if(biome.getType() == BiomeType.DEEP_OCEANIC 
    				|| biome.getType() == BiomeType.OCEANIC) {
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
    		TerraformGeneratorPlugin.logger.info("Defaulted to plains: " + temperature + " : " + moisture + "," + climate + ":" + targetType);
    		return defaultBiome;
    	}else
    		return contenders.get(0);
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

}
