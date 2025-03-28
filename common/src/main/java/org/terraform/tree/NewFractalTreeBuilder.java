package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.version.BeeHiveSpawner;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * This class isn't designed to run in a multithreaded environment
 */
public class NewFractalTreeBuilder implements Cloneable {

    private static final int[][] rotationMatrixX = new int[][] {
            new int[] {1, 0, 0}, new int[] {0, 0, -1}, new int[] {0, 1, 0}
    };
    private static final int[][] rotationMatrixZ = new int[][] {
            new int[] {0, -1, 0}, new int[] {1, 0, 0}, new int[] {0, 0, 1}
    };
    final int maxHeight = 9999;
    // Pitch rotates up or down, yaw rotates left/right
    private final Vector initialNormal = new Vector(0, 1, 0);// .normalize();
    private final double displacementThetaDelta = 2 * Math.PI;
    // Supposedly final fields not meant for mutation during build
    private int maxDepth = 3; // Last branch is depth 1
    private int originalTrunkLength = 20; // Starting length of the tree trunk
    private float lengthVariance = 4;
    private float firstEnd = 0.8f; // First branch will end before it hits full length
    private int crownBranches = 4; // Number of branches to spawn from the top of the first branch
    private float initialBranchRadius = 3f;
    private double branchSpawnChance = 0.08f; // Chance for a new branch to spawn while stepping a branch
    private float minBranchSpawnLength = 0.4f; // New branches only spawn at this branch ratio.
    private int randomBranchSegmentCount = 3; // Controls how randomised branches are placed. Attempts to rotate branches evenly to prevent weird looks.
    private float randomBranchSpawnCooldown = 0; // Controls the cooldown after every branch spawn before the next
    private int randomBranchClusterCount = 1; // Number of random branches to spawn per successful roll
    private double maxInitialNormalDelta = 0.3;
    private double minInitialNormalDelta = -0.3;
    private double minBranchHorizontalComponent = 0.5;
    private double maxBranchHorizontalComponent = 1.3;
    private int treeRootThreshold = 2;
    private float treeRootMultiplier = 1.5f;
    private float noisePriority = 0.1f;
    private int leafSpawnDepth = 1;
    private FractalLeaves fractalLeaves;

    private BiConsumer<Random, SimpleBlock> prePlacement = null;

    /**
     * This function determines how branches will decrease in length
     * each recursion.
     */
    private BiFunction<Float, Float, Float> branchDecrement = (currentBranchLength, totalTreeHeight) ->
            currentBranchLength
            * 0.7f;
    /**
     * This function determines how branch width will decrease
     * each recursion.
     */
    private BiFunction<Float, Float, Float> getBranchWidth = (initialBranchWidth, branchRatio) -> initialBranchWidth * (
            1f
            - branchRatio / 2f);
    private Material branchMaterial = Material.OAK_LOG;
    private Material rootMaterial = Material.OAK_WOOD;

    // [No more mutable fields. They caused concurrency problems]===================
    private boolean spawnBees = false;
    private boolean checkGradient = true;

    public boolean build(@NotNull TerraformWorld tw, @NotNull SimpleBlock base)
    {
        if (!TConfig.areTreesEnabled()) {
            return false;
        }

        // Clear and set mutable structures
        if (!checkGradient(base.getPopData(), base.getX(), base.getZ())) {
            return false;
        }

        // Do preprocessing
        int oriY = base.getY();
        Random random = tw.getHashedRand(base.getX(), base.getY(), base.getZ());
        if(prePlacement != null)
            prePlacement.accept(random, base);

        double displacementTheta = GenUtils.randDouble(random, 0, displacementThetaDelta);
        HashSet<SimpleBlock> prospectiveHives = new HashSet<>();
        double currentBranchTheta = GenUtils.randInt(random, 0, randomBranchSegmentCount);

        fractalLeaves.purgeOccupiedLeavesCache();

        // Spawn the actual tree
        branch(tw,
                random,
                base,
                initialNormal.clone()
                             .add(new Vector(GenUtils.randDouble(random, minInitialNormalDelta, maxInitialNormalDelta),
                                     0,
                                     GenUtils.randDouble(random, minInitialNormalDelta, maxInitialNormalDelta)
                             ))
                             .normalize(),
                prospectiveHives,
                currentBranchTheta,
                oriY,
                displacementTheta,
                originalTrunkLength + (float) GenUtils.randDouble(random, -lengthVariance, lengthVariance),
                firstEnd,
                0,
                this.initialBranchRadius,
                0
        );

        // Process prospectiveHives
        if (spawnBees) {
            for (SimpleBlock b : prospectiveHives) {
                // TerraformGeneratorPlugin.logger.info("Testing " + b);
                if (b.isSolid()) {
                    continue; // occupied block
                }
                // TerraformGeneratorPlugin.logger.info("Success: " + b);
                BeeHiveSpawner.spawnFullBeeNest(b);
                break; // just one.
            }
        }

        // Undo mutated changes
        this.fractalLeaves.setSnowy(false);

        return true;
    }

