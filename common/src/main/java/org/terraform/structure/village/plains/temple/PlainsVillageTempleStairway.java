package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;

import java.util.Random;

public class PlainsVillageTempleStairway extends JigsawStructurePiece {

    public PlainsVillageTempleStairway(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);

    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, Random rand) {
        this.getRoom().fillRoom(data, new Material[] {Material.YELLOW_STAINED_GLASS});
    }

}
