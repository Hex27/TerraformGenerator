package org.terraform.tree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Leaves;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class FractalLeaves {

    Random rand = new Random();

    public float radiusX = 4;
    public float radiusY = 2;
    public float radiusZ = 4;
    public int offsetY = 0;

    public boolean halfSphere = false;
    double hollowLeaves = 0.0;

    boolean coneLeaves = false;

    public Material material = Material.OAK_LEAVES;
    public FractalTreeBuilder builder;

    public FractalLeaves(FractalTreeBuilder builder) {
        this.builder = builder;
    }

    public void placeLeaves(int seed, SimpleBlock block) {
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
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        float noiseMultiplier = builder.leafNoiseMultiplier;
        noise.SetFrequency(builder.leafNoiseFrequency);
        noise.SetFractalOctaves(5);

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

                    if(coneLeaves) {
                    	effectiveY += radiusY / 2; // Shift center area downwards
                    	// Compress negative y
                    	if(effectiveY < 0) effectiveY = effectiveY * 2.0f;

                    	//Extend positive y and multiply it by a power to make it sharp
                    	if(effectiveY > 0) {
                    		effectiveY=effectiveY*(2.0f/3.0f);
                    		effectiveY = (float) Math.pow(effectiveY, 1.3);
                    		if(effectiveY > radiusY) effectiveY = radiusY;
                    	}
                		relativeBlock = relativeBlock.getRelative(0,(int) (radiusY/2),0);
                    }

                    double equationResult = Math.pow(x, 2) / Math.pow(radiusX, 2)
                            + Math.pow(effectiveY, 2) / Math.pow(radiusY, 2)
                            + Math.pow(z, 2) / Math.pow(radiusZ, 2);

                    if (equationResult <= 1 + noiseMultiplier * noise.GetNoise(relativeBlock.getX(), relativeBlock.getY(), relativeBlock.getZ())) {
                        if (equationResult < hollowLeaves)
                            continue;

                        if (Tag.CORALS.isTagged(material)) {
                            if (!changed.contains(relativeBlock))
                                changed.add(relativeBlock);
                        }

                        //Leaves do not replace solid blocks.
                        if (Tag.LEAVES.isTagged(material) && !relativeBlock.getType().isSolid()) {
                            Leaves leaf = (Leaves) Bukkit.createBlockData(material);
//
                            leaf.setDistance(1);
                            relativeBlock.setBlockData(leaf);
                        } else if (!Tag.LEAVES.isTagged(material)) {
                            relativeBlock.setType(material);
                        }

                        if (builder.snowy) {
                            if (!relativeBlock.getRelative(0, 1, 0).getType().isSolid()) {
                                relativeBlock.getRelative(0, 1, 0).setType(Material.SNOW);
                            }
                        }

                        if (builder.cocoaBeans > 0
                                && Math.abs(x) >= radiusX - 2
                                && Math.abs(z) >= radiusZ - 2) {
                            //Coca beans
                            if (GenUtils.chance(builder.cocoaBeans, 100)) {
                                for (BlockFace face : BlockUtils.directBlockFaces) {
                                    Directional dir = (Directional) Bukkit.createBlockData(Material.COCOA);
                                    dir.setFacing(face.getOppositeFace());
                                    ((Ageable) dir).setAge(GenUtils.randInt(rand, 0, ((Ageable) dir).getMaximumAge()));
                                    SimpleBlock beans = relativeBlock.getRelative(face);
                                    if (beans.getType().isSolid() ||
                                            beans.getType() == Material.WATER) continue;

                                    beans.setBlockData(dir);
                                }
                            }

                        }

                        if (builder.vines > 0
                                && Math.abs(x) >= radiusX - 2
                                && Math.abs(z) >= radiusZ - 2) {
                            if (GenUtils.chance(2, 10)) {
                                builder.dangleLeavesDown(relativeBlock, (int) Math.ceil(maxR), builder.vines / 2, builder.vines);
                            }

                            // Vines set only if the leaf type is leaves.
                            if (Tag.LEAVES.isTagged(material))
                                if (GenUtils.chance(1, 10)) {
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

            } else
                sb.setType(material);
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
}
