package org.terraform.v1_17_R1;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureManager;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeSupportedBiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NMSChunkGenerator extends ChunkGenerator {
	private final ChunkGenerator delegate;
    private final TerraformWorld tw;
    private final TerraformWorldProviderBiome twBS;
    
    public NMSChunkGenerator(String worldname, int seed,
    						ChunkGenerator delegate,
                             WorldChunkManager worldchunkmanager,
                             WorldChunkManager worldchunkmanager1,
                             StructureSettings structuresettings, long i) {
        super(new CustomBiomeSource(worldchunkmanager), new CustomBiomeSource(worldchunkmanager1), structuresettings, i);
        

        tw = TerraformWorld.get(worldname, seed);
        this.twBS = new TerraformWorldProviderBiome(tw, worldchunkmanager);
        this.delegate = delegate;
    }

    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    //tf does this do then
    @Override
    public void createBiomes(IRegistry<BiomeBase> iregistry, IChunkAccess ichunkaccess) {
//        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
//        ((ProtoChunk)ichunkaccess).a(new BiomeStorage(iregistry, ichunkaccess, chunkcoordintpair, this.c));
     }

    @Override
    public BlockPosition findNearestMapFeature(WorldServer worldserver, StructureGenerator<?> structuregenerator, BlockPosition blockposition, int i, boolean flag) {
	//      PILLAGER_OUTPOST -> a
	//      MINESHAFT -> b
	//      MINESHAFT_MESA -> c
	//      WOODLAND_MANSION -> d
	//      JUNGLE_TEMPLE -> e
	//      DESERT_PYRAMID -> f
	//      IGLOO -> g
	//      SHIPWRECK -> h
	//      SHIPWRECH_BEACHED -> i
	//      SWAMP_HUT -> j
	//      STRONGHOLD -> k
	//      OCEAN_MONUMENT -> l
	//      OCEAN_RUIN_COLD -> m
	//      OCEAN_RUIN_WARM -> n
	//      NETHER_BRIDGE -> o
	//      NETHER_FOSSIL -> p
	//      END_CITY -> q
	//      BURIED_TREASURE -> r
	//      BASTION_REMNANT -> s
	//      VILLAGE_PLAINS -> t
	//      VILLAGE_DESERT -> u
	//      VILLAGE_SAVANNA -> v
	//      VILLAGE_SNOWY -> w
	//      VILLAGE_TAIGA -> x
	//      RUINED_PORTAL_STANDARD -> y
	//      RUINED_PORTAL_DESERT -> z
	//      RUINED_PORTAL_JUNGLE -> A
	//      RUINED_PORTAL_SWAMP -> B
	//      RUINED_PORTAL_MOUNTAIN -> C
	//      RUINED_PORTAL_OCEAN -> D
	//      RUINED_PORTAL_NETHER -> E
    	
    	//StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
        int pX = blockposition.getX();
        int pZ = blockposition.getZ();
        TerraformGeneratorPlugin.logger.info("Vanilla locate for " + structuregenerator.getClass().getName() + " invoked.");

        if (structuregenerator == StructureGenerator.k) { //stronghold
            int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 20, coords[1]);
        } 
        else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
        {
        	if (structuregenerator == StructureGenerator.l) { //Monument
                
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            } else if (structuregenerator == StructureGenerator.d) { //Mansion
                    
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            } else if (structuregenerator.getClass().getName().equals("net.minecraft.world.level.levelgen.feature.WorldGenBuriedTreasure")) { 
            	//Buried Treasure
            	int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                assert coords != null;
                return new BlockPosition(coords[0], 50, coords[1]);
            }
        }

        return null;
    }

    @Override
    public void addDecorations(RegionLimitedWorldAccess rlwa, StructureManager structuremanager) {
        delegate.addDecorations(rlwa, structuremanager);
    }

    @Override
    public void doCarving(long i, BiomeManager biomemanager, IChunkAccess ichunkaccess, WorldGenStage.Features worldgenstage_features) {
        delegate.doCarving(i, biomemanager, ichunkaccess, worldgenstage_features);
    }

    @Override
    public int getSeaLevel() {
        return TerraformGenerator.seaLevel;
    }

    @Override
    public void createStructures(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager,
                                 long i) {

    }

    public int getSpawnHeight(LevelHeightAccessor levelheightaccessor) {
        return 64;
     }

    @Override
    public CompletableFuture<IChunkAccess> buildNoise(Executor executor, StructureManager structuremanager, IChunkAccess ichunkaccess) {
        return delegate.buildNoise(executor, structuremanager, ichunkaccess);
     }

    @Override
    public void buildBase(RegionLimitedWorldAccess regionlimitedworldaccess, IChunkAccess ichunkaccess) {
        delegate.buildBase(regionlimitedworldaccess, ichunkaccess);
    }

    //what the fuck is this for
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public WeightedRandomList getMobsFor(BiomeBase biomebase, StructureManager structuremanager, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
    	
    	if (structuremanager.a(blockposition, true, StructureGenerator.j).e()) {
	      if (enumcreaturetype == EnumCreatureType.a) { //MobCategory.MONSTER
	          return StructureGenerator.j.c(); //Swamp Hut
	      }
	
	      if (enumcreaturetype == EnumCreatureType.b) { //MobCategory.CREATURE
	          return StructureGenerator.j.h(); //Swamp Hut
	      }
	  }
	
	  if (enumcreaturetype == EnumCreatureType.a) { //MobCategory.MONSTER
	      if (structuremanager.a(blockposition, false, StructureGenerator.b).e()) {
	          return StructureGenerator.b.c(); //pillager outpost
	      }
	
	      if (structuremanager.a(blockposition, false, StructureGenerator.l).e()) {
	          return StructureGenerator.l.c(); //Ocean Monument
	      }
	
	      if (structuremanager.a(blockposition, true, StructureGenerator.n).e()) {
	          return StructureGenerator.n.c(); //Nether Bridge???
	      }
	  }

    	return biomebase.b().a(enumcreaturetype);
     }

    @Override
    public int getBaseHeight(int x, int z, HeightMap.Type var2, LevelHeightAccessor var3) {
        return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }

    @Override
    protected Codec<? extends ChunkGenerator> a() {
        return ChunkGeneratorAbstract.d;
    }

	@Override
	public BlockColumn getBaseColumn(int var0, int var1, LevelHeightAccessor var2) {
		
		return this.delegate.getBaseColumn(var0, var1, var2);
	}
	
	@Override
	public ChunkGenerator withSeed(long arg0) {
		return new NMSChunkGenerator(this.tw.getName(), (int) arg0, this.delegate, this.getWorldChunkManager(), this.getWorldChunkManager(), this.getSettings(), 0L);
	}
}
