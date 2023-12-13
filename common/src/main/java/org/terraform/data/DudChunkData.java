package org.terraform.data;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

/**
 * A dud data structure used to discard writes from height transformation to more-quickly
 * get changes
 */
public class DudChunkData implements ChunkGenerator.ChunkData {
    @Override
    public int getMinHeight() {
        return 0;
    }

    @Override
    public int getMaxHeight() {
        return 0;
    }

    @NotNull
    @Override
    public Biome getBiome(int i, int i1, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public void setBlock(int i, int i1, int i2, @NotNull Material material) {

    }

    @Override
    public void setBlock(int i, int i1, int i2, @NotNull MaterialData materialData) {

    }

    @Override
    public void setBlock(int i, int i1, int i2, @NotNull BlockData blockData) {

    }

    @Override
    public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull Material material) {

    }

    @Override
    public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull MaterialData materialData) {

    }

    @Override
    public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull BlockData blockData) {

    }

    @NotNull
    @Override
    public Material getType(int i, int i1, int i2) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public MaterialData getTypeAndData(int i, int i1, int i2) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public BlockData getBlockData(int i, int i1, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public byte getData(int i, int i1, int i2) {
        throw new NotImplementedException();
    }
}
