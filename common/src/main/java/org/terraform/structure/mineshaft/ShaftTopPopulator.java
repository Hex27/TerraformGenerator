package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Random;

public class ShaftTopPopulator extends RoomPopulatorAbstract {

    public ShaftTopPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        int[] lowerCorner = room.getLowerCorner(1);
        int[] upperCorner = room.getUpperCorner(1);
        int y = room.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                // Use scaffolding instead of fences for top area
                if (b.getDown().getType() == Material.OAK_FENCE) {
                    // Find lowest block
                    while (b.getDown().getType() == Material.OAK_FENCE) {
                        b = b.getDown();
                    }
                    // Start replacing upwards
                    while (b.getY() <= y) {
                        b.setType(Material.SCAFFOLDING);
                        b = b.getUp();
                    }
                }
            }
        }

        // Generate Ore Lift
        Wall w = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 3, room.getZ()));
        w = w.findCeiling(10);
        if (w != null) {
            TerraSchematic schema;
            try {
                Wall target = w.getRelative(0, -GenUtils.randInt(rand, 8, 10), 0);

                // Clear a space
                BlockUtils.carveCaveAir(
                        new Random().nextInt(777123),
                        3,
                        5,
                        3,
                        new SimpleBlock(data, target.getX(), room.getY(), target.getZ()),
                        false,
                        EnumSet.of(Material.BARRIER)
                );

                schema = TerraSchematic.load("ore-lift", target.get().getRelative(-1, 0, -1));
                schema.parser = new OreLiftSchematicParser();
                schema.setFace(BlockFace.NORTH);
                schema.apply();
                target.LPillar(w.getY() - target.getY(), rand, Material.OAK_FENCE);
            }
            catch (FileNotFoundException e) {
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }


}
