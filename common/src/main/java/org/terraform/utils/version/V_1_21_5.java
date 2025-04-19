package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class V_1_21_5 {
    public static Material BUSH = Version.isAtLeast(21.5) ?
                                       Material.valueOf("BUSH") : Material.GRASS;
    public static Material FIREFLY_BUSH = Version.isAtLeast(21.5) ?
                                  Material.valueOf("FIREFLY_BUSH") : Material.GRASS;
    public static Material CACTUS_FLOWER = Version.isAtLeast(21.5) ?
                                           Material.valueOf("CACTUS_FLOWER") : Material.CACTUS;
    public static Material SHORT_DRY_GRASS = Version.isAtLeast(21.5) ?
                                           Material.valueOf("SHORT_DRY_GRASS") : Material.DEAD_BUSH;
    public static Material TALL_DRY_GRASS = Version.isAtLeast(21.5) ?
                                            Material.valueOf("TALL_DRY_GRASS") : Material.DEAD_BUSH;
    public static Material LEAF_LITTER = Version.isAtLeast(21.5) ?
                                            Material.valueOf("LEAF_LITTER") : Material.GRASS;
    public static Material WILDFLOWERS = Version.isAtLeast(21.5) ?
                                            Material.valueOf("WILDFLOWERS") : Material.DANDELION;
    private static BlockData[] leafLitters;

    private static BlockData[] wildflowerSet;

    public static void leafLitter(Random random, PopulatorDataAbstract data, int x, int y, int z){
        if(!Version.isAtLeast(21.5)) return;
        if(!TConfig.c.FEATURE_PLANTS_ENABLED) return;
        if(data.getType(x,y,z) != Material.AIR) return;
        if(leafLitters == null)
            leafLitters = new BlockData[]{
                    Bukkit.createBlockData("leaf_litter[segment_amount=1]"),
                    Bukkit.createBlockData("leaf_litter[segment_amount=2]"),
                    Bukkit.createBlockData("leaf_litter[segment_amount=3]"),
                    Bukkit.createBlockData("leaf_litter[segment_amount=4]")
            };
        Directional d = (Directional) leafLitters[random.nextInt(leafLitters.length)];
        d.setFacing(BlockUtils.getDirectBlockFace(random));
        data.setBlockData(x,y,z,d);
    }

    public static void wildflowers(Random random, PopulatorDataAbstract data, int x, int y, int z){
        if(!Version.isAtLeast(21.5)) return;
        if(!TConfig.c.FEATURE_PLANTS_ENABLED) return;
        if(data.getType(x,y,z) != Material.AIR) return;
        if(wildflowerSet == null)
            wildflowerSet = new BlockData[]{
                    Bukkit.createBlockData("wildflowers[flower_amount=1]"),
                    Bukkit.createBlockData("wildflowers[flower_amount=2]"),
                    Bukkit.createBlockData("wildflowers[flower_amount=3]"),
                    Bukkit.createBlockData("wildflowers[flower_amount=4]")
            };
        Directional d = (Directional) wildflowerSet[random.nextInt(wildflowerSet.length)];
        d.setFacing(BlockUtils.getDirectBlockFace(random));
        data.setBlockData(x,y,z,d);
    }
}
