package org.terraform.data;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Arrays;
import java.util.Random;

public class Wall {
    private final SimpleBlock block;
    private final BlockFace direction;

    public Wall(SimpleBlock block, BlockFace dir) {
        this.block = block;
        this.direction = dir;
    }

    public Wall(SimpleBlock block) {
        this.block = block;
        this.direction = BlockFace.NORTH;
    }

    public Wall clone() {
        return new Wall(block, direction);
    }
    
    public Wall getAtY(int y) {
    	return new Wall(new SimpleBlock(block.getPopData(),block.getX(),y,block.getY()),this.direction);
    }

    public Wall getLeft() {
        return new Wall(block.getRelative(BlockUtils.getAdjacentFaces(direction)[0]), direction);
    }

    public Wall getGround() {
    	return new Wall(new SimpleBlock(
    			block.getPopData(),
    			block.getX(),
    			GenUtils.getHighestGround(block.getPopData(), block.getX(), block.getZ()),
    			block.getZ()),direction);
    }
    
    /**
     * Gets the first solid block above this one
     *
     * @param cutoff
     * @return
     */
    public Wall findCeiling(int cutoff) {
        Wall ceil = this.getRelative(0, 1, 0);
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
     *
     * @param cutoff
     * @return
     */
    public Wall findFloor(int cutoff) {
        Wall floor = this.clone().getRelative(0, -1, 0);
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
     * Gets the first solid block right from this one
     *
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
     * Gets the first solid block above this one
     *
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
        Wall w = this.clone();
        for (int i = 0; i < it; i++) w = w.getLeft();
        return w;
    }

    public Wall getRight() {
        return new Wall(block.getRelative(BlockUtils.getAdjacentFaces(direction)[1]), direction);
    }

    public Wall getRight(int it) {
        if (it < 0) return getLeft(-it);
        Wall w = this.clone();
        for (int i = 0; i < it; i++) w = w.getRight();
        return w;
    }

    public Wall getHighestSolidBlockFromAbove() {
        int highest = GenUtils.getHighestGround(block.getPopData(), block.getX(), block.getZ());
        int x = block.getX();
        int z = block.getZ();

        return new Wall(new SimpleBlock(block.getPopData(), x, highest, z), direction);
    }

    public SimpleBlock get() {
        return block;
    }

    public void setBlockData(BlockData d) {
        this.block.setBlockData(d);
    }

    public Material getType() {
        return block.getType();
    }

    public void setType(Material type) {
        block.setType(type);
    }

    public void setType(Material... type) {
        block.setType(GenUtils.randMaterial(type));
    }

    /**
     * Replaces everything in its way
     *
     * @param height
     * @param rand
     * @param types
     */
    public void Pillar(int height, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            block.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }

    public void Pillar(int height, boolean pattern, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (Arrays.equals(new Material[]{Material.BARRIER}, types)) continue;
            if (!pattern)
                block.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
            else if (types[i % types.length] != Material.BARRIER)
                block.getRelative(0, i, 0).setType(types[i % types.length]);
        }
    }

    /**
     * Corrects all multiple facing block data in a pillar
     *
     * @param height
     */
    public void CorrectMultipleFacing(int height) {
        for (int i = 0; i < height; i++) {
            BlockUtils.correctSurroundingMultifacingData(block.getRelative(0, i, 0));
        }
    }


    /**
     * Replaces until a solid block is reached.
     *
     * @param height
     * @param rand
     * @param types
     */
    public void LPillar(int height, Random rand, Material... types) {
        LPillar(height, false, rand, types);
    }

    /**
     * Replaces until a solid block is reached.
     *
     * @param height
     * @param rand
     * @param types
     */
    public int LPillar(int height, boolean pattern, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (block.getRelative(0, i, 0).getType().isSolid()) return i;
            if (Arrays.equals(new Material[]{Material.BARRIER}, types)) continue;
            if (!pattern)
                block.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
            else
                block.getRelative(0, i, 0).setType(types[i % types.length]);
        }
        return height;
    }

    /**
     * Replaces non-solid blocks only
     *
     * @param height
     * @param rand
     * @param types
     */
    public void RPillar(int height, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (!block.getRelative(0, i, 0).getType().isSolid())
                block.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }

    /**
     * Replaces non-cave air only
     *
     * @param height
     * @param rand
     * @param types
     */
    public void CAPillar(int height, Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (block.getRelative(0, i, 0).getType() != Material.CAVE_AIR)
                block.getRelative(0, i, 0).setType(GenUtils.randMaterial(rand, types));
        }
    }

    public int downUntilSolid(Random rand, Material... types) {
        int depth = 0;
        for (int y = get().getY(); y > 0; y--) {
            if (!block.getRelative(0, -depth, 0).getType().isSolid()) {
                block.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            } else break;
            depth++;
        }

        return depth;
    }

    public void downLPillar(Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = get().getY(); y > 0; y--) {
            if (depth >= h) break;
            if (!block.getRelative(0, -depth, 0).getType().isSolid()) {
                block.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            } else break;
            depth++;
        }
    }

    public void downRPillar(Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = get().getY(); y > 0; y--) {
            if (depth >= h) break;
            if (!block.getRelative(0, -depth, 0).getType().isSolid()) {
                block.getRelative(0, -depth, 0).setType(GenUtils.randMaterial(rand, types));
            }
            depth++;
        }
    }

    public Wall getRear() {
        return new Wall(block.getRelative(direction.getOppositeFace()), direction);
    }

    public Wall getRear(int it) {
    	if(it < 0) return getFront(-it);
        Wall w = this.clone();
        for (int i = 0; i < it; i++) w = w.getRear();
        return w;
    }

    public Wall getFront() {
        return new Wall(block.getRelative(direction), direction);
    }

    public Wall getFront(int it) {
    	if(it < 0) return getRear(-it);
        Wall w = this.clone();
        for (int i = 0; i < it; i++) w = w.getFront();
        return w;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public Wall getRelative(int x, int y, int z) {
        return new Wall(block.getRelative(x, y, z), direction);
    }

    public Wall getRelative(BlockFace face) {
        // TODO Auto-generated method stub
        return new Wall(block.getRelative(face), direction);
    }


    public Wall getRelative(BlockFace face, int depth) {
        // TODO Auto-generated method stub
        return new Wall(block.getRelative(face, depth), direction);
    }

    public int getX() {
        return get().getX();
    }

    public int getY() {
        return get().getY();
    }

    public int getZ() {
        return get().getZ();
    }

}