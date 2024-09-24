package org.terraform.structure.pillager.outpost;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class OutpostTent extends RoomPopulatorAbstract {

    final Material @NotNull [] edgyWools = new Material[] {
            Material.BLACK_WOOL, Material.GRAY_WOOL, Material.BROWN_WOOL, Material.LIGHT_GRAY_WOOL, Material.WHITE_WOOL
    };

    final BiomeBank biome;

    public OutpostTent(Random rand, boolean forceSpawn, boolean unique, BiomeBank biome) {
        super(rand, forceSpawn, unique);
        this.biome = biome;
    }

    private void placeProp(int size, @NotNull SimpleBlock core, @NotNull BlockFace facing, @NotNull Material cloth) {
        Material fenceMat = WoodUtils.getWoodForBiome(biome, WoodType.FENCE);
        new Wall(core).Pillar(size, rand, fenceMat);

        for (BlockFace face : BlockUtils.getAdjacentFaces(facing)) {
            SimpleBlock corner = core.getRelative(face, size - 1);
            corner.getRelative(facing.getOppositeFace()).setType(cloth);
            corner.getRelative(facing.getOppositeFace()).getUp().setType(fenceMat);
            // Place corner. Cache the builder to prevent recalculation from woodutils
            StairBuilder builder = new StairBuilder(WoodUtils.getWoodForBiome(biome, WoodType.STAIRS));

            builder.setFacing(face.getOppositeFace())
                   .apply(corner)
                   .setFacing(face)
                   .setHalf(Half.TOP)
                   .apply(corner.getRelative(face.getOppositeFace()));
            corner = corner.getRelative(face.getOppositeFace()).getUp();

            builder.setHalf(Half.BOTTOM).setFacing(face.getOppositeFace());

            // Place slopes
            for (int i = 0; i < size - 2; i++) {
                builder.apply(corner);
                // Hollow out bottom so tent entrance is clear
                for (int j = corner.getY() - 1; j > core.getY() - 1; j--) {
                    if (!Tag.BEDS.isTagged(corner.getAtY(j).getType()) && !Tag.STAIRS.isTagged(corner.getAtY(j)
                                                                                                     .getType()))
                    {
                        corner.getAtY(j).setType(Material.AIR);
                    }
                }

                SimpleBlock target = corner.getRelative(facing.getOppositeFace());
                target.setType(cloth);

                // Hollow out bottom so tent entrance is clear
                for (int j = target.getY() - 1; j > core.getY() - 1; j--) {
                    if (!Tag.BEDS.isTagged(target.getAtY(j).getType())) {
                        target.getAtY(j).setType(Material.AIR);
                    }
                }

                target.getUp().setType(fenceMat);
                corner = corner.getUp().getRelative(face.getOppositeFace());
            }
        }
    }

    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        Material planks = WoodUtils.getWoodForBiome(biome, WoodType.PLANKS);
        Material fence = WoodUtils.getWoodForBiome(biome, WoodType.FENCE);
        Material cloth = GenUtils.randChoice(rand, edgyWools);

        int height = GenUtils.randInt(rand, 4, 6);
        int length = GenUtils.randInt(rand, 5, 9);

        BlockFace facing = BlockUtils.getDirectBlockFace(rand);
        SimpleBlock firstProp = new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getRelative(facing,
                length / 2).getGroundOrSeaLevel();

        // Tent props
        placeProp(height, firstProp.getUp(), facing, cloth);
        placeProp(
                height,
                firstProp.getUp().getRelative(facing.getOppositeFace(), length),
                facing.getOppositeFace(),
                cloth
        );

        // Tent ground to account for water or potential uneven ground.
        Wall wallProp = new Wall(firstProp, facing);
        for (int relWidth = 0; relWidth < height; relWidth++) {
            for (int relLen = 0; relLen <= length; relLen++) {
                Wall target = wallProp.getLeft(relWidth).getRelative(facing.getOppositeFace(), relLen);
                target.get().lsetType(planks);
                target.getDown().downUntilSolid(getRand(), fence);
                // target.getUp().get().lsetType(Material.WHITE_CARPET);
                target = wallProp.getRight(relWidth).getRelative(facing.getOppositeFace(), relLen);
                target.get().lsetType(planks);
                target.getDown().downUntilSolid(getRand(), fence);
                // target.getUp().get().lsetType(Material.WHITE_CARPET);
            }
        }

        // Connect the 2 props with one long log
        OrientableBuilder ob = new OrientableBuilder(WoodUtils.getWoodForBiome(
                biome,
                WoodType.LOG
        )).setAxis(BlockUtils.getAxisFromBlockFace(facing));
        wallProp = wallProp.getRelative(0, height, 0);
        for (int relLen = 0; relLen <= length; relLen++) {
            ob.apply(wallProp);

            if (relLen != 0 && relLen != length) {
                for (int i = wallProp.getY() - 1; i > firstProp.getY(); i--) {
                    if (!Tag.BEDS.isTagged(wallProp.getAtY(i).getType())) {
                        wallProp.getAtY(i).setType(Material.AIR);
                    }
                }
            }
            wallProp = wallProp.getRelative(facing.getOppositeFace());
        }

        // Place wool walls
        for (BlockFace face : BlockUtils.getAdjacentFaces(facing)) {

            for (int relLen = 2; relLen <= length - 2; relLen++) {
                SimpleBlock corner = firstProp.getRelative(face, height - 1)
                                              .getRelative(facing.getOppositeFace(), relLen)
                                              .getUp(); // Off the ground
                for (int relWidth = height - 2; relWidth >= 0; relWidth--) {

                    for (int i = corner.getY(); i > firstProp.getY(); i--) {
                        if (GenUtils.isGroundLike(corner.getAtY(i).getType())) {
                            corner.getAtY(i).setType(Material.AIR);
                        }
                    }

                    if (relWidth == height - 2) {
                        corner.setType(cloth);
                        // Empty out tent area
                        SimpleBlock target = corner.getRelative(face.getOppositeFace());
                        if (!target.isSolid() && GenUtils.chance(rand, 1, 2)) {
                            switch (rand.nextInt(4)) {
                                case 0:
                                    target.setType(Material.CRAFTING_TABLE);
                                    break;
                                case 1:
                                    target.setType(Material.FLETCHING_TABLE);
                                    break;
                                case 2:
                                    new ChestBuilder(Material.CHEST).setFacing(face.getOppositeFace())
                                                                    .setLootTable(TerraLootTable.PILLAGER_OUTPOST)
                                                                    .apply(target);
                                    break;
                                case 3:
                                    BlockUtils.placeBed(target, BlockUtils.pickBed(), face.getOppositeFace());
                                    break;
                            }
                        }

                        if (GenUtils.chance(rand, 1, 4)) {
                            target.getRelative(face.getOppositeFace()).addEntity(EntityType.PILLAGER);
                        }

                    }
                    corner.getUp().setType(cloth);
                    corner = corner.getRelative(face.getOppositeFace()).getUp();
                }
            }
        }
    }


    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}