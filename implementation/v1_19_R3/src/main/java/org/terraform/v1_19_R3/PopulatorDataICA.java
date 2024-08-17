package org.terraform.v1_19_R3;

import net.minecraft.core.registries.Registries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.EntityMinecartChest;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.storage.loot.LootTables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class PopulatorDataICA extends PopulatorDataICABiomeWriterAbstract {
	private PopulatorDataAbstract parent;
    private final IChunkAccess ica;
    private final int chunkX;
    private final int chunkZ;
    private final WorldServer ws;
    private final TerraformWorld tw;

    public PopulatorDataICA(PopulatorDataAbstract parent, TerraformWorld tw, WorldServer ws, IChunkAccess ica, int chunkX, int chunkZ) {
        this.ica = ica;
        this.parent = parent;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.ws = ws;
        this.tw = tw;
    }
    
    public Material getType(int x, int y, int z) {
    	//return parent.getType(x, y, z);
    	IBlockData ibd = ica.a_(new BlockPosition(x, y, z)); //getState
        return CraftBlockData.fromData(ibd).getMaterial();
    }

    public BlockData getBlockData(int x, int y, int z) {
       //return parent.getBlockData(x, y, z);
    	IBlockData ibd = ica.a_(new BlockPosition(x, y, z)); //getState
        return CraftBlockData.fromData(ibd);
    }
    
	@SuppressWarnings("deprecation")
	@Override
	public void setBiome(int rawX, int rawY, int rawZ, CustomBiomeType cbt, Biome fallback) {
		IRegistry<BiomeBase> biomeRegistry = CustomBiomeHandler.getBiomeRegistry();
        Holder<BiomeBase> targetBiome;
		if(cbt == CustomBiomeType.NONE) {
			
			targetBiome = CraftBlock.biomeToBiomeBase(ica.biomeRegistry, fallback);
		} else {
			ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);//ResourceKey.a(IRegistry.aP, new MinecraftKey(cbt.getKey()));
			Optional<Holder.c<BiomeBase>> optHolder = biomeRegistry.b(rkey); //getHolder
	        if(optHolder.isEmpty()) {
	        	TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");
	        	targetBiome =  CraftBlock.biomeToBiomeBase(ica.biomeRegistry, fallback);
	        }
	        else
	        	targetBiome = optHolder.get();
		}
		
		ica.setBiome(rawX >> 2, rawY >> 2, rawZ >> 2, targetBiome);
	}

	@Override
	public void setBiome(int rawX, int rawY, int rawZ, Biome biome) {
		//TerraformGeneratorPlugin.logger.info("Set " + rawX + "," + rawY + "," + rawZ + " to " + biome);
		ica.setBiome(rawX >> 2, rawY >> 2, rawZ >> 2, CraftBlock.biomeToBiomeBase(ica.biomeRegistry, biome));
	}

    @Override
    public void setType(int x, int y, int z, Material type) {
    	//parent.setType(x, y, z, type);
    	ica.a(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);

    	//ica.setType(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
    	//parent.setBlockData(x, y, z, data);
    	ica.a(new BlockPosition(x, y, z), ((CraftBlockData) data).getState(), false);

    	//ica.setType(new BlockPosition(x, y, z)
        //        , ((CraftBlockData) data).getState(), false);
    }

//	public Biome getBiome(int rawX, int rawY, int rawZ){
//		return CraftBlock.biomeBaseToBiome(gen.getBiome(ica.d(), new BlockPosition(rawX,rawY,rawZ)));
//	}

    public Biome getBiome(int rawX, int rawZ) {
    	return parent.getBiome(rawX, rawZ);
        //return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), y).getHandler().getBiome();//Biome.valueOf(ica
        // .getBiome(rawX, rawY, rawZ).l().replace("biome.minecraft.", "").toUpperCase(Locale.ENGLISH));
    }

     @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
//        EntityTypes<?> et;
//        try {
//            et = (EntityTypes<?>) EntityTypes.class.getDeclaredField(EntityTypeMapper.getObfsNameFromBukkitEntityType(type)).get(null);
//            Entity e = et.a(ws.getMinecraftWorld());
//            e.setPositionRotation((double) rawX + 0.5D, rawY, (double) rawZ + 0.5D, 0.0F, 0.0F);
//            if (e instanceof EntityInsentient) {
//                ((EntityInsentient) e).setPersistent();
//                ((EntityInsentient) e).prepare(ws, ws.getDamageScaler(new BlockPosition(rawX, rawY, rawZ)), EnumMobSpawn.d, null, null); //EnumMobSpawn.STRUCTURE
//            }
//
//            ws.addEntity(e);
//        } catch (IllegalArgumentException | IllegalAccessException
//                | NoSuchFieldException | SecurityException e1) {
//            e1.printStackTrace();
//        }
    	parent.addEntity(rawX, rawY, rawZ, type);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
