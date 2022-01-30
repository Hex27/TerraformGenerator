package org.terraform.data;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.utils.BlockUtils;

public class Wall extends SimpleBlock{
    private final BlockFace direction;

    public Wall(SimpleBlock block, BlockFace dir) {
    	super(block.getPopData(), block.getX(), block.getY(), block.getZ());
        this.direction = dir;
    }

    public Wall(SimpleBlock block) {
    	super(block.getPopData(), block.getX(), block.getY(), block.getZ());
        this.direction = BlockFace.NORTH;
    }
    
    public Wall(PopulatorDataAbstract data, int x, int y, int z, BlockFace dir) {
    	super(data,x,y,z);
        this.direction = dir;
    }

    public Wall clone() {
        return new Wall(this.popData, this.getX(), this.getY(), this.getZ(), direction);
    }

    public Wall getAtY(int y) {
        return new Wall(this.popData, this.getX(), y, this.getZ(), this.direction);
    }

    public Wall getLeft() {
        return new Wall(this.getRelative(BlockUtils.getAdjacentFaces(direction)[0]), direction);
    }
    
    public Wall getUp() {
    	return new Wall(super.getUp(), direction);
    }

    public Wall getUp(int i) {
    	return new Wall(super.getUp(i), direction);
    }

    public Wall getGround() {
        return new Wall(super.getGround(), direction);
    }
    
    public Wall getGroundOrDry() {
        return new Wall(super.getGroundOrDry(), direction);
    }

    public Wall getGroundOrSeaLevel() {
        return new Wall(super.getGroundOrSeaLevel(), direction);
    }

    /**
     * Gets the first solid block above this one
     * @param cutoff
     * @return
     */
    public Wall findCeiling(int cutoff) {
    	SimpleBlock sb = super.findCeiling(cutoff);
    	if(sb == null) return null;
    	return new Wall(sb, direction);
    }

    /**
     * Gets the first solid block below this one
     * @param cutoff
     * @return
     */
    public Wall findFloor(int cutoff) {
    	SimpleBlock sb = super.findFloor(cutoff);
    	if(sb == null) return null;
        return new Wall(sb, direction);
    }
    

    /**
     * Gets the first stone-like block below this one
     * @param cutoff
     * @return
     */
    public Wall findStonelikeFloor(int cutoff) {
    	SimpleBlock sb = super.findStonelikeFloor(cutoff);
    	if(sb == null) return null;
        return new Wall(sb, direction);
    }
    
    /**
     * Gets the first stone-like block above this one
     * @param cutoff
     * @return
     */
    public Wall findStonelikeCeiling(int cutoff) {
    	SimpleBlock sb = super.findStonelikeCeiling(cutoff);
    	if(sb == null) return null;
        return new Wall(super.findStonelikeCeiling(cutoff), direction);
    }

    /**
     * Gets the first solid block right from this one
     * @param cutoff
     * @return
     */
    public Wall findRight(int cutoff) {
        Wall ceil = this.getRight();
        while (cutoff > 0) {
            if (ceil.getType().isSolid()) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getRight();
        }
        return null;
    }
    

    /**
     * Gets the first solid block towards that blockface
     * @param cutoff
     * @return
     */
    public Wall findDir(BlockFace face, int cutoff) {
        Wall ceil = this.getRelative(face);
        while (cutoff > 0) {
            if (ceil.getType().isSolid()) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getRelative(face);
        }
        return null;
    }

    /**
     * Gets the first solid block above this one
     * @param cutoff
     * @return
     */
    public Wall findLeft(int cutoff) {
        Wall ceil = this.getLeft();
        while (cutoff > 0) {
            if (ceil.getType().isSolid()) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getLeft();
        }
        return null;
    }

    public Wall getLeft(int it) {
        if (it < 0) return getRight(-it);
        Wall w = this;
        for (int i = 0; i < it; i++) w = w.getLeft();
        return w;
    }

    public Wall getRight() {
        return new Wall(this.getRelative(BlockUtils.getAdjacentFaces(direction)[1]), direction);
    }

    public Wall getRight(int it) {
        if (it < 0) return getLeft(-it);
        Wall w = this;
        for (int i = 0; i < it; i++) w = w.getRight();
        return w;
    }

    /**
     * @Deprecated Not needed anymore. Wall extends SimpleBlock now.
     * @return
     */
    public SimpleBlock get() {
        return this;
    }

    public Wall getRear() {
        return new Wall(super.getRelative(direction.getOppositeFace()), direction);
    }

    public Wall getRear(int it) {
        if (it < 0) return getFront(-it);
        Wall w = this.clone();
        for (int i = 0; i < it; i++) w = w.getRear();
        return w;
    }

    public Wall getFront() {
        return new Wall(super.getRelative(direction), direction);
    }

    public Wall getFront(int it) {
        if (it < 0) return getRear(-it);
        Wall w = this.clone();
        for (int i = 0; i < it; i++) w = w.getFront();
        return w;
    }

    public BlockFace getDirection() {
        return direction;
    }
    
    public Wall getDown(int i) {
        return new Wall(super.getDown(i), direction);
    }
    
    public Wall getDown() {
        return new Wall(super.getDown(), direction);
    }

    public Wall getRelative(int x, int y, int z) {
        return new Wall(super.getRelative(x, y, z), direction);
    }

    public Wall getRelative(BlockFace face) {
        return new Wall(super.getRelative(face), direction);
    }

    public Wall getRelative(BlockFace face, int depth) {
        // TODO Auto-generated method stub
        return new Wall(super.getRelative(face, depth), direction);
    }

    @Override
    public int hashCode() {
    	return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
    	return super.equals(obj);
    }
    
    public void lootTableChest(TerraLootTable table) {
    	get().getPopData().lootTableChest(getX(), getY(), getZ(), table);
    }

}