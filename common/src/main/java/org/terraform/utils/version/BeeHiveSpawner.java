package org.terraform.utils.version;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.populatordata.IPopulatorDataBeehiveEditor;
import org.terraform.data.SimpleBlock;

public class BeeHiveSpawner {

    public static void spawnFullBeeNest(SimpleBlock block) {
        
        if(block.getPopData() instanceof IPopulatorDataBeehiveEditor)
        {
        	IPopulatorDataBeehiveEditor ipopdata = (IPopulatorDataBeehiveEditor) block.getPopData();
        	ipopdata.setBeehiveWithBee(block.getX(), block.getY(), block.getZ());
        }
        else
        {
            block.setType(Material.BEE_NEST);
        	block.getPopData().addEntity(block.getX(), block.getY(), block.getZ(), EntityType.BEE);
        }
    }

}