//        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
//        ica.setType(pos, Blocks.bV.getBlockData(), true); //Spawner
//        TileEntity tileentity = ica.getTileEntity(pos);
//
//        if (tileentity instanceof TileEntityMobSpawner) {
//            try {
//                ((TileEntityMobSpawner) tileentity).getSpawner().setMobName((EntityTypes<?>) EntityTypes.class.getField(type.toString()).get(null));
//            } catch (IllegalArgumentException | IllegalAccessException
//                    | NoSuchFieldException | SecurityException e) {
//                e.printStackTrace();
//            }
//        } else {
//            TerraformGeneratorPlugin.logger.error("Failed to fetch mob spawner entity at (" + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")");
//            //WorldGenDungeons.LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", blockposition.getX(), blockposition.getY(), blockposition.getZ());
//        }
    	parent.setSpawner(rawX, rawY, rawZ, type);
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        BlockPosition pos = new BlockPosition(x, y, z);
        TileEntityLootable.a(ica, RandomSource.a(tw.getHashedRand(x, y, z).nextLong()), pos, getLootTable(table));
    }

    @SuppressWarnings("deprecation")
	@Override
    public void registerNaturalSpawns(NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1) {
    	ResourceKey<Structure> structureKey = switch(type) {
            case GUARDIAN -> BuiltinStructures.l; //Ocean Monument
            case PILLAGER -> BuiltinStructures.a; //Pillager Outpost
        };

        //ax is STRUCTURE
    	IRegistry<Structure> featureRegistry = MinecraftServer.getServer().aX().d(Registries.ax);
		
    	Structure structureFeature = featureRegistry.a(structureKey);
    	
    	StructurePiece customBoundPiece = new OceanMonumentPieces
        .h(RandomSource.a(), x0, z0,
        EnumDirection.a);
    	
    	PiecesContainer container = new PiecesContainer(new ArrayList<>() {{
            add(customBoundPiece);
        }});

		StructureStart start = new StructureStart(
				structureFeature,
				new ChunkCoordIntPair(chunkX, chunkZ), 
				0, container);

    	try {
        	Field i = StructureStart.class.getDeclaredField("h"); //boundingBox
        	i.setAccessible(true);
        	i.set(start, new StructureBoundingBox(x0, y0, z0, x1, y1, z1));
        }
    	catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) 
    	{
    		e.printStackTrace();
    	}
    	
    	
    	//ws.a() is getStructureManager
    	//a is setStartForStructure
    	/**setStartForStructure(
    	 * SectionPosition sectionposition, 
    	 * Structure structure, 
    	 * StructureStart structurestart, 
    	 * StructureAccess structureaccess)**/
    	//ws.a().a(SectionPosition.a(x0,y0,z0), structureFeature, start, ica);
    	
    	ica.a(structureFeature, start);
