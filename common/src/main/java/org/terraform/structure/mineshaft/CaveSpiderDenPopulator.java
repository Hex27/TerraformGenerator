package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CaveSpiderDenPopulator extends RoomPopulatorAbstract {

    public CaveSpiderDenPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        int[] lowerCorner = room.getLowerCorner(3);
        int[] upperCorner = room.getUpperCorner(3);

        //Flooring - Have a stone brick platform.
        int y = room.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                if (b.getType() == Material.CAVE_AIR) {
                    b.setType(GenUtils.randMaterial(
                            Material.OAK_PLANKS,
                            Material.OAK_SLAB,
                            Material.OAK_PLANKS,
                            Material.OAK_SLAB,
                            Material.MOSSY_COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.COBBLESTONE_SLAB));
                }
            }
        }

        //Cave-spider spawner & Webs
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                int limit = 10;
                while (limit > 0 && b.getType() != Material.CAVE_AIR) {
                    b = b.getRelative(0, 1, 0);
                    limit--;
                }
                if (limit < 0) continue; //No space above.
                if (x == room.getX() && z == room.getZ()) {
                    data.setSpawner(x, b.getY(), z, EntityType.CAVE_SPIDER);
                } else {
                    Wall w = new Wall(b, BlockFace.NORTH);
                    w.LPillar(GenUtils.randInt(0, 2), rand, Material.COBWEB);
                }
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return !room.isBig();
    }
}
