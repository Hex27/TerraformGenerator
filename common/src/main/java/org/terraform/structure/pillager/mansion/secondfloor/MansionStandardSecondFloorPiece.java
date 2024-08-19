package org.terraform.structure.pillager.mansion.secondfloor;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.pillager.mansion.MansionStandardRoomPiece;
import org.terraform.structure.room.jigsaw.JigsawType;

import java.util.Random;

public class MansionStandardSecondFloorPiece extends MansionStandardRoomPiece {

    public MansionStandardSecondFloorPiece(MansionJigsawBuilder builder,
                                           int widthX,
                                           int height,
                                           int widthZ,
                                           JigsawType type,
                                           BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        // Place attic ceiling
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY() + this.getRoom().getHeight(), z, Material.DARK_OAK_PLANKS);

            }
        }
    }


}
