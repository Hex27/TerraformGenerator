package org.terraform.coregen.populatordata;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.TerraformWorld;

import java.util.EnumSet;
import java.util.Random;

public abstract class PopulatorDataAbstract {
    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     * why the fuck were you nullable
     */
    public abstract @NotNull Material getType(int x, int y, int z);

    public @NotNull Material getType(@NotNull Vector v) {
        return getType((int) Math.round(v.getX()), (int) Math.round(v.getY()), (int) Math.round(v.getZ()));
    }

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract @NotNull BlockData getBlockData(int x, int y, int z);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract void setType(int x, int y, int z, Material type);

    public void setType(int x, int y, int z, Material @NotNull ... type)
    {
        setType(x, y, z, type[new Random().nextInt(type.length)]);
    }

    /**
     * This method will ROUND vector coordinates. Be very aware of that.
     */
    public void setType(@NotNull Vector add, Material... mat) {
        setType((int) Math.round(add.getX()), (int) Math.round(add.getY()), (int) Math.round(add.getZ()), mat);
    }

    public void setBlockData(@NotNull Vector add, BlockData data) {
        setBlockData((int) Math.round(add.getX()), (int) Math.round(add.getY()), (int) Math.round(add.getZ()), data);
    }
    public void lsetBlockData(int x, int y, int z, @NotNull BlockData data){
        if (!getType(x, y, z).isSolid()) {
            setBlockData(x, y, z, data);
        }
    }
    public void lsetType(int x, int y, int z, @NotNull Material... type) {
        if (!getType(x, y, z).isSolid()) {
            setType(x, y, z, type);
        }
    }

    public void lsetType(@NotNull Vector v, @NotNull Material... type) {
        if (!getType(v).isSolid()) {
            setType(v, type);
        }
    }

    /**
     * Set the material at the location if the current material is in
     * the enum set
     */
    public void rsetType(@NotNull Vector v, @NotNull EnumSet<Material> replaceable, Material... toSet)
    {
        if (!replaceable.contains(getType(v))) {
            return;
        }
        setType(v, toSet);
    }

    /**
     * Set the material at the location if the current material is in
     * the enum set
     */
    public void rsetBlockData(@NotNull Vector v, @NotNull EnumSet<Material> replaceable, BlockData data)
    {
        if (!replaceable.contains(getType(v))) {
            return;
        }
        setBlockData(v, data);
    }

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract void setBlockData(int x, int y, int z, @NotNull BlockData data);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract @Nullable Biome getBiome(int rawX, int rawZ);

    public abstract void addEntity(int rawX, int rawY, int rawZ, EntityType type);

    public abstract int getChunkX();

    public abstract int getChunkZ();

    public abstract void setSpawner(int rawX, int rawY, int rawZ, EntityType type);

    public abstract void lootTableChest(int x, int y, int z, TerraLootTable table);

    public abstract @NotNull TerraformWorld getTerraformWorld();

    /**
     * @deprecated this shit is a meaningless hashcode
     */
    @Deprecated
    @Override
    public int hashCode() {
        return this.getClass().getCanonicalName().hashCode();
    }


    /**
     * @deprecated this shit is a meaningless comparison
     */
    @Deprecated
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return obj instanceof PopulatorDataAbstract;
    }
}
