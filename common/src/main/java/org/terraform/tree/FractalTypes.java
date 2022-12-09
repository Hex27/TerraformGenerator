package org.terraform.tree;

import org.bukkit.Material;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Objects;

public class FractalTypes {
    public enum Tree {
        FOREST
//                (
//                //Medium Forest Tree
//            new NewFractalTreeBuilder()
//                .setOriginalTrunkLength(12)
//                .setInitialBranchRadius(1.6f)
//                .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth*(1.0f-branchRatio/1.7f))
//                .setBranchDecrement((currentBranchLength, totalTreeHeight) -> currentBranchLength/1.7f)
//                .setBranchSpawnChance(0.15)
//                .setFractalLeaves(new FractalLeaves()
//                        .setWeepingLeaves(0.3f, 3)
//                        .setRadius(4f)
//                        .setRadiusY(2f))
//                .setSpawnBees(true),
//                //Large Forest Tree
//            new NewFractalTreeBuilder()
//                .setOriginalTrunkLength(18)
//                .setInitialBranchRadius(2f)
//                .setGetBranchWidth(
//                        (initialBranchWidth, branchRatio)
//                                -> initialBranchWidth*(1.0f-branchRatio/2f)
//                )
//                .setBranchDecrement(
//                        (currentBranchLength, totalTreeHeight)
//                                -> currentBranchLength-7
//                )
//                .setBranchSpawnChance(0.2)
//                .setFractalLeaves(new FractalLeaves()
//                        .setWeepingLeaves(0.3f, 2)
//                        .setRadius(4f)
//                        .setRadiusY(2.5f))
//                .setSpawnBees(true),
//            //Original Style Tree
//            new NewFractalTreeBuilder()
//                .setOriginalTrunkLength(18)
//                .setInitialBranchRadius(2f)
//                .setCrownBranches(5)
//                .setMaxDepth(2)
//                .setMinBranchHorizontalComponent(0.7)
//                .setGetBranchWidth(
//                        (initialBranchWidth, branchRatio)
//                                -> initialBranchWidth*(1.0f-branchRatio/1.5f)
//                )
//                .setBranchDecrement(
//                                (currentBranchLength, totalTreeHeight)
//                                        -> currentBranchLength/2f
//                )
//                .setBranchSpawnChance(0.2)
//                .setFractalLeaves(new FractalLeaves()
//                        .setWeepingLeaves(0.3f, 1)
//                        .setRadius(4f)
//                        .setRadiusY(2.5f)
//                )
//                .setSpawnBees(true)
//        )
        ,
        NORMAL_SMALL(
                new NewFractalTreeBuilder()
                        .setTreeRootThreshold(1)
                        .setOriginalTrunkLength(6)
                        .setLengthVariance(1)
                        .setMaxDepth(1)
                        .setCrownBranches(3)
                        .setInitialBranchRadius(0.8f)
                        .setNoisePriority(0.05f)
                        .setFirstEnd(1.0f)
                        .setMinBranchHorizontalComponent(1.2)
                        .setMaxBranchHorizontalComponent(2)
                        .setMaxInitialNormalDelta(0)
                        .setMinInitialNormalDelta(0)
                        .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth-(branchRatio*0.2f))
                        .setBranchDecrement((currentBranchLength, totalTreeHeight) -> currentBranchLength-2)
                        .setMinBranchSpawnLength(0.8f)
                        .setBranchSpawnChance(0.1)
                        .setFractalLeaves(new FractalLeaves()
                                .setWeepingLeaves(0.3f, 1)
                                .setRadius(3f)
                                .setRadiusY(1.5f)
                                .setLeafNoiseFrequency(0.2f)
                                .setMaterial(Material.OAK_LEAVES))
        ),
        AZALEA_TOP
                (
                new NewFractalTreeBuilder()
                        .setTreeRootThreshold(1)
                        .setOriginalTrunkLength(6)
                        .setLengthVariance(1)
                        .setMaxDepth(1)
                        .setCrownBranches(3)
                        .setInitialBranchRadius(0.8f)
                        .setNoisePriority(0.05f)
                        .setFirstEnd(1.0f)
                        .setMinBranchHorizontalComponent(1.2)
                        .setMaxBranchHorizontalComponent(2)
                        .setMaxInitialNormalDelta(0)
                        .setMinInitialNormalDelta(0)
                        .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth-(branchRatio*0.2f))
                        .setBranchDecrement((currentBranchLength, totalTreeHeight) -> currentBranchLength-2)
                        .setMinBranchSpawnLength(0.8f)
                        .setBranchSpawnChance(0.1)
                        .setFractalLeaves(new FractalLeaves()
                                .setWeepingLeaves(0.5f, 2)
                                .setRadius(4f)
                                .setRadiusY(1.5f)
                                .setLeafNoiseFrequency(0.2f)
                                .setMaterial(OneOneSevenBlockHandler.AZALEA_LEAVES, OneOneSevenBlockHandler.FLOWERING_AZALEA_LEAVES))
        )
        ,
        TAIGA_BIG,
        TAIGA_SMALL
        //TODO: FIX THIS
