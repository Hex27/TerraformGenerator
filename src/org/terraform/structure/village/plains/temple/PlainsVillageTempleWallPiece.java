package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageTempleWallPiece extends JigsawStructurePiece {

    public PlainsVillageTempleWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);

    }


    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
        Material[] stoneBricks = {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS};
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        Wall core = null;
        for (int i = 0; i < entry.getValue(); i++) {
            w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(5, rand, stoneBricks);
            if (i == 2) {
                core = w;
                setTempleWindows(w);
            }
            w = w.getLeft();
        }


        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setFacing(core.getDirection().getOppositeFace())
                .apply(core.getFront());
        core.getFront().getRelative(0, -1, 0).downUntilSolid(rand, stoneBricks);

        Wall doorAdj = core.getFront().getRight();
        if (!doorAdj.getRelative(0, 2, 0).getType().isSolid()) {
            new StairBuilder(Material.STONE_BRICK_STAIRS)
                    .setFacing(doorAdj.getDirection().getOppositeFace())
                    .apply(doorAdj.getRelative(0, 2, 0));
            doorAdj.getRelative(0, 1, 0).downUntilSolid(rand, stoneBricks);
        } else {
            doorAdj.getRelative(0, 2, 0).setType(Material.CHISELED_STONE_BRICKS);
            doorAdj.getRelative(0, 3, 0).setType(Material.STONE_BRICK_WALL);
        }

        doorAdj = core.getFront().getLeft();
        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setFacing(doorAdj.getDirection().getOppositeFace())
                .apply(doorAdj.getRelative(0, 2, 0));
        doorAdj.getRelative(0, 1, 0).downUntilSolid(rand, stoneBricks);

    }

    public void setLargeWindow(PopulatorDataAbstract data, BlockFace face) {
    	Wall w = new Wall(new SimpleBlock(data,
    			getRoom().getX(),
    			getRoom().getY()+2,
    			getRoom().getZ()),getRotation());
    	
    	//Place the window
    	w = w.getRelative(getRotation().getOppositeFace(),2).getRelative(face,2);
    	w.Pillar(5, new Random(), Material.YELLOW_STAINED_GLASS);
    	w.getRelative(face.getOppositeFace()).Pillar(5, new Random(), Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
    	w.CorrectMultipleFacing(5);
    	
    	//Decorate the sides
    	w = w.getRelative(face.getOppositeFace()).getFront().getRelative(0,1,0);
    	new SlabBuilder(Material.STONE_BRICK_SLAB).setType(Slab.Type.TOP).apply(w);
    	w = w.getRelative(0,1,0);
    	w.Pillar(2, new Random(), Material.COBBLESTONE_WALL);
    	w.CorrectMultipleFacing(2);
    	w = w.getRelative(0,2,0);
    	new StairBuilder(Material.STONE_BRICK_STAIRS)
    	.setFacing(face)
    	.apply(w)
    	.apply(w.getRelative(0,1,0).getRelative(face));
    	
    }
    
    private void setTempleWindows(Wall w) {
        w = w.getRelative(0, 1, 0);
        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setFacing(w.getDirection().getOppositeFace())
                .apply(w)
                .setHalf(Half.TOP)
                .apply(w.getRelative(0, 3, 0));
        w.getRelative(0, 1, 0).downUntilSolid(new Random(), Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

        w.getRelative(0, 1, 0).Pillar(2, new Random(), Material.YELLOW_STAINED_GLASS_PANE);
        w.getRelative(0, 1, 0).CorrectMultipleFacing(2);

    }
}
