package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Map.Entry;
import java.util.Random;

public class ElderGuardianChamber extends RoomPopulatorAbstract {

    public ElderGuardianChamber(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        // 4 statues
        SimpleBlock base = new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            placeStatue(base.getRelative(face, 4), face.getOppositeFace());
        }

        // 4 pillars
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            placePillar(new Wall(base.getRelative(face, 6)), room.getHeight() - 1);
        }

        // Classic Pyramid interior floor decoration. On the ceiling as well.
        SimpleBlock center = new SimpleBlock(data, room.getX(), room.getY(), room.getZ());
        center.setType(Material.BLUE_TERRACOTTA);
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            center.getRelative(face).setType(Material.ORANGE_TERRACOTTA);
            new Wall(center.getRelative(face).getRelative(face).getUp()).Pillar(
                    room.getHeight(),
                    rand,
                    Material.CUT_SANDSTONE
            );
        }
        for (BlockFace face : BlockUtils.directBlockFaces) {
            center.getRelative(face).getRelative(face).setType(Material.ORANGE_TERRACOTTA);
        }

        // On the ceiling as well
        SimpleBlock ceiling = new SimpleBlock(data, room.getX(), room.getY() + room.getHeight(), room.getZ());
        ceiling.setType(Material.BLUE_TERRACOTTA);
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            ceiling.getRelative(face).setType(Material.ORANGE_TERRACOTTA);
        }
        for (BlockFace face : BlockUtils.directBlockFaces) {
            ceiling.getRelative(face).getRelative(face).setType(Material.ORANGE_TERRACOTTA);
        }

        // Elder Guardian cage
        if (TConfig.c.STRUCTURES_PYRAMID_SPAWN_ELDER_GUARDIAN) {
            SimpleBlock cageCenter = center.getUp(11);
            placeElderGuardianCage(cageCenter);
        }

        // Decorate the 4 walls to not be so plain
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            // entry.getValue() == 20
            for (int i = 0; i < entry.getValue(); i++) {
                if (i % 2 == 0 && i != 0 && i != entry.getValue() - 1) {
                    w.getUp(4).Pillar(10, rand, Material.CHISELED_RED_SANDSTONE);
                }
                w = w.getLeft();
            }
        }

        // Stairs at the top
        for (Entry<Wall, Integer> walls : room.getFourWalls(data, 1).entrySet()) {
            Wall w = walls.getKey().getRelative(0, room.getHeight() - 2, 0);
            int length = walls.getValue();
            for (int j = 0; j < length; j++) {

                Stairs stair = (Stairs) Bukkit.createBlockData(Material.RED_SANDSTONE_STAIRS);
                stair.setFacing(w.getDirection().getOppositeFace());
                stair.setHalf(Half.TOP);
                w.setBlockData(stair);

                w = w.getLeft();
            }
        }
    }

    private void placeElderGuardianCage(@NotNull SimpleBlock cageCenter) {
        cageCenter.getDown(2).setType(Material.CUT_SANDSTONE);
        cageCenter.getUp(2).setType(Material.CUT_SANDSTONE);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Wall w = new Wall(cageCenter, face);
            w.getFront(2).setType(Material.CHISELED_SANDSTONE);
            w.getFront(2).getLeft().setType(Material.CHISELED_SANDSTONE);
            w.getFront(2).getRight().setType(Material.CHISELED_SANDSTONE);

            Stairs stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
            stair.setFacing(face.getOppositeFace());
            w.getFront(2).getUp().setBlockData(stair);

            stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
            stair.setFacing(face.getOppositeFace());
            stair.setHalf(Half.TOP);
            w.getFront(2).getDown().setBlockData(stair);

            w.getFront().getUp(2).setType(Material.CUT_SANDSTONE_SLAB);

            Slab slab = (Slab) Bukkit.createBlockData(Material.CUT_SANDSTONE_SLAB);
            slab.setType(Type.TOP);
            w.getFront().getDown(2).setBlockData(slab);

        }

        cageCenter.getPopData()
                  .addEntity(cageCenter.getX(), cageCenter.getY(), cageCenter.getZ(), EntityType.ELDER_GUARDIAN);
    }

    private void placePillar(@NotNull Wall base, int height) {

        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            base.getRelative(face).Pillar(height, rand, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE);
        }

        for (BlockFace face : BlockUtils.directBlockFaces) {
            base.getRelative(face).Pillar(height,
                    true,
                    rand,
                    Material.CUT_SANDSTONE,
                    Material.CHISELED_SANDSTONE,
                    Material.AIR,
                    Material.AIR,
                    Material.AIR,
                    Material.CHISELED_SANDSTONE
            );
        }
        base.Pillar(height, rand, Material.CHISELED_RED_SANDSTONE);
    }

    private void placeStatue(SimpleBlock base, BlockFace dir) {
        try {
            TerraSchematic schema = TerraSchematic.load("pharoah-statue", base);
            schema.parser = new SchematicParser();
            schema.setFace(dir);
            schema.apply();
        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }


}
