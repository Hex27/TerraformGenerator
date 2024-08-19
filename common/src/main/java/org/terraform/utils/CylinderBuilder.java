package org.terraform.utils;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Random;

public class CylinderBuilder {

    private final Random random;
    private final int seed;
    private final SimpleBlock core;
    private final Material[] types;
    private float rX = 1f;
    private float rY = 1f;
    private float rZ = 1f;
    private float minRadius = 0f;
    private boolean singleBlockY = false;
    private boolean startFromZero = false;
    private boolean hardReplace = false;
    private Material[] upperType;
    private Material[] lowerType;
    private float noiseMagnitude = 0.7f;

    public CylinderBuilder(@NotNull Random random, SimpleBlock core, Material... types) {
        this.random = random;
        this.seed = random.nextInt(99999999);
        this.types = types;
        this.core = core;
    }

    public @NotNull CylinderBuilder setStartFromZero(boolean startFromZero) {
        this.startFromZero = startFromZero;
        return this;
    }

    public @NotNull CylinderBuilder setNoiseMagnitude(float mag) {
        this.noiseMagnitude = mag;
        return this;
    }

    public @NotNull CylinderBuilder setUpperType(Material... upperType) {
        this.upperType = upperType;
        return this;
    }

    public @NotNull CylinderBuilder setLowerType(Material... lowerType) {
        this.lowerType = lowerType;
        return this;
    }

    public @NotNull CylinderBuilder setRadius(float radius) {
        this.rX = radius;
        this.rY = radius;
        this.rZ = radius;
        return this;
    }

    public @NotNull CylinderBuilder setMinRadius(float minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    public @NotNull CylinderBuilder setRX(float rX) {
        this.rX = rX;
        return this;
    }

    public @NotNull CylinderBuilder setRZ(float rZ) {
        this.rZ = rZ;
        return this;
    }

    public @NotNull CylinderBuilder setRY(float rY) {
        this.rY = rY;
        return this;
    }

    public @NotNull CylinderBuilder setSnowy() {
        this.upperType = new Material[] {Material.SNOW};
        return this;
    }

    public @NotNull CylinderBuilder setHardReplace(boolean hardReplace) {
        this.hardReplace = hardReplace;
        return this;
    }

    public @NotNull CylinderBuilder setSingleBlockY(boolean singleBlockY) {
        this.singleBlockY = singleBlockY;
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
        noise.SetFrequency(0.09f);

        float effectiveRY = rY;
        if (singleBlockY) {
            effectiveRY = 0;
        }

        for (float x = -rX; x <= rX; x++) {
            for (float y = startFromZero ? 0 : -effectiveRY; y <= effectiveRY; y++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = core.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    float effectiveY = y;

                    if (Math.abs(y) / rY <= 0.7) {
                        effectiveY = 0;
                    }

                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(effectiveY, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    double noiseFuzz;
                    if (noiseMagnitude > 0) {
                        noiseFuzz = 1 + noiseMagnitude * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
                    }
                    else {
                        noiseFuzz = 1;
                    }

                    if (noiseFuzz < minRadius) {
                        noiseFuzz = minRadius;
                    }
                    if (equationResult <= noiseFuzz) {
                        unitReplace(rel);
                    }
                }
            }
        }
    }

    private void unitReplace(@NotNull SimpleBlock rel) {
        if (!hardReplace && rel.isSolid()) {
            return;
        }

        rel.setType(GenUtils.randChoice(random, types));

        if (upperType != null) {
            rel.getUp().lsetType(upperType);
        }
        if (lowerType != null && rel.getDown().isSolid()) {
            rel.getDown().setType(lowerType);
        }
    }
}
