package org.terraform.v1_17_R1;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import net.minecraft.core.BlockPosition;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootTables;

public class PopulatorData extends PopulatorDataAbstract {
    private final int chunkX;
    private final int chunkZ;
    private final NMSChunkGenerator gen;
    RegionLimitedWorldAccess rlwa;

    public PopulatorData(RegionLimitedWorldAccess rlwa, NMSChunkGenerator gen, int chunkX, int chunkZ) {
        this.rlwa = rlwa;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.gen = gen;
    }

    public Material getType(int x, int y, int z) {
        IBlockData ibd = rlwa.getType(new BlockPosition(x, y, z));
        return CraftBlockData.fromData(ibd).getMaterial();
    }

    public BlockData getBlockData(int x, int y, int z) {
        IBlockData ibd = rlwa.getType(new BlockPosition(x, y, z));
        return CraftBlockData.fromData(ibd);
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
        //Cannot place block outside 3x3 radius.
        if (Math.abs((x >> 4) - chunkX) > 1 || Math.abs((z >> 4) - chunkZ) > 1) {
            NativeGeneratorPatcherPopulator.pushChange(rlwa.getMinecraftWorld().getWorld().getName(), x, y, z, Bukkit.createBlockData(type));
        } else {
            rlwa.setTypeAndData(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), 0);
        }
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
        if (Math.abs((x >> 4) - chunkX) > 1 || Math.abs((z >> 4) - chunkZ) > 1) {
            NativeGeneratorPatcherPopulator.pushChange(rlwa.getMinecraftWorld().getWorld().getName(), x, y, z, data);
        } else {
            rlwa.setTypeAndData(new BlockPosition(x, y, z)
                    , ((CraftBlockData) data).getState(), 0);
        }
    }

    
    public Biome getBiome(int rawX, int rawZ) {
        TerraformWorld tw = gen.getTerraformWorld();
        //int y = org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ);
        return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), y).getHandler().getBiome();//Biome.valueOf
        // (rlwa.getBiome(rawX, rawY, rawZ).l().replace("biome.minecraft.", "").toUpperCase());
    }

//	public Biome getBiome(int rawX, int rawY, int rawZ){
//		return CraftBlock.biomeBaseToBiome(gen.getBiome(rlwa.d(), new BlockPosition(rawX,rawY,rawZ)));
//	}

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
        try {
            EntityTypes<?> et = (EntityTypes<?>) EntityTypes.class.getDeclaredField(EntityTypeMapper.getObfsNameFromBukkitEntityType(type)).get(null);
            Entity e = et.a(rlwa.getMinecraftWorld());
            e.setPositionRotation((double) rawX + 0.5D, rawY, (double) rawZ + 0.5D, 0.0F, 0.0F);
            if (e instanceof EntityInsentient) {
                ((EntityInsentient) e).setPersistent();
                ((EntityInsentient) e).prepare(rlwa, rlwa.getDamageScaler(new BlockPosition(rawX, rawY, rawZ)), EnumMobSpawn.d, null, null); //EnumMobSpawn.STRUCTURE
            }

            rlwa.addEntity(e);
        } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
        //Blocks.spawner
        rlwa.setTypeAndData(pos, Blocks.bV.getBlockData(), 2);
        TileEntity tileentity = rlwa.getTileEntity(pos);

        if (tileentity instanceof TileEntityMobSpawner) {
            try {
                ((TileEntityMobSpawner) tileentity).getSpawner().setMobName((EntityTypes<?>) EntityTypes.class.getField(type.toString()).get(null));
            } catch (IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        } else {
            TerraformGeneratorPlugin.logger.error("Failed to fetch mob spawner entity at (" + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")");
            //WorldGenDungeons.LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", blockposition.getX(), blockposition.getY(), blockposition.getZ());
        }
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        BlockPosition pos = new BlockPosition(x, y, z);
        TileEntityLootable.a(rlwa, rlwa.getRandom(), pos, getLootTable(table));
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
        case RUINED_PORTAL:
            return LootTables.P;
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
}
