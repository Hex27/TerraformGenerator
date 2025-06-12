package org.terraform.data;

import com.google.gson.annotations.SerializedName;
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
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.IPopulatorDataPhysicsCapable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

public class SimpleBlock {
    @NotNull
    final PopulatorDataAbstract popData;
    @SerializedName("w") // wtf is this for
    private final int x, y, z;

    public SimpleBlock(@NotNull Location loc) {
        this.popData = new PopulatorDataPostGen(loc.getChunk());
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public SimpleBlock(@NotNull PopulatorDataAbstract data, @NotNull Vector loc) {
        this.popData = data;
        this.x = (int) Math.round(loc.getX());
        this.y = (int) Math.round(loc.getY());
        this.z = (int) Math.round(loc.getZ());
    }

    public SimpleBlock(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        // this.world = world;
        this.popData = data;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SimpleBlock(@NotNull PopulatorDataAbstract data, @NotNull SimpleLocation sLoc) {
        // this.world = world;
        this.popData = data;
        this.x = sLoc.getX();
        this.y = sLoc.getY();
        this.z = sLoc.getZ();

    }


    public SimpleBlock(@NotNull PopulatorDataAbstract data, @NotNull Location loc) {
        // this.world = loc.getWorld().getName();
        this.popData = data;
        this.x = (int) loc.getX();
        this.y = (int) loc.getY();
        this.z = (int) loc.getZ();

    }

    public SimpleBlock(@NotNull PopulatorDataAbstract data, @NotNull Block b) {
        // this.world = b.getWorld().getName();
        this.popData = data;
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
        // this.data = b.getBlockData().getAsString();
    }

    public void pathTowards(int width, int maxLength, @NotNull SimpleBlock target, Material... types) {
        BlockFace dir = BlockFace.NORTH;
        int max = -1;
        if (target.getX() - this.getX() > max) {
            // east
            dir = BlockFace.EAST;
        }
        else if (this.getX() - target.getX() > max) {
            // west
            dir = BlockFace.WEST;
        }
        else if (this.getZ() - target.getZ() > max) {
            // north
            dir = BlockFace.NORTH;
        }
        else if (target.getZ() - this.getZ() > max) {
            // south
            dir = BlockFace.SOUTH;
        }

        SimpleBlock base = this;
        for (int i = 0; i < maxLength; i++) {
            if (!base.lsetType(types)) {
                break;
            }

            for (int w = 0; w < width; w++) {
                for (BlockFace adj : BlockUtils.getAdjacentFaces(dir)) {
                    base.getRelative(adj).setType(types);
                }
            }
            base = base.getRelative(dir);
        }

    }

    public @NotNull SimpleLocation getLoc() {
        return new SimpleLocation(x, y, z);
    }

    public @NotNull SimpleBlock getAtY(int y) {
        return new SimpleBlock(popData, x, y, z);
    }

    public double distanceSquared(@NotNull SimpleBlock other) {
        float selfX = (float) x;
        float selfY = (float) y;
        float selfZ = (float) z;
        float oX = (float) other.x;
        float oY = (float) other.y;
        float oZ = (float) other.z;

        return Math.pow(selfX - oX, 2) + Math.pow(selfY - oY, 2) + Math.pow(selfZ - oZ, 2);
    }

    public boolean isConnected(SimpleBlock other) {
        for (BlockFace face : BlockUtils.sixBlockFaces) {
            if (this.getRelative(face).equals(other)) {
                return true;
            }
        }
        return false;
    }

    public double distance(@NotNull SimpleBlock other) {

        return Math.sqrt(distanceSquared(other));
    }

    public boolean sameLocation(@NotNull SimpleBlock other) {
        return other.x == x && other.y == y && other.z == z;
    }

    public @NotNull Vector toVector() {
        return new Vector(x, y, z);
    }

    public @NotNull SimpleBlock untilSolid(@NotNull BlockFace face) {
        SimpleBlock rel = this.getRelative(face);
        while (!rel.isSolid()) {
            rel = rel.getRelative(face);
        }

        return rel;
    }


    /**
     * Lenient set. Only replaces non-solid blocks.
     *
     * @return if the set was a success.
     */
    public boolean lsetType(@NotNull Material type) {
        if (!isSolid()) {
            setType(type);
            return true;
        }
        return false;
    }

    public void lsetBlockData(BlockData data) {
        if (!isSolid()) {
            setBlockData(data);
        }
    }

    public @Nullable BlockData getBlockData() {
        return popData.getBlockData(x, y, z);// Bukkit.createBlockData(getType());
    }

    public void setBlockData(BlockData dat) {
        if (popData.getType(x, y, z) == Material.WATER) {
            if (dat instanceof Waterlogged wl) {
                wl.setWaterlogged(true);
            }
        }
        popData.setBlockData(x, y, z, dat);
    }

    public void RSolSetBlockData(BlockData data) {
        if (isSolid()) {
            setBlockData(data);
        }
    }

    public @NotNull SimpleBlock getRelative(int nx, int ny, int nz) {
        return new SimpleBlock(popData, x + nx, y + ny, z + nz);
    }

    public @NotNull SimpleBlock getRelative(@NotNull Vector v) {
        return new SimpleBlock(popData,
                (int) Math.round(x + v.getX()),
                (int) Math.round(y + v.getY()),
                (int) Math.round(z + v.getZ())
        );
    }

    public @NotNull String getCoords() {
        return x + "," + y + ',' + z;
    }

    public @NotNull SimpleBlock getRelative(@NotNull BlockFace face) {
        return new SimpleBlock(popData, x + face.getModX(), y + face.getModY(), z + face.getModZ());
    }

    public @NotNull SimpleBlock getRelative(@NotNull BlockFace face, int count) {
        return new SimpleBlock(
                popData,
                x + face.getModX() * count,
                y + face.getModY() * count,
                z + face.getModZ() * count
        );
    }

    public void addEntity(EntityType type) {
        popData.addEntity(x, y, z, type);
    }


    public int countAdjacentsThatMatchType(BlockFace @NotNull [] faces, Material @NotNull ... types) {
        int i = 0;
        for (BlockFace face : faces) {
            for (Material type : types) {
                if (getRelative(face).getType() == type) {
                    i++;
                }
            }
        }
        return i;
    }

    public boolean doAdjacentsMatchType(BlockFace @NotNull [] faces, Material @NotNull ... types) {
        for (BlockFace face : faces) {
            for (Material type : types) {
                if (getRelative(face).getType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    public void replaceAdjacentNonLiquids(BlockFace @NotNull [] faces, Material liquid, Material... types) {
        for (BlockFace face : faces) {
            if (!getRelative(face).isSolid() && getRelative(face).getType() != liquid) {
                getRelative(face).setType(types);
            }
        }
    }

    public boolean hasAdjacentSolid(BlockFace @NotNull [] faces) {
        for (BlockFace face : faces) {
            if (getRelative(face).isSolid()) {
                return true;
            }
        }
        return false;
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }

    public @NotNull SimpleChunkLocation getSChunk(String world) {
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

    public @NotNull Material getType() {
        return popData.getType(x, y, z);
    }

    public void setType(@NotNull Material type) {
        if (popData.getType(x, y, z) == Material.WATER) {
            BlockData data = Bukkit.createBlockData(type);
            if (data instanceof Waterlogged wl) {
                wl.setWaterlogged(true);
            }
            popData.setBlockData(x, y, z, data);
        }
        else {
            popData.setType(x, y, z, type);
        }

        // Setting leaves with setType will be persistent
        if (Tag.LEAVES.isTagged(type)) {
            // if (type.toString().contains("LEAVES")) {
            BlockData l = Bukkit.createBlockData(type);
            if (l instanceof Leaves leaves) {
                leaves.setPersistent(true);
            }

            setBlockData(l);
        }
    }

    public void setType(Material... types) {
        setType(GenUtils.randChoice(types));
    }

    public boolean isAir() {
        return popData.getType(x, y, z) == Material.AIR || popData.getType(x, y, z) == Material.CAVE_AIR;
    }

    public boolean isSolid() {
        return popData.getType(x, y, z).isSolid();
    }

    public void physicsSetType(@NotNull Material type, boolean updatePhysics)
    {
        if (this.popData instanceof IPopulatorDataPhysicsCapable) {
            if (popData.getType(x, y, z) == Material.WATER) {
                BlockData data = Bukkit.createBlockData(type);
                if (data instanceof Waterlogged wl) {
                    wl.setWaterlogged(true);
                }
                ((IPopulatorDataPhysicsCapable) popData).setBlockData(x, y, z, data, updatePhysics);
            }
            else {
                ((IPopulatorDataPhysicsCapable) popData).setType(x, y, z, type, updatePhysics);
            }

            // Setting leaves with setType will be persistent
            if (Tag.LEAVES.isTagged(type)) {
                // if (type.toString().contains("LEAVES")) {
                BlockData l = Bukkit.createBlockData(type);
                if (l instanceof Leaves leaves) {
                    leaves.setPersistent(true);
                }

                ((IPopulatorDataPhysicsCapable) popData).setBlockData(x, y, z, l, updatePhysics);
            }

        }
        else {
            setType(type);
        }
    }

    public void physicsSetBlockData(BlockData dat, boolean updatePhysics)
    {
        if (this.popData instanceof IPopulatorDataPhysicsCapable) {
            if (popData.getType(x, y, z) == Material.WATER) {
                if (dat instanceof Waterlogged wl) {
                    wl.setWaterlogged(true);
                }
            }
            ((IPopulatorDataPhysicsCapable) popData).setBlockData(x, y, z, dat, updatePhysics);
        }
        else {
            setBlockData(dat);
        }
    }

    public boolean lsetType(Material... types) {
        return lsetType(GenUtils.randChoice(types));
    }

    public void RSolSetType(@NotNull Material type) {
        if (isSolid()) {
            setType(type);
        }
    }

    /**
     * @return the popData
     */
    public @NotNull PopulatorDataAbstract getPopData() {
        return popData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + popData.getTerraformWorld().hashCode();
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleBlock other)) {
            return false;
        }
        return Objects.equals(popData.getTerraformWorld(), other.getPopData().getTerraformWorld())
               && x == other.x
               && z == other.z
               && y == other.y;
    }

    public @NotNull SimpleBlock getGround() {
        return new SimpleBlock(popData, x, GenUtils.getHighestGround(popData, x, z), z);
    }

    public @NotNull SimpleBlock getGroundOrSeaLevel() {
        int y = GenUtils.getHighestGround(popData, x, z);
        if (y < TerraformGenerator.seaLevel) {
            y = TerraformGenerator.seaLevel;
        }
        return new SimpleBlock(popData, x, y, z);
    }

    public @NotNull SimpleBlock getGroundOrDry() {
        int y = GenUtils.getHighestGround(popData, x, z);

        while (y < TerraformGeneratorPlugin.injector.getMaxY() && (BlockUtils.isWet(this.getAtY(y + 1))
                                                                   || Tag.ICE.isTagged(this.getAtY(y + 1).getType()))) {
            y++;
        }
        return new SimpleBlock(popData, x, y, z);
    }


    public @NotNull SimpleBlock getUp() {
        return this.getRelative(0, 1, 0);
    }

    public @NotNull SimpleBlock getUp(int i) {
        return this.getRelative(0, i, 0);
    }

    /**
     * @param cutoff number of iterations before stopping and returning null
     * @return first solid block above this one
     */
    public @Nullable SimpleBlock findCeiling(int cutoff) {
        SimpleBlock ceil = this.getUp();
        while (cutoff > 0) {
            if (ceil.isSolid() && ceil.getType() != Material.LANTERN) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getUp();
        }
        return null;
    }

    /**
     * @param cutoff number of iterations before stopping and returning null
     * @return first solid block below this one
     */
    public @Nullable SimpleBlock findFloor(int cutoff) {
        SimpleBlock floor = this.getDown();
        while (cutoff > 0 && floor.getY() >= TerraformGeneratorPlugin.injector.getMinY()) {
            if (floor.isSolid() && floor.getType() != Material.LANTERN) {
                return floor;
            }
            cutoff--;
            floor = floor.getDown();
        }
        return null;
    }

    /**
     * If solid, find nearest air pocket upwards.
     * If not solid, find nearest floor.
     *
     * @param cutoff number of iterations before stopping and returning null
     */
    public @Nullable SimpleBlock findAirPocket(int cutoff) {
        SimpleBlock floor = this.getDown();
        while (cutoff > 0 && floor.getY() >= TerraformGeneratorPlugin.injector.getMinY()) {
            if (!floor.isSolid()) {
                return floor;
            }
            cutoff--;
            floor = floor.getDown();
        }
        return null;
    }

    /**
     * If solid, find nearest air pocket upwards.
     * If not solid, find nearest floor.
     *
     * @param cutoff number of iterations before stopping and returning null
     */
    public @Nullable SimpleBlock findNearestAirPocket(int cutoff) {
        if (this.isSolid()) {
            SimpleBlock rel = this.getUp();
            while (cutoff > 0) {
                if (!rel.isSolid()) {
                    return rel;
                }
                cutoff--;
                rel = rel.getUp();
            }
            return null;
        }
        else {
            SimpleBlock candidate = this.findFloor(cutoff);
            if (candidate != null) {
                candidate = candidate.getUp();
            }
            return candidate;
        }
    }

    /**
     * @param cutoff number of iterations before stopping and returning null
     * @return first stone-like block below this one
     */
    public @Nullable SimpleBlock findStonelikeFloor(int cutoff) {
        SimpleBlock floor = this.getDown();
        while (cutoff > 0 && floor.getY() >= TerraformGeneratorPlugin.injector.getMinY()) {
            // floor.getUp().setType(Material.CYAN_STAINED_GLASS);
            if (BlockUtils.isStoneLike(floor.getType())) {
                return floor;
            }
            cutoff--;
            floor = floor.getDown();
        }
        return null;
    }

    /**
     * @param cutoff the number of iterations before stopping and returning null
     * @return first stone-like block above this one
     */
    public @Nullable SimpleBlock findStonelikeCeiling(int cutoff) {
        SimpleBlock ceil = this.getUp();
        while (cutoff > 0) {
            if (BlockUtils.isStoneLike(ceil.getType())) {
                return ceil;
            }
            cutoff--;
            ceil = ceil.getUp();
        }
        return null;
    }


    /**
     * Replaces everything in its way
     */
    public void Pillar(int height, Material... types) {
        Random rand = new Random();
        for (int i = 0; i < height; i++) {
            this.getRelative(0, i, 0).setType(GenUtils.randChoice(rand, types));
        }
    }

    /**
     * Replaces the block's material with either air or water or lava depending
     * on the block's surrounding fluid (same y).
     */
    public void fluidize()
    {
        Material fluid = Material.AIR;
        if (!BlockUtils.isWet(this)) {
            for (BlockFace face : BlockUtils.directBlockFaces) {
                if (getRelative(face).getType() == Material.WATER) {
                    fluid = Material.WATER;
                }
                else if (getRelative(face).getType() == Material.LAVA) {
                    fluid = Material.LAVA;
                }
            }
        }
        else {
            fluid = Material.WATER;
        }
        setType(fluid);
    }

    /**
     * Replaces everything in its way
     */
    public void Pillar(int height, @NotNull Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            this.getRelative(0, i, 0).setType(GenUtils.randChoice(rand, types));
        }
    }

    /**
     * Replaces everything in its way
     */
    public void Pillar(int height, boolean pattern, @NotNull Random rand, Material @NotNull ... types) {
        for (int i = 0; i < height; i++) {
            if (Arrays.equals(new Material[] {Material.BARRIER}, types)) {
                continue;
            }
            if (!pattern) {
                this.getRelative(0, i, 0).setType(GenUtils.randChoice(rand, types));
            }
            else if (types[i % types.length] != Material.BARRIER) {
                this.getRelative(0, i, 0).setType(types[i % types.length]);
            }
        }
    }


    /**
     * Corrects all multiple facing block data in a pillar
     */
    public void CorrectMultipleFacing(int height) {
        for (int i = 0; i < height; i++) {
            BlockUtils.correctSurroundingMultifacingData(this.getRelative(0, i, 0));
        }
    }


    /**
     * Replaces until a solid block is reached.
     */
    public void LPillar(int height, Material... types) {
        LPillar(height, false, new Random(), types);
    }

    /**
     * Replaces until a solid block is reached.
     *
     * @return height of pillar created
     */
    public int LPillar(int height, @NotNull Random rand, Material... types) {
        return LPillar(height, false, rand, types);
    }

    /**
     * Replaces until a solid block is reached.
     *
     * @return height of pillar created
     */
    public int LPillar(int height, boolean pattern, @NotNull Random rand, Material @NotNull ... types) {
        for (int i = 0; i < height; i++) {
            if (this.getRelative(0, i, 0).isSolid()) {
                return i;
            }
            if (Arrays.equals(new Material[] {Material.BARRIER}, types)) {
                continue;
            }
            if (!pattern) {
                this.getRelative(0, i, 0).setType(GenUtils.randChoice(rand, types));
            }
            else {
                this.getRelative(0, i, 0).setType(types[i % types.length]);
            }
        }
        return height;
    }

    /**
     * Replaces non-solid blocks only
     */
    public void RPillar(int height, @NotNull Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (!this.getRelative(0, i, 0).isSolid()) {
                this.getRelative(0, i, 0).setType(GenUtils.randChoice(rand, types));
            }
        }
    }

    /**
     * Replaces solid blocks only
     *
     * @param height pillar height. This height will be reached.
     * @param types  materials the pillar consists of
     */
    public void ReplacePillar(int height, Material... types) {
        for (int i = 0; i < height; i++) {
            if (this.getRelative(0, i, 0).isSolid()) {
                this.getRelative(0, i, 0).setType(GenUtils.randChoice(types));
            }
        }
    }

    /**
     * Replaces non-cave air only
     *
     * @param height of pillar
     * @param rand   to use for choosing random material types
     * @param types  actual materials to use
     */
    public void CAPillar(int height, @NotNull Random rand, Material... types) {
        for (int i = 0; i < height; i++) {
            if (this.getRelative(0, i, 0).getType() != Material.CAVE_AIR) {
                this.getRelative(0, i, 0).setType(GenUtils.randChoice(rand, types));
            }
        }
    }

    public void waterlog(int height) {
        for (int i = 0; i < height; i++) {
            var rel = this.getRelative(0, i, 0);
            if (rel.getBlockData() instanceof Waterlogged log) {
                log.setWaterlogged(true);
                rel.setBlockData(log);
            }
        }
    }

    public int downUntilSolid(@NotNull Random rand, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (!this.getRelative(0, -depth, 0).isSolid()) {
                this.getRelative(0, -depth, 0).setType(GenUtils.randChoice(rand, types));
            }
            else {
                break;
            }
            depth++;
        }

        return depth;
    }

    public int blockfaceUntilSolid(int maxDepth, @NotNull Random rand, @NotNull BlockFace face, Material... types) {
        int depth = 0;
        while (depth <= maxDepth) {
            if (!this.getRelative(face).isSolid()) {
                this.getRelative(face).setType(GenUtils.randChoice(rand, types));
            }
            else {
                break;
            }
            depth++;
        }

        return depth;
    }

    public int blockface(int maxDepth, @NotNull Random rand, @NotNull BlockFace face, Material... types) {
        int depth = 0;
        while (depth <= maxDepth) {
            this.getRelative(face).setType(GenUtils.randChoice(rand, types));
            depth++;
        }

        return depth;
    }

    public void downPillar(int h, Material... types) {
        downPillar(new Random(), h, types);
    }

    public void downPillar(@NotNull Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) {
                break;
            }
            this.getRelative(0, -depth, 0).setType(GenUtils.randChoice(rand, types));
            depth++;
        }
    }

