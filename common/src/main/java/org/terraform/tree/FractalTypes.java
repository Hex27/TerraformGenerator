package org.terraform.tree;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.V_1_21_5;
import org.terraform.utils.version.Version;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class FractalTypes {
    public enum Tree {
        FOREST(
                // Medium Forest Tree
                new NewFractalTreeBuilder().setLengthVariance(2)
                                           .setOriginalTrunkLength(12)
                                           .setInitialBranchRadius(1.6f)
                                           .setMinBranchHorizontalComponent(0.7)
                                           .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth
                                                                                                   * (1f
                                                                                                      - branchRatio
                                                                                                        / 1.7f))
                                           .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                   currentBranchLength
                                                   / 1.7f)
                                           .setBranchSpawnChance(0.15)
                                           .setCrownBranches(4)
                                           .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 3)
                                                                                .setRadius(4f)
                                                                                .setRadiusY(2f))
                                           .setSpawnBees(true)
                                           .setPrePlacement((random, block) -> {
                                               leafLitter(random, block, 6f);
                                           }),
                // Large Forest Tree
                new NewFractalTreeBuilder().setOriginalTrunkLength(18)
                                           .setInitialBranchRadius(2f)
                                           .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth
                                                                                                   * (1f
                                                                                                      - branchRatio
                                                                                                        / 2f))
                                           .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                   currentBranchLength
                                                   - 7)
                                           .setBranchSpawnChance(0.2)
                                           .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 2)
                                                                                .setRadius(4f)
                                                                                .setRadiusY(2.5f))
                                           .setSpawnBees(true)
                                           .setPrePlacement((random, block) -> {
                                               leafLitter(random, block, 6f);
                                           }),
                //            // Original Style Tree
                new NewFractalTreeBuilder().setOriginalTrunkLength(18)
                                           .setInitialBranchRadius(2f)
                                           .setCrownBranches(5)
                                           .setMaxDepth(2)
                                           .setMinBranchHorizontalComponent(0.7)
                                           .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth
                                                                                                   * (1f
                                                                                                      - branchRatio
                                                                                                        / 1.5f))
                                           .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                   currentBranchLength
                                                   / 2f)
                                           .setBranchSpawnChance(0.2)
                                           .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 1)
                                                                                .setRadius(4f)
                                                                                .setRadiusY(2.5f))
                                           .setSpawnBees(true)
                                           .setPrePlacement((random, block) -> {
                                               leafLitter(random, block, 6f);
                                           })
        ),
        NORMAL_SMALL(new NewFractalTreeBuilder().setTreeRootThreshold(0)
                                                .setOriginalTrunkLength(4)
                                                .setLengthVariance(1)
                                                .setMaxDepth(1)
                                                .setCrownBranches(3)
                                                .setInitialBranchRadius(0.8f)
                                                .setNoisePriority(0.05f)
                                                .setFirstEnd(1f)
                                                .setMinBranchHorizontalComponent(1.2)
                                                .setMaxBranchHorizontalComponent(2)
                                                .setMaxInitialNormalDelta(0)
                                                .setMinInitialNormalDelta(0)
                                                .setGetBranchWidth((initialBranchWidth, branchRatio) ->
                                                        initialBranchWidth
                                                        - (branchRatio * 0.2f))
                                                .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                        currentBranchLength
                                                        - 2)
                                                .setMinBranchSpawnLength(0.8f)
                                                .setBranchSpawnChance(0.1)
                                                .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 1)
                                                                                     .setRadius(3f)
                                                                                     .setRadiusY(3f)
                                                                                     .setLeafNoiseFrequency(0.5f)
                                                                                     .setSemiSphereLeaves(true)
                                                                                     .setMaterial(Material.OAK_LEAVES))
                                                .setPrePlacement((random, block) -> {
                                                    leafLitter(random, block, 3.5f);
                                                })),
        AZALEA_TOP(new NewFractalTreeBuilder().setTreeRootThreshold(0)
                                              .setOriginalTrunkLength(6)
                                              .setLengthVariance(1)
                                              .setMaxDepth(1)
                                              .setCrownBranches(3)
                                              .setInitialBranchRadius(0.8f)
                                              .setNoisePriority(0.05f)
                                              .setFirstEnd(1f)
                                              .setMinBranchHorizontalComponent(1.2)
                                              .setMaxBranchHorizontalComponent(2)
                                              .setMaxInitialNormalDelta(0)
                                              .setMinInitialNormalDelta(0)
                                              .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth
                                                                                                      - (branchRatio
                                                                                                         * 0.2f))
                                              .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                      currentBranchLength
                                                      - 2)
                                              .setMinBranchSpawnLength(0.8f)
                                              .setBranchSpawnChance(0.1)
                                              .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.5f, 2)
                                                                                   .setRadius(4f)
                                                                                   .setRadiusY(1.5f)
                                                                                   .setLeafNoiseFrequency(0.2f)
                                                                                   .setMaterial(
                                                                                           Material.AZALEA_LEAVES,
                                                                                           Material.FLOWERING_AZALEA_LEAVES
                                                                                   ))),
        TAIGA_BIG(new NewFractalTreeBuilder().setFirstEnd(1f)
                                             .setTreeRootThreshold(2)
                                             .setTreeRootMultiplier(1.3f)
                                             .setBranchMaterial(Material.SPRUCE_LOG)
                                             .setRootMaterial(Material.SPRUCE_WOOD)
                                             .setOriginalTrunkLength(30)
                                             .setLengthVariance(2)
                                             .setInitialBranchRadius(1.8f)
                                             // .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth/2)
                                             .setBranchDecrement((currentBranchLength, totalTreeHeight) -> {
                                                 if (currentBranchLength < 10) {
                                                     return 0f;
                                                 }
                                                 return 0.3f * Math.max(0, 30 - totalTreeHeight);
                                             })
                                             .setCrownBranches(0)
                                             .setMaxDepth(3)
                                             .setMaxInitialNormalDelta(0.1)
                                             .setMinInitialNormalDelta(-0.1)
                                             .setMinBranchHorizontalComponent(1.5)
                                             .setMaxBranchHorizontalComponent(2.0)
                                             .setBranchSpawnChance(1.0)
                                             .setRandomBranchSpawnCooldown(4)
                                             .setRandomBranchClusterCount(5)
                                             .setRandomBranchSegmentCount(5)
                                             .setMinBranchSpawnLength(0.3f)
                                             .setLeafSpawnDepth(0)
                                             .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.4f, 2)
                                                                                  .setConeLeaves(true)
                                                                                  .setRadius(1.5f)
                                                                                  .setRadiusY(2.3f)
                                                                                  .setLeafNoiseFrequency(0.3f)
                                                                                  .setMaterial(Material.SPRUCE_LEAVES))
                                             .setPrePlacement((random, block) -> BlockUtils.lambdaCircularPatch(random.nextInt(
                                                     132798), 5f, block, (b) -> {
                                                 if (BlockUtils.isDirtLike(b.getType())) {
                                                     b.setType(Material.PODZOL);
                                                 }
                                             }))),
        TAIGA_SMALL(new NewFractalTreeBuilder().setTreeRootThreshold(0)
                                               .setBranchMaterial(Material.SPRUCE_LOG)
                                               .setOriginalTrunkLength(16)
                                               .setLengthVariance(1)
                                               .setInitialBranchRadius(0.8f)
                                               .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth)
                                               .setBranchDecrement((currentBranchLength, totalTreeHeight) -> {
                                                   if (currentBranchLength < 10) {
                                                       return 0f;
                                                   }
                                                   return Math.min(4, 0.5f * Math.max(0, 12 - totalTreeHeight));
                                               })
                                               .setCrownBranches(0)
                                               .setMaxDepth(3)
                                               .setMaxInitialNormalDelta(0.1)
                                               .setMinInitialNormalDelta(-0.1)
                                               .setMinBranchHorizontalComponent(1.5)
                                               .setMaxBranchHorizontalComponent(2.0)
                                               .setBranchSpawnChance(1.0)
                                               .setRandomBranchSpawnCooldown(4)
                                               .setRandomBranchClusterCount(4)
                                               .setRandomBranchSegmentCount(4)
                                               .setMinBranchSpawnLength(0.2f)
                                               .setLeafSpawnDepth(0)
                                               .setRootMaterial(Material.SPRUCE_WOOD)
                                               .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.4f, 1)
                                                                                    .setConeLeaves(true)
                                                                                    .setRadius(1.3f)
                                                                                    .setRadiusY(2f)
                                                                                    .setMaterial(Material.SPRUCE_LEAVES))
                                               .setPrePlacement((random, block) -> BlockUtils.lambdaCircularPatch(random.nextInt(
                                                       132798), 3.5f, block, (b) -> {
                                                   if (BlockUtils.isDirtLike(b.getType())) {
                                                       b.setType(Material.PODZOL);
                                                   }
                                               })),
                new NewFractalTreeBuilder().setTreeRootThreshold(0)
                                           .setBranchMaterial(Material.SPRUCE_LOG)
                                           .setOriginalTrunkLength(18)
                                           .setLengthVariance(3)
                                           .setInitialBranchRadius(0.8f)
                                           .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth)
                                           .setBranchDecrement((currentBranchLength, totalTreeHeight) -> {
                                               if (currentBranchLength < 10) {
                                                   return 0f;
                                               }
                                               return Math.min(4, 0.5f * Math.max(0, 12 - totalTreeHeight));
                                           })
                                           .setCrownBranches(0)
                                           .setMaxDepth(3)
                                           .setMaxInitialNormalDelta(0)
                                           .setMinInitialNormalDelta(0)
                                           .setMinBranchHorizontalComponent(1.5)
                                           .setMaxBranchHorizontalComponent(2.0)
                                           .setBranchSpawnChance(0.7)
                                           .setRandomBranchSpawnCooldown(2)
                                           .setRandomBranchClusterCount(4)
                                           .setRandomBranchSegmentCount(4)
                                           .setMinBranchSpawnLength(0.4f)
                                           .setLeafSpawnDepth(0)
                                           .setRootMaterial(Material.SPRUCE_WOOD)
                                           .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.4f, 1)
                                                                                .setConeLeaves(true)
                                                                                .setRadius(1.3f)
                                                                                .setRadiusY(3f)
                                                                                .setMaterial(Material.SPRUCE_LEAVES))
                                           .setPrePlacement((random, block) -> BlockUtils.lambdaCircularPatch(random.nextInt(
                                                   132798), 3.5f, block, (b) -> {
                                               if (BlockUtils.isDirtLike(b.getType())) {
                                                   b.setType(Material.PODZOL);
                                               }
                                           }))
        ),
        SCARLET_BIG,
        SCARLET_SMALL,
        SAVANNA_SMALL,
        SAVANNA_BIG,
        WASTELAND_BIG,
        SWAMP_TOP(new NewFractalTreeBuilder().setOriginalTrunkLength(13)
                                             .setLengthVariance(2)
                                             .setMaxDepth(4)
                                             .setInitialBranchRadius(2f)
                                             .setGetBranchWidth((initialBranchWidth, branchRatio) -> initialBranchWidth
                                                                                                     * (1f
                                                                                                        - branchRatio
                                                                                                          / 2f))
                                             .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                     currentBranchLength
                                                     / 1.7f)
                                             .setMinBranchHorizontalComponent(0.9f)
                                             .setMaxBranchHorizontalComponent(1.3f)
                                             .setBranchSpawnChance(0.2)
                                             .setBranchMaterial(V_1_19.MANGROVE_LOG)
                                             .setRootMaterial(V_1_19.MANGROVE_ROOTS)
                                             .setTreeRootMultiplier(2f)
                                             .setTreeRootThreshold(3)
                                             .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.4f, 2)
                                                                                  .setRadius(4f)
                                                                                  .setRadiusY(1.5f)
                                                                                  .setMaterial(V_1_19.MANGROVE_LEAVES)
                                                                                  .setMangrovePropagules(true))),
        BIRCH_BIG,
        BIRCH_SMALL,
        CHERRY_SMALL,
        CHERRY_THICK,
        JUNGLE_BIG,
        JUNGLE_SMALL,
        JUNGLE_EXTRA_SMALL,
        COCONUT_TOP,
        DARK_OAK_SMALL(new NewFractalTreeBuilder().setOriginalTrunkLength(7)
                                                  .setLengthVariance(1)
                                                  .setInitialBranchRadius(2.7f)
                                                  .setCrownBranches(3)
                                                  .setMinBranchSpawnLength(0.2f)
                                                  .setMaxDepth(4)
                                                  .setBranchSpawnChance(0f)
                                                  .setMinBranchHorizontalComponent(0.5f)
                                                  .setMaxBranchHorizontalComponent(0.9f)
                                                  .setGetBranchWidth((initialBranchWidth, branchRatio) ->
                                                          initialBranchWidth
                                                          * (1f - branchRatio / 2f))
                                                  .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                          currentBranchLength
                                                          - 1)
                                                  .setBranchSpawnChance(0.05)
                                                  .setTreeRootMultiplier(1.3f)
                                                  .setTreeRootThreshold(3)
                                                  .setRootMaterial(Material.DARK_OAK_WOOD)
                                                  .setBranchMaterial(Material.DARK_OAK_LOG)
                                                  .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 1)
                                                                                       .setRadius(4.5f)
                                                                                       .setRadiusY(2.5f)
                                                                                       .setMaterial(Material.DARK_OAK_LEAVES))),
        DARK_OAK_BIG_TOP(new NewFractalTreeBuilder().setOriginalTrunkLength(12)
                                                    .setLengthVariance(1)
                                                    .setInitialBranchRadius(2.7f)
                                                    .setCrownBranches(3)
                                                    .setMinBranchSpawnLength(0.2f)
                                                    .setMaxDepth(3)
                                                    .setBranchSpawnChance(0f)
                                                    .setMinBranchHorizontalComponent(-1.2)
                                                    .setMaxBranchHorizontalComponent(1.2f)
                                                    .setGetBranchWidth((initialBranchWidth, branchRatio) ->
                                                            initialBranchWidth
                                                            * (1f - branchRatio / 3f))
                                                    .setBranchDecrement((currentBranchLength, totalTreeHeight) ->
                                                            currentBranchLength
                                                            - 0.5f)
                                                    .setRandomBranchClusterCount(3)
                                                    .setBranchSpawnChance(0.05)
                                                    .setTreeRootMultiplier(1.6f)
                                                    .setTreeRootThreshold(5)
                                                    .setRootMaterial(Material.DARK_OAK_WOOD)
                                                    .setBranchMaterial(Material.DARK_OAK_LOG)
                                                    .setFractalLeaves(new FractalLeaves().setWeepingLeaves(0.3f, 2)
                                                                                         .setRadius(4f)
                                                                                         .setRadiusY(2.5f)
                                                                                         .setMaterial(Material.DARK_OAK_LEAVES))),
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
        DIORITE_PETRIFIED_SMALL;

        private final NewFractalTreeBuilder[] builders;

        Tree() {
            builders = new NewFractalTreeBuilder[] {};
        }

        Tree(NewFractalTreeBuilder... builder) {
            this.builders = builder;
        }

        public boolean build(@NotNull TerraformWorld tw, @NotNull SimpleBlock base)
        {
            return build(tw, base, null);
        }

        // Use of treeMutator is currently not optimal as it makes a copy before every use
        // No idea how bad that is.
        public boolean build(@NotNull TerraformWorld tw,
                             @NotNull SimpleBlock base,
                             @Nullable Consumer<NewFractalTreeBuilder> treeMutator)
        {
            if (builders.length > 0) {
                NewFractalTreeBuilder b = Objects.requireNonNull(GenUtils.choice(tw.getHashedRand(base.getX(),
                        base.getY(),
                        base.getZ()
                ), builders));
                if (treeMutator != null) {
                    try {
                        b = (NewFractalTreeBuilder) b.clone();
                        treeMutator.accept(b);
                    }
                    catch (CloneNotSupportedException e) {
                        // good luck m8
                        TerraformGeneratorPlugin.logger.stackTrace(e);
                        return b.build(tw, base);
                    }
                }
                return b.build(tw, base);
            }
            else {
                return new FractalTreeBuilder(this).build(tw, base);
            }
        }

        private static void leafLitter(Random random, SimpleBlock base, float radius) {
            if (!Version.isAtLeast(21.5)) {
                return;
            }
            BlockUtils.lambdaCircularPatch(random.nextInt(8903245), radius, base, (b) -> {
                if (b.getUp().getType() == Material.AIR
                    && random.nextInt(6) == 0) {
                    V_1_21_5.leafLitter(random, b.getPopData(), b.getX(), b.getY() + 1, b.getZ());
                }
            });
        }
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
        GIANT_RED_MUSHROOM
    }

    public enum MushroomCap {
        ROUND, FLAT, FUNNEL, POINTY
    }
}
