package org.terraform.structure.pillager.mansion;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionHallwayPopulator extends MansionRoomPopulator {

	public MansionHallwayPopulator(CubeRoom room, HashMap<BlockFace, Boolean> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		SimpleBlock center = this.getRoom().getCenterSimpleBlock(data);
		
		center.getRelative(0,1,0).setType(Material.RED_CARPET);
		for(BlockFace face:BlockUtils.xzPlaneBlockFaces)
			center.getRelative(0,1,0).getRelative(face).setType(Material.RED_CARPET);
		
		for(BlockFace face:BlockUtils.directBlockFaces) {
			if(this.getInternalWalls().containsKey(face)) {
				Wall w = new Wall(center, face);
				//This is a solid wall. Bring it forward and decorate it.
				if(getInternalWalls().get(face)) {
					Wall target = w.getFront(3);
					applyHallwaySmoothing(target);
					applyHallwaySmoothing(target.getLeft(1));
					applyHallwaySmoothing(target.getLeft(2));
					applyHallwaySmoothing(target.getLeft(3));
					applyHallwaySmoothing(target.getRight(1));
					applyHallwaySmoothing(target.getRight(2));
					applyHallwaySmoothing(target.getRight(3));
					
					//Room is connected to a window.
					//Spawn the arch and block out the window.
					if(!target.getRight(4).getRelative(0,1,0).getType().isSolid()) {
						applyHallwaySmoothing(target.getRight(4));
						target.getRight(5).Pillar(6, Material.DARK_OAK_PLANKS);
					}
					if(!target.getLeft(4).getRelative(0,1,0).getType().isSolid()) {
						applyHallwaySmoothing(target.getLeft(4));
						target.getLeft(5).Pillar(6, Material.DARK_OAK_PLANKS);
					}
					
				}
				else //This wall opens to another room somewhere. Connect a red carpet to it
				{
					for(int length = 2; length < 6; length++) {
						Wall target = w.getFront(length).getRelative(0,1,0);
						target.setType(Material.RED_CARPET);
						if(length < 5) {
							target.getLeft().setType(Material.RED_CARPET);
							target.getRight().setType(Material.RED_CARPET);
						}
					}
				}
			}
			else //This face connects to a window
			{
				center.getRelative(0,1,0).getRelative(face,2).setType(Material.RED_CARPET);
			}
		}
		
		spawnSmallChandelier(center.getRelative(0,7,0));
	}
	
	private void spawnSmallChandelier(SimpleBlock target) {
		target.setType(Material.DARK_OAK_FENCE);
		target.getRelative(0,-1,0).setType(Material.DARK_OAK_FENCE);
		target.getRelative(0,-2,0).setType(Material.DARK_OAK_FENCE);
		target.getRelative(0,-3,0).setType(Material.DARK_OAK_FENCE);
		
		target = target.getRelative(0,-3,0);
		for(BlockFace face:BlockUtils.directBlockFaces) {
			target.getRelative(face).setType(Material.DARK_OAK_FENCE);
			target.getRelative(face).getRelative(0,1,0).setType(Material.TORCH);
			
		}
		BlockUtils.correctSurroundingMultifacingData(target);
	}
	
	private void applyHallwaySmoothing(Wall w) {
		w.Pillar(7, Material.DARK_OAK_PLANKS);
		
		w = w.getRear();
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(w.getDirection())
		.setHalf(Half.TOP)
		.lapply(w.getRelative(0,5,0));
		w.getRelative(0,6,0).Pillar(2, Material.DARK_OAK_PLANKS);

		w = w.getRear();
		w.getRelative(0,6,0).Pillar(2, Material.DARK_OAK_PLANKS);
		
		w = w.getRear();
		new SlabBuilder(Material.DARK_OAK_SLAB)
		.setType(Type.TOP)
		.lapply(w.getRelative(0,6,0));
		w.getRelative(0,7,0).setType(Material.DARK_OAK_PLANKS);
		
		w = w.getRear();
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(w.getDirection())
		.setHalf(Half.TOP)
		.lapply(w.getRelative(0,7,0));
	}

}
