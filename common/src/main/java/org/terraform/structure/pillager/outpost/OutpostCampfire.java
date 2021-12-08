package org.terraform.structure.pillager.outpost;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class OutpostCampfire extends RoomPopulatorAbstract {

    public OutpostCampfire(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	SimpleBlock core = new SimpleBlock(data,room.getX(),room.getY(),room.getZ()).getGroundOrSeaLevel();
    	
    	BlockUtils.replaceCircularPatch(
    			rand.nextInt(12322), 3f, core, 
    			Material.COAL_ORE, 
    			Material.STONE,
    			Material.COARSE_DIRT, Material.COARSE_DIRT, Material.COARSE_DIRT, Material.COARSE_DIRT);
    	
    	core = core.getRelative(0,1,0);
		unitCampfire(core);
    	for(BlockFace face:BlockUtils.xzDiagonalPlaneBlockFaces) {
    		unitCampfire(core.getRelative(face).getGround().getRelative(0,1,0));
    	}
    }
    
    private void unitCampfire(SimpleBlock block) {
    	switch(rand.nextInt(3)) {
    	case 0:
    		block.setType(Material.CAMPFIRE);
    		break;
    	case 1:
    		block.setType(Material.CAMPFIRE);
    		block.getRelative(0,-1,0).setType(Material.HAY_BLOCK);
    		break;
    	case 2:
    		block.setType(Material.HAY_BLOCK);
    		block.getRelative(0,1,0).setType(Material.CAMPFIRE);
    		for(BlockFace face:BlockUtils.directBlockFaces) {
    			SimpleBlock target = block.getRelative(face).getGround().getRelative(0,1,0);
    			if(!target.getType().isSolid())
    				target.setType(Material.CAMPFIRE);
    		}
    		break;
    	}
    }


    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}