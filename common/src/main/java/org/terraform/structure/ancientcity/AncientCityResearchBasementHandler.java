package org.terraform.structure.ancientcity;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.version.V_1_19;

import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.Random;

public class AncientCityResearchBasementHandler {

    private static final String[] northSouthResearchSchematics = new String[] {
            "ancient-city/ancient-city-basement-sculkresearch",
            "ancient-city/ancient-city-basement-redstoneresearch",
            "ancient-city/ancient-city-basement-sleepingchamber",
            };
    private static final String[] eastWestResearchSchematics = new String[] {
            "ancient-city/ancient-city-basement-farm", "ancient-city/ancient-city-basement-cage",
            };

    public static void populate(@NotNull Random random,
                                @NotNull PopulatorDataAbstract data,
                                @NotNull CubeRoom room,
                                @NotNull BlockFace headFacing)
    {

        // Clear out the room and place floor
        int[] lowerCorner = room.getLowerCorner();
        int[] upperCorner = room.getUpperCorner();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
                    if (y == room.getY() || y == room.getY() + room.getHeight() - 1) {
                        if (y == room.getY()
                            && x > lowerCorner[0]
                            && x < upperCorner[0]
                            && z > lowerCorner[1]
                            && z < upperCorner[1])
                        {
                            data.setType(x, y, z, Material.GRAY_WOOL);
                            data.setType(x, y - 1, z, AncientCityUtils.deepslateBricks);
                        }
                        else {
                            data.setType(x, y, z, AncientCityUtils.deepslateBricks);
                        }
                    }
                    else {
                        data.setType(x, y, z, Material.AIR);
                    }

                }
            }
        }

        // Place walls
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey().getLeft(3);
            for (int i = 3; i < entry.getValue() - 3; i += 3) {
                w.getUp(4).setType(Material.CHISELED_DEEPSLATE);
                w.setType(Material.CHISELED_DEEPSLATE);
                new StairBuilder(Material.DEEPSLATE_TILE_STAIRS).setFacing(w.getDirection())
                                                                .apply(w.getUp(3))
                                                                .setHalf(Half.TOP)
                                                                .apply(w.getUp());
                new OrientableBuilder(Material.POLISHED_BASALT).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                               .apply(w.getUp(2))
                                                               .apply(w.getUp(2).getFront());

                // Back of wall
                w.getUp().getFront().setType(Material.CHISELED_DEEPSLATE);
                w.getUp(3).getFront().setType(Material.CHISELED_DEEPSLATE);
                w.getFront().setType(Material.DEEPSLATE_TILES);
                w.getUp(4).getFront().setType(Material.DEEPSLATE_TILES);


                // Left and right sides
                for (BlockFace face : BlockUtils.getAdjacentFaces(w.getDirection())) {
                    Wall temp = w.getRelative(face);

                    temp.getUp(2).setType(Material.POLISHED_BASALT);

                    new StairBuilder(Material.DEEPSLATE_TILE_STAIRS).setFacing(face.getOppositeFace())
                                                                    .apply(temp)
                                                                    .setHalf(Half.TOP)
                                                                    .apply(temp.getUp(4));

                    new StairBuilder(Material.POLISHED_DEEPSLATE_STAIRS).setFacing(face.getOppositeFace())
                                                                        .apply(temp.getUp(3))
                                                                        .setHalf(Half.TOP)
                                                                        .apply(temp.getUp());

                    // rear
                    w.getFront().getRelative(face).Pillar(2, Material.POLISHED_BASALT);
                    w.getFront().getRelative(face).getUp(2).setType(Material.DEEPSLATE_TILES);
                    w.getFront().getRelative(face).getUp(3).Pillar(2, Material.POLISHED_BASALT);
                }

                w = w.getLeft(3);
            }
        }

        // Place 4 solid cubes of deepslate at the corners
        // Decorate the sides
        for (int[] corner : room.getAllCorners(2)) {

            SimpleBlock core = new SimpleBlock(data, corner[0], room.getY(), corner[1]);

            // Solid rectangle
            for (int relX = -2; relX <= 2; relX++) {
                for (int relZ = -2; relZ <= 2; relZ++) {
                    SimpleBlock target = core.getRelative(relX, 1, relZ);
                    target.RPillar(
                            4,
                            new Random(),
                            Material.DEEPSLATE_BRICKS,
                            Material.CRACKED_DEEPSLATE_BRICKS,
                            Material.DEEPSLATE_BRICKS,
                            Material.CRACKED_DEEPSLATE_BRICKS,
                            Material.DEEPSLATE_BRICKS,
                            Material.CRACKED_DEEPSLATE_BRICKS,
                            Material.DEEPSLATE_BRICK_SLAB
                    );
                }
            }

            // Stairs at the top and bottom
            for (BlockFace side : BlockUtils.directBlockFaces) {
                Wall w = new Wall(core, side).getFront(3).getUp();
                if (!w.getDown().isSolid() || w.isSolid()) {
                    continue;
                }
                // w.setType(Material.RED_WOOL);
                for (BlockFace adj : BlockUtils.getAdjacentFaces(side)) {
                    if (w.getRelative(adj).isSolid()) {
                        continue;
                    }
                    new StairBuilder(Material.DEEPSLATE_BRICK_STAIRS).setFacing(w.getDirection().getOppositeFace())
                                                                     .apply(w)
                                                                     .apply(w.getRelative(adj, 1))
                                                                     .apply(w.getRelative(adj, 2))
                                                                     .apply(w.getRelative(adj, 3))
                                                                     .setHalf(Half.TOP)
                                                                     .apply(w.getUp(3))
                                                                     .apply(w.getRelative(adj, 1).getUp(3))
                                                                     .apply(w.getRelative(adj, 2).getUp(3))
                                                                     .apply(w.getRelative(adj, 3).getUp(3));

                    // Make these corners
                    BlockUtils.correctStairData(w.getRelative(adj, 3));
                    BlockUtils.correctStairData(w.getRelative(adj, 3).getUp(3));
                    w.getRelative(adj, 2).getRear().Pillar(4, Material.POLISHED_BASALT);
                }
            }
        }

        // Build stairs leading out of the room
        int roomBreadth = BlockUtils.getAxisFromBlockFace(headFacing) == Axis.X ? room.getWidthX() : room.getWidthZ();
        SimpleBlock base = room.getCenterSimpleBlock(data).getRelative(headFacing, roomBreadth / 3);

        for (int depth = 0; depth < 10; depth++) {
            boolean breakOut = false;
            for (BlockFace adj : BlockUtils.getAdjacentFaces(headFacing)) {
                Wall core = new Wall(base.getUp(), adj).getFront(2).getRelative(headFacing, depth);
                if (core.isSolid()) {
                    breakOut = true;
                    break;
                }
                if (depth == 0) {
                    core.Pillar(4, Material.POLISHED_BASALT);
                    for (int i = 1; i < 4; i++) {
                        core.getFront(i).Pillar(6, AncientCityUtils.deepslateBricks);
                    }
                }
                else {
                    new StairwayBuilder(Material.DEEPSLATE_BRICK_STAIRS).setDownTypes(AncientCityUtils.deepslateBricks)
                                                                        .setStairwayDirection(BlockFace.UP)
                                                                        .setStopAtY(room.getY() + 6)
                                                                        .setUpwardsCarveUntilNotSolid(false)
                                                                        .build(core);
                }
            }
            if (breakOut) {
                break;
            }
        }

        // Place a center light pillar and partition the room with 8 walls
        SimpleBlock pillarCent = room.getCenterSimpleBlock(data).getUp();
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                pillarCent.getRelative(nx, 0, nz).setType(AncientCityUtils.deepslateTiles);
                pillarCent.getRelative(nx, 3, nz).setType(AncientCityUtils.deepslateTiles);
            }
        }
        pillarCent.getUp().Pillar(2, V_1_19.SCULK_SENSOR);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            new StairBuilder(Material.DEEPSLATE_BRICK_STAIRS).setFacing(face.getOppositeFace())
                                                             .apply(pillarCent.getRelative(face, 2))
                                                             .setHalf(Half.TOP)
                                                             .apply(pillarCent.getRelative(face, 2).getUp(3));

            pillarCent.getRelative(face).getUp().Pillar(2, Material.REDSTONE_LAMP);

            pillarCent.getRelative(face)
                      .getRelative(BlockUtils.getLeft(face))
                      .getUp()
                      .Pillar(2, Material.DEEPSLATE_BRICK_WALL);
            pillarCent.getRelative(face).getRelative(BlockUtils.getLeft(face)).getUp().CorrectMultipleFacing(2);
        }

        // Glass wall up
        for (BlockFace[] faces : BlockUtils.cornerBlockFaces) {
            SimpleBlock rel = pillarCent.getRelative(faces[0], 3).getRelative(faces[1], 3);
            rel.Pillar(4, Material.POLISHED_BASALT);

            for (BlockFace face : faces) {
                int v = face.getModX() != 0 ? room.getWidthX() / 2 - 6 : room.getWidthZ() / 2 - 6;
                SimpleBlock relrel = rel;
                for (int i = 1; i < v; i++) {
                    relrel = relrel.getRelative(face);
                    if (relrel.isSolid()) {
                        break;
                    }
                    relrel.getUp().Pillar(2, Material.GLASS_PANE);
                    relrel.CorrectMultipleFacing(3);
                    relrel.setType(AncientCityUtils.deepslateBricks);
                    relrel.getUp(3).setType(AncientCityUtils.deepslateBricks);
                }
                relrel.Pillar(4, Material.POLISHED_BASALT);
            }
        }

        // Place piston doors at the entrance
        try {
            TerraSchematic schema = TerraSchematic.load("ancient-city/ancient-city-pistondoor",
                    base.getUp().getRelative(headFacing.getOppositeFace(), 2)
            );
            schema.parser = new AncientCitySchematicParser();
            schema.setFace(headFacing);
            schema.apply();
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

        // Decorate the 4 redstone research rooms
        for (BlockFace face : BlockUtils.directBlockFaces) {
            // room.getCenterSimpleBlock(data).getUp().setType(Material.RED_WOOL);
            SimpleBlock pasteCent = room.getCenterSimpleBlock(data)
                                        .getUp()
                                        .getRelative(face, 2)
                                        .getRelative(BlockUtils.getLeft(face), 2);

            try {
                String schematic;

                // This aligns the rooms with the head's piston entrance, as the
                // rooms are of an unequal size.
                if (BlockUtils.getAxisFromBlockFace(face) == BlockUtils.getAxisFromBlockFace(headFacing)) {
                    schematic = northSouthResearchSchematics[random.nextInt(northSouthResearchSchematics.length)];
                }
                else {
                    schematic = eastWestResearchSchematics[random.nextInt(eastWestResearchSchematics.length)];
                }

                TerraSchematic schema = TerraSchematic.load(schematic, pasteCent);
                schema.parser = new AncientCitySchematicParser();
                schema.setFace(face);
                schema.apply();
            }
            catch (FileNotFoundException e) {
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }
    }
}
