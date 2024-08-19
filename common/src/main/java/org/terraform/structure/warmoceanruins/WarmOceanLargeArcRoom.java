package org.terraform.structure.warmoceanruins;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map;
import java.util.Random;

public class WarmOceanLargeArcRoom extends WarmOceanBaseRoom {
    public WarmOceanLargeArcRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        BlockFace facing = BlockUtils.getDirectBlockFace(rand);

        // Place a chest somewhere
        Wall chestTarget = new Wall(room.getCenterSimpleBlock(data), facing).getLeft(GenUtils.getSign(rand)
                                                                                     * rand.nextInt(2))
                                                                            .getFront(GenUtils.getSign(rand)
                                                                                      * rand.nextInt(6))
                                                                            .getGround()
                                                                            .getUp();
        new ChestBuilder(Material.CHEST).setFacing(BlockUtils.getDirectBlockFace(rand))
                                        .setLootTable(TerraLootTable.UNDERWATER_RUIN_BIG)
                                        .setWaterlogged(chestTarget.getY()
                                                        <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                        .apply(chestTarget);

        // Create a 3x3 pattern to use in the arc's walls
        Material[] centrePattern = new Material[] {
                GenUtils.randChoice(Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE),
                GenUtils.randChoice(Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE),
                GenUtils.randChoice(Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE),
                };
        Material[] sidePattern = new Material[] {
                GenUtils.randChoice(Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE),
                GenUtils.randChoice(Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE),
                GenUtils.randChoice(Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE),
                };

        // Create another 3x3 slab pattern to overlay
        BlockData[] centreSlabs = new BlockData[3];
        BlockData[] sideSlabs = new BlockData[3];
        for (int i = 0; i < 3; i++) {
            centreSlabs[i] = Bukkit.createBlockData(Material.CUT_SANDSTONE_SLAB);
            if (rand.nextBoolean()) {
                ((Slab) centreSlabs[i]).setType(Slab.Type.TOP);
            }
            if (GenUtils.chance(rand, 1, 4)) {
                centreSlabs[i] = Bukkit.createBlockData(Material.CHISELED_SANDSTONE);
            }
            sideSlabs[i] = Bukkit.createBlockData(Material.CUT_SANDSTONE_SLAB);
            if (rand.nextBoolean()) {
                ((Slab) sideSlabs[i]).setType(Slab.Type.TOP);
            }
        }

        // For both the left and right buttresses
        for (BlockFace pillarDir : BlockUtils.getAdjacentFaces(facing)) {
            SimpleBlock pillarCore = room.getCenterSimpleBlock(data).getRelative(pillarDir, 5);
            // Core pillar
            pillarCore.LPillar(8, Material.CUT_SANDSTONE);

            // Make the under-stairs to smoothly connect the buttress to the top
            for (BlockFace face : BlockUtils.getAdjacentFaces(pillarDir)) {
                new StairBuilder(Material.SANDSTONE_STAIRS).setHalf(Bisected.Half.TOP)
                                                           .setFacing(pillarDir)
                                                           .setWaterlogged(pillarCore.getY() + 6
                                                                           <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                           .apply(pillarCore.getUp(6)
                                                                            .getRelative(face, 2)
                                                                            .getRelative(
                                                                                    pillarDir.getOppositeFace(),
                                                                                    3
                                                                            ))
                                                           .setWaterlogged(pillarCore.getY() + 7
                                                                           <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                           .apply(pillarCore.getUp(7)
                                                                            .getRelative(face, 2)
                                                                            .getRelative(
                                                                                    pillarDir.getOppositeFace(),
                                                                                    4
                                                                            ));

                pillarCore.getUp(7)
                          .getRelative(face, 2)
                          .getRelative(pillarDir.getOppositeFace(), 3)
                          .setType(Material.SMOOTH_SANDSTONE);
            }

            // Make the corners
            for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
                pillarCore.getRelative(face, 3).setType(Material.CUT_SANDSTONE_SLAB);
                pillarCore.getRelative(face, 2).Pillar(8, Material.CUT_SANDSTONE);
                pillarCore.getUp(4).getRelative(face, 2).setType(Material.CHISELED_SANDSTONE);
            }

            // Create the base and central bit of the pillar
            for (int ny : new int[] {0, 4}) {
                for (int nx = -2; nx <= 2; nx++) {
                    for (int nz = -2; nz <= 2; nz++) {
                        pillarCore.getRelative(nx, ny, nz).setType(Material.CHISELED_SANDSTONE);
                        if (ny == 0) {
                            pillarCore.getDown().downLPillar(new Random(), 10 + rand.nextInt(5), Material.SANDSTONE);
                        }
                    }
                }
            }
            // Walls
            for (BlockFace face : BlockUtils.directBlockFaces) {
                for (BlockFace dir : BlockUtils.getAdjacentFaces(face)) {
                    pillarCore.getRelative(face, 3).getRelative(dir).setType(Material.CUT_SANDSTONE_SLAB);

                    new StairBuilder(Material.SANDSTONE_STAIRS).setFacing(dir)
                                                               .setWaterlogged(pillarCore.getY()
                                                                               <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                               .apply(pillarCore.getRelative(face, 3)
                                                                                .getRelative(dir, 2));
                }
                new StairBuilder(Material.SANDSTONE_STAIRS).setFacing(face.getOppositeFace())
                                                           .setWaterlogged(pillarCore.getY()
                                                                           <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                           .apply(pillarCore.getRelative(face, 3));

                // Place the patterns
                for (int up : new int[] {1, 5}) {
                    Wall patternCore = new Wall(pillarCore.getUp(up).getRelative(face, 1), face);
                    for (int i = 0; i < 3; i++) {
                        patternCore.getUp(i).setType(centrePattern[i]);
                        patternCore.getUp(i).getLeft().setType(sidePattern[i]);
                        patternCore.getUp(i).getRight().setType(sidePattern[i]);
                        patternCore.getUp(i).getFront().setBlockData(centreSlabs[i]);
                        patternCore.getUp(i).getFront().getLeft().setBlockData(sideSlabs[i]);
                        patternCore.getUp(i).getFront().getRight().setBlockData(sideSlabs[i]);
                    }
                }

            }
        }

        // Place the upper bridge connecting the two buttresses
        Wall core = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 8, room.getZ()), facing);
        for (int lr = -9; lr <= 9; lr++) {
            for (int fb = -4; fb <= 4; fb++) {
                if (Math.abs(lr) < 9 && Math.abs(fb) < 4) {
                    core.getLeft(lr).getFront(fb).setType(Material.CUT_SANDSTONE);

                    if (Math.abs(lr) < 7 && Math.abs(fb) < 2) {
                        core.getLeft(lr).getFront(fb).getUp(4).setType(Material.CUT_SANDSTONE_SLAB);
                    }
                    else if (Math.abs(lr) == 7 || Math.abs(fb) == 2) {
                        core.getLeft(lr).getFront(fb).getUp(4).setType(Material.CUT_SANDSTONE);
                    }
                    else {
                        new SlabBuilder(Material.CUT_SANDSTONE_SLAB).setType(Slab.Type.TOP)
                                                                    .setWaterlogged(core.getY() + 4
                                                                                    <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                                    .apply(core.getLeft(lr).getFront(fb).getUp(4));
                    }
                }
                else {
                    new SlabBuilder(Material.CUT_SANDSTONE_SLAB).setType(Slab.Type.TOP)
                                                                .setWaterlogged(core.getY()
                                                                                <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                                .apply(core.getLeft(lr).getFront(fb));
                }
            }
        }

        // Place the pattern on the top of the bridge
        CubeRoom topCoreRoom = new CubeRoom(
                facing.getModX() == 0 ? 15 : 5,
                facing.getModZ() == 0 ? 15 : 5,
                15,
                room.getX(),
                room.getY() + 8,
                room.getZ()
        );

        for (Map.Entry<Wall, Integer> entry : topCoreRoom.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                if (i != 0 && i != entry.getValue() - 1) {
                    if (i == 2 || i == 7 || i == 12) {
                        for (int j = 0; j < 3; j++) {
                            w.getFront().getUp(j).setType(centrePattern[j]);
                        }
                    }
                    else {
                        for (int j = 0; j < 3; j++) {
                            w.getFront().getUp(j).setType(sidePattern[j]);
                        }
                    }
                }

                // Decorations in the top of the bridge
                if (i == 0) {
                    w.Pillar(3, Material.CHISELED_SANDSTONE);
                    new StairBuilder(Material.SANDSTONE_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                               .apply(w.getLeft())
                                                               .setHalf(Bisected.Half.TOP)
                                                               .setWaterlogged(w.getY() + 2
                                                                               <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                               .apply(w.getLeft().getUp(2));
                }
                else if (i == entry.getValue() - 1) {
                    w.Pillar(3, Material.CHISELED_SANDSTONE);
                    new StairBuilder(Material.SANDSTONE_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                               .apply(w.getRight())
                                                               .setHalf(Bisected.Half.TOP)
                                                               .setWaterlogged(w.getY() + 2
                                                                               <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                               .apply(w.getRight().getUp(2));
                }
                else if (i == 4 || i == 10) {
                    w.Pillar(3, Material.CHISELED_SANDSTONE);
                    new StairBuilder(Material.SANDSTONE_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                               .apply(w.getRight())
                                                               .setHalf(Bisected.Half.TOP)
                                                               .setWaterlogged(w.getY() + 2
                                                                               <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                               .apply(w.getRight().getUp(2));
                    new StairBuilder(Material.SANDSTONE_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                               .apply(w.getLeft())
                                                               .setHalf(Bisected.Half.TOP)
                                                               .setWaterlogged(w.getY() + 2
                                                                               <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                               .apply(w.getLeft().getUp(2));
                }
                w = w.getLeft();
            }
        }

        // Procedurally create holes
        Wall centre = new Wall(room.getCenterSimpleBlock(data), facing);
        for (int i = 0; i < 1 + rand.nextInt(3); i++) {
            BlockUtils.replaceWaterSphere(
                    i * room.getX() * room.getZ(),
                    GenUtils.randInt(3, 4),
                    centre.getUp(6)
                          .getLeft(GenUtils.getSign(rand) * rand.nextInt(7))
                          .getFront(GenUtils.getSign(rand) * rand.nextInt(3))
            );
        }

        // Drowned
        for (int i = 0; i < 1 + rand.nextInt(3); i++) {
            centre.getUp().addEntity(EntityType.DROWNED);
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() == 25;
    }
}
