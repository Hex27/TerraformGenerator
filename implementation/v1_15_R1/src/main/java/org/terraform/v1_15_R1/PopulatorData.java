package org.terraform.v1_15_R1;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.GroupDataEntity;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.LootTables;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.RegionLimitedWorldAccess;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityLootable;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

public class PopulatorData extends PopulatorDataAbstract {
    RegionLimitedWorldAccess rlwa;
    int chunkX;
    int chunkZ;
    NMSChunkGenerator gen;

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
        rlwa.setTypeAndData(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), 0);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
        rlwa.setTypeAndData(new BlockPosition(x, y, z)
                , ((CraftBlockData) data).getState(), 0);
    }

    public Biome getBiome(int rawX, int rawY, int rawZ) {
        TerraformWorld tw = TerraformWorld.get(rlwa.getWorldData().getName(), rlwa.getWorldData().getSeed());
        int y = org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ);
        return tw.getBiomeBank(rawX, y, rawZ).getHandler().getBiome();//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), y).getHandler().getBiome();//Biome.valueOf
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
        EntityTypes et;
        try {
            et = (EntityTypes) EntityTypes.class.getDeclaredField(type.toString()).get(null);
            Entity e = et.a(rlwa.getMinecraftWorld());
            e.setPositionRotation((double) rawX + 0.5D, (double) rawY, (double) rawZ + 0.5D, 0.0F, 0.0F);
            if(e instanceof EntityInsentient) {
                ((EntityInsentient) e).setPersistent();
                ((EntityInsentient) e).prepare(rlwa, rlwa.getDamageScaler(new BlockPosition(rawX, rawY, rawZ)), EnumMobSpawn.STRUCTURE, (GroupDataEntity) null,
                        (NBTTagCompound) null);
            }

            rlwa.addEntity(e);
        } catch(IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
        rlwa.setTypeAndData(pos, Blocks.SPAWNER.getBlockData(), 2);
        TileEntity tileentity = rlwa.getTileEntity(pos);

        if(tileentity instanceof TileEntityMobSpawner) {
            try {
                ((TileEntityMobSpawner) tileentity).getSpawner().setMobName((EntityTypes<?>) EntityTypes.class.getField(type.toString()).get(null));
            } catch(IllegalArgumentException | IllegalAccessException
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
        switch(table) {
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
        }
        return null;
    }


}
