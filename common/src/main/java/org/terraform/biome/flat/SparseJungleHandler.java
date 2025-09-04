package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SparseJungleHandler extends JungleHandler {

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SPARSE_JUNGLE;
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        // Almost everything about jungle population is highly disruptive.
        // Only grass spawning remains here. Mushrooms and everything else go to
        // populateLargeItems
        // Generate grass
        if (BlockUtils.isDirtLike(data.getType(rawX, surfaceY, rawZ))) {
            if (BlockUtils.isAir(data.getType(rawX, surfaceY + 1, rawZ))
                && GenUtils.chance(2, 7)) {
                if (random.nextBoolean()) {
                    GenUtils.weightedRandomSmallItem(random, PlantBuilder.GRASS, 5, BlockUtils.pickFlower(), 1)
                            .build(data, rawX, surfaceY + 1, rawZ);
                }
                else {
                    if (BlockUtils.isAir(data.getType(rawX, surfaceY + 2, rawZ))) {
                        PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        SimpleLocation[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 35);

        if (TConfig.c.TREES_JUNGLE_BIG_ENABLED) {
            for (SimpleLocation sLoc : bigTrees) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                    && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
                {
                    new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_BIG).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );
                }
            }
        }


        // Small jungle trees, OR jungle statues
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 15);

        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);

            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                if (GenUtils.chance(random, 1000 - TConfig.c.BIOME_JUNGLE_STATUE_CHANCE, 1000)) {
                    TreeDB.spawnSmallJungleTree(false, tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                }
                else {
                    spawnStatue(random, data, sLoc);
                }
            }
        }

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                //Bushes
                if (GenUtils.chance(1, 95))
                {
                    createBush(data, 0, x, y, z);
                }


                // Generate mushrooms
                if (data.getBiome(x, z) == getBiome() && BlockUtils.isDirtLike(data.getType(x, y, z))) {
                    if (data.getType(x, y + 1, z) == Material.JUNGLE_WOOD
                        && BlockUtils.isAir(data.getType(x, y + 2, z))
                        && GenUtils.chance(2, 9))
                    {
                        PlantBuilder.build(data, x, y + 2, z, PlantBuilder.RED_MUSHROOM, PlantBuilder.BROWN_MUSHROOM);
                    }
                }
            }
        }
    }
}
