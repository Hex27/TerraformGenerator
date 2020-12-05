package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.*;

import java.util.Random;

// A handy tool for creating mushroom stems with right curvature:
// https://www.geogebra.org/classic/hg7ckgwz
public class FractalMushroomBuilder {
    Random rand;

    SimpleBlock stemTop;

    FractalTypes.Mushroom type = FractalTypes.Mushroom.RED_GIANT_MUSHROOM;
    Material stemType = Material.MUSHROOM_STEM;
    Material capType = Material.RED_MUSHROOM_BLOCK;
    Material spotType = Material.MUSHROOM_STEM;

    int baseHeight = 18;
    int heightVariation = 0;
    float baseThickness = 3.8f;

    float segmentFactor = 2;
    Vector2f curvatureControlPoint1 = new Vector2f(-2, 0.5f);
    Vector2f curvatureControlPoint2 = new Vector2f(1.6f, 0.4f);

    double thicknessIncrement = 1;
    Vector2f thicknessControlPoint1 = new Vector2f(0.5f, 0.5f);
    Vector2f thicknessControlPoint2 = new Vector2f(0.5f, 0.5f);

    float capSize = 10;
    int capYOffset = -5;

    double minTilt = Math.PI / 48;
    double maxTilt = Math.PI / 20;

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
                        .setThicknessIncrement(1.5f)
//                        .setMinTilt(Math.PI / 6f)
//                        .setMaxTilt(Math.PI / 5f)
                        .setCapSize(15)
                        .setCapYOffset(-9);
                break;
        }
    }

    public void build(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        this.rand = tw.getRand(16L * 16 * x + 16L * y + z);
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        if (this.stemTop == null) stemTop = base;
        double initialAngle = 2 * Math.PI * Math.random();

        int initialHeight = baseHeight + GenUtils.randInt(-heightVariation, heightVariation);
        createStem(base,
                GenUtils.randDouble(rand, minTilt, maxTilt),
                initialAngle,
                baseThickness,
                initialHeight);

        spawnMushroomCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                capSize, stemTop.getRelative(0, capYOffset, 0), true, capType);
    }

    public void createStem(SimpleBlock base, double tilt, double yaw, double thickness, double length) {
        int totalSegments = (int) (length * segmentFactor);

        // The straight stem represented in 2d (x,y)
        Vector2f stem2d = new Vector2f((float) (length * Math.cos(Math.PI / 2 - tilt)), (float) (length * Math.sin(Math.PI / 2 - tilt)));

        // 2d control points
        Vector2f controlPoint1 = new Vector2f(curvatureControlPoint1.x * stem2d.x, curvatureControlPoint1.y * stem2d.y);
        Vector2f controlPoint2 = new Vector2f(curvatureControlPoint2.x * stem2d.x, curvatureControlPoint2.y * stem2d.y);

        BezierCurve curvature = new BezierCurve(new Vector2f(0, 0), controlPoint1, controlPoint2, stem2d);
        BezierCurve thicknessIncrementCurve = new BezierCurve(thicknessControlPoint1, thicknessControlPoint2);

        SimpleBlock lastSegment = null;
        for (int i = 0; i <= totalSegments; i++) {
            float progress = i / (float) totalSegments;
            Vector2f nextPos = curvature.calculate(progress);

            // Rotate the stem2d vector in 3d space using provided yaw
            Vector stem3d = new Vector(nextPos.x * Math.sin(yaw), nextPos.y, nextPos.x * Math.cos(yaw));

            lastSegment = base.getRelative(stem3d);
            replaceSphere((float) (thickness / 2f + thicknessIncrement * thicknessIncrementCurve.calculate(1 - progress).y),
                    lastSegment, stemType);
        }

        stemTop = lastSegment;
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

    public FractalMushroomBuilder setCapSize(float capSize) {
        this.capSize = capSize;
        return this;
    }

    public FractalMushroomBuilder setCapYOffset(int capYOffset) {
        this.capYOffset = capYOffset;
        return this;
    }

    /**
     * Defines how many segment points are used for drawing lines.
     * Final number of segments will be (stem length * segmentFactor).
     * Default value is 2.0. Generally you want to touch this only if
     * your mushroom is **very** curvy.
     */
    public FractalMushroomBuilder setSegmentFactor(float segmentFactor) {
        this.segmentFactor = segmentFactor;
        return this;
    }

    /**
     * Curvature is calculated with cubic Bezier curve.
     * Here you can set the control points to control the curve.
     * I also created a handy tool for testing your curves:
     * https://www.geogebra.org/classic/hg7ckgwz
     *
     * The start and end points of the curve will always
     * be (0, 0) and (1, 1), so control points should be close by.
     */
    public FractalMushroomBuilder setStemCurve(Vector2f controlPoint1, Vector2f controlPoint2) {
        this.curvatureControlPoint1 = controlPoint1;
        this.curvatureControlPoint2 = controlPoint2;
        return this;
    }

    /**
     * @see FractalMushroomBuilder#setStemCurve(Vector2f, Vector2f)
     */
    public FractalMushroomBuilder setStemCurve(float controlP1x, float controlP1y, float controlP2x, float controlP2y) {
        return setStemCurve(new Vector2f(controlP1x, controlP1y), new Vector2f(controlP2x, controlP2y));
    }

    /**
     * Thickness increment is added to the **radius** of the stem
     * based on Bezier thickness increment curve. On the ground
     * level the width of the stem will be (width + 2 * thicknessIncrement).
     *
     * @param thicknessIncrement Thickness increment towards the ground.
     */
    public FractalMushroomBuilder setThicknessIncrement(double thicknessIncrement) {
        this.thicknessIncrement = thicknessIncrement;
        return this;
    }

    /**
     * Thickness increment is calculated with cubic Bezier curve.
     * Here you can set the control points to control the curve.
     *
     * The start and end points of the curve will always
     * be (0, 0) and (1, 1), so control points should be close by.
     *
     * The curve is linear by default (=both control points are (0.5, 0.5))
     */
    public FractalMushroomBuilder setThicknessIncrementCurve(Vector2f controlPoint1, Vector2f controlPoint2) {
        this.thicknessControlPoint1 = controlPoint1;
        this.thicknessControlPoint2 = controlPoint2;
        return this;
    }

    /**
     * @see FractalMushroomBuilder#setThicknessIncrementCurve(Vector2f, Vector2f)
     */
    public FractalMushroomBuilder setThicknessIncrementCurve(float controlP1x, float controlP1y, float controlP2x, float controlP2y) {
        return setThicknessIncrementCurve(new Vector2f(controlP1x, controlP1y), new Vector2f(controlP2x, controlP2y));
    }
}
