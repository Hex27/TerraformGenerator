package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.ArmorStandUtils;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionGrandStairwayPopulator extends MansionRoomPopulator {

	public MansionGrandStairwayPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		SimpleBlock target = this.getRoom().getCenterSimpleBlock(data);
		try {
			TerraSchematic schema = TerraSchematic.load("mansion/mansion-stairway", target);
	        //schema.parser = new MansionRoomSchematicParser();
	        schema.setFace(BlockUtils.getDirectBlockFace(random));
	        schema.apply();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decorateWall(Random rand, Wall w) {
		//Arch
		
		w.getLeft(3).Pillar(7, Material.DARK_OAK_LOG);
		w.getRelative(0,6,0).setType(Material.DARK_OAK_PLANKS);
		w.getRight(3).Pillar(7, Material.DARK_OAK_LOG);
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setHalf(Half.TOP)
		.setFacing(BlockUtils.getLeft(w.getDirection()))
		.apply(w.getRelative(0,5,0).getLeft(2))
		.apply(w.getRelative(0,6,0).getLeft(2))
		.apply(w.getRelative(0,6,0).getLeft())
		.setFacing(BlockUtils.getRight(w.getDirection()))
		.apply(w.getRelative(0,5,0).getRight(2))
		.apply(w.getRelative(0,6,0).getRight(2))
		.apply(w.getRelative(0,6,0).getRight());
		int choice = rand.nextInt(2);
		switch(choice) {
		case 0: //Wall carving
			w.getRear().getRelative(0,1,0).Pillar(5, Material.DARK_OAK_LOG);
			new OrientableBuilder(Material.DARK_OAK_LOG)
			.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
			.apply(w.getRear().getRelative(0,3,0))
			.apply(w.getRear().getRelative(0,2,0).getLeft())
			.apply(w.getRear().getRelative(0,2,0).getRight())
			.apply(w.getRear().getRelative(0,4,0).getLeft())
			.apply(w.getRear().getRelative(0,4,0).getRight())
			.setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(w.getDirection())))
			.apply(w.getRear().getRelative(0,3,0).getLeft())
			.apply(w.getRear().getRelative(0,3,0).getLeft(2))
			.apply(w.getRear().getRelative(0,3,0).getRight())
			.apply(w.getRear().getRelative(0,3,0).getRight(2));
			
			break;
		default: //Armor stands
			
			BannerUtils.generatePillagerBanner(w.getRelative(0,4,0).get(), w.getDirection(), true);
			
			new SlabBuilder(Material.POLISHED_ANDESITE_SLAB)
			.setType(Type.TOP)
			.apply(w)
			.apply(w.getLeft())
			.apply(w.getLeft(2))
			.apply(w.getRight())
			.apply(w.getRight(2));
			
			ArmorStandUtils.placeArmorStand(w.getRelative(0,2,0).get(), w.getDirection(), rand);
			ArmorStandUtils.placeArmorStand(w.getRelative(0,2,0).getLeft(2).get(), w.getDirection(), rand);
			ArmorStandUtils.placeArmorStand(w.getRelative(0,2,0).getRight(2).get(), w.getDirection(), rand);
			
			break;
		}
	}
	
	@Override
	public void decorateWindow(Random rand, Wall w) {
		
		Entry<Wall, Integer> entry = this.getRoom().getWall(w.get().getPopData(), w.getDirection().getOppositeFace(), 0);
		w = entry.getKey();
		for(int i = 0; i < entry.getValue(); i++) {
			if(w.getFront().getRelative(0,6,0).getType() == Material.DARK_OAK_SLAB) {
				w.getRear().Pillar(7, Material.DARK_OAK_PLANKS);
				w.Pillar(7, Material.DARK_OAK_LOG);
				new StairBuilder(Material.DARK_OAK_STAIRS)
				.setFacing(w.getDirection().getOppositeFace())
				.apply(w.getFront());
			}
			w = w.getLeft();
		}
	}

	@Override
	public MansionRoomSize getSize() {
		return new MansionRoomSize(3,3);
	}
	
}
