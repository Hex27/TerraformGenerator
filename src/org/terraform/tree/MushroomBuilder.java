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
public class MushroomBuilder {
    Random rand;

    SimpleBlock stemTop;

    FractalTypes.Mushroom type = FractalTypes.Mushroom.GIANT_RED_MUSHROOM;
    FractalTypes.MushroomCap capShape = FractalTypes.MushroomCap.ROUND;
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

    float capRadius = 10;
    int capYOffset = -5;

    double minTilt = Math.PI / 48;
    double maxTilt = Math.PI / 20;
    boolean fourAxisRotationOnly = false;

    public MushroomBuilder(FractalTypes.Mushroom type) {
        this.type = type;
        switch (type) {
//            case BROWN_GIANT_MUSHROOM:
//                this.setType(FractalTypes.Mushroom.BROWN_GIANT_MUSHROOM)
//                        .setCapType(Material.BROWN_MUSHROOM_BLOCK);
//                break;
            case GIANT_BROWN_MUSHROOM:
                this
//                        .setCapShape(FractalTypes.MushroomCap.POINTY)
                        .setCapType(Material.BROWN_MUSHROOM_BLOCK);
                break;
            case GIANT_RED_MUSHROOM:
                this.setBaseThickness(6f)
                        .setThicknessIncrement(1.5f)
//                        .setMinTilt(Math.PI / 6f)
//                        .setMaxTilt(Math.PI / 5f)
                        .setCapRadius(15)
                        .setCapYOffset(-9);
                break;
            case SMALL_RED_MUSHROOM:
                this.setBaseThickness(0)
                        .setThicknessIncrement(0.5f)
                        .setBaseHeight(4)
                        .setMaxTilt(Math.PI / 8)
                        .setMinTilt(0)
                        .setStemCurve(0, 0.6f, 1, 0.4f)
                        .setFourAxisRotationOnly(true)
                        .setSegmentFactor(1)
                        .setCapRadius(2.5f)
                        .setCapYOffset(-1);
                break;
            case SMALL_POINTY_RED_MUSHROOM:
                this.setBaseThickness(0)
                        .setThicknessIncrement(0)
                        .setBaseHeight(6)
                        .setMaxTilt(Math.PI / 18)
                        .setMinTilt(0)
                        .setFourAxisRotationOnly(true)
                        .setStemCurve(0.5f, 0.5f, 0.5f, 0.5f)
                        .setSegmentFactor(1f)
                        .setCapRadius(2.3f)
                        .setCapYOffset(-2)
                        .setCapShape(FractalTypes.MushroomCap.POINTY);
                break;
            case TINY_RED_MUSHROOM:
                this.setBaseThickness(0)
                        .setBaseHeight(4)
                        .setMinTilt(0)
                        .setMaxTilt(0)
                        .setStemCurve(0.5f, 0.5f, 0.5f, 0.5f)
                        .setSegmentFactor(1)
                        .setCapRadius(1.2f)
                        .setCapYOffset(-2)
                        .setCapShape(FractalTypes.MushroomCap.POINTY);
                break;
        }
    }

    public void build(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        this.rand = tw.getRand(16L * 16 * x + 16L * y + z);
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        if (this.stemTop == null) stemTop = base;

        double initialAngle;
        if (fourAxisRotationOnly)
            initialAngle = (Math.PI / 2.0) * Math.round(Math.random() * 4);
        else initialAngle = 2 * Math.PI * Math.random();

        System.out.println();

        int initialHeight = baseHeight + GenUtils.randInt(-heightVariation, heightVariation);
        createStem(base,
                GenUtils.randDouble(rand, minTilt, maxTilt),
                initialAngle,
                baseThickness,
                initialHeight);

        switch (capShape) {
            case ROUND:
                MushroomCapMaker.spawnRoundCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                        capRadius, stemTop.getRelative(0, capYOffset, 0), true, capType);
                break;
            case POINTY:
                MushroomCapMaker.spawnSphericalCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                        capRadius, capRadius * 1.8f, stemTop.getRelative(0, capYOffset, 0), true, capType);
                break;
        }
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
        if (radius < 0.5) {
            if (!base.getType().isSolid())
                base.setType(type);

            return;
        }

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

    public MushroomBuilder setBaseThickness(float baseThickness) {
        this.baseThickness = baseThickness;
        return this;
    }

    public MushroomBuilder setBaseHeight(int h) {
        this.baseHeight = h;
        return this;
    }

    public MushroomBuilder setStemType(Material stemType) {
        this.stemType = stemType;
        return this;
    }

    public MushroomBuilder setCapType(Material capType) {
        this.capType = capType;
        return this;
    }

    public MushroomBuilder setSpotType(Material spotType) {
        this.spotType = spotType;
        return this;
    }

    public MushroomBuilder setMinTilt(double minTilt) {
        this.minTilt = minTilt;
        return this;
    }

    public MushroomBuilder setMaxTilt(double maxTilt) {
        this.maxTilt = maxTilt;
        return this;
    }

    public MushroomBuilder setCapRadius(float capRadius) {
        this.capRadius = capRadius;
        return this;
    }

    public MushroomBuilder setCapYOffset(int capYOffset) {
        this.capYOffset = capYOffset;
        return this;
    }

    /**
     * Defines how many segment points are used for drawing lines.
     * Final number of segments will be (stem length * segmentFactor).
     * Default value is 2.0. Generally you want to touch this only if
     * your mushroom is **very** curvy.
     */
    public MushroomBuilder setSegmentFactor(float segmentFactor) {
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
    public MushroomBuilder setStemCurve(Vector2f controlPoint1, Vector2f controlPoint2) {
        this.curvatureControlPoint1 = controlPoint1;
        this.curvatureControlPoint2 = controlPoint2;
        return this;
    }

    /**
     * @see MushroomBuilder#setStemCurve(Vector2f, Vector2f)
     */
    public MushroomBuilder setStemCurve(float controlP1x, float controlP1y, float controlP2x, float controlP2y) {
        return setStemCurve(new Vector2f(controlP1x, controlP1y), new Vector2f(controlP2x, controlP2y));
    }

    /**
     * Thickness increment is added to the **radius** of the stem
     * based on Bezier thickness increment curve. On the ground
     * level the width of the stem will be (width + 2 * thicknessIncrement).
     *
     * @param thicknessIncrement Thickness increment towards the ground.
     */
    public MushroomBuilder setThicknessIncrement(double thicknessIncrement) {
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
    public MushroomBuilder setThicknessIncrementCurve(Vector2f controlPoint1, Vector2f controlPoint2) {
        this.thicknessControlPoint1 = controlPoint1;
        this.thicknessControlPoint2 = controlPoint2;
        return this;
    }

    /**
     * @see MushroomBuilder#setThicknessIncrementCurve(Vector2f, Vector2f)
     */
    public MushroomBuilder setThicknessIncrementCurve(float controlP1x, float controlP1y, float controlP2x, float controlP2y) {
        return setThicknessIncrementCurve(new Vector2f(controlP1x, controlP1y), new Vector2f(controlP2x, controlP2y));
    }

    public MushroomBuilder setCapShape(FractalTypes.MushroomCap capShape) {
        this.capShape = capShape;
        return this;
    }

    public MushroomBuilder setFourAxisRotationOnly(boolean fourAxisRotationOnly) {
        this.fourAxisRotationOnly = fourAxisRotationOnly;
        return this;
    }
}
