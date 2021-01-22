package org.terraform.tree;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.*;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.version.BeeHiveSpawner;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class FractalTreeBuilder {
    int height = 0;
    SimpleBlock top;
    float baseThickness = 3;
    int baseHeight = 7;
    float thicknessDecrement = 0.5f;
    int maxDepth = 4;
    int maxHeight = 999;
    float lengthDecrement = 1;
    Material trunkType = Material.OAK_WOOD;
    FractalLeaves fractalLeaves = new FractalLeaves(this);
    Random rand;
    double minBend = 0.8 * Math.PI / 6;
    double maxBend = 1.2 * Math.PI / 6;
    int heightVariation = 0;
    double initialTilt = 0;
    int alwaysOneStraight = 0;
    int alwaysOneStraightBranchLength = 0;
    boolean alwaysOneStraightExtendedBranches = false;
    boolean noMainStem = false;
    double beeChance = 0.0f;
    int vines = 0;
    int cocoaBeans = 0;
    int fractalThreshold = 1;
    int fractalsDone = 0;
    double maxPitch = 9999;
    double minPitch = -9999;
    protected TerraformWorld tw;
    float branchNoiseMultiplier = 0.7f;
    float branchNoiseFrequency = 0.09f;
    int oriX;
    int oriY;
    int oriZ;
    protected static HashMap<TerraformWorld, FastNoise> noiseCache = new HashMap<>();
    private FastNoise noiseGen;
    private SimpleBlock beeHive;
    private boolean coralDecoration = false;
    private double initialAngle;
    private int initialHeight;

    public FractalTreeBuilder(FractalTypes.Tree type) {
        switch (type) {
            case FOREST:
                this
                        .setBeeChance(TConfigOption.ANIMALS_BEE_HIVEFREQUENCY.getDouble())
                        .setBaseHeight(9)
                        .setBaseThickness(3.0f)
                        .setThicknessDecrement(0.3f)
                        .setLengthDecrement(1.3f)
                        .setMinBend(0.7 * Math.PI / 6)
                        .setMaxBend(0.85 * Math.PI / 6)
                        .setMaxDepth(4)
                        .setHeightVariation(2)
                        .setLeafBranchFrequency(0.05f)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(3)
                                .setLeafNoiseFrequency(1.0f).setLeafNoiseMultiplier(1.0f));
                break;
            case NORMAL_SMALL:
                this.setBeeChance(TConfigOption.ANIMALS_BEE_HIVEFREQUENCY.getDouble())
                        .setBaseHeight(5)
                        .setBaseThickness(1)
                        .setThicknessDecrement(1f)
                        .setMaxDepth(1)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(3)
                                .setLeafNoiseFrequency(1.0f).setLeafNoiseMultiplier(1.0f))
                        .setHeightVariation(1);
                break;
            case BIRCH_BIG:
                this.setBaseHeight(6)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(4)
                        .setHeightVariation(2)
                        .setMinBend(0.9 * Math.PI / 6)
                        .setMaxBend(1.1 * Math.PI / 6)
                        .setLengthDecrement(0.5f)
                        .setTrunkType(Material.BIRCH_WOOD)
                        .setFractalLeaves(new FractalLeaves(this).setMaterial(Material.BIRCH_LEAVES).setRadius(3, 2, 3));
                break;
            case BIRCH_SMALL:
                this.setBaseHeight(3)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(3)
                        .setHeightVariation(1)
                        .setMinBend(0.9 * Math.PI / 6)
                        .setMaxBend(1.1 * Math.PI / 6)
                        .setLengthDecrement(0.5f)
                        .setTrunkType(Material.BIRCH_WOOD)
                        .setFractalLeaves(new FractalLeaves(this).setMaterial(Material.BIRCH_LEAVES).setRadius(3, 1, 3));
                break;
            case SAVANNA_SMALL:
                this.setBaseHeight(7)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0)
                        .setMaxDepth(2)
                        .setTrunkType(Material.ACACIA_LOG)
                        .setMinBend(0.5 * Math.PI / 2)
                        .setMaxBend(0.8 * Math.PI / 2)
                        .setLengthDecrement(1)
                        .setHeightVariation(1)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 1, 4).setMaterial(Material.ACACIA_LEAVES));
                break;
            case JUNGLE_BIG:
                this.setBaseHeight(15)
                        .setBaseThickness(5)
                        .setThicknessDecrement(1f)
                        .setMaxDepth(3)
                        .setHeightVariation(6)
                        .setMaxBend(Math.PI / 6)
                        .setLengthDecrement(2)
                        .setVines(7)
                        .setTrunkType(Material.JUNGLE_WOOD)
                        .setCocoaBeans(3)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 1, 4)
                                .setMaterial(Material.JUNGLE_LEAVES).setOffsetY(1));
                break;
            case JUNGLE_SMALL:
                this.setBaseHeight(5)
                        .setHeightVariation(1)
                        .setLengthDecrement(1.5f)
                        .setMaxDepth(2)
                        .setBaseThickness(3)
                        .setThicknessDecrement(1.5f)
                        .setMaxBend(Math.PI / 3)
                        .setVines(3)
                        .setTrunkType(Material.JUNGLE_WOOD)
                        .setCocoaBeans(1)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 2, 4).setMaterial(Material.JUNGLE_LEAVES));
                break;
            case JUNGLE_EXTRA_SMALL:
                this.setBaseHeight(3)
                        .setMaxDepth(1)
                        .setBaseThickness(1.5f)
                        .setThicknessDecrement(0f)
                        .setVines(3)
                        .setTrunkType(Material.JUNGLE_WOOD)
                        .setCocoaBeans(1)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(3, 2, 3).setMaterial(Material.JUNGLE_LEAVES));
                break;
            case SAVANNA_BIG:
                this.setBaseHeight(10)
                        .setBaseThickness(15)
                        .setThicknessDecrement(4f)
                        .setMaxDepth(4)
                        .setTrunkType(Material.ACACIA_LOG)
                        .setLengthDecrement(0.4f)
                        .setHeightVariation(2)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 1.5f, 4)
                                .setMaterial(Material.ACACIA_LEAVES).setLeafNoiseFrequency(0.7f).setLeafNoiseMultiplier(0.8f));
                break;
            case WASTELAND_BIG:
                this.setBaseHeight(6)
                        .setBaseThickness(4)
                        .setThicknessDecrement(1f)
                        .setMaxDepth(4)
                        .setTrunkType(Material.SPRUCE_WOOD)
                        .setLengthDecrement(0.5f)
                        .setHeightVariation(1)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(0).setMaterial(Material.AIR));
                break;
            case TAIGA_BIG:
                this.setBaseHeight(10).setBaseThickness(3.5f)
                        .setThicknessDecrement(0.5f)
                        .setMaxDepth(5)
                        .setTrunkType(Material.SPRUCE_WOOD)
                        .setLengthDecrement(2)
                        .setHeightVariation(2)
                        .setAlwaysOneStraight(4)
                        .setAlwaysOneStraightExtendedBranches(true)
                        .setMinBend(Math.PI / 2)
                        .setMaxBend(Math.PI / 2)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(3, 5, 3).setMaterial(Material.SPRUCE_LEAVES)
                                .setConeLeaves(true).setLeafNoiseFrequency(0.3f).setLeafNoiseMultiplier(0.7f));
                break;
            case TAIGA_SMALL:
                this.setBaseHeight(5).setBaseThickness(1f)
                        .setThicknessDecrement(0.3f)
                        .setMaxDepth(4)
                        .setTrunkType(Material.SPRUCE_WOOD)
                        .setFractalLeaves(new FractalLeaves(this).setLeafNoiseFrequency(0.65f).setLeafNoiseMultiplier(0.8f)
                                .setRadius(2).setMaterial(Material.SPRUCE_LEAVES).setConeLeaves(true))
                        .setLengthDecrement(1)
                        .setAlwaysOneStraight(4)
                        .setAlwaysOneStraightExtendedBranches(true)
                        .setMinBend(Math.PI / 2)
                        .setMaxBend(Math.PI / 2)
                        .setHeightVariation(2);
                break;
            case SWAMP_BOTTOM:
                this.setBaseHeight(1)
                        .setBaseThickness(3)
                        .setThicknessDecrement(0.5f)
                        .setMaxDepth(3)
                        .setTrunkType(Material.OAK_WOOD)
                        .setLengthDecrement(-2f)
                        .setMaxBend(-Math.PI / 6)
                        .setMinBend(-Math.PI / 3)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(0).setMaterial(Material.OAK_LEAVES));
                break;
            case SWAMP_TOP:
                this.setBaseHeight(8)
                        .setBaseThickness(3)
                        .setThicknessDecrement(0.5f)
                        .setMaxDepth(4)
                        .setLengthDecrement(0f)
                        .setHeightVariation(2)
                        .setTrunkType(Material.OAK_WOOD)
                        .setVines(7)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(5, 2, 5));
                break;
            case COCONUT_TOP:
                this.setBaseHeight(8)
                        .setInitialTilt(Math.PI / 6)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(1)
                        .setLengthDecrement(2)
                        .setHeightVariation(1)
                        .setVines(3)
                        .setTrunkType(Material.JUNGLE_WOOD)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(3, 1.2f, 3));
                break;
            case GIANT_PUMPKIN:
                this.setBaseHeight(6)
                        .setBaseThickness(1)
                        .setThicknessDecrement(1f)
                        .setMaxDepth(0)
                        .setLengthDecrement(-0.5f)
                        .setHeightVariation(0)
                        .setTrunkType(Material.OAK_LOG)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4).setMaterial(Material.PUMPKIN));
            case DARK_OAK_SMALL:
                this.setBaseHeight(1)
                        .setBaseThickness(2)
                        .setThicknessDecrement(0.5f)
                        .setMaxDepth(3)
                        .setTrunkType(Material.DARK_OAK_WOOD)
                        .setLengthDecrement(0)
                        .setHeightVariation(0)
                        .setFractalThreshold(4)
                        .setMaxBend(1.4 * Math.PI / 6)
                        .setMinBend(1 * Math.PI / 6)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(2, 1, 2).setMaterial(Material.DARK_OAK_LEAVES));
                break;
            case DARK_OAK_BIG_TOP:
                this.setBaseHeight(6)
                        .setBaseThickness(8)
                        .setThicknessDecrement(2.5f)
                        .setMaxDepth(3)
                        .setTrunkType(Material.DARK_OAK_WOOD)
                        .setLengthDecrement(0)
                        .setHeightVariation(1)
                        .setFractalThreshold(4)
                        .setMaxBend(1.4 * Math.PI / 6)
                        .setMinBend(1 * Math.PI / 6)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 2, 4).setMaterial(Material.DARK_OAK_LEAVES).setOffsetY(1));
                break;
            case DARK_OAK_BIG_BOTTOM:
                this.setBaseHeight(4)
                        .setBaseThickness(4)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(3)
                        .setTrunkType(Material.DARK_OAK_WOOD)
                        .setLengthDecrement(-1)
                        .setHeightVariation(1)
                        .setFractalThreshold(5)
                        .setMaxBend(2.3 * Math.PI / 6)
                        .setMinBend(2.0 * Math.PI / 6)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(0).setMaterial(Material.DARK_OAK_LEAVES));
                break;
            case FROZEN_TREE_BIG:
                this.setBaseHeight(4)
                        .setBaseThickness(4)
                        .setThicknessDecrement(2f)
                        .setMaxDepth(4)
                        .setVines(4)
                        .setTrunkType(Material.SPRUCE_WOOD)
                        .setLengthDecrement(0)
                        .setHeightVariation(1)
                        .setFractalThreshold(4)
                        .setMaxBend(1.6 * Math.PI / 6)
                        .setMinBend(1.2 * Math.PI / 6)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 2, 4).setMaterial(Material.ICE));
                break;
            case FROZEN_TREE_SMALL:
                this.setBaseHeight(1)
                        .setBaseThickness(2)
                        .setThicknessDecrement(0.2f)
                        .setMaxDepth(4)
                        .setVines(4)
                        .setTrunkType(Material.SPRUCE_WOOD)
                        .setLengthDecrement(0)
                        .setHeightVariation(0)
                        .setFractalThreshold(4)
                        .setMaxBend(1.6 * Math.PI / 6)
                        .setMinBend(1.2 * Math.PI / 6)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(4, 1, 4).setMaterial(Material.ICE));
                break;
            case FIRE_CORAL:
                this.setBaseHeight(2)
                        .setInitialTilt(Math.PI / 2)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0)
                        .setMaxDepth(3)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(1, 4, 1).setMaterial(Material.FIRE_CORAL_BLOCK))
                        .setTrunkType(Material.FIRE_CORAL_BLOCK)
                        .setLengthDecrement(-2f)
                        .setHeightVariation(0)
                        .setMaxBend(Math.PI / 2)
                        .setMinBend(Math.PI / 2.5)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setCoralDecoration(true);
                break;
            case HORN_CORAL:
                this.setBaseHeight(2)
                        .setBaseThickness(2)
                        .setThicknessDecrement(0)
                        .setMaxDepth(3)
                        .setFractalLeaves(new FractalLeaves(this).setRadius(3, 1, 3).setMaterial(Material.HORN_CORAL_BLOCK))
                        .setTrunkType(Material.HORN_CORAL_BLOCK)
                        .setLengthDecrement(-1)
                        .setHeightVariation(0)
                        .setMaxBend(Math.PI / 3)
                        .setMinBend(Math.PI / 4)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setCoralDecoration(true)
                        .setNoMainStem(true);
                break;
            case BRAIN_CORAL:
                this.setBaseHeight(1)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(3)
                        .setFractalLeaves(new FractalLeaves(this)
                                .setRadius(1, 2, 1)
                                .setHollowLeaves(0.9)
                                .setMaterial(Material.BRAIN_CORAL_BLOCK))
                        .setTrunkType(Material.BRAIN_CORAL_BLOCK)
                        .setLengthDecrement(0)
                        .setHeightVariation(0)
                        .setFractalThreshold(3)
                        .setMaxBend(Math.PI / 3)
                        .setMinBend(Math.PI / 4)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setCoralDecoration(true);
                break;
            case TUBE_CORAL:
                this.setBaseHeight(3)
                        .setAlwaysOneStraight(3)
                        .setBaseThickness(3)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(3)
                        .setFractalLeaves(new FractalLeaves(this)
                                .setRadius(1, 1, 1)
                                .setHollowLeaves(0.9)
                                .setMaterial(Material.TUBE_CORAL_BLOCK))
                        .setTrunkType(Material.TUBE_CORAL_BLOCK)
                        .setLengthDecrement(0)
                        .setHeightVariation(1)
                        .setMaxBend(Math.PI / 3)
                        .setMinBend(Math.PI / 4)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setCoralDecoration(true);
                break;
            case BUBBLE_CORAL:
                this.setBaseHeight(3)
                        .setBaseThickness(1)
                        .setThicknessDecrement(0f)
                        .setMaxDepth(3)
                        .setFractalLeaves(new FractalLeaves(this)
                                .setRadius(3, 3, 3)
                                .setHollowLeaves(0.9)
                                .setMaterial(Material.BUBBLE_CORAL_BLOCK))
                        .setTrunkType(Material.BUBBLE_CORAL_BLOCK)
                        .setLengthDecrement(-1)
                        .setHeightVariation(1)
                        .setMaxBend(Math.PI / 2)
                        .setMinBend(Math.PI / 3)
                        .setMaxPitch(Math.PI)
                        .setMinPitch(0)
                        .setCoralDecoration(true)
                        .setNoMainStem(true);
                break;
        }
    }

    public void build(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        if (TConfigOption.MISC_TREES_FORCE_LOGS.getBoolean()) {
            this.trunkType = Material.getMaterial(StringUtils.replace(this.trunkType.toString(), "WOOD", "LOG"));
        }
        this.oriX = x;
        this.oriY = y;
        this.oriZ = z;
        this.tw = tw;
        //this.noiseGen = new FastNoise((int) tw.getSeed());
        if(!noiseCache.containsKey(tw)) {
        	FastNoise noise = new FastNoise((int) tw.getSeed());
        	noise.SetNoiseType(NoiseType.SimplexFractal);
        	noise.SetFractalOctaves(5);
        	noiseCache.put(tw, noise);
        }
        noiseGen = noiseCache.get(tw);
        // Setup noise to be used in randomising the sphere
        noiseGen.SetFrequency(branchNoiseFrequency);
        
        this.rand = tw.getRand(16L * 16 * x + 16L * y + z);
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        if (this.top == null) top = base;
        initialAngle = Math.PI / 2 + GenUtils.randDouble(rand, -initialTilt, initialTilt);

        alwaysOneStraightBranchLength = baseHeight;

        if (alwaysOneStraight > 0) {
            //Starting Trunk
            fractalBranch(rand, base,
                    initialAngle,
                    GenUtils.randDouble(rand, -initialTilt, initialTilt),
                    0,
                    baseThickness,
                    baseHeight);
        } else {
            initialHeight = baseHeight + GenUtils.randInt(-heightVariation, heightVariation);
            fractalBranch(rand, base,
                    initialAngle,
                    GenUtils.randDouble(rand, -initialTilt, initialTilt),
                    0, baseThickness,
                    initialHeight);
        }
        
        if(beeHive != null)
	        for (int i = 0; i < 8; i++) {
	            if (!beeHive.getType().isSolid()) {
	                BeeHiveSpawner.spawnFullBeeNest(beeHive);
	            	//TerraformGeneratorPlugin.logger.debug("Bee nest spawned at " + two.getRelative(0,-i,0).getCoords());
	                break;
	            }else
	            	beeHive = beeHive.getRelative(0,-1,0);
	        }
        
    }

    public void fractalBranch(Random rand, SimpleBlock base, double pitch, double yaw, int depth, double thickness, double size) {

        if (pitch > maxPitch) {
            //reset pitch
            pitch = maxPitch - rta();
        } else if (pitch < minPitch) {
            pitch = minPitch + rta();
        }

        if (depth >= maxDepth) {
            fractalLeaves.placeLeaves(base);
            base.setType(trunkType);
            return;
        }
        if (size <= 0) {
            fractalLeaves.placeLeaves(base);
            base.setType(trunkType);
            return;
        }

        boolean restore = false;
        if (noMainStem && size == initialHeight) {
            restore = true;
            size = 0;
        }

        int y = (int) (Math.round(size * Math.sin(pitch))); //Pitch is vertical tilt
        int x = (int) (Math.round(size * Math.cos(pitch) * Math.sin(yaw)));
        int z = (int) (Math.round(size * Math.cos(pitch) * Math.cos(yaw)));

        SimpleBlock two = base.getRelative(x, y, z);
        if (two.getY() > top.getY()) top = two;

        //Set height
        if (two.getY() - oriY > height) height = two.getY() - oriY;

        if (restore) {
            two = base;
            size = baseHeight;
        }

        drawLine(base, two, (int) (size), thickness);


        if (beeHive == null
                && Version.isAtLeast(15.1)
                && GenUtils.chance(rand, (int) (beeChance * 1000.0), 1000)) {
            for (int i = 0; i < 3; i++) {
                if (!two.getRelative(0, -i, 0).getType().isSolid()) {
                    beeHive = two.getRelative(0,-i,0);
                    break;
                    //TerraformGeneratorPlugin.logger.debug("Bee nest spawned at " + two.getRelative(0,-i,0).getCoords());
                }
            }

        }

        fractalsDone++;

        if (fractalsDone % fractalThreshold != 0
                && thickness >= 1
                && size >= 1) {
            //Make 1 branch
            fractalBranch(rand, two, pitch - randomAngle(),
                    yaw + GenUtils.randInt(rand, 1, 5)
                            * GenUtils.getSign(rand) * rta(),
                    depth,
                    thickness,
                    size);
            return;
        }

        if (alwaysOneStraight > 0 && pitch != initialAngle) {
            fractalBranch(rand, two, pitch - randomAngle(), yaw - rta(), 99, thickness - thicknessDecrement, size - lengthDecrement);
            return;
        }

        if (alwaysOneStraight > 0) {
            alwaysOneStraightBranchLength -= this.lengthDecrement;
            //Extend a central trunk and make more branches.
            //this.logType = Material.GREEN_WOOL;
            fractalBranch(rand, two, pitch + randomAngle(), -ra(Math.PI / 4, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
            //this.logType = Material.BLUE_WOOL;
            fractalBranch(rand, two, pitch + randomAngle(), ra(Math.PI / 4, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
            //this.logType = Material.CYAN_WOOL;
            fractalBranch(rand, two, pitch + randomAngle(), 5 * ra(Math.PI / 4, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
            //this.logType = Material.PURPLE_WOOL;
            fractalBranch(rand, two, pitch + randomAngle(), -5 * ra(Math.PI / 4, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);


            //4 more static angle fractals.
            if (alwaysOneStraightExtendedBranches) {
                fractalBranch(rand, two, pitch + randomAngle(), ra(0, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
                //this.logType = Material.PINK_WOOL;
                fractalBranch(rand, two, pitch + randomAngle(), ra(Math.PI / 2, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
                //this.logType = Material.BLACK_WOOL;
                fractalBranch(rand, two, pitch + randomAngle(), ra(Math.PI, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
                //this.logType = Material.WHITE_WOOL;
                fractalBranch(rand, two, pitch + randomAngle(), -ra(Math.PI / 2, 0.9, 1.1), depth + 1, thickness - thicknessDecrement, alwaysOneStraightBranchLength);
            }
            //this.logType = Material.SPRUCE_WOOD;
            fractalBranch(rand, two, pitch, yaw, depth + 1, thickness - thicknessDecrement, alwaysOneStraight);
        } else {
            //Make 4 branches
            fractalBranch(rand, two, pitch - randomAngle(), yaw - rta(), depth + 1, thickness - thicknessDecrement, size - lengthDecrement);
            fractalBranch(rand, two, pitch + randomAngle(), yaw + rta(), depth + 1, thickness - thicknessDecrement, size - lengthDecrement);
            fractalBranch(rand, two, pitch + randomAngle(), yaw + 5 * rta(), depth + 1, thickness - thicknessDecrement, size - lengthDecrement);
            fractalBranch(rand, two, pitch + randomAngle(), yaw - 5 * rta(), depth + 1, thickness - thicknessDecrement, size - lengthDecrement);
        }
    }

    public void drawLine(SimpleBlock one, SimpleBlock two, int segments, double thickness) {
        if (one.equals(two)) return;
        //Vector one to two;
        Vector v = two.getVector().subtract(one.getVector());
        for (int i = 0; i <= segments; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) segments));
            SimpleBlock segment = one.getRelative(seg);
            replaceSphere(((float) thickness) / 2, segment, trunkType);
        }
    }

    private void replaceSphere(float radius, SimpleBlock base, Material type) {
        if (radius <= 0) {
            return;
        }
        replaceSphere(radius, radius, radius, base, type);
    }

    //private boolean debug = true;
    private void replaceSphere(float rX, float rY, float rZ, SimpleBlock block, Material type) {

        // Don't place anything if radius is nothing
        if (rX <= 0 &&
                rY <= 0 &&
                rZ <= 0) {
            return;
        }

        // Radius 0.5 is 1 block
        if (rX <= 0.5 &&
                rY <= 0.5 &&
                rZ <= 0.5) {
            block.setType(type);
            return;
        }

        float noiseMultiplier = branchNoiseMultiplier;

        double maxR = rX;
        if (rX < rY) maxR = rY;
        if (rY < rZ) maxR = rZ;

        ArrayList<SimpleBlock> changed = new ArrayList<>();

        for (float y = -rY; y <= rY; y++) {
            for (float x = -rX; x <= rX; x++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    if (rel.getY() - this.oriY > this.maxHeight) {
                        return;
                    }
                    if (rel.getY() - this.oriY == this.maxHeight) {
                        if (rand.nextBoolean()) //Fade off if too high
                            return;
                    }

                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);

                    if (equationResult <= 1 + noiseMultiplier * noiseGen.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        rel.setType(type);

                        if (coralDecoration) {
                            if (!changed.contains(rel))
                                changed.add(rel);
                        }

                        //Decorate with fans
                        if (coralDecoration) {
                            CoralGenerator.generateSingleCoral(rel.getPopData(), rel.getX(), rel.getY(), rel.getZ(), this.fractalLeaves.material.toString());
                        }

                        if (cocoaBeans > 0
                                && Math.abs(x) >= rX - 2
                                && Math.abs(z) >= rZ - 2) {
                            //Coca beans
                            if (GenUtils.chance(cocoaBeans, 100)) {
                                for (BlockFace face : BlockUtils.directBlockFaces) {
                                    Directional dir = (Directional) Bukkit.createBlockData(Material.COCOA);
                                    dir.setFacing(face.getOppositeFace());
                                    ((Ageable) dir).setAge(GenUtils.randInt(rand, 0, ((Ageable) dir).getMaximumAge()));
                                    SimpleBlock beans = rel.getRelative(face);
                                    if (beans.getType().isSolid() ||
                                            beans.getType() == Material.WATER) continue;

                                    beans.setBlockData(dir);
                                }
                            }

                        }
                        if (vines > 0
                                && Math.abs(x) >= rX - 2
                                && Math.abs(z) >= rZ - 2) {
                            if (GenUtils.chance(2, 10)) {
                                dangleLeavesDown(rel, (int) Math.ceil(maxR), vines / 2, vines);
                            }

                            // Vines set only if the leaf type is leaves.
                            //Consider removal since this is done in fractalleaves.java
                            if (Tag.LEAVES.isTagged(fractalLeaves.material))
                                if (GenUtils.chance(1, 10)) {
                                    for (BlockFace face : BlockUtils.directBlockFaces) {
                                        MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
                                        dir.setFace(face.getOppositeFace(), true);
                                        SimpleBlock vine = rel.getRelative(face);
                                        if (vine.getType().isSolid() ||
                                                vine.getType() == Material.WATER) continue;

                                        vine.setBlockData(dir);
                                        for (int i = 0; i < GenUtils.randInt(1, vines); i++) {
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
        //if(Tag.LEAVES.isTagged(type))
        //debug = false;

        //Ensures that corals don't die
        while (!changed.isEmpty()) {
            SimpleBlock sb = changed.remove(new Random().nextInt(changed.size()));
            if (!CoralGenerator.isSaturatedCoral(sb)) {
                //No floating coral fans
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (Tag.WALL_CORALS.isTagged(sb.getRelative(face).getType()))
                        sb.getRelative(face).setType(Material.WATER);
                }

                //No levitating sea pickles & fans
                if (sb.getRelative(0, 1, 0).getType() == Material.SEA_PICKLE ||
                        Tag.CORAL_PLANTS.isTagged(sb.getRelative(0, 1, 0).getType())) {
                    sb.getRelative(0, 1, 0).setType(Material.WATER);
                }
                sb.setType(Material.WATER);

            } else
                sb.setType(trunkType);
        }
    }

    void dangleLeavesDown(SimpleBlock block, int leafDist, int min, int max) {
        BlockData type = Bukkit.createBlockData(fractalLeaves.material);
        if (Tag.LEAVES.isTagged(fractalLeaves.material)) {
            Leaves leaf = (Leaves) type;
            leaf.setDistance(1);
        }
        for (int i = 1; i <= GenUtils.randInt(min, max); i++) {
            if (!block.getRelative(0, -i, 0).getType().isSolid())
                block.getRelative(0, -i, 0).lsetBlockData(type);
            else
                break;
        }

        //Log for good measure, as well as some surrounding leaves.
        if (Tag.LEAVES.isTagged(fractalLeaves.material))
            block.setType(this.trunkType);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            block.getRelative(face).lsetBlockData(type);
        }
        block.getRelative(0, 1, 0).lsetBlockData(type);
    }

    public FractalTreeBuilder setSnowyLeaves(boolean snowy) {
        this.fractalLeaves.setSnowy(snowy);
        return this;
    }

    public FractalTreeBuilder setVines(int vines) {
        this.vines = vines;
        return this;
    }

    public FractalTreeBuilder setHeightVariation(int heightVariation) {
        this.heightVariation = heightVariation;
        return this;
    }

    public FractalTreeBuilder setMinBend(double bend) {
        this.minBend = bend;
        return this;
    }

    public FractalTreeBuilder setMaxBend(double bend) {
        this.maxBend = bend;
        return this;
    }

    public FractalTreeBuilder setInitialTilt(double initialTilt) {
        this.initialTilt = initialTilt;
        return this;
    }

    public FractalTreeBuilder setFractalLeaves(FractalLeaves fractalLeaves) {
        this.fractalLeaves = fractalLeaves;
        return this;
    }

    public FractalTreeBuilder setTrunkType(Material log) {
        this.trunkType = log;
        return this;
    }

    public FractalTreeBuilder setLengthDecrement(float d) {
        this.lengthDecrement = d;
        return this;
    }

    public FractalTreeBuilder setMaxDepth(int d) {
        this.maxDepth = d;
        return this;
    }

    public FractalTreeBuilder setAlwaysOneStraight(int val) {
        this.alwaysOneStraight = val;
        return this;
    }

    public FractalTreeBuilder setAlwaysOneStraightExtendedBranches(boolean bool) {
        this.alwaysOneStraightExtendedBranches = bool;
        return this;
    }

    public FractalTreeBuilder setNoMainStem(boolean bool) {
        this.noMainStem = bool;
        return this;
    }

    /**
     * @param beeChance the beeChance to set
     */
    public FractalTreeBuilder setBeeChance(double beeChance) {
        this.beeChance = beeChance;
        return this;
    }

    /**
     * @return the cocabeans
     */
    public int getCocoaBeans() {
        return cocoaBeans;
    }

    /**
     * @param cocoaBeans the cocabeans to set
     */
    public FractalTreeBuilder setCocoaBeans(int cocoaBeans) {
        this.cocoaBeans = cocoaBeans;
        return this;
    }

    public FractalTreeBuilder setThicknessDecrement(float d) {
        this.thicknessDecrement = d;
        return this;
    }

    public FractalTreeBuilder setBaseThickness(float baseThickness) {
        this.baseThickness = baseThickness;
        return this;
    }

    public FractalTreeBuilder setBaseHeight(int h) {
        this.baseHeight = h;
        return this;
    }

    public FractalTreeBuilder setFractalThreshold(int i) {
        this.fractalThreshold = i;
        return this;
    }

    public FractalTreeBuilder setMaxPitch(double max) {
        this.maxPitch = max;
        return this;
    }

    public FractalTreeBuilder setBranchNoiseMultiplier(float multiplier) {
        this.branchNoiseMultiplier = multiplier;
        return this;
    }

    public FractalTreeBuilder setLeafBranchFrequency(float freq) {
        this.branchNoiseFrequency = freq;
        return this;
    }

    public FractalTreeBuilder setMaxHeight(int max) {
        this.maxHeight = max;
        return this;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    public FractalTreeBuilder setMinPitch(double min) {
        this.minPitch = min;
        return this;
    }

    /**
     * Random angle defined by the min and max bend angles
     */
    public double randomAngle() {
        return GenUtils.randDouble(rand, minBend, maxBend);
    }

    /**
     * Random-thirty-ish-angle
     * @return An angle between 0.8*30 to 1.2*30 degrees in radians
     */
    public double rta() {
        return GenUtils.randDouble(new Random(), 0.8 * Math.PI / 6, 1.2 * Math.PI / 6);
    }

    /**
     * Random-angle
     * @return An angle between lowerBound*30 to upperBound*30 degrees in radians
     */
    public double ra(double base, double lowerBound, double upperBound) {
        return GenUtils.randDouble(new Random(), lowerBound * base, upperBound * base);
    }

    public FractalTreeBuilder setCoralDecoration(boolean d) {
        this.coralDecoration = d;
        return this;
    }
}
