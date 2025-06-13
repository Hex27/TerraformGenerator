package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class TreeDB {
    private static final FractalTypes.Tree[] FRACTAL_CORAL_TYPES = {
            FractalTypes.Tree.FIRE_CORAL,
            FractalTypes.Tree.BRAIN_CORAL,
            FractalTypes.Tree.TUBE_CORAL,
            FractalTypes.Tree.HORN_CORAL,
            FractalTypes.Tree.BUBBLE_CORAL
    };

    /**
     * Spawns an Azalea tree, complete with rooted dirt.
     */
    public static void spawnAzalea(@NotNull Random random,
                                   @NotNull TerraformWorld tw,
                                   @NotNull PopulatorDataAbstract data,
                                   int x,
                                   int y,
                                   int z)
    {
        if (!TConfig.areTreesEnabled()) {
            return;
        }

        FractalTreeBuilder builder = new FractalTreeBuilder(FractalTypes.Tree.AZALEA_TOP);
        builder.build(tw, data, x, y, z);

        SimpleBlock rooter = new SimpleBlock(data, x, y - 1, z);
        rooter.setType(Material.ROOTED_DIRT);
        rooter = rooter.getDown();

        while (!BlockUtils.isAir(rooter.getType())) {
            rooter.setType(Material.ROOTED_DIRT);
            for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                SimpleBlock rel = rooter.getRelative(face);
                if (random.nextBoolean() && BlockUtils.isStoneLike(rel.getType())) {
                    rel.setType(Material.ROOTED_DIRT);
                    if (BlockUtils.isAir(rel.getDown().getType())) {
                        rel.getDown().setType(Material.HANGING_ROOTS);
                    }
                }
            }
            rooter = rooter.getDown();
        }
        rooter.setType(Material.HANGING_ROOTS);
    }

    public static void spawnCoconutTree(@NotNull TerraformWorld tw,
                                        @NotNull PopulatorDataAbstract data,
                                        int x,
                                        int y,
                                        int z)
    {
        if (!TConfig.areTreesEnabled()) {
            return;
        }

        SimpleBlock base = new SimpleBlock(data, x, y, z);
        FractalTreeBuilder builder = new FractalTreeBuilder(FractalTypes.Tree.COCONUT_TOP);

        // If gradient too steep, don't try spawning
        if (!builder.checkGradient(data, x, z)) {
            return;
        }

        // Spawn the base
        Material log = Material.JUNGLE_WOOD;
        if (TConfig.c.MISC_TREES_FORCE_LOGS) {
            log = Material.JUNGLE_LOG;
        }
        for (BlockFace face : BlockUtils.directBlockFaces) {
            new Wall(base.getRelative(face), BlockFace.NORTH).downUntilSolid(new Random(), log);
        }
        builder.build(tw, data, x, y, z);
    }

    public static void spawnSmallJungleTree(boolean skipGradientCheck,
                                            @NotNull TerraformWorld tw,
                                            @NotNull PopulatorDataAbstract data,
                                            int x,
                                            int y,
                                            int z)
    {
        if (!TConfig.areTreesEnabled()) {
            return;
        }

        FractalTreeBuilder ftb;
        if (GenUtils.chance(1, 8)) {
            ftb = new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_EXTRA_SMALL);
        }
        else {
            ftb = new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_SMALL);
        }

        if (skipGradientCheck) {
            ftb.skipGradientCheck();
        }
        ftb.build(tw, data, x, y, z);
    }

    /**
     * Corals will always dig 2 blocks deeper first.
     * Grows a random giant coral (fire, tube, etc)
     */
    public static void spawnRandomGiantCoral(@NotNull TerraformWorld tw,
                                             @NotNull PopulatorDataAbstract data,
                                             int x,
                                             int y,
                                             int z)
    {
        FractalTypes.Tree type = FRACTAL_CORAL_TYPES[tw.getHashedRand(x, y, z).nextInt(5)];
        FractalTreeBuilder ftb = new FractalTreeBuilder(type);
        ftb.setMaxHeight(TerraformGenerator.seaLevel - y - 1); // Max height is one below sea level
        ftb.build(tw, data, x, y - 2, z);
    }

    /**
     * Used to create pillars of said material in a randomised circle around
     * a location. Use before spawning the tree.
     * <br>
     * Roots will extend at least a little above sea level
     */
    public static void spawnBreathingRoots(@NotNull TerraformWorld tw, @NotNull SimpleBlock centre, Material type) {
        if (!TConfig.areTreesEnabled()) {
            return;
        }

        Random rand = tw.getHashedRand(centre.getX(), centre.getY(), centre.getZ(), 178782);
        for (int i = 0; i < 4 + rand.nextInt(8); i++) {
            SimpleBlock core = centre.getRelative(GenUtils.getSign(rand) * GenUtils.randInt(4, 8),
                                             0,
                                             GenUtils.getSign(rand) * GenUtils.randInt(4, 8)
                                     )
                                     .getGround()
                                     .getUp();
            //Sometimes these spawn on the sides of mountains in an ugly way.
            if(core.getY() > TerraformGenerator.seaLevel + 2) continue;

            int min = core.getY() < TerraformGenerator.seaLevel ? TerraformGenerator.seaLevel - core.getY() + 1 : 1;
            core.LPillar(min + rand.nextInt(4), type);
        }
    }
}
