package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.version.BeeHiveSpawner;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class isn't designed to run in a multithreaded environment
 */
public class NewFractalTreeBuilder {

    //Supposedly final fields not meant for mutation during build
    private int maxDepth = 3; //Last branch is depth 1
    private int originalTrunkLength = 20; //Starting length of the tree trunk
    private float lengthVariance = 4;
    private float firstEnd = 0.8f; //First branch will end before it hits full length
    private int crownBranches = 3; //Number of branches to spawn from the top of the first branch
    private float initialBranchRadius = 3f;
    private double branchSpawnChance = 0.08f; //Chance for a new branch to spawn while stepping a branch
    private float minBranchSpawnLength = 0.4f; //New branches only spawn at this branch ratio.
    private int randomBranchSegmentCount = 3;

    //Pitch rotates up or down, yaw rotates left/right
    private Vector initialNormal = new Vector(0,1,0);//.normalize();
    private double maxInitialNormalDelta = 0.3;
    private double minBranchHorizontalComponent = 0.5;
    private double maxBranchHorizontalComponent = 1.3;
    private int treeRootThreshold = 2;
    private float noisePriority = 0.1f;
    private FractalLeaves fractalLeaves;

    /**
     * This function determines how branches will decrease in length
     * each recursion.
     */
    private BiFunction<Float, Float, Float> branchDecrement = (currentBranchLength, totalTreeHeight)
            -> currentBranchLength * 0.7f;

    /**
     * This function determines how branch width will decrease
     * each recursion.
     */
    private BiFunction<Float, Float, Float> getBranchWidth =
            (initialBranchWidth, branchRatio)
            -> initialBranchWidth*(1.0f-branchRatio/2.0f);
    /**
     * A chance representing branch bending rates. 0 for no bends.
     * When a branch bends, the branch() method will essentially break its
     * iteration and call itself again with a higher base,
     * but with a slightly different projection
     */
    private float bendChance = 0f;
    private float bendMaxAngle = 0f; //in radians. Max bend angle
    int maxHeight = 9999;

    private Material branchMaterial = Material.OAK_LOG;
    private boolean spawnBees = false;

    //[Mutable fields]=============================================
    Random random;
    TerraformWorld tw;
    int oriY;
    private final HashSet<SimpleBlock> prospectiveHives = new HashSet<>();
    private double currentBranchTheta = 0;

    public void build(TerraformWorld tw, SimpleBlock base)
    {
        //Clear and set mutable structures
        this.prospectiveHives.clear();
        this.random = tw.getHashedRand(base.getX(), base.getY(), base.getZ());
        this.tw = tw;
        this.oriY = base.getY();
        this.currentBranchTheta = GenUtils.randInt(random, 0, randomBranchSegmentCount);
        fractalLeaves.purgeOccupiedLeavesCache();

        //Spawn the actual tree
        branch(base, initialNormal.clone()
                        .add(
                        new Vector(
                            GenUtils.randDouble(random, -maxInitialNormalDelta, maxInitialNormalDelta),
                         0,
                            GenUtils.randDouble(random, -maxInitialNormalDelta, maxInitialNormalDelta)
                        ))
                        .normalize(),
                originalTrunkLength + (float)GenUtils.randDouble(random, -lengthVariance, lengthVariance),firstEnd,
                0, this.initialBranchRadius);

        //Process prospectiveHives
        if(spawnBees)
            for(SimpleBlock b:prospectiveHives)
            {
                //TerraformGeneratorPlugin.logger.info("Testing " + b);
                if(b.isSolid()) continue; //occupied block
                //TerraformGeneratorPlugin.logger.info("Success: " + b);
                BeeHiveSpawner.spawnFullBeeNest(b);
                break; //just one.
            }
    }