//                (
//                new NewFractalTreeBuilder()
//                        .setBranchMaterial(Material.SPRUCE_LOG)
//                        .setOriginalTrunkLength(12)
//                        .setLengthVariance(1)
//                        .setInitialBranchRadius(0.8f)
//                        .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth)
//                        .setBranchDecrement((currentBranchLength, totalTreeHeight) -> {
//                            TerraformGeneratorPlugin.logger.info("Length: " + currentBranchLength + "," + totalTreeHeight + " -> " + (currentBranchLength - 8f*(totalTreeHeight)/10f));
//                            return currentBranchLength - 8f*(totalTreeHeight)/10f;
//                        })
//                        .setCrownBranches(0)
//                        .setMaxDepth(2)
//                        .setMinBranchHorizontalComponent(0.7)
//                        .setMaxBranchHorizontalComponent(0.8)
//                        .setBranchSpawnChance(0.7)
//                        .setMinBranchSpawnLength(0.2f)
//                        .setFractalLeaves(new FractalLeaves()
//                                .setWeepingLeaves(0.3f, 1)
//                                .setConeLeaves(true)
//                                .setRadius(1.3f)
//                                .setRadiusY(2f)
//                                .setMaterial(Material.GLASS)
//                        )
//        )
        ,
        SCARLET_BIG,
        SCARLET_SMALL,
        SAVANNA_SMALL,
        SAVANNA_BIG,
        WASTELAND_BIG,
        SWAMP_TOP,
        SWAMP_BOTTOM,
        BIRCH_BIG,
        BIRCH_SMALL,
        CHERRY_SMALL,
        CHERRY_THICK,
        JUNGLE_BIG,
        JUNGLE_SMALL,
        JUNGLE_EXTRA_SMALL,
        COCONUT_TOP,
        DARK_OAK_SMALL,
        DARK_OAK_BIG_TOP,
        DARK_OAK_BIG_BOTTOM,
        FROZEN_TREE_BIG,
        FROZEN_TREE_SMALL,
        FIRE_CORAL,
        HORN_CORAL,
        BRAIN_CORAL,
        TUBE_CORAL,
        BUBBLE_CORAL,
        GIANT_PUMPKIN,
        ANDESITE_PETRIFIED_SMALL,
        GRANITE_PETRIFIED_SMALL,
        DIORITE_PETRIFIED_SMALL
        ;

        private final NewFractalTreeBuilder[] builders;
        Tree(){builders = new NewFractalTreeBuilder[]{};}
        Tree(NewFractalTreeBuilder... builder){this.builders = builder;}

        public void build(TerraformWorld tw, SimpleBlock base){
            if(builders.length > 0)
                Objects.requireNonNull(
                    GenUtils.choice(
                            tw.getHashedRand(base.getX(), base.getY(), base.getZ()),
                            builders)
                    )
                .build(tw,base);
            else
                new FractalTreeBuilder(this).build(tw, base);
        }

        public static final Tree[] VALUES = Tree.values();

    }

    public enum Mushroom {
        TINY_BROWN_MUSHROOM,
        TINY_RED_MUSHROOM,
        SMALL_RED_MUSHROOM,
        SMALL_POINTY_RED_MUSHROOM,
        SMALL_BROWN_MUSHROOM,
        MEDIUM_BROWN_MUSHROOM,
        MEDIUM_RED_MUSHROOM,
        MEDIUM_BROWN_FUNNEL_MUSHROOM,
        GIANT_BROWN_MUSHROOM,
        GIANT_BROWN_FUNNEL_MUSHROOM,
        GIANT_RED_MUSHROOM;

        public static final Mushroom[] VALUES = Mushroom.values();
    }

    public enum MushroomCap {
        ROUND,
        FLAT,
        FUNNEL,
        POINTY;
        public static final MushroomCap[] VALUES = MushroomCap.values();
    }
}
