package org.terraform.data;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;

public class SimpleBlock {
    @SerializedName("w")
    private final int x, y, z;
    PopulatorDataAbstract popData;
//	
//	public Location getLocation(){
//		return new Location(Bukkit.getWorld(world),x,y,z);
//	}

    public SimpleBlock(Location loc) {
        this.popData = new PopulatorDataPostGen(loc.getChunk());
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public SimpleBlock(PopulatorDataAbstract data, int x, int y, int z) {
        //this.world = world;
        this.popData = data;
        this.x = x;
        this.y = y;
        this.z = z;

    }

    public SimpleBlock(PopulatorDataAbstract data, Location loc) {
        //this.world = loc.getWorld().getName();
        this.popData = data;
        this.x = (int) loc.getX();
        this.y = (int) loc.getY();
        this.z = (int) loc.getZ();

    }

    public SimpleBlock(PopulatorDataAbstract data, Block b) {
        //this.world = b.getWorld().getName();
        this.popData = data;
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
        //this.data = b.getBlockData().getAsString();
    }

    public Vector getVector() {
        return new Vector(x, y, z);
    }

    public double distanceSquared(SimpleBlock other) {
        float selfX = (float) x;
        float selfY = (float) y;
        float selfZ = (float) z;
        float oX = (float) other.x;
        float oY = (float) other.y;
        float oZ = (float) other.z;

        return Math.pow(selfX - oX, 2) + Math.pow(selfY - oY, 2) + Math.pow(selfZ - oZ, 2);
    }

    public boolean sameLocation(SimpleBlock other) {
        return other.x == x && other.y == y && other.z == z;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public SimpleBlock untilSolid(BlockFace face) {
        SimpleBlock rel = this.getRelative(face);
        while (!rel.getType().isSolid())
            rel = rel.getRelative(face);

        return rel;
    }

    /**
     * Lenient set. Only replaces non-solid blocks.
     *
     * @return if the set was a success.
     */
    public boolean lsetType(Material type) {
        if (!getType().isSolid()) {
            setType(type);
            return true;
        }
        return false;
    }

    public boolean lsetBlockData(BlockData data) {
        if (!getType().isSolid()) {
            setBlockData(data);
            return true;
        }
        return false;
    }

    public BlockData getBlockData() {
        return popData.getBlockData(x, y, z);//Bukkit.createBlockData(getType());
    }

    public void setBlockData(BlockData dat) {
        if (popData.getType(x, y, z) == Material.WATER) {
            if (dat instanceof Waterlogged) {
                Waterlogged wl = (Waterlogged) dat;
                wl.setWaterlogged(true);
            }
        }
        popData.setBlockData(x, y, z, dat);
    }

    public SimpleBlock getRelative(int nx, int ny, int nz) {
        return new SimpleBlock(popData, x + nx, y + ny, z + nz);
    }

    public SimpleBlock getRelative(Vector v) {
        return new SimpleBlock(popData,
                (int) Math.round(x + v.getX()),
                (int) Math.round(y + v.getY()),
                (int) Math.round(z + v.getZ()));
    }

    public String getCoords() {
        return x + "," + y + ',' + z;
    }

    public SimpleBlock getRelative(BlockFace face) {
        return new SimpleBlock(popData, x + face.getModX(), y + face.getModY(), z + face.getModZ());
    }

    public SimpleBlock getRelative(BlockFace face, int count) {
        return new SimpleBlock(popData, x + face.getModX() * count, y + face.getModY() * count, z + face.getModZ() * count);
    }

    public int getChunkX() {
        return x / 16;
    }

    public int getChunkZ() {
        return z / 16;
    }

    public SimpleChunkLocation getSChunk(String world) {
        return new SimpleChunkLocation(world, getChunkX(), getChunkZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Material getType() {
        return popData.getType(x, y, z);
    }

    public void setType(Material type) {
        if (popData.getType(x, y, z) == Material.WATER) {
            BlockData data = Bukkit.createBlockData(type);
            if (data instanceof Waterlogged) {
                Waterlogged wl = (Waterlogged) data;
                wl.setWaterlogged(true);
                data = wl;
            }
            popData.setBlockData(x, y, z, data);
        } else
            popData.setType(x, y, z, type);

        //Setting leaves with setType will be persistent
        if (type.toString().contains("LEAVES")) {
            Leaves l = (Leaves) Bukkit.createBlockData(type);
            l.setPersistent(true);

            setBlockData(l);
        }
    }

    /**
     * @return the popData
     */
    public PopulatorDataAbstract getPopData() {
        return popData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((popData == null) ? 0 : popData.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SimpleBlock)) return false;
        SimpleBlock other = (SimpleBlock) obj;
        return popData == other.popData && x == other.x && z == other.z;
    }
}