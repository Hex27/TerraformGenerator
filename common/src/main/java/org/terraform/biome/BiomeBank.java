package org.terraform.biome;

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
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.GenUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public enum BiomeBank {
    //MOUNTAINOUS
    ROCKY_MOUNTAINS(new RockyMountainsHandler(), BiomeType.MOUNTAINOUS),
    BADLANDS_MOUNTAINS(new BadlandsMountainHandler(), BiomeType.MOUNTAINOUS),
    SNOWY_MOUNTAINS(new SnowyMountainsHandler(), BiomeType.MOUNTAINOUS, new FrozenCavePopulator()),
    BIRCH_MOUNTAINS(new BirchMountainsHandler(), BiomeType.MOUNTAINOUS),
    DESERT_MOUNTAINS(new DesertMountainHandler(), BiomeType.MOUNTAINOUS),

    //OCEANIC
    OCEAN(new OceansHandler(), BiomeType.OCEANIC),
    BLACK_OCEAN(new BlackOceansHandler(), BiomeType.OCEANIC),
    COLD_OCEAN(new ColdOceansHandler(), BiomeType.OCEANIC),
    FROZEN_OCEAN(new FrozenOceansHandler(), BiomeType.OCEANIC, new FrozenCavePopulator()),
    WARM_OCEAN(new WarmOceansHandler(), BiomeType.OCEANIC),
    LUKEWARM_OCEAN(new LukewarmOceansHandler(), BiomeType.OCEANIC),
    SWAMP(new SwampHandler(), BiomeType.OCEANIC),

    //RIVERS
    RIVER(new RiverHandler(), BiomeType.RIVER), //Special case, handle later
    JUNGLE_RIVER(new JungleRiverHandler(), BiomeType.RIVER),
    FROZEN_RIVER(new FrozenRiverHandler(), BiomeType.RIVER, new FrozenCavePopulator()), //Special case, handle later

    //DEEP OCEANIC
    DEEP_OCEAN(new OceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_COLD_OCEAN(new ColdOceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_BLACK_OCEAN(new BlackOceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_FROZEN_OCEAN(new FrozenOceansHandler(), BiomeType.DEEP_OCEANIC, new FrozenCavePopulator()),
    DEEP_WARM_OCEAN(new WarmOceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_LUKEWARM_OCEAN(new LukewarmOceansHandler(), BiomeType.DEEP_OCEANIC),

    //FLAT
    PLAINS(new PlainsHandler(), BiomeType.FLAT),
    ERODED_PLAINS(new ErodedPlainsHandler(), BiomeType.FLAT),
    SAVANNA(new SavannaHandler(), BiomeType.FLAT),
    FOREST(new ForestHandler(), BiomeType.FLAT),
    DESERT(new DesertHandler(), BiomeType.FLAT),
    JUNGLE(new JungleHandler(), BiomeType.FLAT),
    BAMBOO_FOREST(new BambooForestHandler(), BiomeType.FLAT),
    BADLANDS(new BadlandsHandler(), BiomeType.FLAT),
    TAIGA(new TaigaHandler(), BiomeType.FLAT),
    SNOWY_TAIGA(new SnowyTaigaHandler(), BiomeType.FLAT, new FrozenCavePopulator()),
    SNOWY_WASTELAND(new SnowyWastelandHandler(), BiomeType.FLAT, new FrozenCavePopulator()),
    ICE_SPIKES(new IceSpikesHandler(), BiomeType.FLAT, new FrozenCavePopulator()),
    DARK_FOREST(new DarkForestHandler(), BiomeType.FLAT),

    //BEACHES
    SANDY_BEACH(new SandyBeachHandler(), BiomeType.BEACH),
    BADLANDS_BEACH(new BadlandsBeachHandler(), BiomeType.BEACH),
    ROCKY_BEACH(new RockBeachHandler(), BiomeType.BEACH),
    ICY_BEACH(new IcyBeachHandler(), BiomeType.BEACH, new FrozenCavePopulator()),
    MUDFLATS(new MudflatsHandler(), BiomeType.BEACH), //Special case, handle later
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
    //Refers to max moisture and max temperature.
    private float moisture;
    private float temperature;

    private static final LoadingCache<BiomeSection, BiomeSection> BIOMESECTION_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(250).build(new BiomeSectionCacheLoader());
    
    BiomeBank(BiomeHandler handler, BiomeType type) {
        this.handler = handler;
        this.type = type;
        this.cavePop = new MossyCavePopulator();
    }

    BiomeBank(BiomeHandler handler, BiomeType type, AbstractCavePopulator cavePop) {
        this.handler = handler;
        this.type = type;
        this.cavePop = cavePop;
    }
    
    public static BiomeSection getBiomeSection(TerraformWorld tw, int x, int z) {
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
    public static BiomeSection getBiomeSection(TerraformWorld tw, int x, int z, boolean useSectionCoords) {
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
     * @param height Ranges between 0-255
     * @return a biome type
     */
    public static BiomeBank calculateBiome(TerraformWorld tw, int x, int z) {

        double dither = TConfigOption.BIOME_DITHER.getDouble();
    	Random locationBasedRandom  = new Random(Objects.hash(tw.getSeed(),x,z));
    	SimpleLocation target  = new SimpleLocation(x,0,z);
    	BiomeSection homeSection = getBiomeSection(tw, x,z);
    	
        //This is a river.
    	//Idk why 10 works, but it sort of does
        if (HeightMap.getRawRiverDepth(tw, x, z) > 10) {
            return BiomeGrid.calculateBiome(BiomeType.RIVER,
            		homeSection.getTemperature() + GenUtils.randDouble(locationBasedRandom, -dither, dither),
            		homeSection.getMoisture() + GenUtils.randDouble(locationBasedRandom, -dither, dither)
            );
        }
    	
    	Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, x, z);
    	BiomeSection mostDominant = homeSection;
    	
    	for(BiomeSection sect:sections) {
    		float dom = (float) (sect.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither));
    		
    		if(dom > mostDominant.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither))
    			mostDominant = sect;
    	}
    	
    	return mostDominant.getBiomeBank();
    
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

    /**
     * @return the max allowed moisture
     */
    public float getMaxMoisture() {
        return moisture;
    }

    /**
     * @return the max allowed temperature
     */
    public float getMaxTemperature() {
        return temperature;
    }

}