    /**
     *
     * @param base the start of the branch's base.
     * @param end is the percentage from 0.0 to 1.0 for where the branch is considered done
     * @param depth of the current recursion. Starts from 0 and stops at maxDepth
     * @param currentWidth width of the current recursion
     */
    public void branch(SimpleBlock base, Vector normal, float length, float end, int depth, float currentWidth)
    {
        boolean spawnedNewBranch = false;
        SimpleBlock lastOperatedCentre = base;
        //Terminate on maxDepth.
        if(length > 0 && depth < maxDepth)
        {
            float initialWidth = currentWidth;
            //Number of rotated cylindrical disks is length

            FastNoise noiseGen = NoiseCacheHandler.getNoise(
                    tw,
                    NoiseCacheHandler.NoiseCacheEntry.FRACTALTREES_BASE_NOISE,
                    world -> {
                        FastNoise n = new FastNoise((int) world.getSeed());
                        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                        n.SetFractalOctaves(5);
                        n.SetFrequency(0.05f);
                        return n;
                    });

            //A vector representing the centre of the branch, including its length.
            Vector branchVect = normal.clone().multiply(length);
            float subBranchLength = branchDecrement.apply(length, (float) (base.getY() - oriY));

            boolean placedPatcherLeaves = false;
            //This for loop places the branch.
            //i is the branchIndex, and steps is the maximum steps the branch will
            //take. Preferably, the radius of the branch will shrink as steps
            //increase.
            for(float i = 0; i < length; i+=0.5f)
            {
                if((i/length) > end) break;
                float appliedWidth = currentWidth;
                float appliedNoisePriority = this.noisePriority;
                Vector appliedNormal = normal;
                //Base branches are thicker at the bottom for roots
                if(depth == 0 && i % 1 == 0 && i < treeRootThreshold) {
                    appliedWidth *= (-0.5*(i/length))+1.5;
                    appliedNoisePriority = 0.8f;
                    appliedNormal = new Vector(0,1,0);
                }
                lastOperatedCentre = generateRotatedCircle(
                        lastOperatedCentre.getPopData(),
                        branchVect.clone().multiply(i/length).add(base.toVector()),
                        appliedNormal, appliedNoisePriority, appliedWidth, noiseGen, i);

                if(!placedPatcherLeaves && lastOperatedCentre.getY() < base.getY()) {
                    placedPatcherLeaves = true;
                    fractalLeaves.placeLeaves(tw, oriY, maxHeight, base);
                }
                
                currentWidth = getBranchWidth.apply(initialWidth, (i/length));
                //TerraformGeneratorPlugin.logger.info("CWidth: " + currentWidth);
                //Spawn more branches
                if((i/length) > minBranchSpawnLength
                        && GenUtils.chance(random, (int) (100*branchSpawnChance), 100))
                {
                    spawnedNewBranch = true;
                    branch(lastOperatedCentre,
                            calculateNextProjection(normal, getNextTheta(randomBranchSegmentCount)),
                            subBranchLength,
                            1.0f,
                            depth + 1,
                            currentWidth);
                }
            }

            //This is the base branch. Check if you want to spawn crowning branches
            if(depth == 0 && crownBranches > 0) {
                double thetaDelta = (2*Math.PI)/crownBranches;
                for(int i = 0; i < crownBranches; i++) {
                    //branchMaterial = BlockUtils.pickWool();
                    spawnedNewBranch = true;
                    branch(lastOperatedCentre,
                            calculateNextProjection(normal, thetaDelta*i),
                            subBranchLength,
                            1.0f,
                            depth + 1,
                            currentWidth);
                }
            }
        }

        if(length <= 0 || !spawnedNewBranch || depth >= maxDepth) {
            fractalLeaves.placeLeaves(tw, oriY, maxHeight, lastOperatedCentre);
            //lastOperatedCentre.setType(Material.RED_WOOL);
        }

    }

    /**
     * @param numSegments refers to the number of branches coming out of the
     *                    same branch
     * @return a theta with the next segment
     */
    private double getNextTheta(int numSegments)
    {
        double thetaDelta = (2*Math.PI)/((double)numSegments);
        currentBranchTheta++;
        return currentBranchTheta * thetaDelta;
    }

    /**
     * Adds a random theta based on the tree instance and applies a
     * rotation matrix to get a new normal vector that is rotated
     * slightly away from the current normal.
     * <br><br>
     * normal will be cloned here, no need to clone before input.
     *
     * @param normal refers to the current normal
     * @param theta refers to the yaw (represented by 0 to 2pi)
     * @return the new normal vector
     */
    private Vector calculateNextProjection(Vector normal, double theta)
    {
        //One perpendicular vector will be rotated about the normal to
        //generate a rotating projection.
        //
        Vector A = normal.clone();

        //Rotate A to be perpendicular to the normal
        A.setX(rotationMatrixX[0][0]*normal.getX()+rotationMatrixX[0][1]*normal.getY()+rotationMatrixX[0][2]*normal.getZ());
        A.setY(rotationMatrixX[1][0]*normal.getX()+rotationMatrixX[1][1]*normal.getY()+rotationMatrixX[1][2]*normal.getZ());
        A.setZ(rotationMatrixX[2][0]*normal.getX()+rotationMatrixX[2][1]*normal.getY()+rotationMatrixX[2][2]*normal.getZ());

        //Rotate A about normal by this random theta
        final double[][] rotationMatrixNormal = new double[][] {
                new double[]{Math.cos(theta), 0, Math.sin(theta)},
                new double[]{0, 1, 0},
                new double[]{-Math.sin(theta), 0, Math.cos(theta)}
        };

        A.setX(rotationMatrixNormal[0][0]*A.getX()+rotationMatrixNormal[0][1]*A.getY()+rotationMatrixNormal[0][2]*A.getZ());
        A.setY(rotationMatrixNormal[1][0]*A.getX()+rotationMatrixNormal[1][1]*A.getY()+rotationMatrixNormal[1][2]*A.getZ());
        A.setZ(rotationMatrixNormal[2][0]*A.getX()+rotationMatrixNormal[2][1]*A.getY()+rotationMatrixNormal[2][2]*A.getZ());

        //Randomly rotate the normal by adding a minimum magnitude scalar to both A and B
        //with a random direction (+ or -)
        return normal.clone()
                .add(A.multiply(
                            GenUtils.randDouble(
                                    random,
                                    minBranchHorizontalComponent,
                                    maxBranchHorizontalComponent)
                            )
                    )
                .normalize();
    }

