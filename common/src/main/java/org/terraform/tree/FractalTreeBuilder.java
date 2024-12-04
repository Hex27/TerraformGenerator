package org.terraform.tree;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.BeeHiveSpawner;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.Random;

public class FractalTreeBuilder {
    protected TerraformWorld tw;
    protected boolean coralDecoration = false;
    int height = 0;
    SimpleBlock top;
    float baseThickness = 3;
    int baseHeight = 7;
    float thicknessDecrement = 0.5f;
    float minThickness = 0f;
    int maxDepth = 4;
    // maxHeight is used for corals to force sea-level cutoff.
    int maxHeight = 999;
    float lengthDecrement = 1;
    float lengthDecrementMultiplier = 1;
    @Nullable
    Material trunkType = Material.OAK_WOOD;
    FractalLeaves fractalLeaves = new FractalLeaves();
    Random rand;
    double minBend = 0.8 * Math.PI / 6;
    double maxBend = 1.2 * Math.PI / 6;
    float depthPitchMultiplier = 1;
    int heightVariation = 0;
    double initialTilt = 0;
    double minInitialTilt = -1;
    int alwaysOneStraight = 0;
    int alwaysOneStraightBranchLength = 0;
    int alwaysOneStraightBranchSpawningDepth = 1;
    boolean alwaysOneStraightExtendedBranches = false;
    double alwaysOneStraightBranchYawLowerMultiplier = 0.9;
    double alwaysOneStraightBranchYawUpperMultiplier = 1.1;
    boolean noMainStem = false;
    double beeChance = 0f;
    int vines = 0;
    int cocoaBeans = 0;
    int fractalThreshold = 1;
    int fractalsDone = 0;
    double maxPitch = 9999;
    double minPitch = -9999;
    float branchNoiseMultiplier = 0.7f;
    float branchNoiseFrequency = 0.09f;
    int oriX;
    int oriY;
    int oriZ;
    private SimpleBlock beeHive;
    private double initialAngle;

    private int initialHeight;
    private boolean heightGradientChecked = false;

