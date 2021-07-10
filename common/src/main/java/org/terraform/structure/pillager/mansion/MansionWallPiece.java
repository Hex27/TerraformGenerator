package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionWallPiece extends JigsawStructurePiece {

    public MansionWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);

    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        
        for (int i = 0; i < entry.getValue(); i++) {
            
        	//Primary Wall and ground beneath wall
        	w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(1, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRelative(0, 1, 0).Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);

            w = w.getLeft();
        }
    }

}
