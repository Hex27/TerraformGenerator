package org.terraform.structure.pillager.mansion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
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
        w = w.getRight(4).getRelative(0, 1, 0);
        
        //Carve empty frame
        w.getRight().Pillar(2, new Random(), Material.AIR);
        //w.Pillar(3, new Random(), Material.AIR);
        
        //for (int i = 0; i < 3; i++) {
        	//TerraformGeneratorPlugin.logger.info(w.getRelative(0, i, 0).get().toString());
            Block b = Bukkit.getWorld("world").getBlockAt(w.getX(),w.getY()+2,w.getZ());
            b.setType(Material.RED_WOOL);
            b.getRelative(0,1,0).setType(Material.GREEN_WOOL);
            b.getRelative(0,3,0).setType(Material.YELLOW_WOOL);
            b.getRelative(0,2,0).setType(Material.BLUE_WOOL);
        //}
        
        w.getLeft().Pillar(2, new Random(), Material.AIR);
        
//        new StairBuilder(Material.DARK_OAK_STAIRS)
//        .setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace())
//        .apply(w.getRelative(0,3,0));
//
//        new StairBuilder(Material.DARK_OAK_STAIRS)
//        .setHalf(Half.TOP).setFacing(BlockUtils.getRight(w.getDirection()))
//        .apply(w.getRight().getRelative(0,2,0));
//
//        new StairBuilder(Material.DARK_OAK_STAIRS)
//        .setHalf(Half.TOP)
//        .setFacing(BlockUtils.getLeft(w.getDirection()))
//        .apply(w.getLeft().getRelative(0,2,0));
//        
//        w.getRear().getLeft().Pillar(2, Material.STRIPPED_DARK_OAK_LOG);
//        w.getRear().getRight().Pillar(2, Material.STRIPPED_DARK_OAK_LOG);
//        
//        new OrientableBuilder(Material.STRIPPED_DARK_OAK_LOG)
//        .setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())))
//        .apply(w.getRelative(0,2,0).getRear())
//        .apply(w.getRelative(0,2,0).getRear().getLeft())
//        .apply(w.getRelative(0,2,0).getRear().getRight());
    }

}
