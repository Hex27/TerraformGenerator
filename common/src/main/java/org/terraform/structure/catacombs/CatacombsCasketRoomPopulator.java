package org.terraform.structure.catacombs;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.RotatableBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

public class CatacombsCasketRoomPopulator extends CatacombsStandardPopulator {

	public CatacombsCasketRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}
	

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
    	SimpleBlock center = room.getCenterSimpleBlock(data).getUp();
    	
    	spawnCasket(new Wall(center, BlockUtils.getDirectBlockFace(rand)),rand);
    	
    	super.spawnHangingChains(data, room);
    }
    
    private void spawnCasket(Wall target, Random rand) {
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		if(face == target.getDirection()) continue;
    		
    		new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
    		.setOpen(true)
    		.setFacing(face)
    		.apply(target.getRelative(face));
    	}

    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		if(face == target.getDirection().getOppositeFace()) continue;
    		
    		new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
    		.setOpen(true)
    		.setFacing(face)
    		.apply(target.getFront().getRelative(face));
    	}
    	
    	new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
    	.setFacing(BlockUtils.getLeft(target.getDirection()))
    	.apply(target.getUp())
    	.apply(target.getFront().getUp());
    	
    	switch(rand.nextInt(3)) {
    	case 0:
        	//Chest inside the casket.
    		new ChestBuilder(Material.CHEST)
    		.setFacing(BlockUtils.getLeft(target.getDirection()))
    		.setLootTable(TerraLootTable.SIMPLE_DUNGEON)
    		.apply(target)
    		.extend(target, target.getFront(), false);
    		break;
    	case 1:
    		//Skull and redstone
    		new RotatableBuilder(Material.SKELETON_SKULL)
    		.setRotation(BlockUtils.getXZPlaneBlockFace(rand))
    		.apply(target);
    		target.getFront().setType(Material.REDSTONE_WIRE);
    		break;
		default:
			//spiders
			target.addEntity(EntityType.CAVE_SPIDER);
			target.getFront().addEntity(EntityType.CAVE_SPIDER);
    		break;
    	}
    }

}
