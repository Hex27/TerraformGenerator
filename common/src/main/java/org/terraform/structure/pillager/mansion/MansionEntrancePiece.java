package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionEntrancePiece extends JigsawStructurePiece {

    public MansionEntrancePiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);

    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {

    	SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        for (int i = 0; i < entry.getValue(); i++) {
            w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(1, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

            w.getRelative(0, 1, 0).Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);
            
            w = w.getLeft();
        }

        //Place doorway
        w = w.getRight(5).getRelative(0, 1, 0);
        
        //Carve empty frame
        w.getRight().Pillar(2, new Random(), Material.AIR);
        w.Pillar(3, new Random(), Material.AIR);        
        w.getLeft().Pillar(2, new Random(), Material.AIR);
        
        //Wall first layer decorations
        w.getFront().getRight(3).Pillar(3, new Random(), Material.DARK_OAK_LOG);
        w.getFront().getLeft(3).Pillar(3, new Random(), Material.DARK_OAK_LOG);

        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.TOP).setFacing(BlockUtils.getRight(w.getDirection()))
        .apply(w.getFront().getRight(2).getRelative(0,2,0))
        .apply(w.getFront().getRight(1).getRelative(0,3,0));

        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.TOP).setFacing(BlockUtils.getLeft(w.getDirection()))
        .apply(w.getFront().getLeft(2).getRelative(0,2,0))
        .apply(w.getFront().getLeft(1).getRelative(0,3,0));

        new OrientableBuilder(Material.DARK_OAK_LOG)
        .setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
        .apply(w.getRelative(0,4,0).getFront())
        .apply(w.getRelative(0,3,0).getFront().getLeft(2))
        .apply(w.getRelative(0,3,0).getFront().getRight(2));
        
        new DirectionalBuilder(Material.STONE_BUTTON)
        .setFacing(w.getDirection())
        .apply(w.getRelative(0,4,0).getFront(2))
        .apply(w.getRelative(0,3,0).getFront(2).getLeft(2))
        .apply(w.getRelative(0,3,0).getFront(2).getRight(2));
        
        new OrientableBuilder(Material.DARK_OAK_LOG)
        .setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())))
        .apply(w.getRelative(0,4,0).getFront().getLeft())
        .apply(w.getRelative(0,4,0).getFront().getRight());
        
        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.BOTTOM).setFacing(BlockUtils.getLeft(w.getDirection()))
        .apply(w.getFront().getRight(3).getRelative(0,3,0))
        .apply(w.getFront().getRight(2).getRelative(0,4,0));

        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.BOTTOM).setFacing(BlockUtils.getRight(w.getDirection()))
        .apply(w.getFront().getLeft(3).getRelative(0,3,0))
        .apply(w.getFront().getLeft(2).getRelative(0,4,0));
        
        //Wall second layer decorations
        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace())
        .apply(w.getRelative(0,3,0));

        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.TOP).setFacing(BlockUtils.getRight(w.getDirection()))
        .apply(w.getRight().getRelative(0,2,0));

        new StairBuilder(Material.DARK_OAK_STAIRS)
        .setHalf(Half.TOP)
        .setFacing(BlockUtils.getLeft(w.getDirection()))
        .apply(w.getLeft().getRelative(0,2,0));
        
        //Inner layer (wall third layer) decorations
        w.getRear().getLeft().Pillar(2, new Random(), Material.STRIPPED_DARK_OAK_LOG);
        w.getRear().getRight().Pillar(2, new Random(), Material.STRIPPED_DARK_OAK_LOG);
        
        new OrientableBuilder(Material.STRIPPED_DARK_OAK_LOG)
        .setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())))
        .apply(w.getRelative(0,2,0).getRear())
        .apply(w.getRelative(0,2,0).getRear().getLeft())
        .apply(w.getRelative(0,2,0).getRear().getRight());
        
        //Stone decorations on the outside
        w.getFront(2).getRight(3).setType(Material.COBBLESTONE);
        w.getFront(2).getLeft(3).setType(Material.COBBLESTONE);
        w.getFront(2).getRight(3).getRelative(0,1,0).setType(Material.COBBLESTONE_WALL);
        w.getFront(2).getLeft(3).getRelative(0,1,0).setType(Material.COBBLESTONE_WALL);
        w.getFront(2).getRight(3).getRelative(0,2,0).setType(Material.LANTERN);
        w.getFront(2).getLeft(3).getRelative(0,2,0).setType(Material.LANTERN);
    }

}