    private static final int[][] rotationMatrixX = new int[][] {
        new int[]{1, 0, 0},
        new int[]{0, 0, -1},
        new int[]{0, 1, 0}
    };

    private static final int[][] rotationMatrixZ = new int[][] {
            new int[]{0, -1, 0},
            new int[]{1, 0, 0},
            new int[]{0, 0, 1}
    };

    /**
     * Generate a noise-fuzzed circle rotated to the normal Vec3D. This will be
     * achieved by iterating values of theta and fuzzed values of radius calculated
     * with theta and heightIndex with 2D noise .
     * <br><br>
     * Theta will be defined as 0 to slightly less than 2 pi
     * <br><br>
     * Theta's step to 2pi must be larger if the circle is larger. There may be
     * an equation for this, but for now i will stick with a small step.
     *
     * @param normal to the circle.
     * @param radius base radius of the circle. Actual radius may be larger or smaller
     * @param heightIndex Noise will be sampled based on theta relative to this height index.
     *                    The height index allows the branch to vary continuously across
     *                    its length.
     * @return the centre of the evaluated circle.
     */
    private SimpleBlock generateRotatedCircle(PopulatorDataAbstract data, Vector centre, Vector normal, float noisePriority, float radius, FastNoise noiseGen, float heightIndex)
    {
        if(radius <= 0.5f)
        {
            data.setType(centre, branchMaterial);
            return new SimpleBlock(data, centre);
        }

        //Material mat = BlockUtils.WOOLS[Math.round(heightIndex) % BlockUtils.WOOLS.length];

        //An orthogonal basis will be used as an alternative
        //coordinate system, (A, B) in terms of x,y,z.
        //
        Vector A = normal.clone();
        Vector B = normal.clone();

        A.setX(rotationMatrixX[0][0]*normal.getX()+rotationMatrixX[0][1]*normal.getY()+rotationMatrixX[0][2]*normal.getZ());
        A.setY(rotationMatrixX[1][0]*normal.getX()+rotationMatrixX[1][1]*normal.getY()+rotationMatrixX[1][2]*normal.getZ());
        A.setZ(rotationMatrixX[2][0]*normal.getX()+rotationMatrixX[2][1]*normal.getY()+rotationMatrixX[2][2]*normal.getZ());

        B.setX(rotationMatrixZ[0][0]*normal.getX()+rotationMatrixZ[0][1]*normal.getY()+rotationMatrixZ[0][2]*normal.getZ());
        B.setY(rotationMatrixZ[1][0]*normal.getX()+rotationMatrixZ[1][1]*normal.getY()+rotationMatrixZ[1][2]*normal.getZ());
        B.setZ(rotationMatrixZ[2][0]*normal.getX()+rotationMatrixZ[2][1]*normal.getY()+rotationMatrixZ[2][2]*normal.getZ());

        boolean didNotGenerate = true;
        //Now that you have an orthogonal basis, you can now
        //iterate in a 2D square based on multiples of A and B
        //Iterate from (-rA,-rB) to (rA, rB)
        double maxPossibleRadius = (1+noisePriority*2)*radius;
        for(double rA = -maxPossibleRadius; rA <= maxPossibleRadius; rA++)
            for(double rB = -maxPossibleRadius; rB <= maxPossibleRadius; rB++)
            {
                double distFromCentre = Math.sqrt(Math.pow(rA,2)+Math.pow(rB,2));
                //Theta will be used to calculate variations in radius
                //for fuzzing.
                double theta = Math.atan2(rB,rA);
                //atan2 returns -pi to pi. Convert this to 0 to 2pi.
                if(theta < 0) theta = 2*Math.PI - theta;

                double newRadius;

                if(noisePriority > 0)
                    newRadius = radius + (noisePriority*radius)*noiseGen.GetNoise(
                        Objects.hash(centre.getX(),centre.getZ()),(float) theta, heightIndex);
                else
                    newRadius = radius;

                if(distFromCentre <= newRadius)
                {
                    //Re-convert coordinates from (A,B) to (x,y,z)
                    //As a recap, (1,1,A), (B,1,1)
                    data.setType(centre.clone()
                                    .add(A.clone().multiply(rA))
                                    .add(B.clone().multiply(rB)),
                            branchMaterial);
                    didNotGenerate = false;

                    //Add possible beehives
                    if(spawnBees
                            && centre.getY() > oriY + originalTrunkLength/2f
                            && GenUtils.chance(random,1,200))
                        prospectiveHives.add(new SimpleBlock(data,
                                centre.clone()
                                        .add(A.clone().multiply(rA))
                                        .add(B.clone().multiply(rB))
                                        .add(new Vector(0,-1,0))));
                }
            }

        if(didNotGenerate)
            data.setType(centre, branchMaterial);

        return new SimpleBlock(data, centre);
    }

