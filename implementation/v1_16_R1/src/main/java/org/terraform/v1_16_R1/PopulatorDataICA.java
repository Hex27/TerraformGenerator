package org.terraform.v1_16_R1;

import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R1.IStructureAccess;
import net.minecraft.server.v1_16_R1.StructureGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R1.block.data.CraftBlockData;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.PopulatorDataICAAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Random;

public class PopulatorDataICA extends PopulatorDataICAAbstract {
    IChunkAccess ica;
    int chunkX;
    int chunkZ;
    WorldServer ws;
    TerraformWorld tw;

    public PopulatorDataICA(TerraformWorld tw, WorldServer ws, IChunkAccess ica, int chunkX, int chunkZ) {
        this.ica = ica;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.ws = ws;
        this.tw = tw;
    }

    public Material getType(int x, int y, int z) {
        IBlockData ibd = ica.getType(new BlockPosition(x, y, z));
        return CraftBlockData.fromData(ibd).getMaterial();
    }

    public BlockData getBlockData(int x, int y, int z) {
        IBlockData ibd = ica.getType(new BlockPosition(x, y, z));
        return CraftBlockData.fromData(ibd);
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
        ica.setType(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
        ica.setType(new BlockPosition(x, y, z)
                , ((CraftBlockData) data).getState(), false);
    }

    public Biome getBiome(int rawX, int rawZ) {
        //int y = org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ);
        return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), y).getHandler().getBiome();//Biome.valueOf(ica
        // .getBiome(rawX, rawY, rawZ).l().replace("biome.minecraft.", "").toUpperCase());
    }

//	public Biome getBiome(int rawX, int rawY, int rawZ){
//		return CraftBlock.biomeBaseToBiome(gen.getBiome(ica.d(), new BlockPosition(rawX,rawY,rawZ)));
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
    @SuppressWarnings("rawtypes")
    public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
		EntityTypes et;
        try {
            et = (EntityTypes) EntityTypes.class.getDeclaredField(type.toString()).get(null);
            Entity e = et.a(ws.getMinecraftWorld());
            e.setPositionRotation((double) rawX + 0.5D, rawY, (double) rawZ + 0.5D, 0.0F, 0.0F);
            if (e instanceof EntityInsentient) {
                ((EntityInsentient) e).setPersistent();
                ((EntityInsentient) e).prepare(ws, ws.getDamageScaler(new BlockPosition(rawX, rawY, rawZ)), EnumMobSpawn.STRUCTURE, null, null);
            }

            ws.addEntity(e);
        } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
        ica.setType(pos, Blocks.SPAWNER.getBlockData(), true);
        TileEntity tileentity = ica.getTileEntity(pos);

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
        TileEntityLootable.a(ica, tw.getHashedRand(x, y, z), pos, getLootTable(table));
    }

    private MinecraftKey getLootTable(TerraLootTable table) {
        switch (table) {
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
        }
        return null;
    }

    @Override
    public void registerNaturalSpawns(NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1) {

    	String tagName = "ocean_monument";
    	StructureGenerator<?> generator = StructureGenerator.MONUMENT;
    	switch(type) {
    	case GUARDIAN:
    		tagName = "ocean_monument";
    		generator = StructureGenerator.MONUMENT;
    		break;
    	case PILLAGER:
    		tagName = "pillager_outpost";
    		generator = StructureGenerator.PILLAGER_OUTPOST;
    		break;
    	}
    	
        TerraStructureStart start = new TerraStructureStart(tagName,
        		generator, chunkX, chunkZ, null, z1, z1);
        start.setStructureBounds(x0, y0, z0, x1, y1, z1);
        IStructureAccess sa = ica;
        sa.a( //setStartForFeature
        		generator, //Get ID
                start);
        sa.a(generator, new ChunkCoordIntPair(chunkX, chunkZ).pair());
    }

    @Override
    public void spawnMinecartWithChest(int x, int y, int z, TerraLootTable table, Random random) {

        EntityMinecartChest entityminecartchest = new EntityMinecartChest(
                ws.getMinecraftWorld(),
                (float) x + 0.5F,
                (float) y + 0.5F,
                (float) z + 0.5F);

        entityminecartchest.setLootTable(getLootTable(table), random.nextLong());
        ws.addEntity(entityminecartchest);
    }


}
