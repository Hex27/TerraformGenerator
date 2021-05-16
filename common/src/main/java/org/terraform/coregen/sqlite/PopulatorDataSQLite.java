package org.terraform.coregen.sqlite;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;

public class PopulatorDataSQLite extends PopulatorDataAbstract {
    private final ChunkData c;
    private final int chunkX;
    private final int chunkZ;

    public PopulatorDataSQLite(int chunkX, int chunkZ, ChunkData c) {
        this.c = c;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @SuppressWarnings("unused")
    private boolean isInBounds(int x, int z) {
        if (x < chunkX * 16) return false;
        if (x > (chunkX * 16) + 15) return false;
        if (z < chunkZ * 16) return false;
        return z <= (chunkZ * 16) + 15;
    }

    @Override
    public Material getType(int x, int y, int z) {
        return null;
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return null;
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
        // TODO Auto-generated method stub

    }

    @Override
    public Biome getBiome(int rawX, int rawZ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getChunkX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getChunkZ() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        // TODO Auto-generated method stub

    }
}