    boolean checkGradient(PopulatorDataAbstract data, int x, int z) {
        return !checkGradient || (HeightMap.getTrueHeightGradient(data, x, z, 3)
                                  <= TConfig.c.MISC_TREES_GRADIENT_LIMIT);
    }

    public @NotNull NewFractalTreeBuilder setCheckGradient(boolean checkGradient)
    {
        this.checkGradient = checkGradient;
        return this;
    }

    /**
     * @param base               the start of the branch's base.
     * @param normal             the direction of the branch represented via a unit vector
     * @param prospectiveHives   a collection of possible beehive locations
     * @param currentBranchTheta a counter for getNextTheta to spawn cluster branches properly
     * @param oriY               original tree base Y
     * @param displacementTheta  I forgot what this is
     * @param length             length of the branch
     * @param end                is the percentage from 0.0 to 1.0 for where the branch is considered done
     * @param depth              of the current recursion. Starts from 0 and stops at maxDepth
     * @param currentWidth       width of the current recursion
     */
    void branch(TerraformWorld tw,
                @NotNull Random random,
                @NotNull SimpleBlock base,
                @NotNull Vector normal,
                @NotNull HashSet<SimpleBlock> prospectiveHives,
                double currentBranchTheta,
                int oriY,
                double displacementTheta,
                float length,
                float end,
                int depth,
                float currentWidth,
                float startingBranchIndex)
    {
        boolean spawnedNewBranch = false;
        SimpleBlock lastOperatedCentre = base;
        // Terminate on maxDepth.
        if (length > 0 && depth < maxDepth) {
            float initialWidth = currentWidth;
            // Number of rotated cylindrical disks is length

            FastNoise noiseGen = NoiseCacheHandler.getNoise(tw,
                    NoiseCacheHandler.NoiseCacheEntry.FRACTALTREES_BASE_NOISE,
                    world -> {
                        FastNoise n = new FastNoise((int) world.getSeed());
                        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                        n.SetFractalOctaves(5);
                        n.SetFrequency(0.05f);
                        return n;
                    }
            );

            // A vector representing the centre of the branch, including its length.
            Vector branchVect = normal.clone().multiply(length);

            // Represents the cooldown before a random branch can spawn during this process
            float randomBranchSpawnCooldownCurrent = 0;

            // This for loop places the branch.
            // I am the branchIndex, and steps is the maximum steps the branch will
            // take. Preferably, the radius of the branch will shrink as steps
            // increase.
            for (float i = 0; i < length - startingBranchIndex; i += 0.5f) {
                if ((i / length) > end) {
                    break;
                }


                float appliedWidth = currentWidth;
                float appliedNoisePriority = this.noisePriority;
                Vector appliedNormal = normal;
                // Base branches are thicker at the bottom for roots.
                Material temp = this.branchMaterial;
                if (depth == 0 && i < treeRootThreshold) {
                    appliedWidth *= (float) (treeRootMultiplier + ((1.0 - treeRootMultiplier) / treeRootThreshold) * i);
                    appliedNoisePriority = (float) (0.7 + ((this.noisePriority - 0.4) / treeRootThreshold) * i);
                    appliedNormal = new Vector(0, 1, 0);
                    this.branchMaterial = this.rootMaterial;
                }

                // this.setBranchMaterial(BlockUtils.WOOLS[(int) length]);

                lastOperatedCentre = generateRotatedCircle(random,
                        oriY,
                        lastOperatedCentre.getPopData(),
                        branchVect.clone().multiply(i / length).add(base.toVector()),
                        appliedNormal,
                        prospectiveHives,
                        appliedNoisePriority,
                        appliedWidth,
                        noiseGen,
                        i
                );

                this.branchMaterial = temp;

                currentWidth = getBranchWidth.apply(initialWidth, (i / length));
                // TerraformGeneratorPlugin.logger.info("CWidth: " + currentWidth);

                // Tick cooldown down by the loop step
                randomBranchSpawnCooldownCurrent -= 0.5F;
                // Spawn more branches. These branches are random in nature.
                if ((i / length) > minBranchSpawnLength
                    && GenUtils.chance(random, (int) (100 * branchSpawnChance), 100)
                    && randomBranchSpawnCooldownCurrent <= 0)
                {
                    randomBranchSpawnCooldownCurrent = randomBranchSpawnCooldown;
                    spawnedNewBranch = true;
                    // If the cluster count is more than 0, you must reshuffle displacement theta
                    double effectiveDisplacementTheta = displacementTheta;
                    if (randomBranchClusterCount > 0) {
                        displacementTheta = GenUtils.randDouble(random, 0, displacementThetaDelta);
                    }

                    // Place the randomised branches.
                    for (int y = 0; y < randomBranchClusterCount; y++) {
                        currentBranchTheta++;
                        branch(tw,
                                random,
                                lastOperatedCentre,
                                calculateNextProjection(random,
                                        normal,
                                        getNextTheta(currentBranchTheta,
                                                randomBranchSegmentCount,
                                                effectiveDisplacementTheta
                                        )
                                ),
                                prospectiveHives,
                                currentBranchTheta,
                                oriY,
                                displacementTheta,
                                branchDecrement.apply(length, (float) (lastOperatedCentre.getY() - oriY)),
                                1f,
                                depth + 1,
                                currentWidth,
                                0
                        );
                    }
                }
            }

            // This is the base branch. Check if you want to spawn crowning branches
            if (depth == 0 && crownBranches > 0) {
                double thetaDelta = (2 * Math.PI) / crownBranches;
                for (int i = 0; i < crownBranches; i++) {
                    // branchMaterial = BlockUtils.pickWool();
                    spawnedNewBranch = true;
                    branch(tw,
                            random,
                            lastOperatedCentre,
                            calculateNextProjection(random, normal, thetaDelta * i),
                            prospectiveHives,
                            currentBranchTheta,
                            oriY,
                            displacementTheta,
                            branchDecrement.apply(length, (float) (lastOperatedCentre.getY() - oriY)),
                            1f,
                            depth + 1,
                            currentWidth,
                            0
                    );
                }
            }
        }

        if (length <= 0 || !spawnedNewBranch || depth >= leafSpawnDepth) {
            fractalLeaves.placeLeaves(tw, oriY, maxHeight, lastOperatedCentre);
            // lastOperatedCentre.setType(Material.RED_WOOL);
        }
    }

