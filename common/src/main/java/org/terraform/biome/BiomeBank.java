package org.terraform.biome;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Range;
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
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public enum BiomeBank {
    //MOUNTAINOUS
    ROCKY_MOUNTAINS(new RockyMountainsHandler(), BiomeType.MOUNTAINOUS, Range.between(-1.0,1.0), Range.between(-2.5,2.5), 1),
    BADLANDS_MOUNTAINS(new BadlandsMountainHandler(), BiomeType.MOUNTAINOUS, Range.between(2.0,2.5), Range.between(-2.5,2.5), 1),
    SNOWY_MOUNTAINS(new SnowyMountainsHandler(), BiomeType.MOUNTAINOUS, Range.between(-2.5,-1.5), Range.between(-2.5,2.5), 1, new FrozenCavePopulator()),
    BIRCH_MOUNTAINS(new BirchMountainsHandler(), BiomeType.MOUNTAINOUS, Range.between(-1.5,-1.0), Range.between(-2.5,2.5), 1),
    DESERT_MOUNTAINS(new DesertMountainHandler(), BiomeType.MOUNTAINOUS, Range.between(1.0,2.0), Range.between(-2.5,2.5), 1),

    //OCEANIC
    OCEAN(new OceansHandler(), BiomeType.OCEANIC, Range.between(-0.5,1.0), Range.between(-2.5,2.5), 1),
    BLACK_OCEAN(new BlackOceansHandler(), BiomeType.OCEANIC, Range.between(-1.0,-0.5), Range.between(1.0,1.5), 1),
    COLD_OCEAN(new ColdOceansHandler(), BiomeType.OCEANIC, Range.between(-1.5,-0.5), Range.between(-2.5,2.5), 1),
    FROZEN_OCEAN(new FrozenOceansHandler(), BiomeType.OCEANIC, Range.between(-2.5,-1.5), Range.between(-2.5,2.5), 1, new FrozenCavePopulator()),
    WARM_OCEAN(new WarmOceansHandler(), BiomeType.OCEANIC, Range.between(1.5,2.5), Range.between(-2.5,2.5), 1),
    LUKEWARM_OCEAN(new LukewarmOceansHandler(), BiomeType.OCEANIC, Range.between(1.0,1.5), Range.between(-2.5,2.5), 1),
    
    //RIVERS (Don't include in selectBiome)
    RIVER(new RiverHandler(), BiomeType.RIVER), //Special case, handle later
    JUNGLE_RIVER(new JungleRiverHandler(), BiomeType.RIVER),
    FROZEN_RIVER(new FrozenRiverHandler(), BiomeType.RIVER, new FrozenCavePopulator()), //Special case, handle later

    //DEEP OCEANIC (Don't include in selectBiome)
    DEEP_OCEAN(new OceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_COLD_OCEAN(new ColdOceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_BLACK_OCEAN(new BlackOceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_FROZEN_OCEAN(new FrozenOceansHandler(), BiomeType.DEEP_OCEANIC, new FrozenCavePopulator()),
    DEEP_WARM_OCEAN(new WarmOceansHandler(), BiomeType.DEEP_OCEANIC),
    DEEP_LUKEWARM_OCEAN(new LukewarmOceansHandler(), BiomeType.DEEP_OCEANIC),

    //FLAT
    PLAINS(new PlainsHandler(), BiomeType.FLAT, Range.between(-1.0,1.0), Range.between(-2.5,1.0), 3),
    ERODED_PLAINS(new ErodedPlainsHandler(), BiomeType.FLAT, Range.between(-1.0,-0.0), Range.between(-0.5,1.0), 3),
    SAVANNA(new SavannaHandler(), BiomeType.FLAT, Range.between(0.0,2.5), Range.between(-2.5,1.0), 6),
    FOREST(new ForestHandler(), BiomeType.FLAT, Range.between(0.0,1.0), Range.between(1.0,2.5), 3),
    DESERT(new DesertHandler(), BiomeType.FLAT, Range.between(1.0,2.5), Range.between(-2.5,0.0), 10),
    JUNGLE(new JungleHandler(), BiomeType.FLAT, Range.between(1.0,2.5), Range.between(1.0,2.5), 5),
    BAMBOO_FOREST(new BambooForestHandler(), BiomeType.FLAT, Range.between(1.0,2.0), Range.between(2.0,2.5), 3),
    BADLANDS(new BadlandsHandler(), BiomeType.FLAT, Range.between(2.0,2.5), Range.between(-2.5,-1.0), 20),
    TAIGA(new TaigaHandler(), BiomeType.FLAT, Range.between(-1.5,-1.0), Range.between(-2.5,2.5), 3),
    SNOWY_TAIGA(new SnowyTaigaHandler(), BiomeType.FLAT, Range.between(-2.5,-1.5), Range.between(0.0,2.0), 3, new FrozenCavePopulator()),
    SNOWY_WASTELAND(new SnowyWastelandHandler(), BiomeType.FLAT, Range.between(-2.5,-1.5), Range.between(-2.5,0.0), 3, new FrozenCavePopulator()),
    ICE_SPIKES(new IceSpikesHandler(), BiomeType.FLAT, Range.between(-2.5,-1.5), Range.between(2.0,2.5), 3, new FrozenCavePopulator()),
    DARK_FOREST(new DarkForestHandler(), BiomeType.FLAT, Range.between(-1.0,-0.0), Range.between(1.0,2.5), 3),
    SWAMP(new SwampHandler(), BiomeType.FLAT, Range.between(-0.5,0.5), Range.between(1.0,2.0), 3),

    //BEACHES (Don't include in selectBiome)
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
    
    private Range<Double> temperatureRange;
    private Range<Double> moistureRange;
    private int biomeWeight;

    private static final LoadingCache<BiomeSection, BiomeSection> BIOMESECTION_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(250).build(new BiomeSectionCacheLoader());
    
    BiomeBank(BiomeHandler handler, BiomeType type) {
        this.handler = handler;
        this.type = type;
        
        //Impossible to pick from selectBiome.
        this.temperatureRange = Range.between(-12.0, -10.0);
        this.moistureRange = Range.between(-12.0, -10.0);
        this.biomeWeight = 0;
        
        this.cavePop = new MossyCavePopulator();
    }
    
    BiomeBank(BiomeHandler handler, BiomeType type, AbstractCavePopulator cavePop) {
        this.handler = handler;
        this.type = type;
        
        //Impossible to pick from selectBiome.
        this.temperatureRange = Range.between(-12.0, -10.0);
        this.moistureRange = Range.between(-12.0, -10.0);
        this.biomeWeight = 0;
        
        this.cavePop = cavePop;
    }
    
    BiomeBank(BiomeHandler handler, BiomeType type, Range<Double> temperatureRange, Range<Double> moistureRange, int biomeWeight) {
        this.handler = handler;
        this.type = type;
        this.temperatureRange = temperatureRange;
        this.moistureRange = moistureRange;
        this.biomeWeight = biomeWeight;
        this.cavePop = new MossyCavePopulator();
    }

    BiomeBank(BiomeHandler handler, BiomeType type, Range<Double> temperatureRange, Range<Double> moistureRange, int biomeWeight, AbstractCavePopulator cavePop) {
        this.handler = handler;
        this.type = type;
        this.cavePop = cavePop;
        this.temperatureRange = temperatureRange;
        this.moistureRange = moistureRange;
        this.biomeWeight = biomeWeight;
    }
    
    /**
     * Block X, block Z.
     * @param tw
     * @param x
     * @param z
     * @return
     */
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
     * WARNING: NOBODY SHOULD BE CALLING THIS METHOD. 
     * THIS METHOD WILL RUN ALL CALCULATIONS.
     * 
     * Use terraformWorld.getCache(...).getBiomeBank(x,y,z) instead.
     * @param tw
     * @param rawX
     * @param height
     * @param rawZ
     * @return
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
    	return bank;
    }
    
    /**
     * NOBODY SHOULD BE CALLING THIS METHOD. THIS IS AN INTERNAL CALCULATION,
     * AND IT WILL NOT RETURN THE FINAL BIOME.
     * USE calculateBiome INSTEAD.
     * SUPPLY HEIGHT WITH GETHIGHESTGROUND.
     * @param height Ranges between 0-255
     * @return a biome type
     */
    public static BiomeBank calculateHeightIndependentBiome(TerraformWorld tw, int x, int z) {

        double dither = TConfigOption.BIOME_DITHER.getDouble();
    	Random locationBasedRandom  = new Random(Objects.hash(tw.getSeed(),x,z));
    	SimpleLocation target  = new SimpleLocation(x,0,z);
    	BiomeSection homeSection = getBiomeSection(tw, x,z);
    	
    	Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, x, z);
    	BiomeSection mostDominant = homeSection;
    	
    	for(BiomeSection sect:sections) {
    		float dom = (float) (sect.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither));
    		
    		if(dom > mostDominant.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither))
    			mostDominant = sect;
    	}
    	
    	return mostDominant.getBiomeBank();
    
    }
    
    public static BiomeBank selectBiome(BiomeSection section, double temperature, double moisture) {
    	Random sectionRand = section.getSectionRandom();
    	
    	BiomeBank defaultBiome = BiomeBank.PLAINS;
    	
    	ArrayList<BiomeBank> contenders = new ArrayList<>();
    	for(BiomeBank biome:BiomeBank.values()) {
    		if(biome.biomeWeight <= 0) continue;
    		if(biome.temperatureRange.contains(temperature) && biome.moistureRange.contains(moisture)) {
    			for(int i = 0; i < biome.biomeWeight; i++)
    				contenders.add(biome);
    		}
    	}
    	
    	Collections.shuffle(contenders, sectionRand);
    	if(contenders.size() == 0)
    		return defaultBiome;
    	else
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

}
