package org.terraform.structure.room;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class CarvedRoom extends CubeRoom {

    private float xMultiplier = 1f;
    private float yMultiplier = 1f;
    private float zMultiplier = 1f;
    private float frequency = 0.09f;

    public CarvedRoom(int widthX, int widthZ, int height, int x, int y, int z) {
        super(widthX, widthZ, height, x, y, z);
    }

    public CarvedRoom(@NotNull CubeRoom room) {
        super(room.getWidthX(), room.getWidthZ(), room.getHeight(), room.getX(), room.getY(), room.getZ());
    }

    /**
     * Fillmat is always CAVE_AIR no matter what's being put lol.
     */
    @Override
    public void fillRoom(@NotNull PopulatorDataAbstract data, int tile, Material[] mat, Material fillMat) {
        int heightOffset = height - (2 * height / 3);
        BlockUtils.carveCaveAir(
                new Random().nextInt(9999291),
                xMultiplier * (widthX / 2f),
                yMultiplier * (2 * height / 3f),
                zMultiplier * (widthZ / 2f),
                frequency,
                new SimpleBlock(data, x, y + heightOffset, z),
                true,
                true,
                BlockUtils.caveCarveReplace
        );
    }

    public double getxMultiplier() {
        return xMultiplier;
    }

    public void setxMultiplier(float xMultiplier) {
        this.xMultiplier = xMultiplier;
    }

    public float getyMultiplier() {
        return yMultiplier;
    }

    public void setyMultiplier(float yMultiplier) {
        this.yMultiplier = yMultiplier;
    }

    public float getzMultiplier() {
        return zMultiplier;
    }

    public void setzMultiplier(float zMultiplier) {
        this.zMultiplier = zMultiplier;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }
}
