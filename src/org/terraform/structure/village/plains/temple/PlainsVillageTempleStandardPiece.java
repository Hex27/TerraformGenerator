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

//    private static final ArrayList<Material> BRIGHT_CARPETS = new ArrayList<Material>(){{
//        add(Material.BLUE_CARPET);
//        add(Material.CYAN_CARPET);
//        add(Material.LIGHT_BLUE_CARPET);
//        add(Material.LIME_CARPET);
//        add(Material.MAGENTA_CARPET);
//        add(Material.PINK_CARPET);
//        add(Material.PURPLE_CARPET);
//        add(Material.RED_CARPET);
//        add(Material.YELLOW_CARPET);
//}};
//	
	private boolean isTower = false;
	

    public PlainsVillageTempleStandardPiece(int widthX, int height, int widthZ, JigsawType type, boolean unique, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, unique, validDirs);
    }
	
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
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

//        int[] choices = {-2, -1, 0, 1, 2};
//        int[] steps = new int[3];
//        for (int i = 0; i < 3; i++) steps[i] = choices[random.nextInt(choices.length)];
//        Material other = BRIGHT_CARPETS.get(random.nextInt(BRIGHT_CARPETS.size()));
        //Place carpets.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++)
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
            	if(!data.getType(x, getRoom().getY()+1, z).isSolid())
            		data.setType(x, getRoom().getY()+1, z, Material.WHITE_CARPET);
            	if(x % 2 == 0 && z % 2 == 0)
                	data.setType(x, getRoom().getY(), z, Material.TORCH);
            }
        
        //Commented out because it's ugly af
        //Random pattern on floor
//        for(BlockFace dir:BlockUtils.directBlockFaces) {
//        	Wall core = new Wall(new SimpleBlock(data,getRoom().getX(),getRoom().getY()+1,getRoom().getZ()), dir);
//        	for(int step = 0; step < 3; step++)
//        		core.getFront(step).getLeft(steps[step]).setType(other);
//        }
    }

	public boolean isTower() {
		return isTower;
	}

	public void setTower(boolean isTower) {
		this.isTower = isTower;
	}
}
