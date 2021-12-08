package org.terraform.v1_18_R1;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.TerraformWorld;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.vehicle.EntityMinecartChest;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.IStructureAccess;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WorldGenMonumentPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.LootTables;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
    	return parent.getType(x, y, z);
        //IBlockData ibd = ica.getType(new BlockPosition(x, y, z));
        //return CraftBlockData.fromData(ibd).getMaterial();
    }

    public BlockData getBlockData(int x, int y, int z) {
       return parent.getBlockData(x, y, z);
    	// IBlockData ibd = ica.getType(new BlockPosition(x, y, z));
       // return CraftBlockData.fromData(ibd);
    }
    
	@Override
	public void setBiome(int rawX, int rawY, int rawZ, BiomeBank bank) {
		DedicatedServer dedicatedserver = ((CraftServer) Bukkit.getServer()).getServer();
		IRegistryWritable<BiomeBase> registrywritable = dedicatedserver.aV().b(IRegistry.aR);
        BiomeBase targetBiome;
		if(bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {
			
			targetBiome = CraftBlock.biomeToBiomeBase(ica.biomeRegistry, bank.getHandler().getBiome());
		} else {
			ResourceKey<BiomeBase> rkey = ResourceKey.a(IRegistry.aR, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
	        BiomeBase base = registrywritable.a(rkey);
	        if(base == null) {
	        	String[] split = bank.getHandler().getCustomBiome().getKey().split(":");
	            ResourceKey<BiomeBase> newrkey = ResourceKey.a(IRegistry.aR, new MinecraftKey(split[0],split[1]));
	            base = registrywritable.a(newrkey);
	        }
			
			if(base != null) {
				targetBiome = base;
			}
			else
				targetBiome =  CraftBlock.biomeToBiomeBase(ica.biomeRegistry, bank.getHandler().getBiome());
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
    	parent.setType(x, y, z, type);
        //ica.setType(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
    	parent.setBlockData(x, y, z, data);
    	//ica.setType(new BlockPosition(x, y, z)
        //        , ((CraftBlockData) data).getState(), false);
    }

//	public Biome getBiome(int rawX, int rawY, int rawZ){
//		return CraftBlock.biomeBaseToBiome(gen.getBiome(ica.d(), new BlockPosition(rawX,rawY,rawZ)));
//	}

    public Biome getBiome(int rawX, int rawZ) {
    	return parent.getBiome(rawX, rawZ);
        //return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), y).getHandler().getBiome();//Biome.valueOf(ica
        // .getBiome(rawX, rawY, rawZ).l().replace("biome.minecraft.", "").toUpperCase());
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
        TileEntityLootable.a(ica, tw.getHashedRand(x, y, z), pos, getLootTable(table));
    }

    @Override
    public void registerNaturalSpawns(NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1) {

    	StructureGenerator<?> generator = StructureGenerator.m;//Monument
    	switch(type) {
    	case GUARDIAN:
    		generator = StructureGenerator.m; //Ocean Monument
    		break;
    	case PILLAGER:
    		generator = StructureGenerator.c; //Pillager Outpost
    		break;
    	}
    	StructurePiece customBoundPiece = new WorldGenMonumentPieces
        .WorldGenMonumentPiece1(new Random(), x0, z0,
        EnumDirection.a);
    	
    	@SuppressWarnings("serial")
		PiecesContainer container = new PiecesContainer(new ArrayList<StructurePiece>() {{add(customBoundPiece);}});
        @SuppressWarnings({ "rawtypes", "unchecked" })
		StructureStart<?> start = new StructureStart(generator, new ChunkCoordIntPair(chunkX, chunkZ), 0, container);

    	try {
        	Field i = StructureStart.class.getDeclaredField("g");
        	i.setAccessible(true);
        	i.set(start, new StructureBoundingBox(x0, y0, z0, x1, y1, z1));
        }
    	catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) 
    	{
    		e.printStackTrace();
    	}
        //StructureBoundingBox b = start.a(); //Recalculate bounding box
//    	
//    	start.c.add(new WorldGenMonumentPieces
//                .WorldGenMonumentPiece1(new Random(), x0, z0,
//                EnumDirection.a));
    	
    	IStructureAccess sa = ica; //IStructureAccess is FeatureAccess
        sa.a( //setStartForFeature
        		generator, //Get ID
                start);
        
        //addReferenceForFeature
        sa.a(generator, new ChunkCoordIntPair(chunkX, chunkZ).a()); //toLong
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
        switch (table) {
        case EMPTY:
            return LootTables.a;
        case SPAWN_BONUS_CHEST:
            return LootTables.b;
        case END_CITY_TREASURE:
            return LootTables.c;
        case SIMPLE_DUNGEON:
            return LootTables.d;
        case VILLAGE_WEAPONSMITH:
            return LootTables.e;
        case VILLAGE_TOOLSMITH:
            return LootTables.f;
        case VILLAGE_ARMORER:
            return LootTables.g;
        case VILLAGE_CARTOGRAPHER:
            return LootTables.h;
        case VILLAGE_MASON:
            return LootTables.i;
        case VILLAGE_SHEPHERD:
            return LootTables.j;
        case VILLAGE_BUTCHER:
            return LootTables.k;
        case VILLAGE_FLETCHER:
            return LootTables.l;
        case VILLAGE_FISHER:
            return LootTables.m;
        case VILLAGE_TANNERY:
            return LootTables.n;
        case VILLAGE_TEMPLE:
            return LootTables.o;
        case VILLAGE_DESERT_HOUSE:
            return LootTables.p;
        case VILLAGE_PLAINS_HOUSE:
            return LootTables.q;
        case VILLAGE_TAIGA_HOUSE:
            return LootTables.r;
        case VILLAGE_SNOWY_HOUSE:
            return LootTables.s;
        case VILLAGE_SAVANNA_HOUSE:
            return LootTables.t;
        case ABANDONED_MINESHAFT:
            return LootTables.u;
        case NETHER_BRIDGE:
            return LootTables.v;
        case STRONGHOLD_LIBRARY:
            return LootTables.w;
        case STRONGHOLD_CROSSING:
            return LootTables.x;
        case STRONGHOLD_CORRIDOR:
            return LootTables.y;
        case DESERT_PYRAMID:
            return LootTables.z;
        case JUNGLE_TEMPLE:
            return LootTables.A;
        case JUNGLE_TEMPLE_DISPENSER:
            return LootTables.B;
        case IGLOO_CHEST:
            return LootTables.C;
        case WOODLAND_MANSION:
            return LootTables.D;
        case UNDERWATER_RUIN_SMALL:
            return LootTables.E;
        case UNDERWATER_RUIN_BIG:
            return LootTables.F;
        case BURIED_TREASURE:
            return LootTables.G;
        case SHIPWRECK_MAP:
            return LootTables.H;
        case SHIPWRECK_SUPPLY:
            return LootTables.I;
        case SHIPWRECK_TREASURE:
            return LootTables.J;
        case PILLAGER_OUTPOST:
            return LootTables.K;
//        case BASTION_TREASURE:
//            return LootTables.L;
//        case BASTION_OTHER:
//            return LootTables.M;
//        case BASTION_BRIDGE:
//            return LootTables.N;
//        case BASTION_HOGLIN_STABLE:
//            return LootTables.O;
//        case RUINED_PORTAL:
//            return LootTables.P;
//        case SHEEP_WHITE:
//            return LootTables.Q;
//        case SHEEP_ORANGE:
//            return LootTables.R;
//        case SHEEP_MAGENTA:
//            return LootTables.S;
//        case SHEEP_LIGHT_BLUE:
//            return LootTables.T;
//        case SHEEP_YELLOW:
//            return LootTables.U;
//        case SHEEP_LIME:
//            return LootTables.V;
//        case SHEEP_PINK:
//            return LootTables.W;
//        case SHEEP_GRAY:
//            return LootTables.X;
//        case SHEEP_LIGHT_GRAY:
//            return LootTables.Y;
//        case SHEEP_CYAN:
//            return LootTables.Z;
//        case SHEEP_PURPLE:
//            return LootTables.aa;
//        case SHEEP_BLUE:
//            return LootTables.ab;
//        case SHEEP_BROWN:
//            return LootTables.ac;
//        case SHEEP_GREEN:
//            return LootTables.ad;
//        case SHEEP_RED:
//            return LootTables.ae;
//        case SHEEP_BLACK:
//            return LootTables.af;
//        case FISHING:
//            return LootTables.ag;
//        case FISHING_JUNK:
//            return LootTables.ah;
//        case FISHING_TREASURE:
//            return LootTables.ai;
//        case FISHING_FISH:
//            return LootTables.aj;
//        case CAT_MORNING_GIFT:
//            return LootTables.ak;
//        case ARMORER_GIFT:
//            return LootTables.al;
//        case BUTCHER_GIFT:
//            return LootTables.am;
//        case CARTOGRAPHER_GIFT:
//            return LootTables.an;
//        case CLERIC_GIFT:
//            return LootTables.ao;
//        case FARMER_GIFT:
//            return LootTables.ap;
//        case FISHERMAN_GIFT:
//            return LootTables.aq;
//        case FLETCHER_GIFT:
//            return LootTables.ar;
//        case LEATHERWORKER_GIFT:
//            return LootTables.as;
//        case LIBRARIAN_GIFT:
//            return LootTables.at;
//        case MASON_GIFT:
//            return LootTables.au;
//        case SHEPHERD_GIFT:
//            return LootTables.av;
//        case TOOLSMITH_GIFT:
//            return LootTables.aw;
//        case WEAPONSMITH_GIFT:
//            return LootTables.ax;
//        case PIGLIN_BARTERING:
//            return LootTables.ay
        }
        return null;
    }

	@Override
	public TerraformWorld getTerraformWorld() {
		return tw;
	}
}
