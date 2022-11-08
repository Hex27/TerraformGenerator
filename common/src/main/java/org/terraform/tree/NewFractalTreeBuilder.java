package org.terraform.tree;

import net.minecraft.world.phys.Vec3D;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.Vector3f;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.HashSet;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NewFractalTreeBuilder {

    private int maxDepth = 3; //Last branch is depth 1
    private int originalTrunkLength = 10; //Starting length of the tree trunk
    private float firstEnd = 0.8f; //First branch will end before it hits full length
    private int crownBranches = 0; //Number of branches to spawn from the top of the first branch
    private float initialBranchRadius = 3f;
    private double branchSpawnChance = 0.1f; //Chance for a new branch to spawn while stepping a branch
    private float minBranchSpawnLength = 0.6f; //New branches only spawn at this branch ratio.
    //Pitch rotates up or down, yaw rotates left/right
    private double initialPitch = -Math.PI/2; //Vertically upwards
    private double initialYaw = 0; //Rotating a straight branch as no effect, only change with pitch

    private double branchMinPitchDelta = 0.8 * Math.PI / 6;
    private double branchMaxPitchDelta = 1.2 * Math.PI / 6;
    /**
     * This function determines how branches will decrease in length
     * each recursion.
     */
    private Function<Float, Float> branchDecrement = (currentBranchLength)
            -> currentBranchLength/2;

    /**
     * This function determines how branch width will decrease
     * each recursion.
     */
    private BiFunction<Float, Float, Float> getBranchWidth = (currentBranchWidth, branchRatio)
            -> currentBranchWidth-branchRatio;
    /**
     * A chance representing branch bending rates. 0 for no bends.
     * When a branch bends, the branch() method will essentially break its
     * iteration and call itself again with a higher base,
     * but with a slightly different projection
     */
    private float bendChance = 0f;
    private float bendMaxAngle = 0f; //in radians. Max bend angle

    private Material branchMaterial = Material.OAK_LOG;

    Random random;
    TerraformWorld tw;
    public void build(TerraformWorld tw, SimpleBlock base)
    {
        this.random = tw.getHashedRand(base.getX(), base.getY(), base.getZ());
        this.tw = tw;
        branch(base, base.getRelative(0,20,0), 1.0f, 0, 3);
    }

    /**
     *
     * @param base
     * @param projection
     * @param end is the percentage from 0.0 to 1.0 for
     * @param depth
     * @param currentWidth
     */
    public void branch(SimpleBlock base, SimpleBlock projection, float end, int depth, float currentWidth)
    {
        int steps = (int) Math.round(base.distance(projection));
        int unitX = Math.round((projection.getX() - base.getX())/((float)steps));
        int unitY = Math.round((projection.getY() - base.getY())/((float)steps));
        int unitZ = Math.round((projection.getZ() - base.getZ())/((float)steps));
        Vector normal = projection.toVector().subtract(base.toVector()).normalize();

        FastNoise noiseGen = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheHandler.NoiseCacheEntry.FRACTALTREES_BASE_NOISE,
                world -> {
                    FastNoise n = new FastNoise((int) world.getSeed());
                    n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    n.SetFractalOctaves(5);
                    return n;
                });

        SimpleBlock lastOperatedCentre = base;
        //i is the branchIndex, and steps is the maximum steps the branch will
        //take. Preferably, the radius of the branch will shrink as steps
        //increase.
        for(int i = 0; i < steps; i++)
        {
            lastOperatedCentre = base.getRelative(unitX*i, unitY*i, unitZ*i);
            generateRotatedCircle(lastOperatedCentre, normal, currentWidth, noiseGen, i);
            currentWidth = getBranchWidth.apply(currentWidth, ((float) i)/steps);
        }

        //This is the base branch. Check if you want to spawn crowning branches
//        if(depth == 0)
//            for(int i = 0; i < crownBranches; i++)
//                branch(lastOperatedCentre, )
        //TODO: unimplemented
    }

    private SimpleBlock calculateProjection(SimpleBlock base, Vector newNormal, float currentBranchLength)
    {
        return null; //TODO: unimplemented
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
     *
     * Theta will be defined as 0 to slightly less than 2 pi
     *
     * Theta's step to 2pi must be larger if the circle is larger. There may be
     * an equation for this, but for now i will stick with a small step.
     *
     * @param normal to the circle.
     * @param radius base radius of the circle. Actual radius may be larger or smaller
     * @param heightIndex Noise will be sampled based on theta relative to this height index.
     *                    The height index allows the branch to vary continuously across
     *                    its length.
     */
    private void generateRotatedCircle(SimpleBlock centre, Vector normal, float radius, FastNoise noiseGen, int heightIndex)
    {
        //locus of points defined by a.normal <= nDotA, where x,y,z can
        //be calculated with cartesian xyz
        //Calculate the constant n.a
        double nDotA = normal.dot(centre.toVector());

        //An orthogonal basis will be used as a temporary alternative
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
        for(double rA = -radius; rA <= radius; rA++)
            for(double rB = -radius; rB <= radius; rB++)
            {
                double distFromCentre = Math.sqrt(Math.pow(rA,2)+Math.pow(rB,2));
                //Theta will be used to calculate variations in radius
                //for fuzzing.
                double theta = Math.atan2(rB,rA);
                //atan2 returns -pi to pi. Convert this to 0 to 2pi.
                if(theta < 0) theta = 2*Math.PI - theta;

                double newRadius = radius; //TODO: Calculate noise with theta and heightIndex and add to this
                if(distFromCentre <= newRadius)
                {
                    //Re-convert coordinates from (A,B) to (x,y,z)
                    //As a recap, (1,1,A), (B,1,1)
                    centre.getRelative(A.clone().multiply(rA))
                            .getRelative(B.clone().multiply(rB))
                            .setType(branchMaterial);
                    didNotGenerate = false;
                }
            }
        if(didNotGenerate) centre.setType(branchMaterial);
    }
}
