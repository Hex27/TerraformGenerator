package org.terraform.structure.pillager.mansion.secondfloor;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
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

public class MansionSecondFloorGrandStairwayPopulator extends MansionRoomPopulator {

	public MansionSecondFloorGrandStairwayPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		
	}

	@Override
	public void decorateWall(Random rand, Wall w) {
		//Arch
		
		w.getLeft(3).Pillar(6, Material.DARK_OAK_LOG);
		w.getRelative(0,5,0).setType(Material.DARK_OAK_PLANKS);
		w.getRight(3).Pillar(6, Material.DARK_OAK_LOG);
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setHalf(Half.TOP)
		.setFacing(BlockUtils.getLeft(w.getDirection()))
		.apply(w.getRelative(0,4,0).getLeft(2))
		.apply(w.getRelative(0,5,0).getLeft(2))
		.apply(w.getRelative(0,5,0).getLeft())
		.setFacing(BlockUtils.getRight(w.getDirection()))
		.apply(w.getRelative(0,4,0).getRight(2))
		.apply(w.getRelative(0,5,0).getRight(2))
		.apply(w.getRelative(0,5,0).getRight());
		int choice = rand.nextInt(2);
		switch(choice) {
		case 0: //Wall carving
			w.getRear().Pillar(5, Material.DARK_OAK_LOG);
			new OrientableBuilder(Material.DARK_OAK_LOG)
			.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
			.apply(w.getRear().getRelative(0,2,0))
			.apply(w.getRear().getRelative(0,1,0).getLeft())
			.apply(w.getRear().getRelative(0,1,0).getRight())
			.apply(w.getRear().getRelative(0,3,0).getLeft())
			.apply(w.getRear().getRelative(0,3,0).getRight())
			.setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(w.getDirection())))
			.apply(w.getRear().getRelative(0,2,0).getLeft())
			.apply(w.getRear().getRelative(0,2,0).getLeft(2))
			.apply(w.getRear().getRelative(0,2,0).getRight())
			.apply(w.getRear().getRelative(0,2,0).getRight(2));
			
			for(BlockFace face:BlockUtils.directBlockFaces)
				new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
				.setFacing(face.getOppositeFace())
				.lapply(w.getLeft(3).getRelative(face));
			
			for(BlockFace face:BlockUtils.directBlockFaces)
				new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
				.setFacing(face.getOppositeFace())
				.lapply(w.getRight(3).getRelative(face));
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
		w.getRear().Pillar(6, Material.DARK_OAK_PLANKS);
		w.Pillar(6, Material.DARK_OAK_LOG);
		w.getLeft(3).Pillar(6, Material.DARK_OAK_LOG);
		w.getRight(3).Pillar(6, Material.DARK_OAK_LOG);
		
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(w.getDirection().getOppositeFace())
		.apply(w.getFront())
		.apply(w.getLeft(3).getFront())
		.apply(w.getRight(3).getFront());
		
		new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
		.setFacing(w.getDirection().getOppositeFace())
		.apply(w.getLeft())
		.apply(w.getLeft(2))
		.apply(w.getRight())
		.apply(w.getRight(2));
	}
	@Override
	public MansionRoomSize getSize() {
		return new MansionRoomSize(3,3);
	}

	@Override
	public int[] getSpawnLocation() {
		return new int[] {getRoom().getX(), getRoom().getY()-7, getRoom().getZ()};
	}
}
