package org.terraform.coregen.bukkit;

import com.google.common.cache.LoadingCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeSupportedBiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TerraformGenerator extends ChunkGenerator {
    public static final List<SimpleChunkLocation> preWorldInitGen = new ArrayList<>();

    private static final Object LOCK = new Object();

    public static LoadingCache<ChunkCache, ChunkCache> CHUNK_CACHE;
    public static int seaLevel = 62;
    
    public static void updateSeaLevelFromConfig() {
        seaLevel = TConfigOption.HEIGHT_MAP_SEA_LEVEL.getInt();
    }

    /**
     * Refers to raw X and raw Z (block coords). NOT chunk coords.
     */
    public static ChunkCache getCache(TerraformWorld tw, int x, int z) {
        ChunkCache cache = new ChunkCache(tw, x, 0, z);
		
//		return CHUNK_CACHE.compute(cache, (k, v) -> { if (v != null) return v;
//		cache.initInternalCache(); return cache; });
        try {
			return CHUNK_CACHE.get(cache);
		} catch (ExecutionException e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
			cache.initInternalCache();
			return cache;
		}
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    //private static boolean debugged = false;
    
    ConcurrentHashMap<SimpleChunkLocation, PopulatorDataAbstract> generatorDataAccess = new ConcurrentHashMap<SimpleChunkLocation, PopulatorDataAbstract>();
    
    public void addPopulatorData(PopulatorDataAbstract data) {
    	//TerraformGeneratorPlugin.logger.info("Added (" + data.getChunkX() + "," + data.getChunkZ() + ")");
    	generatorDataAccess.put(new SimpleChunkLocation(data.getTerraformWorld().getName(),data.getChunkX(),data.getChunkZ()), data);
    }
    
	@SuppressWarnings("deprecation")
	protected
    ChunkData createChunkData(World world, int chunkX, int chunkZ) {
    	if(Version.isAtLeast(18)) {
    		PopulatorDataAbstract data = generatorDataAccess.remove(new SimpleChunkLocation(world.getName(), chunkX, chunkZ));
    		if(data == null)
    			throw new IllegalArgumentException("Requested for chunkX and Z that weren't in the concurrenthashmap!");
    		//TerraformGeneratorPlugin.logger.info("Called for (" + chunkX + "," + chunkZ + "):(" + data.getChunkX() + "," + data.getChunkZ() + ")");
    		return new TerraformChunkData(data);
    	}
    	else
    		return super.createChunkData(world);
    }
	
	private int getVanillaGeneratedHeight(TerraformWorld tw, ChunkData chunk, int x, int z) {
		int y = tw.maxY;
		while(y > tw.minY) {
			if(!chunk.getType(x, y, z).isAir())
				break;
			else
				y--;
		}
		return y;
	}
    
    @SuppressWarnings("deprecation")
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world, chunkX, chunkZ);
        TerraformWorld tw = TerraformWorld.get(world);
        
        //Patch for WorldInitEvent issues.
        if (!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) {
            preWorldInitGen.add(new SimpleChunkLocation(world.getName(), chunkX, chunkZ));
        }
        
        boolean newLogic = Version.isAtLeast(18);
        List<BiomeHandler> biomesToTransform = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                //tw.getBiomeBank(rawX, rawZ);
                int height = (int) HeightMap.getBlockHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);

                BiomeBank bank = BiomeBank.calculateBiome(tw, rawX, height, rawZ);
                
                Material[] crust = bank.getHandler().getSurfaceCrust(random);
                
                if(newLogic) {
                    //Fix up height before doing anything else
                	//New chunk generator will output vanilla heights. Correct them.
                	int vanillaHeight = getVanillaGeneratedHeight(tw, chunk,x,z);
                    if(vanillaHeight < height)
                    {
                    	for(int y = vanillaHeight; y <= height; y++) {
                    		if(chunk.getType(x, y, z) != Material.CAVE_AIR) {
                    			setBlockSync(chunk,x,y,z, Material.STONE);
                    		}
                    	}
                    }
                    else if(vanillaHeight > height)
                    {
                    	for(int y = vanillaHeight; y > height; y--)
                    		setBlockSync(chunk,x,y,z, Material.AIR);
                    }
                }
                else
                {
                	//Old chunkgenerator must set biomes in ChunkData
                    if(bank.getHandler().getCustomBiome() != CustomBiomeType.NONE && 
                    		biome instanceof CustomBiomeSupportedBiomeGrid) {
                    	((CustomBiomeSupportedBiomeGrid) biome).setBiome(
                    			tw,
                    			x, z, 
                    			bank.getHandler().getCustomBiome(), 
                    			bank.getHandler().getBiome());
                    }
                    else
                    {
                    	biome.setBiome(x, z, bank.getHandler().getBiome());
                    }
                }
                
                
                int undergroundHeight = height;
                int index = 0;
                while (index < crust.length) {
                    setBlockSync(chunk, x, undergroundHeight, z, crust[index]);
                    index++;
                    undergroundHeight--;
                }
                
                for (int y = undergroundHeight; y > tw.minY; y--) {
                	if(chunk.getType(x, y, z).isSolid() || !newLogic)
	                	if(y > 2)
	                		setBlockSync(chunk, x, y, z, Material.STONE);
	                	else if(y > 0 && y <= 2)
	                		setBlockSync(chunk, x, y, z, GenUtils.randMaterial(OneOneSevenBlockHandler.DEEPSLATE, Material.STONE));
	                	else
	                		setBlockSync(chunk, x, y, z, OneOneSevenBlockHandler.DEEPSLATE);
                }
                
                //New logic fills seas after cave carving
                if(!newLogic)
                	fillSeaAndRivers(chunk,x,z,height);

                //Bedrock Base
                setBlockSync(chunk, x, tw.minY, z, Material.BEDROCK);
                
                //Up till y = minY+5
                for(int i = 1; i <= 4; i++) {
                	if(random.nextInt(3) > 0)
                		setBlockSync(chunk, x, tw.minY+i, z, Material.BEDROCK);
                	else
                		break;
                }
                
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if (transformHandler != null && !biomesToTransform.contains(transformHandler))
                	biomesToTransform.add(transformHandler);
            }
        }

        for (BiomeHandler handler : biomesToTransform) {
            handler.transformTerrain(tw, random, chunk, biome, chunkX, chunkZ);
        }

        return chunk;
    }
    
    public void fillSeaAndRivers(ChunkData chunk, int x, int z, int height) {

    	int realLandHeight = seaLevel;
    	for (int y = seaLevel; y > height; y--) {
    		if(chunk.getType(x, y, z).isSolid())
    		{
    			realLandHeight = y;
        		break;
    		}
        }
    	
    	//Implies that the hole was carved by a cave. Do not fill with water.
    	if(height > TerraformGenerator.seaLevel && 
    			height > realLandHeight) return;
    	
        //Any low elevation is sea. Iterate and fill until ground.
        for (int y = seaLevel; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
        	if(!chunk.getType(x, y, z).isSolid())
        		setBlockSync(chunk, x, y, z, Material.WATER);
        	else 
        		break;
        }
    }

    private void setBlockSync(ChunkData data, int x, int y, int z, Material material) {
    	if(Version.isAtLeast(18))
            data.setBlock(x, y, z, material);
    	else
	        synchronized(LOCK) {
	            data.setBlock(x, y, z, material);
	        }
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, HeightMap.getBlockHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return Collections.singletonList(new TerraformBukkitBlockPopulator(tw));
    }

    //This probably affects noise caves
    public boolean shouldGenerateNoise() {
        return true;
    }
    
    //No effect on plugin, this is overridden.
    public boolean shouldGenerateSurface() {
        return false;
    }

    //no effect on plugin, this is overridden.
    public boolean shouldGenerateBedrock() {
        return false;
    }

    //Affects the carver caves
    public boolean shouldGenerateCaves() {
        return true;
    }

    //No effect on plugin, this is overridden.
    public boolean shouldGenerateDecorations() {
        return true;
    }

    //No effect on plugin, this is overridden.
    public boolean shouldGenerateMobs() {
        return false;
    }

    //No effect on plugin, this is overridden.
    public boolean shouldGenerateStructures() {
        return false;
    }
}
