package org.terraform.structure.catacombs;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class CatacombsPathPopulator extends PathPopulatorAbstract {
    public static final Material[] pathMaterial = new Material[] {
            Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.DRIPSTONE_BLOCK
    };
    private final Random rand;

    public CatacombsPathPopulator(Random rand) {
        this.rand = rand;
    }

    @Override
    public int getPathMaxBend() {
        return 15;
    }

    @Override
    public void populate(@NotNull PathPopulatorData ppd) {
        Wall core = new Wall(ppd.base, ppd.dir);

        // Was populated before.
        if (core.getType() != Material.CAVE_AIR) {
            return;
        }

        Wall floor = core.getDown();
        if (!floor.isSolid()) {
            return; // Don't populate a path if there's no floor
        }

        // Set the base path material to a brownish-dirt texture
        core.setType(pathMaterial);
        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            if (rand.nextBoolean()) {
                core.getRelative(face).setType(pathMaterial);
            }
        }

        //Crossway
        if(ppd.dir == BlockFace.UP) return;

        // Spawn supports
        boolean spawnSupports = true;
        for (BlockFace dir : BlockUtils.getAdjacentFaces(core.getDirection())) {
            Wall relPillar = core.getUp().findDir(dir, 2);
            if (relPillar == null || !relPillar.getDown().isSolid() || !relPillar.getUp().isSolid() || !relPillar.getUp(
                    3).getRelative(dir.getOppositeFace()).isSolid())
            {
                spawnSupports = false;
            }
            else if (core.getDirection().getModX() != 0) {
                if (core.getX() % 5 != 0) {
                    spawnSupports = false;
                }
            }
            else if (core.getDirection().getModZ() != 0) {
                if (core.getZ() % 5 != 0) {
                    spawnSupports = false;
                }
            }

            if (spawnSupports) { // All supports can be a same-ish width
                relPillar.Pillar(3, BlockUtils.stoneBricks);

                relPillar.getUp().setType(Material.CHISELED_STONE_BRICKS);

                // Tiny arch
                new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(dir)
                                                             .setHalf(Half.TOP)
                                                             .apply(relPillar.getUp(2)
                                                                             .getRelative(dir.getOppositeFace()));

                // Small bases at the sides
                new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(core.getDirection().getOppositeFace())
                                                             .apply(relPillar.getFront())
                                                             .setFacing(core.getDirection())
                                                             .apply(relPillar.getRear());
            }
        }


        // Apply wall decorations (skulls, andesite and chests)
        for (int i = 1; i <= 3; i++) {
            for (BlockFace dir : BlockUtils.getAdjacentFaces(core.getDirection())) {
                Wall rel = core.getUp(i).findDir(dir, 3);

                if (rel != null) {
                    // wall texturing
                    if (rand.nextBoolean() && rel.getType() == Material.STONE) {
                        rel.setType(Material.ANDESITE, Material.COBBLESTONE);
                    }

                    // Skulls
                    if (rel.getAtY(core.getY()).distance(core) > 1
                        && BlockUtils.isStoneLike(rel.getType())
                        && rand.nextBoolean())
                    {
                        new DirectionalBuilder(Material.SKELETON_WALL_SKULL).setFacing(dir.getOppositeFace())
                                                                            .apply(rel.getRelative(dir.getOppositeFace()));
                    }

                    // Ground side decorations
                    if (i == 1) {
                        if (GenUtils.chance(rand, 1, 60)) {
                            // Chests
                            if (TConfig.areDecorationsEnabled()) {
                                new ChestBuilder(Material.CHEST).setFacing(dir.getOppositeFace())
                                                                .setLootTable(TerraLootTable.SIMPLE_DUNGEON)
                                                                .apply(rel.getRelative(dir.getOppositeFace()));
                            }
                        }
                        else if (GenUtils.chance(rand, 1, 20)) {
                            // Candles
                            new StairBuilder(
                                    Material.STONE_BRICK_STAIRS,
                                    Material.MOSSY_STONE_BRICK_STAIRS,
                                    Material.COBBLESTONE_STAIRS
                            ).setHalf(Half.TOP).setFacing(dir).apply(rel.getRelative(dir.getOppositeFace()));

                            BlockUtils.placeCandle(
                                    rel.getRelative(dir.getOppositeFace()).getUp(),
                                    GenUtils.randInt(1, 4),
                                    true
                            );
                        }
                    }
                    else if (rel.getRelative(dir.getOppositeFace()).getUp().isSolid() && GenUtils.chance(rand, 1, 10)) {
                        // Cobwebs
                        if (TConfig.areDecorationsEnabled()) {
                            rel.getRelative(dir.getOppositeFace()).setType(Material.COBWEB);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean customCarve(@NotNull SimpleBlock base, BlockFace dir, int pathWidth) {
        Wall core = new Wall(base.getUp(2), dir);
        int seed = 2293 + 5471 * core.getX() + 9817 * core.getY() ^ 2 + 1049 * core.getZ() ^ 3;
        BlockUtils.carveCaveAir(seed, pathWidth, pathWidth, pathWidth, core.get(), false, BlockUtils.badlandsStoneLike);

        return true;
    }

    @Override
    public int getPathWidth() {
        return 2;
    }


}
