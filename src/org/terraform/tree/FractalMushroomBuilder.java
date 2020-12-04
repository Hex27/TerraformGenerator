package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class FractalMushroomBuilder {
    Random rand;

    SimpleBlock topBlock;

    FractalTypes.Mushroom type = FractalTypes.Mushroom.RED_GIANT_MUSHROOM;
    Material stemType = Material.MUSHROOM_STEM;
    Material capType = Material.RED_MUSHROOM_BLOCK;
    Material spotType = Material.MUSHROOM_STEM;

    int baseHeight = 18;
    int heightVariation = 0;
    float baseThickness = 3.8f;

    // Curvature and thickness increment are both calculated with a
    // power function using the current height. See javadocs on setters
    double curvature = 7;
    float curvaturePower = 4;
    double thicknessIncrement = 1;
    float thicknessIncrementPower = 1.3f;

    float capSize = 10;
    int capYOffset = -5;

    double minTilt = Math.PI / 8;
    double maxTilt = Math.PI / 4;

    public FractalMushroomBuilder(FractalTypes.Mushroom type) {
        this.type = type;
        switch (type) {
            case BROWN_GIANT_MUSHROOM:
                this.setType(FractalTypes.Mushroom.BROWN_GIANT_MUSHROOM)
                        .setCapType(Material.BROWN_MUSHROOM_BLOCK);
                break;
            case RED_GIANT_MUSHROOM:
                this.setType(FractalTypes.Mushroom.RED_GIANT_MUSHROOM)
                        .setBaseThickness(6f)
                        .setCurvature(10)
                        .setCurvaturePower(8)
                        .setThicknessIncrement(1.5f)
                        .setThicknessIncrementPower(1.1f)
                        .setMinTilt(Math.PI / 6f)
                        .setMaxTilt(Math.PI / 5f)
                        .setCapSize(15)
                        .setCapYOffset(-9);
                break;
        }
    }

    public void build(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        this.rand = tw.getRand(16L * 16 * x + 16L * y + z);
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        if (this.topBlock == null) topBlock = base;
        double initialAngle = 2 * Math.PI * Math.random();

        int initialHeight = baseHeight + GenUtils.randInt(-heightVariation, heightVariation);
        createStem(base,
                GenUtils.randDouble(rand, minTilt, maxTilt),
                initialAngle,
                baseThickness,
                initialHeight);

        spawnMushroomCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                capSize, topBlock.getRelative(0, capYOffset, 0), true, capType);
    }

    public void createStem(SimpleBlock base, double tilt, double yaw, double thickness, double length) {
        int segments = (int) (length * curvature);

        tilt += Math.PI / 2f;
        Vector v = new Vector(length * Math.cos(tilt) * Math.sin(yaw), length * Math.sin(tilt), length * Math.cos(yaw) * Math.cos(tilt));

        SimpleBlock lastSegment = null;
        for (int i = 0; i <= segments; i++) {
            Vector seg = v.clone().multiply(i / ((float) segments));
            seg.setY(seg.getY() + curvature * Math.pow(i / (float) segments, curvaturePower)); // Straighten the stem
            lastSegment = base.getRelative(seg);
            replaceSphere((float) (thickness / 2f + thicknessIncrement * Math.pow(1 - i / (float) segments, thicknessIncrementPower)),
                    lastSegment, stemType);
        }

        topBlock = lastSegment;
    }

    private void replaceSphere(float radius, SimpleBlock base, Material type) {
        FastNoise noise = new FastNoise();
        noise.SetNoiseType(FastNoise.NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (int x = -Math.round(radius); x <= Math.round(radius); x++) {
            for (int y = -Math.round(radius); y <= Math.round(radius); y++) {
                for (int z = -Math.round(radius); z <= Math.round(radius); z++) {
                    SimpleBlock block = base.getRelative(x, y, z);

                    if (Math.pow(x, 2) / Math.pow(radius, 2) +
                            Math.pow(y, 2) / Math.pow(radius, 2) +
                            Math.pow(z, 2) / Math.pow(radius, 2)
                            <= 1 + 0.7 * noise.GetNoise(block.getX(), block.getY(), block.getZ())) {
                        if (!block.getType().isSolid()) {
                            block.setType(type);
                        }
                    }
                }
            }
        }
    }

    public double randomAngle() {
        return GenUtils.randDouble(rand, minTilt, maxTilt);
    }

    /**
     * Random-thirty-ish-angle
     *
     * @return An angle between 0.8*30 to 1.2*30 degrees in radians
     */
    public double rta() {
        return GenUtils.randDouble(new Random(), 0.8 * Math.PI / 6, 1.2 * Math.PI / 6);
    }

    /**
     * Random-angle
     *
     * @return An angle between lowerBound*30 to upperBound*30 degrees in radians
     */
    public double ra(double base, double lowerBound, double upperBound) {
        return GenUtils.randDouble(new Random(), lowerBound * base, upperBound * base);
    }

    public static void spawnMushroomCap(int seed, float r, SimpleBlock block, boolean hardReplace, Material... type) {
        Random rand = new Random(seed);
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(FastNoise.NoiseType.Simplex);
        noise.SetFrequency(0.09f);
        for (float x = -r; x <= r; x++) {
            for (float y = 0; y <= r; y++) {
                for (float z = -r; z <= r; z++) {

                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(r, 2)
                            + Math.pow(y, 2) / Math.pow(r, 2)
                            + Math.pow(z, 2) / Math.pow(r, 2);
                    if (equationResult <= 1 + 0.3 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())
                            && equationResult >= 0.5) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.getType().isSolid()) {
                            rel.setType(GenUtils.randMaterial(rand, type));
                            BlockUtils.correctSurroundingMushroomData(rel);
                        }
                        //rel.setReplaceType(ReplaceType.ALL);
                    }
                }
            }
        }
    }

    public FractalMushroomBuilder setType(FractalTypes.Mushroom type) {
        this.type = type;
        return this;
    }

    public FractalMushroomBuilder setBaseThickness(float baseThickness) {
        this.baseThickness = baseThickness;
        return this;
    }

    public FractalMushroomBuilder setBaseHeight(int h) {
        this.baseHeight = h;
        return this;
    }

    public FractalMushroomBuilder setStemType(Material stemType) {
        this.stemType = stemType;
        return this;
    }

    public FractalMushroomBuilder setCapType(Material capType) {
        this.capType = capType;
        return this;
    }

    public FractalMushroomBuilder setSpotType(Material spotType) {
        this.spotType = spotType;
        return this;
    }

    public FractalMushroomBuilder setMinTilt(double minTilt) {
        this.minTilt = minTilt;
        return this;
    }

    public FractalMushroomBuilder setMaxTilt(double maxTilt) {
        this.maxTilt = maxTilt;
        return this;
    }

    /**
     * Curvature is added to the final length of the stem.
     * The result will be more straight stems towards the top.
     * A value to the current height is added with the following
     * equation: height += curvature * (progresss^curvaturePower),
     * where progress is value between 0 at ground level and 1 at stem top.
     *
     * @param curvature Curvature. Higher values mean higher and
     *                  more straightened mushrooms towards the top.
     */
    public FractalMushroomBuilder setCurvature(double curvature) {
        this.curvature = curvature;
        return this;
    }

    /**
     * @see FractalMushroomBuilder#setCurvature(double)
     */
    public FractalMushroomBuilder setCurvaturePower(float curvaturePower) {
        this.curvaturePower = curvaturePower;
        return this;
    }
    /**
     * Thickness increment is added to the final thickness (radius) of the stem
     * using a power function, resulting more thick stems towards the ground.
     * The following function is used: radius += thicknessIncrement * ((1 - progress)^thicknessIncrementPower),
     * where progress is value between 0 at ground level and 1 at stem top.
     *
     * @param thicknessIncrement Thickness increment towards the ground.
     */
    public FractalMushroomBuilder setThicknessIncrement(double thicknessIncrement) {
        this.thicknessIncrement = thicknessIncrement;
        return this;
    }

    /**
     * @see FractalMushroomBuilder#setThicknessIncrementPower(float)
     */
    public FractalMushroomBuilder setThicknessIncrementPower(float thicknessIncrementPower) {
        this.thicknessIncrementPower = thicknessIncrementPower;
        return this;
    }

    public FractalMushroomBuilder setCapSize(float capSize) {
        this.capSize = capSize;
        return this;
    }

    public FractalMushroomBuilder setCapYOffset(int capYOffset) {
        this.capYOffset = capYOffset;
        return this;
    }
}
