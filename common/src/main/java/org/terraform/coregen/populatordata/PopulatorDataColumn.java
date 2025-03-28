package org.terraform.coregen.populatordata;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.TerraformWorld;

/**
 * Same thing as the spigot one, but it will forcefully constrain internal x and z
 */
public class PopulatorDataColumn extends PopulatorDataAbstract {
    private final PopulatorDataAbstract delegate;
    int constrainX = 0;
    int constrainZ = 0;

    public PopulatorDataColumn(PopulatorDataAbstract delegate)
    {
        this.delegate = delegate;
    }

    public void setConstraints(int constrainX, int constrainZ)
    {
        this.constrainX = constrainX;
        this.constrainZ = constrainZ;
    }

    @Override
    public @NotNull Material getType(int x, int y, int z) {
        if (x != constrainX || z != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Read Violation");
        }
        return delegate.getType(x, y, z);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        if (x != constrainX || z != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Read Violation");
        }
        return delegate.getBlockData(x, y, z);
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
        if (x != constrainX || z != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Write Violation");
        }
        delegate.setType(x, y, z, type);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        if (x != constrainX || z != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Write Violation");
        }
        delegate.setBlockData(x, y, z, data);
    }

    @Override
    public Biome getBiome(int rawX, int rawZ) {
        return delegate.getBiome(rawX, rawZ);
    }

    @Override
    public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
        if (rawX != constrainX || rawZ != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Write Violation");
        }
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
        if (rawX != constrainX || rawZ != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Write Violation");
        }
        delegate.setSpawner(rawX, rawY, rawZ, type);
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        if (x != constrainX || z != constrainZ) {
            throw new IllegalArgumentException("Column Constraint Write Violation");
        }
        delegate.lootTableChest(x, y, z, table);
    }

    @Override
    public @NotNull TerraformWorld getTerraformWorld() {
        return delegate.getTerraformWorld();
    }
}
