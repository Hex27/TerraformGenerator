package org.terraform.structure.village.plains.house;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;

public class PlainsVillageHouseJigsawBuilder extends JigsawBuilder {

	public PlainsVillageHouseJigsawBuilder(int widthX, int widthZ, PopulatorDataAbstract data, int x, int y, int z) {
		super(widthX, widthZ, data, x, y, z);
		this.pieceRegistry = new JigsawStructurePiece[] {
				new PlainsVillageLivingRoomPiece(5,4,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageKitchenPiece(5,4,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageLibraryPiece(5,4,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageWallPiece(5,4,5,JigsawType.END,BlockUtils.directBlockFaces)
		};
	}

}
