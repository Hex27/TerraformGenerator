package org.terraform.coregen.populatordata;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class PopulatorDataRecursiveICA extends PopulatorDataPostGen {

    private final @NotNull World w;
    private final @NotNull Chunk c;
    private final ConcurrentHashMap<SimpleChunkLocation, PopulatorDataICAAbstract> loadedChunks = new ConcurrentHashMap<>();

    public PopulatorDataRecursiveICA(@NotNull Chunk c) {
        super(c);
        this.c = c;
        this.w = c.getWorld();
    }

    /**
     * @return The PopulatorDataICA related to those coords.
     */
    private @NotNull PopulatorDataICAAbstract getData(int x, int z) {
        SimpleChunkLocation scl = new SimpleChunkLocation(w.getName(), x, z);
        synchronized (loadedChunks) {
            return loadedChunks.computeIfAbsent(scl, k -> {
                int chunkX = x >> 4;
                int chunkZ = z >> 4;
                if (!w.isChunkLoaded(chunkX, chunkZ)) {
                    w.loadChunk(chunkX, chunkZ);
                }
                PopulatorDataICAAbstract data = TerraformGeneratorPlugin.injector.getICAData(w.getChunkAt(
                        chunkX,
                        chunkZ
                ));
                loadedChunks.put(scl, data);
                return data;
            });
        }
    }

    @Override
    public @NotNull Material getType(int x, int y, int z) {
        return getData(x, z).getType(x, y, z);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return getData(x, z).getBlockData(x, y, z);
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
        getData(x, z).setType(x, y, z, type);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        getData(x, z).setBlockData(x, y, z, data);
    }

    @Override
    public Biome getBiome(int rawX, int rawZ) {
        return getData(rawX, rawZ).getBiome(rawX, rawZ);
    }

    @Override
    public void addEntity(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        getData(rawX, rawZ).addEntity(rawX, rawY, rawZ, type);
    }

    @Override
    public int getChunkX() {
        return c.getX();
    }

    @Override
    public int getChunkZ() {
        return c.getZ();
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        getData(rawX, rawZ).setSpawner(rawX, rawY, rawZ, type);
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        getData(x, z).lootTableChest(x, y, z, table);
    }

}
