package org.terraform.structure.pillager.outpost;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class OutpostLogpile extends RoomPopulatorAbstract {

    private final BiomeBank biome;

    public OutpostLogpile(Random rand, boolean forceSpawn, boolean unique, BiomeBank biome) {
        super(rand, forceSpawn, unique);
        this.biome = biome;
    }

    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        Material fenceMat = WoodUtils.getWoodForBiome(biome, WoodType.FENCE);
        Material planksMat = WoodUtils.getWoodForBiome(biome, WoodType.PLANKS);
        int stackHeight = GenUtils.randInt(rand, 2, 4);
        BlockFace facing = BlockUtils.getDirectBlockFace(rand);
        OrientableBuilder ob = new OrientableBuilder(WoodUtils.getWoodForBiome(
                biome,
                WoodType.LOG
        )).setAxis(BlockUtils.getAxisFromBlockFace(facing));
        Wall core = new Wall(
                new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getGroundOrSeaLevel(),
                facing
        );

        for (int i = 0; i <= stackHeight; i++) {
            for (int width = 0; width < stackHeight - i; width++) {
                int logLength = GenUtils.randInt(rand, 2, 3);
                for (int len = 0; len <= logLength; len++) {
                    ob.apply(core.getRelative(0, i + 1, 0).getLeft(width).getFront(len));
                    ob.apply(core.getRelative(0, i + 1, 0).getLeft(width).getRear(len));

                    ob.apply(core.getRelative(0, i + 1, 0).getRight(width).getFront(len));
                    ob.apply(core.getRelative(0, i + 1, 0).getRight(width).getRear(len));

                    // Place rails
                    if (len == 1) {
                        core.getRelative(0, i + 2, 0).getLeft(width).getFront(len).setType(Material.RAIL);
                        core.getRelative(0, i + 2, 0).getRight(width).getFront(len).setType(Material.RAIL);
                        core.getRelative(0, i + 2, 0).getLeft(width).getRear(len).setType(Material.RAIL);
                        core.getRelative(0, i + 2, 0).getRight(width).getRear(len).setType(Material.RAIL);
                        BlockUtils.correctSurroundingRails(core.getRelative(0, i + 2, 0)
                                                               .getLeft(width)
                                                               .getFront(len)
                                                               .get());
                        BlockUtils.correctSurroundingRails(core.getRelative(0, i + 2, 0)
                                                               .getRight(width)
                                                               .getFront(len)
                                                               .get());
                        BlockUtils.correctSurroundingRails(core.getRelative(0, i + 2, 0)
                                                               .getLeft(width)
                                                               .getRear(len)
                                                               .get());
                        BlockUtils.correctSurroundingRails(core.getRelative(0, i + 2, 0)
                                                               .getRight(width)
                                                               .getRear(len)
                                                               .get());
                        if (i == 0) {
                            new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(BlockUtils.getRight(core.getDirection()))
                                                                               .apply(core.getRelative(0, i + 1, 0)
                                                                                          .getLeft(width + 1)
                                                                                          .getFront(len))
                                                                               .apply(core.getRelative(0, i + 1, 0)
                                                                                          .getLeft(width + 1)
                                                                                          .getRear(len))
                                                                               .setFacing(BlockUtils.getLeft(core.getDirection()))
                                                                               .apply(core.getRelative(0, i + 1, 0)
                                                                                          .getRight(width + 1)
                                                                                          .getFront(len))
                                                                               .apply(core.getRelative(0, i + 1, 0)
                                                                                          .getRight(width + 1)
                                                                                          .getRear(len));
                        }

                    }
                }
            }
        }

        // Replace the top rails with stone slabs, because they're buggy.
        // Fuck rails
        core.getRelative(0, stackHeight + 1, 0).getRelative(facing).setType(Material.COBBLESTONE_SLAB);
        core.getRelative(0, stackHeight + 1, 0)
            .getRelative(facing.getOppositeFace())
            .setType(Material.COBBLESTONE_SLAB);

        for (int nx = -4; nx <= 4; nx++) {
            for (int nz = -4; nz <= 4; nz++) {
                core.getRelative(nx, 0, nz).get().lsetType(planksMat);
                core.getRelative(nx, -1, nz).downUntilSolid(new Random(), fenceMat);
            }
        }

    }


    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}