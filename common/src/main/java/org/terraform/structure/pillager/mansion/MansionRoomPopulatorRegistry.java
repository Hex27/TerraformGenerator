package org.terraform.structure.pillager.mansion;

import java.util.ArrayList;

import org.terraform.structure.pillager.mansion.ground.*;
import org.terraform.structure.pillager.mansion.secondfloor.*;

public enum MansionRoomPopulatorRegistry {
	
	GROUND_3_3(new MansionGrandStairwayPopulator(null,null)), //
	GROUND_2_2(
			new MansionGroundLevelLibraryPopulator(null,null), 
			new MansionGroundLevelWarroomPopulator(null,null)),
	GROUND_1_2(
			new MansionGroundLevelKitchenPopulator(null,null),
			new MansionGroundLevelMushroomFarmPopulator(null,null),
			new MansionGroundLevelForgePopulator(null,null)),
	GROUND_2_1(
			new MansionGroundLevelDiningRoomPopulator(null,null),
			new MansionGroundLevelBrewingRoomPopulator(null,null)),
	GROUND_1_1(new MansionGroundFloorHallwayPopulator(null,null)),

	SECOND_3_3(new MansionGrandStairwayPopulator(null,null)),
	SECOND_2_2(
			new MansionSecondFloorBedroomPopulator(null,null),
			new MansionSecondFloorStudyPopulator(null,null)),
	SECOND_1_2(
			new MansionSecondFloorLoungePopulator(null,null),
			new MansionSecondFloorPianoRoomPopulator(null,null)),
	SECOND_2_1(
			new MansionSecondFloorBunkPopulator(null,null),
			new MansionSecondFloorStoreroomPopulator(null,null)),
	SECOND_1_1(new MansionSecondFloorHallwayPopulator(null,null)),
	
	;
	ArrayList<MansionRoomPopulator> populators = new ArrayList<>();
	MansionRoomPopulatorRegistry(MansionRoomPopulator... populators){
		for(MansionRoomPopulator populator:populators) {
			this.populators.add(populator);
		}
	}
	
	public ArrayList<MansionRoomPopulator> getPopulators(){
		ArrayList<MansionRoomPopulator> clone = new ArrayList<>();
		clone.addAll(populators);
		return clone;
	}
	
	public static MansionRoomPopulatorRegistry getByRoomSize(MansionRoomSize size, boolean isGround) {
		if(isGround) {
			if(size.getWidthX() == 3 && size.getWidthZ() == 3)
				return MansionRoomPopulatorRegistry.GROUND_3_3;
			if(size.getWidthX() == 2 && size.getWidthZ() == 2)
				return MansionRoomPopulatorRegistry.GROUND_2_2;
			if(size.getWidthX() == 1 && size.getWidthZ() == 2)
				return MansionRoomPopulatorRegistry.GROUND_1_2;
			if(size.getWidthX() == 2 && size.getWidthZ() == 1)
				return MansionRoomPopulatorRegistry.GROUND_2_1;
			if(size.getWidthX() == 1 && size.getWidthZ() == 1)
				return MansionRoomPopulatorRegistry.GROUND_1_1;
		}
		else{
			if(size.getWidthX() == 3 && size.getWidthZ() == 3)
				return MansionRoomPopulatorRegistry.SECOND_3_3;
			if(size.getWidthX() == 2 && size.getWidthZ() == 2)
				return MansionRoomPopulatorRegistry.SECOND_2_2;
			if(size.getWidthX() == 1 && size.getWidthZ() == 2)
				return MansionRoomPopulatorRegistry.SECOND_1_2;
			if(size.getWidthX() == 2 && size.getWidthZ() == 1)
				return MansionRoomPopulatorRegistry.SECOND_2_1;
			if(size.getWidthX() == 1 && size.getWidthZ() == 1)
				return MansionRoomPopulatorRegistry.SECOND_1_1;
		}
		return null;
	}
}
