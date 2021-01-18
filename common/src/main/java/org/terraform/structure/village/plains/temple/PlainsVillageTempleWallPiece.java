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
import java.util.ArrayList;
import java.util.Random;

public class PlainsVillageTempleWallPiece extends JigsawStructurePiece {


    private static final ArrayList<Material> BRIGHT_STAINED_GLASS_PANES = new ArrayList<Material>(){{
            add(Material.BLUE_STAINED_GLASS_PANE);
            add(Material.CYAN_STAINED_GLASS_PANE);
            add(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            add(Material.LIME_STAINED_GLASS_PANE);
            add(Material.MAGENTA_STAINED_GLASS_PANE);
            add(Material.PINK_STAINED_GLASS_PANE);
            add(Material.PURPLE_STAINED_GLASS_PANE);
            add(Material.RED_STAINED_GLASS_PANE);
            add(Material.YELLOW_STAINED_GLASS_PANE);
    }};

	
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
    	Material pane;
    	Wall w = new Wall(new SimpleBlock(data,
    			getRoom().getX(),
    			getRoom().getY()+2,
    			getRoom().getZ()),getRotation());
    	w = w.getRelative(getRotation().getOppositeFace(),2).getRelative(face,2);
    	
    	//Remove roof ledge for windows
    	w.getRear().getRelative(0,3,0).Pillar(3,new Random(),Material.AIR);
    	w.getRear(2).getRelative(0,3,0).Pillar(4,new Random(),Material.AIR);
    	
    	//Interior stair decor
    	new StairBuilder(Material.POLISHED_DIORITE_STAIRS)
    	.setHalf(Half.TOP)
    	.setFacing(w.getDirection())
    	.apply(w.getRear().getRelative(0,5,0))
    	.apply(w.getRear(2).getRelative(0,6,0));
    	
    	//Place the window
    	if(BRIGHT_STAINED_GLASS_PANES.contains(w.getRelative(face).getType())) {
    		pane = w.getRelative(face).getType();
    	}else
    		pane = BRIGHT_STAINED_GLASS_PANES.get(new Random().nextInt(BRIGHT_STAINED_GLASS_PANES.size()));
    	
    	w.Pillar(5, new Random(), pane);
    	w.getRelative(0,-1,0).getRelative(face.getOppositeFace()).Pillar(6, new Random(), Material.POLISHED_DIORITE);//Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
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
    	.apply(w.getRear().getRelative(0,1,0))
    	.apply(w.getRear(2).getRelative(0,2,0))
    	.apply(w.getRelative(0,1,0).getRelative(face))
    	.apply(w.getRear().getRelative(0,1,0).getRelative(face))
    	.apply(w.getRear().getRelative(0,2,0).getRelative(face))
    	.apply(w.getRear(2).getRelative(0,3,0).getRelative(face));
    	
    	w.getRelative(0,1,0).getRelative(face).getRear().setType(Material.CHISELED_STONE_BRICKS);
    	w.getRelative(0,1,0).getRear(2).setType(Material.CHISELED_STONE_BRICKS);
    	w.getRelative(0,2,0).getRelative(face).getRear(2).setType(Material.CHISELED_STONE_BRICKS);
    	w.getRelative(0,3,0).getRelative(face).getRear(3).setType(Material.CHISELED_STONE_BRICKS);
    	
    	
    }
    
    private void setTempleWindows(Wall w) {
    	Material pane = BRIGHT_STAINED_GLASS_PANES.get(new Random().nextInt(BRIGHT_STAINED_GLASS_PANES.size()));
        w = w.getRelative(0, 1, 0);
        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setFacing(w.getDirection().getOppositeFace())
                .apply(w)
                .setHalf(Half.TOP)
                .apply(w.getRelative(0, 3, 0));
        w.getRelative(0, 1, 0).downUntilSolid(new Random(), Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

        w.getRelative(0, 1, 0).Pillar(2, new Random(), pane);
        w.getRelative(0, 1, 0).CorrectMultipleFacing(2);

    }
}