    /**
     * @param numSegments refers to the number of branches coming out of the
     *                    same branch
     * @return a theta with the next segment
     */
    double getNextTheta(double currentBranchTheta, int numSegments, double displacementTheta)
    {
        double thetaDelta = (2 * Math.PI) / ((double) numSegments);
        return (displacementTheta + currentBranchTheta * thetaDelta);
    }

    /**
     * Adds a random theta based on the tree instance and applies a
     * rotation matrix to get a new normal vector that is rotated
     * slightly away from the current normal.
     * <br><br>
     * normal will be cloned here, no need to clone before input.
     * <br><br>
     * This method works by creating a cloned normal, rotating it to be
     * perpendicular to the original, then rotating this
     * clone by theta. This rotated vector is then added
     * to the normal to slightly displace it.
     *
     * @param normal refers to the current normal
     * @param theta  refers to the yaw (represented by 0 to 2pi)
     * @return the new normal vector
     */
    @NotNull
    Vector calculateNextProjection(@NotNull Random random, @NotNull Vector normal, double theta) {
        // One perpendicular vector will be rotated about the normal to
        // generate a rotating projection.
        //
        Vector A = normal.clone();

        // Rotate A to be perpendicular to the normal
        A.setX(rotationMatrixX[0][0] * normal.getX()
               + rotationMatrixX[0][1] * normal.getY()
               + rotationMatrixX[0][2] * normal.getZ());
        A.setY(rotationMatrixX[1][0] * normal.getX()
               + rotationMatrixX[1][1] * normal.getY()
               + rotationMatrixX[1][2] * normal.getZ());
        A.setZ(rotationMatrixX[2][0] * normal.getX()
               + rotationMatrixX[2][1] * normal.getY()
               + rotationMatrixX[2][2] * normal.getZ());

        double x, y, z;
        double u, v, w;
        x = A.getX();
        y = A.getY();
        z = A.getZ();
        u = normal.getX();
        v = normal.getY();
        w = normal.getZ();
        double AdotNormal = A.dot(normal);
        double xPrime = u * AdotNormal * (1d - Math.cos(theta)) + x * Math.cos(theta) + (-w * y + v * z) * Math.sin(
                theta);
        double yPrime = v * AdotNormal * (1d - Math.cos(theta))
                        + y * Math.cos(theta)
                        + (w * x - u * z) * Math.sin(theta);
        double zPrime = w * AdotNormal * (1d - Math.cos(theta)) + z * Math.cos(theta) + (-v * x + u * y) * Math.sin(
                theta);
        A = new Vector(xPrime, yPrime, zPrime);

        // Randomly rotate the normal by adding a minimum magnitude scalar to both A and B
        // with a random direction (+ or -)
        return normal.clone()
                     .add(A.multiply(GenUtils.randDouble(random,
                             minBranchHorizontalComponent,
                             maxBranchHorizontalComponent
                     )))
                     .normalize();
    }

