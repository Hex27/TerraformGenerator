package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillageTempleStandardPiece extends JigsawStructurePiece {

	private boolean isTower = false;
	
    public PlainsVillageTempleStandardPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        //Place flooring.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++)
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY(), z,
                        GenUtils.randMaterial(
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.CRACKED_STONE_BRICKS
                        ));
                new Wall(new SimpleBlock(data, x, this.getRoom().getY() - 1, z))
                        .downUntilSolid(rand,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.CRACKED_STONE_BRICKS
                        );
            }
    }

    @Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {

    }

	public boolean isTower() {
		return isTower;
	}

	public void setTower(boolean isTower) {
		this.isTower = isTower;
	}
}
