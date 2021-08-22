package org.terraform.structure.pillager.mansion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.pillager.mansion.ground.MansionGrandStairwayPopulator;
import org.terraform.structure.pillager.mansion.ground.MansionGroundLevelDiningRoomPopulator;
import org.terraform.structure.pillager.mansion.ground.MansionGroundLevelKitchenPopulator;
import org.terraform.structure.pillager.mansion.ground.MansionLibraryPopulator;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

/**
 * Mansions will distribute room populators long after JigsawStructureBuilder's main
 * operations are done.
 * <br>
 * This is because we want to merge some of the rooms into larger rooms.
 * These rooms will be referred to as Compound Rooms.
 * <br>
 * Mansions will always have 1 3x3 room that extends to the second story. This is the
 * stairway.
 * <br>
 * Depending on RNG and size, mansions will also try to generate some 2x2 rooms
 * and 2x1 (or 1x2) rooms.
 */
public class MansionCompoundRoomDistributor {
	
	//A map of populators and their respective room areas
	public static final HashMap<MansionRoomSize, ArrayList<MansionRoomPopulator>> groundFloorPopulators = new HashMap<>() {{
		put(new MansionRoomSize(3,3), new ArrayList<MansionRoomPopulator>() {{
			add(new MansionGrandStairwayPopulator(null,null));
			}});
		put(new MansionRoomSize(2,2), new ArrayList<MansionRoomPopulator>() {{
			add(new MansionLibraryPopulator(null,null));
			}});
		put(new MansionRoomSize(1,2), new ArrayList<MansionRoomPopulator>() {{
			add(new MansionGroundLevelKitchenPopulator(null,null));
			}});
		put(new MansionRoomSize(2,1), new ArrayList<MansionRoomPopulator>() {{
			add(new MansionGroundLevelDiningRoomPopulator(null,null));
			}});
		put(new MansionRoomSize(1,1), new ArrayList<MansionRoomPopulator>() {{
			add(new MansionHallwayPopulator(null,null));
			}});
	}}; 
	
	public static void distributeRooms(Collection<JigsawStructurePiece> pieces, Random random, boolean includeStairway) {
		
		ArrayList<JigsawStructurePiece> shuffledList = new ArrayList<>();
		shuffledList.addAll(pieces);
		
		ArrayList<MansionRoomSize> potentialRoomSizes = new ArrayList<>();
		int occupiedCells = 13;
		if(includeStairway)
			potentialRoomSizes.add(new MansionRoomSize(3,3)); //Stairway Room
		potentialRoomSizes.add(new MansionRoomSize(2,2)); //At least one 2x2 room
		
		while(GenUtils.chance(random, pieces.size()-occupiedCells/4, pieces.size())) {
//			if(occupiedCells/pieces.size() < 0.7) {
//				occupiedCells += 4;
//				potentialRoomSizes.add(new MansionRoomSize(2,2));
//			}else {
				occupiedCells += 2;
				if(random.nextBoolean())
					potentialRoomSizes.add(new MansionRoomSize(2,1));
				else
					potentialRoomSizes.add(new MansionRoomSize(1,2));
//			}
		}
		
		//Iterate this way because index 0 is the 3x3 room which we want.
		for(int i = 0; i < potentialRoomSizes.size(); i++) {
			MansionRoomSize roomSize = potentialRoomSizes.get(i);
			Collections.shuffle(shuffledList);
			for(JigsawStructurePiece piece:shuffledList) {
				if(canRoomSizeFitWithCenter((MansionStandardRoomPiece) piece, pieces, roomSize)) {
					//Shuffle and distribute populator
					Collections.shuffle(groundFloorPopulators.get(roomSize), random);
					MansionRoomPopulator populator = groundFloorPopulators.get(roomSize).get(0).getInstance(piece.getRoom(), ((MansionStandardRoomPiece) piece).internalWalls);
					TerraformGeneratorPlugin.logger.info(populator.getClass().getSimpleName() + " generating at " + piece.getRoom().getSimpleLocation());
					((MansionStandardRoomPiece) piece).setRoomPopulator(populator); //set the populator;
					
					break;
				}
			}
		}
		
		//Fill the rest of the rooms with 1x1 rooms
		for(JigsawStructurePiece piece:pieces) {
			MansionRoomSize roomSize = new MansionRoomSize(1,1);
			if(((MansionStandardRoomPiece) piece).getRoomPopulator() == null) {
				Collections.shuffle(groundFloorPopulators.get(roomSize), random);
				MansionRoomPopulator populator = groundFloorPopulators.get(roomSize).get(0).getInstance(piece.getRoom(), ((MansionStandardRoomPiece) piece).internalWalls);
				TerraformGeneratorPlugin.logger.info(populator.getClass().getSimpleName() + " generating at " + piece.getRoom().getSimpleLocation());
				((MansionStandardRoomPiece) piece).setRoomPopulator(populator); //set the populator;
			}
				
		}
	}
	
