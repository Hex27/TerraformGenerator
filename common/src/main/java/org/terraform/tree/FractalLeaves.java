package org.terraform.tree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.V_1_21_4;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class FractalLeaves implements Cloneable {

    @NotNull
    final Random rand = new Random();
    /**
     * Used for caching leaves that are already processed.
     * Meant to be cleared when a new tree begins using the class.
     */
    private final HashSet<SimpleBlock> occupiedLeaves = new HashSet<>();
    public float radiusX = 4;
    public float radiusY = 2;
    public int numYSegments = 5;
    public float radiusZ = 4;
    public int offsetY = 0;
    public Material[] material = new Material[] {Material.OAK_LEAVES};
    int oriY;
    // public FractalTreeBuilder builder;
    int maxHeight;
    TerraformWorld tw;
    boolean semiSphereLeaves = false;
    float leafNoiseMultiplier = 0.7f;
    float leafNoiseFrequency = 0.09f;
    double hollowLeaves = 0.0;
    boolean coneLeaves = false;
    boolean snowy = false;
    float weepingLeavesChance = 0;
    int weepingLeavesLength = 0;
    float paleVinesChance = 0;
    int paleVinesLength = 0;
    boolean coralDecoration = false;
    boolean mangrovePropagules = false;
    int unitLeafSize = 0;
    float unitLeafChance = 0;

    public void purgeOccupiedLeavesCache() {
        occupiedLeaves.clear();
    }

    public void placeLeaves(@NotNull SimpleBlock centre)
    {
        placeLeaves(centre.getPopData().getTerraformWorld(), -999, 999, centre);
    }

    /**
     * This is a sphere algorithm
     *
     * @param centre the centre of the sphere.
     */
    public void placeLeaves(TerraformWorld tw, int oriY, int maxHeight, @NotNull SimpleBlock centre) {
        // Setup noise to be used in randomising the sphere

        this.tw = tw;
        this.oriY = oriY;
        this.maxHeight = maxHeight;

        FastNoise noiseGen = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.FRACTALTREES_LEAVES_NOISE, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetFractalOctaves(5);
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);

            return n;
        });
        noiseGen.SetFrequency(leafNoiseFrequency);

        // Don't place anything if radius is nothing
        if (radiusX <= 0 && radiusY <= 0 && radiusZ <= 0) {
            return;
        }

        // Radius 0.5 is 1 block
        if (radiusX <= 0.5 && radiusY <= 0.5 && radiusZ <= 0.5) {
            centre.setType(material);
            return;
        }

        // Initialise noise to be used in randomising the sphere
        float noiseMultiplier = leafNoiseMultiplier;
        int minRadiusY = -Math.round(radiusY);
        if (semiSphereLeaves) {
            minRadiusY = 0;
        }
        ArrayList<SimpleBlock> changed = new ArrayList<>();

        for (int y = minRadiusY; y <= radiusY; y++) {
            for (int x = -Math.round(radiusX); x <= radiusX; x++) {
                for (int z = -Math.round(radiusZ); z <= radiusZ; z++) {
                    Material material = this.material[rand.nextInt(this.material.length)];
                    SimpleBlock relativeBlock = centre.getRelative(x, y + offsetY, z);

                    if (relativeBlock.getY() - oriY > maxHeight) {
                        return;
                    }

                    if (relativeBlock.getY() - oriY == maxHeight) {
                        if (rand.nextBoolean()) // Fade off if too high
                        {
                            return;
                        }
                    }

                    float effectiveY = y;

                    if (coneLeaves) {
                        effectiveY += radiusY / 2; // Shift center area downwards
                        // Compress negative y
                        if (effectiveY < 0) {
                            effectiveY = effectiveY * 2f;
                        }

                        // Extend positive y and multiply it by a power to make it sharp
                        if (effectiveY > 0) {
                            effectiveY = effectiveY * (2f / 3f);
                            effectiveY = (float) Math.pow(effectiveY, 1.3);
                            if (effectiveY > radiusY) {
                                effectiveY = radiusY;
                            }
                        }
                        relativeBlock = relativeBlock.getRelative(0, (int) (radiusY / 2), 0);
                    }
                    // Continue as early as possible.
                    if (occupiedLeaves.contains(relativeBlock)) {
                        continue;
                    }

                    double equationResult = Math.pow(x, 2) / Math.pow(radiusX, 2) + Math.pow(effectiveY, 2) / Math.pow(
                            radiusY,
                            2
                    ) + Math.pow(z, 2) / Math.pow(radiusZ, 2);

                    if (equationResult <= 1 + noiseMultiplier * noiseGen.GetNoise(
                            relativeBlock.getX(),
                            relativeBlock.getY(),
                            relativeBlock.getZ()
                    ))
                    {
                        if (equationResult < hollowLeaves) {
                            continue;
                        }
                        // cache this block so that getType and setType aren't called for already processed blocks
                        occupiedLeaves.add(relativeBlock);

                        if (mangrovePropagules && Version.isAtLeast(19) && !BlockUtils.isWet(relativeBlock.getDown())) {
                            if (GenUtils.chance(1, 50)) {
                                relativeBlock.getDown()
                                             .rsetBlockData(
                                                     BlockUtils.replacableByTrees,
                                                     V_1_19.getHangingMangrovePropagule()
                                             );
                            }
                        }

                        // For fixing up corals later
                        if (Tag.CORALS.isTagged(material)) {
                            if (!changed.contains(relativeBlock)) {
                                changed.add(relativeBlock);
                            }
                        }

                        // Decorate with fans
                        if (coralDecoration) {
                            CoralGenerator.generateSingleCoral(
                                    relativeBlock.getPopData(),
                                    relativeBlock.getX(),
                                    relativeBlock.getY(),
                                    relativeBlock.getZ(),
                                    this.material[0].toString()
                            );
                        }

                        // Leaves do not replace solid blocks.
                        if (!relativeBlock.isSolid()) {
                            unitSet(relativeBlock, material);

                            // Handle placing random cubes in the leaves
                            if (this.unitLeafSize > 0 && this.unitLeafChance > 0) {
                                if (GenUtils.chance(this.rand, (int) (this.unitLeafChance * 100), 100)) {
                                    for (int scaleX = -unitLeafSize; scaleX < unitLeafSize; scaleX++) {
                                        for (int scaleZ = -unitLeafSize; scaleZ < unitLeafSize; scaleZ++) {
                                            for (int scaleY = -unitLeafSize; scaleY < unitLeafSize; scaleY++) {
                                                unitSet(relativeBlock.getRelative(scaleX, scaleY, scaleZ), material);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Set snow
                        if (snowy) {
                            if (!relativeBlock.getUp().isSolid()) {
                                relativeBlock.getUp().setType(Material.SNOW);
                            }
                        }

                        if (weepingLeavesChance > 0 && Math.random() < weepingLeavesChance) {
                            weepingLeaves(
                                    relativeBlock,
                                    Math.round(weepingLeavesChance * weepingLeavesLength),
                                    weepingLeavesLength
                            );
                        }
                        if (Version.isAtLeast(21.4)
                            && paleVinesChance > 0 && Math.random() < paleVinesChance) {
                            paleVines(
                                    relativeBlock,
                                    Math.round(paleVinesChance * paleVinesLength),
                                    paleVinesLength
                            );
                        }
                    }
                }
            }
        }

        // Ensures that corals don't die
        while (!changed.isEmpty()) {
            SimpleBlock sb = changed.remove(new Random().nextInt(changed.size()));
            if (!CoralGenerator.isSaturatedCoral(sb)) {
                // No floating coral fans
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (Tag.WALL_CORALS.isTagged(sb.getRelative(face).getType())) {
                        sb.getRelative(face).setType(Material.WATER);
                    }
                }

                // No levitating sea pickles & fans
                if (sb.getUp().getType() == Material.SEA_PICKLE || Tag.CORAL_PLANTS.isTagged(sb.getUp().getType())) {
                    sb.getUp().setType(Material.WATER);
                }
                sb.setType(Material.WATER);

            }
            else {
                sb.setType(material);
            }
        }
    }

    private void unitSet(@NotNull SimpleBlock relativeBlock, @NotNull Material material) {
        if (Tag.LEAVES.isTagged(material)) {
            Leaves leaf = (Leaves) Bukkit.createBlockData(material);
            leaf.setDistance(1);
            relativeBlock.rsetBlockData(BlockUtils.replacableByTrees, leaf);
        }
        else {
            relativeBlock.rsetType(BlockUtils.replacableByTrees, material);
        }
    }

    private void paleVines(@NotNull SimpleBlock base, int minDist, int maxDist) {
        int lowest = 0;
        for (int i = 1; i <= GenUtils.randInt(minDist, maxDist); i++) {
            if (BlockUtils.isAir(base.getRelative(0, -i, 0).getType())) {
                base.getRelative(0, -i, 0).rsetBlockData(BlockUtils.replacableByTrees, V_1_21_4.PALE_HANGING_MOSS);
                lowest++;
            }
            else break;
        }
        if(lowest > 0 )
            base.getDown(lowest).setBlockData(V_1_21_4.PALE_HANGING_MOSS_TIP);
    }
    private void weepingLeaves(@NotNull SimpleBlock base, int minDist, int maxDist) {
        Material material = this.material[rand.nextInt(this.material.length)];
        BlockData type = Bukkit.createBlockData(material);
        if (Tag.LEAVES.isTagged(material)) {
            Leaves leaf = (Leaves) type;
            leaf.setDistance(1);
        }

        for (int i = 1; i <= GenUtils.randInt(minDist, maxDist); i++) {
            if (BlockUtils.isAir(base.getRelative(0, -i, 0).getType())) {
                base.getRelative(0, -i, 0).rsetBlockData(BlockUtils.replacableByTrees, type);
            }
            else {
                break;
            }
        }
    }

    public @NotNull FractalLeaves setMaterial(Material... material) {
        this.material = material;
        return this;
    }

    public @NotNull FractalLeaves setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public @NotNull FractalLeaves setRadiusX(float radiusX) {
        this.radiusX = radiusX;
        return this;
    }

    public @NotNull FractalLeaves setRadiusY(float radiusY) {
        this.radiusY = radiusY;
        this.numYSegments = (int) Math.ceil(radiusY * 2 + 1);
        return this;
    }

    public @NotNull FractalLeaves setRadiusZ(float radiusZ) {
        this.radiusZ = radiusZ;
        return this;
    }

    public @NotNull FractalLeaves setRadius(float radius) {
        this.radiusX = radius;
        setRadiusY(radius);
        this.radiusZ = radius;
        return this;
    }

    public @NotNull FractalLeaves setRadius(float x, float y, float z) {
        this.radiusX = x;
        setRadiusY(y);
        this.radiusZ = z;
        return this;
    }

    public @NotNull FractalLeaves setHollowLeaves(double hollow) {
        this.hollowLeaves = hollow;
        return this;
    }

    public @NotNull FractalLeaves setConeLeaves(boolean coneLeaves) {
        this.coneLeaves = coneLeaves;
        return this;
    }

    public @NotNull FractalLeaves setLeafNoiseMultiplier(float multiplier) {
        this.leafNoiseMultiplier = multiplier;
        return this;
    }

    public @NotNull FractalLeaves setLeafNoiseFrequency(float freq) {
        this.leafNoiseFrequency = freq;
        return this;
    }

    public @NotNull FractalLeaves setSnowy(boolean snowy) {
        this.snowy = snowy;
        return this;
    }

    public @NotNull FractalLeaves setMangrovePropagules(boolean mangrovePropagules) {
        this.mangrovePropagules = mangrovePropagules;
        return this;
    }

    /**
     * Creates pale oak tree style tipped moss vines
     *
     * @param chance    chance of creating dangling leaves per block (0 - 1)
     * @param maxLength maximum length of dangling leaves
     */
    public @NotNull FractalLeaves setPaleMossVines(float chance, int maxLength) {
        this.paleVinesChance = chance;
        this.paleVinesLength = maxLength;
        return this;
    }

    /**
     * Creates dangling leaves without vines. Useful for
     * creating types of weeping trees.
     *
     * @param chance    chance of creating dangling leaves per block (0 - 1)
     * @param maxLength maximum length of dangling leaves
     */
    public @NotNull FractalLeaves setWeepingLeaves(float chance, int maxLength) {
        this.weepingLeavesChance = chance;
        this.weepingLeavesLength = maxLength;
        return this;
    }

    public @NotNull FractalLeaves setUnitLeafChance(float unitLeafChance)
    {
        this.unitLeafChance = unitLeafChance;
        return this;
    }

    public @NotNull FractalLeaves setUnitLeafSize(int unitSize)
    {
        this.unitLeafSize = unitSize;
        return this;
    }

    public @NotNull FractalLeaves setSemiSphereLeaves(boolean semiSphereLeaves)
    {
        this.semiSphereLeaves = semiSphereLeaves;
        return this;
    }

    public int getOriY() {
        return oriY;
    }

    public void setOriY(int oriY) {
        this.oriY = oriY;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public TerraformWorld getTw() {
        return tw;
    }

    public void setTw(TerraformWorld tw) {
        this.tw = tw;
    }


    @Override
    public FractalLeaves clone() throws CloneNotSupportedException {
        return (FractalLeaves) super.clone();
    }
}
