package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class TreeDB {
    private static final FractalTypes.Tree[] FRACTAL_TREE_TYPES = {
            FractalTypes.Tree.FIRE_CORAL,
            FractalTypes.Tree.BRAIN_CORAL,
            FractalTypes.Tree.TUBE_CORAL,
            FractalTypes.Tree.HORN_CORAL,
            FractalTypes.Tree.BUBBLE_CORAL
    };

    public static void spawnCoconutTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        //Spawn the base
        Material log = Material.JUNGLE_WOOD;
        if(TConfigOption.MISC_TREES_FORCE_LOGS.getBoolean()) log = Material.JUNGLE_LOG;
        for (BlockFace face : BlockUtils.directBlockFaces) {
            new Wall(base.getRelative(face), BlockFace.NORTH).downUntilSolid(new Random(), log);
        }
        new FractalTreeBuilder(FractalTypes.Tree.COCONUT_TOP).build(tw, data, x, y, z);
    }

    public static void spawnSmallJungleTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        if (GenUtils.chance(1, 8))
            new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_EXTRA_SMALL).build(tw, data, x, y, z);
        else
            new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_SMALL).build(tw, data, x, y, z);
    }

    public static void spawnBigDarkOakTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_BIG_TOP).build(tw, data, x, y, z);
        new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_BIG_BOTTOM).build(tw, data, x, y - 5, z);
    }

    /**
     * Corals will always dig 2 blocks deeper first.
     * Grows a random giant coral (fire, tube, etc)
     */
    public static void spawnRandomGiantCoral(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        FractalTypes.Tree type = FRACTAL_TREE_TYPES[tw.getHashedRand(x, y, z).nextInt(5)];
        FractalTreeBuilder ftb = new FractalTreeBuilder(type);
        ftb.setMaxHeight(TerraformGenerator.seaLevel - y - 1); //Max height is one below sea level
        ftb.build(tw, data, x, y - 2, z);
    }
}