    public FractalTreeBuilder(FractalTypes.@NotNull Tree type) {
        switch (type) {
            case FOREST:
                this.setBeeChance(TConfig.c.ANIMALS_BEE_HIVEFREQUENCY)
                    .setBaseHeight(9)
                    .setBaseThickness(3f)
                    .setThicknessDecrement(0.3f)
                    .setLengthDecrement(1.3f)
                    .setMinBend(0.7 * Math.PI / 6)
                    .setMaxBend(0.85 * Math.PI / 6)
                    .setMaxDepth(4)
                    .setHeightVariation(2)
                    .setLeafBranchFrequency(0.05f)
                    .setFractalLeaves(new FractalLeaves().setRadius(3)
                                                         .setLeafNoiseFrequency(1f)
                                                         .setLeafNoiseMultiplier(1f));
                break;
            case NORMAL_SMALL:
                this.setBeeChance(TConfig.c.ANIMALS_BEE_HIVEFREQUENCY)
                    .setBaseHeight(5)
                    .setBaseThickness(1)
                    .setThicknessDecrement(1f)
                    .setMaxDepth(1)
                    .setFractalLeaves(new FractalLeaves().setRadius(3)
                                                         .setLeafNoiseFrequency(1f)
                                                         .setLeafNoiseMultiplier(1f))
                    .setHeightVariation(1);
                break;
            case AZALEA_TOP:
                this.setBeeChance(TConfig.c.ANIMALS_BEE_HIVEFREQUENCY)
                    .setBaseHeight(3)
                    .setBaseThickness(1)
                    .setThicknessDecrement(0.3f)
                    .setLengthDecrement(0.3f)
                    .setMaxDepth(2)
                    .setFractalLeaves(new FractalLeaves().setMaterial(
                                                                 Material.AZALEA_LEAVES,
                                                                 Material.FLOWERING_AZALEA_LEAVES
                                                         )
                                                         .setRadiusX(3)
                                                         .setRadiusZ(3)
                                                         .setRadiusY(1.5f)
                                                         .setLeafNoiseFrequency(1f)
                                                         .setLeafNoiseMultiplier(1f)
                                                         .setWeepingLeaves(0.3f, 3))
                    .setVines(3)
                    .setMinBend(0.9 * Math.PI / 6)
                    .setMaxBend(1.1 * Math.PI / 6)
                    .setHeightVariation(0);
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
                    .setFractalLeaves(new FractalLeaves().setMaterial(Material.BIRCH_LEAVES).setRadius(3, 2.3f, 3));
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
                    .setFractalLeaves(new FractalLeaves().setMaterial(Material.BIRCH_LEAVES).setRadius(3, 2.3f, 3));
                break;
            case CHERRY_SMALL:
                this.setBaseHeight(4)
                    .setBaseThickness(2.5f)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(3)
                    .setDepthPitchMultiplier(0.8f)
                    .setInitialTilt(Math.PI / 8)
                    .setHeightVariation(1)
                    .setMinBend(0.9 * Math.PI / 6)
                    .setMaxBend(1.1 * Math.PI / 6)
                    .setLengthDecrement(-0.5f)
                    .setMinThickness(1f)
                    .setTrunkType(V_1_20.CHERRY_LOG)
                    .setFractalLeaves(new FractalLeaves().setMaterial(V_1_20.CHERRY_LEAVES)
                                                         .setRadius(3, 2f, 3));
                break;
            case CHERRY_THICK:
                this.setBaseHeight(5)
                    .setBaseThickness(3f)
                    .setThicknessDecrement(0.4f)
                    .setMaxDepth(4)
                    .setDepthPitchMultiplier(-0.6f)
                    .setInitialTilt(1.3 * Math.PI / 6)
                    .setMinInitialTilt(Math.PI / 6)
                    .setHeightVariation(0)
                    .setMinBend(0.9 * Math.PI / 6)
                    .setMaxBend(1.1 * Math.PI / 6)
                    .setLengthDecrement(0.3f)
                    .setMinThickness(1f)
                    .setTrunkType(V_1_20.CHERRY_WOOD)
                    .setFractalLeaves(new FractalLeaves().setMaterial(V_1_20.CHERRY_LEAVES)
                                                         .setRadius(3, 2f, 3)
                                                         .setLeafNoiseFrequency(0.15f));
                break;
            case ANDESITE_PETRIFIED_SMALL:
                this.setBaseHeight(6)
                    .setBaseThickness(3)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(3)
                    .setTrunkType(Material.ANDESITE)
                    .setMinBend(1.1 * Math.PI / 6)
                    .setMaxBend(1.3 * Math.PI / 6)
                    .setLengthDecrement(1)
                    .setHeightVariation(2)
                    .setVines(3)
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2f, 4)
                                                         .setMaterial(Material.ANDESITE,
                                                                 Material.POLISHED_ANDESITE,
                                                                 Material.ANDESITE
                                                         )
                                                         .setWeepingLeaves(0.3f, 3));
                break;
            case GRANITE_PETRIFIED_SMALL:
                this.setBaseHeight(6)
                    .setBaseThickness(3)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(3)
                    .setTrunkType(Material.GRANITE)
                    .setMinBend(1.1 * Math.PI / 6)
                    .setMaxBend(1.3 * Math.PI / 6)
                    .setLengthDecrement(1)
                    .setHeightVariation(2)
                    .setVines(3)
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2f, 4)
                                                         .setMaterial(Material.GRANITE,
                                                                 Material.POLISHED_GRANITE,
                                                                 Material.GRANITE
                                                         )
                                                         .setWeepingLeaves(0.3f, 3));
                break;
            case DIORITE_PETRIFIED_SMALL:
                this.setBaseHeight(6)
                    .setBaseThickness(3)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(3)
                    .setTrunkType(Material.DIORITE)
                    .setMinBend(1.1 * Math.PI / 6)
                    .setMaxBend(1.3 * Math.PI / 6)
                    .setLengthDecrement(1)
                    .setHeightVariation(2)
                    .setVines(3)
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2f, 4)
                                                         .setMaterial(Material.DIORITE,
                                                                 Material.POLISHED_DIORITE,
                                                                 Material.DIORITE
                                                         )
                                                         .setWeepingLeaves(0.3f, 3));
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
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2f, 4).setMaterial(Material.ACACIA_LEAVES));
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
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 1, 4)
                                                         .setMaterial(Material.JUNGLE_LEAVES)
                                                         .setOffsetY(1)
                                                         .setWeepingLeaves(0.4f, 7));
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
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2, 4)
                                                         .setMaterial(Material.JUNGLE_LEAVES)
                                                         .setWeepingLeaves(0.3f, 3));
                break;
            case JUNGLE_EXTRA_SMALL:
                this.setBaseHeight(3)
                    .setMaxDepth(1)
                    .setBaseThickness(1.5f)
                    .setThicknessDecrement(0f)
                    .setVines(3)
                    .setTrunkType(Material.JUNGLE_WOOD)
                    .setCocoaBeans(1)
                    .setFractalLeaves(new FractalLeaves().setRadius(3, 2, 3)
                                                         .setMaterial(Material.JUNGLE_LEAVES)
                                                         .setWeepingLeaves(0.3f, 3));
                break;
            case SAVANNA_BIG:
                this.setBaseHeight(10)
                    .setBaseThickness(15)
                    .setThicknessDecrement(4f)
                    .setMaxDepth(4)
                    .setTrunkType(Material.ACACIA_LOG)
                    .setLengthDecrement(0.4f)
                    .setHeightVariation(2)
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2.5f, 4)
                                                         .setMaterial(Material.ACACIA_LEAVES)
                                                         .setLeafNoiseFrequency(0.7f)
                                                         .setLeafNoiseMultiplier(0.8f));
                break;
            case WASTELAND_BIG:
                this.setBaseHeight(6)
                    .setBaseThickness(4)
                    .setThicknessDecrement(1f)
                    .setMaxDepth(4)
                    .setTrunkType(Material.SPRUCE_WOOD)
                    .setLengthDecrement(0.5f)
                    .setHeightVariation(1)
                    .setFractalLeaves(new FractalLeaves().setRadius(0).setMaterial(Material.AIR));
                break;
            case TAIGA_BIG:
                this.setBaseHeight(10)
                    .setBaseThickness(3.5f)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(5)
                    .setTrunkType(Material.SPRUCE_WOOD)
                    .setLengthDecrement(2)
                    .setHeightVariation(2)
                    .setAlwaysOneStraight(4)
                    .setAlwaysOneStraightExtendedBranches(true)
                    .setMinBend(Math.PI / 2)
                    .setMaxBend(Math.PI / 2)
                    .setFractalLeaves(new FractalLeaves().setRadius(3, 5, 3)
                                                         .setMaterial(Material.SPRUCE_LEAVES)
                                                         .setConeLeaves(true)
                                                         .setLeafNoiseFrequency(0.3f)
                                                         .setLeafNoiseMultiplier(0.7f));
                break;
            case TAIGA_SMALL:
                this.setBaseHeight(5)
                    .setBaseThickness(1f)
                    .setThicknessDecrement(0.3f)
                    .setMaxDepth(4)
                    .setTrunkType(Material.SPRUCE_WOOD)
                    .setFractalLeaves(new FractalLeaves().setLeafNoiseFrequency(0.65f)
                                                         .setLeafNoiseMultiplier(0.8f)
                                                         .setRadius(2)
                                                         .setMaterial(Material.SPRUCE_LEAVES)
                                                         .setConeLeaves(true))
                    .setLengthDecrement(1)
                    .setAlwaysOneStraight(4)
                    .setAlwaysOneStraightExtendedBranches(true)
                    .setMinBend(Math.PI / 2)
                    .setMaxBend(Math.PI / 2)
                    .setHeightVariation(2);
                break;
            case SCARLET_BIG:
                this.setBaseHeight(10)
                    .setBaseThickness(6f)
                    .setThicknessDecrement(0.7f)
                    .setLengthDecrement(0.5f)
                    .setLengthDecrementMultiplier(1.5f)
                    .setMinThickness(0.5f)
                    .setMaxDepth(7)
                    .setTrunkType(Material.BIRCH_WOOD)
                    .setHeightVariation(2)
                    .setAlwaysOneStraightBranchLength(14)
                    .setAlwaysOneStraight(6)
                    .setAlwaysOneStraightExtendedBranches(false)
                    .setAlwaysOneStraightBranchYawLowerMultiplier(0.7d)
                    .setAlwaysOneStraightBranchYawUpperMultiplier(1.3d)
                    .setAlwaysOneStraightBranchSpawningDepth(3)
                    .setMinBend(Math.PI / 3)
                    .setMaxBend(Math.PI / 2)
                    .setFractalLeaves(new FractalLeaves().setRadius(5, 2, 5)
                                                         .setMaterial(Material.OAK_LEAVES)
                                                         .setConeLeaves(true)
                                                         .setLeafNoiseFrequency(0.5f)
                                                         .setLeafNoiseMultiplier(0.8f));
                break;
            case SCARLET_SMALL:
                this.setBaseHeight(2)
                    .setBaseThickness(1f)
                    .setThicknessDecrement(0.3f)
                    .setMaxDepth(1)
                    .setTrunkType(Material.BIRCH_LOG)
                    .setFractalLeaves(new FractalLeaves().setLeafNoiseFrequency(0.65f)
                                                         .setLeafNoiseMultiplier(0.8f)
                                                         .setRadius(2)
                                                         .setMaterial(Material.OAK_LEAVES)
                                                         .setConeLeaves(true))
                    .setLengthDecrement(1)
                    .setHeightVariation(1);
                break;
            case SWAMP_TOP:
                this.setBaseHeight(8)
                    .setBaseThickness(3)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(4)
                    .setLengthDecrement(0f)
                    .setHeightVariation(2)
                    .setTrunkType(V_1_19.MANGROVE_WOOD)
                    .setVines(7)
                    .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.4f, 7)
                                                         .setMaterial(V_1_19.MANGROVE_LEAVES)
                                                         .setRadius(5, 2, 5)
                                                         .setMangrovePropagules(true));
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
                    .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 3).setRadius(3, 1.2f, 3));
                break;
            case GIANT_PUMPKIN:
                this.setBaseHeight(6)
                    .setBaseThickness(1)
                    .setThicknessDecrement(1f)
                    .setMaxDepth(0)
                    .setLengthDecrement(-0.5f)
                    .setHeightVariation(0)
                    .setTrunkType(Material.OAK_LOG)
                    .setFractalLeaves(new FractalLeaves().setRadius(4).setMaterial(Material.PUMPKIN));
            case DARK_OAK_SMALL:
                this.setBaseHeight(3)
                    .setBaseThickness(3)
                    .setThicknessDecrement(0.5f)
                    .setMaxDepth(3)
                    .setTrunkType(Material.DARK_OAK_WOOD)
                    .setLengthDecrement(0)
                    .setHeightVariation(0)
                    .setFractalThreshold(4)
                    .setMaxBend(1.4 * Math.PI / 6)
                    .setMinBend(1 * Math.PI / 6)
                    .setMaxPitch(Math.PI / 1.5)
                    .setMinPitch(0)
                    .setFractalLeaves(new FractalLeaves().setRadius(5, 1, 5).setMaterial(Material.DARK_OAK_LEAVES));
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
                    .setMaxPitch(Math.PI / 1.5)
                    .setMinPitch(0)
                    .setFractalLeaves(new FractalLeaves().setRadius(6, 2, 6)
                                                         .setMaterial(Material.DARK_OAK_LEAVES)
                                                         .setOffsetY(1));
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
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 2, 4).setMaterial(Material.ICE));
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
                    .setFractalLeaves(new FractalLeaves().setRadius(4, 1, 4).setMaterial(Material.ICE));
                break;
            case FIRE_CORAL:
                this.setBaseHeight(2)
                    .setInitialTilt(Math.PI / 2)
                    .setBaseThickness(1)
                    .setThicknessDecrement(0)
                    .setMaxDepth(3)
                    .setFractalLeaves(new FractalLeaves().setRadius(1, 4, 1).setMaterial(Material.FIRE_CORAL_BLOCK))
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
                    .setFractalLeaves(new FractalLeaves().setRadius(3, 1, 3).setMaterial(Material.HORN_CORAL_BLOCK))
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
                    .setFractalLeaves(new FractalLeaves().setRadius(1, 2, 1)
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
                    .setFractalLeaves(new FractalLeaves().setRadius(1, 1, 1)
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
                    .setFractalLeaves(new FractalLeaves().setRadius(3, 3, 3)
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


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkGradient(PopulatorDataAbstract data, int x, int z) {
        heightGradientChecked = true;
        return (HeightMap.getTrueHeightGradient(data, x, z, 3) <= TConfig.c.MISC_TREES_GRADIENT_LIMIT);
    }

    public boolean build(@NotNull TerraformWorld tw, @NotNull SimpleBlock block) {
        return build(tw, block.getPopData(), block.getX(), block.getY(), block.getZ());
    }

    public boolean build(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.areTreesEnabled()) {
            return false;
        }

        fractalLeaves.purgeOccupiedLeavesCache();
        // Terrain too steep, don't attempt tree generation,
        // lest it gets stuck in a wall.
        if (!heightGradientChecked) {
            if (!checkGradient(data, x, z)) {
                return false;
            }
        }

        if (TConfig.c.MISC_TREES_FORCE_LOGS) {
            this.trunkType = Material.getMaterial(StringUtils.replace(this.trunkType.toString(), "WOOD", "LOG"));
        }
        this.oriX = x;
        this.oriY = y;
        this.oriZ = z;
        this.tw = tw;
        this.fractalLeaves.setOriY(oriY);
        this.fractalLeaves.setTw(tw);
        this.fractalLeaves.setMaxHeight(maxHeight);
        // this.noiseGen = new FastNoise((int) tw.getSeed());

        FastNoise noiseGen = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.FRACTALTREES_BASE_NOISE, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(5);
            return n;
        });

        // Setup noise to be used in randomising the sphere
        noiseGen.SetFrequency(branchNoiseFrequency);

        this.rand = tw.getRand(16L * 16 * x + 16L * y + z);
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        if (this.top == null) {
            top = base;
        }
        initialAngle = Math.PI / 2 + GenUtils.randDouble(rand, -initialTilt, initialTilt);

        if (alwaysOneStraightBranchLength == 0) {
            alwaysOneStraightBranchLength = baseHeight;
        }

        double initialPitch;
        if (minInitialTilt != -1) {
            initialPitch = new int[] {-1, 1}[rand.nextInt(2)] * GenUtils.randDouble(rand, minInitialTilt, initialTilt);
        }
        else {
            initialPitch = GenUtils.randDouble(rand, -initialTilt, initialTilt);
        }


        if (alwaysOneStraight > 0) {
            // Starting Trunk
            fractalBranch(rand, base, initialAngle, initialPitch, 0, baseThickness, baseHeight);
        }
        else {
            initialHeight = baseHeight + GenUtils.randInt(-heightVariation, heightVariation);
            fractalBranch(rand, base, initialAngle, initialPitch, 0, baseThickness, initialHeight);
        }

        if (beeHive != null) {
            for (int i = 0; i < 8; i++) {
                if (!beeHive.isSolid()) {
                    BeeHiveSpawner.spawnFullBeeNest(beeHive);
                    // TerraformGeneratorPlugin.logger.debug("Bee nest spawned at " + two.getRelative(0,-i,0).getCoords());
                    break;
                }
                else {
                    beeHive = beeHive.getDown();
                }
            }
        }

        return true;

    }

    private void fractalBranch(@NotNull Random rand,
                               @NotNull SimpleBlock base,
                               double pitch,
                               double yaw,
                               int depth,
                               double thickness,
                               double size)
    {

        if (thickness < minThickness) {
            thickness = minThickness;
        }

        if (pitch > maxPitch) {
            // reset pitch
            pitch = maxPitch - rta();
        }
        else if (pitch < minPitch) {
            pitch = minPitch + rta();
        }

        if (depth >= maxDepth) {
            fractalLeaves.placeLeaves(tw, oriY, maxHeight, base);
            base.setType(trunkType);
            return;
        }
        if (size <= 0) {
            fractalLeaves.placeLeaves(tw, oriY, maxHeight, base);
            base.setType(trunkType);
            return;
        }

        boolean restore = false;
        if (noMainStem && size == initialHeight) {
            restore = true;
            size = 0;
        }

        int y = (int) (Math.round(size * Math.sin(pitch))); // Pitch is vertical tilt
        int x = (int) (Math.round(size * Math.cos(pitch) * Math.sin(yaw)));
        int z = (int) (Math.round(size * Math.cos(pitch) * Math.cos(yaw)));

        SimpleBlock two = base.getRelative(x, y, z);
        if (two.getY() > top.getY()) {
            top = two;
        }

        // Set height
        if (two.getY() - oriY > height) {
            height = two.getY() - oriY;
        }

        if (restore) {
            two = base;
            size = baseHeight;
        }

        drawLine(base, two, (int) (size), thickness);


        if (beeHive == null && Version.isAtLeast(15.1) && GenUtils.chance(rand, (int) (beeChance * 1000.0), 1000)) {
            for (int i = 0; i < 3; i++) {
                if (!two.getRelative(0, -i, 0).isSolid()) {
                    beeHive = two.getRelative(0, -i, 0);
                    break;
                    // TerraformGeneratorPlugin.logger.debug("Bee nest spawned at " + two.getRelative(0,-i,0).getCoords());
                }
            }

        }

        fractalsDone++;

        if (fractalsDone % fractalThreshold != 0 && thickness >= 1 && size >= 1) {
            // Make 1 branch
            fractalBranch(rand,
                    two,
                    pitch - randomAngle(depth),
                    yaw + GenUtils.randInt(rand, 1, 5) * GenUtils.getSign(rand) * rta(),
                    depth,
                    thickness,
                    size
            );
            return;
        }

        // This indicates a branch as the initial is no longer the same.
        // For always straight trees, the branches don't have additional fractals.
        // Set depth to 99 and force leaf generation.
        if (alwaysOneStraight > 0 && pitch != initialAngle) {
            fractalBranch(
                    rand,
                    two,
                    pitch - randomAngle(depth),
                    yaw - rta(),
                    99,
                    thickness - thicknessDecrement,
                    size - lengthDecrement
            );
            return;
        }

        if (alwaysOneStraight > 0) {
            alwaysOneStraightBranchLength -= (int) this.lengthDecrement;
            this.lengthDecrement *= this.lengthDecrementMultiplier;
            // Extend a central trunk and make more branches.

            // Only spawn branches if depth is sufficient.
            if (depth >= alwaysOneStraightBranchSpawningDepth) {
                fractalBranch(
                        rand,
                        two,
                        pitch + randomAngle(depth),
                        -ra(Math.PI / 4,
                                alwaysOneStraightBranchYawLowerMultiplier,
                                alwaysOneStraightBranchYawUpperMultiplier
                        ),
                        depth + 1,
                        thickness - thicknessDecrement,
                        alwaysOneStraightBranchLength
                );
                fractalBranch(
                        rand,
                        two,
                        pitch + randomAngle(depth),
                        ra(Math.PI / 4,
                                alwaysOneStraightBranchYawLowerMultiplier,
                                alwaysOneStraightBranchYawUpperMultiplier
                        ),
                        depth + 1,
                        thickness - thicknessDecrement,
                        alwaysOneStraightBranchLength
                );
                fractalBranch(
                        rand,
                        two,
                        pitch + randomAngle(depth),
                        5 * ra(Math.PI / 4,
                                alwaysOneStraightBranchYawLowerMultiplier,
                                alwaysOneStraightBranchYawUpperMultiplier
                        ),
                        depth + 1,
                        thickness - thicknessDecrement,
                        alwaysOneStraightBranchLength
                );
                fractalBranch(
                        rand,
                        two,
                        pitch + randomAngle(depth),
                        -5 * ra(Math.PI / 4,
                                alwaysOneStraightBranchYawLowerMultiplier,
                                alwaysOneStraightBranchYawUpperMultiplier
                        ),
                        depth + 1,
                        thickness - thicknessDecrement,
                        alwaysOneStraightBranchLength
                );


                // 4 more static angle fractals.
                if (alwaysOneStraightExtendedBranches) {
                    fractalBranch(
                            rand,
                            two,
                            pitch + randomAngle(depth),
                            ra(0, alwaysOneStraightBranchYawLowerMultiplier, alwaysOneStraightBranchYawUpperMultiplier),
                            depth + 1,
                            thickness - thicknessDecrement,
                            alwaysOneStraightBranchLength
                    );
                    fractalBranch(
                            rand,
                            two,
                            pitch + randomAngle(depth),
                            ra(Math.PI / 2,
                                    alwaysOneStraightBranchYawLowerMultiplier,
                                    alwaysOneStraightBranchYawUpperMultiplier
                            ),
                            depth + 1,
                            thickness - thicknessDecrement,
                            alwaysOneStraightBranchLength
                    );
                    fractalBranch(
                            rand,
                            two,
                            pitch + randomAngle(depth),
                            ra(Math.PI,
                                    alwaysOneStraightBranchYawLowerMultiplier,
                                    alwaysOneStraightBranchYawUpperMultiplier
                            ),
                            depth + 1,
                            thickness - thicknessDecrement,
                            alwaysOneStraightBranchLength
                    );
                    fractalBranch(
                            rand,
                            two,
                            pitch + randomAngle(depth),
                            -ra(Math.PI / 2,
                                    alwaysOneStraightBranchYawLowerMultiplier,
                                    alwaysOneStraightBranchYawUpperMultiplier
                            ),
                            depth + 1,
                            thickness - thicknessDecrement,
                            alwaysOneStraightBranchLength
                    );
                }
            }

            // this.logType = Material.SPRUCE_WOOD;
            fractalBranch(rand, two, pitch, yaw, depth + 1, thickness - thicknessDecrement, alwaysOneStraight);
        }
        else {
            // Make 4 branches
            fractalBranch(
                    rand,
                    two,
                    pitch - randomAngle(depth),
                    yaw - rta(),
                    depth + 1,
                    thickness - thicknessDecrement,
                    size - lengthDecrement
            );
            fractalBranch(
                    rand,
                    two,
                    pitch + randomAngle(depth),
                    yaw + rta(),
                    depth + 1,
                    thickness - thicknessDecrement,
                    size - lengthDecrement
            );
            fractalBranch(
                    rand,
                    two,
                    pitch + randomAngle(depth),
                    yaw + 5 * rta(),
                    depth + 1,
                    thickness - thicknessDecrement,
                    size - lengthDecrement
            );
            fractalBranch(
                    rand,
                    two,
                    pitch + randomAngle(depth),
                    yaw - 5 * rta(),
                    depth + 1,
                    thickness - thicknessDecrement,
                    size - lengthDecrement
            );
        }
    }

    private void drawLine(@NotNull SimpleBlock one, @NotNull SimpleBlock two, int segments, double thickness) {
        if (one.equals(two)) {
            return;
        }
        // Vector one to two;
        Vector v = two.toVector().subtract(one.toVector());
        for (int i = 0; i <= segments; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) segments));
            SimpleBlock segment = one.getRelative(seg);
            replaceSphere(((float) thickness) / 2, segment, trunkType);
        }
    }

    private void replaceSphere(float radius, @NotNull SimpleBlock base, @NotNull Material type) {
        if (radius <= 0) {
            return;
        }
        replaceSphere(radius, radius, radius, base, type);
    }

    // private boolean debug = true;
    private void replaceSphere(float rX, float rY, float rZ, @NotNull SimpleBlock block, @NotNull Material type) {

        // Don't place anything if radius is nothing
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }

        // Radius 0.5 is 1 block
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            block.setType(type);
            if (Tag.WALLS.isTagged(type)) {
                BlockUtils.correctMultifacingData(block);
            }
            return;
        }

        float noiseMultiplier = branchNoiseMultiplier;


        FastNoise noiseGen = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.FRACTALTREES_BASE_NOISE, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(5);
            return n;
        });

        // Setup noise to be used in randomising the sphere
        noiseGen.SetFrequency(branchNoiseFrequency);

        ArrayList<SimpleBlock> changed = new ArrayList<>();

        for (float y = -rY; y <= rY; y++) {
            for (float x = -rX; x <= rX; x++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    if (rel.getY() - this.oriY > this.maxHeight) {
                        return;
                    }
                    if (rel.getY() - this.oriY == this.maxHeight) {
                        if (rand.nextBoolean()) // Fade off if too high
                        {
                            return;
                        }
                    }

                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(y, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);

                    if (equationResult <= 1 + noiseMultiplier * noiseGen.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        rel.rsetType(BlockUtils.replacableByTrees, type);
                        if (Tag.WALLS.isTagged(type)) {
                            BlockUtils.correctMultifacingData(rel);
                        }
                        if (coralDecoration) {
                            if (!changed.contains(rel)) {
                                changed.add(rel);
                            }
                        }

                        if (cocoaBeans > 0 && Math.abs(x) >= rX - 2 && Math.abs(z) >= rZ - 2) {
                            // Coca beans
                            if (GenUtils.chance(cocoaBeans, 100)) {
                                for (BlockFace face : BlockUtils.directBlockFaces) {
                                    Directional dir = (Directional) Bukkit.createBlockData(Material.COCOA);
                                    dir.setFacing(face.getOppositeFace());
                                    ((Ageable) dir).setAge(GenUtils.randInt(rand, 0, ((Ageable) dir).getMaximumAge()));
                                    SimpleBlock beans = rel.getRelative(face);
                                    if (beans.isSolid() || beans.getType() == Material.WATER) {
                                        continue;
                                    }

                                    beans.setBlockData(dir);
                                }
                            }

                        }
                        if (vines > 0 && Math.abs(x) >= rX - 2 && Math.abs(z) >= rZ - 2) {
                            if (GenUtils.chance(2, 10)) {
                                dangleLeavesDown(rel, vines / 2, vines);
                            }
                            else if (GenUtils.chance(1, 10))
                            {
                                rel.rsetType(BlockUtils.replacableByTrees, this.trunkType);
                                BlockUtils.vineUp(rel, 4);
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
                sb.setType(trunkType);
            }
        }
    }

    /**
     * For a randomised length downwards, dangle a downward pillar of leaves
     * (up till a solid is hit)
     * <p>
     * On top of the vine dangle, set 1 log, then surround that log with leaves.
     * <p>
     * For some ungodly reason this is in this class instead of the leaf class.
     * That ungodly reason is that the branches run this
     *
     * @param block under which the vine spawns
     * @param min   minimum vine length
     * @param max   maximum vine length
     */
    void dangleLeavesDown(@NotNull SimpleBlock block, int min, int max) {
        Material material = fractalLeaves.material[rand.nextInt(fractalLeaves.material.length)];
        BlockData type = Bukkit.createBlockData(material);
        if (Tag.LEAVES.isTagged(material)) {
            Leaves leaf = (Leaves) type;
            leaf.setDistance(1);
        }
        for (int i = 1; i <= GenUtils.randInt(min, max); i++) {
            if (!block.getRelative(0, -i, 0).isSolid()) {
                block.getRelative(0, -i, 0).rsetBlockData(BlockUtils.replacableByTrees, type);
            }
            else {
                break;
            }
        }

        // Log for good measure, as well as some surrounding leaves.
        if (Tag.LEAVES.isTagged(material)) {
            block.rsetType(BlockUtils.replacableByTrees, this.trunkType);
        }
        for (BlockFace face : BlockUtils.directBlockFaces) {
            material = fractalLeaves.material[rand.nextInt(fractalLeaves.material.length)];
            type = Bukkit.createBlockData(material);
            if (Tag.LEAVES.isTagged(material)) {
                Leaves leaf = (Leaves) type;
                leaf.setDistance(1);
            }

            block.getRelative(face).rsetBlockData(BlockUtils.replacableByTrees, type);
        }
        block.getUp().rsetBlockData(BlockUtils.replacableByTrees, type);
    }

    public @NotNull FractalTreeBuilder setSnowyLeaves(boolean snowy) {
        this.fractalLeaves.setSnowy(snowy);
        return this;
    }

    private @NotNull FractalTreeBuilder setVines(int vines) {
        this.vines = vines;
        return this;
    }

    private @NotNull FractalTreeBuilder setHeightVariation(int heightVariation) {
        this.heightVariation = heightVariation;
        return this;
    }

    private @NotNull FractalTreeBuilder setMinBend(double bend) {
        this.minBend = bend;
        return this;
    }

    private @NotNull FractalTreeBuilder setMaxBend(double bend) {
        this.maxBend = bend;
        return this;
    }

    private @NotNull FractalTreeBuilder setInitialTilt(double initialTilt) {
        this.initialTilt = initialTilt;
        return this;
    }

    private @NotNull FractalTreeBuilder setMinInitialTilt(double minInitialTilt) {
        this.minInitialTilt = minInitialTilt;
        return this;
    }

    public @NotNull FractalTreeBuilder setFractalLeaves(FractalLeaves fractalLeaves) {
        this.fractalLeaves = fractalLeaves;
        return this;
    }

    public @NotNull FractalTreeBuilder setTrunkType(Material log) {
        this.trunkType = log;
        return this;
    }

    private @NotNull FractalTreeBuilder setLengthDecrement(float d) {
        this.lengthDecrement = d;
        return this;
    }

    private @NotNull FractalTreeBuilder setMaxDepth(int d) {
        this.maxDepth = d;
        return this;
    }

    private @NotNull FractalTreeBuilder setAlwaysOneStraight(int val) {
        this.alwaysOneStraight = val;
        return this;
    }

    private @NotNull FractalTreeBuilder setAlwaysOneStraightExtendedBranches(boolean bool) {
        this.alwaysOneStraightExtendedBranches = bool;
        return this;
    }

    private @NotNull FractalTreeBuilder setNoMainStem(boolean bool) {
        this.noMainStem = bool;
        return this;
    }

    /**
     * @param beeChance the beeChance to set
     */
    private @NotNull FractalTreeBuilder setBeeChance(double beeChance) {
        this.beeChance = beeChance;
        return this;
    }

    /**
     * @return the cocabeans
     */
    private int getCocoaBeans() {
        return cocoaBeans;
    }

    /**
     * @param cocoaBeans the cocabeans to set
     */
    private @NotNull FractalTreeBuilder setCocoaBeans(int cocoaBeans) {
        this.cocoaBeans = cocoaBeans;
        return this;
    }

    private @NotNull FractalTreeBuilder setThicknessDecrement(float d) {
        this.thicknessDecrement = d;
        return this;
    }

    private @NotNull FractalTreeBuilder setBaseThickness(float baseThickness) {
        this.baseThickness = baseThickness;
        return this;
    }

    private @NotNull FractalTreeBuilder setMinThickness(float minThickness) {
        this.minThickness = minThickness;
        return this;
    }

    private @NotNull FractalTreeBuilder setBaseHeight(int h) {
        this.baseHeight = h;
        return this;
    }

    private @NotNull FractalTreeBuilder setFractalThreshold(int i) {
        this.fractalThreshold = i;
        return this;
    }

    private @NotNull FractalTreeBuilder setMaxPitch(double max) {
        this.maxPitch = max;
        return this;
    }

    private @NotNull FractalTreeBuilder setBranchNoiseMultiplier(float multiplier) {
        this.branchNoiseMultiplier = multiplier;
        return this;
    }

    private @NotNull FractalTreeBuilder setDepthPitchMultiplier(float depthPitchMultiplier) {
        this.depthPitchMultiplier = depthPitchMultiplier;
        return this;
    }

    private @NotNull FractalTreeBuilder setLeafBranchFrequency(float freq) {
        this.branchNoiseFrequency = freq;
        return this;
    }

    public @NotNull FractalTreeBuilder setMaxHeight(int max) {
        this.maxHeight = max;
        return this;
    }

    public @NotNull FractalTreeBuilder skipGradientCheck() {
        this.heightGradientChecked = true;
        return this;
    }

    /**
     * @return the height
     */
    private int getHeight() {
        return height;
    }

    private @NotNull FractalTreeBuilder setMinPitch(double min) {
        this.minPitch = min;
        return this;
    }

    /**
     * Random angle defined by the min and max bend angles
     */
    private double randomAngle(int depth) {
        return (Math.pow(depthPitchMultiplier, depth)) * GenUtils.randDouble(rand, minBend, maxBend);
    }

    /**
     * Random-thirty-ish-angle
     *
     * @return An angle between 0.8*30 to 1.2*30 degrees in radians
     */
    private double rta() {
        return GenUtils.randDouble(new Random(), 0.8 * Math.PI / 6, 1.2 * Math.PI / 6);
    }

    /**
     * Random-angle
     *
     * @return An angle between lowerBound*base to upperBound*base degrees in radians
     */
    private double ra(double base, double lowerBound, double upperBound) {
        return GenUtils.randDouble(new Random(), lowerBound * base, upperBound * base);
    }

    private @NotNull FractalTreeBuilder setCoralDecoration(boolean d) {
        this.coralDecoration = d;
        this.fractalLeaves.coralDecoration = d;
        return this;
    }

    private @NotNull FractalTreeBuilder setAlwaysOneStraightBranchLength(int alwaysOneStraightBranchLength) {
        this.alwaysOneStraightBranchLength = alwaysOneStraightBranchLength;
        return this;
    }

    private @NotNull FractalTreeBuilder setLengthDecrementMultiplier(float lengthDecrementMultiplier) {
        this.lengthDecrementMultiplier = lengthDecrementMultiplier;
        return this;
    }

    private @NotNull FractalTreeBuilder setAlwaysOneStraightBranchYawLowerMultiplier(double alwaysOneStraightBranchYawLowerMultiplier) {
        this.alwaysOneStraightBranchYawLowerMultiplier = alwaysOneStraightBranchYawLowerMultiplier;
        return this;
    }

    private @NotNull FractalTreeBuilder setAlwaysOneStraightBranchYawUpperMultiplier(double alwaysOneStraightBranchYawUpperMultiplier) {
        this.alwaysOneStraightBranchYawUpperMultiplier = alwaysOneStraightBranchYawUpperMultiplier;
        return this;
    }

    private @NotNull FractalTreeBuilder setAlwaysOneStraightBranchSpawningDepth(int alwaysOneStraightBranchSpawningDepth) {
        this.alwaysOneStraightBranchSpawningDepth = alwaysOneStraightBranchSpawningDepth;
        return this;
    }
}
