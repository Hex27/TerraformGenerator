package org.terraform.v1_18_R2;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.SeededRandom;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.generator.CraftLimitedRegion;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.biome.custombiomes.CustomBiomeSupportedBiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformBukkitBlockPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.small.buriedtreasure.BuriedTreasurePopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("deprecation")
public class NMSChunkGenerator extends ChunkGenerator {
	private final ChunkGenerator delegate;
    private final TerraformWorld tw;
    
	public NMSChunkGenerator(String worldname, long seed,
    						ChunkGenerator delegate) {
    	super(
    			delegate.b,//b is structureSets
    			delegate.e,//e is structureOverrides
    			new TerraformWorldProviderBiome(TerraformWorld.get(worldname,seed), 
    					delegate.e()));  //Last arg is WorldChunkManager

        tw = TerraformWorld.get(worldname, seed);
        this.delegate = delegate;
    }
    

    @Override //getBiomeSource
    public WorldChunkManager e() {
    	//d is runtimeBiomeSource
    	if(!(d instanceof TerraformWorldProviderBiome))
        	TerraformGeneratorPlugin.logger.error("d was not an instance of TerraformWorldProviderBiome!");

        return this.d;
    }
    
    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    //createBiomes
    @Override
    public CompletableFuture<IChunkAccess> a(IRegistry<BiomeBase> iregistry, Executor executor, Blender blender, StructureManager structuremanager, IChunkAccess ichunkaccess) {

    	return CompletableFuture.supplyAsync(SystemUtils.a("init_biomes", () -> {
             return ichunkaccess; //Don't do any calculations here, biomes are set in applyCarvers
         }), SystemUtils.f());
     }
    
	@Override //findNearestMapFeature
//	//StructureManager is StructureFeatureManager, NOT StructureManager. Spigot remapped it.
	//(spigot) StructureGenerator is (mojang) StructureFeature
	//(spigot) StructureFeature is (mojang) ConfiguredStructureFeature
    public Pair<BlockPosition, Holder<StructureFeature<?, ?>>> a(WorldServer worldserver, HolderSet<StructureFeature<?, ?>> holderset, BlockPosition blockposition, int i, boolean flag) 
    {
//    	   net.minecraft.world.level.levelgen.feature.StructureFeature PILLAGER_OUTPOST -> b
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature MINESHAFT -> c
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature WOODLAND_MANSION -> d
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature JUNGLE_TEMPLE -> e
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature DESERT_PYRAMID -> f
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature IGLOO -> g
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature RUINED_PORTAL -> h
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature SHIPWRECK -> i
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature SWAMP_HUT -> j
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature STRONGHOLD -> k
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature OCEAN_MONUMENT -> l
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature OCEAN_RUIN -> m
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature FORTRESS -> n
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature END_CITY -> o
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature BURIED_TREASURE -> p
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature VILLAGE -> q
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature NETHER_FOSSIL -> r
//    	    net.minecraft.world.level.levelgen.feature.StructureFeature BASTION_REMNANT -> s
    	
    	int pX = blockposition.u(); //getX
        int pZ = blockposition.w(); //getZ
        
        for(Holder<StructureFeature<?, ?>> holder:holderset) {
        	StructureFeature<?,?> feature = holder.a();
        	StructureGenerator<?> structuregenerator = feature.d;
        	TerraformGeneratorPlugin.logger.info("Vanilla locate for " + structuregenerator.getClass().getName() + " invoked.");

            if (structuregenerator == StructureGenerator.k) { //stronghold
                int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
                return new Pair<BlockPosition, Holder<StructureFeature<?, ?>>>
                (new BlockPosition(coords[0], 20, coords[1]), holder);
            } 
            else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
            {
            	if (structuregenerator == StructureGenerator.l) { //Monument
                    
            		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());

                    return new Pair<BlockPosition, Holder<StructureFeature<?, ?>>>
                    (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (structuregenerator == StructureGenerator.d) { //Mansion
                        
            		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());

                    return new Pair<BlockPosition, Holder<StructureFeature<?, ?>>>
                    (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (structuregenerator.getClass().getName().equals("net.minecraft.world.level.levelgen.feature.WorldGenBuriedTreasure")) { 
                	//Buried Treasure
                	int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                    if(coords == null) return null;
                    return new Pair<BlockPosition, Holder<StructureFeature<?, ?>>>
                    (new BlockPosition(coords[0], 50, coords[1]), holder);
                }
            }
        }
        return null;
    }

    @Override //applyBiomeDecoration
    public void a(GeneratorAccessSeed generatoraccessseed, IChunkAccess ichunkaccess, StructureManager structuremanager) {
    	delegate.a(generatoraccessseed, ichunkaccess, structuremanager);
    }

    @Override //applyCarvers
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, long var2, BiomeManager var4, StructureManager var5, IChunkAccess ichunkaccess, WorldGenStage.Features var7) {

    	//POPULATES BIOMES. IMPORTANT
    	//ichunkaccess.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler())
    	ichunkaccess.a(this.d, this.d());
    	delegate.a(regionlimitedworldaccess, var2, var4, var5, ichunkaccess, var7);
    }

    @Override //getSeaLevel
    public int g() {
        return 256;
    }

    @Override //createStructures should be empty
    public void a(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager,
                                 long i) {
    	
    }

    @Override //getSpawnHeight
    public int a(LevelHeightAccessor levelheightaccessor) {
        return 64;
     }

    @Override //fillFromNoise
    public CompletableFuture<IChunkAccess> a(Executor executor, Blender blender, StructureManager structuremanager, IChunkAccess ichunkaccess) {
    	return delegate.a(executor, blender, structuremanager, ichunkaccess);
     }
    

//  	@SuppressWarnings("unchecked")
    @Override //buildSurface. Used to be buildBase
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, StructureManager structuremanager, IChunkAccess ichunkaccess) {
        delegate.a(regionlimitedworldaccess, structuremanager, ichunkaccess);
    }
  	
