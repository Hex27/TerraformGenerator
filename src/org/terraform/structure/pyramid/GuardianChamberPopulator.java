package org.terraform.structure.pyramid;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class GuardianChamberPopulator extends RoomPopulatorAbstract {

    public GuardianChamberPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        int[] lowerCorner = room.getLowerCorner(1);
        int[] upperCorner = room.getUpperCorner(1);
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                //Leave some stepping stones
                if (GenUtils.chance(rand, 1, 30)) {
                    if (rand.nextBoolean()) {
                        data.setType(x, room.getY() + 1, z, GenUtils.randMaterial(
                                Material.STONE,
                                Material.ANDESITE,
                                Material.ANDESITE_WALL,
                                Material.COBBLESTONE,
                                Material.COBBLESTONE_WALL
                        ));
                    }
                    continue;
                }

                //Sides have more platforms.
                if (x == lowerCorner[0] || x == upperCorner[0] || z == lowerCorner[1] || z == upperCorner[1]) {
                    if (rand.nextBoolean()) continue;
                }

                //The closer to the center, the deeper the pool.
                double heightMultiplierX = ((double) (2 * Math.abs(room.getX() - x))) / ((double) Math.abs(room.getWidthX()));
                double heightMultiplierZ = ((double) (2 * Math.abs(room.getZ() - z))) / ((double) Math.abs(room.getWidthZ()));
                double heightMultiplier = 1 - ((heightMultiplierX + heightMultiplierZ) / 2);
                int poolDepth = (int) (1 + heightMultiplier * 4);
                //Set water
                for (int y = room.getY(); y > room.getY() - poolDepth; y--) {
                    data.setType(x, y, z, Material.WATER);
                    if (data.getType(x, y + 1, z) == Material.STONE_PRESSURE_PLATE)
                        data.setType(x, y + 1, z, Material.AIR);
                }
            }
        }
        for (int i = 0; i < GenUtils.randInt(3, 5); i++) {
            data.addEntity(room.getX(), room.getY() + 2, room.getZ(), EntityType.GUARDIAN);
        }
    }


    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 5 && room.getWidthZ() >= 5;
    }


}