//    	ws.a().a( //setStartForFeature
//        		structureFeature,
//        		start);
        
        //addReferenceForFeature
    	ica.a(structureFeature, new ChunkCoordIntPair(chunkX, chunkZ).a()); //a is toLong
    }

    @SuppressWarnings("deprecation")
	@Override
    public void spawnMinecartWithChest(int x, int y, int z, TerraLootTable table, Random random) {
        EntityMinecartChest entityminecartchest = new EntityMinecartChest(
                ws.getMinecraftWorld(),
                (float) x + 0.5F,
                (float) y + 0.5F,
                (float) z + 0.5F);

        entityminecartchest.a(getLootTable(table), random.nextLong());
        ws.addFreshEntity(entityminecartchest, SpawnReason.CHUNK_GEN);
    }
    
    private MinecraftKey getLootTable(TerraLootTable table) {
        return switch(table) {
            case SPAWN_BONUS_CHEST -> LootTables.b;
            case END_CITY_TREASURE -> LootTables.c;
            case SIMPLE_DUNGEON -> LootTables.d;
            case VILLAGE_WEAPONSMITH -> LootTables.e;
            case VILLAGE_TOOLSMITH -> LootTables.f;
            case VILLAGE_ARMORER -> LootTables.g;
            case VILLAGE_CARTOGRAPHER -> LootTables.h;
            case VILLAGE_MASON -> LootTables.i;
            case VILLAGE_SHEPHERD -> LootTables.j;
            case VILLAGE_BUTCHER -> LootTables.k;
            case VILLAGE_FLETCHER -> LootTables.l;
            case VILLAGE_FISHER -> LootTables.m;
            case VILLAGE_TANNERY -> LootTables.n;
            case VILLAGE_TEMPLE -> LootTables.o;
            case VILLAGE_DESERT_HOUSE -> LootTables.p;
            case VILLAGE_PLAINS_HOUSE -> LootTables.q;
            case VILLAGE_TAIGA_HOUSE -> LootTables.r;
            case VILLAGE_SNOWY_HOUSE -> LootTables.s;
            case VILLAGE_SAVANNA_HOUSE -> LootTables.t;
            case ABANDONED_MINESHAFT -> LootTables.u;
            case NETHER_BRIDGE -> LootTables.v;
            case STRONGHOLD_LIBRARY -> LootTables.w;
            case STRONGHOLD_CROSSING -> LootTables.x;
            case STRONGHOLD_CORRIDOR -> LootTables.y;
            case DESERT_PYRAMID -> LootTables.z;
            case JUNGLE_TEMPLE -> LootTables.A;
            case JUNGLE_TEMPLE_DISPENSER -> LootTables.B;
            case IGLOO_CHEST -> LootTables.C;
            case WOODLAND_MANSION -> LootTables.D;
            case UNDERWATER_RUIN_SMALL -> LootTables.E;
            case UNDERWATER_RUIN_BIG -> LootTables.F;
            case BURIED_TREASURE -> LootTables.G;
            case SHIPWRECK_MAP -> LootTables.H;
            case SHIPWRECK_SUPPLY -> LootTables.I;
            case SHIPWRECK_TREASURE -> LootTables.J;
            case PILLAGER_OUTPOST -> LootTables.K;
//            case BASTION_TREASURE:
//                return LootTables.L;
//            case BASTION_OTHER:
//                return LootTables.M;
//            case BASTION_BRIDGE:
//                return LootTables.N;
//            case BASTION_HOGLIN_STABLE:
//                return LootTables.O;
            case ANCIENT_CITY -> LootTables.P;
            case ANCIENT_CITY_ICE_BOX -> LootTables.Q;
            case RUINED_PORTAL -> LootTables.R;
//            case SHEEP_WHITE:
//                return LootTables.S;
//            case SHEEP_ORANGE:
//                return LootTables.T;
//            case SHEEP_MAGENTA:
//                return LootTables.U;
//            case SHEEP_LIGHT_BLUE:
//                return LootTables.V;
//            case SHEEP_YELLOW:
//                return LootTables.W;
//            case SHEEP_LIME:
//                return LootTables.X;
//            case SHEEP_PINK:
//                return LootTables.Y;
//            case SHEEP_GRAY:
//                return LootTables.Z;
//            case SHEEP_LIGHT_GRAY:
//                return LootTables.aa;
//            case SHEEP_CYAN:
//                return LootTables.ab;
//            case SHEEP_PURPLE:
//                return LootTables.ac;
//            case SHEEP_BLUE:
//                return LootTables.ad;
//            case SHEEP_BROWN:
//                return LootTables.ae;
//            case SHEEP_GREEN:
//                return LootTables.af;
//            case SHEEP_RED:
//                return LootTables.ag;
//            case SHEEP_BLACK:
//                return LootTables.ah;
//            case FISHING:
//                return LootTables.ai;
//            case FISHING_JUNK:
//                return LootTables.aj;
//            case FISHING_TREASURE:
//                return LootTables.ak;
//            case FISHING_FISH:
//                return LootTables.al;
//            case CAT_MORNING_GIFT:
//                return LootTables.am;
//            case ARMORER_GIFT:
//                return LootTables.an;
//            case BUTCHER_GIFT:
//                return LootTables.ao;
//            case CARTOGRAPHER_GIFT:
//                return LootTables.ap;
//            case CLERIC_GIFT:
//                return LootTables.aq;
//            case FARMER_GIFT:
//                return LootTables.ar;
//            case FISHERMAN_GIFT:
//                return LootTables.as;
//            case FLETCHER_GIFT:
//                return LootTables.at;
//            case LEATHERWORKER_GIFT:
//                return LootTables.au;
//            case LIBRARIAN_GIFT:
//                return LootTables.av;
//            case MASON_GIFT:
//                return LootTables.aw;
//            case SHEPHERD_GIFT:
//                return LootTables.ax;
//            case TOOLSMITH_GIFT:
//                return LootTables.ay;
//            case WEAPONSMITH_GIFT:
//                return LootTables.az;
//            case PIGLIN_BARTERING:
//                return LootTables.aA;
            default -> LootTables.a;
        };
    }

	@Override
	public TerraformWorld getTerraformWorld() {
		return tw;
	}
}
