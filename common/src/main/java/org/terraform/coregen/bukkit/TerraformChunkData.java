package org.terraform.coregen.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.IPopulatorDataBaseHeightAccess;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;

@SuppressWarnings("deprecation")
public class TerraformChunkData implements ChunkData {

    private final PopulatorDataAbstract popData;

    public TerraformChunkData(PopulatorDataAbstract popData)
    {
        this.popData = popData;
    }

    // private static boolean debug = true;
    public int getBaseHeight(int x, int z) {
        int height = -64;
        if (popData instanceof IPopulatorDataBaseHeightAccess) {
            height = ((IPopulatorDataBaseHeightAccess) popData).getBaseHeight(
                    x + (popData.getChunkX() * 16),
                    z + (popData.getChunkZ() * 16)
            );
        }
        return height;
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        return popData.getBiome(x + (popData.getChunkX() * 16), z + (popData.getChunkZ() * 16));
    }

    @Override
    public @NotNull BlockData getBlockData(int x, int y, int z) {
        return popData.getBlockData(x + (popData.getChunkX() * 16), y, z + (popData.getChunkZ() * 16));
    }

    @Override
    public byte getData(int x, int y, int z) {
        throw new UnsupportedOperationException("getData was called on TerraformChunkData!");
    }

    @Override
    public int getMaxHeight() {
        return popData.getTerraformWorld().maxY;
    }

    @Override
    public int getMinHeight() {
        return popData.getTerraformWorld().minY;
    }

    @Override
    public @NotNull Material getType(int x, int y, int z) {
        return popData.getType(x + (popData.getChunkX() * 16), y, z + (popData.getChunkZ() * 16));
    }

    @Override
    public @NotNull MaterialData getTypeAndData(int x, int y, int z) {
        throw new UnsupportedOperationException("getTypeAndData was called on TerraformChunkData with MaterialData!");
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Material arg3) {
        // TerraformGeneratorPlugin.logger.info("Called setBlock at " + x + "," + y + "," + z);
        popData.setType(x + (popData.getChunkX() * 16), y, z + (popData.getChunkZ() * 16), arg3);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull MaterialData arg3) {
        throw new UnsupportedOperationException("setBlock was called on TerraformChunkData with MaterialData!");
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull BlockData arg3) {
        popData.setBlockData(x + (popData.getChunkX() * 16), y, z + (popData.getChunkZ() * 16), arg3);
    }

    @Override
    public void setRegion(int x, int y, int z, int x2, int y2, int z2, @NotNull Material arg6) {
        throw new UnsupportedOperationException("setRegion was called on TerraformChunkData!");
    }

    @Override
    public void setRegion(int x, int y, int z, int x2, int y2, int z2, @NotNull MaterialData arg6) {
        throw new UnsupportedOperationException("setRegion was called on TerraformChunkData with MaterialData!");
    }

    @Override
    public void setRegion(int x, int y, int z, int x2, int y2, int z2, @NotNull BlockData arg6) {
        throw new UnsupportedOperationException("setRegion was called on TerraformChunkData!");
    }

}
