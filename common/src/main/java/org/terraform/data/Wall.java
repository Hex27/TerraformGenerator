package org.terraform.data;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Objects;

public class Wall extends SimpleBlock {
    private final BlockFace direction;

    public Wall(@NotNull SimpleBlock block, BlockFace dir) {
        super(block.getPopData(), block.getX(), block.getY(), block.getZ());
        this.direction = dir;
    }
    public Wall(@NotNull PopulatorDataAbstract data, @NotNull SimpleLocation loc, BlockFace dir) {
        super(data, loc.getX(), loc.getY(), loc.getZ());
        this.direction = dir;
    }

    public Wall(@NotNull SimpleBlock block) {
        super(block.getPopData(), block.getX(), block.getY(), block.getZ());
        this.direction = BlockFace.NORTH;
    }

    public Wall(@NotNull PopulatorDataAbstract data, int x, int y, int z, BlockFace dir) {
        super(data, x, y, z);
        this.direction = dir;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull Wall clone() {
        return new Wall(this.popData, this.getX(), this.getY(), this.getZ(), direction);
    }

    public @NotNull Wall getAtY(int y) {
        return new Wall(this.popData, this.getX(), y, this.getZ(), this.direction);
    }

    public @NotNull Wall getLeft() {
        return new Wall(this.getRelative(BlockUtils.getAdjacentFaces(direction)[0]), direction);
    }

    public @NotNull Wall getUp() {
        return new Wall(super.getUp(), direction);
    }

    public @NotNull Wall getUp(int i) {
        return new Wall(super.getUp(i), direction);
    }

    public @NotNull Wall getGround() {
        return new Wall(super.getGround(), direction);
    }

    public @NotNull Wall getGroundOrDry() {
        return new Wall(super.getGroundOrDry(), direction);
    }

    public @NotNull Wall getGroundOrSeaLevel() {
        return new Wall(super.getGroundOrSeaLevel(), direction);
    }

    /**
     * Gets the first solid block above this one
     */
    public Wall findCeiling(int cutoff) {
        SimpleBlock sb = super.findCeiling(cutoff);
        if (sb == null) {
            return null;
        }
        return new Wall(sb, direction);
    }

    /**
     * Gets the first solid block below this one
     */
    public Wall findFloor(int cutoff) {
        SimpleBlock sb = super.findFloor(cutoff);
        if (sb == null) {
            return null;
        }
        return new Wall(sb, direction);
    }

    /**
     * Gets the first solid block below this one
     */
    public Wall findNearestAirPocket(int cutoff) {
        SimpleBlock sb = super.findNearestAirPocket(cutoff);
        if (sb == null) {
            return null;
        }
        return new Wall(sb, direction);
    }

    /**
     * Gets the first stone-like block below this one
     */
    public Wall findStonelikeFloor(int cutoff) {
        SimpleBlock sb = super.findStonelikeFloor(cutoff);
        if (sb == null) {
            return null;
        }
        return new Wall(sb, direction);
    }

    /**
     * Gets the first stone-like block above this one
     */
    public Wall findStonelikeCeiling(int cutoff) {
        SimpleBlock sb = super.findStonelikeCeiling(cutoff);
        if (sb == null) {
            return null;
        }
        return new Wall(Objects.requireNonNull(super.findStonelikeCeiling(cutoff)), direction);
    }

    /**
     * Gets the first solid block right from this one
     */
    public @Nullable Wall findRight(int cutoff) {
        Wall ceil = this.getRight();
        while (cutoff > 0) {
            if (ceil.isSolid()) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getRight();
        }
        return null;
    }


    /**
     * Gets the first solid block towards that blockface
     */
    public @Nullable Wall findDir(@NotNull BlockFace face, int cutoff) {
        Wall ceil = this.getRelative(face);
        while (cutoff > 0) {
            if (ceil.isSolid()) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getRelative(face);
        }
        return null;
    }

    /**
     * Gets the first solid block above this one
     */
    public @Nullable Wall findLeft(int cutoff) {
        Wall ceil = this.getLeft();
        while (cutoff > 0) {
            if (ceil.isSolid()) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getLeft();
        }
        return null;
    }

    public Wall getLeft(int it) {
        if (it < 0) {
            return getRight(-it);
        }
        Wall w = this;
        for (int i = 0; i < it; i++) {
            w = w.getLeft();
        }
        return w;
    }

    public @NotNull Wall getRight() {
        return new Wall(this.getRelative(BlockUtils.getAdjacentFaces(direction)[1]), direction);
    }

    public Wall getRight(int it) {
        if (it < 0) {
            return getLeft(-it);
        }
        Wall w = this;
        for (int i = 0; i < it; i++) {
            w = w.getRight();
        }
        return w;
    }

    /**
     * @deprecated Not needed anymore. Wall extends SimpleBlock now.
     */
    public @NotNull SimpleBlock get() {
        return this;
    }

    public @NotNull Wall getRear() {
        return new Wall(super.getRelative(direction.getOppositeFace()), direction);
    }

    public @NotNull Wall flip(){
        return new Wall(this, direction.getOppositeFace());
    }
    public Wall getRear(int it) {
        if (it < 0) {
            return getFront(-it);
        }
        Wall w = this.clone();
        for (int i = 0; i < it; i++) {
            w = w.getRear();
        }
        return w;
    }

    public @NotNull Wall getFront() {
        return new Wall(super.getRelative(direction), direction);
    }

    public Wall getFront(int it) {
        if (it < 0) {
            return getRear(-it);
        }
        Wall w = this.clone();
        for (int i = 0; i < it; i++) {
            w = w.getFront();
        }
        return w;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public @NotNull Wall getDown(int i) {
        return new Wall(super.getDown(i), direction);
    }

    public @NotNull Wall getDown() {
        return new Wall(super.getDown(), direction);
    }

    public @NotNull Wall getRelative(int x, int y, int z) {
        return new Wall(super.getRelative(x, y, z), direction);
    }

    public @NotNull Wall getRelative(@NotNull BlockFace face) {
        return new Wall(super.getRelative(face), direction);
    }

    public @NotNull Wall getRelative(@NotNull BlockFace face, int depth) {
        // TODO Auto-generated method stub
        return new Wall(super.getRelative(face, depth), direction);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public void lootTableChest(TerraLootTable table) {
        get().getPopData().lootTableChest(getX(), getY(), getZ(), table);
    }

}