    public void downLPillar(@NotNull Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) {
                break;
            }
            if (!this.getRelative(0, -depth, 0).isSolid()) {
                this.getRelative(0, -depth, 0).setType(GenUtils.randChoice(rand, types));
            }
            else {
                break;
            }
            depth++;
        }
    }

    public void downRPillar(@NotNull Random rand, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) {
                break;
            }
            if (!this.getRelative(0, -depth, 0).isSolid()) {
                this.getRelative(0, -depth, 0).setType(GenUtils.randChoice(rand, types));
            }
            depth++;
        }
    }

    public void directionalLPillar(@NotNull Random rand, @NotNull BlockFace face, int h, Material... types) {
        int depth = 0;
        for (int y = this.y; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
            if (depth >= h) {
                break;
            }
            if (!this.getRelative(face, depth).isSolid()) {
                this.getRelative(face, depth).setType(GenUtils.randChoice(rand, types));
            }
            else {
                break;
            }
            depth++;
        }
    }

    public @NotNull SimpleBlock getDown(int i) {
        return this.getRelative(0, -i, 0);
    }

    public @NotNull SimpleBlock getDown() {
        return this.getRelative(0, -1, 0);
    }

    @Override
    public @NotNull String toString() {
        return x + "," + y + "," + z;
    }

    public void rsetType(@NotNull EnumSet<Material> toReplace, Material... type) {
        popData.rsetType(this.toVector(), toReplace, type);
    }

    public void rsetBlockData(@NotNull EnumSet<Material> toReplace, BlockData data) {
        popData.rsetBlockData(this.toVector(), toReplace, data);
    }
}