    /**
     * Generate a noise-fuzzed circle rotated to the normal Vec3D. This will be
     * achieved by iterating values of theta and fuzzed values of radius calculated
     * with theta and heightIndex with 2D noise .
     * <br><br>
     * Theta will be defined as 0 to slightly less than 2 pi
     * <br><br>
     * Theta's step to 2pi must be larger if the circle is larger. There may be
     * an equation for this, but for now I will stick with a small step.
     *
     * @param normal      to the circle.
     * @param radius      base radius of the circle. Actual radius may be larger or smaller
     * @param heightIndex Noise will be sampled based on theta relative to this height index.
     *                    The height index allows the branch to vary continuously across
     *                    its length.
     * @return the centre of the evaluated circle.
     */
    @NotNull
    SimpleBlock generateRotatedCircle(@NotNull Random random,
                                      int oriY,
                                      @NotNull PopulatorDataAbstract data,
                                      @NotNull Vector centre,
                                      @NotNull Vector normal,
                                      @NotNull HashSet<SimpleBlock> prospectiveHives,
                                      float noisePriority,
                                      float radius,
                                      @NotNull FastNoise noiseGen,
                                      float heightIndex)
    {
        if (radius <= 0.5f) {
            data.rsetType(centre, BlockUtils.replacableByTrees, branchMaterial);
            return new SimpleBlock(data, centre);
        }

        // Material mat = BlockUtils.WOOLS[Math.round(heightIndex) % BlockUtils.WOOLS.length];

        // An orthogonal basis will be used as an alternative
        // coordinate system, (A, B) in terms of x,y,z.
        //
        Vector A = normal.clone();
        Vector B = normal.clone();

        A.setX(rotationMatrixX[0][0] * normal.getX()
               + rotationMatrixX[0][1] * normal.getY()
               + rotationMatrixX[0][2] * normal.getZ());
        A.setY(rotationMatrixX[1][0] * normal.getX()
               + rotationMatrixX[1][1] * normal.getY()
               + rotationMatrixX[1][2] * normal.getZ());
        A.setZ(rotationMatrixX[2][0] * normal.getX()
               + rotationMatrixX[2][1] * normal.getY()
               + rotationMatrixX[2][2] * normal.getZ());

        B.setX(rotationMatrixZ[0][0] * normal.getX()
               + rotationMatrixZ[0][1] * normal.getY()
               + rotationMatrixZ[0][2] * normal.getZ());
        B.setY(rotationMatrixZ[1][0] * normal.getX()
               + rotationMatrixZ[1][1] * normal.getY()
               + rotationMatrixZ[1][2] * normal.getZ());
        B.setZ(rotationMatrixZ[2][0] * normal.getX()
               + rotationMatrixZ[2][1] * normal.getY()
               + rotationMatrixZ[2][2] * normal.getZ());

        boolean didNotGenerate = true;
        // Now that you have an orthogonal basis, you can now
        // iterate in a 2D square based on multiples of A and B
        // Iterate from (-rA,-rB) to (rA, rB)
        double maxPossibleRadius = (1 + noisePriority * 2) * radius;
        for (double rA = -maxPossibleRadius; rA <= maxPossibleRadius; rA++) {
            for (double rB = -maxPossibleRadius; rB <= maxPossibleRadius; rB++) {
                double distFromCentre = Math.sqrt(Math.pow(rA, 2) + Math.pow(rB, 2));
                // Theta will be used to calculate variations in radius
                // for fuzzing.
                double theta = Math.atan2(rB, rA);
                // atan2 returns -pi to pi. Convert this to 0 to 2pi.
                if (theta < 0) {
                    theta = 2 * Math.PI - theta;
                }

                double newRadius;

                if (noisePriority > 0) {
                    newRadius = radius + (noisePriority * radius) * noiseGen.GetNoise(Objects.hash(centre.getX(),
                            centre.getZ()
                    ), (float) theta, heightIndex);
                }
                else {
                    newRadius = radius;
                }

                if (distFromCentre <= newRadius) {
                    // Re-convert coordinates from (A,B) to (x,y,z)
                    // As a recap, (1,1,A), (B,1,1)
                    data.rsetType(centre.clone().add(A.clone().multiply(rA)).add(B.clone().multiply(rB)),
                            BlockUtils.replacableByTrees,
                            branchMaterial
                    );
                    didNotGenerate = false;

                    // Add possible beehives
                    if (spawnBees && centre.getY() > oriY + originalTrunkLength / 2f && GenUtils.chance(random,
                            1,
                            200
                    ))
                    {
                        prospectiveHives.add(new SimpleBlock(data,
                                centre.clone()
                                      .add(A.clone().multiply(rA))
                                      .add(B.clone().multiply(rB))
                                      .add(new Vector(0, -1, 0))
                        ));
                    }
                }
            }
        }

        if (didNotGenerate) {
            data.rsetType(centre, BlockUtils.replacableByTrees, branchMaterial);
        }

        return new SimpleBlock(data, centre);
    }

