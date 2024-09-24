package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;
import org.terraform.utils.noise.BezierCurve;
import org.terraform.utils.noise.BresenhamLine;
import org.terraform.utils.noise.FastNoise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// A handy tool for creating mushroom stems with right curvature:
// https://www.geogebra.org/classic/hg7ckgwz
public class MushroomBuilder {
    final FractalTypes.Mushroom type;
    final int heightVariation = 0;
    Random rand;
    FastNoise noiseGen;
    @Nullable
    SimpleBlock stemTop;
    FractalTypes.MushroomCap capShape = FractalTypes.MushroomCap.ROUND;
    Material stemType = Material.MUSHROOM_STEM;
    Material capType = Material.RED_MUSHROOM_BLOCK;
    Material spotType = Material.MUSHROOM_STEM;
    int baseHeight = 18;
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

    public MushroomBuilder(FractalTypes.@NotNull Mushroom type) {
        this.type = type;
        switch (type) {
            case GIANT_BROWN_FUNNEL_MUSHROOM:
                this.setCapType(Material.BROWN_MUSHROOM_BLOCK)
                    .setCapRadius(13)
                    .setCapYOffset(-2)
                    .setCapShape(FractalTypes.MushroomCap.FUNNEL);
                break;
            case GIANT_BROWN_MUSHROOM:
                this.setCapType(Material.BROWN_MUSHROOM_BLOCK);
                break;
            case GIANT_RED_MUSHROOM:
                this.setBaseThickness(6f).setThicknessIncrement(1.5f).setCapRadius(15).setCapYOffset(-10);
                break;
            case MEDIUM_BROWN_FUNNEL_MUSHROOM:
                this.setCapType(Material.BROWN_MUSHROOM_BLOCK)
                    .setCapRadius(5)
                    .setCapYOffset(-1)
                    .setBaseHeight(8)
                    .setBaseThickness(1.2f)
                    .setCapShape(FractalTypes.MushroomCap.FUNNEL);
                break;
            case MEDIUM_BROWN_MUSHROOM:
                this.setCapType(Material.BROWN_MUSHROOM_BLOCK)
                    .setCapRadius(5)
                    .setCapYOffset(-2)
                    .setBaseHeight(8)
                    .setBaseThickness(1.2f);
                break;
            case MEDIUM_RED_MUSHROOM:
                this.setBaseThickness(1.3f)
                    .setBaseHeight(8)
                    .setThicknessIncrement(1.2f)
                    .setCapRadius(4.5f)
                    .setCapYOffset(-3);
                break;
            case SMALL_BROWN_MUSHROOM:
                this.setBaseThickness(0)
                    .setThicknessIncrement(0)
                    .setBaseHeight(5)
                    .setCapType(Material.BROWN_MUSHROOM_BLOCK)
                    .setMinTilt(0)
                    .setMaxTilt(Math.PI / 8)
                    .setStemCurve(0.8f, 0.2f, 0.8f, 0.4f)
                    .setFourAxisRotationOnly(true)
                    .setCapShape(FractalTypes.MushroomCap.FLAT)
                    .setCapRadius(3f)
                    .setCapYOffset(0);
                break;
            case SMALL_RED_MUSHROOM:
                this.setBaseThickness(0)
                    .setThicknessIncrement(0.5f)
                    .setBaseHeight(4)
                    .setMaxTilt(Math.PI / 8)
                    .setMinTilt(0)
                    .setStemCurve(0.8f, 0.2f, 0.8f, 0.4f)
                    .setFourAxisRotationOnly(true)
                    .setCapRadius(2.3f)
                    .setCapYOffset(-1);
                break;
            case SMALL_POINTY_RED_MUSHROOM:
                this.setBaseThickness(0)
                    .setThicknessIncrement(0)
                    .setBaseHeight(6)
                    .setMaxTilt(Math.PI / 18)
                    .setMinTilt(0)
                    .setFourAxisRotationOnly(true)
                    .setStemCurve(0.8f, 0.2f, 0.8f, 0.4f)
                    .setCapRadius(2.3f)
                    .setCapYOffset(-2)
                    .setCapShape(FractalTypes.MushroomCap.POINTY);
                break;
            case TINY_RED_MUSHROOM:
                this.setBaseThickness(0)
                    .setThicknessIncrement(0)
                    .setBaseHeight(4)
                    .setMinTilt(0)
                    .setMaxTilt(0)
                    .setStemCurve(0.5f, 0.5f, 0.5f, 0.5f)
                    .setSegmentFactor(1)
                    .setCapRadius(1.2f)
                    .setCapYOffset(-2)
                    .setCapShape(FractalTypes.MushroomCap.POINTY);
            case TINY_BROWN_MUSHROOM:
                this.setBaseThickness(0)
                    .setThicknessIncrement(0)
                    .setBaseHeight(2)
                    .setMinTilt(0)
                    .setMaxTilt(0)
                    .setStemCurve(0.5f, 0.5f, 0.5f, 0.5f)
                    .setSegmentFactor(1)
                    .setCapRadius(1.5f)
                    .setCapYOffset(1)
                    .setCapShape(FractalTypes.MushroomCap.ROUND)
                    .setCapType(Material.BROWN_MUSHROOM_BLOCK);
                break;
        }
    }

