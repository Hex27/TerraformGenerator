package org.terraform.utils;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class BoxBuilder {

    private final Random random;
    private final int seed;
    private final SimpleBlock core;
    private final @NotNull Collection<Material> replaceWhitelist = new ArrayList<>();
    private float rX = 1f;
    private float rY = 1f;
    private float rZ = 1f;
    private boolean hardReplace = false;
    private Material[] types;
    private Material[] upperType;
    private Material[] lowerType;
    private int staticWaterLevel = -9999;
    private BoxType boxType = BoxType.FULL_BOX;


    public BoxBuilder(@NotNull Random random, SimpleBlock core, Material... types) {
        this.random = random;
        this.seed = random.nextInt(99999999);
        this.types = types;
        this.core = core;
    }

    public @NotNull BoxBuilder setBoxType(BoxType sphereType) {
        this.boxType = sphereType;
        return this;
    }

    public @NotNull BoxBuilder setUpperType(Material... upperType) {
        this.upperType = upperType;
        return this;
    }

    public @NotNull BoxBuilder setLowerType(Material... lowerType) {
        this.lowerType = lowerType;
        return this;
    }

    public @NotNull BoxBuilder setStaticWaterLevel(int staticWaterLevel) {
        this.staticWaterLevel = staticWaterLevel;
        return this;
    }

    public @NotNull BoxBuilder addToWhitelist(Material @NotNull ... mats) {
        replaceWhitelist.addAll(Arrays.asList(mats));
        return this;
    }

    public @NotNull BoxBuilder setRadius(float radius) {
        this.rX = radius;
        this.rY = radius;
        this.rZ = radius;
        return this;
    }

    public @NotNull BoxBuilder setRX(float rX) {
        this.rX = rX;
        return this;
    }

    public @NotNull BoxBuilder setRZ(float rZ) {
        this.rZ = rZ;
        return this;
    }

    public @NotNull BoxBuilder setRY(float rY) {
        this.rY = rY;
        return this;
    }

    public @NotNull BoxBuilder setSnowy() {
        this.upperType = new Material[] {Material.SNOW};
        return this;
    }

    public @NotNull BoxBuilder setHardReplace(boolean hardReplace) {
        this.hardReplace = hardReplace;
        return this;
    }

    public void build() {
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            unitReplace(core);
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.12f);

        float effectiveRYLower = -rY;
        if (boxType == BoxType.UPPER_SEMIBOX) {
            effectiveRYLower = 0;
        }
        float effectiveRYUpper = rY;
        if (boxType == BoxType.LOWER_SEMIBOX) {
            effectiveRYUpper = 0;
        }
        float fuzzMultiplier = 0.2f;
        for (float y = effectiveRYLower * (1f + fuzzMultiplier); y <= effectiveRYUpper * (1f + fuzzMultiplier); y++) {
            float yMultiplier = 1f - (Math.abs(y) / rY);
            for (float x = -rX * (1f + fuzzMultiplier) * yMultiplier;
                 x <= rX * (1f + fuzzMultiplier) * yMultiplier;
                 x++) {
                for (float z = -rZ * (1f + fuzzMultiplier) * yMultiplier;
                     z <= rZ * (1f + fuzzMultiplier) * yMultiplier;
                     z++) {
                    SimpleBlock rel = core.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double noiseVal = Math.abs(noise.GetNoise(rel.getX(), rel.getY(), rel.getZ()));

                    if (Math.abs(x) <= rX * (1 + (noiseVal * fuzzMultiplier))
                        && Math.abs(y) <= rY * (1 + (noiseVal
                                                     * fuzzMultiplier))
                        && Math.abs(z) <= rZ * (1 + (noiseVal * fuzzMultiplier)))
                    {
                        Material[] original = types;
                        if (rel.getY() <= staticWaterLevel) {
                            types = new Material[] {Material.WATER};
                            for (BlockFace face : new BlockFace[] {
                                    BlockFace.NORTH,
                                    BlockFace.SOUTH,
                                    BlockFace.EAST,
                                    BlockFace.WEST,
                                    BlockFace.DOWN
                            }) {
                                if (BlockUtils.isAir(rel.getRelative(face).getType())) {
                                    types = new Material[] {Material.STONE};
                                }
                            }
                        }
                        unitReplace(rel);
                        types = original;
                    }
                }
            }
        }
    }

    private void unitReplace(@NotNull SimpleBlock rel) {
        if (replaceWhitelist.isEmpty()) {
            if (hardReplace || !rel.isSolid()) {
                rel.setType(GenUtils.randChoice(random, types));
            }
            else {
                return;
            }
        }
        else if (replaceWhitelist.contains(rel.getType())) {
            rel.setType(GenUtils.randChoice(random, types));
        }
        else {
            return;
        }

        if (rel.getDown().isSolid()) {
            if (upperType != null) {
                rel.getUp().lsetType(upperType);
            }
            if (lowerType != null) {
                rel.getDown().setType(lowerType);
            }
        }
    }

    public enum BoxType {
        UPPER_SEMIBOX, LOWER_SEMIBOX, FULL_BOX
    }

}