  	@Override //createReferences. Idk what this is
  	public void a(GeneratorAccessSeed gas,StructureManager manager,IChunkAccess ica)
  	{
  		delegate.a(gas, manager, ica);
  	}

    @Override
    protected Codec<? extends ChunkGenerator> b() {
        return ChunkGeneratorAbstract.a;
    }

	@Override //getBaseColumn
	public BlockColumn a(int var0, int var1, LevelHeightAccessor var2) {
		
		return this.delegate.a(var0, var1, var2);
	}
	
	@Override //withSeed
	public ChunkGenerator a(long seed) {
		return new NMSChunkGenerator(this.tw.getName(), (int) seed, this.delegate);
	}
	
	//spawnOriginalMobs
	public void a(RegionLimitedWorldAccess regionlimitedworldaccess) {
		this.delegate.a(regionlimitedworldaccess);
	}
	//getGenDepth
	public int f() {
		return this.delegate.f();
	}
	//climateSampler
	public Sampler d() {
		return this.delegate.d();
	}
	
	//getMinY
	@Override
	public int h() {
		return this.delegate.h();
	}
	
	@Override //getFirstFreeHeight
	public int b(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor) {
		return this.a(i, j, heightmap_type, levelheightaccessor);
	}
	
	
	@Override //getFirstOccupiedHeight
	public int c(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor) {
		return this.a(i, j, heightmap_type, levelheightaccessor) - 1;
	}

    @Override //getBaseHeight
    public int a(int x, int z, HeightMap.Type var2, LevelHeightAccessor var3) {
        //return delegate.a(x, z, var2, var3);
    	return 100;
    	//return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }
   
    private static boolean biomeDebug = false;
    
    @Override
	public Holder<BiomeBase> getNoiseBiome(int x, int y, int z) {
    	if(!biomeDebug) {
    		biomeDebug = true;
			TerraformGeneratorPlugin.logger.info("[getNoiseBiome] called for " + x + "," + y + "," + z);
    	}
      return this.c.getNoiseBiome(x, y, z, null);
    }

	@Override //addDebugScreenInfo
	public void a(List<String> arg0, BlockPosition arg1) {
		
	}
    
}