    private void replaceSphere(float radius, @NotNull SimpleBlock base, @NotNull Material type) {
        if (radius < 0.5) {
            if (!base.isSolid()) {
                base.setType(type);
            }

            return;
        }

        noiseGen.SetNoiseType(FastNoise.NoiseType.Simplex);
        noiseGen.SetFrequency(0.09f);

        for (int x = -Math.round(radius); x <= Math.round(radius); x++) {
            for (int y = -Math.round(radius); y <= Math.round(radius); y++) {
                for (int z = -Math.round(radius); z <= Math.round(radius); z++) {
                    SimpleBlock block = base.getRelative(x, y, z);

                    if (Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(y, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2) <= 1 + 0.7 * noiseGen.GetNoise(
                            block.getX(),
                            block.getY(),
                            block.getZ()
                    ))
                    {
                        if (!block.isSolid()) {
                            block.setType(type);
                        }
                    }
                }
            }
        }
    }

    private void spawnSphericalCap(int seed,
                                   float r,
                                   float ry,
                                   @NotNull SimpleBlock base,
                                   boolean hardReplace,
                                   Material... type)
    {
        Random rand = new Random(seed);
        // FastNoise noise = new FastNoise(seed);
        noiseGen.SetNoiseType(FastNoise.NoiseType.Simplex);
        noiseGen.SetFrequency(1.4f);

        float belowY = -0.25f * 2 * ry;
        float lowThreshold = Math.min(
                (float) (0.6 / 5 * Math.min(r, ry)),
                0.6f
        ); // When radius < 5 mushrooms less hollow

        for (int x = Math.round(-r); x <= Math.round(r); x++) {
            for (int y = Math.round(belowY); y <= Math.round(ry); y++) {
                for (int z = Math.round(-r); z <= Math.round(r); z++) {
                    float factor = y / belowY;

                    // Hems
                    if (y < 0 && factor + Math.abs(noiseGen.GetNoise(x / r, z / r)) > 0.6) {
                        continue;
                    }

                    SimpleBlock rel = base.getRelative(x, y, z);
                    double equationResult = Math.pow(x, 2) / Math.pow(r, 2)
                                            + Math.pow(y, 2) / Math.pow(ry, 2)
                                            + Math.pow(z, 2) / Math.pow(r, 2);

                    if (equationResult <= 1 + 0.25 * Math.abs(noiseGen.GetNoise(x / r, y / ry, z / r))
                        && equationResult >= lowThreshold)
                    {

                        if (hardReplace || !rel.isSolid()) {
                            rel.setType(GenUtils.randChoice(rand, type));
                            BlockUtils.correctSurroundingMushroomData(rel);
                        }
                    }
                }
            }
        }
    }

