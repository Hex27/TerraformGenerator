package org.terraform.structure.village.plains.house;

import java.util.Random;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Switch;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class PlainsVillageKitchenPiece extends PlainsVillageStandardPiece {

	public PlainsVillageKitchenPiece(PlainsVillageHouseVariant variant, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(variant, widthX, height, widthZ, type, validDirs);
	}
	
	@Override
	public void postBuildDecoration(Random random,PopulatorDataAbstract data) {
		super.postBuildDecoration(random, data);
		
		//No walls :V
		if(this.getWalledFaces().size() == 0) {
			return;
		}
		
		//Pick a random walled face to be the primary wall, where all the stuff goes.
		BlockFace primaryWall = this.getWalledFaces().get(random.nextInt(this.getWalledFaces().size()));
		SimpleBlock core = new SimpleBlock(data,this.getRoom().getX(),this.getRoom().getY()+1,this.getRoom().getZ());
		int numUtilities = 5;
		if(core.getRelative(primaryWall,3).getType() == Material.OAK_DOOR) {
			numUtilities--;
		}
		
		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, primaryWall, 0);
		Wall w = entry.getKey();
		ArrayList<Material> utilities = new ArrayList<Material>() {{
			add(Material.SMOKER);
		}};
		for(int i = 0; i < numUtilities; i++) {
			utilities.add(GenUtils.randMaterial(random,Material.HOPPER,Material.FURNACE,Material.CRAFTING_TABLE));
		}
		Collections.shuffle(utilities);
		for(int i = 0; i < entry.getValue(); i++) {
			if(w.getRear().getType() != Material.OAK_DOOR) {
				numUtilities--;
				Material mat = utilities.get(numUtilities);
				switch(mat) {
				case HOPPER:
					w.setType(mat);
					if(w.getRear().getRelative(0,1,0).getType() != Material.GLASS_PANE) {
						Switch lever = (Switch) Bukkit.createBlockData(Material.LEVER);
						lever.setAttachedFace(AttachedFace.WALL);
						lever.setFacing(w.getDirection());
						w.getRelative(0,1,0).setBlockData(lever);
					}
					break;
				case FURNACE: //Furnace and smoker handled the same way
				case SMOKER:
					new DirectionalBuilder(mat)
					.setFacing(w.getDirection()).apply(w);
					w.getRelative(0,1,0).setType(Material.OAK_PRESSURE_PLATE);
					
					new StairBuilder(Material.BRICK_STAIRS)
					.setFacing(w.getDirection().getOppositeFace())
					.setHalf(Bisected.Half.TOP)
					.apply(w.getRelative(0,2,0));
					
					Wall chimneyWall = w.getRelative(0,3,0);
					boolean hitCeiling = false;
					int chimneyHeight = 0;
					while(chimneyHeight < 4) {
						if(chimneyWall.getType().isSolid()) {
							hitCeiling = true;
						}else if(hitCeiling){
							chimneyHeight++;
							if(GenUtils.chance(random,chimneyHeight,3)) break;
						}
						chimneyWall.setType(Material.BRICKS);
						
						chimneyWall = chimneyWall.getRelative(0,1,0);
					}
					chimneyWall.setType(Material.BRICK_WALL);
					break;
				case CRAFTING_TABLE:
					w.setType(mat);
					break;
				default:
					break;
				}
			}
			w = w.getLeft();
		}
		
		//Other walls can be decorated with random and loot
		//Populate for walled areas
		for(BlockFace face:this.getWalledFaces()) {
			if(face == primaryWall) continue;
			entry = this.getRoom().getWall(data, face, 0);
			w = entry.getKey();
			
			for(int i = 0; i < entry.getValue(); i++) {
				if(w.getRear().getType() != Material.OAK_DOOR
					&& !w.getType().isSolid()) {
					int decor = random.nextInt(5);
					switch(decor) {
					case 0: //Counter
						new StairBuilder(Material.STONE_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.OAK_STAIRS)
						.setFacing(w.getDirection().getOppositeFace())
						.setHalf(Half.TOP)
						.apply(w);
						break;
					case 1: //Solid counter or other random solid blocks
						w.setType(
								Material.SMOOTH_STONE,
								Material.POLISHED_ANDESITE,
								Material.PUMPKIN,
								Material.DRIED_KELP_BLOCK,
								Material.MELON);
						break;
					case 2: //Random loot
						new ChestBuilder(Material.CHEST)
						.setFacing(w.getDirection())
						.setLootTable(TerraLootTable.VILLAGE_BUTCHER,TerraLootTable.VILLAGE_PLAINS_HOUSE);
					default: //Do nothing
						break;
					}
				}
				w = w.getLeft();
			}
		}
		
	}

	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		//this.getRoom().fillRoom(data, new Material[] {Material.BLUE_STAINED_GLASS});
		super.build(data, rand);
	}

}