    public NewFractalTreeBuilder setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public NewFractalTreeBuilder setOriginalTrunkLength(int originalTrunkLength) {
        this.originalTrunkLength = originalTrunkLength;
        return this;
    }

    public NewFractalTreeBuilder setFirstEnd(float firstEnd) {
        this.firstEnd = firstEnd;
        return this;
    }

    public NewFractalTreeBuilder setCrownBranches(int crownBranches) {
        this.crownBranches = crownBranches;
        return this;
    }

    public NewFractalTreeBuilder setInitialBranchRadius(float initialBranchRadius) {
        this.initialBranchRadius = initialBranchRadius;
        return this;
    }

    public NewFractalTreeBuilder setBranchSpawnChance(double branchSpawnChance) {
        this.branchSpawnChance = branchSpawnChance;
        return this;
    }

    public NewFractalTreeBuilder setMinBranchSpawnLength(float minBranchSpawnLength) {
        this.minBranchSpawnLength = minBranchSpawnLength;
        return this;
    }
    public NewFractalTreeBuilder setTreeRootThreshold(int treeRootThreshold) {
        this.treeRootThreshold = treeRootThreshold;
        return this;
    }
    public NewFractalTreeBuilder setRandomBranchSegmentCount(int randomBranchSegmentCount) {
        this.randomBranchSegmentCount = randomBranchSegmentCount;
        return this;
    }

    public NewFractalTreeBuilder setInitialNormal(Vector initialNormal) {
        this.initialNormal = initialNormal;
        return this;
    }

    public NewFractalTreeBuilder setMaxInitialNormalDelta(double maxInitialNormalDelta) {
        this.maxInitialNormalDelta = maxInitialNormalDelta;
        return this;
    }

    public NewFractalTreeBuilder setMinBranchHorizontalComponent(double minBranchHorizontalComponent) {
        this.minBranchHorizontalComponent = minBranchHorizontalComponent;
        return this;
    }

    public NewFractalTreeBuilder setMaxBranchHorizontalComponent(double maxBranchHorizontalComponent) {
        this.maxBranchHorizontalComponent = maxBranchHorizontalComponent;
        return this;
    }

    public NewFractalTreeBuilder setFractalLeaves(FractalLeaves fractalLeaves) {
        this.fractalLeaves = fractalLeaves;
        return this;
    }

    public NewFractalTreeBuilder setBranchDecrement(BiFunction<Float, Float, Float> branchDecrement) {
        this.branchDecrement = branchDecrement;
        return this;
    }

    public NewFractalTreeBuilder setGetBranchWidth(BiFunction<Float, Float, Float> getBranchWidth) {
        this.getBranchWidth = getBranchWidth;
        return this;
    }

    public NewFractalTreeBuilder setSpawnBees(boolean spawnBees) {
        this.spawnBees = spawnBees;
        return this;
    }

    public NewFractalTreeBuilder setLengthVariance(float lengthVariance)
    {
        this.lengthVariance = lengthVariance;
        return this;
    }

    public NewFractalTreeBuilder setBendChance(float bendChance) {
        this.bendChance = bendChance;
        return this;
    }

    public NewFractalTreeBuilder setBendMaxAngle(float bendMaxAngle) {
        this.bendMaxAngle = bendMaxAngle;
        return this;
    }

    public NewFractalTreeBuilder setBranchMaterial(Material branchMaterial) {
        this.branchMaterial = branchMaterial;
        return this;
    }

    public NewFractalTreeBuilder setNoisePriority(float noisePriority)
    {
        this.noisePriority = noisePriority;
        return this;
    }

    public NewFractalTreeBuilder setMinInitialNormalDelta(float minInitialNormalDelta) {
        this.maxInitialNormalDelta = minInitialNormalDelta;
        return this;
    }
}
