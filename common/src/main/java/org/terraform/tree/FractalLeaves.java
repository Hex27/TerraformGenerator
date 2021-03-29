package org.terraform.tree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Leaves;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.ArrayList;
import java.util.Random;

public class FractalLeaves {

    public float radiusX = 4;
    public float radiusY = 2;
    public float radiusZ = 4;
    public int offsetY = 0;
    public boolean halfSphere = false;
    public Material material = Material.OAK_LEAVES;
    public FractalTreeBuilder builder;
    Random rand = new Random();
    float leafNoiseMultiplier = 0.7f;
    float leafNoiseFrequency = 0.09f;
    double hollowLeaves = 0.0;
    boolean coneLeaves = false;
    boolean snowy = false;
    float weepingLeavesChance = 0;
    int weepingLeavesLength = 0;
    boolean coralDecoration = false;

    public FractalLeaves(FractalTreeBuilder builder) {
    	this.builder = builder;
    }

    public void placeLeaves(SimpleBlock block) { 
    	// Setup noise to be used in randomising the sphere

        FastNoise noiseGen = NoiseCacheHandler.getNoise(
        		builder.tw, 
        		NoiseCacheEntry.FRACTALTREES_LEAVES_NOISE, 
        		world -> {
                    FastNoise n = new FastNoise((int) world.getSeed());
                	n.SetFractalOctaves(5);
                	n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                
        	        return n;
        		});
        noiseGen.SetFrequency(leafNoiseFrequency);
        
        // Don't place anything if radius is nothing
        if (radiusX <= 0 &&
                radiusY <= 0 &&
                radiusZ <= 0) {
            return;
        }

        // Radius 0.5 is 1 block
        if (radiusX <= 0.5 &&
                radiusY <= 0.5 &&
                radiusZ <= 0.5) {
            block.setType(material);
            return;
        }

        //Initialise noise to be used in randomising the sphere
        float noiseMultiplier = leafNoiseMultiplier;

        double maxR = radiusX;
        if (radiusX < radiusY) maxR = radiusY;
        if (radiusY < radiusZ) maxR = radiusZ;

        ArrayList<SimpleBlock> changed = new ArrayList<>();

        for (int y = halfSphere ? 0 : -Math.round(radiusY); y <= radiusY; y++) {
            for (int x = -Math.round(radiusX); x <= radiusX; x++) {
                for (int z = -Math.round(radiusZ); z <= radiusZ; z++) {
                    SimpleBlock relativeBlock = block.getRelative(Math.round(x), Math.round(y) + offsetY, Math.round(z));

                    if (relativeBlock.getY() - builder.oriY > builder.maxHeight) {
                        return;
                    }

                    if (relativeBlock.getY() - builder.oriY == builder.maxHeight) {
                        if (rand.nextBoolean()) //Fade off if too high
                            return;
                    }

                    float effectiveY = y;

                    if (coneLeaves) {
                        effectiveY += radiusY / 2; // Shift center area downwards
                        // Compress negative y
                        if (effectiveY < 0) effectiveY = effectiveY * 2.0f;

                        // Extend positive y and multiply it by a power to make it sharp
                        if (effectiveY > 0) {
                            effectiveY = effectiveY * (2.0f / 3.0f);
                            effectiveY = (float) Math.pow(effectiveY, 1.3);
                            if (effectiveY > radiusY) effectiveY = radiusY;
                        }
                        relativeBlock = relativeBlock.getRelative(0, (int) (radiusY / 2), 0);
                    }

                    double equationResult = Math.pow(x, 2) / Math.pow(radiusX, 2)
                            + Math.pow(effectiveY, 2) / Math.pow(radiusY, 2)
                            + Math.pow(z, 2) / Math.pow(radiusZ, 2);

                    if (equationResult <= 1 + noiseMultiplier * noiseGen.GetNoise(relativeBlock.getX(), relativeBlock.getY(), relativeBlock.getZ())) {
                        if (equationResult < hollowLeaves)
                            continue;

                        if (Tag.CORALS.isTagged(material)) {
                            if (!changed.contains(relativeBlock))
                                changed.add(relativeBlock);
                        }

                        //Decorate with fans
                        if (coralDecoration) {
                        	CoralGenerator.generateSingleCoral(relativeBlock.getPopData(), relativeBlock.getX(), relativeBlock.getY(), relativeBlock.getZ(), this.material.toString());
                        }

                        // Leaves do not replace solid blocks.
                        if (!relativeBlock.getType().isSolid()) {
                            if (Tag.LEAVES.isTagged(material)) {
                                Leaves leaf = (Leaves) Bukkit.createBlockData(material);
//
                                leaf.setDistance(1);
                                relativeBlock.setBlockData(leaf);
                            } else {
                                relativeBlock.setType(material);
                            }
                        }

                        if (snowy) {
                            if (!relativeBlock.getRelative(0, 1, 0).getType().isSolid()) {
                                relativeBlock.getRelative(0, 1, 0).setType(Material.SNOW);
                            }
                        }

                        if (builder.vines > 0
                                && Math.abs(x) >= radiusX - 2
                                && Math.abs(z) >= radiusZ - 2) {
                            if (GenUtils.chance(2, 10)) {
                                builder.dangleLeavesDown(relativeBlock, (int) Math.ceil(maxR), builder.vines / 2, builder.vines);
                            }
                            
                            if (Tag.LEAVES.isTagged(material)&&GenUtils.chance(1, 10)) {
                                for (BlockFace face : BlockUtils.directBlockFaces) {
                                    MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
                                    dir.setFace(face.getOppositeFace(), true);
                                    SimpleBlock vine = relativeBlock.getRelative(face);
                                    if (vine.getType().isSolid() ||
                                            vine.getType() == Material.WATER) continue;

                                    vine.setBlockData(dir);
                                    for (int i = 0; i < GenUtils.randInt(1, builder.vines); i++) {
                                        if (vine.getRelative(0, -i, 0).getType().isSolid() ||
                                                vine.getRelative(0, -i, 0).getType() == Material.WATER) break;
                                        vine.getRelative(0, -i, 0).setBlockData(dir);
                                    }
                                }
                            }
                        }

                        if (weepingLeavesChance > 0 && Math.random() < weepingLeavesChance) {
                            weepingLeaves(relativeBlock, Math.round(weepingLeavesChance * weepingLeavesLength), weepingLeavesLength);
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
                    if (Tag.WALL_CORALS.isTagged(sb.getRelative(face).getType()))
                        sb.getRelative(face).setType(Material.WATER);
                }

                // No levitating sea pickles & fans
                if (sb.getRelative(0, 1, 0).getType() == Material.SEA_PICKLE ||
                        Tag.CORAL_PLANTS.isTagged(sb.getRelative(0, 1, 0).getType())) {
                    sb.getRelative(0, 1, 0).setType(Material.WATER);
                }
                sb.setType(Material.WATER);

            } else {
                sb.setType(material);
            }
        }
    }

    private void weepingLeaves(SimpleBlock base, int minDist, int maxDist) {
        BlockData type = Bukkit.createBlockData(material);
        if (Tag.LEAVES.isTagged(material)) {
            Leaves leaf = (Leaves) type;
            leaf.setDistance(1);
        }

        for (int i = 1; i <= GenUtils.randInt(minDist, maxDist); i++) {
            if (base.getRelative(0, -i, 0).getType().isAir())
                base.getRelative(0, -i, 0).lsetBlockData(type);
            else
                break;
        }
    }

    public FractalLeaves setHalfSphere(boolean halfSphere) {
        this.halfSphere = halfSphere;
        return this;
    }

    public FractalLeaves setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public FractalLeaves setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public FractalLeaves setRadiusX(float radiusX) {
        this.radiusX = radiusX;
        return this;
    }

    public FractalLeaves setRadiusY(float radiusY) {
        this.radiusY = radiusY;
        return this;
    }

    public FractalLeaves setRadiusZ(float radiusZ) {
        this.radiusZ = radiusZ;
        return this;
    }

    public FractalLeaves setRadius(float radius) {
        this.radiusX = radius;
        this.radiusY = radius;
        this.radiusZ = radius;
        return this;
    }

    public FractalLeaves setRadius(float x, float y, float z) {
        this.radiusX = x;
        this.radiusY = y;
        this.radiusZ = z;
        return this;
    }

    public FractalLeaves setHollowLeaves(double hollow) {
        this.hollowLeaves = hollow;
        return this;
    }

    public FractalLeaves setConeLeaves(boolean coneLeaves) {
        this.coneLeaves = coneLeaves;
        return this;
    }

    public FractalLeaves setLeafNoiseMultiplier(float multiplier) {
        this.leafNoiseMultiplier = multiplier;
        return this;
    }

    public FractalLeaves setLeafNoiseFrequency(float freq) {
        this.leafNoiseFrequency = freq;
        return this;
    }

    public FractalLeaves setSnowy(boolean snowy) {
        this.snowy = snowy;
        return this;
    }

    /**
     * Creates dangling leaves without vines. Useful for
     * creating types of weeping trees.
     * @param chance    chance of creating dangling leaves per block (0 - 1)
     * @param maxLength maximum length of dangling leaves
     */
    public FractalLeaves setWeepingLeaves(float chance, int maxLength) {
        this.weepingLeavesChance = chance;
        this.weepingLeavesLength = maxLength;
        return this;
    }
}
