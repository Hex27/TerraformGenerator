package org.terraform.data;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

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
    

    public SimpleBlock(PopulatorDataAbstract data, SimpleLocation sLoc) {
        //this.world = world;
        this.popData = data;
        this.x = sLoc.getX();
        this.y = sLoc.getY();
        this.z = sLoc.getZ();

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
    
    public SimpleBlock getAtY(int y) {
    	return new SimpleBlock(popData,x,y,z);
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
    
    public boolean isConnected(SimpleBlock other) {
    	for(BlockFace face:BlockUtils.sixBlockFaces) {
    		if(this.getRelative(face).equals(other))
    			return true;
    	}
    	return false;
    }
    
    public double distance(SimpleBlock other) {
    	
        return Math.sqrt(distanceSquared(other));
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

    public void RSolSetBlockData(BlockData data) {
        if (getType().isSolid())
            setBlockData(data);
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
    
    public void addEntity(EntityType type) {
    	popData.addEntity(x, y, z, type);
    }
    

    public int countAdjacentsThatMatchType(BlockFace[] faces, Material...types) {
    	int i = 0;
    	for(BlockFace face:faces) {
    		for(Material type:types)
    			if(getRelative(face).getType() == type)
    				i++;
    	}
    	return i;
    }
    
    public boolean doAdjacentsMatchType(BlockFace[] faces, Material...types) {
    	for(BlockFace face:faces) {
    		for(Material type:types)
    			if(getRelative(face).getType() == type)
    				return true;
    	}
    	return false;
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
        if (Tag.LEAVES.isTagged(type)) {
            //if (type.toString().contains("LEAVES")) {
            Leaves l = (Leaves) Bukkit.createBlockData(type);
            l.setPersistent(true);

            setBlockData(l);
        }
    }

    public void setType(Material... types) {
        setType(GenUtils.randMaterial(types));
    }

    public void lsetType(Material... types) {
        lsetType(GenUtils.randMaterial(types));
    }

    public void RSolSetType(Material type) {
        if (getType().isSolid())
            setType(type);
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
        return popData == other.popData && x == other.x && z == other.z && y == other.y;
    }

    public SimpleBlock getGround() {
        return new SimpleBlock(
                popData,
                x,
                GenUtils.getHighestGround(popData, x, z),
                z);
    }

    public SimpleBlock getGroundOrSeaLevel() {
    	int y = GenUtils.getHighestGround(popData, x, z);
    	if(y < TerraformGenerator.seaLevel) y = TerraformGenerator.seaLevel;
        return new SimpleBlock(
                popData,
                x,
                y,
                z);
    }

    public SimpleBlock getGroundOrDry() {
    	int y = GenUtils.getHighestGround(popData, x, z);
    	
    	while(y < TerraformGeneratorPlugin.injector.getMaxY() && (BlockUtils.isWet(this.getRelative(0,1,0))||
    			Tag.ICE.isTagged(this.getRelative(0,1,0).getType()))) y++;
        return new SimpleBlock(
                popData,
                x,
                y,
                z);
    }


	public SimpleBlock getUp() {
		return new SimpleBlock(popData,x,y+1,z);
	}
	public SimpleBlock getUp(int i) {
		return new SimpleBlock(popData,x,y+i,z);
	}
	
    /**
     * Gets the first solid block above this one
     * @param cutoff
     * @return
     */
    public SimpleBlock findCeiling(int cutoff) {
    	SimpleBlock ceil = this.getRelative(0, 1, 0);
        while (cutoff > 0) {
            if (ceil.getType().isSolid() && ceil.getType() != Material.LANTERN) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getRelative(0, 1, 0);
        }
        return null;
    }
    
    /**
     * Gets the first solid block below this one
     * @param cutoff
     * @return
     */
    public SimpleBlock findFloor(int cutoff) {
    	SimpleBlock floor = this.getRelative(0, -1, 0);
        while (cutoff > 0 && floor.getY() >= 0) {
            if (floor.getType().isSolid() && floor.getType() != Material.LANTERN) {
                return floor;
            }
            cutoff--;
            floor = floor.getRelative(0, -1, 0);
        }
        return null;
    }
    
    /**
     * Gets the first stone-like block below this one
     * @param cutoff
     * @return
     */
    public SimpleBlock findStonelikeFloor(int cutoff) {
    	SimpleBlock floor = this.getRelative(0, -1, 0);
        while (cutoff > 0 && floor.getY() >= 0) {
            if (BlockUtils.isStoneLike(floor.getType())) {
                return floor;
            }
            cutoff--;
            floor = floor.getRelative(0, -1, 0);
        }
        return null;
    }
    
    /**
     * Gets the first stone-like block above this one
     * @param cutoff
     * @return
     */
    public SimpleBlock findStonelikeCeiling(int cutoff) {
    	SimpleBlock ceil = this.getRelative(0, 1, 0);
        while (cutoff > 0) {
            if (BlockUtils.isStoneLike(ceil.getType())) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getRelative(0, 1, 0);
        }
        return null;
    }
    

    /**
     * Replaces everything in its way
     * @param height
     * @param rand
     * @param types
     */
    public void Pillar(int height, Material... types) {
    	Random rand = new Random();
        for (int i = 0; i < height; i++) {
            this.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }
    
    /**
     * Replaces everything in its way
     * @param height
     * @param rand
     * @param types
     */
    public void Pillar(int height, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
        	this.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }

    public void Pillar(int height, boolean pattern, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (Arrays.equals(new Material[]{Material.BARRIER}, types)) continue;
            if (!pattern)
            	this.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
            else if (types[i % types.length] != Material.BARRIER)
            	this.getRelative(0, i, 0).setType(types[i % types.length]);
        }
    }
    

    /**
     * Corrects all multiple facing block data in a pillar
     * @param height
     */
    public void CorrectMultipleFacing(int height) {
        for (int i = 0; i < height; i++) {
            BlockUtils.correctSurroundingMultifacingData(this.getRelative(0, i, 0));
        }
    }


    /**
     * Replaces until a solid block is reached.
     * @param height
     * @param rand
     * @param types
     */
    public int LPillar(int height, Random rand, Material... types) {
        return LPillar(height, false, rand, types);
    }

    /**
     * Replaces until a solid block is reached.
     * @param height
     * @param rand
     * @param types
     */
    public int LPillar(int height, boolean pattern, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (this.getRelative(0, i, 0).getType().isSolid()) return i;
            if (Arrays.equals(new Material[]{Material.BARRIER}, types)) continue;
            if (!pattern)
                this.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
            else
                this.getRelative(0, i, 0).setType(types[i % types.length]);
        }
        return height;
    }

    /**
     * Replaces non-solid blocks only
     * @param height
     * @param rand
     * @param types
     */
    public void RPillar(int height, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (!this.getRelative(0, i, 0).getType().isSolid())
                this.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }

    /**
     * Replaces non-cave air only
     * @param height
     * @param rand
     * @param types
     */
    public void CAPillar(int height, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (this.getRelative(0, i, 0).getType() != Material.CAVE_AIR)
                this.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }
    
    public void waterlog(int height) {
    	for (int i = 0; i < height; i++) {
    		if(this.getRelative(0,i,0).getBlockData() instanceof Waterlogged) {
    			Waterlogged log = (Waterlogged) (this.getRelative(0,i,0).getBlockData());
    			log.setWaterlogged(true);
    			this.getRelative(0,i,0).setBlockData(log);
    		}
    	}
    }

    public int downUntilSolid(Random rand, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (!this.getRelative(0, -depth, 0).getType().isSolid()) {
                this.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            } else break;
            depth++;
        }

        return depth;
    }

    public int blockfaceUntilSolid(int maxDepth, Random rand, BlockFace face, Material... types) {
        int depth = 0;
        while (depth <= maxDepth) {
            if (!this.getRelative(face).getType().isSolid()) {
                this.getRelative(face).setType(GenUtils.randMaterial(rand, types));
            } else break;
            depth++;
        }

        return depth;
    }
    
    public void downPillar(int h, Material... types) {
    	downPillar(new Random(),h,types);
    }

    public void downPillar(Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) break;
            this.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            depth++;
        }
    }

    public void downLPillar(Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) break;
            if (!this.getRelative(0, -depth, 0).getType().isSolid()) {
                this.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            } else break;
            depth++;
        }
    }

    public void downRPillar(Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) break;
            if (!this.getRelative(0, -depth, 0).getType().isSolid()) {
                this.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            }
            depth++;
        }
    }
    

    public SimpleBlock getDown(int i) {
        return this.getRelative(0, -i, 0);
    }
    
    public SimpleBlock getDown() {
        return this.getRelative(0, -1, 0);
    }

    
    public String toString() {
    	return x + "," + y + "," + z;
    }
}