    @NotNull
    NewFractalTreeBuilder setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setOriginalTrunkLength(int originalTrunkLength) {
        this.originalTrunkLength = originalTrunkLength;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setFirstEnd(float firstEnd) {
        this.firstEnd = firstEnd;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setCrownBranches(int crownBranches) {
        this.crownBranches = crownBranches;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setInitialBranchRadius(float initialBranchRadius) {
        this.initialBranchRadius = initialBranchRadius;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setBranchSpawnChance(double branchSpawnChance) {
        this.branchSpawnChance = branchSpawnChance;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setMinBranchSpawnLength(float minBranchSpawnLength) {
        this.minBranchSpawnLength = minBranchSpawnLength;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setTreeRootThreshold(int treeRootThreshold) {
        this.treeRootThreshold = treeRootThreshold;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setRandomBranchClusterCount(int randomBranchClusterCount)
    {
        this.randomBranchClusterCount = randomBranchClusterCount;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setRandomBranchSegmentCount(int randomBranchSegmentCount) {
        this.randomBranchSegmentCount = randomBranchSegmentCount;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setMaxInitialNormalDelta(double maxInitialNormalDelta) {
        this.maxInitialNormalDelta = maxInitialNormalDelta;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setRandomBranchSpawnCooldown(float randomBranchSpawnCooldown)
    {
        this.randomBranchSpawnCooldown = randomBranchSpawnCooldown;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setTreeRootMultiplier(float treeRootMultiplier)
    {
        this.treeRootMultiplier = treeRootMultiplier;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setLeafSpawnDepth(int leafSpawnDepth)
    {
        this.leafSpawnDepth = leafSpawnDepth;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setMinBranchHorizontalComponent(double minBranchHorizontalComponent) {
        this.minBranchHorizontalComponent = minBranchHorizontalComponent;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setMaxBranchHorizontalComponent(double maxBranchHorizontalComponent) {
        this.maxBranchHorizontalComponent = maxBranchHorizontalComponent;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setBranchDecrement(BiFunction<Float, Float, Float> branchDecrement) {
        this.branchDecrement = branchDecrement;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setGetBranchWidth(BiFunction<Float, Float, Float> getBranchWidth) {
        this.getBranchWidth = getBranchWidth;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setPrePlacement(BiConsumer<Random, SimpleBlock> func){
        this.prePlacement = func;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setSpawnBees(boolean spawnBees) {
        this.spawnBees = spawnBees;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setLengthVariance(float lengthVariance)
    {
        this.lengthVariance = lengthVariance;
        return this;
    }

    public @NotNull NewFractalTreeBuilder setBranchMaterial(Material branchMaterial) {
        this.branchMaterial = branchMaterial;
        return this;
    }

    public @NotNull NewFractalTreeBuilder setRootMaterial(Material rootMaterial) {
        this.rootMaterial = rootMaterial;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setNoisePriority(float noisePriority)
    {
        this.noisePriority = noisePriority;
        return this;
    }

    @NotNull
    NewFractalTreeBuilder setMinInitialNormalDelta(double minInitialNormalDelta) {
        this.minInitialNormalDelta = minInitialNormalDelta;
        return this;
    }

    public FractalLeaves getFractalLeaves() {
        return fractalLeaves;
    }

    @NotNull
    NewFractalTreeBuilder setFractalLeaves(FractalLeaves fractalLeaves) {
        this.fractalLeaves = fractalLeaves;
        return this;
    }

    @Override
    protected @NotNull Object clone() throws CloneNotSupportedException {
        NewFractalTreeBuilder cl = (NewFractalTreeBuilder) super.clone();
        cl.setFractalLeaves(this.fractalLeaves.clone());
        return cl;
    }
}
