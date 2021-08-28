package org.terraform.structure.pillager.mansion;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs.Shape;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.PaintingUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

public class MansionHallwayPopulator extends MansionRoomPopulator {

	public MansionHallwayPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		SimpleBlock center = this.getRoom().getCenterSimpleBlock(data);
		
		center.getRelative(0,1,0).setType(Material.RED_CARPET);
		for(BlockFace face:BlockUtils.xzPlaneBlockFaces)
			center.getRelative(0,1,0).getRelative(face).setType(Material.RED_CARPET);
		
		for(BlockFace face:BlockUtils.directBlockFaces) {
			if(getInternalWalls().get(face) != MansionInternalWallState.WINDOW) {
				Wall w = new Wall(center, face);
				//This is a solid wall. Bring it forward and decorate it.
				if(getInternalWalls().get(face) == MansionInternalWallState.SOLID) {
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
					decorateHallwayWall(random, new Wall(target.getRear().getRelative(0,1,0).get(), w.getDirection().getOppositeFace()), false);
				}
				else if(getInternalWalls().get(face) == MansionInternalWallState.ROOM_ENTRANCE) 
					//This wall opens to another room somewhere. Connect a red carpet to it
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
				decorateHallwayWall(random, new Wall(center.getRelative(face, 3).getRelative(0,1,0), face.getOppositeFace()), true);
			}
		}
		
		spawnSmallChandelier(center.getRelative(0,7,0));
	}
	
	private void decorateHallwayWall(Random random, Wall center, boolean isWindow) {
		int decorationType = random.nextInt(3);
		if(!isWindow) { //Solid wall decorations
			switch(decorationType) {
			case 0: //3 1x2 paintings
				PaintingUtils.placePainting(center.getRelative(0,1,0).get(), center.getDirection(), PaintingUtils.getArtFromDimensions(random, 1, 2));
				PaintingUtils.placePainting(center.getRight(2).getRelative(0,1,0).get(), center.getDirection(), PaintingUtils.getArtFromDimensions(random, 1, 2));
				PaintingUtils.placePainting(center.getLeft(2).getRelative(0,1,0).get(), center.getDirection(), PaintingUtils.getArtFromDimensions(random, 1, 2));
			
				new StairBuilder(Material.DARK_OAK_STAIRS)
				.setFacing(center.getDirection().getOppositeFace())
				.setHalf(Half.TOP)
				.apply(center.getLeft())
				.apply(center.getRight());
				
				center.getLeft().getRelative(0,1,0).Pillar(3, Material.DARK_OAK_FENCE);
				center.getLeft().getRelative(0,1,0).CorrectMultipleFacing(3);
				center.getRight().getRelative(0,1,0).Pillar(3, Material.DARK_OAK_FENCE);
				center.getRight().getRelative(0,1,0).CorrectMultipleFacing(3);
				center.getRight().getRelative(0,4,0).setType(Material.DARK_OAK_PLANKS);
				center.getLeft().getRelative(0,4,0).setType(Material.DARK_OAK_PLANKS);
				break;
			case 1: //3 banners
				BannerUtils.generateBanner(random, center.getRelative(0,2,0).get(), center.getDirection(), true);
				BannerUtils.generateBanner(random, center.getRight(2).getRelative(0,2,0).get(), center.getDirection(), true);
				BannerUtils.generateBanner(random, center.getLeft(2).getRelative(0,2,0).get(), center.getDirection(), true);
				
				center.getLeft().getRear().Pillar(4, Material.DARK_OAK_LOG);
				center.getRight().getRear().Pillar(4, Material.DARK_OAK_LOG);
				new DirectionalBuilder(Material.WALL_TORCH)
				.setFacing(center.getDirection())
				.apply(center.getLeft().getRelative(0,2,0))
				.apply(center.getRight().getRelative(0,2,0));
				break;
			case 2: //chair
				new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
				.setFacing(center.getDirection().getOppositeFace())
				.apply(center)
				.setFacing(BlockUtils.getLeft(center.getDirection()))
				.setShape(Shape.INNER_RIGHT)
				.apply(center.getLeft())
				.setFacing(BlockUtils.getRight(center.getDirection()))
				.setShape(Shape.INNER_LEFT)
				.apply(center.getRight());
				
				center.getLeft(2).setType(Material.DARK_OAK_LOG);
				center.getRight(2).setType(Material.DARK_OAK_LOG);
				if(random.nextBoolean())
					center.getLeft(2).getRelative(0,1,0).setType(Material.LANTERN);
				if(random.nextBoolean())
					center.getRight(2).getRelative(0,1,0).setType(Material.LANTERN);
				break;
			default:
				break;
			}
		} else { //Window decorations
			switch(decorationType) {
			case 0: //Chair
				new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
				.setFacing(center.getDirection().getOppositeFace())
				.apply(center)
				.setFacing(BlockUtils.getLeft(center.getDirection()))
				.setShape(Shape.INNER_RIGHT)
				.apply(center.getLeft())
				.setFacing(BlockUtils.getRight(center.getDirection()))
				.setShape(Shape.INNER_LEFT)
				.apply(center.getRight());
				break;
			case 1: //Big potted plants
				center.setType(Material.DARK_OAK_PLANKS);
				center.getLeft().setType(Material.GRASS_BLOCK);
				center.getRight().setType(Material.GRASS_BLOCK);
				center.getLeft().getRelative(0,1,0).setType(Material.OAK_FENCE);
				center.getRight().getRelative(0,1,0).setType(Material.OAK_FENCE);
				center.getLeft().getRelative(0,2,0).setType(Material.OAK_LEAVES);
				center.getRight().getRelative(0,2,0).setType(Material.OAK_LEAVES);
				
				//Pot
				new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
				.setFacing(center.getDirection())
				.setOpen(true)
				.apply(center.getLeft().getFront())
				.apply(center.getRight().getFront());
				
				new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
				.setFacing(center.getDirection().getOppositeFace())
				.setOpen(true)
				.apply(center.getLeft().getRear())
				.apply(center.getRight().getRear());
				
				new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
				.setFacing(BlockUtils.getLeft(center.getDirection()))
				.setOpen(true)
				.apply(center.getLeft(2))
				.setFacing(BlockUtils.getRight(center.getDirection()))
				.apply(center.getRight(2));
				break;
			default:
				//Sometimes windows don't get decorated.
				break;
			}
		}
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
