package org.terraform.coregen.populatordata;

import java.util.EnumSet;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.TerraformWorld;

public abstract class PopulatorDataAbstract {
    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract Material getType(int x, int y, int z);
    public Material getType(Vector v){
        return getType((int)Math.round(v.getX()),(int)Math.round(v.getY()),(int)Math.round(v.getZ()));
    }

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract BlockData getBlockData(int x, int y, int z);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract void setType(int x, int y, int z, Material type);
    
    public void setType(int x, int y, int z, Material... type)
    {
    	setType(x,y,z,type[new Random().nextInt(type.length)]);
    }

    /**
     * This method will ROUND vector coordinates. Be very aware of that.
     */
    public void setType(Vector add, Material... mat) {
        setType((int)Math.round(add.getX()),
                (int)Math.round(add.getY()),
                (int)Math.round(add.getZ()), mat);
    }
    public void setBlockData(Vector add, BlockData data) {
        setBlockData((int)Math.round(add.getX()),
                (int)Math.round(add.getY()),
                (int)Math.round(add.getZ()), data);
    }

    public void lsetType(int x, int y, int z, Material... type) {
        if(!getType(x,y,z).isSolid())
            setType(x,y,z,type);
    }
    public void lsetType(Vector v, Material... type) {
        if(!getType(v).isSolid())
            setType(v,type);
    }

    /**
     * Set the material at the location if the current material is in
     * the enum set
     */
    public void rsetType(Vector v, EnumSet<Material> replaceable, Material... toSet)
    {
        if(!replaceable.contains(getType(v))) return;
        setType(v, toSet);
    }
    /**
     * Set the material at the location if the current material is in
     * the enum set
     */
    public void rsetBlockData(Vector v, EnumSet<Material> replaceable, BlockData data)
    {
        if(!replaceable.contains(getType(v))) return;
        setBlockData(v, data);
    }
    
    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract void setBlockData(int x, int y, int z, BlockData data);

    /**
     * Refers to raw x,y,z coords, not the chunk 0-15 coords.
     */
    public abstract Biome getBiome(int rawX, int rawZ);

    public abstract void addEntity(int rawX, int rawY, int rawZ, EntityType type);

    public abstract int getChunkX();

    public abstract int getChunkZ();

    public abstract void setSpawner(int rawX, int rawY, int rawZ, EntityType type);

    public abstract void lootTableChest(int x, int y, int z, TerraLootTable table);

    public abstract TerraformWorld getTerraformWorld();

    /**
     * @Deprecated this shit is a meaningless hashcode
     */
    @Override
    public int hashCode() {
        return this.getClass().getCanonicalName().hashCode();
    }


    /**
     * @Deprecated this shit is a meaningless comparison
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        return obj instanceof PopulatorDataAbstract;
    }
}
