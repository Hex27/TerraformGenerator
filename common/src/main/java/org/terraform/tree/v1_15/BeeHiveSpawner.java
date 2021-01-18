package org.terraform.tree.v1_15;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.data.SimpleBlock;

public class BeeHiveSpawner {

    public static void spawnFullBeeNest(SimpleBlock block) {
        block.setType(Material.BEE_NEST);
        block.getPopData().addEntity(block.getX(), block.getY(), block.getZ(), EntityType.BEE);
    }

}
