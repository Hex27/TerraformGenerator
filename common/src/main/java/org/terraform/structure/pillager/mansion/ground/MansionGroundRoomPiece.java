package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.block.BlockFace;
import org.terraform.structure.room.jigsaw.JigsawType;

/**
 * This piece is important as it will allow parts of the mansion to have a populator
 * that's empty. This allows larger rooms to generate to occupy the spaces
 */
public class MansionGroundRoomPiece extends MansionStandardGroundRoomPiece {

    public MansionGroundRoomPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }

}
