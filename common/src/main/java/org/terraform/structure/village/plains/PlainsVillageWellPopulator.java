package org.terraform.structure.village.plains;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.Version;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Random;

public class PlainsVillageWellPopulator extends PlainsVillageAbstractRoomPopulator {

    private static final String[] villageWellSchems = new String[] {
            "plainsvillage-well1", "plainsvillage-well2"
    };

    private final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageWellPopulator(PlainsVillagePopulator plainsVillagePopulator,
                                      Random rand,
                                      boolean forceSpawn,
                                      boolean unique)
    {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        // This code is repeated from super, as it must be changed a little
        int y = super.calculateRoomY(data, room);

        int worldHeight = TerraformGeneratorPlugin.injector.getMaxY() - TerraformGeneratorPlugin.injector.getMinY() + 1;

        for (int[] corner : room.getAllCorners()) {
            SimpleBlock sb = new SimpleBlock(data, corner[0], y, corner[1]);
            int lowSb = sb.findFloor(worldHeight).getY();
            if (Math.abs(lowSb - y) > TConfig.c.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE) {
                // place platform as uneven ground was detected.
                this.placeFixerPlatform(y, data, room);
                break;
            }
        }

        int x = room.getX();
        int z = room.getZ();
        SimpleBlock tester = new SimpleBlock(data, x, y + 1, z);
        if (BlockUtils.isWet(tester)) {
            y = tester.getGroundOrDry().getY(); // Force wells to not be submerged.
        }

        BlockFace roomDir = ((DirectionalCubeRoom) room).getDirection();

        try {
            SimpleBlock core = new SimpleBlock(data, x, y + 1, z);
            TerraSchematic schem = TerraSchematic.load(
                    villageWellSchems[rand.nextInt(villageWellSchems.length)],
                    core.getDown()
            );
            schem.parser = new PlainsVillageWellSchematicParser();
            schem.apply();

            int depth = GenUtils.randInt(rand, 5, 20);

            for (int i = 0; i < depth; i++) {
                boolean breakOut = false;
                if (i > 0) {
                    for (BlockFace face : BlockUtils.flatBlockFaces3x3) {
                        // no solid ground beneath. Do not place water.
                        if (!core.getRelative(face).getDown(depth + 1).isSolid()) {
                            breakOut = true;
                            break;
                        }
                    }
                }
                if (breakOut) {
                    break;
                }
                if (i == 0) {
                    core.setType(Material.AIR);
                }
                else {
                    core.getRelative(0, -i, 0).setType(Material.WATER);
                }

                for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                    if (i == 0) {
                        core.getRelative(face).setType(Material.AIR);
                    }
                    else {
                        core.getRelative(0, -i, 0).getRelative(face).setType(Material.WATER);
                    }
                }
            }

            for (int nx = -3; nx <= 3; nx++) {
                for (int nz = -3; nz <= 3; nz++) {
                    Wall target = new Wall(core.getRelative(nx, -1, nz));
                    if (target.getType() == Material.COBBLESTONE || target.getType() == Material.MOSSY_COBBLESTONE) {
                        target.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
                    }
                }
            }

            Wall w = new Wall(core.getRelative(roomDir, 3), roomDir);

            int pathLength = room.getWidthX() / 2;
            if (BlockUtils.getAxisFromBlockFace(roomDir) == Axis.Z) {
                pathLength = room.getWidthZ() / 2;
            }

            for (int i = 0; i < pathLength - 1; i++) {
                w.getGround().setType(Material.DIRT_PATH);
                w.getLeft().getGround().setType(Material.DIRT_PATH);
                w.getRight().getGround().setType(Material.DIRT_PATH);

                if (GenUtils.chance(rand, 1, 10)) {
                    BlockFace lampFace = BlockUtils.getTurnBlockFace(rand, roomDir);
                    SimpleBlock target = w.getRelative(lampFace, 2).getGround().getUp().get();
                    if (target.getDown().getType() != Material.DIRT_PATH && PlainsVillagePathPopulator.canPlaceLamp(
                            target))
                    {
                        PlainsVillagePathPopulator.placeLamp(rand, target);
                    }
                }

                w = w.getRelative(roomDir);
            }

        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() <= 10;
    }

    private class PlainsVillageWellSchematicParser extends SchematicParser {
        @Override
        public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
            if (data.getMaterial().toString().contains("COBBLESTONE")) {
                data = Bukkit.createBlockData(data.getAsString().replaceAll("cobblestone",
                        GenUtils.randChoice(rand,
                                Material.COBBLESTONE,
                                Material.COBBLESTONE,
                                Material.COBBLESTONE,
                                Material.MOSSY_COBBLESTONE
                        ).toString().toLowerCase(Locale.ENGLISH)
                ));
                super.applyData(block, data);
            }
            else if (data.getMaterial() == Material.IRON_BARS) {
                if (Version.isAtLeast(16)) {
                    data = Bukkit.createBlockData(Material.CHAIN);
                }
                super.applyData(block, data);
            }
            else if (data.getMaterial().toString().startsWith("OAK_")) {
                data = Bukkit.createBlockData(data.getAsString().replaceAll("oak_", plainsVillagePopulator.wood));
                super.applyData(block, data);
            }
            else {
                super.applyData(block, data);
            }
        }
    }
}
