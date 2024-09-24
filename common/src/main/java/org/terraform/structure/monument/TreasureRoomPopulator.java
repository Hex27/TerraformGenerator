package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.io.FileNotFoundException;
import java.util.Random;

public class TreasureRoomPopulator extends DecoratedSidesElderRoomPopulator {

    public TreasureRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
        // TODO Auto-generated constructor stub
    }

    private static void spawnLowerClaw(@NotNull PopulatorDataAbstract data, int x, int y, int z) {

        SimpleBlock block = new SimpleBlock(data, x, y, z);
        block.setType(Material.SEA_LANTERN);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stair = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
            stair.setWaterlogged(true);

            stair.setFacing(face);
            block.getUp().getRelative(face).setBlockData(stair);
            block.getUp(2).getRelative(face).getRelative(face).setBlockData(stair);

            stair.setFacing(face.getOppositeFace());
            stair.setHalf(Half.TOP);
            block.getUp().getRelative(face).getRelative(face).setBlockData(stair);
        }

        block.getRelative(-1, 1, -1).setType(Material.PRISMARINE_BRICK_SLAB);
        block.getRelative(1, 1, -1).setType(Material.PRISMARINE_BRICK_SLAB);
        block.getRelative(1, 1, 1).setType(Material.PRISMARINE_BRICK_SLAB);
        block.getRelative(-1, 1, 1).setType(Material.PRISMARINE_BRICK_SLAB);
    }

    private static void spawnUpperClaw(@NotNull PopulatorDataAbstract data, int x, int y, int z) {

        SimpleBlock block = new SimpleBlock(data, x, y, z);
        block.setType(Material.SEA_LANTERN);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stair = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
            stair.setHalf(Half.TOP);
            stair.setWaterlogged(true);

            stair.setFacing(face);
            block.getDown().getRelative(face).setBlockData(stair);
            block.getDown(2).getRelative(face).getRelative(face).setBlockData(stair);

            stair.setFacing(face.getOppositeFace());
            stair.setHalf(Half.BOTTOM);
            block.getDown().getRelative(face).getRelative(face).setBlockData(stair);
        }

        Waterlogged slab = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_BRICK_SLAB);
        slab.setWaterlogged(true);

        block.getRelative(-1, -1, -1).setBlockData(slab);
        block.getRelative(1, -1, -1).setBlockData(slab);
        block.getRelative(1, -1, 1).setBlockData(slab);
        block.getRelative(-1, -1, 1).setBlockData(slab);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        int x = room.getX();
        int y = room.getY() + room.getHeight() / 2;
        int z = room.getZ();
        TerraSchematic schema;

        // Spawn gold core
        try {
            // World w = ((PopulatorDataPostGen) data).getWorld();
            // Add one to all to correct some weird aligning shit.
            schema = TerraSchematic.load("monument-gold", new SimpleBlock(data, x + 1, y - 5, z + 1));
            schema.parser = new MonumentSchematicParser();
            schema.setFace(BlockFace.NORTH);
            schema.apply();
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

        // Spawn elder guardian
        // data.addEntity(x+5, y, z+5, EntityType.ELDER_GUARDIAN);

        // spawn claws
        spawnLowerClaw(data, x, room.getY(), z);
        spawnUpperClaw(data, x, room.getY() + room.getHeight(), z);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 10;
    }

}
