package org.terraform.coregen.populatordata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;

import java.util.HashMap;

public class PopulatorDataWriteTracker extends PopulatorDataAbstract{
    private final PopulatorDataAbstract delegate;
    public final HashMap<SimpleLocation, BlockData> written;
    public PopulatorDataWriteTracker(PopulatorDataAbstract delegate) {
        this.delegate = delegate;
        this.written = new HashMap<>();
    }

    @Override
    public @NotNull Material getType(int x, int y, int z) {
        return delegate.getType(x, y, z);
    }

    @Override
    public @NotNull BlockData getBlockData(int x, int y, int z) {
        return delegate.getBlockData(x, y, z);
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
        written.put(new SimpleLocation(x,y,z), Bukkit.createBlockData(type));
        delegate.setType(x, y, z, type);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        written.put(new SimpleLocation(x,y,z), data);
        delegate.setBlockData(x, y, z, data);
    }

    @Override
    public @Nullable Biome getBiome(int rawX, int rawZ) {
        return delegate.getBiome(rawX, rawZ);
    }

    @Override
    public void addEntity(float rawX, float rawY, float rawZ, EntityType type) {
        delegate.addEntity(rawX, rawY, rawZ, type);
    }

    @Override
    public int getChunkX() {
        return delegate.getChunkX();
    }

    @Override
    public int getChunkZ() {
        return delegate.getChunkZ();
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        delegate.setSpawner(rawX, rawY, rawZ, type);
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        delegate.lootTableChest(x, y, z, table);
    }

    @Override
    public @NotNull TerraformWorld getTerraformWorld() {
        return delegate.getTerraformWorld();
    }

    @Override
    public boolean isInBound(int x, int y, int z) {
        return delegate.isInBound(x, y, z);
    }
}
