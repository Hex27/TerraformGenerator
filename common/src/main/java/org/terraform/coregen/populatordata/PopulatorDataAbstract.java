package org.terraform.coregen.populatordata;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.TerraformWorld;

public abstract class PopulatorDataAbstract {
    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public abstract Material getType(int x, int y, int z);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public abstract BlockData getBlockData(int x, int y, int z);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public abstract void setType(int x, int y, int z, Material type);
    
    public void lsetType(int x, int y, int z, Material type) {
    	if(!getType(x,y,z).isSolid())
    		setType(x,y,z,type);
    }
    
    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public abstract void setBlockData(int x, int y, int z, BlockData data);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     * @return
     */
    public abstract Biome getBiome(int rawX, int rawZ);

    public abstract void addEntity(int rawX, int rawY, int rawZ, EntityType type);

    public abstract int getChunkX();

    public abstract int getChunkZ();

    public abstract void setSpawner(int rawX, int rawY, int rawZ, EntityType type);

    public abstract void lootTableChest(int x, int y, int z, TerraLootTable table);

    public abstract TerraformWorld getTerraformWorld();
    
    @Override
    public int hashCode() {
        return this.getClass().getCanonicalName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        return obj instanceof PopulatorDataAbstract;
    }
}
