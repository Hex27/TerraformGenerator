package org.terraform.v1_19_R3;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.storage.loot.LootTables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R3.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R3.util.RandomSourceWrapper;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.coregen.populatordata.IPopulatorDataBaseHeightAccess;
import org.terraform.coregen.populatordata.IPopulatorDataBeehiveEditor;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class PopulatorData extends PopulatorDataAbstract implements IPopulatorDataBaseHeightAccess, IPopulatorDataBeehiveEditor {
    private final int chunkX;
    private final int chunkZ;
    private int radius = 1;
    private final NMSChunkGenerator gen;
    GeneratorAccessSeed rlwa;
    IChunkAccess ica;
    
    private static final HashMap<EntityType,EntityTypes<?>> entityTypesDict = new HashMap<>();
    
    public PopulatorData(GeneratorAccessSeed rlwa, IChunkAccess ica, NMSChunkGenerator gen, int chunkX, int chunkZ) {
        this.rlwa = rlwa;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.gen = gen;
        this.ica = ica;
        
        if(entityTypesDict.isEmpty()) {
        	for(EntityType type:EntityType.values()) {
        		if(type == EntityType.ENDER_SIGNAL) continue;
        		if(type == EntityType.UNKNOWN) continue;
				try {
                    //EntityTypes.byString
					Optional<EntityTypes<?>> et = EntityTypes.a("minecraft:"+type.toString().toLowerCase(Locale.ENGLISH));
                    //EntityTypes<?> et = (EntityTypes<?>) EntityTypes.class.getDeclaredField(EntityTypeMapper.getObfsNameFromBukkitEntityType(type)).get(null);
                    //TerraformGeneratorPlugin.logger.info(type + ":" + et.isPresent());
                    et.ifPresent(entityTypes -> entityTypesDict.put(type, entityTypes));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
        	}
        }
    }
    
    public void setRadius(int radius) {
    	this.radius = radius;
    }

    public Material getType(int x, int y, int z) {
    	try {
        	//return rlwa.getType(x, y, z);
        	return CraftMagicNumbers.getMaterial(rlwa.a_(new BlockPosition(x, y, z)).b());
    	}catch(Exception e) {
        	Bukkit.getLogger().info("Error chunk: " + chunkX + "," + chunkZ + "--- Block Coords: " + 16*chunkX + "," + 16*chunkZ + " for coords " + x + "," + y + "," + z);
    		e.printStackTrace();
        }
    	return null;
    }

    public BlockData getBlockData(int x, int y, int z) {
        //return rlwa.getBlockData(x,y,z);
    	return CraftBlockData.fromData(rlwa.a_(new BlockPosition(x, y, z)));
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
    	if (Math.abs((x >> 4) - chunkX) > radius || Math.abs((z >> 4) - chunkZ) > radius) {
    		if(radius > 0)
    			NativeGeneratorPatcherPopulator.pushChange(rlwa.getMinecraftWorld().getWorld().getName(), x, y, z, Bukkit.createBlockData(type));
    		else
    			new Exception("Tried to call adjacent chunk with populator radius 0: (" + x + "," + y + "," + z  + ") for chunk (" + chunkX + "," + chunkZ + ")").printStackTrace();
        } else {
            rlwa.a(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), 0);
    	}
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
    	if (Math.abs((x >> 4) - chunkX) > radius || Math.abs((z >> 4) - chunkZ) > radius) {
    		if(radius > 0)
    			NativeGeneratorPatcherPopulator.pushChange(rlwa.getMinecraftWorld().getWorld().getName(), x, y, z, data);
    		else
    			new Exception("Tried to call adjacent chunk with populator radius 0: (" + x + "," + y + "," + z  + ") for chunk (" + chunkX + "," + chunkZ + ")").printStackTrace();
        } else {
        	rlwa.a(new BlockPosition(x, y, z), ((CraftBlockData) data).getState(), 0);
        }
    }

    //wtf
    public Biome getBiome(int rawX, int rawZ) {
        TerraformWorld tw = gen.getTerraformWorld();
        return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();
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
    	if (Math.abs((rawX >> 4) - chunkX) > 1 || Math.abs((rawZ >> 4) - chunkZ) > 1) {
    		TerraformGeneratorPlugin.logger.info("Failed to spawn " + type + " as it was out of bounds.");
    		return;
    	}
    	
    	//Use this method for thread safety.
    	CraftLimitedRegion clr = new CraftLimitedRegion(rlwa, ica.f());
    	net.minecraft.world.entity.Entity e = clr.createEntity(new Location(gen.getTerraformWorld().getWorld(),rawX,rawY,rawZ),
    			type.getEntityClass());
    	rlwa.b(e);
    	//TerraformGeneratorPlugin.logger.info("Spawned " + e.getType() + " at " + rawX + " " + rawY + " " + rawZ);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
        
        setType(rawX, rawY, rawZ, Material.SPAWNER);
        TileEntity tileentity = rlwa.c_(pos);

        if (tileentity instanceof TileEntityMobSpawner) {
            try {
                //Refer to WorldGenDungeons
//                TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) tileentity;
//
//                tileentitymobspawner.setEntityId(this.randomEntityId(randomsource), randomsource);

                //Fetch from ENTITY_TYPE (Q)'s map
            	//q is ENTITY_TYPE
            	EntityTypes<?> nmsEntity = entityTypesDict.get(type);
                if(nmsEntity == null) TerraformGeneratorPlugin.logger.error(type + " was not present in the entityTypesDict.");
                ((TileEntityMobSpawner) tileentity).a(nmsEntity, new RandomSourceWrapper(new Random()));
            } catch (IllegalArgumentException | SecurityException e) {
                e.printStackTrace();
            }
        } else {
            TerraformGeneratorPlugin.logger.error("Failed to fetch mob spawner entity at (" + "," + rawX + "," + rawY + "," + rawZ + ")");
        }
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        BlockPosition pos = new BlockPosition(x, y, z);
        TileEntityLootable.a(rlwa, RandomSource.a(gen.getTerraformWorld().getHashedRand(x, y, z).nextLong()), pos, getLootTable(table));
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
		case ANCIENT_CITY:
			return LootTables.P;
		case ANCIENT_CITY_ICE_BOX:
			return LootTables.Q;
        case RUINED_PORTAL:
            return LootTables.R;
		default:
			break;
        }
        return null;
    }

	@Override
	public TerraformWorld getTerraformWorld() {
		return gen.getTerraformWorld();
	}

	@Override
	public int getBaseHeight(int rawX, int rawZ) {
		//(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor, RandomState randomstate)
		return 100;
		//return gen.a(rawX, rawZ, HeightMap.Type.a, this.rlwa);
	}

	@Override
	public void setBeehiveWithBee(int rawX, int rawY, int rawZ) {
		BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
        //TerraformGeneratorPlugin.logger.info(IRegistry.X.b(EntityTypes.h).toString());
        setType(rawX, rawY, rawZ, Material.BEE_NEST);

        try {
            //TerraformGeneratorPlugin.logger.error("Failed to set beehive at (" + rawX + "," + rawY + "," + rawZ + ") " + BuiltInRegistries.h.b(entityTypesDict.get(EntityType.BEE)));
            TileEntityBeehive tileentity = (TileEntityBeehive) rlwa.c_(pos);
            if(tileentity == null)
            { //retry?
            	 setType(rawX, rawY, rawZ, Material.BEE_NEST);
            	 tileentity = (TileEntityBeehive) rlwa.c_(pos);
            }
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            //BuiltInRegistries.ENTITY_TYPE -> h
            //b is getKey
            nbttagcompound.a("id", "minecraft:bee");
            //TileEntityBeehive.storeBee
            tileentity.a(nbttagcompound, 0, false);
        } catch (NullPointerException | IllegalArgumentException | SecurityException e) {
             e.printStackTrace();
        }
	}
}
