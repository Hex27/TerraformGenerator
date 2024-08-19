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

public class SphereBuilder {

    private final Random random;
    private final int seed;
    private final SimpleBlock core;
    private final @NotNull Collection<Material> replaceWhitelist = new ArrayList<>();
    private boolean isSmooth = false;
    private float rX = 1f;
    private float rY = 1f;
    private float rZ = 1f;
    private float padding = 0f;
    private double minRadius = 0;
    private double maxRadius = 100;
    private boolean hardReplace = false;
    private Material[] types;
    private Material[] containmentMaterial = new Material[] {Material.STONE};
    private Material[] upperType;
    private Material[] lowerType;
    private int staticWaterLevel = -9999;
    private float sphereFrequency = 0.09f;
    private boolean doLiquidContainment = false;
    private SphereType sphereType = SphereType.FULL_SPHERE;


    public SphereBuilder(@NotNull Random random, SimpleBlock core, Material... types) {
        this.random = random;
        this.seed = random.nextInt(99999999);
        this.types = types;
        this.core = core;
    }

    public @NotNull SphereBuilder setSphereType(SphereType sphereType) {
        this.sphereType = sphereType;
        return this;
    }

    public @NotNull SphereBuilder setUpperType(Material... upperType) {
        this.upperType = upperType;
        return this;
    }

    public @NotNull SphereBuilder setLowerType(Material... lowerType) {
        this.lowerType = lowerType;
        return this;
    }

    public @NotNull SphereBuilder setStaticWaterLevel(int staticWaterLevel) {
        this.staticWaterLevel = staticWaterLevel;
        return this;
    }

    public @NotNull SphereBuilder addToWhitelist(Material @NotNull ... mats) {
        replaceWhitelist.addAll(Arrays.asList(mats));
        return this;
    }

    public @NotNull SphereBuilder setRadius(float radius) {
        this.rX = radius;
        this.rY = radius;
        this.rZ = radius;
        return this;
    }

    public @NotNull SphereBuilder setSphereFrequency(float sphereFrequency) {
        this.sphereFrequency = sphereFrequency;
        return this;
    }

    public @NotNull SphereBuilder setRX(float rX) {
        this.rX = rX;
        return this;
    }

    public @NotNull SphereBuilder setRZ(float rZ) {
        this.rZ = rZ;
        return this;
    }

    public @NotNull SphereBuilder setRY(float rY) {
        this.rY = rY;
        return this;
    }

    public @NotNull SphereBuilder setSnowy() {
        this.upperType = new Material[] {Material.SNOW};
        return this;
    }

    public @NotNull SphereBuilder setHardReplace(boolean hardReplace) {
        this.hardReplace = hardReplace;
        return this;
    }

    public @NotNull SphereBuilder setDoLiquidContainment(boolean doLiquidContainment) {
        this.doLiquidContainment = doLiquidContainment;
        return this;
    }

    public @NotNull SphereBuilder setCointainmentMaterials(Material... containmentMaterial) {
        this.containmentMaterial = containmentMaterial;
        return this;
    }

    /**
     * Refers to minimum percentage radius (0.0 to 1.0)
     */
    public @NotNull SphereBuilder setMinRadius(double minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    /**
     * Refers to minimum percentage radius (0.0 to 1.0)
     */
    public @NotNull SphereBuilder setMaxRadius(double maxRadius) {
        this.maxRadius = maxRadius;
        return this;
    }

    public @NotNull SphereBuilder setSmooth(boolean isSmooth) {
        this.isSmooth = isSmooth;
        return this;
    }

    public @NotNull SphereBuilder setPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public void build() {
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            unitReplace(core, core.getY());
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(sphereFrequency);

        float effectiveRYLower = -rY;
        if (sphereType == SphereType.UPPER_SEMISPHERE) {
            effectiveRYLower = 0;
        }
        float effectiveRYUpper = rY;
        if (sphereType == SphereType.LOWER_SEMISPHERE) {
            effectiveRYUpper = 0;
        }

        for (float x = -rX - padding; x <= rX + padding; x++) {
            for (float y = effectiveRYLower; y <= effectiveRYUpper; y++) {
                for (float z = -rZ - padding; z <= rZ + padding; z++) {
                    SimpleBlock rel = core.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(y, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    double noiseVal;

                    if (!isSmooth) {
                        noiseVal = 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
                    }
                    else {
                        noiseVal = 1;
                    }

                    if (noiseVal < minRadius) {
                        noiseVal = minRadius;
                    }
                    if (noiseVal > maxRadius) {
                        noiseVal = maxRadius;
                    }

                    if (equationResult <= noiseVal) {
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
                        unitReplace(rel, (int) (core.getY() + effectiveRYUpper));
                        types = original;
                    }
                }
            }
        }
    }

    private void unitReplace(@NotNull SimpleBlock rel, int effectiveRYUpper) {
        if (replaceWhitelist.isEmpty()) {
            if (hardReplace || !rel.isSolid()) {
                rel.setType(GenUtils.randChoice(random, types));
                if (this.doLiquidContainment) {
                    rel.replaceAdjacentNonLiquids(new BlockFace[] {
                            BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
                    }, types[0], containmentMaterial);
                }
            }
        }
        else if (replaceWhitelist.contains(rel.getType())) {
            rel.setType(GenUtils.randChoice(random, types));
            if (this.doLiquidContainment) {
                rel.replaceAdjacentNonLiquids(new BlockFace[] {
                        BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
                }, types[0], containmentMaterial);
            }
        }

        if (rel.isSolid()) {
            if (upperType != null && rel.getY() == effectiveRYUpper) {
                rel.getUp().lsetType(upperType);
            }
            if (lowerType != null) {
                rel.getDown().setType(lowerType);
            }
        }


    }

    public enum SphereType {
        UPPER_SEMISPHERE, LOWER_SEMISPHERE, FULL_SPHERE
    }

}