    public void build(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.areTallMushroomsEnabled()) {
            return;
        }

        if (TConfig.c.DEVSTUFF_VANILLA_MUSHROOMS) {
            String schemName;
            if (this.type.toString().contains("RED")) {
                schemName = VanillaMushroomBuilder.RED_MUSHROOM_CAP;
            }
            else {
                schemName = VanillaMushroomBuilder.BROWN_MUSHROOM_CAP;
            }

            VanillaMushroomBuilder.buildVanillaMushroom(tw, data, x, y, z, schemName);
            return;
        }

        this.noiseGen = new FastNoise((int) tw.getSeed());
        this.rand = tw.getRand(16L * 16 * x + 16L * y + z);
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        if (this.stemTop == null) {
            stemTop = base;
        }

        double initialAngle;
        if (fourAxisRotationOnly) {
            initialAngle = (Math.PI / 2.0) * Math.round(Math.random() * 4);
        }
        else {
            initialAngle = 2 * Math.PI * Math.random();
        }

        int initialHeight = baseHeight + GenUtils.randInt(-heightVariation, heightVariation);
        createStem(base, GenUtils.randDouble(rand, minTilt, maxTilt), initialAngle, baseThickness, initialHeight);

        switch (capShape) {
            case ROUND:
                spawnSphericalCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                        capRadius,
                        capRadius,
                        stemTop.getRelative(0, capYOffset, 0),
                        true,
                        capType
                );
                break;
            case FLAT:
                spawnSphericalCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                        capRadius,
                        0.6f * capRadius,
                        stemTop.getRelative(0, capYOffset, 0),
                        true,
                        capType
                );
                break;
            case POINTY:
                spawnSphericalCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                        capRadius,
                        capRadius * 1.8f,
                        stemTop.getRelative(0, capYOffset, 0),
                        true,
                        capType
                );
                break;
            case FUNNEL: // Implement funnel algorithm
                spawnFunnelCap(tw.getHashedRand(x, y, z).nextInt(94929297),
                        capRadius,
                        capRadius * 0.7f,
                        capRadius * 0.1f,
                        stemTop.getRelative(0, capYOffset, 0),
                        capType
                );
                break;
        }
    }

    private void createStem(@NotNull SimpleBlock base, double tilt, double yaw, double thickness, double length) {
        int totalSegments = (int) (length * segmentFactor);
        // If only one block wide, only place one block per y level = looks more natural
        boolean oneBlockWide = thickness == 0;

        // The straight stem represented in 2d (x,y)
        Vector2f stem2d = new Vector2f(
                (float) (length * Math.cos(Math.PI / 2 - tilt)),
                (float) (length * Math.sin(Math.PI / 2 - tilt))
        );

        // 2d control points
        Vector2f controlPoint1 = new Vector2f(curvatureControlPoint1.x * stem2d.x, curvatureControlPoint1.y * stem2d.y);
        Vector2f controlPoint2 = new Vector2f(curvatureControlPoint2.x * stem2d.x, curvatureControlPoint2.y * stem2d.y);

        BezierCurve curvature = new BezierCurve(new Vector2f(0, 0), controlPoint1, controlPoint2, stem2d);
        BezierCurve thicknessIncrementCurve = new BezierCurve(thicknessControlPoint1, thicknessControlPoint2);

        List<Integer> changedYs = new ArrayList<>();

        SimpleBlock lastSegment = null;
        for (int i = 0; i <= totalSegments; i++) {
            float progress = i / (float) totalSegments;
            Vector2f nextPos = curvature.calculate(progress);

            // Rotate the stem2d vector in 3d space using provided yaw
            Vector stem3d = new Vector(nextPos.x * Math.sin(yaw), nextPos.y, nextPos.x * Math.cos(yaw));

            lastSegment = base.getRelative(stem3d);

            if (!changedYs.contains(lastSegment.getY()) || !oneBlockWide) {
                replaceSphere((float) (thickness / 2f + thicknessIncrement * thicknessIncrementCurve.calculate(1
                                                                                                               - progress).y),
                        lastSegment,
                        stemType
                );

                changedYs.add(lastSegment.getY());
            }
        }

        stemTop = lastSegment;
    }

    private void spawnFunnelCap(int seed,
                                float r,
                                float height,
                                float thickness,
                                @NotNull SimpleBlock base,
                                Material... type)
    {
        Random rand = new Random(seed);
        // FastNoise noise = new FastNoise(seed);
        noiseGen.SetNoiseType(FastNoise.NoiseType.Simplex);
        noiseGen.SetFrequency(1.4f);

        for (int x = Math.round(-r); x <= Math.round(r); x++) {
            for (int y = 0; y <= Math.round(3 * thickness); y++) {
                for (int z = Math.round(-r); z <= Math.round(r); z++) {
                    // Replace blocks in the middle
                    if (stemTop.getRelative(0, y, 0).getType() == stemType) {
                        stemTop.getRelative(0, y, 0).setType(GenUtils.randChoice(rand, type));
                    }

                    double distToCenter = Math.sqrt(x * x + z * z) / r;

                    // Actual Y value calculated with mafs magic
                    // https://www.geogebra.org/classic/x3xzkzwd < the curve
                    double realY = y + height * (Math.pow(distToCenter + 0.02, 0.5) - Math.pow(distToCenter - 0.15, 8));
                    realY += thickness * Math.abs(noiseGen.GetNoise(x / r, z / r)); // Noise
                    SimpleBlock rel = base.getRelative(x, (int) Math.round(realY), z);

                    double equationResult = Math.pow(x / r, 2)
                                            // For height max height: https://www.geogebra.org/classic/k5nefypu
                                            + Math.pow(Math.abs(y) / (thickness / (1 - Math.pow(1 - (distToCenter
                                                                                                     + 0.1), 6))), 4)
                                            + Math.pow(z / r, 2);

                    if (equationResult <= 1) {
                        rel.setType(GenUtils.randChoice(rand, type));
                        BlockUtils.correctSurroundingMushroomData(rel);
                    }
                }
            }
        }

        // Mushroom gills
        double angle = Math.random() * Math.PI * 2;
        int heightLimit = base.getY() + Math.round(height);
        int gillAmount = 16;

        for (int i = 0; i < gillAmount; i++) {
            angle += Math.PI / (gillAmount / 2.0); // Do full circle

            // Points from middle to a point on circle with radius of 0.9r
            List<Vector2f> points = new BresenhamLine(new Vector2f(0, 0),
                    new Vector2f((float) (0.9 * r * Math.cos(angle)), (float) (0.9 * r * Math.sin(angle)))
            ).getPoints();

points:
            for (Vector2f point : points) {
                SimpleBlock pointBase = base.getRelative(Math.round(point.x), 0, Math.round(point.y));

                while (true) {
                    if (pointBase.isSolid()) {
                        if (BlockUtils.isAir(pointBase.getDown().getType())) {
                            pointBase.getDown().setType(Material.MUSHROOM_STEM);
                        }
                        continue points;
                    }
                    else {
                        pointBase = pointBase.getUp();
                    }

                    if (pointBase.getY() > heightLimit) {
                        continue points;
                    }
                }
            }
        }
    }

    private @NotNull MushroomBuilder setBaseThickness(float baseThickness) {
        this.baseThickness = baseThickness;
        return this;
    }

    private @NotNull MushroomBuilder setBaseHeight(int h) {
        this.baseHeight = h;
        return this;
    }

    private @NotNull MushroomBuilder setStemType(Material stemType) {
        this.stemType = stemType;
        return this;
    }

    private @NotNull MushroomBuilder setCapType(Material capType) {
        this.capType = capType;
        return this;
    }

    private @NotNull MushroomBuilder setSpotType(Material spotType) {
        this.spotType = spotType;
        return this;
    }

    private @NotNull MushroomBuilder setMinTilt(double minTilt) {
        this.minTilt = minTilt;
        return this;
    }

    private @NotNull MushroomBuilder setMaxTilt(double maxTilt) {
        this.maxTilt = maxTilt;
        return this;
    }

    private @NotNull MushroomBuilder setCapRadius(float capRadius) {
        this.capRadius = capRadius;
        return this;
    }

    private @NotNull MushroomBuilder setCapYOffset(int capYOffset) {
        this.capYOffset = capYOffset;
        return this;
    }

    /**
     * Defines how many segment points are used for drawing lines.
     * Final number of segments will be (stem length * segmentFactor).
     * Default value is 2.0. Generally you want to touch this only if
     * your mushroom is **very** curvy.
     */
    private @NotNull MushroomBuilder setSegmentFactor(float segmentFactor) {
        this.segmentFactor = segmentFactor;
        return this;
    }

    /**
     * Curvature is calculated with cubic Bezier curve.
     * Here you can set the control points to control the curve.
     * I also created a handy tool for testing your curves:
     * <a href="https://www.geogebra.org/classic/hg7ckgwz">...</a>
     * <p>
     * The start and end points of the curve will always
     * be (0, 0) and (1, 1), so control points should be close by.
     */
    private @NotNull MushroomBuilder setStemCurve(Vector2f controlPoint1, Vector2f controlPoint2) {
        this.curvatureControlPoint1 = controlPoint1;
        this.curvatureControlPoint2 = controlPoint2;
        return this;
    }

    /**
     * @see MushroomBuilder#setStemCurve(Vector2f, Vector2f)
     */
    private @NotNull MushroomBuilder setStemCurve(float controlP1x,
                                                  float controlP1y,
                                                  float controlP2x,
                                                  float controlP2y)
    {
        return setStemCurve(new Vector2f(controlP1x, controlP1y), new Vector2f(controlP2x, controlP2y));
    }

    /**
     * Thickness increment is added to the **radius** of the stem
     * based on Bezier thickness increment curve. On the ground
     * level the width of the stem will be (width + 2 * thicknessIncrement).
     *
     * @param thicknessIncrement Thickness increment towards the ground.
     */
    private @NotNull MushroomBuilder setThicknessIncrement(double thicknessIncrement) {
        this.thicknessIncrement = thicknessIncrement;
        return this;
    }

    /**
     * Thickness increment is calculated with cubic Bezier curve.
     * Here you can set the control points to control the curve.
     * <p>
     * The start and end points of the curve will always
     * be (0, 0) and (1, 1), so control points should be close by.
     * <p>
     * The curve is linear by default (=both control points are (0.5, 0.5))
     */
    private @NotNull MushroomBuilder setThicknessIncrementCurve(Vector2f controlPoint1, Vector2f controlPoint2) {
        this.thicknessControlPoint1 = controlPoint1;
        this.thicknessControlPoint2 = controlPoint2;
        return this;
    }

    /**
     * @see MushroomBuilder#setThicknessIncrementCurve(Vector2f, Vector2f)
     */
    private @NotNull MushroomBuilder setThicknessIncrementCurve(float controlP1x,
                                                                float controlP1y,
                                                                float controlP2x,
                                                                float controlP2y)
    {
        return setThicknessIncrementCurve(new Vector2f(controlP1x, controlP1y), new Vector2f(controlP2x, controlP2y));
    }

    private @NotNull MushroomBuilder setCapShape(FractalTypes.MushroomCap capShape) {
        this.capShape = capShape;
        return this;
    }

    private @NotNull MushroomBuilder setFourAxisRotationOnly(boolean fourAxisRotationOnly) {
        this.fourAxisRotationOnly = fourAxisRotationOnly;
        return this;
    }
}
