package org.terraform.structure.village.plains.house;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageEntrancePiece extends JigsawStructurePiece {

	PlainsVillageHouseVariant var;
	public PlainsVillageEntrancePiece(PlainsVillageHouseVariant var,int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		this.var = var;
		
	}

	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		
		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
		Wall w = entry.getKey().getRelative(0,-1,0);
		for(int i = 0; i < entry.getValue(); i++) {
			w.getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.Pillar(2,rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			if(this.var == PlainsVillageHouseVariant.CLAY)
				w.getRelative(0,2,0).Pillar(2, rand, Material.WHITE_TERRACOTTA);
			else
				w.getRelative(0,2,0).Pillar(2, rand, Material.OAK_PLANKS);
			
			w = w.getLeft();
		}
		
		//The door
		w = w.getRight(3).getRelative(0, 1, 0);
		BlockUtils.placeDoor(data, Material.OAK_DOOR, 
				w.getX(), w.getY(), w.getZ(), w.getDirection().getOppositeFace());
		
		BlockUtils.stairwayUntilSolid(
				w.getFront().getRelative(0,-1,0).get(), w.getDirection(),
				new Material[] {Material.COBBLESTONE,Material.MOSSY_COBBLESTONE},
				Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS);
		
		//Decorations depending on variant
		if(this.var == PlainsVillageHouseVariant.COBBLESTONE) {
			w = w.getFront();
			
			//Logs at the front
			w.getLeft().Pillar(2, rand, Material.OAK_LOG);
			w.getRight().Pillar(2, rand, Material.OAK_LOG);
			w.getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.getLeft().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.getRight().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			Orientable log = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
			if(w.getDirection().getModZ() != 0)
				log.setAxis(Axis.X);
			else
				log.setAxis(Axis.Z);
			
			w.getRelative(0,2,0).setBlockData(log);
			w.getRight().getRelative(0,2,0).setBlockData(log);
			w.getLeft().getRelative(0,2,0).setBlockData(log);
			
			w = w.getFront();
			w.getLeft().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.getRight().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			
			new SlabBuilder(Material.COBBLESTONE_SLAB,Material.MOSSY_COBBLESTONE_SLAB)
			.setType(Slab.Type.TOP)
			.apply(w.getRelative(0,2,0));

			new SlabBuilder(Material.COBBLESTONE_SLAB,Material.MOSSY_COBBLESTONE_SLAB)
			.setType(Slab.Type.BOTTOM)
			.apply(w.getRelative(0,2,0).getLeft())
			.apply(w.getRelative(0,2,0).getRight());
			
			w.getLeft().Pillar(2, rand, Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getRight().Pillar(2, rand, Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getLeft().CorrectMultipleFacing(2);
			w.getRight().CorrectMultipleFacing(2);
			
		}else if(this.var == PlainsVillageHouseVariant.CLAY) {
			w.getLeft().getRelative(0,1,0).setType(Material.OAK_LOG);
			w.getRight().getRelative(0,1,0).setType(Material.OAK_LOG);
			w = w.getFront();
			w.getLeft().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.getRight().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			Orientable log = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
			if(w.getDirection().getModZ() != 0)
				log.setAxis(Axis.Z);
			else
				log.setAxis(Axis.X);
			
			w.getLeft().setType(Material.OAK_LOG);
			w.getLeft().getRelative(0,1,0).setType(Material.STONE_BRICK_WALL,Material.MOSSY_STONE_BRICK_WALL);
			w.getLeft().getRelative(0,2,0).setBlockData(log);
			w.getLeft().getRelative(0,1,0).CorrectMultipleFacing(1);

			w.getRight().setType(Material.OAK_LOG);
			w.getRight().getRelative(0,1,0).setType(Material.STONE_BRICK_WALL,Material.MOSSY_STONE_BRICK_WALL);
			w.getRight().getRelative(0,2,0).setBlockData(log);
			w.getRight().getRelative(0,1,0).CorrectMultipleFacing(1);
			
			w.getRelative(0,2,0).setType(BlockUtils.stoneBrickSlabs);
			
			w = w.getFront();
			w.getLeft().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.getRight().getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			
			w.getLeft().setType(Material.STONE_BRICK_WALL,Material.MOSSY_STONE_BRICK_WALL);
			w.getLeft().CorrectMultipleFacing(1);
			w.getRight().setType(Material.STONE_BRICK_WALL,Material.MOSSY_STONE_BRICK_WALL);
			w.getRight().CorrectMultipleFacing(1);
		}else if(this.var == PlainsVillageHouseVariant.WOODEN) {
			Orientable log = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
			if(w.getDirection().getModZ() != 0)
				log.setAxis(Axis.Z);
			else
				log.setAxis(Axis.X);
			
			//w.getRelative(0,2,0).setBlockData(log);
			
			w = w.getFront();
			w.getLeft().getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_LOG);
			w.getRight().getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_LOG);
			
			
			w.getRelative(0,2,0).setBlockData(log);
			
			w.getLeft().setBlockData(log);
			w.getLeft().getRelative(0,1,0).setType(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getLeft().getRelative(0,2,0).setType(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getLeft().getRelative(0,1,0).CorrectMultipleFacing(2);

			w.getRight().setBlockData(log);
			w.getRight().getRelative(0,1,0).setType(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getRight().getRelative(0,2,0).setType(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getRight().getRelative(0,1,0).CorrectMultipleFacing(2);
			
			w = w.getFront();
			w.getLeft().getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_LOG);
			w.getRight().getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_LOG);
			
			
			w.getLeft().setType(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getLeft().CorrectMultipleFacing(1);
			w.getRight().setType(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
			w.getRight().CorrectMultipleFacing(1);
		}
	}

}