	/**
	 * Also sets the needed rooms to empty room populator and knocks down relevant walls
	 * if the return value is true.
	 * @param piece
	 * @param pieces NOT TO BE MODIFIED
	 * @param roomSize
	 * @return
	 */
	public static boolean canRoomSizeFitWithCenter(MansionStandardRoomPiece piece, Collection<JigsawStructurePiece> pieces, MansionRoomSize roomSize) {
		
		SimpleLocation center = piece.getRoom().getSimpleLocation();
		
		ArrayList<SimpleLocation> relevantLocations = new ArrayList<>();
		relevantLocations.add(center);
		//Positive X
		if(roomSize.getWidthX() == 2) {
			relevantLocations.add(center.getRelative(BlockFace.EAST, MansionJigsawBuilder.groundFloorRoomWidth));
		}
		
		//Positive Z
		if(roomSize.getWidthZ() == 2) {
			relevantLocations.add(center.getRelative(BlockFace.SOUTH, MansionJigsawBuilder.groundFloorRoomWidth));
		}
		
		//Corner for 2x2 rooms
		if(roomSize.getWidthZ() == 2 && roomSize.getWidthX() == 2) {
			relevantLocations.add(center.getRelative(BlockFace.SOUTH_EAST, MansionJigsawBuilder.groundFloorRoomWidth));
		}
		
		//3x3 room
		if(roomSize.getWidthX() == 3 && roomSize.getWidthZ() == 3) {
			for(BlockFace face:BlockUtils.xzPlaneBlockFaces) {
				relevantLocations.add(center.getRelative(face, MansionJigsawBuilder.groundFloorRoomWidth));
			}
		}
		
		int hits = 0;
		//First pass, if any rooms are occupied, return false.
		for(JigsawStructurePiece p:pieces) {
			if(relevantLocations.contains(p.getRoom().getSimpleLocation())) {
				if(((MansionStandardRoomPiece) p).getRoomPopulator() != null)
					return false;
				hits++;
			}
		}
		if(hits < relevantLocations.size()) return false;
		
		//Second pass, set all rooms to occupied. Center room will be set by calling
		//code.
		for(JigsawStructurePiece p:pieces) {
			if(relevantLocations.contains(p.getRoom().getSimpleLocation())) {
				MansionStandardRoomPiece spiece = ((MansionStandardRoomPiece) p);
				spiece.setRoomPopulator(new MansionEmptyRoomPopulator(p.getRoom(), spiece.internalWalls));
				for(BlockFace face:spiece.adjacentPieces.keySet()) {
					if(relevantLocations.contains(spiece.adjacentPieces.get(face).getRoom().getSimpleLocation()))
					{
						spiece.internalWalls.remove(face); //Knock down walls to join rooms
						spiece.adjacentPieces.get(face).internalWalls.remove(face.getOppositeFace());
					}
				}
			}
		}
		
		return true;
	}
}
