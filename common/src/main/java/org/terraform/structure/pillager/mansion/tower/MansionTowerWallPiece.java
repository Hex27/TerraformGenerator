package org.terraform.structure.pillager.mansion.tower;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionTowerWallPiece extends JigsawStructurePiece {

    public boolean isTentRoofFace = false;

    public MansionTowerWallPiece(MansionJigsawBuilder builder,
                                 int widthX,
                                 int height,
                                 int widthZ,
                                 JigsawType type,
                                 BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);

    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getDown();

        for (int i = 0; i < entry.getValue(); i++) {

            // Primary Wall and wooden stair decorations
            // w.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getUp().Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);

            w = w.getLeft();
        }
    }


}
